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

package com.voxlearning.washington.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionServiceProvider;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import lombok.Data;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class POIExcel {
    private static final Logger logger = LoggerFactory.getLogger(POIExcel.class);

    // 下载excel文件的版本
    public final static int EXCEL_VERSION_07 = 1;
    public final static int EXCEL_VERSION_03 = 2;

    @SuppressWarnings("deprecation")
    public static boolean downLoadExcelSheets(HttpServletRequest request,
                                              HttpServletResponse response, String fileName, String[] labelName, List<Object[]> title, List<List<Object[]>> context) {
//        return downLoadExcelSheets(request, response, fileName, null, labelName, title, context, null, null, null, false, null, null);
        List<TitleGenerator> titleGenerators = new ArrayList<>();
        List<ContentGenerator> contentGenerators = new ArrayList<>();

        for (int i = 0; i < labelName.length; i++) {
            titleGenerators.add(new DefaultTitleGenerator(null, title.get(i)));
            contentGenerators.add(new DefaultContentGenerator(context.get(i), true));
        }

        return downLoadExcelSheets(request,
                response,
                title != null && title.stream().anyMatch(objects -> objects != null && objects.length > 256) ? EXCEL_VERSION_07 : EXCEL_VERSION_03,
                fileName,
                labelName,
                titleGenerators,
                contentGenerators,
                null);
    }

    //TODO 应该不用了，不过还是先测一段时间吧，by changyuan.liu
    @Deprecated
    @SuppressWarnings("deprecation")
    public static boolean downLoadExcelSheets(HttpServletRequest request,
                                              HttpServletResponse response, String fileName, String headTitle, String[] labelName, List<Object[]> title, List<List<Object[]>> context,
                                              Short alignment, String fontName, Short fontSize, Boolean showBorder, Float rowHeight, Integer columnWidth) {
        try {
            // 文件名
            fileName += ".xls";
            fileName = UtopiaHttpRequestContext.attachmentFilenameEncoding(fileName, request);
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            // 转换编码-浏览器安全规则
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            HSSFWorkbook wb = new HSSFWorkbook();    //第一步

            HSSFCellStyle hssfCellStyle = wb.createCellStyle();
            HSSFCellStyle headTitleStyle = wb.createCellStyle();
            // 对齐方式
            if (alignment != null) {
                hssfCellStyle.setAlignment(alignment);
                hssfCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                headTitleStyle.setAlignment(alignment);
                headTitleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            }
            // 字体
            if (fontName != null) {
                HSSFFont font = wb.createFont();
                font.setFontName(fontName);
                font.setFontHeightInPoints(fontSize);
                hssfCellStyle.setFont(font);

                font = wb.createFont();
                font.setFontName(fontName);
                font.setFontHeightInPoints(fontSize);
                font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                headTitleStyle.setFont(font);
            }
            // 边框
            if (showBorder) {
                hssfCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
                hssfCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //左边框
                hssfCellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);    //上边框
                hssfCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  //右边框

                headTitleStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
                headTitleStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //左边框
                headTitleStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);    //上边框
                headTitleStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  //右边框
            }

            // 初始化标签
            for (int i = 0; i < labelName.length; i++) {

                HSSFSheet sheet = wb.createSheet(labelName[i]);      //第二步

                // 行高
                if (rowHeight != null) {
                    sheet.setDefaultRowHeightInPoints(rowHeight);
                }
                // 列宽
                if (columnWidth != null) {
                    sheet.setDefaultColumnWidth(columnWidth);
                }

                int titleIndex = 0;
                // 加入试卷标题
                HSSFRow row = sheet.createRow(titleIndex++);
                if (StringUtils.isNotBlank(headTitle)) {
                    for (int j = 0; j < title.get(0).length; j++) {
                        row.createCell(j).setCellStyle(headTitleStyle);
                    }
                    CellRangeAddress region = new CellRangeAddress(0, 0, (short) 0, (short) (title.get(0).length - 1));
                    sheet.addMergedRegion(region);
                    row.getCell(0).setCellValue(headTitle);
                    row = sheet.createRow(titleIndex++);
                }

                HSSFCell cell;

                // 遍历标题行
                if (null != title && title.get(i).length > 0) {
                    for (int j = 0; j < title.get(i).length; j++) {
                        cell = row.createCell((short) j);
                        cell.setCellStyle(hssfCellStyle);
                        cell.setCellValue(ConversionServiceProvider.instance().getConversionService().convert(title.get(i)[j], String.class));
                    }
                }
                // 遍历动态内容
                if (null != context && context.size() > 0) {
                    List<Object[]> objList = context.get(i);
                    for (int k = 0; k < objList.size(); k++) {
                        Object[] obj = objList.get(k);
                        // 行 +1
                        row = sheet.createRow(k + titleIndex);
                        if (obj.length > 0) {
                            for (int x = 0; x < obj.length; x++) {
                                // 列 +1
                                cell = row.createCell((short) x);
                                cell.setCellStyle(hssfCellStyle);

                                //当单元格的值为null时，表示需要和他的上一行单元格合并，比较tricky,如果要合并两列单元格好像没法表示
                                if (obj[x] == null) {
                                    // 单元格合并
                                    // 四个参数分别是：起始行，起始列，结束行，结束列
                                    sheet.addMergedRegion(new CellRangeAddress(k + 1, (short) x, k + 2, (short) x));
                                    obj[x] = "";
                                }
                                try {
                                    cell.setCellValue(Integer.valueOf(String.valueOf(obj[x])));
                                } catch (Exception e) {
                                    cell.setCellValue(String.valueOf(obj[x]));
                                }
                            }
                        }
                    }
                }
            }

            // 以流的方式传给浏览器
            wb.write(response.getOutputStream());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 新写的一个下载excel表格的方法
     * 支持TitleGenerator和ContentGenerator，从而能够更好的控制生成的内容
     * 如果是一般格式的话，用DefaultTitleGenerator和DefaultContentGenerator即可，从而不需要了解POI的相关语法
     * 具体可看这些类的注释
     *
     * @param request
     * @param response
     * @param excelVersion
     * @param fileName
     * @param sheetNames
     * @param titleGenerators
     * @param contentGenerators
     * @param excelStyle
     * @return
     * @author changyuan.liu
     */
    @SuppressWarnings("deprecation")
    public static boolean downLoadExcelSheets(HttpServletRequest request,
                                              HttpServletResponse response,
                                              Integer excelVersion,
                                              String fileName,
                                              String[] sheetNames,
                                              List<TitleGenerator> titleGenerators,
                                              List<ContentGenerator> contentGenerators,
                                              ExcelStyle excelStyle) {
        try {
            // 文件名
            if (excelVersion == null || excelVersion != EXCEL_VERSION_03) {
                fileName += ".xlsx";
            } else {
                fileName += ".xls";
            }
            fileName = UtopiaHttpRequestContext.attachmentFilenameEncoding(fileName, request);
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            // 转换编码-浏览器安全规则
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 创建workbook,即excel文件对象
            Workbook wb;
            if (excelVersion == null || excelVersion != EXCEL_VERSION_03) {
                wb = new XSSFWorkbook();    //第一步
            } else {
                wb = new HSSFWorkbook();    //第一步
            }
            CellStyle cellStyle = wb.createCellStyle();
            CellStyle headTitleStyle = wb.createCellStyle();

            if (excelStyle != null) {
                // 对齐方式
                if (excelStyle.getHorizontalAlignment() != null) {
                    cellStyle.setAlignment(excelStyle.getHorizontalAlignment());
                    headTitleStyle.setAlignment(excelStyle.getHorizontalAlignment());
                }
                if (excelStyle.getVerticalAlignment() != null) {
                    cellStyle.setVerticalAlignment(excelStyle.getVerticalAlignment());
                    headTitleStyle.setVerticalAlignment(excelStyle.getVerticalAlignment());
                }
                // 字体
                if (excelStyle.getFontName() != null || excelStyle.getFontSize() != null) {
                    Font font = wb.createFont();
                    if (excelStyle.getFontName() != null) {
                        font.setFontName(excelStyle.getFontName());
                    }
                    if (excelStyle.getFontSize() != null) {
                        font.setFontHeightInPoints(excelStyle.getFontSize());
                    }

                    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                    cellStyle.setFont(font);
                    headTitleStyle.setFont(font);
                }
                // 边框
                if (excelStyle.getShowBorder()) {
                    cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
                    cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //左边框
                    cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);    //上边框
                    cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  //右边框

                    headTitleStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
                    headTitleStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //左边框
                    headTitleStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);    //上边框
                    headTitleStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  //右边框
                }
            }

            // 初始化标签
            if (sheetNames != null) {
                for (int i = 0; i < sheetNames.length; i++) {

                    Sheet sheet = wb.createSheet(sheetNames[i]);      //第二步

                    // 行高
                    if (excelStyle != null && excelStyle.getRowHeight() != null) {
                        sheet.setDefaultRowHeightInPoints(excelStyle.getRowHeight());
                    }
                    // 列宽
                    if (excelStyle != null && excelStyle.getColumnWidth() != null) {
                        sheet.setDefaultColumnWidth(excelStyle.getColumnWidth());
                    }

                    int titleIndex = 0;
                    if (titleGenerators != null && i < titleGenerators.size() && titleGenerators.get(i) != null) {
                        titleIndex = titleGenerators.get(i).generateTitle(sheet, cellStyle, headTitleStyle);
                    }

                    // 遍历动态内容
                    if (contentGenerators != null && i < contentGenerators.size() && contentGenerators.get(i) != null) {
                        titleIndex = contentGenerators.get(i).generateContent(sheet, cellStyle, titleIndex);
                    }
                }
            }

            // 以流的方式传给浏览器
            wb.write(response.getOutputStream());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 表格抬头生成器接口
     *
     * @author changyuan.liu
     */
    public static interface TitleGenerator {
        /**
         * @param sheet          表格对象
         * @param cellStyle      当前表格默认的单元格格式
         * @param headTitleStyle 当前表格默认的抬头单元格格式
         * @return
         * @author changyuan.liu
         */
        int generateTitle(Sheet sheet, CellStyle cellStyle, CellStyle headTitleStyle);
    }

    /**
     * 表格内容生成器接口
     *
     * @author changyuan.liu
     */
    public static interface ContentGenerator {
        /**
         * @param sheet     表格对象
         * @param cellStyle 当前表格默认的单元格格式
         * @param rowInd    内容首行
         * @return
         * @author changyuan.liu
         */
        int generateContent(Sheet sheet, CellStyle cellStyle, int rowInd);
    }

    /**
     * 表格样式
     *
     * @author changyuan.liu
     */
    @Data
    public static class ExcelStyle {
        // 内容在单元格的水平位置
        Short horizontalAlignment;

        // 内容在单元格的垂直位置
        Short verticalAlignment;

        // 字体
        String fontName;

        // 字体大小
        Short fontSize;

        // 是否有边框
        Boolean showBorder;

        // 行高
        Float rowHeight;

        // 行宽
        Integer columnWidth;
    }

    /**
     * 默认的抬头生成器，适用于大部分的下载报告
     * 包含两个字段，标题、各列抬头
     * 生成形式如下：
     * [           标题            ]
     * [列1,列2，列3............... ]
     *
     * @author changyuan.liu
     */
    public static class DefaultTitleGenerator implements TitleGenerator {

        private String headTitle;
        private Object[] title;

        public DefaultTitleGenerator(final String headTitle,
                                     final Object[] title) {
            this.headTitle = headTitle;
            this.title = title;
        }

        @Override
        public int generateTitle(Sheet sheet, CellStyle cellStyle, CellStyle headTitleStyle) {
            int titleIndex = 0;
            // 加入试卷标题
            Row row = sheet.createRow(titleIndex++);
            if (StringUtils.isNotBlank(headTitle)) {
                for (int j = 0; j < title.length; j++) {
                    row.createCell(j).setCellStyle(headTitleStyle);
                }
                CellRangeAddress region = new CellRangeAddress(0, 0, (short) 0, (short) (title.length - 1));
                sheet.addMergedRegion(region);
                row.getCell(0).setCellValue(headTitle);
                row = sheet.createRow(titleIndex++);
            }

            // 遍历标题行
            if (null != title && title.length > 0) {
                for (int j = 0; j < title.length; j++) {
                    Cell cell = row.createCell((short) j);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(ConversionServiceProvider.instance().getConversionService().convert(title[j], String.class));
                }
            }
            return titleIndex;
        }
    }

    /**
     * 默认的内容生成器
     * 包含content二维数组，记录具体的内容值
     * mergeNullCell    true表示当元素为null的时候，与前一行合并
     * false表示null为空单元格
     *
     * @author changyuan.liu
     */
    public static class DefaultContentGenerator implements ContentGenerator {

        private List<Object[]> content = null;
        private boolean mergeNullCell = false;

        public DefaultContentGenerator(List<Object[]> content) {
            this.content = content;
        }

        public DefaultContentGenerator(List<Object[]> content, boolean mergeNullCell) {
            this.content = content;
            this.mergeNullCell = mergeNullCell;
        }

        public void withMergeNullCell(boolean mergeNullCell) {
            this.mergeNullCell = mergeNullCell;
        }

        @Override
        public int generateContent(Sheet sheet, CellStyle cellStyle, int rowInd) {
            for (int k = 0; k < content.size(); k++) {
                Object[] obj = content.get(k);
                // 行 +1
                Row row = sheet.createRow(k + rowInd);
                if (obj.length > 0) {
                    for (int x = 0; x < obj.length; x++) {
                        // 列 +1
                        Cell cell = row.createCell((short) x);
                        cell.setCellStyle(cellStyle);

                        if (obj[x] == null) {
                            if (mergeNullCell) {
                                // 指定需要合并单元格时
                                // 当单元格的值为null时，表示需要和他的上一行单元格合并，比较tricky,如果要合并两列单元格只能新写个ContentGenerator了
                                // 单元格合并
                                // 四个参数分别是：起始行，结束行, 起始列，结束列
                                sheet.addMergedRegion(new CellRangeAddress(k + rowInd - 1, k + rowInd, (short) x, (short) x));
                            }
                            obj[x] = "";
                        }
                        try {
                            cell.setCellValue(Integer.valueOf(String.valueOf(obj[x])));
                        } catch (Exception e) {
                            cell.setCellValue(String.valueOf(obj[x]));
                        }
                    }
                }
            }
            return content.size() + rowInd;
        }
    }
}