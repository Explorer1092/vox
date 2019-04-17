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

package com.voxlearning.utopia.agent.controller.user;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.AgentGroupLevelInfo;
import com.voxlearning.utopia.agent.bean.AgentGroupRegionInfo;
import com.voxlearning.utopia.agent.bean.AgentGroupRoleInfo;
import com.voxlearning.utopia.agent.bean.SchoolShortInfo;
import com.voxlearning.utopia.agent.bean.group.GroupData;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombUserData;
import com.voxlearning.utopia.agent.constants.AgentDateConfigType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentDateConfig;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCost;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombUserService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.partner.AgentPartnerService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDateConfigService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentPerformanceGoalManager;
import com.voxlearning.utopia.agent.service.user.OrgConfigService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupRegionLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * OrgConfigController
 *
 * @author song.wang
 * @date 2016/6/20
 */
@Controller
@RequestMapping("/user/orgconfig")
@Slf4j
public class OrgConfigController extends AbstractAgentController {
    private static final String DEFAULTPASSWORD = "123456";
    private static final String USER_ACCOUNT_SCHOOL_DETAIL = "/config/templates/user_account_school_detail.xlsx";
    private static final FastDateFormat TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
    private static final int BYTES_BUFFER_SIZE = 1024 * 8;
    private static final String UPDATE_USER_BUD_TEMPLATE = "/config/templates/update_user_bud_template.xlsx";
    private final static String IMPORT_AGENT_PERFORMANCE_GOAL_TEMPLATE = "/config/templates/import_agent_performance_goal.xlsx";
    private final static String EXPORT_AGENT_PERFORMANCE_GOAL_TEMPLATE = "/config/templates/export_agent_performance_goal.xlsx";

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject BaseOrgService baseOrgService;
    @Inject OrgConfigService orgConfigService;
    @Inject private AgentDateConfigService agentDateConfigService;
    @Inject private AgentPerformanceGoalManager agentPerformanceGoalManager;
    @Inject private AgentMaterialBudgetService agentMaterialBudgetService;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    AgentGroupServiceClient agentGroupServiceClient;
    @Inject
    private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;
    @Inject
    private AgentGroupRegionLoaderClient agentGroupRegionLoaderClient;
    @Inject
    private HoneycombUserService honeycombUserService;
    @Inject
    private AgentPartnerService agentPartnerService;

    @RequestMapping(value = "update_usable_cash_amount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserUsableCashAmount() {
        Long userId = getRequestLong("userId");
        AgentUser user = baseOrgService.getUser(userId);
        if (user == null) {
            return MapMessage.errorMessage("需要调整余额的用户不存在");
        }
        Long pid = getRequestLong("pid");
        Integer usableCashAmountOpt = getRequestInt("usableCashAmountOpt");
        Double usableCashAmount = getRequestDouble("usableCashAmount");
        String usableCashAmountCause = getRequestString("usableCashAmountCause");
        if (usableCashAmount != 0.0 && usableCashAmountOpt != 0 && StringUtils.isBlank(usableCashAmountCause)) {
            return MapMessage.errorMessage("用户余额修改原因不能为空");
        }
        float preCash = SafeConverter.toFloat(user.getCashAmount());
        float changedData = MathUtils.floatMultiply(usableCashAmountOpt, usableCashAmount.floatValue());
        float afterCash = MathUtils.floatAdd(preCash, changedData);
        float preUsableCash = SafeConverter.toFloat(user.getUsableCashAmount());
        float afterUsableCash = MathUtils.floatAdd(preUsableCash, changedData);
        float materielBudge = SafeConverter.toFloat(user.getMaterielBudget());
        if (afterCash < 0) {
            return MapMessage.errorMessage("调整后余额小于0");
        }
        if (afterUsableCash < 0) {
            return MapMessage.errorMessage("调整后可用余额小于0");
        }
        if (afterCash > materielBudge) {
            return MapMessage.errorMessage("调整后余额不能大于预算");
        }
        if (afterUsableCash > materielBudge) {
            return MapMessage.errorMessage("调整后余额不能大于预算");
        }
        user.setCashAmount(afterCash >= 0 ? afterCash : 0);
        baseUserService.addAgentUserCashDataRecord(2, userId, getCurrentUserId(), preCash, afterCash, changedData, StringUtils.formatMessage("【调整余额】{}", usableCashAmountCause));
        user.setCashAmount(afterCash);
        user.setUsableCashAmount(afterUsableCash);
        baseUserService.updateAgentUser(user);
        return MapMessage.successMessage().add("userId", userId).add("parentId", pid);
    }

    @RequestMapping(value = "config_user_bud.vpage", method = RequestMethod.GET)
    public String updateUserBud() {
        return "user/configuserbud";
    }

    @RequestMapping(value = "update_user_bud.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUSerBud() {
        if (!getCurrentUser().isCountryManager()) {
            return MapMessage.errorMessage("只有是全国总监才能使用该功能");
        }
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        return orgConfigService.updateAgentUserBud(workbook);
    }

    private XSSFWorkbook readRequestWorkbook(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    @RequestMapping(value = "download_update_template.vpage", method = RequestMethod.GET)
    public void downloadUpdateTemplate() {
        try {
            Resource resource = new ClassPathResource(UPDATE_USER_BUD_TEMPLATE);
            if (!resource.exists()) {
                logger.error("download update user bud template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "更新用户预算模板.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download update user bud template - Excp : {};", e);
        }
    }

    private static void write(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    @RequestMapping(value = "downloadAgentUserInfo.vpage", method = RequestMethod.GET)
    public void downloadAgentUserInfo(HttpServletResponse response) {
        HSSFWorkbook hssfWorkbook = convertToWorkbook();
        String filename = "市场人员基本信息下载-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());

            response.getWriter().write("不能下载");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IOException ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }
    }

    private HSSFWorkbook convertToWorkbook() {
        AgentGroup group = baseOrgService.getGroupFirstOne(getCurrentUserId(), null);
        List<AgentGroup> allSubGroupList = new ArrayList<>();
        allSubGroupList.add(group);
        baseOrgService.getAllSubGroupList(allSubGroupList, group.getId());
        List<Long> groupIds = allSubGroupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
        Map<Long, AgentGroup> agentGroupMap = allSubGroupList.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        List<AgentGroupUser> agentGroupUsers = baseOrgService.getGroupUserByGroups(groupIds);
        Map<Long, List<AgentGroupUser>> agentGroupUsersMap = agentGroupUsers.stream().collect(Collectors.groupingBy(AgentGroupUser::getGroupId));
        Map<Long, AgentUser> agentUser = baseUserService.getUsers(agentGroupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet schoolSheet = workbook.createSheet();
        schoolSheet.setColumnWidth(0, 5000);
        schoolSheet.setColumnWidth(1, 5000);
        schoolSheet.setColumnWidth(2, 5000);
        schoolSheet.setColumnWidth(3, 5000);

        HSSFCellStyle borderStyle = workbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);
        Row firstRow = HssfUtils.createRow(schoolSheet, 0, 3, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "部门");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "角色");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "用户名");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "真实姓名");
        int rowNum = 1;
        for (Long groupId : groupIds) {
            List<AgentGroupUser> groupUsers = agentGroupUsersMap.get(groupId);
            if (CollectionUtils.isEmpty(groupUsers)) {
                continue;
            }
            for (AgentGroupUser user : groupUsers) {
                AgentUser userInfo = agentUser.get(user.getUserId());
                if (userInfo == null) {
                    continue;
                }
                Row row = HssfUtils.createRow(schoolSheet, rowNum++, 3, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, agentGroupMap.get(groupId).getGroupName());
                HssfUtils.setCellValue(row, 1, borderStyle, user.getUserRoleType() == null ? "" : user.getUserRoleType().getRoleName());
                HssfUtils.setCellValue(row, 2, borderStyle, userInfo.getAccountName());
                HssfUtils.setCellValue(row, 3, borderStyle, userInfo.getRealName());
            }
        }
        return workbook;
    }

    // 部门管理首页
    @RequestMapping(value = "department.vpage", method = RequestMethod.GET)
    @OperationCode("72fad515c1314fae")
    public String departmentIndex() {
        return "user/orgconfig/department";
    }

    //
    @RequestMapping(value = "cityBudgetInfo.vpage", method = RequestMethod.GET)
    public String cityBudgetInfo() {
        return "user/orgconfig/cityBudgetInfo";
    }

    // 部门列表
    @RequestMapping(value = "groupTree.vpage")
    @ResponseBody
    public String groupTree() {
        AuthCurrentUser user = getCurrentUser();
        //TODO 获取用户权限角色封装对象
        //TODO 获取数据 市场全部未关闭的帐号
        List<Map<String, Object>> maps = baseOrgService.loadUserGroupTreeIncludeUsers(user);
        return JsonUtils.toJson(maps);
    }

