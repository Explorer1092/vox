package com.voxlearning.utopia.schedule.schedule.teachertask;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateFormatUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.StopWatch;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityCardService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCard;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCardOrder;
import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Named
@ScheduledJobDefinition(
        jobName = "老师集卡片活动详细统计任务",
        jobDescription = "老师集卡片活动详细统计任务",
        disabled = {Mode.STAGING, Mode.DEVELOPMENT, Mode.TEST},
        cronExpression = "0 5 0 * * ?",
        ENABLED = false
)
public class ActivityCardStatisticsDataJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = TeacherActivityCardService.class)
    private TeacherActivityCardService teacherActivityCardService;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        StopWatch started = StopWatch.createStarted();

        Long startId = 0L;
        Integer pageSize = 10000;

        List<TeacherActivityCard> daUserList = new ArrayList<>();
        Map<Long, Long> cardCountMap = new HashMap<>();
        Map<Integer, Long> typeCountMap = new HashMap<>();

        while (true) {
            try {
                List<TeacherActivityCard> teacherActivityCards = teacherActivityCardService.loadCard(startId, pageSize);
                if (teacherActivityCards.isEmpty()) {
                    break;
                }

                for (TeacherActivityCard userCard : teacherActivityCards) {
                    startId = userCard.getUserId();

                    CardData cardData = getCardData(userCard.getCards());
                    if (cardData.containsDa) {
                        daUserList.add(userCard);
                    }
                    cardCountMap.compute(cardData.getCardCount(), (key, oldValue) -> {
                        if (oldValue == null) return 1L;
                        return ++oldValue;
                    });
                    typeCountMap.compute(cardData.getTypeCount(), (key, oldValue) -> {
                        if (oldValue == null) return 1L;
                        return ++oldValue;
                    });
                }
            } catch (Exception e) {
                logger.error("ActivityCardStatisticsDataJob exception, startId = " + startId, e);
            }
        }

        StringBuilder mailContent = new StringBuilder();

        List<Integer> type = new ArrayList<>(typeCountMap.keySet());
        Collections.sort(type);
        for (Integer typeName : type) {
            mailContent.append("手中有 ").append(typeName).append(" 种卡片的用户数量").append("：").append(typeCountMap.get(typeName)).append("\n");
        }
        mailContent.append("\n");

        List<Long> count = new ArrayList<>(cardCountMap.keySet());
        Collections.sort(count);
        for (Long countName : count) {
            mailContent.append("手中有 ").append(countName).append(" 张卡片的用户数量").append("：").append(cardCountMap.get(countName)).append("\n");
        }
        mailContent.append("\n");

        for (TeacherActivityCard activityCard : daUserList) {
            List<TeacherActivityCard.Card> cards = activityCard.getCards();
            CardData cardData = getCardData(cards);
            mailContent.append("用户：").append(activityCard.getUserId()).append("  ");
            mailContent.append("卡片种类：").append(cardData.getTypeCount()).append("  ");
            mailContent.append("卡片数量：").append(cardData.getCardCount()).append("  ");

            Map<String, Integer> map = cards.stream()
                    .filter(i -> !i.getDisabled())
                    .collect(groupingBy(TeacherActivityCard.Card::getType, Collectors.summingInt(p -> 1)));
            mailContent.append("卡片详情：").append(map.toString()).append("\n");
        }

        try {
            mailContent.append("\n兑换大礼包的老师：\n");
            List<TeacherActivityCardOrder> orders = teacherActivityCardService.loadCardOrder();
            Set<Long> teacherIdSet = orders.stream().map(TeacherActivityCardOrder::getUserId).collect(toSet());
            Map<Long, TeacherDetail> teacherDetailMap = teacherLoaderClient.loadTeacherDetails(teacherIdSet);

            for (TeacherActivityCardOrder order : orders) {
                Long userId = order.getUserId();
                TeacherDetail teacherDetail = teacherDetailMap.get(userId);
                if (teacherDetail != null) {
                    mailContent.append(userId).append(" ")
                            .append(Optional.ofNullable(teacherDetail.getProfile()).get().getRealname())
                            .append(" ")
                            .append(DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"))
                            .append("\n");
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        emailServiceClient.createPlainEmail()
                .body(mailContent.toString())
                .subject("【" + RuntimeMode.current() + "】" + "寒假作业集卡活动详细报告")
                .to(RuntimeMode.isProduction() ? "junbao.zhang@17zuoye.com;cong.yu@17zuoye.com;te.wang@17zuoye.com" : "junbao.zhang@17zuoye.com")
                .send();

        started.stop();
        logger.info("ActivityCardStatisticsDataJob 耗时: {} s", started.getTime(TimeUnit.SECONDS));
    }

    private CardData getCardData(List<TeacherActivityCard.Card> cards) {
        long daCount = cards.stream().filter(i -> !i.getDisabled() && i.getType().equals(ActivityCardEnum.da.name())).count();
        int typeCount = cards.stream().filter(i -> !i.getDisabled()).map(TeacherActivityCard.Card::getType).collect(toSet()).size();
        long cardCount = cards.stream().filter(i -> !i.getDisabled()).count();

        return new CardData(daCount > 0, typeCount, cardCount);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class CardData {
        private boolean containsDa;
        private Integer typeCount;
        private Long cardCount;
    }
}