package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.workflow.AgentWorkflowService;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.DataReportApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.DataReportApplyServiceClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/13
 */
@Named
public class AgentDataReportApplyHandler extends SpringContainerSupport {

    @Inject
    private DataReportApplyLoaderClient dataReportApplyLoaderClient;
    @Inject
    private DataReportApplyServiceClient dataReportApplyServiceClient;
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private AgentWorkflowService agentWorkflowService;
    @Inject
    private AgentTagService agentTagService;

    public void handle(Long workflowId, WorkFlowProcessResult processResult, Boolean hasFollowStatus){

        if(workflowId == null || processResult == null || hasFollowStatus == null){
            return;
        }
        DataReportApply dataReportApply = dataReportApplyLoaderClient.loadByWorkflowId(workflowId);
        if(dataReportApply == null){
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
                dataReportApply.setStatus(applyStatus);
                dataReportApplyServiceClient.update(dataReportApply);
            }

            // 发送站内通知
            String regionOrSchool = "";
            if(dataReportApply.getReportLevel() == 1){ // 市级
                regionOrSchool += "区域：" + dataReportApply.getCityName();
            }else if(dataReportApply.getReportLevel() == 2){ // 区级
                regionOrSchool += "区域：" + dataReportApply.getCityName() + "/" + dataReportApply.getCountyName();
            }else if(dataReportApply.getReportLevel() == 3){ // 校级
                regionOrSchool += "学校：" + dataReportApply.getSchoolName() + "(" + dataReportApply.getSchoolId() + ")";
            }

            if(WorkFlowProcessResult.agree == processResult){
                String content = StringUtils.formatMessage("您提交的“{}{}”数据报告申请已审批通过。",regionOrSchool,dataReportApply.getSubject());
                agentNotifyService.sendNotify(AgentNotifyType.DATA_REPORT_APPLY.getType(), "大数据报告", content,
                        Collections.singleton(Long.valueOf(dataReportApply.getAccount())), null);
            }else if(WorkFlowProcessResult.reject == processResult){
                // 获取审批历史
                WorkFlowProcessHistory latestProcessHistory = agentWorkflowService.getLatestProcessHistory(workflowId);
                String rejectNote = "";
                String rejectName = "";
                if(latestProcessHistory != null){
                    rejectNote = latestProcessHistory.getProcessNotes();
                    rejectName = latestProcessHistory.getProcessorName();
                }
                String content = StringUtils.formatMessage("您提交的“{}{}”数据报告申请”申请被驳回。\r\n" +
                        "驳回原因：{}【驳回人：{}】。",regionOrSchool,dataReportApply.getSubject(),rejectNote,rejectName);
                List<Long> tagIds = agentTagService.getNotifyTagIdsByName("驳回");
                agentNotifyService.sendNotifyWithTags(AgentNotifyType.DATA_REPORT_APPLY.getType(), "大数据报告", content ,
                        Collections.singleton(Long.valueOf(dataReportApply.getAccount())), null, null, null, tagIds);
            }
        }
    }
}
