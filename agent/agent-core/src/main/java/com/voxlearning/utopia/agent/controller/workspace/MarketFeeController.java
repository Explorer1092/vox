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

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.bean.incomes2016.GreatRegionIncomeS2016Bean;
import com.voxlearning.utopia.agent.bean.incomes2016.PartRegionIncomeS2016Bean;
import com.voxlearning.utopia.agent.bean.incomes2016.UserIncomeS2016Bean;
import com.voxlearning.utopia.agent.bean.incomes2016.UserRegionIncomeS2016Bean;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.workspace.MarketFeeService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 市场费用
 * Created by Shuai.Huan on 2014/8/13.
 */
@Controller
@RequestMapping("/workspace/marketfee")
@Slf4j
public class MarketFeeController extends AbstractAgentController {

    @Inject private MarketFeeService marketFeeService;

    private static final List<String> keys = Arrays.asList(
            "201506", "201509", "201510", "201511", "201512",
//            "201603", "201604", "201605", "201606", "201609",
            "manual", "overau", "overds"
    );

    private static int GROUP_INDEX = 0;
    private static int PROVINCE_INDEX = 1;
    private static int USER_NAME_INDEX = 2;
    private static int USER_ROLE_INDEX = 3;
    private static int REGION_INDEX = 4;
    private static int START_DATE_INDEX = 5;
    private static int END_DATE_INDEX = 6;
    private static int TYPE_INDEX = 7;
    private static int SALARY_INDEX = 8;
    private static int NOTE_INDEX = 9;
    private static int TOTAL_SIZE = 9;

    @RequestMapping(value = "history/index.vpage", method = RequestMethod.GET)
    public String marketFeeHistory() {
        String type = getRequestString("type");
        if (keys.contains(type)) {
            return "workspace/kpihistory/" + type;
        }
        return "workspace/kpihistory/marketfeequery";
    }

