package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.apply.AgentApplyManagementService;
import com.voxlearning.utopia.agent.service.mobile.AgentAuditService;
import com.voxlearning.utopia.agent.service.workflow.AgentWorkflowService;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 审核
 *
 * @author song.wang
 * @date 2017/5/25
 */
@Controller
@RequestMapping(value = "/mobile/audit")
public class AgentAuditController extends AbstractAgentController {

    @Inject
    private AgentWorkflowService agentWorkflowService;
    @Inject
    private AgentApplyManagementService agentApplyManagementService;

    @Inject
    private AgentAuditService agentAuditService;

    @RequestMapping(value = "index.vpage")
    public String index(Model model){

        AuthCurrentUser user = getCurrentUser();
        Map<WorkFlowType, Integer> countMap = agentAuditService.getTodoWorkflowCount(user.getUserId(), Arrays.asList(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL, WorkFlowType.AGENT_MATERIAL_APPLY, WorkFlowType.AGENT_DATA_REPORT_APPLY));

        model.addAttribute("modifyDictSchoolCount", countMap.get(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL));
        model.addAttribute("orderCount", countMap.get(WorkFlowType.AGENT_MATERIAL_APPLY));
        model.addAttribute("dataReportCount", countMap.get(WorkFlowType.AGENT_DATA_REPORT_APPLY));
//        if(user.isCityManager()){
//            Set<Long> managedUsers = baseOrgService.getManagedGroupUsers(user.getUserId(), true)
//                    .stream().map(AgentUser::getId).collect(toSet());
//            List<CrmMainSubAccountApply> applyRecord = agentResourceService.getUserApplyRecord(managedUsers);
//            long mainSubAccountApplyCount = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.PENDING.equals(app.getAuditStatus())).count();
//            model.addAttribute("mainSubAccountApplyCount", mainSubAccountApplyCount);
//        }

        return "rebuildViewDir/mobile/audit/index";
    }

    @RequestMapping(value = "todo_list.vpage")
    public String todoList(Model model){
//        Long userId = getCurrentUserId();
//        Integer workflowTypeId = getRequestInt("workflowType");
//        WorkFlowType workFlowType = WorkFlowType.typeOf(workflowTypeId);
//        if(workFlowType == null){
//            workFlowType = WorkFlowType.AGENT_MODIFY_DICT_SCHOOL;
//        }
//        List<WorkFlowTargetUserProcessData> workflowRecordList = agentWorkflowService.getTodoList(userId, workFlowType);
//        List<ApplyWithProcessResultData> dataList = new ArrayList<>();
//        if(CollectionUtils.isNotEmpty(workflowRecordList)){
//            workflowRecordList.forEach(p -> {
//                ApplyType applyType = null;
//                if(p.getWorkFlowRecord() != null && p.getWorkFlowRecord().getWorkFlowType() != null){
//                    applyType = ApplyType.nameOf(p.getWorkFlowRecord().getWorkFlowType().name());
//                }
//                if(applyType == null){
//                    return;
//                }
//                ApplyWithProcessResultData data = agentApplyManagementService.getApplyDetailByWorkflowId(applyType, p.getWorkFlowRecord().getId(), false);
//                if(data != null){
//                    dataList.add(data);
//                }
//            });
//        }
//
//        model.addAttribute("dataList", dataList);
//        if(workFlowType == WorkFlowType.AGENT_MODIFY_DICT_SCHOOL){
//            return "rebuildViewDir/mobile/audit/dictionaryTable";
//        }else if(workFlowType == WorkFlowType.AGENT_MATERIAL_APPLY){
//            return "rebuildViewDir/mobile/audit/orderCount";
//        }else if(workFlowType == WorkFlowType.AGENT_DATA_REPORT_APPLY){
//            return "rebuildViewDir/mobile/audit/dataReport";
//        }
        Long userId = getCurrentUserId();
        List<WorkFlowTargetUserProcessData> dataList = agentWorkflowService.getTodoList(userId);
        model.addAttribute("dataList", dataList);
        return "rebuildViewDir/mobile/audit/workflow_list";
    }


    @RequestMapping(value = "done_list.vpage")
    public String doneList(Model model){
//        Long userId = getCurrentUserId();
//        Integer workflowTypeId = getRequestInt("workflowType", 1);
//        WorkFlowType workFlowType = WorkFlowType.typeOf(workflowTypeId);
//        Integer processResultId = getRequestInt("processResult", 1);
//        WorkFlowProcessResult processResult = WorkFlowProcessResult.typeOf(processResultId);
//        if(workFlowType == null){
//            workFlowType = WorkFlowType.AGENT_MODIFY_DICT_SCHOOL;
//        }
//
//        List<WorkFlowTargetUserProcessData> workflowRecordList = agentWorkflowService.getDoneList(userId, workFlowType, processResult);
//        List<Map<String, Object>> dataList = new ArrayList<>();
//        if(CollectionUtils.isNotEmpty(workflowRecordList)){
//            workflowRecordList.forEach(p -> {
//                ApplyType applyType = null;
//                if(p.getWorkFlowRecord() != null && p.getWorkFlowRecord().getWorkFlowType() != null){
//                    applyType = ApplyType.nameOf(p.getWorkFlowRecord().getWorkFlowType().name());
//                }
//                if(applyType == null){
//                    return;
//                }
//                ApplyWithProcessResultData applyData = agentApplyManagementService.getApplyDetailByWorkflowId(applyType, p.getWorkFlowRecord().getId(), false);
//                if(applyData != null){
//                    Map<String, Object> data = new HashMap<>();
//                    data.put("apply", applyData.getApply());
//                    data.put("processHistory", p.getProcessHistory());
//                    dataList.add(data);
//                }
//            });
//        }
//
//        model.addAttribute("dataList", dataList);
//        model.addAttribute("processResultId", processResultId);
//        if(workFlowType == WorkFlowType.AGENT_MODIFY_DICT_SCHOOL){
//            return "rebuildViewDir/mobile/audit/dictionary_done";
//        }else if(workFlowType == WorkFlowType.AGENT_MATERIAL_APPLY){
//            return "rebuildViewDir/mobile/audit/orderCount_done";
//        } else if(workFlowType == WorkFlowType.AGENT_DATA_REPORT_APPLY){
//            return "rebuildViewDir/mobile/audit/dataReport_done";
//        }
        Long userId = getCurrentUserId();
        List<WorkFlowTargetUserProcessData> dataList = agentWorkflowService.getDoneList(userId);
        model.addAttribute("dataList", dataList);
        return "rebuildViewDir/mobile/audit/workFlowDone_list";
    }


