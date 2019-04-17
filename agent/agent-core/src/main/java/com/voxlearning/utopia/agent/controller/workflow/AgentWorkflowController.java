package com.voxlearning.utopia.agent.controller.workflow;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCost;
import com.voxlearning.utopia.agent.service.apply.AgentApplyManagementService;
import com.voxlearning.utopia.agent.service.apply.AgentDictSchoolApplyService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.workflow.AgentWorkflowService;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.DataReportApplyLoader;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author song.wang
 * @date 2016/12/30
 */
@Controller
@RequestMapping("/workflow")
@Slf4j
public class AgentWorkflowController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentWorkflowService agentWorkflowService;
    @Inject
    private AgentApplyManagementService agentApplyManagementService;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private DataReportApplyLoader dataReportApplyLoader;
    @Inject
    private AgentDictSchoolApplyService agentDictSchoolApplyService;

    @Inject private AgentMaterialBudgetService agentMaterialBudgetService;

    @RequestMapping(value = "todo/list.vpage")
    public String todoList(Model model) {
        Long userId = getCurrentUserId();
        List<WorkFlowTargetUserProcessData> dataList = agentWorkflowService.getTodoList(userId);
        model.addAttribute("dataList", dataList);
        model.addAttribute("userId", userId);
        return "workflow/todo_list";
    }

    @RequestMapping(value = "done/list.vpage")
    public String doneList(Model model) {
        Long userId = getCurrentUserId();
        List<WorkFlowTargetUserProcessData> dataList = agentWorkflowService.getDoneList(userId);
        model.addAttribute("dataList", dataList);
        return "workflow/done_list";
    }

    // 审核处理详情页
    @RequestMapping(value = "todo/proccess_page.vpage")
    public String proccessPage(Model model) {
        Long workflowId = requestLong("workflowId");
        String applyTypeStr = getRequestString("applyType");
        ApplyType applyType = null;
        if (StringUtils.isNotBlank(applyTypeStr)) {
            applyType = ApplyType.nameOf(applyTypeStr);
        }
        if (applyType == null || workflowId == null) {
            try {
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return "";
            } catch (Exception e) {
            }
        }
        // 获取申请及处理信息
        ApplyWithProcessResultData applyDetail = agentApplyManagementService.getApplyDetailByWorkflowId(applyType, workflowId, false);
        model.addAttribute("applyData", applyDetail);
        List<WorkFlowProcessResult> processList = new ArrayList<>();
        processList.add(WorkFlowProcessResult.agree);
        processList.add(WorkFlowProcessResult.reject);
        model.addAttribute("processList", processList);
        if (applyType == ApplyType.AGENT_MODIFY_DICT_SCHOOL) {
            if (applyDetail != null) {  // 如果applyDetail 为空， 则会返回一个空白页
                // 字典表变更申请
                // 获取历史申请记录
                AgentModifyDictSchoolApply modifyDictSchoolApply = (AgentModifyDictSchoolApply) applyDetail.getApply();
                List<Map<String, Object>> historyApplyList = agentDictSchoolApplyService.getDictSchoolApplyList(modifyDictSchoolApply.getSchoolId());
                // 删除当前申请数据
                if (CollectionUtils.isNotEmpty(historyApplyList)) {
                    Iterator<Map<String, Object>> iterator = historyApplyList.iterator();
                    while (iterator.hasNext()) {
                        Map<String, Object> item = iterator.next();
                        Long id = (Long) item.get("id");
                        if (Objects.equals(applyDetail.getApply().getId(), id)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                model.addAttribute("historyApplyList", historyApplyList);
            }
            return "workflow/dictschool_apply_process";
        } else if (applyType == ApplyType.AGENT_MATERIAL_APPLY) {
            if (applyDetail != null && applyDetail.getApply() != null && applyDetail.getApply().getAccount() != null) {
                Long agentUserId = SafeConverter.toLong(applyDetail.getApply().getAccount());
                AgentMaterialCost userMaterialCost = agentMaterialBudgetService.getUserMaterialCostByUserId(agentUserId);
                if (userMaterialCost != null) {
                    model.addAttribute("usableCashAmount", userMaterialCost.getBalance());
                }
            }
            return "workflow/shopping_car_process";
        } else if (applyType == ApplyType.AGENT_UNIFIED_EXAM_APPLY) {//统考申请跳转界面
            // 获取申请及处理信息
            if (applyDetail != null) {//若是为空则跳转至列表吧
                UnifiedExamApply unifiedExamApply = (UnifiedExamApply) applyDetail.getApply();
                if (Objects.equals(unifiedExamApply.getRegionLeve(), "school")) {
                    String[] schoolIdStr = unifiedExamApply.getUnifiedExamSchool().split(",");
                    List<Long> managedSchools = new ArrayList<>();
                    for (String s : schoolIdStr) {
                        managedSchools.add(Long.valueOf(s));
                    }
                    Map<Long, CrmSchoolSummary> schools = crmSummaryLoaderClient.loadSchoolSummary(managedSchools);
                    model.addAttribute("schoolList", schools.values());
                }
                return "workflow/unified_exam_process";
            }
        } else if (applyType == ApplyType.AGENT_DATA_REPORT_APPLY) {
            if (applyDetail != null) {//若是为空则跳转至列表吧
                String account = applyDetail.getApply().getAccount();
                List<DataReportApply> historyApplies = dataReportApplyLoader.loadByAccount(SystemPlatformType.AGENT, account);
                DataReportApply dataReportApply = (DataReportApply) applyDetail.getApply();
                Long schoolId = dataReportApply.getSchoolId();
                if (null != schoolId) {
                    School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
                    Integer countyCode = school.getRegionCode();
                    if (null != countyCode) {
                        ExRegion exRegion = raikouSystem.loadRegion(countyCode);
                        model.addAttribute("schoolRegion", exRegion);
                    }
                }
                model.addAttribute("historyApplies", historyApplies);
                AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
                if (roleType == AgentRoleType.CityManager) {
                    return "workflow/datareport/data_report_city_process";
                } else {
                    return "workflow/datareport/data_report_po_process";
                }
            }
        }
        return "workflow/done_list";
    }

    // 工作流审核
    @RequestMapping(value = "todo/process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage process() {
        AuthCurrentUser currentUser = getCurrentUser();
        WorkFlowProcessResult processResult = WorkFlowProcessResult.typeOf(getRequestInt("processResult")); // 操作类型：  1：通过   2：拒绝  4：撤销
        Long workflowId = requestLong("workflowId");
        String processNote = getRequestString("processNote");
        if (processResult == null || workflowId == null || StringUtils.isBlank(processNote)) {
            return MapMessage.errorMessage("参数无效，请重新操作！");
        }

        List<WorkFlowProcessUser> processUserList = null;
        String processUsersJsonStr = getRequestString("processUsers");
        if (StringUtils.isNotBlank(processUsersJsonStr)) {
            processUserList = JsonUtils.fromJsonToList(processUsersJsonStr, WorkFlowProcessUser.class);
        }
        return agentWorkflowService.processWorkflow(currentUser, workflowId, processResult, processNote, processUserList);
    }

    /**
     * 批量同意
     *
     * @return
     */
    @RequestMapping(value = "todo/batchApproved.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchApproved() {
        AuthCurrentUser currentUser = getCurrentUser();
        String workflowIds = requestString("workflowIds");
        String processNote = getRequestString("processNote");
        if (StringUtils.isBlank(workflowIds) || StringUtils.isBlank(processNote)) {
            return MapMessage.errorMessage("参数无效，请重新操作！");
        }
        WorkFlowProcessResult processResult = WorkFlowProcessResult.agree;
        String[] workflowIdsArray = workflowIds.split(",");
        List<String> errorMessages = new ArrayList<>();
        for (int i = 0; i < workflowIdsArray.length; i++) {
            long workflowId = SafeConverter.toLong(workflowIdsArray[i]);
            if (workflowId > 0) {
                MapMessage mapMessage = agentWorkflowService.processWorkflow(currentUser, workflowId, processResult, processNote, null);
                if (!mapMessage.isSuccess()) {
                    errorMessages.add("workflowId:" + workflowId + "," + mapMessage.getInfo());
                }
            }
        }
        MapMessage successMessage = MapMessage.successMessage();
        successMessage.add("allCount", workflowIdsArray.length);
        successMessage.add("errorMessages", errorMessages);
        return successMessage;
    }
}
