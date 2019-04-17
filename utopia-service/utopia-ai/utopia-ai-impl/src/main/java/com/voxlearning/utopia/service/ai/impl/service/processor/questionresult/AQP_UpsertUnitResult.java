package com.voxlearning.utopia.service.ai.impl.service.processor.questionresult;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.support.MessageConfig;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2018/3/29
 * 是否计算单元结果
 */
@Named
public class AQP_UpsertUnitResult extends AbstractAiSupport implements IAITask<AIUserQuestionContext> {

    @Override
    public void execute(AIUserQuestionContext context) {
        if (!context.getAiUserQuestionResultRequest().getLessonLast()) {
            return;
        }

        Long userId = context.getUser().getId();
        String unitId = context.getAiUserQuestionResultRequest().getUnitId();

        // 课程结束 判断是否已有单元结果
        AIUserUnitResultHistory unitResultHistory = aiUserUnitResultHistoryDao.load(userId, unitId);
        // 已有单元结果， 如果课程完成的星星有变化， 重新计算单元结果
        // 没有单元结果， 学生第一次完成 计算单元结果
        if ((unitResultHistory != null) || (context.getAiUserQuestionResultRequest().getUnitLast())) {

            AIUserUnitResultHistory unitResult = calUnitResult(userId, unitId);
            unitResult.setId(null);
            unitResult.setDisabled(false);
            aiUserUnitResultHistoryDao.disableOld(userId, unitId);
            aiUserUnitResultHistoryDao.insert(unitResult);
            unitResultHistory = unitResult;
        }

        if (!context.getAiUserQuestionResultRequest().getUnitLast()) {
            context.terminateTask();
            return;
        }

        AIUserUnitResultPlan aiUserUnitResultPlan = aiUserUnitResultPlanDao.load(AIUserUnitResultPlan.generateId(context.getUser().getId(),
                context.getAiUserQuestionResultRequest().getUnitId()));
        List<AIUserUnitResultPlan> unitResultPlans = aiUserUnitResultPlanDao.loadByUserId(context.getUser().getId()).stream().filter(e -> !chipsContentService.isTrailUnit(e.getUnitId())).collect(Collectors.toList());
        if (aiUserUnitResultPlan == null) {
            aiUserUnitResultPlan = AIUserUnitResultPlan.initPlan(unitResultHistory);
            aiUserUnitResultPlan.setGrade(grade(aiUserUnitResultPlan.getScore()));
            aiUserUnitResultPlan.setPointAbility(pointAbility(unitResultPlans, aiUserUnitResultPlan));
            aiUserUnitResultPlan.setStudyPlan(summary(aiUserUnitResultPlan.getPointAbility()));
            Integer rank = Optional.ofNullable(newContentLoaderClient.loadBookCatalogByCatalogId(aiUserUnitResultPlan.getUnitId()))
                    .filter(e -> e.getRank() != null)
                    .map(NewBookCatalog::getRank)
                    .orElse(0);
            aiUserUnitResultPlan.setRank(rank);
            aiUserUnitResultPlanDao.insert(aiUserUnitResultPlan);
            context.getAiUserUnitResultPlans().add(aiUserUnitResultPlan);
            sendWechatTemplateMessageNotify(aiUserUnitResultPlan);
        }

        if (CollectionUtils.isNotEmpty(unitResultPlans)) {
            context.getAiUserUnitResultPlans().addAll(unitResultPlans);
        }
    }