    @RequestMapping(value = "process_page.vpage")
    public String processPage(Model model){
        Long workflowId = requestLong("workflowId");
        String applyTypeStr = getRequestString("applyType");
        ApplyType applyType = null;
        if(StringUtils.isNotBlank(applyTypeStr)){
            applyType = ApplyType.nameOf(applyTypeStr);
        }
        if(applyType == null || workflowId == null){
            try {
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return "";
            }catch (Exception e){
            }
        }
        // 获取申请及处理信息
        ApplyWithProcessResultData applyDetail = agentApplyManagementService.getApplyDetailByWorkflowId(applyType, workflowId, false);

        model.addAttribute("applyData", applyDetail);
        List<WorkFlowProcessResult> processList = new ArrayList<>();
        processList.add(WorkFlowProcessResult.agree);
        processList.add(WorkFlowProcessResult.reject);
        model.addAttribute("processList", processList);
        if(applyType == ApplyType.AGENT_MODIFY_DICT_SCHOOL){
            model.addAttribute("applyType", "agent_modify_dict_school");
            return "rebuildViewDir/mobile/apply/dictionaryInfo_process";
        } else if (applyType == ApplyType.AGENT_MATERIAL_APPLY) {
            return "rebuildViewDir/mobile/apply/purchaseInfo_process";
        } else if (applyType == ApplyType.AGENT_DATA_REPORT_APPLY) {
            if(getCurrentUser().isCityManager()) {  // 如果当前用户是市经理，则进入审核页
                return "rebuildViewDir/mobile/apply/dataReportInfo_process";
            }
        }
        return "rebuildViewDir/mobile/apply/dictionaryInfo_process";
    }

    // 工作流审核
    @RequestMapping(value = "process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage process(){
        AuthCurrentUser currentUser = getCurrentUser();
        WorkFlowProcessResult processResult = WorkFlowProcessResult.typeOf(getRequestInt("processResult")); // 操作类型：  1：通过   2：拒绝  4：撤销
        Long workflowId = requestLong("workflowId");
        String processNote = getRequestString("processNote");
        if(processResult == null || workflowId == null || StringUtils.isBlank(processNote)){
            return MapMessage.errorMessage("参数无效，请重新操作！");
        }

        List<WorkFlowProcessUser> processUserList = null;
        String processUsersJsonStr = getRequestString("processUsers");
        if(StringUtils.isNotBlank(processUsersJsonStr)){
            processUserList = JsonUtils.fromJsonToList(processUsersJsonStr, WorkFlowProcessUser.class);
        }
        return agentWorkflowService.processWorkflow(currentUser, workflowId, processResult, processNote, processUserList);
    }


//    /**
//     * 处理申请包班记录
//     */
//    @RequestMapping(value = "clazz_apply_list.vpage", method = RequestMethod.GET)
//    public String mainSubAccountApplyList(Model model) {
//        // 只有市经理有权限
//        if (!getCurrentUser().isCityManager()) {
//            return errorInfoPage(AgentErrorCode.NO_PERMISSION_TO_USE, "该功能只对市经理开放", model);
//        }
//        Long userId = getCurrentUserId();
//        Set<Long> managedUsers = baseOrgService.getManagedGroupUsers(userId, true)
//                .stream().map(AgentUser::getId).collect(toSet());
//
//        List<CrmMainSubAccountApply> applyRecord = agentResourceService.getUserApplyRecord(managedUsers);
//        // 待审核
//        List<CrmMainSubAccountApply> pendingList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.PENDING.equals(app.getAuditStatus())).collect(Collectors.toList());
//        model.addAttribute("pendingList", pendingList);
//        // 已通过
//        List<CrmMainSubAccountApply> successList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.APPROVED.equals(app.getAuditStatus())).collect(Collectors.toList());
//        model.addAttribute("successList", successList);
//        // 已驳回
//        List<CrmMainSubAccountApply> rejectList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.REJECT.equals(app.getAuditStatus())).collect(Collectors.toList());
//        model.addAttribute("rejectList", rejectList);
////        return "rebuildViewDir/mobile/my/clazz_apply_list";
//        return "rebuildViewDir/mobile/audit/clazz_apply_list";
//    }
}
