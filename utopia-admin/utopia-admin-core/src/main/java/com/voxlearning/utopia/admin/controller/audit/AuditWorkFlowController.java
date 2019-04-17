package com.voxlearning.utopia.admin.controller.audit;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.admin.dao.AgentDictSchoolPersistence;
import com.voxlearning.utopia.admin.entity.CrmSchoolEvaluate;
import com.voxlearning.utopia.admin.parser.PmConfigParser;
import com.voxlearning.utopia.admin.service.audit.AuditApplyManagementService;
import com.voxlearning.utopia.admin.service.crm.CrmSchoolEvaluateService;
import com.voxlearning.utopia.entity.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.ApplyManagementLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupRegionLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/14
 */
@Controller
@RequestMapping("/audit/workflow")
public class AuditWorkFlowController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Inject private WorkFlowServiceClient workFlowServiceClient;
    @Inject private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject private ApplyManagementLoaderClient applyManagementLoaderClient;
    @Inject private AuditApplyManagementService crmApplyManagementService;
    @Inject private CrmSchoolEvaluateService crmSchoolEvaluateService;
    @Inject private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private AgentGroupRegionLoaderClient agentGroupRegionLoaderClient;
    @Inject private AgentDictSchoolPersistence agentDictSchoolPersistence;

    public static List<WorkFlowProcessUser> PM_ACCOUNT_LIST = PmConfigParser.getPmConfig();

    private List<AgentRoleType> USER_ROLE_LIST = Arrays.asList(
            AgentRoleType.Region,
            AgentRoleType.Country,
            AgentRoleType.CityManager,
            AgentRoleType.BusinessDeveloper
    );

    @RequestMapping(value = "todo_list.vpage")
    public String todoList(Model model, HttpServletResponse response) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        Integer workflowTypeId = getRequestInt("workflowType");
        WorkFlowType workFlowType = WorkFlowType.typeOf(workflowTypeId);
        Date startDate = requestDate("startDate");
        Date endDate = requestDate("endDate");
        String applicant = getRequestString("applicant");
        int pageNo = getRequestInt("PAGE");
        pageNo = pageNo < 0 ? 0 : pageNo;
        int pageSize = 50;
        Page<WorkFlowTargetUserProcessData> dataPage = workFlowLoaderClient.fetchTodoWorkflowList("admin", adminUser.getAdminUserName(), workFlowType, startDate, endDate == null ? null : DateUtils.addDays(endDate, 1), applicant, pageNo, pageSize);
        int totalPages = dataPage.getTotalPages();
        if (pageNo > 0 && pageNo > totalPages - 1) {
            try {
                List<String> parmas = new ArrayList<>();
                parmas.add("workflowType=" + (requestString("workflowType") != null ? requestString("workflowType") : ""));
                parmas.add("startDate=" + (requestString("startDate") != null ? requestString("startDate") : ""));
                parmas.add("endDate=" + (requestString("endDate") != null ? requestString("endDate") : ""));
                parmas.add("applicant=" + (requestString("applicant") != null ? requestString("applicant") : ""));
                parmas.add("PAGE=" + (totalPages - 1));
                response.sendRedirect("todo_list.vpage?" + StringUtils.join(parmas, "&"));
            } catch (IOException e) {
            }
        }
        model.addAttribute("dataPage", dataPage);
        model.addAttribute("workFlowTypeList", WorkFlowType.values());
        model.addAttribute("workflowTypeId", workflowTypeId);
        model.addAttribute("startDate", startDate == null ? "" : DateUtils.dateToString(startDate, "yyyy-MM-dd"));
        model.addAttribute("endDate", endDate == null ? "" : DateUtils.dateToString(endDate, "yyyy-MM-dd"));
        model.addAttribute("applicant", applicant);
        return "audit/workflow/todo_list";
    }

    @RequestMapping(value = "done_list.vpage")
    public String doneList(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        Integer workflowTypeId = getRequestInt("workflowType");
        WorkFlowType workFlowType = WorkFlowType.typeOf(workflowTypeId);
        Integer processResultId = getRequestInt("processResult");
        WorkFlowProcessResult processResult = WorkFlowProcessResult.typeOf(processResultId);
        Date startDate = requestDate("startDate");
        Date endDate = requestDate("endDate");
        int pageNo = getRequestInt("PAGE");
        pageNo = pageNo < 0 ? 0 : pageNo;
        int pageSize = 50;
        Page<WorkFlowTargetUserProcessData> dataPage = workFlowLoaderClient.fetchDoneWorkflowList("admin", adminUser.getAdminUserName(), workFlowType, processResult, startDate, endDate == null ? null : DateUtils.addDays(endDate, 1), pageNo, pageSize);
        model.addAttribute("dataPage", dataPage);
        model.addAttribute("workFlowTypeList", WorkFlowType.values());
        model.addAttribute("processResultTypeList", WorkFlowProcessResult.values());
        model.addAttribute("workflowTypeId", workflowTypeId);
        model.addAttribute("processResultId", processResultId);
        model.addAttribute("startDate", startDate == null ? "" : DateUtils.dateToString(startDate, "yyyy-MM-dd"));
        model.addAttribute("endDate", endDate == null ? "" : DateUtils.dateToString(endDate, "yyyy-MM-dd"));
        return "audit/workflow/done_list";
    }

    // 审核处理详情页
    @RequestMapping(value = "proccess_page.vpage")
    public String processPage(Model model) {

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
            } catch (Exception ignored) {
            }
        }
        model.addAttribute("currentAccount", getCurrentAdminUser().getAdminUserName());
        // 获取申请及处理信息
        ApplyWithProcessResultData applyDetail = applyManagementLoaderClient.fetchApplyWithProcessResultByWorkflowId(applyType, workflowId, false);
        model.addAttribute("applyData", applyDetail);
        List<WorkFlowProcessResult> processList = new ArrayList<>();
        processList.add(WorkFlowProcessResult.agree);
        processList.add(WorkFlowProcessResult.reject);
        model.addAttribute("processList", processList);
        model.addAttribute("prePath", RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net");

        if (applyType == ApplyType.AGENT_MODIFY_DICT_SCHOOL) {
            if (applyDetail != null) {  // 如果applyDetail 为空， 则会返回一个空白页
                // 字典表变更申请
                // 获取历史申请记录
                AgentModifyDictSchoolApply modifyDictSchoolApply = (AgentModifyDictSchoolApply) applyDetail.getApply();
                List<Map<String, Object>> historyApplyList = crmApplyManagementService.getDictSchoolApplyList(modifyDictSchoolApply.getSchoolId());
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
                // 设置学校
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(modifyDictSchoolApply.getSchoolId())
                        .getUninterruptibly();
                if (schoolExtInfo != null) {
                    model.addAttribute("schoolSize", schoolExtInfo.getSchoolSize());
                }

                // 设置学校评分记录
                List<CrmSchoolEvaluate> evaluateHistoryList = crmSchoolEvaluateService.loadCrmSchoolEvaluateBySchoolId(modifyDictSchoolApply.getSchoolId());
                model.addAttribute("evaluateHistoryList", evaluateHistoryList);

                // 设置当前部门概况
                Map<String, Object> agentDepartmentData = getAgentDepartmentData(SafeConverter.toLong(modifyDictSchoolApply.getAccount()));
                model.addAttribute("agentDepartmentData", agentDepartmentData);
            }
            return "audit/workflow/dictschool_apply_process";
        }
        if (applyType == ApplyType.AGENT_PRODUCT_FEEDBACK) {
            if (applyDetail == null) {
                return "audit/workflow/product_feedback_so_process";
            }

            AuthCurrentAdminUser currentAdminUser = getCurrentAdminUser();
            boolean isPm = false;
            if (CollectionUtils.isNotEmpty(PM_ACCOUNT_LIST)) {
                isPm = PM_ACCOUNT_LIST.stream().anyMatch(p -> Objects.equals(p.getUserPlatform(), "admin") && Objects.equals(p.getAccount(), currentAdminUser.getAdminUserName()));
            }
            AgentProductFeedback agentProductFeedback = (AgentProductFeedback) applyDetail.getApply();
            if (agentProductFeedback != null) {
                String account = agentProductFeedback.getAccount();
                List<AgentGroupUser> groupUsers = agentGroupUserLoaderClient.findByUserId(SafeConverter.toLong(account));
                groupUsers = groupUsers.stream().filter(p -> USER_ROLE_LIST.contains(p.getUserRoleType())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(groupUsers)) {
                    AgentGroup group = agentGroupLoaderClient.load(groupUsers.get(0).getGroupId());
                    if (group != null) {
                        model.addAttribute("groupName", group.getGroupName());
                        AgentGroup parentGroup = agentGroupLoaderClient.load(group.getParentId());
                        if (parentGroup != null) {
                            model.addAttribute("prentGroupName", parentGroup.getGroupName());
                        }
                    }
                }
            }

            if (isPm) { // PM 审核页
                processList.add(WorkFlowProcessResult.raiseup);
                model.addAttribute("pmList", PM_ACCOUNT_LIST);
                return "audit/workflow/product_feedback_pm_process";
            } else { // 销运审核页
                model.addAttribute("subjectList", AgentProductFeedbackSubject.values());
                model.addAttribute("typeList", AgentProductFeedbackType.values());
                model.addAttribute("pmList", PM_ACCOUNT_LIST);
                if (agentProductFeedback != null) {
                    model.addAttribute("category", JsonUtils.toJson(AgentProductFeedbackCategory.fetchCategory(agentProductFeedback.getFeedbackType() == null ? 0 : agentProductFeedback.getFeedbackType().getType())));
                    String phone = sensitiveUserDataServiceClient.showUserMobile(agentProductFeedback.getTeacherId(), "admin反馈信息详情" + getCurrentAdminUser().getRealName() + "查看老师电话", SafeConverter.toString(agentProductFeedback.getTeacherId()));
                    if (phone != null) {
                        model.addAttribute("teacherMobile", phone);

                    }
                    AgentUser agentUser = agentUserLoaderClient.load(SafeConverter.toLong(agentProductFeedback.getAccount()));
                    if (agentUser != null) {
                        model.addAttribute("accountMobile", agentUser.getTel());
                    }
                }
                if (agentProductFeedback != null && agentProductFeedback.getFeedbackStatus() == AgentProductFeedbackStatus.SO_CONFIRMING) { // PM 已驳回的情况下， 销运最终确认
                    return "audit/workflow/product_feedback_so2_process";
                }
                processList.add(WorkFlowProcessResult.raiseup);
                List<WorkFlowProcessUser> soList = new ArrayList<>();
                WorkFlowProcessUser soUser1 = new WorkFlowProcessUser();
                soUser1.setUserPlatform("admin");
                soUser1.setAccount("jingxiao.duan");
                soUser1.setAccountName("段景孝");
                soList.add(soUser1);

                WorkFlowProcessUser soUser2 = new WorkFlowProcessUser();
                soUser2.setUserPlatform("admin");
                soUser2.setAccount("baochang.yang");
                soUser2.setAccountName("杨宝长");
                soList.add(soUser2);

                WorkFlowProcessUser soUser3 = new WorkFlowProcessUser();
                soUser3.setUserPlatform("admin");
                soUser3.setAccount("shen.yang");
                soUser3.setAccountName("杨屾");
                soList.add(soUser3);
                model.addAttribute("soList", soList);
                return "audit/workflow/product_feedback_so_process";
            }
        }
        return "audit/workflow/todo_list";
    }


    private Map<String, Object> getAgentDepartmentData(Long userId) {
        if (userId == null || userId == 0L) {
            return Collections.emptyMap();
        }
        List<AgentGroupUser> agentGroupUserList = agentGroupUserLoaderClient.findByUserId(userId);
        if (CollectionUtils.isEmpty(agentGroupUserList)) {
            return Collections.emptyMap();
        }
        Map<String, Object> retMap = new HashMap<>();
        Long groupId = agentGroupUserList.get(0).getGroupId();
        AgentGroup group = agentGroupLoaderClient.load(groupId);
        String groupName = (group != null && !group.isDisabledTrue()) ? group.getGroupName() : "";
        retMap.put("groupName", groupName);
        List<AgentGroupUser> agentGroupUsers = agentGroupUserLoaderClient.findByGroupId(groupId);
        String groupManagerName = "";// 部门经理
        Integer dbUserCount = 0; // 专员数
        if (CollectionUtils.isNotEmpty(agentGroupUsers)) {
            List<Long> managerList = agentGroupUsers.stream()
                    .filter(p -> p.getUserRoleType() != null && (AgentRoleType.CityManager == p.getUserRoleType() || AgentRoleType.Region == p.getUserRoleType() || AgentRoleType.Country == p.getUserRoleType()))
                    .map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(managerList)) {
                AgentUser manager = agentUserLoaderClient.load(managerList.get(0));
                groupManagerName = (manager != null && manager.isValidUser()) ? manager.getRealName() : "";
            }

            dbUserCount = (int) agentGroupUsers.stream().filter(p -> p.getUserRoleType() != null && AgentRoleType.BusinessDeveloper == p.getUserRoleType()).count();

        }
        retMap.put("groupManagerName", groupManagerName);
        retMap.put("dbUserCount", dbUserCount);

        Integer schoolCount = 0; // 学校总数
        Integer allSchoolStudentCount = 0; // 所有学校的学生总数
        if (group != null && CollectionUtils.isNotEmpty(group.fetchServiceTypeList())) {
            List<SchoolLevel> schoolLevelList = convertServiceTypeToSchoolLevel(group.fetchServiceTypeList());
            if (CollectionUtils.isNotEmpty(schoolLevelList)) {
                Set<Long> schoolIds = new HashSet<>();
                List<AgentGroupRegion> groupRegionList = agentGroupRegionLoaderClient.findByGroupId(groupId);
                if (CollectionUtils.isNotEmpty(groupRegionList)) {
                    List<Integer> regionCodeList = groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
                    List<Integer> countyCodes = getCountyCodes(regionCodeList);
                    List<AgentDictSchool> allDictSchools = agentDictSchoolPersistence.findAllDictSchool();
                    List<AgentDictSchool> dictSchoolList = allDictSchools.stream().filter(p -> countyCodes.contains(p.getCountyCode())).collect(Collectors.toList());
                    List<Integer> schoolLevelCodes = schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toList());
                    dictSchoolList.stream().forEach(p -> {
                        if (schoolLevelCodes.contains(p.getSchoolLevel())) {
                            schoolIds.add(p.getSchoolId());
                        }
                    });
                }
                if (CollectionUtils.isNotEmpty(schoolIds)) {
                    schoolCount = schoolIds.size();
                    Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService()
                            .loadSchoolsExtInfoAsMap(schoolIds)
                            .getUninterruptibly();
                    if (MapUtils.isNotEmpty(extInfoMap)) {
                        allSchoolStudentCount = extInfoMap.values().stream().map(SchoolExtInfo::getSchoolSize).filter(p -> p != null).reduce(0, (x, y) -> (x + y));
                    }
                }
            }
        }

        retMap.put("schoolCount", schoolCount);
        retMap.put("allSchoolStudentCount", allSchoolStudentCount);
        Integer perSchoolCount = dbUserCount == 0 ? 0 : schoolCount / dbUserCount;
        Integer perStudentCount = dbUserCount == 0 ? 0 : allSchoolStudentCount / dbUserCount;
        retMap.put("perSchoolCount", perSchoolCount);
        retMap.put("perStudentCount", perStudentCount);
        return retMap;
    }

    private List<Integer> getCountyCodes(Collection<Integer> regionCodes) {
        if (CollectionUtils.isEmpty(regionCodes)) {
            return Collections.emptyList();
        }

        List<Integer> countyCodes = new ArrayList<>();
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
        exRegionMap.values().forEach(p -> {
            if (p.fetchRegionType() == RegionType.COUNTY) {
                countyCodes.add(p.getId());
            } else if (p.fetchRegionType() == RegionType.CITY) {
                countyCodes.addAll(p.getChildren().stream().map(ExRegion::getId).collect(Collectors.toList()));
            } else if (p.fetchRegionType() == RegionType.PROVINCE) {
                p.getChildren().forEach(c -> {
                    countyCodes.addAll(c.getChildren().stream().map(ExRegion::getId).collect(Collectors.toList()));
                });
            }
        });
        return countyCodes;
    }

    private List<SchoolLevel> convertServiceTypeToSchoolLevel(List<AgentServiceType> serviceTypes) {
        if (CollectionUtils.isEmpty(serviceTypes)) {
            return new ArrayList<>();
        }
        List<SchoolLevel> schoolLevelList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(serviceTypes)) {
            for (AgentServiceType serviceType : serviceTypes) {
                if (serviceType == AgentServiceType.PRE_SCHOOL) {
                    schoolLevelList.add(SchoolLevel.INFANT);
                } else if (serviceType == AgentServiceType.JUNIOR_SCHOOL) {
                    schoolLevelList.add(SchoolLevel.JUNIOR);
                } else if (serviceType == AgentServiceType.MIDDLE_SCHOOL) {
                    schoolLevelList.add(SchoolLevel.MIDDLE);
                } else if (serviceType == AgentServiceType.SENIOR_SCHOOL) {
                    schoolLevelList.add(SchoolLevel.HIGH);
                }
            }
        }
        return schoolLevelList;
    }

    // 工作流审核
    @RequestMapping(value = "process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage process() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        WorkFlowProcessResult processResult = WorkFlowProcessResult.typeOf(getRequestInt("processResult")); // 操作类型：  1：通过   2：拒绝  3:转发 4：撤销
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
        return workFlowServiceClient.processWorkflow("admin", adminUser.getAdminUserName(), adminUser.getRealName(), workflowId, processResult, processNote, processUserList);
    }


}