    @RequestMapping(value = "history/query.vpage", method = RequestMethod.GET)
    public String marketFeeQueryHistory() {
        return "workspace/kpihistory/marketfeequery";
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String marketFee(Model model) {
        Integer salaryMonth = getRequestInt("month");
        if (salaryMonth == 0) {
            // 每个月25日以后看本月，否则看前一个月
            Date curTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(curTime);
            if (calendar.get(Calendar.DAY_OF_MONTH) >= 25) {
                salaryMonth = Integer.parseInt(DateUtils.dateToString(curTime, "yyyyMM"));
            } else {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
                salaryMonth = Integer.parseInt(DateUtils.dateToString(calendar.getTime(), "yyyyMM"));
            }
        }
        model.addAttribute("month", salaryMonth);
        model.addAttribute("marketFeeData", marketFeeService.getMarketFee(salaryMonth));

        return "workspace/kpi/marketfee";
    }

    @RequestMapping(value = "marketfeeviewer.vpage", method = RequestMethod.GET)
    String marketFeeViewer(Model model) {
        Integer salaryMonth = getRequestInt("month");
        if (salaryMonth == 0) {
            // 每个月25日以后看本月，否则看前一个月
            Date curTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(curTime);
            if (calendar.get(Calendar.DAY_OF_MONTH) >= 25) {
                salaryMonth = Integer.parseInt(DateUtils.dateToString(curTime, "yyyyMM"));
            } else {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
                salaryMonth = Integer.parseInt(DateUtils.dateToString(calendar.getTime(), "yyyyMM"));
            }
        }
        model.addAttribute("month", salaryMonth);
        model.addAttribute("marketFeeData", marketFeeService.getMarketFee(salaryMonth));

        return "workspace/kpi/marketfeeviewer";
    }

//    @RequestMapping(value = "query.vpage", method = RequestMethod.GET)
//    String marketFeeQuery(Model model) {
//        List<Integer> mouthList = userKpiService.getAllKpiEvalDatesSpring2016(SPRINGSTARTTIME, SPRINGENDTIME);
//        List<marketfeeData> marketFeeDatas = null;
//        if (CollectionUtils.isNotEmpty(mouthList)) {
//            marketFeeDatas = new ArrayList<>();
//            for (Integer mouth : mouthList) {
//                marketFeeDatas.add(new marketfeeData(mouth, strFormatMouthTime(mouth)));
//            }
//        }
//        model.addAttribute("marketFeeDatas", marketFeeDatas);
//        return "workspace/kpi/marketfeequery";
//    }

    private String strFormatMouthTime(Integer mouthTime) {
        return mouthTime / 100 + "年" + mouthTime % 100 + "月";
    }

    @RequestMapping(value = "financeconfirm.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage financeConfirm(Integer month, Long user) {

        MapMessage mapMessage = new MapMessage();
        try {
            marketFeeService.financeConfirm(month, user);

            asyncLogService.logMarketFeeConfirmed(getCurrentUser(), getRequest().getRequestURI(), "Finance  Confirmed",
                    "month：" + month + ", user:" + user);

            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            log.error("财务总监审核通过失败! month:{}, user:{}", month, user, ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("财务总监审核通过失败!");
        }
        return mapMessage;
    }

    @RequestMapping(value = "marketconfirm.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage marketConfirm(Integer month, Long user) {

        MapMessage mapMessage = new MapMessage();
        try {
            marketFeeService.marketConfirm(month, user);

            asyncLogService.logMarketFeeConfirmed(getCurrentUser(), getRequest().getRequestURI(), "Market  Confirmed",
                    "month：" + month + ", user:" + user);

            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            log.error("市场总监审核通过失败! month:{}, user:{}", month, user, ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("市场总监审核通过失败!");
        }
        return mapMessage;
    }

    @RequestMapping(value = "downloadmarketfee.vpage", method = RequestMethod.POST)
    void downloadMarketFee(HttpServletResponse response) {
        try {
            Integer salaryMonth = getRequestInt("month");
            if (salaryMonth == 0) {
                // 每个月25日以后看本月，否则看前一个月
                Date curTime = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(curTime);
                if (calendar.get(Calendar.DAY_OF_MONTH) >= 25) {
                    salaryMonth = Integer.parseInt(DateUtils.dateToString(curTime, "yyyyMM"));
                } else {
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
                    salaryMonth = Integer.parseInt(DateUtils.dateToString(calendar.getTime(), "yyyyMM"));
                }
            }

            Map<String, GreatRegionIncomeS2016Bean> feeData = marketFeeService.getMarketFee(salaryMonth);
            HSSFWorkbook hssfWorkbook = convertToHSSfWorkbook(feeData);
            String filename = "一起作业网市场后台系统-市场支持费用-" + salaryMonth + ".xls";
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

    @RequestMapping(value = "partner.vpage", method = RequestMethod.POST)
    void downloadPartner(HttpServletResponse response) {
        try {
            Integer salaryMonth = getRequestInt("month");
            
            List<List<String>> data = marketFeeService.getPartnerData(salaryMonth);
            HSSFWorkbook hssfWorkbook = convertToHSSfWorkbook(data);
            String filename = "合作伙伴数据导出-" + salaryMonth + ".xls";
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

    private HSSFWorkbook convertToHSSfWorkbook(Map<String, GreatRegionIncomeS2016Bean> feeData) {

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFCellStyle stringStyle = (HSSFCellStyle) hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFCellStyle numberStyle = (HSSFCellStyle) hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        numberStyle.setBorderBottom(CellStyle.BORDER_THIN);
        numberStyle.setBorderTop(CellStyle.BORDER_THIN);
        numberStyle.setBorderLeft(CellStyle.BORDER_THIN);
        numberStyle.setBorderRight(CellStyle.BORDER_THIN);
        numberStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));

        HSSFCellStyle dateStyle = (HSSFCellStyle) hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        dateStyle.setBorderBottom(CellStyle.BORDER_THIN);
        dateStyle.setBorderTop(CellStyle.BORDER_THIN);
        dateStyle.setBorderLeft(CellStyle.BORDER_THIN);
        dateStyle.setBorderRight(CellStyle.BORDER_THIN);
        dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("yyyy-MM-dd"));

        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        hssfSheet.setColumnWidth(GROUP_INDEX, 4000);
        hssfSheet.setColumnWidth(PROVINCE_INDEX, 4000);
        hssfSheet.setColumnWidth(USER_NAME_INDEX, 4000);
        hssfSheet.setColumnWidth(USER_ROLE_INDEX, 4000);
        hssfSheet.setColumnWidth(REGION_INDEX, 4000);
        hssfSheet.setColumnWidth(START_DATE_INDEX, 5000);
        hssfSheet.setColumnWidth(END_DATE_INDEX, 5000);
        hssfSheet.setColumnWidth(TYPE_INDEX, 7000);
        hssfSheet.setColumnWidth(SALARY_INDEX, 3000);
        hssfSheet.setColumnWidth(NOTE_INDEX, 14000);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, TOTAL_SIZE, stringStyle);
        HssfUtils.setCellValue(firstRow, GROUP_INDEX, stringStyle, "大区");
        HssfUtils.setCellValue(firstRow, PROVINCE_INDEX, stringStyle, "区域");
        HssfUtils.setCellValue(firstRow, USER_NAME_INDEX, stringStyle, "用户名");
        HssfUtils.setCellValue(firstRow, USER_ROLE_INDEX, stringStyle, "用户角色");
        HssfUtils.setCellValue(firstRow, REGION_INDEX, stringStyle, "城市");
        HssfUtils.setCellValue(firstRow, START_DATE_INDEX, stringStyle, "开始日期");
        HssfUtils.setCellValue(firstRow, END_DATE_INDEX, stringStyle, "结束日期");
        HssfUtils.setCellValue(firstRow, TYPE_INDEX, stringStyle, "费用内容");
        HssfUtils.setCellValue(firstRow, SALARY_INDEX, stringStyle, "金额");
        HssfUtils.setCellValue(firstRow, NOTE_INDEX, stringStyle, "明细");

        int greatRegionMergeIndex = 1;
        int rowNum = 1;
        for (String greatRegionName : feeData.keySet()) {
            // 大区
            GreatRegionIncomeS2016Bean greatRegionData = feeData.get(greatRegionName);

            Row row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);

            CellRangeAddress greatRegionRange = new CellRangeAddress(greatRegionMergeIndex, greatRegionMergeIndex + greatRegionData.getDataSize() - 1, GROUP_INDEX, GROUP_INDEX);
            hssfSheet.addMergedRegion(greatRegionRange);
            HssfUtils.setCellValue(row, GROUP_INDEX, stringStyle, greatRegionData.getGroupName());

            // 区域
            Map<String, PartRegionIncomeS2016Bean> partRegionFeeData = greatRegionData.getPartRegionIncomeData();
            int partRegionMergeIndex = greatRegionMergeIndex;
            for (String partRegionName : partRegionFeeData.keySet()) {
                PartRegionIncomeS2016Bean partRegionData = partRegionFeeData.get(partRegionName);
                if (partRegionMergeIndex > greatRegionMergeIndex) {
                    row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);
                }

                CellRangeAddress userRange = new CellRangeAddress(partRegionMergeIndex, partRegionMergeIndex + partRegionData.getDataSize() - 1, PROVINCE_INDEX, PROVINCE_INDEX);
                hssfSheet.addMergedRegion(userRange);

                HssfUtils.setCellValue(row, PROVINCE_INDEX, stringStyle, partRegionData.getGroupName());

                // 用户
                Map<String, UserIncomeS2016Bean> userFeeData = partRegionData.getUserIncomeData();
                int userMergeIndex = partRegionMergeIndex;
                for (String userId : userFeeData.keySet()) {
                    UserIncomeS2016Bean userData = userFeeData.get(userId);

                    if (userMergeIndex > partRegionMergeIndex) {
                        row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);
                    }

                    CellRangeAddress durationRange = new CellRangeAddress(userMergeIndex, userMergeIndex + userData.getDataSize() - 1, USER_NAME_INDEX, USER_NAME_INDEX);
                    hssfSheet.addMergedRegion(durationRange);

                    HssfUtils.setCellValue(row, USER_NAME_INDEX, stringStyle, userData.getUserName());

                    // 用户角色
                    CellRangeAddress roleRange = new CellRangeAddress(userMergeIndex, userMergeIndex + userData.getDataSize() - 1, USER_ROLE_INDEX, USER_ROLE_INDEX);
                    hssfSheet.addMergedRegion(roleRange);

                    AgentRoleType roleType = baseOrgService.getUserRole(userData.getUserId());
                    HssfUtils.setCellValue(row, USER_ROLE_INDEX, stringStyle, roleType == null ? "-" : roleType.getRoleName());

                    // 城市
                    Map<String, UserRegionIncomeS2016Bean> userRegionFeeData = userData.getUserRegionIncomeData();
                    int userRegionMergeIndex = userMergeIndex;
                    for (String userRegionName : userRegionFeeData.keySet()) {
                        UserRegionIncomeS2016Bean userRegionData = userRegionFeeData.get(userRegionName);
                        if (userRegionMergeIndex > userMergeIndex) {
                            row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);
                        }

                        CellRangeAddress regionRange = new CellRangeAddress(userRegionMergeIndex, userRegionMergeIndex + userRegionData.getDataSize() - 1, REGION_INDEX, REGION_INDEX);
                        hssfSheet.addMergedRegion(regionRange);

                        HssfUtils.setCellValue(row, REGION_INDEX, stringStyle, userRegionData.getRegionName());

                        for (int i = 0; i < userRegionData.getIncomeList().size(); i++) {
                            if (i > 0) {
                                row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);
                            }

                            HssfUtils.setCellValue(row, START_DATE_INDEX, dateStyle, userRegionData.getIncomeList().get(i).getStartTime());
                            HssfUtils.setCellValue(row, END_DATE_INDEX, dateStyle, userRegionData.getIncomeList().get(i).getEndTime());
                            HssfUtils.setCellValue(row, TYPE_INDEX, stringStyle, userRegionData.getIncomeList().get(i).getSource());
                            HssfUtils.setCellValue(row, SALARY_INDEX, numberStyle, userRegionData.getIncomeList().get(i).getIncome());
                            HssfUtils.setCellValue(row, NOTE_INDEX, numberStyle, userRegionData.getIncomeList().get(i).getExtInfo());
                        }

                        userRegionMergeIndex += userRegionData.getDataSize();
                    }

                    userMergeIndex += userData.getDataSize();

                    // 用户小计
                    row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);
                    CellRangeAddress userNameRange = new CellRangeAddress(userMergeIndex - 1, userMergeIndex - 1, REGION_INDEX, TYPE_INDEX);
                    hssfSheet.addMergedRegion(userNameRange);
                    CellRangeAddress userSumRange = new CellRangeAddress(userMergeIndex - 1, userMergeIndex - 1, SALARY_INDEX, NOTE_INDEX);
                    hssfSheet.addMergedRegion(userSumRange);
                    HssfUtils.setCellValue(row, REGION_INDEX, stringStyle, userData.getUserName() + "费用小计");
                    HssfUtils.setCellValue(row, SALARY_INDEX, numberStyle, userData.getUserIncome());
                }

