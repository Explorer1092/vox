
/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.account;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.PerformanceData;
import com.voxlearning.utopia.agent.bean.incomes2016.UserIncomeS2016Bean;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016Persistence;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.agent.service.account.AccountService;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentPerformanceConfigService;
import com.voxlearning.utopia.agent.service.sysconfig.ProductConfigService;
import com.voxlearning.utopia.agent.service.user.UserConfigService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.entity.agent.AgentPerformanceConfig;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/15.
 */
@Controller
@RequestMapping("/account")
@Slf4j
public class AccountController extends AbstractAgentController {

    private static final String UNIT = "万仟佰拾亿仟佰拾万仟佰拾元角分";
    private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";
    private static final double MAX_VALUE = 9999999999999.99D;
    private static final Integer LESS_DAY = 10;

    private static final List<String> JuniorAgentType = Arrays.asList(
            SalaryKpiType.JUNIOR_CLUE_SUPPORT.getDesc(),
            SalaryKpiType.JUNIOR_MEETING_CITY.getDesc(),
            SalaryKpiType.JUNIOR_MEETING_COUNTY.getDesc(),
            SalaryKpiType.JUNIOR_MEETING_INTERRUPTED.getDesc()
    );

    private static final List<String> MiddleAgentType = Arrays.asList(
            SalaryKpiType.MIDDLE_CLUE_SUPPORT.getDesc(),
            SalaryKpiType.MIDDLE_MEETING_CITY.getDesc(),
            SalaryKpiType.MIDDLE_MEETING_COUNTY.getDesc(),
            SalaryKpiType.MIDDLE_MEETING_INTERRUPTED.getDesc()
    );

    private static final int titleIndex = 3; // 乙方
    private static final int contDateIndex = 9; // 合同日期
    private static final int contractIndex = 23; // 合同
    private static final int monthIndex = 15; // 结算月
    private static final int scaleIndex = 0; // 开发基数
    private static final int targetIndex = 20; // 预算
    private static final int resultIndex = 29; // 实绩
    private static final int ratioIndex = 4; // 完成率
    private static final int clueIndex = 17; // 线索
    private static final int cityIndex = 25; // 市级会议
    private static final int countyIndex = 5; // 区级会议
    private static final int cutIndex = 18; // 插播会议
    private static final int startIndex = 16; // 开始
    private static final int endIndex = 25; // 结束
    private static final int amountIndex = 6; // 金额

    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Inject private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject private AgentOrderServiceClient agentOrderServiceClient;
    @Inject BaseUserService baseUserService;
    @Inject AccountService accountService;
    @Inject UserConfigService userConfigService;
    @Inject BaseOrderService baseOrderService;
    @Inject BaseGroupService baseGroupService;
    @Inject ProductConfigService productConfigService;
    @Inject private AgentPerformanceConfigService agentPerformanceConfigService;
    @Inject private PerformanceService performanceService;
    @Inject private AgentUserKpiResultSpring2016Persistence agentUserKpiResultSpring2016Persistence;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private WorkRecordService workRecordService;

    @RequestMapping(value = "myaccount/index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        model.addAttribute("user", baseUserService.getById(getCurrentUserId()));
//        model.addAttribute("orders", accountService.getOrdersByUserId(getCurrentUserId()));
        model.addAttribute("myincome", accountService.getUserIncome(0, getCurrentUserId()));
        model.addAttribute("invoiceList", accountService.getInvoiceList(getCurrentUserId()));

        // 获取所有下属的收入
        List<AgentUser> memberList = baseOrgService.getManagedGroupUsers(getCurrentUserId(), false);
        if (CollectionUtils.isNotEmpty(memberList)) {
            List<UserIncomeS2016Bean> memberIncome = memberList.stream()
                    .filter(e -> e != null && e.getId() != null)
                    .map(AgentUser::getId)
                    .map(e -> accountService.getUserIncome(0, e))
                    .filter(e -> e != null)
                    .collect(Collectors.toList());
            model.addAttribute("memberIncome", memberIncome);
        }

        model.addAttribute("marketData", getMarketData(getCurrentUserId()));

        return "account/myaccount/index";
    }