    // 部门的详情
    @RequestMapping(value = "departmentDetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage departmentDetail() {
        Long agentGroupId = getRequestLong("agentGroupId");
        AuthCurrentUser user = getCurrentUser();
        MapMessage msg = MapMessage.successMessage();
        try {
            //获取部门详情
            AgentGroup agentGroup = baseOrgService.getGroupById(agentGroupId);
            if (agentGroup == null) {
                return MapMessage.errorMessage("获取部门详情失败");
            }
            String groupName = agentGroup.getGroupName();
            Map<String, Object> groupHeadCountMap = getGroupHeadCount(agentGroup);
            //HC相关的设置
            msg.putAll(groupHeadCountMap);
            msg.put("groupName", groupName);
            msg.put("groupId", ConversionUtils.toString(agentGroupId));
            msg.put("groupRole", agentGroup.fetchGroupRoleType());
            msg.put("groupRoleId", agentGroup.getRoleId());
            Long parentId = agentGroup.getParentId();
            AgentGroup parentGroup = baseOrgService.getGroupById(parentId);
            String pGroupName = "";
            AgentGroupRoleType parentGroupRoleType = null;
            if (parentGroup != null) {
                pGroupName = parentGroup.getGroupName();
                parentGroupRoleType = parentGroup.fetchGroupRoleType();
            }
            msg.put("parentGroupName", pGroupName);
            msg.put("parentGroupId", parentId);
            String description = agentGroup.getDescription();
            msg.put("description", ConversionUtils.toString(description));
            List<AgentGroupRegion> agentGroupRegionList = baseOrgService.getGroupRegionByGroup(agentGroupId);
            msg.put("agentGroupRegionInfo", createAgentGroupRegionInfo(agentGroupRegionList));
            msg.put("isManager", user.isAdmin() || user.isCountryManager());
            msg.put("isCountryManager", user.isCountryManager());
            // id 为级别ID roleName 级别名称
            msg.put("canAddSubDepartment", canAddSubDepartment(ConversionUtils.toInt(agentGroup.getRoleId())));
            AgentGroupRoleType groupType = AgentGroupRoleType.of(agentGroup.getRoleId());
            msg.put("groupType", groupType == null ? "" : groupType.getRoleName());
            msg.put("logo", SafeConverter.toString(agentGroup.getLogo()));
            Map<String, Object> latest6MonthCityBudgetData = getLatest6MonthCityBudgetData(agentGroup);
            msg.put("latest6MonthCityBudgetData", latest6MonthCityBudgetData);

            List<AgentGroupRoleType> groupRoleList = AgentGroupRoleType.getManageableRoleList(parentGroupRoleType);
            msg.put("groupRoleList", getAgentGroupLevelInfo(groupRoleList));

            //设置业务类型
            List<AgentServiceType> groupServiceTypeList = agentGroup.fetchServiceTypeList();
            if (CollectionUtils.isNotEmpty(groupServiceTypeList)) {
                msg.put("serviceTypeStr", StringUtils.join(groupServiceTypeList.stream().map(AgentServiceType::getTypeName).collect(Collectors.toList()), "、"));
            }

            List<AgentServiceType> viableTypeList = new ArrayList<>();
            if (parentGroup != null) {
                viableTypeList = parentGroup.fetchServiceTypeList();
            } else {
                viableTypeList = Arrays.asList(AgentServiceType.values());
            }


            List<Map<String, Object>> serviceTypeList = new ArrayList<>();
            viableTypeList.forEach(p -> {
                Map<String, Object> serviceTypeMap = new HashMap<>();
                serviceTypeMap.put("st_key", p.name());
                serviceTypeMap.put("st_value", p.getTypeName());
                if (groupServiceTypeList.contains(p)) {
                    serviceTypeMap.put("st_show", true);
                } else {
                    serviceTypeMap.put("st_show", false);
                }
                serviceTypeList.add(serviceTypeMap);
            });
            msg.put("serviceTypeList", serviceTypeList);
        } catch (Exception ex) {
            logger.error(String.format("can`t find department detail agentGroupId= %d", agentGroupId), ex);
            return MapMessage.errorMessage("获取部门详情失败");
        }
        return msg;
    }

    private Map<String, Object> getGroupHeadCount(AgentGroup agentGroup) {
        Map<String, Object> resultMap = new HashMap<>();
        Integer headCount = 0;
        if (null != agentGroup) {
            if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
                if (null != agentGroup.getHeadCount()) {
                    headCount = agentGroup.getHeadCount();
                }
            } else {
                List<AgentGroup> agentSubGroupList = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
                for (int i = 0; i < agentSubGroupList.size(); i++) {
                    AgentGroup groupTemp = agentSubGroupList.get(i);
                    if (groupTemp.getHeadCount() != null) {
                        headCount += groupTemp.getHeadCount();
                    }
                }
            }
        }
        Integer actuallyCount = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(agentGroup.getId(), AgentRoleType.BusinessDeveloper.getId()).size();
        resultMap.put("headCount", headCount);
        resultMap.put("actuallyCount", actuallyCount);
        resultMap.put("actuallyRate", MathUtils.doubleDivide(actuallyCount * 100, headCount));
        return resultMap;
    }

    // 部门设置HC
    @RequestMapping(value = "set_hc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setDepartHC() {
        long groupId = getRequestLong("groupId");
        int headCount = getRequestInt("headCount");
        if (headCount <= 0) {
            return MapMessage.errorMessage("请输入有效的HC");
        }
        AuthCurrentUser user = getCurrentUser();
        if (!user.isCountryManager()) {
            return MapMessage.errorMessage("您无权限进行该操作");
        }
        MapMessage msg = MapMessage.successMessage();
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (group == null) {
            return MapMessage.errorMessage(groupId + "对应的部门未找到");
        }
        group.setHeadCount(headCount);
        baseOrgService.updateAgentGroup(group);
        return msg;
    }

    private Map<String, Object> getLatest6MonthCityBudgetData(AgentGroup agentGroup) {
        Map<String, Object> result = new HashMap<>();
        Set<Long> groupIds = new HashSet<>();
        AgentGroupRoleType groupType = AgentGroupRoleType.of(agentGroup.getRoleId());
        if (AgentGroupRoleType.City.equals(groupType)) {
            groupIds.add(agentGroup.getId());
        } else {
            Set<Long> groupIdSet = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> AgentGroupRoleType.City.getId().equals(item.getRoleId())).map(AgentGroup::getId).collect(Collectors.toSet());
            groupIds.addAll(groupIdSet);
        }
        List<AgentMaterialBudget> latest6MonthCityBudgetList = agentMaterialBudgetService.getLatest6MonthCityBudget(groupIds);
        double balance = 0d;
        for (int i = 0; i < latest6MonthCityBudgetList.size(); i++) {
            balance = MathUtils.doubleAdd(balance, latest6MonthCityBudgetList.get(i).getBalance());
        }
        result.put("balance", balance);
        result.put("dataList", latest6MonthCityBudgetList);
        return result;
    }

    /**
     * 可以添加子部门按钮
     *
     * @param groupRole
     * @return
     */
    private Boolean canAddSubDepartment(Integer groupRole) {
        AgentGroupRoleType groupRoleType = AgentGroupRoleType.of(groupRole);
        return groupRoleType != AgentGroupRoleType.City;
    }

    /**
     * 删除指定的部门
     *
     * @return 返回是否成功
     */
    @RequestMapping(value = "removeDepartment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeDepartment() {
        Long agentGroupId = getRequestLong("agentGroupId");
        AuthCurrentUser user = getCurrentUser();
        try {
            if (!user.isAdmin() && !user.isCountryManager()) {
                return MapMessage.errorMessage("您没有删除部门权限");
            }
            orgConfigService.deleteGroupData(agentGroupId);
        } catch (Exception ex) {
            logger.error(String.format("remove department is field agentGroupId=%d,userId=%d", agentGroupId, ConversionUtils.toLong(user.getUserId())), ex);
            return MapMessage.errorMessage("删除部门失败");
        }
        return MapMessage.successMessage();
    }

    /**
     * 添加用户页
     *
     * @return
     */
    @RequestMapping(value = "userRegistration.vpage")
    @ResponseBody
    public MapMessage userRegistration() {
        Long agentGroupId = getRequestLong("agentGroupId");
        AuthCurrentUser user = getCurrentUser();
        MapMessage map = new MapMessage();
        map.setSuccess(true);
        try {
            if (!user.isAdmin() && !user.isCountryManager()) {
                return MapMessage.errorMessage().add("errorMessage", "当前用户无权限操着");
            }
            AgentGroup group = baseOrgService.getGroupById(agentGroupId);
            if (group == null) {
                return MapMessage.errorMessage().add("errorMessage", "所选的部门不存");
            }
            //添加
            String groupName = group.getGroupName();
            map.put("groupName", ConversionUtils.toString(groupName));
            map.put("agentGroupId", agentGroupId);
            map.put("roleList", getRoleList(group.getRoleId()));
        } catch (Exception ex) {
            logger.error(String.format("user registration open failed agentGroupId=%d,userId=%d", agentGroupId, ConversionUtils.toLong(user.getUserId())), ex);
            return MapMessage.errorMessage().add("errorMessage", "所选的部门不存");
        }
        return map;
    }

    /**
     * 添加学校页
     * 会返回符合规定的学校
     *
     * @return
     */
    @RequestMapping(value = "searchSchoolsBySchoolIdList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchSchoolsBySchoolIdList() {
        String schoolIds = getRequestString("schoolIds");   //学校ids
        Long agentUserId = getRequestLong("agentUerId");    //被分配权限的人
        Long agentGroupId = getRequestLong("agentGroupId"); //需要分配权限的部门
        if (StringUtils.isBlank(schoolIds)) {
            return MapMessage.errorMessage("请输入学校ID,已\",\"分割");
        }
        try {
            Set<Long> schoolSet = strToSchoolIdsSet(schoolIds);
            if (CollectionUtils.isEmpty(schoolSet)) {
                return MapMessage.errorMessage("学校ID数据有误,请输入正确的学校ID,并以\",\"分割");
            }
            return MapMessage.successMessage().add("searchResult", orgConfigService.searchSchoolsBySchoolIdList(agentGroupId, agentUserId, schoolSet));
        } catch (Exception ex) {
            logger.error(String.format("find school info failed schoolIdList=%s", schoolIds), ex);
            return MapMessage.errorMessage("查找学校信息失败");
        }
    }

