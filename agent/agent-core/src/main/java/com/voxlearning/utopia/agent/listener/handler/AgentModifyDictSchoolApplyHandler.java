package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.service.workflow.AgentWorkflowService;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentModifyDictSchoolApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentModifyDictSchoolApplyServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 *
 * @author song.wang
 * @date 2016/12/28
 */
@Named
public class AgentModifyDictSchoolApplyHandler extends SpringContainerSupport {

    @Inject
    private AgentModifyDictSchoolApplyLoaderClient agentModifyDictSchoolApplyLoaderClient;
    @Inject
    private AgentModifyDictSchoolApplyServiceClient agentModifyDictSchoolApplyServiceClient;
    @Inject
    private AgentWorkflowService agentWorkflowService;
    @Inject
    private WorkFlowServiceClient workFlowServiceClient;
    @Inject
    private AgentDictSchoolService agentDictSchoolService;

    @Inject
    private AgentModifyDictSchoolApplyMessageHandler agentModifyDictSchoolApplyMessageHandler;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    public void handle(Long workflowId, WorkFlowProcessResult processResult, String workflowStatus, Boolean hasFollowStatus){

        if(workflowId == null || processResult == null || hasFollowStatus == null || StringUtils.isBlank(workflowStatus)){
            return;
        }
        AgentModifyDictSchoolApply modifyDictSchoolApply = agentModifyDictSchoolApplyLoaderClient.findByWorkflowId(workflowId);
        if(modifyDictSchoolApply == null){
            return;
        }
        if(!hasFollowStatus){ // 工作流已审批结束，没有后续状态
            ApplyStatus applyStatus = null;
            if(WorkFlowProcessResult.agree == processResult){
                applyStatus = ApplyStatus.APPROVED;
            }else if(WorkFlowProcessResult.reject == processResult){
                applyStatus = ApplyStatus.REJECTED;
            }else if(WorkFlowProcessResult.revoke == processResult){
                applyStatus = ApplyStatus.REVOKED;
            }
            if(applyStatus != null){
                agentModifyDictSchoolApplyServiceClient.updateStatus(modifyDictSchoolApply.getId(), applyStatus);
                //审核通过后直接加入字典
                if(applyStatus == ApplyStatus.APPROVED){
                    AgentModifyDictSchoolApply apply = agentModifyDictSchoolApplyLoaderClient.findByWorkflowId(workflowId);
                    if(apply != null && apply.getStatus() == ApplyStatus.APPROVED){
                        agentDictSchoolService.disposeApply(Collections.singletonList(apply));
                    }
                }
            }

            // 发送站内通知
            Map<String, Object> command = new HashMap<>();
            command.put("modifyType", modifyDictSchoolApply.getModifyType());
            command.put("schoolName", modifyDictSchoolApply.getSchoolName());
            command.put("schoolId", modifyDictSchoolApply.getSchoolId());
            command.put("receiverId", modifyDictSchoolApply.getAccount());
            command.put("processResult", processResult.toString());
            if (WorkFlowProcessResult.reject == processResult){
                WorkFlowProcessHistory latestProcessHistory = agentWorkflowService.getLatestProcessHistory(workflowId);
                String rejectNote = "";
                String rejectName = "";
                if(latestProcessHistory != null){
                    rejectNote = latestProcessHistory.getProcessNotes();
                    rejectName = latestProcessHistory.getProcessorName();
                }
                command.put("rejectName", rejectName);
                command.put("rejectNote", rejectNote);
            }
            agentModifyDictSchoolApplyMessageHandler.handle(command);
        } else {
            // 市经理审核通过
            if(WorkFlowProcessResult.agree == processResult && Objects.equals(workflowStatus, "lv1")){

                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(modifyDictSchoolApply.getSchoolId())
                        .getUninterruptibly();

                Boolean autoLv2Flag = true;
                //如果是加入字典表，且等级是初中或者高中,要走风控
                if (modifyDictSchoolApply.getModifyType() == AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType()
                        && (SchoolLevel.MIDDLE.getLevel() == school.getLevel() || SchoolLevel.HIGH.getLevel() == school.getLevel())) {
                    autoLv2Flag = false;
                }
                //如果是变更学校等级的任务，要走风控
                if (autoLv2Flag) {
                    if (Objects.equals(modifyDictSchoolApply.getModifyType(), AgentModifyDictSchoolApply.ModifyType.UPDATE_POPULARITY.getType())) {
                        autoLv2Flag = false;
                    }
                }
                //如果学校是学前学校，则不进行风控审核，系统自动审核通过
                if (modifyDictSchoolApply.getSchoolLevel() == SchoolLevel.INFANT.getLevel()) {
                    autoLv2Flag = true;
                }

                if (autoLv2Flag) {
                    workFlowServiceClient.processWorkflow("agent", "system", "系统", workflowId, WorkFlowProcessResult.agree, "系统自动通过", null);
                }
            }
        }
    }

}
