package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceStatistics;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.workspace.PerformanceStatisticsService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * PerformanceStatisticsController
 *
 * @author song.wang
 * @date 2017/3/27
 */
@Controller
@RequestMapping("/workspace/performance")
@Slf4j
public class PerformanceStatisticsController extends AbstractAgentController {

    @Inject
    private PerformanceStatisticsService performanceStatisticsService;
    @Inject
    PerformanceService performanceService;

    @RequestMapping(value = "my_data.vpage")
    public String loadMyPerformance(Model model){
        Long userId = getCurrentUserId();
        List<AgentPerformanceStatistics> performanceStatisticsList = performanceStatisticsService.loadUserPerformanceStatistics(userId);
        model.addAttribute("dataList", performanceStatisticsList);
        return "workspace/performance/my_data";
    }


    @RequestMapping(value = "manage_users_data.vpage")
    public String loadManagerUserData(Model model){
        Long userId = getCurrentUserId();
        Integer month = requestInteger("month");
        if(month == null){ // 没有指定月份的话取当前月
            Integer day = performanceService.lastSuccessDataDay();
            month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));
        }

        List<AgentPerformanceStatistics> performanceStatisticsList = performanceStatisticsService.loadManagedUserDataByMonth(userId, month);

        model.addAttribute("dataList", performanceStatisticsList);
        model.addAttribute("month", month);
        return "workspace/performance/manage_users_data";
    }


    @RequestMapping(value = "manage_groups_data.vpage")
    public String loadManagerGroupData(Model model){
        Long userId = getCurrentUserId();
        Integer month = requestInteger("month");
        if(month == null){ // 没有指定月份的话取当前月
            Integer day = performanceService.lastSuccessDataDay();
            month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));
        }

        List<AgentPerformanceStatistics> performanceStatisticsList = performanceStatisticsService.loadManagedGroupDataListByMonth(userId, month);

        model.addAttribute("dataList", performanceStatisticsList);
        model.addAttribute("month", month);
        return "workspace/performance/manage_groups_data";
    }


    @RequestMapping(value = "downloadperformance.vpage")
    void downloadWorkRecord(HttpServletResponse response) {
        try {
            Long userId = getCurrentUserId();
            Integer type = getRequestInt("type");
            Integer month = requestInteger("month");
            if(month == null){ // 没有指定月份的话取当前月
                Integer day = performanceService.lastSuccessDataDay();
                month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));
            }

            List<AgentPerformanceStatistics> dataList = new ArrayList<>();
            if(type == 1){ // 我的业绩
                dataList = performanceStatisticsService.loadUserPerformanceStatistics(userId);
            }else if(type == 2){ // 下属的业绩
                dataList = performanceStatisticsService.loadManagedUserDataByMonth(userId, month);
            }else if(type == 3){ // 部门业绩
                dataList = performanceStatisticsService.loadManagedGroupDataListByMonth(userId, month);
            }

            HSSFWorkbook hssfWorkbook = convertToWorkbook(dataList, type);
            String filename = "业绩统计下载-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xls";
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

    private HSSFWorkbook convertToWorkbook(List<AgentPerformanceStatistics> dataList, Integer type){
        HSSFWorkbook workbook = new HSSFWorkbook();
        int column = 7;
        String sheetName = "业绩统计";
        if(type == 1){
            sheetName = "我的业绩";
            column = 7;
        }else if(type == 2){
            sheetName = "下属业绩";
            column = 8;
        }else if(type == 3){
            sheetName = "部门业绩";
            column = 7;
        }
        HSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(15);
        // 定义字体
        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        // 设置单元格格式（边框，字体）
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setFont(font);

        HSSFCellStyle titleRowStyle = workbook.createCellStyle();
        titleRowStyle.cloneStyleFrom(cellStyle);
        titleRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        titleRowStyle.setFillForegroundColor(HSSFColor.ROYAL_BLUE.index);

        Row firstRow = HssfUtils.createRow(sheet, 0, column, titleRowStyle);
        HssfUtils.setCellValue(firstRow, 0, titleRowStyle, "月份");
        HssfUtils.setCellValue(firstRow, 1, titleRowStyle, "大区");
        HssfUtils.setCellValue(firstRow, 2, titleRowStyle, "部门");

        if(type == 1){
            HssfUtils.setCellValue(firstRow, column - 4, titleRowStyle, "角色");
        }else if(type == 2){
            HssfUtils.setCellValue(firstRow, column - 5, titleRowStyle, "负责人");
            HssfUtils.setCellValue(firstRow, column - 4, titleRowStyle, "角色");
        }else if(type == 3){
            HssfUtils.setCellValue(firstRow, column - 4, titleRowStyle, "部门类别");
        }
        HssfUtils.setCellValue(firstRow, column - 3, titleRowStyle, "类别");
        HssfUtils.setCellValue(firstRow, column - 2, titleRowStyle, "预算");
        HssfUtils.setCellValue(firstRow, column - 1, titleRowStyle, "完成");
        HssfUtils.setCellValue(firstRow, column, titleRowStyle, "完成率");

        if(CollectionUtils.isNotEmpty(dataList)){
            int rowNo = 1;
            for(AgentPerformanceStatistics data : dataList){
                Row row = HssfUtils.createRow(sheet, rowNo++, column, cellStyle);
                HssfUtils.setCellValue(row, 0, cellStyle, data.getMonth());
                HssfUtils.setCellValue(row, 1, cellStyle, data.getRegionGroupName());
                HssfUtils.setCellValue(row, 2, cellStyle, data.getCityGroupName());
                if(type == 1){
                    HssfUtils.setCellValue(row, column - 4, cellStyle, data.getUserRoleType() == null ? "" : data.getUserRoleType().getRoleName());
                }else if(type == 2){
                    HssfUtils.setCellValue(row, column - 5, cellStyle, data.getUserName());
                    HssfUtils.setCellValue(row, column - 4, cellStyle, data.getUserRoleType() == null ? "" : data.getUserRoleType().getRoleName());
                }else if(type == 3){
                    HssfUtils.setCellValue(row, column - 4, cellStyle, data.getGroupRoleType() == null ? "" : data.getGroupRoleType().getRoleName());
                }
                HssfUtils.setCellValue(row, column - 3, cellStyle, data.getPerformanceKpiType() == null ? "" : data.getPerformanceKpiType().getDesc());
                HssfUtils.setCellValue(row, column - 2, cellStyle, data.getBudget());
                HssfUtils.setCellValue(row, column - 1, cellStyle, data.getComplete());
                HssfUtils.setCellValue(row, column, cellStyle, data.getCompleteRate());
            }
        }
        return workbook;
    }






}
