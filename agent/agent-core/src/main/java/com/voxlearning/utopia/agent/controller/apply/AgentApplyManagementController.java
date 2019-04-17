/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.apply;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentInvoice;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;
import com.voxlearning.utopia.agent.service.apply.AgentApplyManagementService;
import com.voxlearning.utopia.agent.service.apply.AgentDictSchoolApplyService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.invoice.AgentInvoiceService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.service.workflow.AgentWorkflowService;
import com.voxlearning.utopia.agent.view.unifiedexam.ExamControlView;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.crm.api.bean.ApplyProcessResult;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.bean.ExamRank;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.api.loader.agent.DataReportApplyLoader;
import com.voxlearning.utopia.service.crm.api.service.agent.UnifiedExamApplyService;
import com.voxlearning.utopia.service.crm.consumer.loader.ApplyManagementLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.UnifiedExamApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.UnifiedExamApplyServiceClient;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.*;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author song.wang
 * @date 2016/12/29
 */
@Controller
@RequestMapping("/apply")
@Slf4j
public class AgentApplyManagementController extends AbstractAgentController {

    private static final String EXAMPLE_NAME = "统考样卷.pdf";
    private static final String EXAMPLE_PATH = "/config/templates/unified_exam_example180620.pdf";
    private static final String NOTES_NAME = "统考天权使用说明.pdf";
    private static final String NOTES_PATH = "/config/templates/unified_exam_explain180621.pdf";

    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentApplyManagementService agentApplyManagementService;
    @Inject private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject private AgentWorkflowService agentWorkflowService;
    @Inject private ApplyManagementLoaderClient applyManagementLoaderClient;
    @Inject private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private UnifiedExamApplyServiceClient unifiedExamApplyServiceClient;
    @Inject private BaseOrgService baseOrgService;
    @Inject private UnifiedExamApplyService unifiedExamApplyService;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private PaperLoaderClient paperLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private DataReportApplyLoader dataReportApplyLoader;
    @Inject private AgentDictSchoolApplyService agentDictSchoolApplyService;
    @Inject private AgentMaterialBudgetService agentMaterialBudgetService;
    @Inject private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject private UnifiedExamApplyLoaderClient unifiedExamApplyLoaderClient;

    @Inject AgentInvoiceService agentInvoiceService;
    @Inject
    private SchoolResourceService schoolResourceService;


    @Inject private com.voxlearning.utopia.service.mid_english_content.consumer.NewContentLoaderClient midContentLoaderClient;
    @Inject private com.voxlearning.utopia.service.mid_english_question.consumer.PaperLoaderClient midPaperLoaderClient;

    @RequestMapping(value = "create/index.vpage")
    String createIndex(Model model) {

        return "apply/index";
    }

