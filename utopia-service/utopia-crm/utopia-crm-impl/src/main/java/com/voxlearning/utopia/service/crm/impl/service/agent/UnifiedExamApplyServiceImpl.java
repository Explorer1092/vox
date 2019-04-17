package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.TestPaperEntryStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;
import com.voxlearning.utopia.service.crm.api.service.agent.UnifiedExamApplyService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.UnifiedExamApplyPersistence;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tao.zang on 2017/4/17.
 */
@Named
@Service(interfaceClass = UnifiedExamApplyService.class)
@ExposeService(interfaceClass = UnifiedExamApplyService.class)
public class UnifiedExamApplyServiceImpl extends SpringContainerSupport implements UnifiedExamApplyService {
    @Inject
    private UnifiedExamApplyPersistence unifiedExamApplyPersistence;
    @Inject
    private WorkFlowServiceClient workFlowService;
    @Inject
    private EmailServiceClient emailServiceClient;

    @AlpsQueueProducer(queue = "utopia.agent.command.queue")
    private MessageProducer messageProducer;

    @Override
    public UnifiedExamApply persist(UnifiedExamApply unifiedExamApply) {
        if(unifiedExamApply !=null){
            if(unifiedExamApply.getId()!=null){
                unifiedExamApplyPersistence.replace(unifiedExamApply);
            }else{
                unifiedExamApplyPersistence.insert(unifiedExamApply);
            }
        }
        return unifiedExamApply;
    }

    @Override
    public UnifiedExamApply update(UnifiedExamApply unifiedExamApply) {
        if(unifiedExamApply !=null){
            unifiedExamApply = unifiedExamApplyPersistence.replace(unifiedExamApply);
        }
        return unifiedExamApply;
    }

    /**
     * @param unifiedExamId
     * @param status
     * @param failureCause
     * @return
     */
    public MapMessage testPaperEnteryResult(String unifiedExamId, Integer status, String failureCause, String papers) {
        if(status == null || !(status == 0 || status == 1 || status == 2)){
            return MapMessage.errorMessage("status 数据异常");
        }
        UnifiedExamApply unifiedExamApply = unifiedExamApplyPersistence.load(Long.valueOf(unifiedExamId));
        if(unifiedExamApply == null){
            return MapMessage.errorMessage("统考申请数据信息对照异常");
        }

        WorkFlowProcessResult processResult = null;
        String processNote = "";
        if(status == 0){ //内容库审核通过
            processResult = WorkFlowProcessResult.agree;
            processNote = "内容库审核通过";
        }else if(status == 1){//内容库审核失败
            processResult = WorkFlowProcessResult.reject;
            processNote = StringUtils.isBlank(failureCause)? "内容库驳回" : failureCause;
        }else {//内容库上线  status == 2
            if(unifiedExamApply.getStatus() != ApplyStatus.APPROVED){
                processResult = WorkFlowProcessResult.agree;
                processNote = "内容库审核通过";
            }
            unifiedExamApply.setEntryStatus(TestPaperEntryStatus.ONLINE);
            //fixme entryTestPaperAddress paperId
            List<Map> papersInfo = JsonUtils.fromJsonToList(papers, Map.class);
            String entryTestPaperName = StringUtils.join(papersInfo.stream().map(p -> p.get("fileName")).collect(Collectors.toList()), ",");
            String entryTestPaperAddress = StringUtils.join(papersInfo.stream().map(p -> p.get("fileUrl")).collect(Collectors.toList()), ",");
            String paperId = StringUtils.join(papersInfo.stream().map(p -> p.get("paperId")).collect(Collectors.toList()), ",");
            unifiedExamApply.setEntryTestPapeName(entryTestPaperName);
            unifiedExamApply.setEntryTestPaperAddress(entryTestPaperAddress);
            unifiedExamApply.setTestPaperId(paperId);//试卷ID 标识
            unifiedExamApplyPersistence.replace(unifiedExamApply);//更改申请记录中的状态
            //上线提醒
            String sendEmail = "xuyi.tian@17zuoye.com";
            emailServiceClient.createPlainEmail()
                    .body(StringUtils.formatMessage("{}申请的 {} 的 {} 试卷已经录入完成并已上线，学生可正常考试。",unifiedExamApply.getAccountName(), DateUtils.dateToString(unifiedExamApply.getUnifiedExamBeginTime()),unifiedExamApply.getUnifiedExamName()))
                    .subject("统考申请发布通知")
                    .to(sendEmail)
                    .send();

            // 发送消息到Agent
            Map<String, Object> command = new HashMap<>();
            command.put("command", "unified_exam_apply_online");
            command.put("unifiedExamName", unifiedExamApply.getUnifiedExamName());
            command.put("receiverId", SafeConverter.toLong(unifiedExamApply.getAccount()));
            Message message = Message.newMessage();
            message.withPlainTextBody(JsonUtils.toJson(command));
            messageProducer.produce(message);

        }

        // 更新工作流
        if(processResult != null){
            workFlowService.processWorkflow("agent", "cr", "内容库", unifiedExamApply.getWorkflowId(), processResult, processNote, null);
        }

        return MapMessage.successMessage();
    }

    @Override
    public void updateStatus(Long id, ApplyStatus applyStatus) {
        UnifiedExamApply unifiedExamApply  = unifiedExamApplyPersistence.load(id);
        unifiedExamApply.setStatus(applyStatus);
        unifiedExamApplyPersistence.replace(unifiedExamApply);
    }
}
