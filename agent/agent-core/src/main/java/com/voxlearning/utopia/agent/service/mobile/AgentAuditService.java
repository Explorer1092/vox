package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentAuditService
 *
 * @author song.wang
 * @date 2017/5/25
 */
@Named
public class AgentAuditService extends AbstractAgentService {

    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;

//    // 获取待审核的工作流申请数
//    public int getTodoWorkflowCount(Long userId, WorkFlowType workFlowType){
//        List<WorkFlowProcess> workFlowProcessList = workFlowLoaderClient.loadWorkFlowProcessByTargetUser("agent", String.valueOf(userId));
//        if(workFlowType == null){
//            if(CollectionUtils.isEmpty(workFlowProcessList)){
//                return 0;
//            }
//            return workFlowProcessList.size();
//        }
//        return getTargetWorkflowCount(workFlowProcessList, workFlowType);
//    }
    private int getTargetWorkflowCount(List<WorkFlowProcess> workFlowProcessList, WorkFlowType workFlowType){
        int count = 0;
        if(CollectionUtils.isNotEmpty(workFlowProcessList) && workFlowType != null){
            count = (int)workFlowProcessList.stream().filter(p -> p.getWorkFlowType() == workFlowType).count();
        }
        return count;
    }

    public Map<WorkFlowType, Integer> getTodoWorkflowCount(Long userId, List<WorkFlowType> typeList){
        if(CollectionUtils.isEmpty(typeList)){
            return Collections.emptyMap();
        }
        List<WorkFlowProcess> workFlowProcessList = workFlowLoaderClient.loadWorkFlowProcessByTargetUser("agent", String.valueOf(userId));
        Map<WorkFlowType, Integer> retMap = new HashMap<>();
        typeList.forEach(p -> retMap.put(p, getTargetWorkflowCount(workFlowProcessList, p)));
        return retMap;
    }



}
