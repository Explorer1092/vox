package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.wechat.api.entity.WechatUserRef;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatService;
import com.voxlearning.galaxy.service.wechat.api.util.StudyTogetherWechatInfoProvider;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.TalkShardLoader;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkEnrollment;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkTopic;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * @author malong
 * @since 2018/05/28
 */
@Named
@ScheduledJobDefinition(
        jobName = "17说发送活动即将开始模板消息",
        jobDescription = "17说发送活动即将开始提醒模板消息",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 30 17 31 5 ?")
@ProgressTotalWork(100)
public class AutoSendYQTalkTemplateMsgJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = TalkShardLoader.class)
    private TalkShardLoader talkShardLoader;

    @Inject
    private WechatLoaderClient wechatLoaderClient;

    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;
    @ImportService(interfaceClass = DPWechatService.class)
    private DPWechatService dpWechatService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String topicId = SafeConverter.toString(parameters.get("topicId"));
        String first = SafeConverter.toString(parameters.get("first"));
        String keyword1 = SafeConverter.toString(parameters.get("keyword1"));
        String keyword2 = SafeConverter.toString(parameters.get("keyword2"));
        String remark = SafeConverter.toString(parameters.get("remark"));
        if (StringUtils.isBlank(topicId) || StringUtils.isBlank(first) || StringUtils.isBlank(keyword1) || StringUtils.isBlank(keyword2) || StringUtils.isBlank(remark)) {
            logger.error("AutoSendYQTalkTemplateMsgJob parameter error!");
            return;
        }
        TalkTopic topic = talkShardLoader.loadTopicById(topicId);
        if (topic == null) {
            logger.error("AutoSendYQTalkTemplateMsgJob topic doesn't exist, topicId:{}", topicId);
            return;
        }
        String templateId;
        if (RuntimeMode.isProduction()) {
            templateId = "I1YEmlajMH9hYYwhporSrUrJMLcwX5a2TGHB9TgTP8w";
        } else if (RuntimeMode.isStaging()) {
            templateId = "KtpLVlL3IGFNK53xvUIWAeybcWks6U-hcjoUUAgKMDA";
        } else {
            templateId = "rlwev5FpTRPVVD39_C_t-S_qhvJSkbVZOpllQJsLT-0";
        }

        List<String> keywords = new ArrayList<>();
        keywords.add(keyword1);
        keywords.add(keyword2);
        String url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/17shuo/index.vpage?from=app&topicId=" + topicId;

        int pageSize = 500;
        long timestamp = System.currentTimeMillis();
        int userCount = 0;
        int wxCount = 0;
        List<Long> userIds;
        do {
            List<TalkEnrollment> talkEnrollments = talkShardLoader.getTalkEnrollmentByTopicId(topicId, timestamp, pageSize);
            if (isEmpty(talkEnrollments)) {
                break;
            }
            userIds = talkEnrollments.stream().map(TalkEnrollment::getUserId).collect(Collectors.toList());
            Date date = talkEnrollments.stream().filter(Objects::nonNull).map(TalkEnrollment::getCreateTime).min(Date::compareTo).orElse(null);
            if(null != date){
                timestamp = date.getTime() - 1;
            } else {
                timestamp = 0;
            }
            userCount += userIds.size();
            int count = sendMsg(userIds, templateId, first, remark, url, keywords);
            wxCount += count;
        } while (CollectionUtils.isNotEmpty(userIds));
        logger.info("AutoSendYQTalkTemplateMsgJob finished, userCount:{}, wxCount:{}", userCount, wxCount);
        progressMonitor.done();
    }

    private int sendMsg(List<Long> userIds, String templateId, String first, String remark, String url, List<String> keywords) {
        Map<Long, List<WechatUserRef>> wechatUserRefsMap = dpWechatLoader.getWechatUserRefs(userIds, StudyTogetherWechatInfoProvider.INSTANCE.type());
        List<WechatUserRef> refs = wechatUserRefsMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Set<String> openIds = refs.stream().map(WechatUserRef::getId).collect(Collectors.toSet());

        openIds.forEach(openId -> {
            try {
                dpWechatService.sendTemplateMessage(StudyTogetherWechatInfoProvider.INSTANCE.wechatInfoContext(), openId, templateId, url, first, remark, keywords);
            } catch (Exception ex) {
                logger.error("AutoSendYQTalkTemplateMsgJob send message error, openId:{}", openId, ex.getMessage());
            }
        });
        return openIds.size();
    }
}