    private AIUserUnitResultHistory calUnitResult(Long userId, String unitId) {
        List<AIUserLessonResultHistory> lessonResultHistoryList = aiUserLessonResultHistoryDao.loadByUserIdAndUnitId(userId, unitId);

        int totalStar = lessonResultHistoryList.stream().mapToInt(AIUserLessonResultHistory::getStar).sum();

        // 计算单元星星
        int unitStar = new BigDecimal(totalStar).divide(new BigDecimal(lessonResultHistoryList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();

        int totalScore = lessonResultHistoryList.stream().mapToInt(AIUserLessonResultHistory::getScore).sum();

        // 计算单元分数
        int unitSCore = new BigDecimal(totalScore).divide(new BigDecimal(lessonResultHistoryList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();

        // 计算能力概况
        List<AIUserQuestionResultHistory> questionResultHistoryList = aiUserQuestionResultHistoryDao.loadByUidAndUnitId(userId, unitId);

        // 发音--这里有个坑:发音APP传来的分数是8分制的需要转一下
        int t_p_score = questionResultHistoryList.stream().mapToInt(e -> {
            if (Integer.compare(e.getPronunciation(), 8) <= 0) {
                return new BigDecimal(e.getPronunciation()).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            }
            return e.getPronunciation();
        }).sum();
        int p_score = new BigDecimal(t_p_score).divide(new BigDecimal(questionResultHistoryList.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        p_score = Math.min(100, Math.max(60, p_score));

        // 过滤出情景对话跟任务的题目
        List<AIUserQuestionResultHistory> subHis = questionResultHistoryList.stream().filter(h -> h.getLessonType() != LessonType.WarmUp)
                .collect(Collectors.toList());

        // 流利度--去掉热身环节的打分
        int t_f_score = subHis.stream().mapToInt(AIUserQuestionResultHistory::getFluency).sum();
        int f_score = new BigDecimal(t_f_score).divide(new BigDecimal(subHis.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        f_score = Math.max(60, f_score);

        // 回答总得分
        int t_q_score = subHis.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum();
        // 总扣分
        int t_deduct_score = subHis.stream().mapToInt(AIUserQuestionResultHistory::getDeductScore).sum();
        // 完整性总得分
        int t_c_score = subHis.stream().mapToInt(AIUserQuestionResultHistory::getCompleteScore).sum();

        // 听力
        int l_score = new BigDecimal(t_q_score).divide(new BigDecimal(subHis.size()), 0, BigDecimal.ROUND_HALF_UP).intValue() - t_deduct_score;
        l_score = Math.max(60, l_score);

        // 表达
        int e_score = new BigDecimal(t_q_score).divide(new BigDecimal(subHis.size()), 0, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP).intValue() +
                new BigDecimal(t_c_score).divide(new BigDecimal(subHis.size()), 0, BigDecimal.ROUND_HALF_UP)
                        .divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP).intValue();
        e_score = Math.max(60, e_score);
        // 独立完成
        int i_score = 100 - t_deduct_score;
        i_score = Math.max(60, i_score);

        // 薄弱项
        List<QuestionWeekPoint> allWeekPoints = new ArrayList<>();
        Set<String> words = new HashSet<>();
        for (AIUserQuestionResultHistory qh : questionResultHistoryList) {
            if (qh == null || CollectionUtils.isEmpty(qh.getWeekPoints())) {
                continue;
            }
            for (QuestionWeekPoint weekPoint : qh.getWeekPoints()) {
                if (words.contains(weekPoint.getContent())) {
                    continue;
                }
                allWeekPoints.add(weekPoint);
                words.add(weekPoint.getContent());
            }
        }

        AIUserUnitResultHistory newUnitResult = new AIUserUnitResultHistory();
        newUnitResult.setUnitId(unitId);
        newUnitResult.setUserId(userId);
        // 获取bookId
        NewBookCatalog unitCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unitCatalog != null && StringUtils.isNotBlank(unitCatalog.bookId())) {
            newUnitResult.setBookId(unitCatalog.bookId());
        }
        newUnitResult.setStar(unitStar);
        newUnitResult.setScore(unitSCore);
        newUnitResult.setExpress(e_score);
        newUnitResult.setFluency(f_score);
        newUnitResult.setIndependent(i_score);
        newUnitResult.setListening(l_score);
        newUnitResult.setPronunciation(p_score);
        newUnitResult.setWeekPoints(allWeekPoints);
        newUnitResult.setFinished(true);
        newUnitResult.setUpdateDate(new Date());

        return newUnitResult;
    }

    private AIUserUnitResultPlan.Grade grade(Integer score) {
        if (score == null) {
            if (RuntimeMode.lt(Mode.PRODUCTION)) {
                logger.error("score is null");
            }
            return AIUserUnitResultPlan.Grade.C;
        }
        if (score >= 90) {
            return AIUserUnitResultPlan.Grade.A;
        }
        if (score > 40) {
            return AIUserUnitResultPlan.Grade.B;
        }
        return AIUserUnitResultPlan.Grade.C;
    }

    private AIUserUnitResultPlan.Ability pointAbility(List<AIUserUnitResultPlan> unitResultPlans, AIUserUnitResultPlan aiUserUnitResultPlan) {
        List<AbilityBean> abilityBeans = new ArrayList<>();
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Independent, aiUserUnitResultPlan.getIndependent()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Express, aiUserUnitResultPlan.getExpress()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Listening, aiUserUnitResultPlan.getListening()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Pronunciation, aiUserUnitResultPlan.getPronunciation()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Fluency, aiUserUnitResultPlan.getFluency()));
        abilityBeans.sort((e1, e2) -> e1.score.compareTo(e2.getScore()));
        if (CollectionUtils.isEmpty(unitResultPlans)) {
            return abilityBeans.get(0).getPointAbility();
        }
        unitResultPlans.sort((e1, e2) -> e2.getCreateDate().compareTo(e1.getCreateDate()));

        AbilityBean bean = abilityBeans.stream().filter(e -> unitResultPlans.get(0).getPointAbility() != e.getPointAbility() &&
                (unitResultPlans.size() == 1 || (unitResultPlans.size() > 1 && unitResultPlans.get(1).getPointAbility() != e.getPointAbility())))
                .findFirst().orElse(null);
        return bean == null ? abilityBeans.get(0).getPointAbility() : bean.getPointAbility();
    }

    private String summary(AIUserUnitResultPlan.Ability ability) {
        Random random = new Random();
        int index = random.nextInt(2);
        switch (ability) {
            case Express:
                return MessageConfig.unit_plan_express[index];
            case Fluency:
                return MessageConfig.unit_plan_fluency[index];
            case Independent:
                return MessageConfig.unit_plan_independent[index];
            case Listening:
                return MessageConfig.unit_plan_listening[index];
            case Pronunciation:
                return MessageConfig.unit_plan_production[index];
        }
        return "";
    }

    @Deprecated
    private void sendWechatTemplateMessageNotify(AIUserUnitResultPlan aiUserUnitResultPlan) {
        Date now = new Date();
        OrderProduct orderProduct = userOrderLoaderClient.loadAvailableProduct().stream().filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                .filter(e -> {
                    Map<String, Object> map = JsonUtils.fromJson(e.getAttributes());
                    if (MapUtils.isEmpty(map) || map.get("beginDate") == null || map.get("endDate") == null) {
                        return false;
                    }
                    Date beginDate = SafeConverter.toDate(map.get("beginDate"));
                    Date endDate = SafeConverter.toDate(map.get("endDate"));
                    if (beginDate == null || endDate == null) {
                        return false;
                    }
                    return now.after(beginDate) && now.before(endDate);
                })
                .findFirst()
                .orElse(null);
        if (orderProduct == null) {
            return;
        }

        UserOrder userOrder = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), aiUserUnitResultPlan.getUserId()).stream().filter(e -> OrderProductServiceType.safeParse(e.getOrderProductServiceType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> orderProduct.getId().equals(e.getProductId())).findFirst().orElse(null);

        if (userOrder == null) {
            return;
        }

        Date begin = Optional.ofNullable(orderProduct).map(e -> JsonUtils.fromJson(e.getAttributes())).map(e -> SafeConverter.toDate(e.get("beginDate"))).orElse(null);
        if (begin == null) {
            return;
        }

        NewBookCatalog newBookCatalog = chipCourseSupport.fetchDayUnit(now, true, orderProduct, aiUserUnitResultPlan.getBookId());
        if (newBookCatalog == null || !newBookCatalog.getId().equals(aiUserUnitResultPlan.getUnitId())) {
            return;
        }

        String cnInfo = Optional.ofNullable(newBookCatalog)
                .filter(e -> MapUtils.isNotEmpty(e.getExtras()))
                .map(e -> JsonUtils.fromJson(SafeConverter.toString(e.getExtras().get("ai_teacher"))))
                .filter(e -> MapUtils.isNotEmpty(e))
                .map(e -> SafeConverter.toString(e.get("cardTitle"), ""))
                .orElse("");

        String enInfo = Optional.ofNullable(newBookCatalog)
                .filter(e -> MapUtils.isNotEmpty(e.getExtras()))
                .map(e -> JsonUtils.fromJson(SafeConverter.toString(e.getExtras().get("ai_teacher"))))
                .filter(e -> MapUtils.isNotEmpty(e))
                .map(e -> SafeConverter.toString(e.get("pageTitle"), ""))
                .orElse("");

        chipsContentService.sendDailySummaryTemplateMessage(enInfo, cnInfo, aiUserUnitResultPlan.getUserId(), aiUserUnitResultPlan.getUnitId(), aiUserUnitResultPlan.getBookId());
    }

    @Getter
    @Setter
    private class AbilityBean {
        private AIUserUnitResultPlan.Ability pointAbility;
        private Integer score;

        public AbilityBean(AIUserUnitResultPlan.Ability pointAbility, Integer score) {
            this.score = score;
            this.pointAbility = pointAbility;
        }
    }

}