    // 页面控制能看到此页的人都为代理
    private Map<String, Object> getMarketData(Long userId) {
        Map<String, Object> retMap = new HashMap<>();
        Integer day = performanceService.lastSuccessDataDay();

        Date end = new Date();
        Date start = DayUtils.getFirstDayOfMonth(end);
        if(DayUtils.getDay(new Date()) <= 10){ // 10号以前查看上月数据
            start = DateUtils.addMonths(start, -1);
            end = DayUtils.getLastDayOfMonth(start);
        }

        String regionGroupName = getGroupName(userId, AgentGroupRoleType.Region);
        String cityGroupName = getGroupName(userId, AgentGroupRoleType.City);
        Integer endDay = ConversionUtils.toInt(DayUtils.DATE_FORMAT.format(end));
        if (endDay < day) {
            day = endDay;
        }
        List<Long> managedJuniorSchools = baseOrgService.getManagedJuniorSchoolList(userId);
        List<Long> managedMiddleSchools = baseOrgService.getManagedMiddleSchoolList(userId);

        List<Long> shoolIdList = new ArrayList<>();
        List<Map<String, Object>> performanceDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(managedJuniorSchools)) {
            Map<String, Object> juniorDataMap = new HashMap<>();
            juniorDataMap.put("regionGroupName", regionGroupName);
            juniorDataMap.put("cityGroupName", cityGroupName);
            juniorDataMap.put("schoolLevel", "小学");
            juniorDataMap.put("schoolCount", managedJuniorSchools.size());
            juniorDataMap.put("totalStudentsCount", getTotalSchoolSize(managedJuniorSchools));
            juniorDataMap.put("budget", 0);
            juniorDataMap.put("complete", 0);
            juniorDataMap.put("completeRate", 0d);
            performanceDataList.add(juniorDataMap);
            shoolIdList.addAll(managedJuniorSchools);
        }
        if (CollectionUtils.isNotEmpty(managedMiddleSchools)) {
            Map<String, Object> juniorDataMap = new HashMap<>();
            juniorDataMap.put("regionGroupName", regionGroupName);
            juniorDataMap.put("cityGroupName", cityGroupName);
            juniorDataMap.put("schoolLevel", "中学");
            juniorDataMap.put("schoolCount", managedMiddleSchools.size());
            juniorDataMap.put("totalStudentsCount", getTotalSchoolSize(managedMiddleSchools));
            juniorDataMap.put("budget", 0);
            juniorDataMap.put("complete", 0);
            juniorDataMap.put("completeRate", 0d);
            performanceDataList.add(juniorDataMap);
            shoolIdList.addAll(managedMiddleSchools);
        }
        retMap.put("perfromanceDataList", performanceDataList);

        // 获取绩效配置信息

        Map<String, Object> meetingAndClueMap = new HashMap<>();


        List<CrmWorkRecord> workRecordList = new ArrayList<>();
        List<CrmWorkRecord> schoolRecordList = workRecordList.stream().filter(p -> p.getWorkType() == CrmWorkRecordType.SCHOOL).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schoolRecordList)) {
            meetingAndClueMap.put("juniorClueCount", 0);
            meetingAndClueMap.put("middleClueCount", 0);
        } else {
            Set<Long> schoolIdSet = schoolRecordList.stream().map(CrmWorkRecord::getSchoolId).collect(Collectors.toSet());
            meetingAndClueMap.put("juniorClueCount", baseOrgService.getSchoolListByLevel(schoolIdSet, SchoolLevel.JUNIOR).size());
            meetingAndClueMap.put("middleClueCount", baseOrgService.getSchoolListByLevel(schoolIdSet, SchoolLevel.MIDDLE).size());
        }
        retMap.put("meetingAndClueData", meetingAndClueMap);

        List<Map<String, Object>> schoolDataList = shoolIdList.stream().map(p -> {
            CrmSchoolSummary schoolSummary = crmSummaryLoaderClient.loadSchoolSummary(p);
            if (schoolSummary == null) {
                return null;
            }
            Map<String, Object> schoolDataMap = new HashMap<>();
            schoolDataMap.put("cityName", schoolSummary.getCityName());
            schoolDataMap.put("countyName", schoolSummary.getCountyName());
            schoolDataMap.put("countyCode", schoolSummary.getCountyCode());
            schoolDataMap.put("schoolId", schoolSummary.getSchoolId());
            schoolDataMap.put("schoolName", schoolSummary.getSchoolName());
            schoolDataMap.put("schoolLevel", schoolSummary.getSchoolLevel());
            return schoolDataMap;
        }).filter(Objects::nonNull).collect(Collectors.toList());