    @RequestMapping(value = "create/dictschool_apply.vpage")
    public String modifyDictSchoolApplyPage(Model model) {
        model.addAttribute("schoolPopularityList", AgentSchoolPopularityType.viewSchoolPopularity());
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isCityManager()) {
            List<AgentUser> userList = baseOrgService.getManagedGroupUsers(currentUser.getUserId(), false);
            model.addAttribute("bdUserList", userList);
        }
        return "apply/dictschool_apply";
    }

    @RequestMapping(value = "create/search_school.vpage")
    @ResponseBody
    public MapMessage searchSchool() {
        Long schoolId = getRequestLong("schoolId");
        School school = raikouSystem.loadSchool(schoolId);
        //过滤出有效的学校 详见School    认证状态（0等待认证、1已认证、3未通过(假)）
        if (school == null || school.getAuthenticationState() == null || (school.getAuthenticationState() != 0 && school.getAuthenticationState() != 1)) {
            return MapMessage.errorMessage("输入的学校不存在，或者为假学校！");
        }
        //公私海场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限", mapMessage.get("schoolManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        MapMessage message = MapMessage.successMessage();
        message.put("school", school);
        String regionName = agentDictSchoolApplyService.generateRegionName(school.getRegionCode());
        message.put("regionName", regionName);
        List<Map<String, Object>> historyApplyList = agentDictSchoolApplyService.getDictSchoolApplyList(schoolId);
        message.put("historyApplyList", historyApplyList);
        AgentDictSchool dictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (dictSchool != null) {
            message.put("engMode", dictSchool.getEngMode());
            message.put("mathMode", dictSchool.getMathMode());
            message.put("schoolPopularity", dictSchool.getSchoolPopularity());
        } else {
            message.put("engMode", 0);
            message.put("mathMode", 0);
            message.put("schoolPopularity", "");
        }
        return message;
    }

    @RequestMapping(value = "create/submit_dictschool_apply.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("e97c7ac129cb4051")
    public MapMessage submitModifyDicSchoolApply() {
        AuthCurrentUser currentUser = getCurrentUser();
        Integer modifyType = requestInteger("modifyType");
        Long schoolId = requestLong("schoolId");
        String comment = getRequestString("comment");
        AgentSchoolPopularityType schoolPopularity = AgentSchoolPopularityType.of(getRequestString("schoolPopularity"));
        if (modifyType == 0 || modifyType > 3) {
            return MapMessage.errorMessage("审核类型无效！");
        }
        if (StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("未填写申请原因，请重新填写！");
        }
        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());
        if (AgentRoleType.BusinessDeveloper != userRole && AgentRoleType.CityManager != userRole) {
            return MapMessage.errorMessage("您无权创建该类申请！");
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("输入的学校不存在！");
        }

        //公私海场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限", mapMessage.get("schoolManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }

        MapMessage checkMessage = agentDictSchoolApplyService.checkModifyDictSchool(currentUser.getUserId(), school, modifyType, schoolPopularity);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }

        // 指定分配给的专员
        Long targetUserId = null;
        if (modifyType == 1) {
            if (AgentRoleType.BusinessDeveloper == userRole) {
                targetUserId = currentUser.getUserId();
            } else {
                targetUserId = requestLong("targetUserId");
            }
        }

        return agentDictSchoolApplyService.saveModifyDicSchoolApply(modifyType, school, comment, schoolPopularity, targetUserId);
    }

    private Long fetchWorkflowId(MapMessage mapMessage) {
        if (!mapMessage.isSuccess()) {
            return null;
        }
        WorkFlowRecord workFlowRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");
        if (workFlowRecord == null) {
            return null;
        }
        return workFlowRecord.getId();
    }


    @RequestMapping(value = "view/list.vpage", method = RequestMethod.GET)
    public String viewListPage(Model model) {
        Long userId = getCurrentUserId();
        Integer statusCode = getRequestInt("status");
        ApplyStatus status = ApplyStatus.typeOf(statusCode);
        if (status == null) {
            status = ApplyStatus.PENDING;
        }
        List<AbstractBaseApply> applyList = agentApplyManagementService.fetchUserApplyList(userId, status, true);
        model.addAttribute("statusList", ApplyStatus.values());
        model.addAttribute("selectStatus", statusCode);
        model.addAttribute("applyList", applyList);
        model.addAttribute("status", status);
        return "apply/list";
    }

    @RequestMapping(value = "view/apply_datail.vpage")
    public String viewApplyDetail(Model model) {
        Long applyId = requestLong("applyId");
        Long workflowId = requestLong("workflowId");
        String applyTypeStr = getRequestString("applyType");
        ApplyType applyType = null;
        if (StringUtils.isNotBlank(applyTypeStr)) {
            applyType = ApplyType.nameOf(applyTypeStr);
        }
        if (applyType == null || (applyId == null && workflowId == null)) {
            try {
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return "";
            } catch (Exception e) {
            }
        }

        ApplyWithProcessResultData applyDetail = null;
        if (applyId != null) {
            applyDetail = agentApplyManagementService.getApplyDetailWithProcessResultByApplyId(applyType, applyId);
            model.addAttribute("page_num", 3);
        } else {
            applyDetail = agentApplyManagementService.getApplyDetailByWorkflowId(applyType, workflowId, true);
            model.addAttribute("page_num", 11);
        }
        model.addAttribute("applyData", applyDetail);
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            return "apply/dictschool_apply_detail";
        } else if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
            AgentOrder agentOrder = (AgentOrder) applyDetail.getApply();
            if (agentOrder.getInvoiceId() != null) {
                AgentInvoice agentInvoice = agentInvoiceService.findAgentInvoiceById(agentOrder.getInvoiceId());
                model.addAttribute("agentInvoice", agentInvoice);
            }
            return "apply/shopping_car_detail";
        } else if (ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType) {
            UnifiedExamApply unifiedExamApply = (UnifiedExamApply) applyDetail.getApply();
            if (ObjectUtils.equals(unifiedExamApply.getRegionLeve(), "school")) {
                String[] schoolIdStr = unifiedExamApply.getUnifiedExamSchool().split(",");
                List<Long> managedSchools = new ArrayList<>();
                for (String s : schoolIdStr) {
                    managedSchools.add(Long.valueOf(s));
                }
                Map<Long, CrmSchoolSummary> schools = crmSummaryLoaderClient.loadSchoolSummary(managedSchools);
                model.addAttribute("schoolList", schools.values());
            }
            model.addAttribute("tikuDomain", getTikuDomain());
            return "apply/unified_exam_detail";
        } else if (ApplyType.AGENT_DATA_REPORT_APPLY == applyType) {
            String account = applyDetail.getApply().getAccount();
            List<DataReportApply> historyApplies = dataReportApplyLoader.loadByAccount(SystemPlatformType.AGENT, account);
            model.addAttribute("historyApplies", historyApplies);
/*            if (applyDetail.getApply() != null) {
                DataReportApply apply = (DataReportApply) applyDetail.getApply();
                if (DateUtils.dayDiff(new Date(), apply.getUpdateDatetime()) > 7L) {
                    apply.setFirstDocument(null);
                    apply.setSecondDocument(null);
                }
            }*/
            return "apply/datareport/data_report_detail";
        }
        return "apply/list";
    }


    @RequestMapping(value = "create/revoke_apply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage revokeApply() {
        Long applyId = requestLong("applyId");
        String applyTypeStr = getRequestString("applyType");
        ApplyType applyType = null;
        if (StringUtils.isNotBlank(applyTypeStr)) {
            applyType = ApplyType.nameOf(applyTypeStr);
        }
        if (applyType == null || applyId == null) {
            return MapMessage.errorMessage("参数有误！");
        }
        AbstractBaseApply apply = applyManagementLoaderClient.fetchApplyDetail(applyType, applyId);
        if (apply == null) {
            return MapMessage.errorMessage("申请记录不存在！");
        }
        if (ApplyType.AGENT_MATERIAL_APPLY.equals(applyType)) {
            AgentOrder agentOrder = (AgentOrder) apply;
            if (AgentOrderPaymentMode.MATERIAL_COST.getPayId().equals(agentOrder.getPaymentMode())) {
                AgentMaterialBudget userMaterialBudget = agentMaterialBudgetService.getUserMaterialBudget(agentOrder.getCreator());
                if (null == userMaterialBudget || userMaterialBudget.getCreateTime().after(agentOrder.getOrderTime())) {
                    return MapMessage.errorMessage("订单已过期，不能撤销！");
                }
            }
        }
        // 判断是否可撤销
        boolean flag = agentApplyManagementService.judgeCanRevokeApply(applyType, applyId);

        // 执行撤销操作
        if (flag && apply.getWorkflowId() != null) {
            MapMessage msg = agentWorkflowService.processWorkflow(getCurrentUser(), apply.getWorkflowId(), WorkFlowProcessResult.revoke, "撤销", null);
            if (msg.isSuccess() && applyType == ApplyType.AGENT_MATERIAL_APPLY) {
                ApplyWithProcessResultData processResultData = applyManagementLoaderClient.fetchApplyWithProcessResultByApplyId(applyType, apply.getId(), false);
                if (processResultData != null) {
                    List<ApplyProcessResult> applyProcessResults = processResultData.getProcessResultList();
                    if (CollectionUtils.isNotEmpty(applyProcessResults)) {
                        applyProcessResults.forEach(p -> sendSmsToProcesser(p, (AgentOrder) apply));
                    }
                }
            }
            return msg;
        }
        return MapMessage.errorMessage();
    }

    private void sendSmsToProcesser(ApplyProcessResult processResult, AgentOrder order) {
        if (processResult == null || order == null || processResult.getUserPlatform() != SystemPlatformType.AGENT || Objects.equals(processResult.getAccount(), order.getAccount())) {
            return;
        }
        Long agentUserId = SafeConverter.toLong(processResult.getAccount());
        AgentUser user = baseOrgService.getUser(agentUserId);
        if (user == null) {
            return;
        }
        String tel = user.getTel();
        if (MobileRule.isMobile(tel)) {
            smsServiceClient.createSmsMessage(tel)
                    .content(StringUtils.formatMessage("您于{}审批通过的物料申请已被{}撤销，订单号：{}",
                            DateUtils.dateToString(processResult.getProcessDate(), DateUtils.FORMAT_SQL_DATE), order.getAccountName(), order.getId()))
                    .send();
        }
    }


    @RequestMapping(value = "manage/all_list.vpage", method = RequestMethod.GET)
    @OperationCode("da435ccb7a584086")
    public String viewAllListPage(Model model) {
        List<ApplyType> applyTypeList = new ArrayList<>();
        applyTypeList.add(ApplyType.AGENT_MODIFY_DICT_SCHOOL);
        applyTypeList.add(ApplyType.AGENT_MATERIAL_APPLY);
        applyTypeList.add(ApplyType.AGENT_UNIFIED_EXAM_APPLY);
        applyTypeList.add(ApplyType.AGENT_DATA_REPORT_APPLY);

        String applyTypeStr = getRequestString("applyType");
        ApplyType applyType = ApplyType.nameOf(applyTypeStr);
        if (applyType == null) {
            applyType = applyTypeList.get(0);
        }

        Date endDate = requestDate("endDate");
        if (endDate == null) {
            endDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyyMMdd"), "yyyyMMdd");
        }
        Date startDate = requestDate("startDate");
        if (startDate == null) {
            startDate = DateUtils.addDays(endDate, -1);
        }

        // 超过7天
        if (startDate.before(DateUtils.addDays(endDate, -7))) {
            startDate = DateUtils.addDays(endDate, -7);
        }

        List<AbstractBaseApply> applyList = agentApplyManagementService.fetchApplyListByTypeAndDate(applyType, startDate, DayUtils.addDay(endDate, 1));
        List<Map<String, Object>> applyMapList = convertToApplyMapList(applyList, applyType);

        model.addAttribute("applyTypeList", applyTypeList);
        model.addAttribute("selectType", applyType);
        model.addAttribute("startDate", DateUtils.dateToString(startDate, "yyyy-MM-dd"));
        model.addAttribute("endDate", DateUtils.dateToString(endDate, "yyyy-MM-dd"));
        model.addAttribute("applyList", applyMapList);
        return "apply/all_list";
    }

    private List<Map<String, Object>> convertToApplyMapList(List<AbstractBaseApply> applyList, ApplyType applyType) {
        List<Map<String, Object>> applyMapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(applyList)) {
            if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
                List<Map<String, Object>> dictSchoolApplyMapList = applyList.stream().map(p -> this.convertModifyDictSchoolApply((AgentModifyDictSchoolApply) p)).filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(dictSchoolApplyMapList)) {
                    applyMapList.addAll(dictSchoolApplyMapList);
                }
            }
            if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
                List<Map<String, Object>> materialApplyMapList = applyList.stream().map(p -> this.convertMaterialApply((AgentOrder) p)).filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(materialApplyMapList)) {
                    applyMapList.addAll(materialApplyMapList);
                }
            }
            if (ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType) {
                List<Map<String, Object>> materialApplyMapList = applyList.stream().map(p -> this.convertUnifiedExamApply((UnifiedExamApply) p)).filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(materialApplyMapList)) {
                    applyMapList.addAll(materialApplyMapList);
                }
            }
            if (ApplyType.AGENT_DATA_REPORT_APPLY == applyType) {
                List<Map<String, Object>> dataReportApplyMapList = applyList.stream().map(p -> this.convertDataReportApply((DataReportApply) p)).filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(dataReportApplyMapList)) {
                    applyMapList.addAll(dataReportApplyMapList);
                }
            }
        }
        return applyMapList;
    }

    private Map<String, Object> convertMaterialApply(AgentOrder apply) {
        if (apply == null) {
            return null;
        }
        AgentOrderType orderType = AgentOrderType.of(apply.getOrderType());
        if (orderType == null || AgentOrderType.BUY_MATERIAL != orderType) {
            return null;
        }

        Map<String, Object> retMap = BeanMapUtils.tansBean2Map(apply);
        String groupName = "";
        String parentGroupName = "";
        if (apply.getAccount() != null) {
            Map<String, String> result = getOrgInfoByUserId(SafeConverter.toLong(apply.getAccount()));
            groupName = result.get("groupName");
            parentGroupName = result.get("parentGroupName");
        }
        retMap.put("groupName", groupName);
        retMap.put("parentGroupName", parentGroupName);
        List<AgentOrderProduct> orderProducts = agentOrderLoaderClient.findAgentOrderProductByOrderId(SafeConverter.toLong(retMap.get("id")));
        retMap.put("orderProducts", createProductInfo(orderProducts));
        retMap.put("consigneeInfo", createConsigneeInfo(apply));

        ApplyStatus applyStatus = apply.getStatus();
        String applyStatusStr = "";
        if (applyStatus != null) {
            applyStatusStr = applyStatus.getDesc();
        }
        if (apply.getWorkflowId() != null) {
            WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(apply.getWorkflowId());
            if (Objects.equals(workFlowRecord.getStatus(), "init")) {
                applyStatusStr = "市经理审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv1")) {
                applyStatusStr = "财务审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv2")) {
                applyStatusStr = "销运审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv3")) {
                applyStatusStr = "审核通过";
            } else if (Objects.equals(workFlowRecord.getStatus(), "reject") || Objects.equals(workFlowRecord.getStatus(), "rejected")) {
                applyStatusStr = "已驳回";
            } else if (Objects.equals(workFlowRecord.getStatus(), "revoke")) {
                applyStatusStr = "撤回";
            }
        }
        retMap.put("applyStatus", applyStatusStr);


        //fixme 产品展示
        return retMap;
    }

    private Map<String, String> getOrgInfoByUserId(Long userId) {
        Map<String, String> result = new HashMap<>();
        List<AgentGroup> groupList = baseOrgService.getUserGroups(userId);
        if (CollectionUtils.isNotEmpty(groupList)) {
            AgentGroup group = groupList.get(0);
            if (group.fetchGroupRoleType() == AgentGroupRoleType.Area) {//区域
                result.put("groupName", group.getGroupName());
            } else {
                result.put("groupName", "");
            }
            AgentGroup parentGroup = baseOrgService.getParentGroupByRole(group.getId(), AgentGroupRoleType.Region);
            result.put("parentGroupName", parentGroup == null ? "" : parentGroup.getGroupName());
            AgentGroup cityGroup = null;
            if (group.fetchGroupRoleType() == AgentGroupRoleType.City) {//如果是分区
                result.put("cityGroupName", group.getGroupName());
                cityGroup = baseOrgService.getParentGroupByRole(group.getId(), AgentGroupRoleType.Area);//查下区域
                result.put("groupName", cityGroup == null ? "" : cityGroup.getGroupName());
            } else {
                result.put("cityGroupName", "");
            }

        }
        return result;
    }

    private List<String> createProductInfo(List<AgentOrderProduct> orderProducts) {
        List<String> productInfo = new ArrayList<>();
        orderProducts.forEach(p -> productInfo.add(StringUtils.formatMessage("{}:{}元*{}，{}元 ", p.getProductName(), p.getPrice(), p.getProductQuantity(), p.getPrice() * p.getProductQuantity())));
        return productInfo;
    }

    private List<String> createConsigneeInfo(AgentOrder apply) {
        List<String> consigneeInfo = new ArrayList<>();
        consigneeInfo.add("收货人：" +
                apply.getConsignee());
        consigneeInfo.add("收货地址："
                + (apply.getProvince() == null ? "" : apply.getProvince())
                + (apply.getCity() == null ? "" : apply.getCity())
                + (apply.getCounty() == null ? "" : apply.getCounty())
                + apply.getAddress());
        consigneeInfo.add("联系电话：" +
                apply.getMobile());
        return consigneeInfo;
    }

    private Map<String, Object> convertModifyDictSchoolApply(AgentModifyDictSchoolApply apply) {
        if (apply == null) {
            return null;
        }
        Map<String, Object> retMap = BeanMapUtils.tansBean2Map(apply);
        String groupName = "";
        String parentGroupName = "";
        if (apply.getAccount() != null) {
            Map<String, String> result = getOrgInfoByUserId(SafeConverter.toLong(apply.getAccount()));
            groupName = result.get("groupName");
            parentGroupName = result.get("parentGroupName");
        }
        retMap.put("groupName", groupName);
        retMap.put("parentGroupName", parentGroupName);

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(apply.getSchoolId())
                .getUninterruptibly();
        Integer schoolSize = 0;
        if (schoolExtInfo != null && schoolExtInfo.getSchoolSize() != null) {
            schoolSize = schoolExtInfo.getSchoolSize();
        }
        retMap.put("schoolSize", schoolSize);
        ApplyStatus applyStatus = apply.getStatus();
        String applyStatusStr = "";
        if (applyStatus != null) {
            applyStatusStr = applyStatus.getDesc();
        }
        if (apply.getWorkflowId() != null) {
            WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(apply.getWorkflowId());
            if (Objects.equals(workFlowRecord.getStatus(), "init")) {
                applyStatusStr = "市经理审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv1")) {
                applyStatusStr = "风控审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv2")) {
                applyStatusStr = "风控已通过";
            } else if (Objects.equals(workFlowRecord.getStatus(), "reject_init")) {
                applyStatusStr = "市经理已拒绝";
            } else if (Objects.equals(workFlowRecord.getStatus(), "reject_lv1")) {
                applyStatusStr = "风控已拒绝";
            }
        }
        retMap.put("applyStatus", applyStatusStr);
        return retMap;
    }

    @RequestMapping(value = "manage/download.vpage", method = RequestMethod.GET)
    public void dowloadApplyData(HttpServletResponse response) {

        try {

            String applyTypeStr = getRequestString("applyType");
            ApplyType applyType = ApplyType.nameOf(applyTypeStr);
            if (applyType == null) {
                response.getWriter().write("请选择申请类型");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Date endDate = requestDate("endDate");
            if (endDate == null) {
                endDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyyMMdd"), "yyyyMMdd");
            }

            Date startDate = requestDate("startDate");
            if (startDate == null) {
                startDate = DateUtils.addDays(endDate, -1);
            }

            // 超过7天
            if (startDate.before(DateUtils.addDays(endDate, -7))) {
                startDate = DateUtils.addDays(endDate, -7);
            }

            List<AbstractBaseApply> applyList = agentApplyManagementService.fetchApplyListByTypeAndDate(applyType, startDate, DayUtils.addDay(endDate, 1));
            List<Map<String, Object>> applyMapList = convertToApplyMapList(applyList, applyType);

            XSSFWorkbook workbook = null;
            String filename = "申请下载-";
            if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
                workbook = generateDictSchoolApplyExcel(applyMapList);
                filename += "字典表申请-";
            }
            if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
                workbook = generateMaterialApplyExcel(applyMapList);
                filename += "物料表申请-";
            }
            if (ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType) {
                workbook = generateUnifiedExamApplyExcel(applyMapList);
                filename += "统考申请-";
            }
            if (ApplyType.AGENT_DATA_REPORT_APPLY == applyType) {
                workbook = generateDataReportApplyExcel(applyMapList);
                filename += "大数据报告申请-";
            }
            if (workbook == null) {
                response.getWriter().write("下载失败");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            filename += DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }
    }

    private XSSFWorkbook generateDictSchoolApplyExcel(List<Map<String, Object>> applyMapList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "申请日期");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "大区");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "部门");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "申请人");
            XssfUtils.setCellValue(firstRow, 4, cellStyle, "操作类型");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "学校ID");
            XssfUtils.setCellValue(firstRow, 6, cellStyle, "学校名称");
            XssfUtils.setCellValue(firstRow, 7, cellStyle, "阶段");
            XssfUtils.setCellValue(firstRow, 8, cellStyle, "学校规模");
            XssfUtils.setCellValue(firstRow, 9, cellStyle, "备注");
            XssfUtils.setCellValue(firstRow, 10, cellStyle, "状态");
            if (CollectionUtils.isNotEmpty(applyMapList)) {
                Integer index = 1;
                for (Map<String, Object> applyMap : applyMapList) {
                    XSSFRow row = sheet.createRow(index++);
                    XssfUtils.setCellValue(row, 0, cellStyle, DateUtils.dateToString((Date) applyMap.get("createDatetime"), DateUtils.FORMAT_SQL_DATE));
                    XssfUtils.setCellValue(row, 1, cellStyle, (String) applyMap.get("parentGroupName"));
                    XssfUtils.setCellValue(row, 2, cellStyle, (String) applyMap.get("groupName"));
                    XssfUtils.setCellValue(row, 3, cellStyle, (String) applyMap.get("accountName"));
                    Integer modifyType = (Integer) applyMap.get("modifyType");
                    String modifyTypeStr = "";
                    if (modifyType != null) {
                        if (modifyType == 1) {
                            modifyTypeStr = "添加学校";
                        } else if (modifyType == 2) {
                            modifyTypeStr = "删除学校";
                        } else if (modifyType == 3) {
                            modifyTypeStr = "业务变更";
                        }
                    }
                    XssfUtils.setCellValue(row, 4, cellStyle, modifyTypeStr);
                    XssfUtils.setCellValue(row, 5, cellStyle, (Long) applyMap.get("schoolId"));
                    XssfUtils.setCellValue(row, 6, cellStyle, (String) applyMap.get("schoolName"));

                    Integer schoolLevel = (Integer) applyMap.get("schoolLevel");
                    String schoolLevelStr = "";
                    if (schoolLevel != null) {
                        if (schoolLevel == 1) {
                            schoolLevelStr = "小学";
                        } else if (schoolLevel == 2) {
                            schoolLevelStr = "初中";
                        } else if (schoolLevel == 4) {
                            schoolLevelStr = "高中";
                        } else if (schoolLevel == 5) {
                            schoolLevelStr = "学前";
                        }
                    }
                    XssfUtils.setCellValue(row, 7, cellStyle, schoolLevelStr);
                    XssfUtils.setCellValue(row, 8, cellStyle, applyMap.get("schoolSize") != null ? (Integer) applyMap.get("schoolSize") : 0);
                    XssfUtils.setCellValue(row, 9, cellStyle, (String) applyMap.get("comment"));
                    XssfUtils.setCellValue(row, 10, cellStyle, (String) applyMap.get("applyStatus"));
                }
            }
            return workbook;
        } catch (Exception ex) {
            return null;
        }
    }

    private XSSFWorkbook generateMaterialApplyExcel(List<Map<String, Object>> applyMapList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "申请日期");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "大区");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "部门");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "申请人");
            XssfUtils.setCellValue(firstRow, 4, cellStyle, "购买商品");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "订单金额");
            XssfUtils.setCellValue(firstRow, 6, cellStyle, "备注");
            XssfUtils.setCellValue(firstRow, 7, cellStyle, "收获信息");
            XssfUtils.setCellValue(firstRow, 8, cellStyle, "支付方式");
            XssfUtils.setCellValue(firstRow, 9, cellStyle, "状态");
            if (CollectionUtils.isNotEmpty(applyMapList)) {
                int index = 1;
                for (Map<String, Object> applyMap : applyMapList) {
                    XSSFRow row = sheet.createRow(index++);
                    XssfUtils.setCellValue(row, 0, cellStyle, DateUtils.dateToString((Date) applyMap.get("orderTime"), DateUtils.FORMAT_SQL_DATE));
                    XssfUtils.setCellValue(row, 1, cellStyle, SafeConverter.toString(applyMap.get("parentGroupName")));
                    XssfUtils.setCellValue(row, 2, cellStyle, SafeConverter.toString(applyMap.get("groupName")));
                    XssfUtils.setCellValue(row, 3, cellStyle, SafeConverter.toString(applyMap.get("accountName")));
                    XssfUtils.setCellValue(row, 4, cellStyle, applyMap.get("orderProducts") == null ? "" : StringUtils.join((List<String>) applyMap.get("orderProducts"), ","));
                    XssfUtils.setCellValue(row, 5, cellStyle, SafeConverter.toString(applyMap.get("orderAmount")));
                    XssfUtils.setCellValue(row, 6, cellStyle, SafeConverter.toString(applyMap.get("orderNotes")));
                    XssfUtils.setCellValue(row, 7, cellStyle, applyMap.get("consigneeInfo") == null ? "" : StringUtils.join((List<String>) applyMap.get("consigneeInfo"), ","));
                    Integer paymentModeId = SafeConverter.toInt(applyMap.get("paymentMode"));
                    AgentOrderPaymentMode paymentMode = AgentOrderPaymentMode.safePayIdToMode(paymentModeId, null);
                    XssfUtils.setCellValue(row, 8, cellStyle, paymentMode == null ? "" : paymentMode.getPayDes());

                    ApplyStatus status = applyMap.get("status") == null ? null : (ApplyStatus) applyMap.get("status");
                    XssfUtils.setCellValue(row, 9, cellStyle, (String) applyMap.get("applyStatus"));
                }
            }
            return workbook;
        } catch (Exception ex) {
            return null;
        }
    }

    private XSSFWorkbook generateUnifiedExamApplyExcel(List<Map<String, Object>> applyMapList) {
        try {

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            XSSFCellStyle headCellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 居中
            headCellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            headCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            headCellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
            headCellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, headCellStyle, "申请日期");
            XssfUtils.setCellValue(firstRow, 1, headCellStyle, "申请人");
            XssfUtils.setCellValue(firstRow, 2, headCellStyle, "大区");
            XssfUtils.setCellValue(firstRow, 3, headCellStyle, "区域");
            XssfUtils.setCellValue(firstRow, 4, headCellStyle, "分区");
            XssfUtils.setCellValue(firstRow, 5, headCellStyle, "级别");
            XssfUtils.setCellValue(firstRow, 6, headCellStyle, "城市");
            XssfUtils.setCellValue(firstRow, 7, headCellStyle, "地区");
            XssfUtils.setCellValue(firstRow, 8, headCellStyle, "学校");
            XssfUtils.setCellValue(firstRow, 9, headCellStyle, "学科");
            XssfUtils.setCellValue(firstRow, 10, headCellStyle, "年级");
            XssfUtils.setCellValue(firstRow, 11, headCellStyle, "统考名称");
            XssfUtils.setCellValue(firstRow, 12, headCellStyle, "审核状态");
            XssfUtils.setCellValue(firstRow, 13, headCellStyle, "录入状态");
            XssfUtils.setCellValue(firstRow, 14, headCellStyle, "是否重复使用已录入试卷");
            if (CollectionUtils.isNotEmpty(applyMapList)) {
                int index = 1;
                for (Map<String, Object> applyMap : applyMapList) {
                    XSSFRow row = sheet.createRow(index++);
                    XssfUtils.setCellValue(row, 0, cellStyle, DateUtils.dateToString((Date) applyMap.get("createDatetime"), DateUtils.FORMAT_SQL_DATE));
                    XssfUtils.setCellValue(row, 1, cellStyle, SafeConverter.toString(applyMap.get("accountName")));
                    XssfUtils.setCellValue(row, 2, cellStyle, SafeConverter.toString(applyMap.get("parentGroupName")));
                    XssfUtils.setCellValue(row, 3, cellStyle, SafeConverter.toString(applyMap.get("groupName")));
                    XssfUtils.setCellValue(row, 4, cellStyle, SafeConverter.toString(applyMap.get("cityGroupName")));
                    String regionLevel = SafeConverter.toString(applyMap.get("regionLeve"));
                    XssfUtils.setCellValue(row, 5, cellStyle, "city".equals(regionLevel) ? "市级" : ("country".equals(regionLevel) ? "区级" : "校级"));
                    XssfUtils.setCellValue(row, 6, cellStyle, SafeConverter.toString(applyMap.get("cityName")));
                    XssfUtils.setCellValue(row, 7, cellStyle, SafeConverter.toString(applyMap.get("regionName")));
                    StringBuffer sbf = new StringBuffer();
                    if ("school".equals(regionLevel)) {
                        String schoolIds = SafeConverter.toString(applyMap.get("unifiedExamSchool"));
                        String[] idArr = schoolIds.split(",");
                        for (int i = 0; i < idArr.length; i++) {
                            CrmSchoolSummary crmSchoolSummary = crmSummaryLoaderClient.loadSchoolSummary(SafeConverter.toLong(idArr[i]));
                            sbf.append(idArr[i]).append(":").append(crmSchoolSummary.getSchoolName());
                            if (i != (idArr.length - 1)) {
                                sbf.append(",");
                            }
                        }

                    }
                    XssfUtils.setCellValue(row, 8, cellStyle, sbf.toString());
                    String subject = SafeConverter.toString(applyMap.get("subject"));
                    String subjectContent = "";
                    switch (subject) {
                        case "101":
                            subjectContent = "小学语文";
                            break;
                        case "102":
                            subjectContent = "小学数学";
                            break;
                        case "103":
                            subjectContent = "小学英语";
                            break;
                        case "201":
                            subjectContent = "初中语文";
                            break;
                        case "202":
                            subjectContent = "初中数学";
                            break;
                        case "203":
                            subjectContent = "初中英语";
                            break;
                        default:
                            subjectContent = "暂无学科";
                    }

                    XssfUtils.setCellValue(row, 9, cellStyle, subjectContent);
//                    Integer  level = SafeConverter.toInt(applyMap.get("gradeLevel"));
//                    List<Integer>  list = UnifiedExamApply.fetchGradeLevel(level);
                    Integer level = SafeConverter.toInt(applyMap.get("gradeLevel"));
                    String gradeLevelStr = "";
                    switch (level) {
                        case 1:
                            gradeLevelStr = "一年级";
                            break;
                        case 2:
                            gradeLevelStr = "二年级";
                            break;
                        case 3:
                            gradeLevelStr = "三年级";
                            break;
                        case 4:
                            gradeLevelStr = "四年级";
                            break;
                        case 5:
                            gradeLevelStr = "五年级";
                            break;
                        case 6:
                            gradeLevelStr = "六年级";
                            break;
                        case 7:
                            gradeLevelStr = "七年级";
                            break;
                        case 8:
                            gradeLevelStr = "八年级";
                            break;
                        case 9:
                            gradeLevelStr = "九年级";
                            break;
                        default:
                            gradeLevelStr = "年级错误";
                    }
                    XssfUtils.setCellValue(row, 10, cellStyle, gradeLevelStr);
                    XssfUtils.setCellValue(row, 11, cellStyle, SafeConverter.toString(applyMap.get("unifiedExamName")));
                    UnifiedExamApplyStatus unifiedExamApplyStatus = applyMap.get("unifiedExamStatus") == null ? null : (UnifiedExamApplyStatus) applyMap.get("unifiedExamStatus");
                    XssfUtils.setCellValue(row, 12, cellStyle, unifiedExamApplyStatus == null ? "" : unifiedExamApplyStatus.getDesc());
                    TestPaperEntryStatus entryStatus = applyMap.get("entryStatus") == null ? null : (TestPaperEntryStatus) applyMap.get("entryStatus");
                    XssfUtils.setCellValue(row, 13, cellStyle, entryStatus == null ? "" : entryStatus.getDesc());
                    XssfUtils.setCellValue(row, 14, cellStyle, "NEWLYADDED".equals(SafeConverter.toString(applyMap.get("testPaperSourceType"))) ? "否" : "是");
                }
            }
            return workbook;
        } catch (Exception ex) {
            return null;
        }
    }

    private XSSFWorkbook generateDataReportApplyExcel(List<Map<String, Object>> applyMapList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "申请日期");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "大区");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "部门");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "申请人");
            XssfUtils.setCellValue(firstRow, 4, cellStyle, "级别");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "申请学科");
            XssfUtils.setCellValue(firstRow, 6, cellStyle, "时间维度");
            XssfUtils.setCellValue(firstRow, 7, cellStyle, "状态");
            if (CollectionUtils.isNotEmpty(applyMapList)) {
                int index = 1;
                for (Map<String, Object> applyMap : applyMapList) {
                    XSSFRow row = sheet.createRow(index++);
                    XssfUtils.setCellValue(row, 0, cellStyle, DateUtils.dateToString((Date) applyMap.get("createDatetime"), DateUtils.FORMAT_SQL_DATE));
                    XssfUtils.setCellValue(row, 1, cellStyle, SafeConverter.toString(applyMap.get("parentGroupName")));
                    XssfUtils.setCellValue(row, 2, cellStyle, SafeConverter.toString(applyMap.get("groupName")));
                    XssfUtils.setCellValue(row, 3, cellStyle, SafeConverter.toString(applyMap.get("accountName")));
                    Integer reportLevel = SafeConverter.toInt(applyMap.get("reportLevel"));
                    XssfUtils.setCellValue(row, 4, cellStyle, reportLevel == 1 ? "市级" : (reportLevel == 2 ? "区级" : "校级"));
                    XssfUtils.setCellValue(row, 5, cellStyle, SafeConverter.toInt(applyMap.get("subject")) == 1 ? "小学英语" : "小学数学");
                    XssfUtils.setCellValue(row, 6, cellStyle, SafeConverter.toString(applyMap.get("timeDimensionality")));
                    XssfUtils.setCellValue(row, 7, cellStyle, SafeConverter.toString(applyMap.get("applyStatus")));
                }
            }
            return workbook;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 统考申请
     * 跳转界面
     *
     * @param model
     * @return
     */
    @RequestMapping("create/unified_exam_apply_view.vpage")
    public String unifiedExamApplyView(Model model) {
        Long applyId = requestLong("applyId");
        Subject subject = Subject.JENGLISH; //默认展示小学英语
        Integer cityCodeDefault = 0;
        //当前用户默认城市信息
        AuthCurrentUser currentUser = getCurrentUser();
        List<Map<String, Object>> cityInfoList = agentApplyManagementService.getCityManagerAdministerCityInfo(currentUser);
        if (CollectionUtils.isNotEmpty(cityInfoList)) {
            model.addAttribute("cityInfoList", cityInfoList);
            cityCodeDefault = (Integer) cityInfoList.get(0).get("cityCode");
        }
        //重新申请
        if (applyId != null && applyId > 0) {
            //直接查询会好些，暂时不动了有时间在修改下
            ApplyWithProcessResultData applyDetail = agentApplyManagementService.getApplyDetailWithProcessResultByApplyId(ApplyType.AGENT_UNIFIED_EXAM_APPLY, applyId);
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
            model.addAttribute("applyDetail", applyDetail);
            cityCodeDefault = Integer.valueOf(unifiedExamApply.getCityCode());
            subject = Subject.fromSubjectId(Integer.valueOf(unifiedExamApply.getSubject()));//此处科目不可能为空
        }
        //默认城市信息
        model.addAttribute("cityCodeDefault", cityCodeDefault);
        Long groupId = 0L;
        if (CollectionUtils.isNotEmpty(cityInfoList)) {
            groupId = (Long) cityInfoList.get(0).get("groupId");
        }
        model.addAttribute("regionList", getRegionInfoByGroupIdAndCityCode(groupId, cityCodeDefault));
        AgentUser au = baseOrgService.getUser(getCurrentUserId());
        model.addAttribute("sendEmail", au.getEmail());
        model.addAttribute("agentUserId", currentUser.getUserId());
        //学科对应的课本信息
        List<Object> nbpList = new ArrayList<>();
        if (subject.getId() < 201) { // 小学
            nbpList.addAll(newContentLoaderClient.loadBooks(subject).stream().filter(t -> !t.isDeletedTrue()).collect(Collectors.toList()));
        } else { // 中学
            nbpList.addAll(midContentLoaderClient.loadBooks(subject).stream().filter(t -> !t.isDeletedTrue()).collect(Collectors.toList()));
        }
        model.addAttribute("bookProfiles", nbpList);
        return "apply/unified_exam_apply";
    }

    @RequestMapping(value = "create/download_example.vpage", method = RequestMethod.GET)
    public void downloadExample(HttpServletResponse response) {
        try {
            String type = getRequestString("type");//1下载统考样卷 2 下载统考须知
            Resource resource = new ClassPathResource("1".equals(type) ? EXAMPLE_PATH : NOTES_PATH);
            if (!resource.exists()) {
                logger.error("example is not exists");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    "1".equals(type) ? EXAMPLE_NAME : NOTES_NAME,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("download example is failed", ex);
        }
    }

    /**
     * 统考申请
     *
     * @return
     */
    @RequestMapping(value = "create/unified_exam_apply.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("2cf618848bb2437d")
    public MapMessage unifiedExamApply(HttpServletRequest request) {
        AuthCurrentUser currentUser = getCurrentUser();
        //获取前端传递对象
        String unifiedExamApplyJson = getRequestString("unifiedExamApply");
        String ranks = getRequestString("ranks");
        UnifiedExamApply unifiedExamApply = JsonUtils.fromJson(unifiedExamApplyJson, UnifiedExamApply.class);
        unifiedExamApply.setRanks(ranks);
        MapMessage checkMessage = checkUnifiedExamApply(unifiedExamApply);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }
        if (unifiedExamApply.fetchTestGradeType() != UnifiedExamTestGradeType.GRADE) {
            unifiedExamApply.setRanks(null);
        }
        MapMessage message = MapMessage.successMessage();
        unifiedExamApply.setUpdateDatetime(new Date());
        unifiedExamApply.setCreateDatetime(new Date());
        unifiedExamApply.setAccount(String.valueOf(currentUser.getUserId()));
        unifiedExamApply.setAccountName(currentUser.getRealName());
        unifiedExamApply.setApplyType(ApplyType.AGENT_UNIFIED_EXAM_APPLY);
        unifiedExamApply.setStatus(ApplyStatus.PENDING);
        unifiedExamApply.setUserPlatform(SystemPlatformType.AGENT);
        unifiedExamApply.setUnifiedExamStatus(UnifiedExamApplyStatus.SO_PENDING);

        if (unifiedExamApply.getId() == null) {
            // 创建工作流
            unifiedExamApply = unifiedExamApplyService.persist(unifiedExamApply);
            WorkFlowRecord workFlowRecord = new WorkFlowRecord();
            workFlowRecord.setStatus("lv1");
            workFlowRecord.setSourceApp("agent");
            workFlowRecord.setTaskName("统考申请");
            workFlowRecord.setTaskContent(unifiedExamApply.generateSummary());
            workFlowRecord.setLatestProcessorName(currentUser.getRealName());
            workFlowRecord.setCreatorName(currentUser.getRealName());
            workFlowRecord.setCreatorAccount(String.valueOf(currentUser.getUserId()));
            workFlowRecord.setWorkFlowType(WorkFlowType.AGENT_UNIFIED_EXAM_APPLY);
            //校运直接指定
            MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord);
            Long workflowId = fetchWorkflowId(mapMessage);
            //指定工作流程Id
            if (workflowId != null) {
                unifiedExamApply.setWorkflowId(workflowId);
                unifiedExamApplyServiceClient.update(unifiedExamApply);
            }
        } else {
            unifiedExamApply = unifiedExamApplyService.update(unifiedExamApply);
            workFlowDataServiceClient.updateRecordStatus(unifiedExamApply.getWorkflowId(), "lv1", null);
        }

        return message;
    }

    // 更新统考的考试控制信息
    @RequestMapping(value = "view/update_exam_control_page.vpage")
    public String updateExamControlPage(Model model) {
        Long applyId = requestLong("applyId");
        if (applyId != null) {
            UnifiedExamApply examApply = unifiedExamApplyLoaderClient.load(applyId);
            if (examApply != null && examApply.getEntryStatus() == TestPaperEntryStatus.ONLINE) {
                model.addAttribute("apply", examApply);
            }
        }
        return "apply/unified_exam_edit";
    }

    // 更新统考的考试控制信息
    @RequestMapping(value = "view/update_exam_control.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateExamControlData(@RequestBody ExamControlView examControlView) {

        Long applyId = examControlView.getApplyId();
        if (applyId == null) {
            return MapMessage.errorMessage("ID为空");
        }

        UnifiedExamApply examApply = unifiedExamApplyLoaderClient.load(applyId);
        if (examApply == null) {
            return MapMessage.errorMessage("考试申请不存在");
        }
        if (examApply.getEntryStatus() != TestPaperEntryStatus.ONLINE) {
            return MapMessage.errorMessage("试卷录入完毕后，才能修改相关控制");
        }

        AuthCurrentUser currentUser = getCurrentUser();
        examControlView.setUserId(currentUser.getUserId());
        examControlView.setUserName(currentUser.getRealName());

        updateExamControl(examControlView);

        return MapMessage.successMessage();
    }

    private MapMessage updateExamControl(ExamControlView examControlView) {
        Map<Object, Object> parameters = ExamControlView.UpdateRequestParamBuilder.build(examControlView);
        String url = getTikuDomain() + "/service/updateExam";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).addParameter(parameters).execute();
        if (response.getStatusCode() == 200) {
            Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
            if (null != resultMap) {
                Object result = resultMap.get("success");
                Boolean bResult = (Boolean) result;
                if (!bResult) {
                    return MapMessage.errorMessage(SafeConverter.toString(resultMap.get("info")));
                } else {
                    return updateApplyExamControl(examControlView);
                }
            } else {
                return MapMessage.errorMessage("更新失败！");
            }
        } else {
            return MapMessage.errorMessage("更新失败！");
        }
    }

    private MapMessage updateApplyExamControl(ExamControlView examControlView) {
        UnifiedExamApply examApply = unifiedExamApplyLoaderClient.load(examControlView.getApplyId());
        if (examApply == null) {
            return MapMessage.errorMessage("考试申请不存在");
        }
        examApply.setUnifiedExamBeginTime(examControlView.getUnifiedExamBeginTime());
        examApply.setUnifiedExamEndTime(examControlView.getUnifiedExamEndTime());
        examApply.setCorrectingTestPaper(examControlView.getCorrectingTestPaper());
        examApply.setAchievementReleaseTime(examControlView.getAchievementReleaseTime());
        examApply.setMinSubmittedTestPaper(examControlView.getMinSubmittedTestPaper());
        examApply.setOralLanguageFrequency(examControlView.getOralLanguageFrequency());
        examApply.setMaxSubmittedTestPaper(examControlView.getMaxSubmittedTestPaper());
        examApply.setGradeType(examControlView.getGradeType());
        examApply.setRanks(examControlView.getRanks());
        unifiedExamApplyServiceClient.update(examApply);
        return MapMessage.successMessage();
    }

    private String getTikuDomain() {
        if (RuntimeMode.current().lt(Mode.STAGING)) {
            return "http://zytiku.test.17zuoye.net";
        } else {
            return "http://zytiku.17zuoye.com";
        }
    }

    private Map<String, Object> convertUnifiedExamApply(UnifiedExamApply apply) {
        if (apply == null) {
            return null;
        }
        Map<String, Object> retMap = BeanMapUtils.tansBean2Map(apply);
        String groupName = "";
        String parentGroupName = "";
        String cityGroupName = "";
        if (apply.getAccount() != null) {
            Map<String, String> result = getOrgInfoByUserId(SafeConverter.toLong(apply.getAccount()));
            groupName = result.get("groupName");
            parentGroupName = result.get("parentGroupName");
            cityGroupName = result.get("cityGroupName");
        }
        retMap.put("groupName", groupName);
        retMap.put("parentGroupName", parentGroupName);
        retMap.put("cityGroupName", cityGroupName);
        List<Integer> list = UnifiedExamApply.fetchGradeLevel(apply.getGradeLevel());
        retMap.put("gradeLevel", (list != null && list.size() > 0) ? list.get(0) : 0);
        return retMap;
    }

    private Map<String, Object> convertDataReportApply(DataReportApply apply) {
        if (apply == null) {
            return null;
        }
        Map<String, Object> retMap = BeanMapUtils.tansBean2Map(apply);
        String groupName = "";
        String parentGroupName = "";
        if (apply.getAccount() != null) {
            Map<String, String> result = getOrgInfoByUserId(SafeConverter.toLong(apply.getAccount()));
            groupName = result.get("groupName");
            parentGroupName = result.get("parentGroupName");
        }
        retMap.put("groupName", groupName);
        retMap.put("parentGroupName", parentGroupName);
        retMap.put("timeDimensionality", apply.timeDimensionality());
        ApplyStatus applyStatus = apply.getStatus();
        String applyStatusStr = "";
        if (applyStatus != null) {
            applyStatusStr = applyStatus.getDesc();
        }
        if (ApplyStatus.PENDING == applyStatus && apply.getWorkflowId() != null) {
            // fixme wyg 这里可能要改
            WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(apply.getWorkflowId());
            if (Objects.equals(workFlowRecord.getStatus(), "init")) {
                applyStatusStr = "市经理审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv1")) {
                applyStatusStr = "销运审中";
            }
        }
        retMap.put("applyStatus", applyStatusStr);
        return retMap;
    }

    /**
     * 上传试卷
     *
     * @return
     */
    @RequestMapping(value = "create/upload_testpaper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadUnifiedExamApplyTestPaper() {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile inputFile = multipartRequest.getFile("testPaper");
            if (inputFile.isEmpty()) {
                return MapMessage.errorMessage("文件为空");
            }
            if (inputFile.getSize() > 1024 * 1024 * 10) {
                return MapMessage.errorMessage("文件大小不能超过10M");
            }
            // 获取文件类型
            String originalFileName = inputFile.getOriginalFilename();
            String ext = StringUtils.substringAfterLast(originalFileName, ".");
            ext = StringUtils.defaultString(ext).trim().toLowerCase();
            if (!ObjectUtils.equals(ext, "docx") && !Objects.equals(ext, "doc")) {
                throw new RuntimeException("文件格式有误请重新上传");
            }
            SupportedFileType fileType;
            try {
                fileType = SupportedFileType.valueOf(ext);
            } catch (Exception ex) {
                throw new RuntimeException("不支持此格式文件");
            }
            String fileId = RandomUtils.nextObjectId();
            String fileName = "testPaper-" + fileId + "." + ext;
            String contentType = fileType.getContentType();

            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            @Cleanup InputStream inStream = inputFile.getInputStream();
            bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, inStream);
            String prePath = RuntimeMode.isUsingProductionData() ? "http://cdn-portrait.17zuoye.cn" : "http://cdn-portrait.test.17zuoye.net";
            return MapMessage.successMessage("上传成功").add("fileUrl", prePath + "/gridfs/" + fileName);
        } catch (Exception ex) {
            log.error("上传失败,msg:{}", ex.getMessage(), ex);
            //此处建议使用通用编码识别 防止不必要的异常暴露给用户
            return MapMessage.errorMessage(StringUtils.formatMessage("上传失败,msg:{}", ex.getMessage()));
        }

    }

    private MapMessage checkUnifiedExamApply(UnifiedExamApply unifiedExamApply) {
        if (StringUtils.isBlank(unifiedExamApply.getUnifiedExamName())) {
            return MapMessage.errorMessage("考试名称不能为空");
        }
        if (unifiedExamApply.getTestPaperSourceType() == UnifiedExamTestPaperSourceType.ANCIENT) {
            if (StringUtils.isNotBlank(unifiedExamApply.getTestPaperAddress())) { // 采用现有的试卷的情况下， 清空TestPaperAddress
                unifiedExamApply.setTestPaperAddress("");
            }
            if (CollectionUtils.isEmpty(unifiedExamApply.fetchPaperId())) {
                return MapMessage.errorMessage("试卷ID必须填");
            }
            if (unifiedExamApply.fetchPaperId().size() > 10) {
                return MapMessage.errorMessage("最多添加10套试卷ID");
            }
            if (StringUtils.isBlank(unifiedExamApply.getSubject())) {
                return MapMessage.errorMessage("统考学科不能为空");
            }
            Set<String> existsPaperIds = new HashSet<>();
            for (String testPaperId : unifiedExamApply.fetchPaperId()) {
                if (StringUtils.isBlank(unifiedExamApply.getTestPaperId()) || !StringUtils.startsWith(unifiedExamApply.getTestPaperId(), "P")) {
                    return MapMessage.errorMessage("试卷ID必须以'P'开头");
                }
                List<Object> npList = new ArrayList<>();
                if (SafeConverter.toInt(unifiedExamApply.getSubject()) < 201) { // 小学
                    npList.addAll(paperLoaderClient.loadPaperAsListByDocid(testPaperId));
                } else {
                    npList.addAll(midPaperLoaderClient.loadPaperAsListByDocid(testPaperId));
                }

                if (CollectionUtils.isEmpty(npList)) {
                    return MapMessage.errorMessage("以录入试卷ID不存在或者已失效");
                }
                NewPaper newPaper = new NewPaper();
                try {
                    BeanUtilsBean2.getInstance().copyProperties(newPaper, npList.get(0));
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    log.error("对象copy失败,msg:{}", ex.getMessage(), ex);
                }

                //构造2017年12月4日时间
                Calendar calendar = Calendar.getInstance();
                calendar.set(2017, 11, 4, 0, 0, 0);
                //2017-12-04之前创建的试卷，不允许再做关联
                if (null == newPaper.getCreatedAt() || newPaper.getCreatedAt().before(calendar.getTime())) {
                    return MapMessage.errorMessage("只能关联2017-12-04之后的试卷");
                }

                //录入试卷考试科目校验
                if (ObjectUtils.notEqual(newPaper.getSubjectId().toString(), unifiedExamApply.getSubject())) {
                    return MapMessage.errorMessage("以录入试卷ID:" + testPaperId + "学科与考试学科不对应");
                }
                if (existsPaperIds.contains(testPaperId)) {
                    return MapMessage.errorMessage("录入的试卷ID重复 ID:" + testPaperId);
                } else {
                    existsPaperIds.add(testPaperId);
                }
            }
        } else if (unifiedExamApply.getTestPaperSourceType() == UnifiedExamTestPaperSourceType.NEWLYADDED) {
            if (StringUtils.isNotBlank(unifiedExamApply.getTestPaperId())) { // 如果是新建试卷的情况下， 情况TestPaperId
                unifiedExamApply.setTestPaperId("");
            }
            if (StringUtils.isBlank(unifiedExamApply.getTestPaperAddress())) {
                return MapMessage.errorMessage("考试试卷不能为空");
            }
            if (unifiedExamApply.fetchPaperAddress().size() > 10) {
                return MapMessage.errorMessage("最多添加10套试卷");
            }
        } else {
            return MapMessage.errorMessage("是否重复使用已录入无效");
        }
        if ((sizeGtOne(unifiedExamApply.fetchPaperId()) || sizeGtOne(unifiedExamApply.fetchPaperAddress())) && unifiedExamApply.fetchDistribution() == null) {
            return MapMessage.errorMessage("请选择学生获取试卷方式");
        }
        if (unifiedExamApply.fetchTestScene() == null) {
            return MapMessage.errorMessage("请选择考试的场景");
        }

        if (CollectionUtils.isEmpty(unifiedExamApply.fetchTestPaperType())) {
            return MapMessage.errorMessage("统考类型必选");
        }
        if (CollectionUtils.isEmpty(unifiedExamApply.fetchGradeLevel())) {
            return MapMessage.errorMessage("年级有误");//需要特殊处理
        }
        if (Objects.equals(unifiedExamApply.getRegionLeve(), "country") && StringUtils.isBlank(unifiedExamApply.getRegionCode())) {
            return MapMessage.errorMessage("地区有误");//需要特殊处理
        } else if (Objects.equals(unifiedExamApply.getRegionLeve(), "school")) {
            if (StringUtils.isBlank(unifiedExamApply.getUnifiedExamSchool())) {
                return MapMessage.errorMessage("若是选择校级，则学校不能为空");//需要特殊处理
            }
            String[] unifiedExamSchoolIds = unifiedExamApply.getUnifiedExamSchool().split(",");
            if (unifiedExamSchoolIds.length > 20) {
                return MapMessage.errorMessage("选择校级，学校不能超过20个");
            }
        }
        //使用新添加数据 时间间隔为 6 天 使用以往数据 间隔为 2 天
        int numDate = 1;
        if (unifiedExamApply.getTestPaperSourceType() == UnifiedExamTestPaperSourceType.ANCIENT) {
            numDate = 1;
        } else {
            if (SafeConverter.toInt(unifiedExamApply.getSubject()) < 201) {  // 小学
                numDate = 9;
            } else {
                numDate = 6;
            }
        }
        if (unifiedExamApply.getUnifiedExamBeginTime() == null || unifiedExamApply.getUnifiedExamBeginTime().before(DateUtils.addDays(new Date(), numDate))) {
            return MapMessage.errorMessage("统考开始时间不能在早于" + DateUtils.dateToString(DateUtils.addDays(new Date(), numDate)));//需要特殊处理
        }
        if (unifiedExamApply.getUnifiedExamEndTime() == null || unifiedExamApply.getUnifiedExamBeginTime().after(unifiedExamApply.getUnifiedExamEndTime())) {
            return MapMessage.errorMessage("统考结束时间时间不能在早于开始时间");//需要特殊处理
        }
        if (unifiedExamApply.getCorrectingTestPaper() == null || unifiedExamApply.getUnifiedExamEndTime().after(unifiedExamApply.getCorrectingTestPaper())) {
            return MapMessage.errorMessage("老师判卷时间不能在早于统考结束时间时间");//需要特殊处理
        }
        if (unifiedExamApply.getAchievementReleaseTime() == null || unifiedExamApply.getCorrectingTestPaper().after(unifiedExamApply.getAchievementReleaseTime())) {
            return MapMessage.errorMessage("老师判卷时间不能在早于统考结束时间时间");//需要特殊处理
        }
        if (unifiedExamApply.getMinSubmittedTestPaper() == null || unifiedExamApply.getMinSubmittedTestPaper() <= 0) {
            return MapMessage.errorMessage("请填写正确的最早交卷时间");//需要特殊处理
        }
        if (unifiedExamApply.getMaxSubmittedTestPaper() == null || unifiedExamApply.getMaxSubmittedTestPaper() <= 0) {
            return MapMessage.errorMessage("请填写正确的答题时长");//需要特殊处理
        }
        if (unifiedExamApply.fetchTestGradeType() == null) {
            return MapMessage.errorMessage("请填写有效的成绩分制");
        }
        if (unifiedExamApply.getScore() == null || unifiedExamApply.getScore() <= 0) {
            return MapMessage.errorMessage("请填写有效的试卷分值");//需要特殊处理
        }
        if (unifiedExamApply.fetchTestGradeType() == UnifiedExamTestGradeType.GRADE && !checkExamRanks(unifiedExamApply.getRanks())) {
            return MapMessage.errorMessage("请填写有效的分级数据");//需要特殊处理
        }

        return MapMessage.successMessage();
    }

    private Boolean checkExamRanks(String ranks) {
        if (StringUtils.isBlank(ranks)) {
            return false;
        }
        List<ExamRank> examRanks = JsonUtils.fromJsonToList(ranks, ExamRank.class);
        if (CollectionUtils.isEmpty(examRanks)) {
            return false;
        }
        if (examRanks.stream().filter(p -> StringUtils.isBlank(p.getRankName()) || p.getBottom() == null || p.getTop() == null).count() > 0) {
            return false;
        }
        if (examRanks.stream().map(ExamRank::getRankName).collect(Collectors.toSet()).size() < examRanks.size()) {
            return false;
        }
        ExamRank first = examRanks.get(0);
        if (first.getTop() != 100) {
            return false;
        }
        ExamRank last = examRanks.get(examRanks.size() - 1);
        if (last.getBottom() != 0) {
            return false;
        }
        return examRanks.stream().map(p -> p.getTop() - p.getBottom()).reduce(0, (x, y) -> x + y) == 100;
    }

    private Boolean sizeGtOne(List<String> list) {
        return CollectionUtils.isNotEmpty(list) && list.size() > 1;
    }

    /**
     * 根据学校id查询学校
     * 包含所有学校
     *
     * @return 权限校验 需要注意下
     */
    @RequestMapping(value = "create/searchSchoolsByIdAndCityCode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchSchoolsInAll() {

        String schoolIds = getRequestString("schoolIds");   //学校ids
        Integer cityCode = getRequestInt("cityCode");   //城市编码
        if (StringUtils.isBlank(schoolIds)) {
            return MapMessage.errorMessage("请输入学校ID,已\",\"分割");
        }
        try {
            Set<Long> schoolSet = strToSchoolIdsSet(schoolIds);
            if (CollectionUtils.isEmpty(schoolSet)) {
                return MapMessage.errorMessage("学校ID数据有误,请输入正确的学校ID,并以\",\"分割");
            }
            return MapMessage.successMessage().add("searchResult", agentApplyManagementService.searchSchoolsIncludeAll(schoolSet, getCurrentUser(), cityCode));
        } catch (Exception ex) {
            logger.error(String.format("find school info failed schoolIdList=%s", schoolIds), ex);
            return MapMessage.errorMessage("查找学校信息失败");
        }
    }

    /**
     * 将前端
     *
     * @param schoolIds
     * @return
     */
    private Set<Long> strToSchoolIdsSet(String schoolIds) {
        if (StringUtils.isBlank(schoolIds)) {
            return Collections.emptySet();
        }
        Set<Long> res = new HashSet<>();
        String[] schoolIdStr = schoolIds.split(",");
        Set<String> schoolSet = new HashSet<>(Arrays.asList(schoolIdStr));
        if (CollectionUtils.isEmpty(schoolSet)) {
            return Collections.emptySet();
        }
        schoolSet.forEach(p -> res.add(ConversionUtils.toLong(p)));
        return res;
    }

    /**
     * 通过考试科目获取 教材信息
     *
     * @return
     */
    @RequestMapping(value = "create/searchBookProfileBySubject.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage obtainBookProfile() {
        int subjectInt = requestInteger("subject");
        if (subjectInt == 0) {
            return MapMessage.errorMessage("数据有误.");
        }
        Subject subject = Subject.fromSubjectId(subjectInt);
        MapMessage mapMessage = MapMessage.successMessage();
        if (subject != null) {
            List<Object> nbpList = new ArrayList<>();
            if (subject.getId() < 201) { // 小学
                nbpList.addAll(newContentLoaderClient.loadBooks(subject).stream().filter(t -> !t.isDeletedTrue()).collect(Collectors.toList()));
            } else {
                nbpList.addAll(midContentLoaderClient.loadBooks(subject).stream().filter(t -> !t.isDeletedTrue()).collect(Collectors.toList()));
            }
            mapMessage.put("bookProfiles", nbpList);
        }
        return mapMessage;
    }

    /**
     * 统考根据 组信息获取区域信息
     * 多个卫星城市的出现
     *
     * @return
     */
    @RequestMapping(value = "create/searchUnifiedExamApplyRegionList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unifiedExamApplyGetRegionList() {
        Long groupId = requestLong("groupId");
        Integer cityCode = requestInteger("cityCode");
        MapMessage mapMessage = MapMessage.successMessage().add("regionList", getRegionInfoByGroupIdAndCityCode(groupId, cityCode));
        return mapMessage;
    }

    private List<ExRegion> getRegionInfoByGroupIdAndCityCode(Long groupId, Integer cityCode) {
        if (groupId == null || groupId == 0 || cityCode == null || cityCode == 0) {
            return Collections.emptyList();
        }

        List<Integer> countyCodeList = baseOrgService.getGroupRegionCountyCodeList(groupId);
        Map<Integer, ExRegion> erMap = raikouSystem.getRegionBuffer().loadRegions(countyCodeList);
        return erMap.values().stream().filter(p -> Objects.equals(p.getCityCode(), cityCode)).collect(Collectors.toList());
    }
}