//    /**
//     * 初始化用户的学校列表
//     *
//     * @return
//     */
//    @RequestMapping(value = "getUserSchoolDataList.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getUserSchoolDataList() {
//        Long agentUserId = getRequestLong("agentUserId");
//        Long groupId = getRequestLong("groupId");
//        if (agentUserId == 0L || groupId == 0L) {
//            return MapMessage.errorMessage(String.format("用户信息和部门信息为找到 用户ID为%d,部门信息为%d", agentUserId, groupId));
//        }
//        MapMessage msg = MapMessage.successMessage();
//        try {
////            List<School> userSchoolList = orgConfigService.getUserSchoolDataList(groupId, agentUserId);
////            msg.put("agentGroupSchoolInfo", createAgentSchoolInfo(userSchoolList));
//        } catch (Exception ex) {
//            logger.error(String.format("get user school data list is failed agentUserId=%d,groupId=%d", agentUserId, groupId), ex);
//            return MapMessage.errorMessage("无法获取用户的学校列表");
//        }
//        return msg;
//    }

    /**
     * 保存用户学校信息（添加学校的保存）
     *
     * @return
     */
    @RequestMapping(value = "saveUserSchoolDataList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUserSchoolDataList() {
        String schoolIds = getRequestString("schoolIds");   //学校ids
        Long agentUserId = getRequestLong("agentUserId");
        Long groupId = getRequestLong("agentGroupId");
        AuthCurrentUser user = getCurrentUser();
        try {
            if (!user.isAdmin() && !(user.isCountryManager() || user.isCityManager()) && !user.isRegionManager() && !user.isAreaManager()) {
                return MapMessage.errorMessage().add("errorMessage", "当前用户无权限操着");
            }
            Set<Long> schoolIdsSet = strToSchoolIdsSet(schoolIds);
            if (CollectionUtils.isEmpty(schoolIdsSet)) {
                return MapMessage.errorMessage("学校ID数据有误,请输入正确的学校ID,并以\",\"分割");
            }
            if (user.isCityManager()) {
                AgentDateConfig agentDateConfig = agentDateConfigService.findDateConfigByType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);
                if (!agentDateConfig.checkCityManagerConfigSchool(new Date())) {
                    return MapMessage.errorMessage("学校负责人调整的开放是日期为每月的{}-{}号，其余时间若需调整，请联系销运数据团队，邮箱：xiaoyunshuju@17zuoye.com", agentDateConfig.getStartDay(), agentDateConfig.getEndDay());
                }
            }
            return orgConfigService.setSchoolsForUser(groupId, agentUserId, schoolIdsSet);
        } catch (Exception ex) {
            logger.error(String.format("save user school data list is failed schoolIds=%s,agentUserId=%d,groupId=%d", schoolIds, agentUserId, groupId), ex);
            return MapMessage.errorMessage("保存用户的学校信息失败");
        }
    }

    /**
     * 修改部门详情
     *
     * @return 返回修改是否成功
     */
    @RequestMapping(value = "modificationDepartmentInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modificationDepartmentInfo() {
        Long agentGroupId = getRequestLong("groupId");
        String agentGroupName = getRequestString("groupName");
        Integer roleId = requestInteger("roleId");
        String description = getRequestString("description");
        String logoUrl = getRequestString("logo");
        String serviceTypeStr = getRequestString("serviceTypeStr");

        List<AgentServiceType> selectedServiceTypeList = AgentServiceType.toTypeList(serviceTypeStr);

        try {
            AgentGroup group = baseOrgService.getGroupById(agentGroupId);
            if (group == null) return MapMessage.errorMessage(agentGroupId + "对应的部门未找到");
            AgentGroup groupByName = baseOrgService.getGroupByName(agentGroupName);
            if (groupByName != null && !Objects.equals(groupByName.getId(), agentGroupId)) {
                return MapMessage.errorMessage("名称为" + agentGroupName + "的部门已经存在");
            }

            AgentGroup parentGroup = baseOrgService.getGroupById(group.getParentId());
            if (parentGroup != null) {
                List<AgentServiceType> parentServiceTypeList = parentGroup.fetchServiceTypeList();
                if (selectedServiceTypeList.stream().anyMatch(p -> !parentServiceTypeList.contains(p))) {
                    return MapMessage.errorMessage("业务类型不在上级部门的范围内");
                }
            }

            group.setGroupName(agentGroupName);
            group.setDescription(description);
            group.setLogo(logoUrl);

            List<AgentServiceType> serviceTypeList = group.fetchServiceTypeList();
            boolean hasPermissionDeleted = serviceTypeList.stream().anyMatch(p -> !selectedServiceTypeList.contains(p));
            boolean hasPermissionChanged = false;
            if (selectedServiceTypeList.size() != serviceTypeList.size()) {
                hasPermissionChanged = true;
            } else {
                hasPermissionChanged = serviceTypeList.stream().anyMatch(p -> !selectedServiceTypeList.contains(p));
            }

            if (CollectionUtils.isNotEmpty(selectedServiceTypeList)) {
                List<String> serviceTypeNameList = selectedServiceTypeList.stream().map(AgentServiceType::name).collect(Collectors.toList());
                group.setServiceType(StringUtils.join(serviceTypeNameList, ","));
            } else {
                group.setServiceType("");
            }

            baseOrgService.updateAgentGroup(group);
            if (AgentGroupRoleType.of(roleId) != group.fetchGroupRoleType()) {
                agentGroupServiceClient.updateGroupRole(agentGroupId, AgentGroupRoleType.of(roleId));
            }

            //如果修改“部门级别”或者“业务类型”
            if (hasPermissionDeleted) {
                // 调整子部门的业务类型
                orgConfigService.adjustSubGroupServiceTypes(agentGroupId);
            }

            if (hasPermissionChanged) {
                //删除部门下所有用户对应的权限内的SchoolLevel缓存
                orgConfigService.deleteUserSchoolLevelCacheKey(agentGroupId, null);
            }


        } catch (Exception ex) {
            logger.error(String.format("modification departmentInfo field agentGroupId=%d,agentGroupName=%s,description=%s", agentGroupId, agentGroupName, description), ex);
            return MapMessage.errorMessage("修改部门详情失败");
        }
        return MapMessage.successMessage();
    }

    /**
     * 用户信息
     *
     * @return 返回用户的基本信息
     */
    @RequestMapping(value = "userAccountsDetail.vpage")
    @ResponseBody
    public MapMessage userDetail() {
        Long agentUserId = getRequestLong("agentUserId");
        Long groupId = getRequestLong("agentGroupId"); //所选用户的上级部门的ID
        if (agentUserId == 0L) {
            return MapMessage.errorMessage("用户信息不存在,请刷新页面");
        }
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            AgentUser agentUser = baseUserService.getById(agentUserId);
            if (agentUser == null) {
                return MapMessage.errorMessage("用户信息不存在,请刷新页面");
            }
            AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
            if (agentGroup == null) {
                return MapMessage.errorMessage(String.format("用户所在的部门已不存在 groupId=%d", groupId));
            }
            msg.put("realName", agentUser.getRealName());
            msg.put("accountName", agentUser.getAccountName());
            msg.put("groupName", agentGroup.getGroupName());
            Map<Long, Integer> groupUserRoleMapByUserId = baseOrgService.getGroupUserRoleMapByUserId(agentUserId);
            Integer userRoleId = groupUserRoleMapByUserId.get(groupId);
            AgentRoleType agentRoleType = AgentRoleType.of(userRoleId);
            msg.put("userRole", agentRoleType != null ? agentRoleType.getRoleName() : "");
            msg.put("tel", agentUser.getTel());
            msg.put("cashDeposit", agentUser.getCashDeposit());//保证金
            msg.put("bankName", agentUser.getBankName());       //开户行
            msg.put("bankHostName", agentUser.getBankHostName()); //开户人姓名
            msg.put("bankAccount", agentUser.getBankAccount()); //银行帐号
            msg.put("contractStartDate", agentUser.getContractStartDate() != null ? DateUtils.dateToString(agentUser.getContractStartDate(), DateUtils.FORMAT_SQL_DATE) : ""); //合同开始时间
            msg.put("contractEndDate", agentUser.getContractEndDate() != null ? DateUtils.dateToString(agentUser.getContractEndDate(), DateUtils.FORMAT_SQL_DATE) : "");      //合同结束时间
            msg.put("contractNumber", agentUser.getContractNumber());        //合同编号
            msg.put("userComment", agentUser.getUserComment());              //简介
            msg.put("address", agentUser.getAddress());
            msg.put("accountNumber", agentUser.getAccountNumber());//工号
            Boolean thisUserIsManager = isGroupManager(agentUserId, groupId); //判断这个用户在这个组下是不是管理员
            msg.put("thisUserIsManager", thisUserIsManager);
            Boolean theUserIsManageAble = isManageAbleUser(agentUserId, groupId, getCurrentUser().isCityManager(), userRoleId);
            msg.put("theUserIsManageAble", theUserIsManageAble);
            msg.put("canOperation", getCurrentUser().isAdmin() || getCurrentUser().isCountryManager() || getCurrentUser().isRegionManager() || getCurrentUser().isAreaManager() || getCurrentUser().isCityManager());
            msg.put("userId", agentUserId);
            msg.put("groupId", groupId);
            List<Long> honeycombUserIds = honeycombUserService.getHoneycombUserIds(agentUserId);
            if (CollectionUtils.isNotEmpty(honeycombUserIds)) {
                Long honeycombId = honeycombUserIds.get(0);
                msg.put("honeycombId", honeycombId);
                List<HoneycombUserData> honeycombUserList = honeycombUserService.getHoneycombUserData(Collections.singleton(honeycombId));
                if (CollectionUtils.isNotEmpty(honeycombUserList)) {
                    msg.put("honeycombMobile", honeycombUserList.get(0).getMobile());
                }
            }

            AgentMaterialCost userMaterialCost = agentMaterialBudgetService.getUserMaterialCostByUserId(agentUserId);
            if (null != userMaterialCost) {
//                msg.put("materielBudget", userMaterialCost.getBudget());
                msg.put("usableCashAmount", userMaterialCost.getBalance());
//                msg.put("budgetChangeRecords", agentMaterialBudgetService.getBudgetChangeRecords(userMaterialCost.getId()));
                msg.put("balanceChangeRecords", agentMaterialBudgetService.getBalanceChangeRecords(userMaterialCost.getId()));
            }
            msg.put("isCityManage", getCurrentUser().isCityManager());
            if (thisUserIsManager) {
                List<AgentGroupRegion> agentGroupRegionList = baseOrgService.getGroupRegionByGroup(groupId);
                msg.put("agentGroupRegionInfo", createAgentGroupRegionInfo(agentGroupRegionList));
            } else {
                List<School> userSchoolList = orgConfigService.getUserSchoolDataList(groupId, agentUserId);
                msg.put("agentGroupSchoolInfo", createAgentSchoolInfo(userSchoolList, theUserIsManageAble));
            }
        } catch (Exception ex) {
            logger.error(String.format("user detail is not found agentUserId=%d,groupId=%d", agentUserId, groupId), ex);
            return MapMessage.errorMessage("用户信息不存在,请刷新页面");
        }
        return msg;
    }

    /**
     * 添加用户到指定的部门
     *
     * @return 返回是否成功
     */
    @RequestMapping(value = "addUserAccounts.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addUser() {
        Long agentGroupId = getRequestLong("agentGroupId");     //部门ID
        if (agentGroupId == 0) {
            return MapMessage.errorMessage("请刷新页面,并重新选择部门");
        }
        String realName = getRequestString("realName");         //用户真实姓名
        if (StringUtils.isBlank(realName)) {
            return MapMessage.errorMessage("请输入真实姓名");
        }
        String accountName = getRequestString("accountName");   //用户登录名
        if (StringUtils.isBlank(accountName) || orgConfigService.judgeUserExistByAccount(accountName)) {
            return MapMessage.errorMessage("登录名为空或重复");
        }
        Integer roleType = getRequestInt("roleType");           //角色类型
        if (roleType == 0 || AgentRoleType.of(roleType) == null) {
            return MapMessage.errorMessage("请选择角色");
        }
        String tel = getRequestString("tel");                   //电话
        if (!MobileRule.isMobile(tel) || orgConfigService.judgeUserExistByMobile(tel)) {
            return MapMessage.errorMessage("不是正确的电话号码或电话号码已存在");
        }
        Integer cashDeposit = getRequestInt("cashDeposit");      //保证金（可为空）
        String bankName = getRequestString("bankName");          //开户行名称
        String bankHostName = getRequestString("bankHostName");  //开户人
        String bankAccount = getRequestString("bankAccount");    //银行帐号
        Date contractStartDate = getRequestDate("contractStartDate"); //合同开始时间
        if (contractStartDate == null) {
            return MapMessage.errorMessage("合同开始时间不能为空");
        }
        Date contractEndDate = getRequestDate("contractEndDate");  //合同结束时间
        String contractNumber = getRequestString("contractNumber"); //合同编号
        String address = getRequestString("address"); //地址
        String userComment = getRequestString("userComment"); //备注
        String avatar = getRequestString("avatar"); //头像
        Integer budgetOpt = getRequestInt("budgetOpt"); //预算 +1、-1
        Double materielBudget = getRequestDouble("materielBudget"); //预算增量
        AgentRoleType role = AgentRoleType.of(roleType);
        String adjust_cause = getRequestString("adjust_cause");
        String accountNumber = getRequestString("accountNumber");//工号

        boolean bindHoneycomb = getRequestBool("bindHoneycomb");
        // 添加角色前判断是否可以再次创建该类型的角色
        if (baseOrgService.getGroupManager(agentGroupId) != null && (role == AgentRoleType.BUManager || role == AgentRoleType.Region || role == AgentRoleType.AreaManager || role == AgentRoleType.CityManager)) {
            return MapMessage.errorMessage("该部门已经有" + role.getRoleName() + "，不可重复设置");
        }
        //判断该工号是否存在
        if (StringUtils.isNotBlank(accountNumber)) {
            Long accountNumberLong = SafeConverter.toLong(accountNumber);
            if (accountNumberLong > 0) {
                List<AgentUser> userList = baseOrgService.getUserByAccountNumber(String.format("%04d", accountNumberLong));
                if (CollectionUtils.isNotEmpty(userList)) {
                    return MapMessage.errorMessage("该工号已经存在");
                }
            } else {
                return MapMessage.errorMessage("工号不正确");
            }
        }
        //添加用户
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            MapMessage userMessage = orgConfigService.addAgentUser(realName, accountName, DEFAULTPASSWORD, tel, cashDeposit,
                    bankName, bankHostName, bankAccount, contractStartDate, contractEndDate, contractNumber, address,
                    userComment, avatar, budgetOpt * (float) materielBudget.doubleValue(), accountNumber);
            if (!userMessage.isSuccess()) {
                return userMessage;
            }
            msg = orgConfigService.addAgentUserRecord((AgentUser) userMessage.get("user"), getCurrentUserId(), budgetOpt, materielBudget, adjust_cause);
            if (!msg.isSuccess()) {
                return msg;
            }
            Long userId = (Long) userMessage.get("userId");
            MapMessage groupUserMessage = orgConfigService.addGroupUser(agentGroupId, userId, AgentRoleType.of(roleType));
            if (!groupUserMessage.isSuccess()) {
                return groupUserMessage;
            }
            msg.put("userId", userId);
            msg.put("parentId", agentGroupId);
            if (bindHoneycomb) {
                orgConfigService.bindHoneycombAccount(userId, tel);
            }

        } catch (Exception ex) {
            logger.error(String.format("add user accounts agentGroupId=%d", agentGroupId), ex);
            return MapMessage.errorMessage("添加用户失败");
        }
        return msg;
    }

    /**
     * 添加子部门信息页
     *
     * @return 部门的名称和id 可选部门的级别
     */
    @RequestMapping(value = "entryDepartmentInfo.vpage")
    @ResponseBody
    public MapMessage entryDepartmentInfo() {
        AuthCurrentUser user = getCurrentUser();
        Long pGroupId = getRequestLong("agentGroupId");     //父部门ID
        AgentGroup pGroup = baseOrgService.getGroupById(pGroupId);
        if (null == pGroup) {
            return MapMessage.errorMessage("该部门已不存在,请重新刷新页面");
        }
        //对该部门无权限
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            if (!user.isAdmin() && !user.isCountryManager()) {
                return MapMessage.errorMessage("您对该部门无操作权限");
            }
            msg.put("pGroupId", ConversionUtils.toLong(pGroup.getId()));
            msg.put("pGroupRole", pGroup.fetchGroupRoleType());
            msg.put("pGroupName", ConversionUtils.toString(pGroup.getGroupName()));
            msg.put("dpLevels", getAgentGroupLevelInfo(AgentGroupRoleType.getManageableRoleList(AgentGroupRoleType.of(ConversionUtils.toInt(pGroup.getRoleId())))));


            List<AgentServiceType> viableTypeList = pGroup.fetchServiceTypeList();
            //业务类型列表
            List<Map<String, Object>> serviceTypeList = new ArrayList<>();
            viableTypeList.forEach(p -> {
                Map<String, Object> serviceTypeMap = new HashMap<>();
                serviceTypeMap.put("st_key", p.name());
                serviceTypeMap.put("st_value", p.getTypeName());
                serviceTypeList.add(serviceTypeMap);
            });
            msg.put("serviceTypeList", serviceTypeList);

        } catch (Exception ex) {
            logger.error(String.format("entry department info failed pGroupId=%d,userId=%d", pGroupId, ConversionUtils.toLong(user.getUserId())), ex);
            return MapMessage.errorMessage("打开添加子部门功能失败");
        }
        return msg;
    }

    /**
     * 获取负责的部门 体哪家负责区域左侧的树形结构
     *
     * @return
     */
    @RequestMapping(value = "getDepartmentRange.vpage")
    @ResponseBody
    public String getDepartmentRange() {
        Long pGroupId = getRequestLong("agentGroupId");     //父部门ID
        List<Map<String, Object>> maps = orgConfigService.loadMarkedGroupRegionTreeByGroupId(pGroupId);
        return JsonUtils.toJson(maps);
    }

    /**
     * 获取本指定部门负责的区域
     *
     * @return
     */
    @RequestMapping(value = "getGroupRegionTree.vpage")
    @ResponseBody
    public String getGroupRegionTree() {
        Long groupId = getRequestLong("groupId");     //部门ID
        List<Map<String, Object>> regionTree = baseOrgService.loadGroupRegionTreeByGroupId(groupId);
        return JsonUtils.toJson(regionTree);
    }