//        .sorted((o1, o2) -> {
//            if(o1.get("countyCode") == null || o2.get("countyCode") == null){
//                return 0;
//            }
//            return (((Integer) o1.get("countyCode"))).compareTo((Integer) o2.get("countyCode"));
//        }).collect(Collectors.toList());
        retMap.put("schoolDataList", schoolDataList);

        List<CrmWorkRecord> meetingWorkRecord = workRecordList.stream().filter(p -> p.getWorkType() == CrmWorkRecordType.MEETING).collect(Collectors.toList());
        List<Map<String, Object>> meetingDataList = meetingWorkRecord.stream().map(this::createMeetingData).filter(Objects::nonNull).collect(Collectors.toList());
        retMap.put("meetingDataList", meetingDataList);

        return retMap;
    }

    private Map<String, Object> createMeetingData(CrmWorkRecord workRecord) {
        if (workRecord == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("meetingTime", DateUtils.dateToString(workRecord.getWorkTime(), DateUtils.FORMAT_SQL_DATE));
        // workType:"MEETING",partnerName:{$eq:null},createTime:{$gte:new Date('2016/10/1 00:00:00')} 不存在空值
        result.put("meetingPlace", StringUtils.formatMessage("{} {} {}",
                SafeConverter.toString(workRecord.getProvinceName()),
                SafeConverter.toString(workRecord.getCityName()),
                SafeConverter.toString(workRecord.getCountyName())));
        result.put("meetingTitle", workRecord.getWorkTitle());
        result.put("meetingLevel", SafeConverter.toString(workRecord.getMeetingType() != null ? workRecord.getMeetingType().getValue() : ""));
        result.put("meetingType", SafeConverter.toInt(workRecord.getShowFrom()) == 1 ? "专场" : "插播");
        return result;
    }

    // 获取学校规模之和
    private Integer getTotalSchoolSize(List<Long> schoolList) {
        if (CollectionUtils.isEmpty(schoolList)) {
            return 0;
        }
        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolsExtInfoAsMap(schoolList)
                .getUninterruptibly();
        if (MapUtils.isEmpty(schoolExtInfoMap)) {
            return 0;
        }
        return schoolExtInfoMap.values().stream().filter(p -> p != null).map(SchoolExtInfo::getSchoolSize).filter(p -> p != null).reduce(0, (x, y) -> (x + y));
    }

    // 获取所在级别部门的名称
    private String getGroupName(Long userId, AgentGroupRoleType groupRoleType) {
        List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return "";
        }
        Long groupId = groupIdList.get(0);
        // 获取当前用户所在的大区级部门
        AgentGroup group = baseOrgService.getParentGroupByRole(groupId, groupRoleType);
        if (group == null) {
            return "";
        }
        return group.getGroupName();
    }

    @RequestMapping(value = "memberincome/index.vpage", method = RequestMethod.GET)
    String memberIncomeIndex(Model model) {
        Long memberId = getRequestLong("memberId");

        List<AgentUser> memberList = null;
        if (memberId == 0) {
            memberList = baseOrgService.getManagedGroupUsers(getCurrentUserId(), false);
        } else {
            memberList = baseOrgService.getManagedGroupUsers(memberId, false);
        }

        if (CollectionUtils.isNotEmpty(memberList)) {
            List<UserIncomeS2016Bean> memberIncome = memberList.stream()
                    .filter(e -> e != null && e.getId() != null)
                    .map(AgentUser::getId)
                    .map(e -> accountService.getUserIncome(0, e))
                    .filter(e -> e != null)
                    .collect(Collectors.toList());
            model.addAttribute("memberIncome", memberIncome);
        }

        return "account/memberincome/index";
    }


    @RequestMapping(value = "myorder/index.vpage", method = RequestMethod.GET)
    String orderIndex(Model model) {
        model.addAttribute("user", baseUserService.getById(getCurrentUserId()));
        model.addAttribute("orders", baseOrderService.loadUserOrders(getCurrentUserId()));
        return "account/myorder/index";
    }

    @RequestMapping(value = "myaccount/editprofile.vpage", method = RequestMethod.GET)
    String toEditProfilePage(Model model) {
        model.addAttribute("user", baseUserService.getById(getCurrentUserId()));
        return "account/myaccount/editprofile";
    }

    @RequestMapping(value = "myaccount/editprofile.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage editProfile() {
        // fix bug #20900 , using getRequestParameter instead of function params
        MapMessage mapMessage = new MapMessage();
        Long userId = getCurrentUserId();
        try {
            String userComment = getRequestString("userComment");
            String tel = getRequestString("tel");
            String email = getRequestString("email");
            String imAccount = getRequestString("imAccount");
            String address = getRequestString("address");
            String bankName = getRequestString("bankName");
            String bankHostname = getRequestString("bankHostname");
            String bankAccount = getRequestString("bankAccount");
            AgentUser agentUser = baseUserService.getById(userId);
            if (agentUser != null) {
                agentUser.setUserComment(userComment);
                agentUser.setTel(tel);
                agentUser.setEmail(email);
                agentUser.setImAccount(imAccount);
                agentUser.setAddress(address);
                agentUser.setCashDeposit(agentUser.getCashDeposit());
                agentUser.setBankName(bankName);
                agentUser.setBankHostName(bankHostname);
                agentUser.setBankAccount(bankAccount);
                baseUserService.updateAgentUser(agentUser);
            }
            mapMessage.setSuccess(true);
            asyncLogService.logUserModified(getCurrentUser(), getRequest().getRequestURI(),
                    "editProfile " + userId, tel + "," +
                            email + "," + imAccount + "," + address + "," + userComment + "," +
                            bankName + "," + bankHostname + "," + bankAccount
            );
        } catch (Exception ex) {
            log.error("编辑个人信息失败,userId:{},pointAmount:{},msg:{}", userId, ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("操作失败!" + ex.getMessage());
            return mapMessage;
        }
        return mapMessage;
    }

    @RequestMapping(value = "myaccount/printinvoice.vpage", method = RequestMethod.POST)
    void printInvoice(HttpServletResponse response) {

        try {
            Integer salaryMonth = getRequestInt("salaryMonth");

            HSSFWorkbook hssfWorkbook = convertToHSSfWorkbook(salaryMonth, baseUserService.getById(getCurrentUserId()));
            String filename = "付款证明" + getRequestString("salaryMonth") + ".xls";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();

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

    private HSSFWorkbook convertToHSSfWorkbook(Integer salaryMonth, AgentUser user) throws Exception {
        Resource resource = new ClassPathResource("/config/payment_invoice_template.xls");
        if (!resource.exists()) {
            throw new RuntimeException("template file not exists!");
        }

        @Cleanup InputStream inStream = resource.getInputStream();
        POIFSFileSystem fs = new POIFSFileSystem(inStream);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fs);
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);

        HSSFFont f = hssfWorkbook.createFont();
        f.setFontHeightInPoints((short) 14);//字号
        HSSFCellStyle cellStyle = (HSSFCellStyle) hssfWorkbook.createCellStyle();
        cellStyle.setFont(f);

        // 合同乙方
        if (StringUtils.isNotEmpty(user.getRealName())) {
            HSSFRow row = sheet.getRow(5);
            HSSFCell cell = row.createCell(3);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getRealName());
        }

        // 合同开始日期
        if (user.getContractStartDate() != null) {
            HSSFRow row = sheet.getRow(8);
            HSSFCell cell = row.createCell(9);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(DateUtils.dateToString(user.getContractStartDate(), "yyyy年MM月dd日"));
        }

        // 合同编号
        if (StringUtils.isNotEmpty(user.getContractNumber())) {
            HSSFRow row = sheet.getRow(8);
            HSSFCell cell = row.createCell(23);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getContractNumber());
        }

        // 金额
        UserIncomeS2016Bean userIncome = accountService.getUserIncome(salaryMonth, user.getId());
        if (userIncome != null) {
            Long amount = userIncome.getUserIncome();
            String capValue = change(amount);
            String amountFormat = "{0}，（￥{1}元）。";
            DecimalFormat df = new DecimalFormat("###,##0.00");
            String amountValue = MessageFormat.format(amountFormat, capValue, df.format(amount));
            HSSFRow row = sheet.getRow(11);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(amountValue);
        }

        if (userIncome != null) {
            Date startTime = userIncome.getStarTime();
            HSSFRow row = sheet.getRow(9);
            HSSFCell cell = row.createCell(20);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(DateUtils.dateToString(startTime, "yyyy年MM月dd日"));
        }

        if (userIncome != null) {
            Date endTime = userIncome.getEndTime();
            HSSFRow row = sheet.getRow(10);
            HSSFCell cell = row.createCell(0);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(DateUtils.dateToString(endTime, "yyyy年MM月dd日"));
        }

        // 合同乙方
        if (StringUtils.isNotEmpty(user.getRealName())) {
            HSSFRow row = sheet.getRow(21);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getRealName());
        }

        // 开户行
        if (StringUtils.isNotEmpty(user.getBankName())) {
            HSSFRow row = sheet.getRow(22);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getBankName());
        }

        // 开户行
        if (StringUtils.isNotEmpty(user.getBankAccount())) {
            HSSFRow row = sheet.getRow(23);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getBankAccount());
        }

        return hssfWorkbook;
    }

    @RequestMapping(value = "myaccount/printagentinvoice.vpage", method = RequestMethod.POST)
    void printAgentInvoice(HttpServletResponse response) {

        try {
            Integer salaryMonth = getRequestInt("salaryMonth");
            Integer slv = getRequestInt("slv");
            SchoolLevel schoolLevel = SchoolLevel.safeParse(slv);
            HSSFWorkbook hssfWorkbook = null;
            if (SchoolLevel.JUNIOR == schoolLevel) {
                hssfWorkbook = juniorToHSSfWorkbook(salaryMonth, baseUserService.getById(getCurrentUserId()));
            } else if (SchoolLevel.MIDDLE == schoolLevel) {
                hssfWorkbook = middleToHSSfWorkbook(salaryMonth, baseUserService.getById(getCurrentUserId()));
            }
            if (hssfWorkbook == null) {
                return;
            }
            String filename = "付款证明（" + schoolLevel.getDescription() + "）" + getRequestString("salaryMonth") + ".xls";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();

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

    private HSSFWorkbook juniorToHSSfWorkbook(Integer salaryMonth, AgentUser user) throws Exception {
        Resource resource = new ClassPathResource("/config/templates/payment_invoice_template_primary.xls");
        if (!resource.exists()) {
            throw new RuntimeException("template file not exists!");
        }

        @Cleanup InputStream inStream = resource.getInputStream();
        POIFSFileSystem fs = new POIFSFileSystem(inStream);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fs);
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);

        HSSFFont f = hssfWorkbook.createFont();
        f.setFontHeightInPoints((short) 14);//字号
        HSSFCellStyle cellStyle = (HSSFCellStyle) hssfWorkbook.createCellStyle();
        cellStyle.setFont(f);

        // 合同乙方
        if (StringUtils.isNotEmpty(user.getRealName())) {
            HSSFRow row = sheet.getRow(5);
            HSSFCell cell = row.createCell(titleIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getRealName());
        }

        // 合同开始日期
        if (user.getContractStartDate() != null) {
            HSSFRow row = sheet.getRow(8);
            HSSFCell cell = row.createCell(contDateIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(DateUtils.dateToString(user.getContractStartDate(), "yyyy年MM月dd日"));
        }

        // 合同编号
        if (StringUtils.isNotEmpty(user.getContractNumber())) {
            HSSFRow row = sheet.getRow(8);
            HSSFCell cell = row.createCell(contractIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getContractNumber());
        }

        // 结算月份
        if (salaryMonth != 0) {
            String month = salaryMonth / 100 + "年" + salaryMonth % 100 + "月";
            HSSFRow row = sheet.getRow(9);
            HSSFCell cell = row.createCell(monthIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(month);
        }

        Date runDate = DateUtils.stringToDate(String.valueOf(salaryMonth), "yyyyMM");
        MonthRange monthRange = MonthRange.newInstance(runDate.getTime());
        Integer runDay = Integer.valueOf(DateUtils.dateToString(monthRange.getEndDate(), "yyyyMMdd"));
//        PerformanceData data = performanceService.loadUserData(user.getId(), runDay);
        AgentUserKpiResultSpring2016 data = agentUserKpiResultSpring2016Persistence.findUserIncome(salaryMonth, user.getId())
                .stream()
//                .filter(t -> t.getSchoolLevel() != null && SchoolLevel.JUNIOR.getLevel() == t.getSchoolLevel())
                .filter(t -> t.getCpaType() != null && SalaryKpiType.JUNIOR_CLUE_SUPPORT.getDesc().equals(t.getCpaType())) // 小学市场支持
                .findFirst().orElse(null);

        // 基数所在档位
        List<Long> schoolList = baseOrgService.getManagedJuniorSchoolList(user.getId());
        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolsExtInfoAsMap(schoolList)
                .getUninterruptibly();
        int schoolSum = schoolExtInfoMap.values().stream().mapToInt(t -> SafeConverter.toInt(t.getSchoolSize())).sum();
        String scale = getScaleString(SchoolLevel.JUNIOR, schoolSum);
        if (scale != null) {
            HSSFRow row = sheet.getRow(10);
            HSSFCell cell = row.createCell(scaleIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(scale);
        }

        // 预算
        Long budget = data == null ? 0 : data.getCpaTarget();
        HSSFRow row1 = sheet.getRow(10);
        HSSFCell cell1 = row1.createCell(targetIndex);
        cell1.setCellStyle(cellStyle);
        cell1.setCellValue(budget);

        // 实际完成
        Long target = data == null ? 0 : data.getCpaResult();
        HSSFRow row2 = sheet.getRow(10);
        HSSFCell cell2 = row2.createCell(resultIndex);
        cell2.setCellStyle(cellStyle);
        cell2.setCellValue(target);

        // 完成比例
        double ratio = data == null ? 0 : budget == 0 ? 0 : MathUtils.doubleDivide(target, budget, 4, BigDecimal.ROUND_HALF_UP);
        DecimalFormat df0 = new DecimalFormat("##.##%");
        HSSFRow row3 = sheet.getRow(11);
        HSSFCell cell3 = row3.createCell(ratioIndex);
        cell3.setCellStyle(cellStyle);
        cell3.setCellValue(df0.format(ratio));


        // 获取绩效配置信息
        AgentPerformanceConfig config = agentPerformanceConfigService.findByUserIdAndMonth(user.getId(), salaryMonth);

        // 有效线索
        Integer clue = config == null ? 0 : SafeConverter.toInt(config.getJuniorTheMothClue());
        HSSFRow row4 = sheet.getRow(11);
        HSSFCell cell4 = row4.createCell(clueIndex);
        cell4.setCellStyle(cellStyle);
        cell4.setCellValue(clue);

        // 市级专场会议
        Integer cityMeeting = config == null ? 0 : SafeConverter.toInt(config.getCityJuniorMeet());
        HSSFRow row5 = sheet.getRow(11);
        HSSFCell cell5 = row5.createCell(cityIndex);
        cell5.setCellStyle(cellStyle);
        cell5.setCellValue(cityMeeting);

        // 区级专场会议
        Integer countyMeeting = config == null ? 0 : SafeConverter.toInt(config.getCountyJuniorMeet());
        HSSFRow row6 = sheet.getRow(12);
        HSSFCell cell6 = row6.createCell(countyIndex);
        cell6.setCellStyle(cellStyle);
        cell6.setCellValue(countyMeeting);

        // 插播有效会议
        Integer cutMeeting = config == null ? 0 : SafeConverter.toInt(config.getInterCutJuniorMeet());
        HSSFRow row7 = sheet.getRow(12);
        HSSFCell cell7 = row7.createCell(cutIndex);
        cell7.setCellStyle(cellStyle);
        cell7.setCellValue(cutMeeting);


        UserIncomeS2016Bean userIncome = accountService.getUserIncomeByType(salaryMonth, user.getId(), JuniorAgentType);
        // 结算开始日期
        Date startTime = userIncome == null ? monthRange.getStartDate() : userIncome.getStarTime();
        HSSFRow row8 = sheet.getRow(13);
        HSSFCell cell8 = row8.createCell(startIndex);
        cell8.setCellStyle(cellStyle);
        cell8.setCellValue(DateUtils.dateToString(startTime, "yyyy年MM月dd日"));

        // 结算结束日期
        Date endTime = userIncome == null ? monthRange.getEndDate() : userIncome.getEndTime();
        HSSFRow row9 = sheet.getRow(13);
        HSSFCell cell9 = row9.createCell(endIndex);
        cell9.setCellStyle(cellStyle);
        cell9.setCellValue(DateUtils.dateToString(endTime, "yyyy年MM月dd日"));

        // 金额
        Long amount = userIncome == null ? 0 : userIncome.getUserIncome();
        String capValue = change(amount);
        String amountFormat = "{0}，（￥{1}元）。";
        DecimalFormat df = new DecimalFormat("###,##0.00");
        String amountValue = MessageFormat.format(amountFormat, capValue, df.format(amount));
        HSSFRow row10 = sheet.getRow(15);
        HSSFCell cell10 = row10.createCell(amountIndex);
        cell10.setCellStyle(cellStyle);
        cell10.setCellValue(amountValue);

        // 合同乙方
        if (StringUtils.isNotEmpty(user.getRealName())) {
            HSSFRow row = sheet.getRow(25);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getRealName());
        }

        // 开户行
        if (StringUtils.isNotEmpty(user.getBankName())) {
            HSSFRow row = sheet.getRow(26);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getBankName());
        }

        // 开户行账号
        if (StringUtils.isNotEmpty(user.getBankAccount())) {
            HSSFRow row = sheet.getRow(27);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getBankAccount());
        }

        return hssfWorkbook;
    }

    private HSSFWorkbook middleToHSSfWorkbook(Integer salaryMonth, AgentUser user) throws Exception {
        Resource resource = new ClassPathResource("/config/templates/payment_invoice_template_middle.xls");
        if (!resource.exists()) {
            throw new RuntimeException("template file not exists!");
        }

        @Cleanup InputStream inStream = resource.getInputStream();
        POIFSFileSystem fs = new POIFSFileSystem(inStream);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fs);
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);

        HSSFFont f = hssfWorkbook.createFont();
        f.setFontHeightInPoints((short) 14);//字号
        HSSFCellStyle cellStyle = (HSSFCellStyle) hssfWorkbook.createCellStyle();
        cellStyle.setFont(f);

        // 合同乙方
        if (StringUtils.isNotEmpty(user.getRealName())) {
            HSSFRow row = sheet.getRow(5);
            HSSFCell cell = row.createCell(titleIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getRealName());
        }

        // 合同开始日期
        if (user.getContractStartDate() != null) {
            HSSFRow row = sheet.getRow(8);
            HSSFCell cell = row.createCell(contDateIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(DateUtils.dateToString(user.getContractStartDate(), "yyyy年MM月dd日"));
        }

        // 合同编号
        if (StringUtils.isNotEmpty(user.getContractNumber())) {
            HSSFRow row = sheet.getRow(8);
            HSSFCell cell = row.createCell(contractIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getContractNumber());
        }

        // 结算月份
        if (salaryMonth != 0) {
            String month = salaryMonth / 100 + "年" + salaryMonth % 100 + "月";
            HSSFRow row = sheet.getRow(9);
            HSSFCell cell = row.createCell(monthIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(month);
        }

        Date runDate = DateUtils.stringToDate(String.valueOf(salaryMonth), "yyyyMM");
        MonthRange monthRange = MonthRange.newInstance(runDate.getTime());
        Integer runDay = Integer.valueOf(DateUtils.dateToString(monthRange.getEndDate(), "yyyyMMdd"));
//        PerformanceData data = performanceService.loadUserData(user.getId(), runDay);
        AgentUserKpiResultSpring2016 data = agentUserKpiResultSpring2016Persistence.findUserIncome(salaryMonth, user.getId())
                .stream()
//                .filter(t -> t.getSchoolLevel() != null && SchoolLevel.MIDDLE.getLevel() == t.getSchoolLevel())
                .filter(t -> t.getCpaType() != null && SalaryKpiType.MIDDLE_CLUE_SUPPORT.getDesc().equals(t.getCpaType())) // 中学市场支持
                .findFirst().orElse(null);

        // 基数所在档位
        List<Long> schoolList = baseOrgService.getManagedMiddleSchoolList(user.getId());
        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolsExtInfoAsMap(schoolList)
                .getUninterruptibly();
        int schoolSum = schoolExtInfoMap.values().stream().mapToInt(t -> SafeConverter.toInt(t.getSchoolSize())).sum();
        String scale = getScaleString(SchoolLevel.MIDDLE, schoolSum);
        if (scale != null) {
            HSSFRow row = sheet.getRow(10);
            HSSFCell cell = row.createCell(scaleIndex);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(scale);
        }

        // 预算
        Long budget = data == null ? 0 : data.getCpaTarget();
        HSSFRow row1 = sheet.getRow(10);
        HSSFCell cell1 = row1.createCell(targetIndex);
        cell1.setCellStyle(cellStyle);
        cell1.setCellValue(budget);

        // 实际完成
        Long target = data == null ? 0 : data.getCpaResult();
        HSSFRow row2 = sheet.getRow(10);
        HSSFCell cell2 = row2.createCell(resultIndex);
        cell2.setCellStyle(cellStyle);
        cell2.setCellValue(target);

        // 完成比例
        double ratio = data == null ? 0 : budget == 0 ? 0 : MathUtils.doubleDivide(target, budget, 4, BigDecimal.ROUND_HALF_UP);
        DecimalFormat df0 = new DecimalFormat("##.##%");
        HSSFRow row3 = sheet.getRow(11);
        HSSFCell cell3 = row3.createCell(ratioIndex);
        cell3.setCellStyle(cellStyle);
        cell3.setCellValue(df0.format(ratio));


        // 获取绩效配置信息
        AgentPerformanceConfig config = agentPerformanceConfigService.findByUserIdAndMonth(user.getId(), salaryMonth);

        // 有效线索
        Integer clue = config == null ? 0 : SafeConverter.toInt(config.getMiddleTheMothClue());
        HSSFRow row4 = sheet.getRow(11);
        HSSFCell cell4 = row4.createCell(clueIndex);
        cell4.setCellStyle(cellStyle);
        cell4.setCellValue(clue);

        // 市级专场会议
        Integer cityMeeting = config == null ? 0 : SafeConverter.toInt(config.getCityMiddleMeet());
        HSSFRow row5 = sheet.getRow(11);
        HSSFCell cell5 = row5.createCell(cityIndex);
        cell5.setCellStyle(cellStyle);
        cell5.setCellValue(cityMeeting);

        // 区级专场会议
        Integer countyMeeting = config == null ? 0 : SafeConverter.toInt(config.getCountyMiddleMeet());
        HSSFRow row6 = sheet.getRow(12);
        HSSFCell cell6 = row6.createCell(countyIndex);
        cell6.setCellStyle(cellStyle);
        cell6.setCellValue(countyMeeting);

        // 插播有效会议
        Integer cutMeeting = config == null ? 0 : SafeConverter.toInt(config.getInterCutMiddleMeet());
        HSSFRow row7 = sheet.getRow(12);
        HSSFCell cell7 = row7.createCell(cutIndex);
        cell7.setCellStyle(cellStyle);
        cell7.setCellValue(cutMeeting);

        UserIncomeS2016Bean userIncome = accountService.getUserIncomeByType(salaryMonth, user.getId(), MiddleAgentType);
        // 结算开始日期
        Date startTime = userIncome == null ? monthRange.getStartDate() : userIncome.getStarTime();
        HSSFRow row8 = sheet.getRow(13);
        HSSFCell cell8 = row8.createCell(startIndex);
        cell8.setCellStyle(cellStyle);
        cell8.setCellValue(DateUtils.dateToString(startTime, "yyyy年MM月dd日"));

        // 结算结束日期
        Date endTime = userIncome == null ? monthRange.getEndDate() : userIncome.getEndTime();
        HSSFRow row9 = sheet.getRow(13);
        HSSFCell cell9 = row9.createCell(endIndex);
        cell9.setCellStyle(cellStyle);
        cell9.setCellValue(DateUtils.dateToString(endTime, "yyyy年MM月dd日"));

        // 金额
        Long amount = userIncome == null ? 0 : userIncome.getUserIncome();
        String capValue = change(amount);
        String amountFormat = "{0}，（￥{1}元）。";
        DecimalFormat df = new DecimalFormat("###,##0.00");
        String amountValue = MessageFormat.format(amountFormat, capValue, df.format(amount));
        HSSFRow row10 = sheet.getRow(15);
        HSSFCell cell10 = row10.createCell(amountIndex);
        cell10.setCellStyle(cellStyle);
        cell10.setCellValue(amountValue);

        // 合同乙方
        if (StringUtils.isNotEmpty(user.getRealName())) {
            HSSFRow row = sheet.getRow(25);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getRealName());
        }

        // 开户行
        if (StringUtils.isNotEmpty(user.getBankName())) {
            HSSFRow row = sheet.getRow(26);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getBankName());
        }

        // 开户行账号
        if (StringUtils.isNotEmpty(user.getBankAccount())) {
            HSSFRow row = sheet.getRow(27);
            HSSFCell cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(user.getBankAccount());
        }

        return hssfWorkbook;
    }

    public static String change(long v) {
        if (v < 0 || v > MAX_VALUE)
            return "参数非法!";
        long l = Math.round(v * 100);
        if (l == 0)
            return "零元整";
        String strValue = l + "";
        // i用来控制数
        int i = 0;
        // j用来控制单位
        int j = UNIT.length() - strValue.length();
        String rs = "";
        boolean isZero = false;
        for (; i < strValue.length(); i++, j++) {
            char ch = strValue.charAt(i);

            if (ch == '0') {
                isZero = true;
                if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' || UNIT.charAt(j) == '元') {
                    rs = rs + UNIT.charAt(j);
                    isZero = false;
                }
            } else {
                if (isZero) {
                    rs = rs + "零";
                    isZero = false;
                }
                rs = rs + DIGIT.charAt(ch - '0') + UNIT.charAt(j);
            }
        }

        if (!rs.endsWith("分")) {
            rs = rs + "整";
        }
        rs = rs.replaceAll("亿万", "亿");
        return rs;
    }


    @RequestMapping(value = "myorder/cancel.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage orderCancel(Model model) {
        Long userId = getCurrentUserId();
        Long orderId = getRequestLong("orderId");
        if (orderId == 0) {
            return MapMessage.errorMessage("订单ID错误");
        }
        // 修改订单状态
        AgentOrder agentOrder = agentOrderLoaderClient.getOrderById(orderId);
        if (agentOrder.getOrderStatus() != AgentOrderStatus.PENDING_FINANCIAL.getStatus() && agentOrder.getOrderStatus() != AgentOrderStatus.INIT.getStatus() && agentOrder.getOrderStatus() != AgentOrderStatus.UNCHECKED.getStatus() && agentOrder.getOrderStatus() != AgentOrderStatus.APPROVED.getStatus()) {
            return MapMessage.errorMessage("该订单不能取消!");
        }
        Integer preStatus = agentOrder.getOrderStatus();
        agentOrder.setOrderStatus(AgentOrderStatus.CANCELED.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        orderResetProcess(agentOrder, preStatus, userId, 2);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "myorder/edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reEditOrder() {
        Long userId = getCurrentUserId();
        Long orderId = getRequestLong("orderId");
        if (orderId == 0) {
            return MapMessage.errorMessage("订单ID错误");
        }
        AgentOrder agentOrder = agentOrderLoaderClient.getOrderById(orderId);
        if (agentOrder.getOrderStatus() != AgentOrderStatus.PENDING_FINANCIAL.getStatus() && agentOrder.getOrderStatus() != AgentOrderStatus.INIT.getStatus() && agentOrder.getOrderStatus() != AgentOrderStatus.UNCHECKED.getStatus() && agentOrder.getOrderStatus() != AgentOrderStatus.APPROVED.getStatus()) {
            return MapMessage.errorMessage("该订单不能再次编辑");
        }
        // 删除现有的 draft 订单
        AgentOrder draftOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
        if (draftOrder != null) {
            baseOrderService.removeOrderById(draftOrder.getId());
        }

        Integer preStatus = agentOrder.getOrderStatus();
        // 将订单修改为 draft 状态
        agentOrder.setOrderStatus(AgentOrderStatus.DRAFT.getStatus());
        agentOrderServiceClient.saveOrder(agentOrder);

        orderResetProcess(agentOrder, preStatus, userId, 1);
        return MapMessage.successMessage();
    }

    // cancelOrReEditFlag : 取消订单或者修改订单标记  1：修改订单  2 取消订单
    private void orderResetProcess(AgentOrder agentOrder, Integer preStatus, Long operator, Integer cancelOrReEditFlag) {
        if (agentOrder == null) {
            return;
        }
        // 更新库存
        List<AgentOrderProduct> orderProductList = agentOrder.getOrderProductList();
        if (CollectionUtils.isNotEmpty(orderProductList)) {

            List<AgentGroup> groupList = baseOrgService.getUserGroups(agentOrder.getCreator());
            String groupName = "";
            if (CollectionUtils.isNotEmpty(groupList)) {
                groupName = groupList.get(0).getGroupName();
            }
            AgentUser user = baseOrgService.getUser(agentOrder.getCreator());
            String quantityChangeDesc = (cancelOrReEditFlag == 1 ? "【修改订单】" : "【取消订单】") + "订单号：" + agentOrder.getId() + "(" + (StringUtils.isBlank(groupName) ? "" : groupName + " - ") + (user == null ? "" : user.getRealName()) + ")";

            orderProductList.stream().forEach(p -> {
                AgentProduct product = productConfigService.getById(p.getProductId());
                Integer preQuantity = product.getInventoryQuantity() == null ? 0 : product.getInventoryQuantity();
                product.setInventoryQuantity(product.getInventoryQuantity() + p.getProductQuantity());
                productConfigService.updateProduct(product);

                // 添加库存变更记录
                productConfigService.addAgentProductInventoryRecord(operator, product.getId(), preQuantity, product.getInventoryQuantity(), p.getProductQuantity(), quantityChangeDesc);
            });
        }

        // 更新用户账户可用余额
        Long creator = agentOrder.getCreator();
        if (creator != null && creator != 0) {
            AgentUser agentUser = baseUserService.getById(agentOrder.getCreator());
            if (agentUser != null) {
                if (preStatus != null && preStatus == AgentOrderStatus.APPROVED.getStatus()) {
                    agentUser.setCashAmount(MathUtils.floatAdd(agentUser.getCashAmount(), agentOrder.getOrderAmount()));
                }
                agentUser.setUsableCashAmount(MathUtils.floatAdd(agentUser.getUsableCashAmount(), agentOrder.getOrderAmount()));
                baseUserService.updateAgentUser(agentUser);
            }
        }

        // 删除处理流程
        baseOrderService.deleteProcessByOrderId(agentOrder.getId());
    }

    private String getScaleString(SchoolLevel schoolLevel, Integer schoolSize) {
        if (SchoolLevel.JUNIOR == schoolLevel) {
            if (schoolSize < 50001) {
                return "0-50000";
            } else if (schoolSize > 50000 && schoolSize < 100001) {
                return "50001-100000";
            } else if (schoolSize > 100000 && schoolSize < 150001) {
                return "100001-150000";
            } else if (schoolSize > 150000 && schoolSize < 200001) {
                return "150001-200000";
            } else if (schoolSize > 200000 && schoolSize < 250001) {
                return "200001-250000";
            } else {
                return "250001以上";
            }
        } else if (SchoolLevel.MIDDLE == schoolLevel) {
            if (schoolSize < 30001) {
                return "0-30000";
            } else if (schoolSize > 30000 && schoolSize < 60001) {
                return "30001-60000";
            } else if (schoolSize > 60000 && schoolSize < 90001) {
                return "60001-90000";
            } else {
                return "90001以上";
            }
        }
        return null;
    }

    /*public static void main(String[] args) {
        double s = MathUtils.doubleDivide(2, 3, 4, BigDecimal.ROUND_HALF_UP);
        System.out.println(new DecimalFormat(".##%").format(s));
    }*/
}
