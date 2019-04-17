package com.voxlearning.utopia.schedule.schedule.productfeedback;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.crm.api.bean.ApplyProcessResult;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.consumer.loader.ApplyManagementLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentProductFeedbackLoadClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品反馈通知PM和销运人员处理
 * Created by yaguang.wang on 2017/2/24.
 */
@Named
@ScheduledJobDefinition(
        jobName = "产品反馈通知PM和该反馈的销运处理人员",
        jobDescription = "每天7:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 7 * * ?"
)
public class ProductFeedbackNotificationJob extends ScheduledJobWithJournalSupport {

    @Inject private ApplyManagementLoaderClient applyManagementLoaderClient;

    @Inject private AgentProductFeedbackLoadClient agentProductFeedbackLoadClient;

    @Inject private EmailServiceClient emailServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Date stateDate = DateUtils.nextDay(new Date(), -2);
        Date endDate = DateUtils.nextDay(new Date(), -1);
        List<AgentProductFeedback> feedbacks = agentProductFeedbackLoadClient.loadByNeedNotification(AgentProductFeedbackStatus.PM_PENDING);
        List<Long> applyIds = feedbacks.stream().map(AgentProductFeedback::getId).collect(Collectors.toList());
        List<ApplyWithProcessResultData> resultDatas = applyIds.stream()
                .map(p -> applyManagementLoaderClient.fetchApplyWithProcessResultByApplyId(ApplyType.AGENT_PRODUCT_FEEDBACK, p, false))
                .collect(Collectors.toList());
        Map<Long, Map<String, Object>> pmEmailList = new HashMap<>();
        Map<Long, Map<String, Object>> soEmailList = new HashMap<>();
        resultDatas.forEach(p -> {
            AgentProductFeedback feedback = (AgentProductFeedback) p.getApply();
            List<ApplyProcessResult> processResults = p.getProcessResultList();
            String pmAccount = feedback.getPmAccount();
            for (ApplyProcessResult processResult : processResults) {
                if (!Objects.equals(processResult.getAccount(), pmAccount) && stateDate.before(processResult.getProcessDate()) && endDate.after(processResult.getProcessDate())) {
                    pmEmailList.put(feedback.getId(), createPMContent(feedback));
                    soEmailList.put(feedback.getId(), createSOContent(feedback, processResult.getAccount()));
                }
            }
        });
        //fixme 这里需要等长远发版 改成 pm 和 so 的两个模板
        pmEmailList.forEach((k, v) -> sendEmail(SafeConverter.toString(v.get("pmEmail")), v,  EmailTemplate.productfeedbackpm));
        soEmailList.forEach((k, v) -> sendEmail(SafeConverter.toString(v.get("accountEmail")), v,  EmailTemplate.productfeedbackso));
    }

    private void sendEmail(String toEmail, Map<String, Object> content, EmailTemplate emailTemplate) {
        emailServiceClient.createTemplateEmail(emailTemplate)
                .subject("产品反馈信息")
                .to(toEmail)
                .content(content)
                .send();
    }

    private Map<String, Object> createPMContent(AgentProductFeedback feedback) {
        Map<String, Object> content = new HashMap<>();
        content.put("workflowId", feedback.getWorkflowId());
        content.put("feedbackId",feedback.getId());
        content.put("fbPeople", feedback.getAccountName());
        content.put("teacherName", feedback.getTeacherName());
        content.put("subject", feedback.getTeacherSubject() == null ? "" : feedback.getTeacherSubject().getDesc());
        content.put("feedbackType", feedback.getFeedbackType().getDesc());
        content.put("first", feedback.getFirstCategory());
        content.put("second", feedback.getSecondCategory());
        content.put("third", feedback.getThirdCategory());
        content.put("grade", feedback.getBookGrade());
        content.put("content", feedback.getContent());
        content.put("bookName", feedback.getBookName());
        content.put("bookUnit", feedback.getBookUnit());
        content.put("bookCoveredArea", feedback.getBookCoveredArea());
        content.put("stuCount", feedback.getBookCoveredStudentCount());
        content.put("pmEmail", StringUtils.formatMessage("{}@17zuoye.com", feedback.getPmAccount()));
        return content;
    }

    private Map<String, Object> createSOContent(AgentProductFeedback feedback, String account) {
        Map<String, Object> content = new HashMap<>();
        content.put("workflowId", feedback.getWorkflowId());
        content.put("feedbackId",feedback.getId());
        content.put("feedbackContent", feedback.getContent());
        content.put("pmName", feedback.getPmAccountName());
        content.put("accountEmail", StringUtils.formatMessage("{}@17zuoye.com", account));
        return content;
    }

}
