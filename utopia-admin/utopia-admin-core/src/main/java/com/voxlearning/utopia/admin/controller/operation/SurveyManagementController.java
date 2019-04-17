/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.operation;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.entity.questionsurvey.QuestionSurveyResult;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 问卷型导出
 * Created by yaguang.wang on 2016/4/22.
 */
@Controller
@RequestMapping("/opmanager/survey")
public class SurveyManagementController extends AbstractAdminSystemController {
    @Inject MiscServiceClient miscServiceClient;

    @RequestMapping(value = "surveyinfo.vpage", method = RequestMethod.GET)
    public String surveyIndex(Model model) {
        return "opmanager/survey/survey_info";
    }

    @RequestMapping(value = "downloadactivityinfo.vpage", method = RequestMethod.GET)
    public void surveyInfo(HttpServletResponse response) {
        String activityId = getRequestString("activityId");
        if (StringUtils.isBlank(activityId)) {
            return;
        }

        List<QuestionSurveyResult> exportList = miscServiceClient.loadQuestionSurveyResult(activityId);
        XSSFWorkbook workbook = convertToSurveyXSSF(activityId, exportList);

        String filename = "问卷调查详情查询" + "-" + DateUtils.dateToString(new Date()) + ".xlsx";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download auditing order exception!");
            }
        }
    }

    private XSSFWorkbook convertToSurveyXSSF(String activityId, List<QuestionSurveyResult> exportList) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("问卷" + activityId);
        XSSFCellStyle stringStyle = workbook.createCellStyle();
        // 设置单元格边框样式
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);
        Integer userColNum = 1;                                //用户姓名占了几列
        if (CollectionUtils.isNotEmpty(exportList)) {
            HashSet<String> questionNames = new HashSet<>();                            //用于存放所有的列明 问题的标题
            exportList.forEach(p -> p.getQuestionAnswerMap().keySet().forEach(questionNames::add)); //将所有问题的标题存放在rowNames 中
            XSSFRow firstRow = sheet.createRow(0);                                      //设置第一行
            firstRow.setHeightInPoints(20);
            XSSFCell firstRowFirstCol = firstRow.createCell(0);
            firstRowFirstCol.setCellValue("用户ID");
            firstRowFirstCol.setCellStyle(stringStyle);

            List<String> nameList = questionNames.stream().collect(Collectors.toList()); //转换成问题的标题的List
            for (int i = 0; i < nameList.size(); i++) {
                XSSFCell firstRowNCol = firstRow.createCell(i + userColNum);//取第二列的数据开始设置题目的标题
                firstRowNCol.setCellValue(nameList.get(i));          //取每一个问题的名字放在相应的列上
                firstRowNCol.setCellStyle(stringStyle);
            }

            int rowNum = 1;                                                         //记录行数初始化为1
            for (QuestionSurveyResult export : exportList) {                         //为各列添加数据
                Map<String, String> questionAnswerMap = export.getQuestionAnswerMap();// 获取每一列中每个题目对应的答案的map
                XSSFRow xssfRow = sheet.createRow(rowNum++);                         //设置行数
                xssfRow.setHeightInPoints(20);
                XSSFCell nRowFirstCol = xssfRow.createCell(0);
                nRowFirstCol.setCellValue(export.getUserId() + "");              // 设置每行的第一列未useId转为文本可以左对齐
                nRowFirstCol.setCellStyle(stringStyle);
                for (int i = 0; i < nameList.size(); i++) {                          // 第二列开始每列添加对应的答案如果答案为空那么显示为空白
                    XSSFCell nRowNCol = xssfRow.createCell(i + userColNum);
                    nRowNCol.setCellValue(ConversionUtils.toString(questionAnswerMap.get(nameList.get(i))));
                    nRowNCol.setCellStyle(stringStyle);
                }
            }
        }
        return workbook;
    }

}