//    /**
//     * 获取当前部门有权限的部门的信息
//     *
//     * @return
//     */
//    @RequestMapping(value = "getDepartmentRegionInfo.vpage")
//    @ResponseBody
//    public MapMessage getDepartmentRegionInfo() {
//        Long groupId = getRequestLong("agentGroupId");
//        MapMessage msg = new MapMessage();
//        msg.setSuccess(true);
//        try {
//            List<Map<String, Object>> groupRegionData = orgConfigService.getGroupRegionData(groupId);
//            if (groupRegionData == null) {
//                return MapMessage.errorMessage("获取部门的区域信息失败");
//            }
//            msg.put("groupRegionData", groupRegionData);
//        } catch (Exception ex) {
//            logger.error(String.format("get department region info failed groupId=%d", groupId), ex);
//            return MapMessage.errorMessage("获取部门的区域信息失败");
//        }
//        return msg;
//    }

//    /**
//     * 获取当前部门的权限信息 点击树形结构右侧弹出的信息
//     *
//     * @return
//     */
//    @RequestMapping(value = "getDepartmentPower.vpage")
//    @ResponseBody
//    public MapMessage getDepartmentPower() {
//        Long pGroupId = getRequestLong("agentGroupId");     //部门ID
//        Integer regionCode = getRequestInt("regionCode");
//
//        AgentGroup pGroup = baseOrgService.getGroupById(pGroupId);
//        if (null == pGroup) {
//            return MapMessage.errorMessage("该部门已不存在,请重新刷新页面");
//        }
//        if (regionCode == 0) {
//            return MapMessage.errorMessage("地区选择失败，请重新选择");
//        }
////        Integer groupRoleId = pGroup.getRoleId();
//        MapMessage msg = new MapMessage();
//        msg.setSuccess(true);
//        try {
////            if (!checkRegionCodeLevel(AgentGroupRoleType.of(groupRoleId), regionCode)) {
////                return MapMessage.successMessage();
////            }
//            Map<String, Object> regionDataForGroup = orgConfigService.getRegionDataForGroup(pGroupId, regionCode);
//            if (regionDataForGroup == null) {
//                return MapMessage.errorMessage("无法选择该地区");
//            }
//            msg.put("regionDataForGroup", regionDataForGroup);
//        } catch (Exception ex) {
//            logger.error(String.format("get department region data failed pGroupId=%d,regionCode=%d", pGroupId, regionCode), ex);
//            return MapMessage.errorMessage("无法选择该地区或地区信息不存在");
//        }
//        return msg;
//    }

    /**
     * 保存当前部门的区域信息
     *
     * @return
     */
    @RequestMapping(value = "saveDepartmentRegion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveDepartmentRegion(@RequestBody String departmentRegion) {
        try {
            String departmentRegionStr = URLDecoder.decode(departmentRegion, "UTF-8");
            Map<String, Object> responsibleRegionsMap = JsonUtils.fromJson(departmentRegionStr);
            Long groupId = ConversionUtils.toLong(responsibleRegionsMap.get("agentGroupId"));     //部门的ID
            AgentGroup group = baseOrgService.getGroupById(groupId);
            if (null == group) {
                return MapMessage.errorMessage("该部门已不存在,请重新刷新页面");
            }
//            AgentGroupRoleType groupRoleType = AgentGroupRoleType.of(group.getRoleId());
//            if (groupRoleType == null) {
//                return MapMessage.errorMessage("该部门无部门级别,请联系管理员");
//            }
            List<Map<String, String>> responsibleRegion = (List<Map<String, String>>) responsibleRegionsMap.get("responsibleRegion");
            if (responsibleRegion == null) {
                return MapMessage.errorMessage("所选择的区域权限信息错误");
            }

            Set<Integer> regionSet = new HashSet<>();
            responsibleRegion.forEach(p -> {
//                boolean selected = ConversionUtils.toBool(p.get("selected"));
//                if (!selected) {
//                    return;
//                }
                Integer regionCode = ConversionUtils.toInt(p.get("regionCode"));
//                if (checkRegionCodeLevel(groupRoleType, regionCode)) {
                regionSet.add(regionCode);
//                }
            });
            // 保存部门对应的地区信息
            return orgConfigService.updateGroupRegion(groupId, orgConfigService.getTopRegions(regionSet));

        } catch (Exception ex) {
            logger.error(String.format("save department region failed departmentRegion=%s", departmentRegion), ex);
            return MapMessage.errorMessage("保存部门区域失败");
        }
    }

    /**
     * 检查page
     *
     * @return
     */
    @RequestMapping(value = "checkDataPage.vpage", method = RequestMethod.GET)
    public String checkInData() {
        return "/user/orgconfig/checkData";
    }

    /**
     * 检查功能
     *
     * @return
     */
    @RequestMapping(value = "checkGroupData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkGroupData() {
        MapMessage mapMessage = orgConfigService.checkGroupData();
        AuthCurrentUser currentUser = getCurrentUser();
        if (mapMessage.isSuccess() && currentUser.isCountryManager()) {
            mapMessage.add("checkAgentPerformanceGoalResult", agentPerformanceGoalManager.checkAgentPerformanceGoal());
        }
        return mapMessage;
    }

    /**
     * 判断该部门级别下此地区是否符合约定
     *
     * @param groupRoleType 部门级别
     * @param regionCode    地区编码
     * @return
     */
    public boolean checkRegionCodeLevel(AgentGroupRoleType groupRoleType, Integer regionCode) {
        if (groupRoleType == null) {
            return false;
        }
        ExRegion region = raikouSystem.loadRegion(regionCode);
        return region != null && (((AgentGroupRoleType.Country == groupRoleType || AgentGroupRoleType.BusinessUnit == groupRoleType || AgentGroupRoleType.Region == groupRoleType || AgentGroupRoleType.Area == groupRoleType) && (region.fetchRegionType() == RegionType.CITY)) || (AgentGroupRoleType.City == groupRoleType && region.fetchRegionType() == RegionType.COUNTY));
    }

    /**
     * 添加子部门 创建子部门按钮
     *
     * @return
     */
    @RequestMapping(value = "addSubDepartment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSubDepartment() {
        Long pGroupId = getRequestLong("agentGroupId");     //父部门ID
        String groupName = getRequestString("groupName");   //该部门名称
        Integer dpLevel = getRequestInt("dpLevel");         //部门级别
        String logo = getRequestString("logo");              //大区徽标
        String serviceTypeStr = getRequestString("serviceTypeStr");//业务类型


        List<AgentServiceType> selectedServiceTypeList = AgentServiceType.toTypeList(serviceTypeStr);

        AgentGroup pGroup = baseOrgService.getGroupById(pGroupId);
        if (null == pGroup) {
            return MapMessage.errorMessage("该部门已不存在,请重新刷新页面");
        } else {
            List<AgentServiceType> parentServiceTypeList = pGroup.fetchServiceTypeList();
            if (selectedServiceTypeList.stream().anyMatch(p -> !parentServiceTypeList.contains(p))) {
                return MapMessage.errorMessage("业务类型不在上级部门的范围内");
            }
        }
        if (StringUtils.isBlank(groupName)) {
            return MapMessage.errorMessage("子部门名称不能为空");
        }

        String description = getRequestString("description");
        try {
            return orgConfigService.addAgentGroup(groupName, description, pGroupId, AgentGroupRoleType.of(dpLevel), logo, selectedServiceTypeList);
        } catch (Exception ex) {
            logger.error("add sub department failed, pGroupId:{},groupName:{},dpLevel:{}", pGroupId, groupName, dpLevel, ex);
            return MapMessage.errorMessage("添加子部门失败");
        }
    }

    /**
     * 用户详情
     *
     * @return 返回修改是否成功
     */
    @RequestMapping(value = "userAccountInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userAccountInfo() {
        Long userId = getRequestLong("userId");     //用户ID
        AgentUser agentUser = baseUserService.getById(userId);
        if (agentUser == null) {
            return MapMessage.errorMessage("该账户信息已不存在");
        }
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            msg.put("realName", ConversionUtils.toString(agentUser.getRealName()));
            msg.put("tel", ConversionUtils.toString(agentUser.getTel()));
            msg.put("address", ConversionUtils.toString(agentUser.getAddress()));
            msg.put("cashDeposit", ConversionUtils.toInt(agentUser.getCashDeposit()));
            msg.put("bankName", ConversionUtils.toString(agentUser.getBankName()));
            msg.put("bankHostName", ConversionUtils.toString(agentUser.getBankHostName()));
            msg.put("bankAccount", ConversionUtils.toString(agentUser.getBankAccount()));
            msg.put("contractStartDate", agentUser.getContractStartDate() != null ? DateUtils.dateToString(agentUser.getContractStartDate(), DateUtils.FORMAT_SQL_DATE) : "");
            msg.put("contractEndDate", agentUser.getContractEndDate() != null ? DateUtils.dateToString(agentUser.getContractEndDate(), DateUtils.FORMAT_SQL_DATE) : "");
            msg.put("contractNumber", ConversionUtils.toString(agentUser.getContractNumber()));
            msg.put("userComment", ConversionUtils.toString(agentUser.getUserComment()));
            msg.put("avatar", ConversionUtils.toString(agentUser.getAvatar()));
            msg.put("materielBudget", agentUser.getMaterielBudget());
            msg.put("usableCashAmount", agentUser.getUsableCashAmount());
            msg.put("accountNumber", agentUser.getAccountNumber());
            List<Long> honeycombUserIds = honeycombUserService.getHoneycombUserIds(agentUser.getId());
            if (CollectionUtils.isNotEmpty(honeycombUserIds)) {
                Long honeycombId = honeycombUserIds.get(0);
                msg.put("honeycombId", honeycombId);
                List<HoneycombUserData> honeycombUserList = honeycombUserService.getHoneycombUserData(Collections.singleton(honeycombId));
                if (CollectionUtils.isNotEmpty(honeycombUserList)) {
                    msg.put("honeycombMobile", honeycombUserList.get(0).getMobile());
                }
            }
        } catch (Exception ex) {
            logger.error(String.format("get user account info failed agentUserId=%d", userId), ex);
            return MapMessage.errorMessage("获取用户信息失败");
        }
        return msg;
    }

    /**
     * 保存用户详情
     *
     * @return
     */
    @RequestMapping(value = "modificationUserAccountInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modificationUserInfo() {
        Long userId = getRequestLong("userId");     //当前用户信息
        AgentUser agentUser = baseUserService.getById(userId);
        if (agentUser == null) {
            return MapMessage.errorMessage("该账户信息已不存在");
        }
        MapMessage msg = MapMessage.successMessage();
        try {
            String oldTel = agentUser.getTel();
            String realName = getRequestString("realName");             //真是姓名
            String tel = getRequestString("tel");                       //电话号码
            String address = getRequestString("address");               //地址信息
            Integer cashDeposit = getRequestInt("cashDeposit");         //保证金
            String bankName = getRequestString("bankName");             //开户行
            String bankHostName = getRequestString("bankHostName");     //开户人
            String bankAccount = getRequestString("bankAccount");       //银行帐号
            Date contractStartDate = getRequestDate("contractStartDate");   //合同开始时间
            Date contractEndDate = getRequestDate("contractEndDate");       //合同结束时间
            String contractNumber = getRequestString("contractNumber");     //合同编号
            String userComment = getRequestString("userComment");           //简介
            String avatar = getRequestString("avatar");           //简介
            Integer budgetOpt = getRequestInt("budgetOpt");
            Double materielBudget = getRequestDouble("materielBudget");
            String adjust_cause = getRequestString("adjust_cause");
            String accountNumber = getRequestString("accountNumber");//工号
            if (!MobileRule.isMobile(tel)) {
                return MapMessage.errorMessage("电话号码格式错误");
            }
            if (!Objects.equals(oldTel, tel) && orgConfigService.judgeUserExistByMobile(tel)) {
                return MapMessage.errorMessage("电话号码重复");
            }
            if (StringUtils.isBlank(realName)) {
                return MapMessage.errorMessage("请输入真实姓名");
            }
            if (contractStartDate == null) {
                return MapMessage.errorMessage("合同开始时间不能为空");
            }
            agentUser.setRealName(realName);
            agentUser.setTel(tel);
            agentUser.setAddress(address);
            agentUser.setCashDeposit(cashDeposit);
            agentUser.setBankName(bankName);
            agentUser.setBankHostName(bankHostName);
            agentUser.setBankAccount(bankAccount);
            agentUser.setContractStartDate(contractStartDate);
            agentUser.setContractEndDate(contractEndDate);
            agentUser.setContractNumber(contractNumber);
            agentUser.setUserComment(userComment);
            if (StringUtils.isNotBlank(avatar)) {
                agentUser.setAvatar(avatar);
            }
            if (StringUtils.isNotBlank(accountNumber)) {
                Long accountNumberLong = SafeConverter.toLong(accountNumber);
                if (accountNumberLong > 0) {
                    List<AgentUser> userList = baseOrgService.getUserByAccountNumber(String.format("%04d", accountNumberLong));
                    if (CollectionUtils.isNotEmpty(userList)) {
                        for (int i = 0; i < userList.size(); i++) {
                            if (!userList.get(i).getId().equals(agentUser.getId())) {
                                return MapMessage.errorMessage("已存在相同工号的用户！");
                            }
                        }
                    }
                    agentUser.setAccountNumber(String.format("%04d", SafeConverter.toLong(accountNumber)));
                } else {
                    return MapMessage.errorMessage("工号不正确");
                }
            } else {
                agentUser.setAccountNumber("");
            }
            List<AgentUser> agentUserList = baseOrgService.getUserByRealName(realName);
            for (int i = 0; i < agentUserList.size(); i++) {
                if (!agentUserList.get(i).getId().equals(agentUser.getId())) {
                    return MapMessage.errorMessage("已存在相同姓名的用户！");
                }
            }
            msg = orgConfigService.addAgentUserRecord(agentUser, getCurrentUserId(), budgetOpt, materielBudget, adjust_cause);
            if (!msg.isSuccess()) {
                return msg;
            }
            /*if (budgetOpt != 0 && materielBudget != 0) {
                if (StringUtils.isBlank(adjust_cause)) {
                    return MapMessage.errorMessage("物料预算有调整，需填写调整原因。");
                }
                float changedData = MathUtils.floatMultiply(budgetOpt, materielBudget.floatValue());

                float preBudget = agentUser.getMaterielBudget() == null ? 0 : agentUser.getMaterielBudget();
                float f1 = MathUtils.floatAdd(preBudget, changedData);
                agentUser.setMaterielBudget(f1 >= 0 ? f1 : 0);

                float preCashAmount = agentUser.getCashAmount() == null ? 0 : agentUser.getCashAmount();
                float f2 = MathUtils.floatAdd(preCashAmount, changedData);
                agentUser.setCashAmount(f2 >= 0 ? f2 : 0);

                float preUsableCashAmount = agentUser.getUsableCashAmount() == null ? 0 : agentUser.getUsableCashAmount();
                float f3 = MathUtils.floatAdd(preUsableCashAmount, changedData);
                agentUser.setUsableCashAmount(f3 >= 0 ? f3 : 0);

                // 添加用户物料预算变化记录
                baseUserService.addAgentUserCashDataRecord(1, agentUser.getId(), getCurrentUserId(), preBudget, agentUser.getMaterielBudget(), changedData, StringUtils.formatMessage("【物料预算调整】原因:{}", adjust_cause));
                // 添加用户余额变动记录
                baseUserService.addAgentUserCashDataRecord(2, agentUser.getId(), getCurrentUserId(), preCashAmount, agentUser.getCashAmount(), changedData, StringUtils.formatMessage("【物料预算调整】原因:{}", adjust_cause));
            }*/
            baseUserService.updateAgentUser(agentUser);
        } catch (Exception ex) {
            logger.error(String.format("save user account info failed agentserId=%d", userId), ex);
            return MapMessage.errorMessage("保存用户信息失败");
        }
        return msg;
    }

    /**
     * 移除账户的多个学校的权限
     *
     * @return
     */
    @RequestMapping(value = "removeAgentAccountSchool.vpage")
    @ResponseBody
    public MapMessage removeAgentAccountSchool() {
        Long userId = getRequestLong("agentUserId");
        String schoolIds = getRequestString("schoolIds");
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            Set<Long> removeSchoolIds = strToSchoolIdsSet(schoolIds);
            if (CollectionUtils.isEmpty(removeSchoolIds)) {
                return MapMessage.errorMessage("学校ID数据有误,请输入正确的学校ID,并以\",\"分割");
            }
            if (getCurrentUser().isCityManager()) {
                AgentDateConfig agentDateConfig = agentDateConfigService.findDateConfigByType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);
                if (agentDateConfig != null && !agentDateConfig.checkCityManagerConfigSchool(new Date())) {
                    return MapMessage.errorMessage("学校负责人调整的开放是日期为每月的{}-{}号，其余时间若需调整，请联系销运数据团队，邮箱：xiaoyunshuju@17zuoye.com", agentDateConfig.getStartDay(), agentDateConfig.getEndDay());
                }
            }
            orgConfigService.deleteUserSchoolList(userId, removeSchoolIds);
        } catch (Exception ex) {
            logger.error("remove  agent account school is failed userId=" + userId + ",schoolIds=" + schoolIds, ex);
            return MapMessage.errorMessage("移除该用户学校权限失败");
        }
        return msg;
    }

    /**
     * 检查电话号码是否重复
     *
     * @return success为不重复
     */
    @RequestMapping(value = "mobileRechecking.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage telephoneRechecking() {
        String tel = getRequestString("tel");
        if (!MobileRule.isMobile(tel)) {
            return MapMessage.errorMessage("电话号码不合法");
        }
        if (orgConfigService.judgeUserExistByMobile(tel)) {
            return MapMessage.errorMessage("电话号码重复");
        }
        return MapMessage.successMessage();
    }


    /**
     * 检查帐号是否重复
     *
     * @return success为不重复
     */
    @RequestMapping(value = "accountNameRechecking.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage accountNameRechecking() {
        String accountName = getRequestString("accountName");
        if (orgConfigService.judgeUserExistByAccount(accountName)) {
            return MapMessage.errorMessage("用户名重复");
        }
        return MapMessage.successMessage();
    }

    /**
     * 下载负责学校
     *
     * @param response
     */
    @RequestMapping(value = "exportResponsibleSchoolExcel.vpage", method = RequestMethod.GET)
    void exportResponsibleSchoolExcel(HttpServletResponse response) {
        Long agentUserId = getRequestLong("agentUserId");
        Long groupId = getRequestLong("agentGroupId"); //所选用户的上级部门的ID
        if (agentUserId == 0L || groupId == 0L) {
            return;
        }
        try {
            List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(agentUserId);
            Map<Long, School> managedSchoolMap = raikouSystem.loadSchools(managedSchoolList);
            if (managedSchoolMap == null) {
                return;
            }
            List<School> userSchoolList = new ArrayList<>(managedSchoolMap.values());
            AgentUser agentUser = baseUserService.getById(agentUserId);
            String filename = agentUser.getRealName() + "学校信息下载-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            AgentGroup group = baseOrgService.getGroupById(groupId);
            XSSFWorkbook workbook = convertToResponsibleSchool(group, agentUser, userSchoolList);
            if (workbook == null) {
                response.getWriter().write("下载失败");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ex) {
            try {
                response.getWriter().write("下载失败");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error(String.format("download responsible school exception! agentUserId=%d,groupId=%d", agentUserId, groupId), e);
            }
        }
    }


    /**
     * 关闭当前部门下的帐号（其他部门也会失效）
     *
     * @return
     */
    @RequestMapping(value = "closeUserAccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage closeUserAccount() {
        Long agentUserId = getRequestLong("agentUserId");     //需要被关闭的帐号ID
        Long groupId = getRequestLong("agentGroupId");      //所在的部门的ID
        if (agentUserId == 0L) {
            return MapMessage.errorMessage("用户帐号错误,请刷新页面后重试");
        }
        AuthCurrentUser user = getCurrentUser(); //当前用户
        if (!user.isAdmin() && !user.isCountryManager()) {
            return MapMessage.errorMessage("当前帐号无权进行次操作,请刷新页面后重试");
        }
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            //部门中的人员账号变更，不在此部门时，将该人员名下剩余的物料费用划归到部门的“未分配”费用中
            AgentUser agentUser = baseOrgService.getUser(agentUserId);
            String userName = "";
            if (agentUser != null) {
                userName = agentUser.getAccountName();
            }
            String modifyReason = StringUtils.formatMessage("关闭账号:{}，人员余额划归到部门“未分配”费用", userName);
            agentMaterialBudgetService.changeUserBalanceToGroup(groupId, agentUserId, modifyReason);
            // 关闭帐号
            orgConfigService.closeUserAccount(agentUserId);
        } catch (Exception ex) {
            logger.error(String.format("close user accounts field %d,groupId=%d", agentUserId, groupId), ex);
            return MapMessage.errorMessage("关闭帐号失败");
        }
        return msg;
    }

    /**
     * 重置密码
     *
     * @return
     */
    @RequestMapping(value = "resetAccountPassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    private MapMessage resetAccountPassword() {
        Long agentUserId = getRequestLong("agentUserId");
        Long groupId = getRequestLong("agentGroupId");      //所在的部门的ID
        if (agentUserId == 0L) {
            return MapMessage.errorMessage("用户帐号错误,请刷新页面后重试");
        }
        AuthCurrentUser user = getCurrentUser(); //当前用户
        if (!user.isAdmin() && !user.isCountryManager()) {
            return MapMessage.errorMessage("当前帐号无权进行次操作,请刷新页面后重试");
        }
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            // 重置密码
            orgConfigService.resetPassword(agentUserId);
        } catch (Exception ex) {
            logger.error(String.format("reset accounts  password field userId=%d,groupId=%d", agentUserId, groupId), ex);
            return MapMessage.errorMessage("重置当前帐号密码失败");
        }
        return msg;
    }

    /**
     * 保存调整用户的部门及角色的结果（调整部门）
     *
     * @return
     */
    @RequestMapping(value = "changeGroupForUser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeGroupForUser() {
        Long oldGroupId = getRequestLong("oldGroupId"); //旧的部门id
        Long newGroupId = getRequestLong("newGroupId");
        Long userId = getRequestLong("userId");
        Integer roleId = getRequestInt("roleId");
        try {
            AgentRoleType agentRoleType = AgentRoleType.of(roleId);
            if (agentRoleType == null) {
                return MapMessage.errorMessage("用户角色选择错误,请重新选择");
            }
            return orgConfigService.changeGroupForUser(oldGroupId, newGroupId, userId, agentRoleType);
        } catch (Exception ex) {
            logger.error(String.format("chang group for user is field oldGroupId=%d,newGroupId=%d,userId=%d,roleId=%d", oldGroupId, newGroupId, userId, roleId), ex);
            return MapMessage.errorMessage("调正用户部门及角色失败");
        }
    }

    /**
     * 给用户分配部门
     *
     * @return
     */
    @RequestMapping(value = "addGroupUser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addGroupUser() {

        Long groupId = getRequestLong("groupId");
        Long userId = getRequestLong("userId");
        Integer roleId = getRequestInt("roleId");
        try {
            AgentRoleType agentRoleType = AgentRoleType.of(roleId);
            if (agentRoleType == null) {
                return MapMessage.errorMessage("用户角色选择错误,请重新选择");
            }
            return orgConfigService.addGroupUser(groupId, userId, agentRoleType);
        } catch (Exception ex) {
            logger.error(String.format("chang group for user is field groupId=%d,userId=%d,roleId=%d", groupId, userId, roleId), ex);
            return MapMessage.errorMessage("调正用户部门及角色失败");
        }
    }


    /**
     * 保存调整用户的角色的结果 （调整角色）
     *
     * @return
     */
    @RequestMapping(value = "changeRoleForUser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeRoleForUser() {
        Long groupId = getRequestLong("groupId");
        Long userId = getRequestLong("userId");
        Integer roleId = getRequestInt("roleId");
        try {
            AgentRoleType agentRoleType = AgentRoleType.of(roleId);
            if (agentRoleType == null) {
                return MapMessage.errorMessage("用户角色选择错误,请重新选择");
            }
            return orgConfigService.updateGroupUserRole(groupId, userId, agentRoleType);
        } catch (Exception ex) {
            logger.error(String.format("change role fro user is field groupId=%d,userId=%d,roleId=%d", groupId, userId, roleId), ex);
            return MapMessage.errorMessage("调正用户角色失败");
        }
    }

    /**
     * 获取某个部门可以选择的角色列表（调整部门，调整角色）
     * groupId 调整部门时选择树上被选中的部门，调整角色时选择当前部门
     *
     * @return （调增部门选择的部门，可以显示的列表，或者调整用户角色可以显示的角色列表）
     */
    @RequestMapping(value = "getGroupRoleList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupRoleList() {
        Long userId = getRequestLong("agentUserId"); //如果传了
        Long groupId = getRequestLong("groupId");
        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        try {
            AgentGroup group = baseOrgService.getGroupById(groupId);
            if (group == null) {
                return MapMessage.errorMessage(String.format("ID为%d的部门不存在", groupId));
            }
            List<AgentGroupRoleInfo> roleList = getRoleList(group.getRoleId());
            roleList = roleList.stream().filter(p -> {
                if (userId == 0) {
                    return true;
                }
                Map<Long, Integer> roleMap = baseOrgService.getGroupUserRoleMapByUserId(userId);
                if (roleMap == null) {
                    return true;
                }
                if (!roleMap.containsKey(groupId)) {
                    return true;
                }
                Integer roleId = roleMap.get(groupId);
                if (roleId == null) {
                    return true;
                }
                if (p.getURoleId() == null) {
                    return true;
                }
                return p.getURoleId() != roleId;
            }).collect(Collectors.toList());
            msg.put("roleList", roleList);
        } catch (Exception ex) {
            logger.error(String.format("get group role list is failed groupId=%d", groupId), ex);
            return MapMessage.errorMessage(String.format("无法获取ID为%d的部门的角色列表", groupId));
        }
        return msg;
    }

    /**
     * 获取调整所属部门的新部门树（调整部门）
     */
    @RequestMapping(value = "getNewDepartmentTree.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String getNewDepartmentTree() {
        AuthCurrentUser user = getCurrentUser();
        Set<Long> markGroupIds = requestLongSet("groupIds");
        List<Map<String, Object>> maps = baseOrgService.loadUserGroupTree(user);
        if (CollectionUtils.isNotEmpty(markGroupIds) && CollectionUtils.isNotEmpty(maps)) {
            baseOrgService.markSelectedGroup(maps, markGroupIds);
        }
        return JsonUtils.toJson(maps);
    }

    /**
     * 是否是这个部门的管理者
     */
    private Boolean isGroupManager(Long userId, Long groupId) {
        //数据错误直接返回false
        return !(userId == null || groupId == 0L) && baseOrgService.isGroupManager(userId, groupId);
    }

    // 如果当前用户是这个部门的管理者就不能被管理，并且当前用户不是市经理或者是市经理但是所选的用户不是代理和有限代理 就可以被管理
    private Boolean isManageAbleUser(Long userId, Long groupId, Boolean isCityManage, Integer userRoleId) {
        return !isGroupManager(userId, groupId) && (!isCityManage || (userRoleId != 15 && userRoleId != 16));
    }

   /* private Boolean isManageAbleUser(Long userId, Long groupId, Boolean isCityManage) {
        return !isGroupManager(userId, groupId) && !isCityManage;
    }*/

    /**
     * 部门负责的区域列表
     */
    private List<AgentGroupRegionInfo> createAgentGroupRegionInfo(List<AgentGroupRegion> agentGroupRegionList) {
        if (CollectionUtils.isEmpty(agentGroupRegionList)) {
            return Collections.emptyList();
        }
        List<AgentGroupRegionInfo> agentGroupRegionInfoList = new ArrayList<>();
        agentGroupRegionList.forEach(p -> {
            AgentGroupRegionInfo agentGroupRegionInfo = new AgentGroupRegionInfo();
//            Integer schoolLevel = ConversionUtils.toInt(p.getSchoolLevel());
//            agentGroupRegionInfo.setHasJunior(Objects.equals(schoolLevel, SchoolLevelType.JUNIOR.getLevel()) || Objects.equals(schoolLevel, SchoolLevelType.ALL.getLevel()));
//            agentGroupRegionInfo.setHasMiddle(Objects.equals(schoolLevel, SchoolLevelType.MIDDLE.getLevel()) || Objects.equals(schoolLevel, SchoolLevelType.ALL.getLevel()));
            Integer regionCode = p.getRegionCode();
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (exRegion != null) {
                if (exRegion.fetchRegionType() == RegionType.PROVINCE) {
                    agentGroupRegionInfo.setProvinceName(exRegion.getProvinceName());
                } else if (exRegion.fetchRegionType() == RegionType.CITY) {
                    agentGroupRegionInfo.setProvinceName(exRegion.getProvinceName());
                    agentGroupRegionInfo.setCityName(exRegion.getCityName());
                } else {
                    agentGroupRegionInfo.setProvinceName(exRegion.getProvinceName());
                    agentGroupRegionInfo.setCityName(exRegion.getCityName());
                    agentGroupRegionInfo.setCountyName(exRegion.getCountyName());
                }
                agentGroupRegionInfoList.add(agentGroupRegionInfo);
            }
        });
        return agentGroupRegionInfoList;
    }

    /**
     * 获取当前用户在该部门下可以获取的用户角色
     * 把枚举的AgentRoleType 转换成页面可以用的 AgentGroupRoleInfo
     */
    List<AgentGroupRoleInfo> getRoleList(Integer roleId) {
        AgentGroupRoleType groupRoleType = AgentGroupRoleType.of(roleId);
        List<AgentRoleType> agentRoleTypeList = baseOrgService.getAgentRoleTypeList(groupRoleType);
        if (CollectionUtils.isEmpty(agentRoleTypeList)) {
            return Collections.emptyList();
        }
        List<AgentGroupRoleInfo> agentGroupRoleInfoList = new ArrayList<>();
        agentRoleTypeList.forEach(p -> {
            AgentGroupRoleInfo agentGroupRoleInfo = new AgentGroupRoleInfo();
            agentGroupRoleInfo.setURoleId(p.getId());
            agentGroupRoleInfo.setURoleName(p.getRoleName());
            agentGroupRoleInfoList.add(agentGroupRoleInfo);
        });
        return agentGroupRoleInfoList;
    }

    private List<SchoolShortInfo> createAgentSchoolInfo(List<School> schoolIds) {
        return createAgentSchoolInfo(schoolIds, false);// theUserIsManageAble 此字段只对人员部门信息展示有意义
    }


    private List<SchoolShortInfo> createAgentSchoolInfo(List<School> schoolIds, Boolean theUserIsManageAble) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        List<SchoolShortInfo> SchoolShortInfos = new ArrayList<>();
        schoolIds.forEach(p -> {
            if (p == null) {
                return;
            }
            SchoolShortInfo info = new SchoolShortInfo();
            info.setSchoolId(p.getId());
            info.setSchoolName(p.getCname());
            info.setLevel(p.getLevel());
            info.setTheUserIsManageAble(theUserIsManageAble);
            SchoolShortInfos.add(info);
        });
        return SchoolShortInfos;
    }

    /**
     * 添加子部门详情页，讲部门类型转成对象
     */
    private List<AgentGroupLevelInfo> getAgentGroupLevelInfo(List<AgentGroupRoleType> agentGroupRoleTypeList) {
        if (CollectionUtils.isEmpty(agentGroupRoleTypeList)) {
            return Collections.emptyList();
        }
        List<AgentGroupLevelInfo> agentGroupLevelInfoList = new ArrayList<>();
        agentGroupRoleTypeList.forEach(p -> {
            AgentGroupLevelInfo agentGroupLevelInfo = new AgentGroupLevelInfo();
            agentGroupLevelInfo.setDplLevId(p.getId());
            agentGroupLevelInfo.setDpLName(p.getRoleName());
            agentGroupLevelInfoList.add(agentGroupLevelInfo);
        });
        return agentGroupLevelInfoList;
    }


    /**
     * 下载agentUser 用户所负责的学校
     *
     * @param agentUser      需要下载学校的账户ID
     * @param userSchoolList 该账户锁负责的学校
     * @return 返回对应的Excel
     */
    private XSSFWorkbook convertToResponsibleSchool(AgentGroup group, AgentUser agentUser, List<School> userSchoolList) {
        //TODO 处理Excel格式
        Resource resource = new ClassPathResource(USER_ACCOUNT_SCHOOL_DETAIL);
        if (!resource.exists()) {
            logger.error("exportSchoolTasks - template not exists");
            return null;
        }
        try {
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet userInfo = workbook.getSheetAt(0);
            XSSFSheet schoolDetail = workbook.getSheetAt(1);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            Map<Long, Integer> groupUserRoleMap = baseOrgService.getGroupUserRoleMapByUserId(agentUser.getId());
            XSSFRow row1 = userInfo.createRow(0);
            createCell(row1, 0, cellStyle, "姓名：");
            createCell(row1, 1, cellStyle, format(agentUser.getRealName()));
            createCell(row1, 2, cellStyle, "帐号：");
            createCell(row1, 3, cellStyle, format(agentUser.getAccountName()));
            XSSFRow row2 = userInfo.createRow(1);
            createCell(row2, 0, cellStyle, "所属部门：");
            createCell(row2, 1, cellStyle, format(group.getGroupName()));        //所属部门
            createCell(row2, 2, cellStyle, "角色：");
            createCell(row2, 3, cellStyle, format(groupUserRoleMap.get(group.getId()) != null ? AgentRoleType.of(groupUserRoleMap.get(group.getId())).getRoleName() : ""));     //用户角色
            XSSFRow row3 = userInfo.createRow(2);
            createCell(row3, 0, cellStyle, "电话：");
            createCell(row3, 1, cellStyle, format(agentUser.getTel()));
            createCell(row3, 2, cellStyle, "地址：");
            createCell(row3, 3, cellStyle, format(agentUser.getAddress()));
            XSSFRow row4 = userInfo.createRow(3);
            createCell(row4, 0, cellStyle, "保证金：");
            createCell(row4, 1, cellStyle, format(agentUser.getCashDeposit()));
            createCell(row4, 2, cellStyle, "开户行：");
            createCell(row4, 3, cellStyle, format(agentUser.getBankName()));
            XSSFRow row5 = userInfo.createRow(4);
            createCell(row5, 0, cellStyle, "开户人：");
            createCell(row5, 1, cellStyle, format(agentUser.getBankHostName()));
            createCell(row5, 2, cellStyle, "银行帐号：");
            createCell(row5, 3, cellStyle, format(agentUser.getBankAccount()));
            XSSFRow row6 = userInfo.createRow(5);
            createCell(row6, 0, cellStyle, "合同开始时间：");
            createCell(row6, 1, cellStyle, formatTime(agentUser.getContractStartDate()));
            createCell(row6, 2, cellStyle, "合同结束时间：");
            createCell(row6, 3, cellStyle, formatTime(agentUser.getContractEndDate()));
            XSSFRow row7 = userInfo.createRow(6);
            createCell(row7, 0, cellStyle, "合同编号：");
            createCell(row7, 1, cellStyle, format(agentUser.getContractNumber()));
            XSSFRow row8 = userInfo.createRow(7);
            createCell(row8, 0, cellStyle, "简介：");
            createCell(row8, 1, cellStyle, format(agentUser.getUserComment()));

            if (CollectionUtils.isNotEmpty(userSchoolList)) {
                Set<Long> schoolIds = userSchoolList.stream().map(School::getId).collect(Collectors.toSet());
                List<AgentDictSchool> agentDictSchools = orgConfigService.getAgentGroupSchoolBySchoolIds(schoolIds);
                Map<Long, List<AgentDictSchool>> dictSchoolMap = agentDictSchools.stream().collect(Collectors.groupingBy(AgentDictSchool::getSchoolId));
                if (dictSchoolMap == null) {
                    return workbook;
                }
                int index = 1;
                for (School school : userSchoolList) {
                    if (school == null) {
                        continue;
                    }
                    XSSFRow row = schoolDetail.createRow(index++);
                    createCell(row, 0, cellStyle, format(school.getId()));
                    createCell(row, 1, cellStyle, format(school.getCname()));
                    createCell(row, 2, cellStyle, format(school.getLevel() != null ? SchoolLevel.safeParse(school.getLevel()).getDescription() : ""));
                    createCell(row, 3, cellStyle, format(school.getRegionCode()));
                }
            }
            return workbook;
        } catch (Exception ex) {
            return null;
        }
    }

    private XSSFCell createCell(XSSFRow row, int index, XSSFCellStyle style, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    private String formatTime(Date time) {
        return time == null ? "" : TIME_FORMAT.format(time);
    }

    private String format(Object value) {
        return value == null ? "" : String.valueOf(value);
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


    // 获取物料预算变动记录， 余额变动记录  type=1:物料预算变动记录  type=2:余额变动记录
    @RequestMapping(value = "getCashDataRecord.vpage")
    @ResponseBody
    public MapMessage getCashDataRecord() {
        Long userId = getRequestLong("userId");
        Integer type = getRequestInt("type");
        List dataList = null;
        if (type == 1) {
            dataList = orgConfigService.loadUserMaterialBudgetRecordList(userId);
        } else if (type == 2) {
            dataList = orgConfigService.loadUserCashRecordList(userId);
        }
        MapMessage message = MapMessage.successMessage();
        message.put("dataList", dataList);
        return message;
    }

    /**
     * 导出组织结构
     */
    @RequestMapping(value = "exportOrganization.vpage", method = RequestMethod.GET)
    public void exportOrganization() {
        Long agentGroupId = getRequestLong("agentGroupId");
        List<Map<String, Object>> organizationList = new ArrayList<>();
        try {
            //组装各部门之间的等级对应关系
            List<GroupData> groupDataList = new ArrayList<>();
            //导出部门结构中，部门级别为“市场部”的所有下属人员信息
            List<AgentGroupUser> groupUserList = new ArrayList<>();
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(agentGroupId);
            subGroupList.forEach(p -> {
                if (p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit) {
                    List<AgentGroup> subSubGroupList = baseOrgService.getGroupListByParentId(p.getId());
                    subSubGroupList.forEach(item -> {
                        if (item.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                            groupUserList.addAll(baseOrgService.getAllGroupUsersByGroupIdWithGroupData(item.getId(), groupDataList));
                        }
                    });
                }
            });

            Map<Long, GroupData> groupDataMap = groupDataList.stream().collect(Collectors.toMap(GroupData::getGroupId, Function.identity(), (o1, o2) -> o1));

            Set<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            Set<Long> groupIds = groupUserList.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
            Map<Long, AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
            //用户与部门对应关系
            Map<Long, Long> userGroupIdMap = groupUserList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, AgentGroupUser::getGroupId, (o1, o2) -> o1));

            //用户与角色对应关系
            Map<Long, Integer> userRoleMap = groupUserList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, AgentGroupUser::getUserRoleId, (o1, o2) -> o1));
            //用户与学校对应关系
            Map<Long, List<AgentUserSchool>> userSchoolMap = agentUserSchoolLoaderClient.findByUserIds(userIds);
            //部门与区域对应关系
            Map<Long, List<AgentGroupRegion>> groupRegionMap = agentGroupRegionLoaderClient.findByGroupIds(groupIds);

            userMap.forEach((userId, user) -> {
                if (null != userId && null != user) {
                    Map<String, Object> organizationMap = new HashMap<>();
                    //获取角色信息
                    AgentRoleType agentRoleType = null;
                    Integer roleId = userRoleMap.get(userId);
                    if (null != roleId) {
                        agentRoleType = AgentRoleType.of(roleId);
                        //设置“角色”
                        organizationMap.put("userRole", agentRoleType != null ? agentRoleType.getRoleName() : "");
                    } else {
                        organizationMap.put("userRole", "");
                    }

                    //设置“姓名”
                    organizationMap.put("realName", ConversionUtils.toString(user.getRealName()));
                    //设置“账号”
                    organizationMap.put("accountName", ConversionUtils.toString(user.getAccountName()));
                    //设置“userId”
                    organizationMap.put("userId", userId);
                    //设置“工号”
                    organizationMap.put("accountNumber", ConversionUtils.toString(user.getAccountNumber()));

                    if (userGroupIdMap.containsKey(userId)) {
                        Long groupId = userGroupIdMap.get(userId);

                        GroupData groupData = groupDataMap.get(groupId);
                        if (null != groupData) {
                            organizationMap.put("marketingName", groupData.getMarketingName());
                            organizationMap.put("regionName", groupData.getRegionName());
                            organizationMap.put("areaName", groupData.getAreaName());
                            organizationMap.put("cityName", groupData.getCityName());
                        }

                        int schoolNum = 0;
                        //如果是“专员”或者“市经理”
                        if (agentRoleType == AgentRoleType.BusinessDeveloper || agentRoleType == AgentRoleType.CityManager) {
                            Set<String> cityNameList = new HashSet<>();
                            Set<Integer> regionCodes = new HashSet<>();
                            //如果是“专员”，“负责区域”显示专员的学校所属的城市
                            if (agentRoleType == AgentRoleType.BusinessDeveloper) {
                                //获取该用户负责的学校
                                List<AgentUserSchool> userSchoolList = userSchoolMap.get(userId);
                                if (CollectionUtils.isNotEmpty(userSchoolList)) {
                                    schoolNum = userSchoolList.size();

                                    regionCodes.addAll(userSchoolList.stream().map(AgentUserSchool::getRegionCode).collect(Collectors.toSet()));
                                }
                                //如果是“市经理”，“负责区域”显示市经理所负责的城市
                            } else if (agentRoleType == AgentRoleType.CityManager) {

                                //获取该部门负责的学校
                                List<Long> groupSchoolList = baseOrgService.getManagedSchoolListByGroupId(groupId);

                                if (CollectionUtils.isNotEmpty(groupSchoolList)) {
                                    schoolNum = groupSchoolList.size();
                                }
                                //获取该部门负责的区域编码
                                List<AgentGroupRegion> groupRegionList = groupRegionMap.get(groupId);
                                if (CollectionUtils.isNotEmpty(groupRegionList)) {
                                    regionCodes.addAll(groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet()));
                                }

                            }
                            Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
                            regionCodes.forEach(item -> {
                                if (exRegionMap.containsKey(item)) {
                                    ExRegion exRegion = exRegionMap.get(item);
                                    if (null != exRegion) {
                                        cityNameList.add(exRegion.getCityName());
                                    }
                                }
                            });
                            //设置“负责区域”
                            organizationMap.put("agentGroupRegionInfo", StringUtils.join(cityNameList.stream().collect(Collectors.toList()), "、"));
                        } else {
                            schoolNum = baseOrgService.getManagedSchoolListByGroupId(groupId).size();
                        }
                        //设置负责学校数量
                        organizationMap.put("schoolNum", schoolNum);
                        organizationList.add(organizationMap);
                    }
                }
            });
            //获取当前时间
            String nowTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME);
            //设置导出文件名
            String fileName = "市场部组织结构" + nowTime + ".xlsx";
            //导出Excel文件
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            orgConfigService.exportOrganization(workbook, organizationList);
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (Exception e) {
            logger.error("error info: ", e);
            emailServiceClient.createPlainEmail()
                    .body("error info: " + e)
                    .subject("市场部组织结构数据导出异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }

    /**
     * 解除蜂巢账号绑定
     * @return
     */
    @RequestMapping(value = "unBindHoneycomb.vpage")
    @ResponseBody
    public MapMessage unBindHoneycomb() {
        Long userId = getRequestLong("userId");
        if (userId <= 0) {
            return MapMessage.errorMessage("请选择解除对象！");
        }
        honeycombUserService.unbindAgentUser(userId);
        agentPartnerService.removeFansByUserId(userId);
        return MapMessage.successMessage();
    }

}