                partRegionMergeIndex = partRegionMergeIndex + partRegionData.getDataSize();

                // 区域小计
                row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);
                CellRangeAddress partRegionNameRange = new CellRangeAddress(partRegionMergeIndex - 1, partRegionMergeIndex - 1, USER_NAME_INDEX, TYPE_INDEX);
                hssfSheet.addMergedRegion(partRegionNameRange);
                CellRangeAddress partRegionSumRange = new CellRangeAddress(partRegionMergeIndex - 1, partRegionMergeIndex - 1, SALARY_INDEX, NOTE_INDEX);
                hssfSheet.addMergedRegion(partRegionSumRange);
                HssfUtils.setCellValue(row, USER_NAME_INDEX, stringStyle, partRegionData.getGroupName() + "费用小计");
                HssfUtils.setCellValue(row, SALARY_INDEX, numberStyle, partRegionData.getTotalIncome());

            }

            greatRegionMergeIndex = greatRegionMergeIndex + greatRegionData.getDataSize();

            // 大区合计
            row = HssfUtils.createRow(hssfSheet, rowNum++, TOTAL_SIZE, stringStyle);
            CellRangeAddress greatRegionNameRange = new CellRangeAddress(greatRegionMergeIndex - 1, greatRegionMergeIndex - 1, PROVINCE_INDEX, TYPE_INDEX);
            hssfSheet.addMergedRegion(greatRegionNameRange);
            CellRangeAddress greatRegionSumRange = new CellRangeAddress(greatRegionMergeIndex - 1, greatRegionMergeIndex - 1, SALARY_INDEX, NOTE_INDEX);
            hssfSheet.addMergedRegion(greatRegionSumRange);
            HssfUtils.setCellValue(row, PROVINCE_INDEX, stringStyle, greatRegionData.getGroupName() + "费用小计");
            HssfUtils.setCellValue(row, SALARY_INDEX, numberStyle, greatRegionData.getTotalIncome());
        }

        return hssfWorkbook;
    }

    private HSSFWorkbook convertToHSSfWorkbook(List<List<String>> data) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFCellStyle stringStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        hssfSheet.setColumnWidth(0, 14000);
        hssfSheet.setColumnWidth(1, 4000);
        hssfSheet.setColumnWidth(2, 4000);
        hssfSheet.setColumnWidth(3, 4000);
        hssfSheet.setColumnWidth(4, 5000);
        hssfSheet.setColumnWidth(5, 5000);
        hssfSheet.setColumnWidth(6, 4000);
        hssfSheet.setColumnWidth(7, 4000);
        hssfSheet.setColumnWidth(8, 4000);
        hssfSheet.setColumnWidth(9, 4000);
        hssfSheet.setColumnWidth(10, 4000);
        hssfSheet.setColumnWidth(11, 4000);
        hssfSheet.setColumnWidth(12, 4000);
        hssfSheet.setColumnWidth(13, 4000);

        Row firstRow = HssfUtils.createRow(hssfSheet, 0, 13, stringStyle);
        HssfUtils.setCellValue(firstRow, 0, stringStyle, "合作伙伴名称");
        HssfUtils.setCellValue(firstRow, 1, stringStyle, "月份");
        HssfUtils.setCellValue(firstRow, 2, stringStyle, "中/小学");
        HssfUtils.setCellValue(firstRow, 3, stringStyle, "城市");
        HssfUtils.setCellValue(firstRow, 4, stringStyle, "城市级别");
        HssfUtils.setCellValue(firstRow, 5, stringStyle, "大区");
        HssfUtils.setCellValue(firstRow, 6, stringStyle, "部门");
        HssfUtils.setCellValue(firstRow, 7, stringStyle, "学生基数");
        HssfUtils.setCellValue(firstRow, 8, stringStyle, "基数对应金额");
        HssfUtils.setCellValue(firstRow, 9, stringStyle, "完成率");
        HssfUtils.setCellValue(firstRow, 10, stringStyle, "本月线索数量");
        HssfUtils.setCellValue(firstRow, 11, stringStyle, "线索费用");
        HssfUtils.setCellValue(firstRow, 12, stringStyle, "组会费用");
        HssfUtils.setCellValue(firstRow, 13, stringStyle, "本月费用合计");

        int rowNum = 1;
        for (List<String> temp : data) {
            Row row = HssfUtils.createRow(hssfSheet, rowNum++, 13, stringStyle);
            for (int i = 0; i < temp.size(); ++i) {
                HssfUtils.setCellValue(row, i, stringStyle, temp.get(i));
            }
        }
        return hssfWorkbook;
    }

}
