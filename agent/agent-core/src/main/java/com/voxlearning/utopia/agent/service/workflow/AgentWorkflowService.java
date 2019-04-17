package com.voxlearning.utopia.agent.service.workflow;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2017/1/4
 */
@Named
public class AgentWorkflowService extends AbstractAgentService {
    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject
    private WorkFlowServiceClient workFlowServiceClient;

    public List<WorkFlowTargetUserProcessData> getTodoList(Long userId){
        return workFlowLoaderClient.fetchTodoWorkflowList("agent", String.valueOf(userId), null, null, null, null, 0, 10000).getContent();
    }

    public List<WorkFlowTargetUserProcessData> getTodoList(Long userId, WorkFlowType workFlowType){
        return workFlowLoaderClient.fetchTodoWorkflowList("agent", String.valueOf(userId), workFlowType, null, null, null, 0, 10000).getContent();
    }

    public List<WorkFlowTargetUserProcessData> getDoneList(Long userId){
        return workFlowLoaderClient.fetchDoneWorkflowList("agent", String.valueOf(userId), null, null, null, null, 0, 10000).getContent();
    }

    public List<WorkFlowTargetUserProcessData> getDoneList(Long userId, WorkFlowType workFlowType, WorkFlowProcessResult workFlowProcessResult){
        return workFlowLoaderClient.fetchDoneWorkflowList("agent", String.valueOf(userId), workFlowType, workFlowProcessResult, null, null, 0, 10000).getContent();
    }

    public MapMessage processWorkflow(AuthCurrentUser currentUser, Long workflowId, WorkFlowProcessResult processResult, String processNote, List<WorkFlowProcessUser> processUserList){
        return workFlowServiceClient.processWorkflow("agent", String.valueOf(currentUser.getUserId()), currentUser.getRealName(), workflowId, processResult, processNote, processUserList);
    }

    // 获取最近一次的处理记录
    public WorkFlowProcessHistory getLatestProcessHistory(Long workflowId){
        List<WorkFlowProcessHistory> processHistoryList = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(workflowId);
        if(CollectionUtils.isEmpty(processHistoryList)){
            return null;
        }
        Collections.sort(processHistoryList, ((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())));
        return processHistoryList.get(0);
    }

}
