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

package com.voxlearning.utopia.admin.util;

/**
 * Created by Alex on 2015/9/12.
 */

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Alex on 15-3-13.
 */
public class XssfUtils {
    private XssfUtils() {

    }

    public static XSSFRow createRow(XSSFSheet sheet, int rowNum, int column,  CellStyle style) {
        XSSFRow row = sheet.createRow(rowNum);
        for (int i = 0; i <= column; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }

        return row;
    }

    public static void setCellValue(XSSFRow row, int column,  CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static void setCellValue(XSSFRow row, int column,  CellStyle style, Long value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }

        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static void setCellValue(XSSFRow row, int column,  CellStyle style, Integer value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static void setCellValue(XSSFRow row, int column,  CellStyle style, Double value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static Integer getIntCellValue(XSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).intValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toInt(cell.getStringCellValue().trim());
        }

        return null;
    }

    public static Double getDoubleCellValue(XSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return SafeConverter.toDouble(cell.getStringCellValue().trim());
        }
        return null;
    }

    public static Long getLongCellValue(XSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).longValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toLong(cell.getStringCellValue().trim());
        }

        return null;
    }

    public static String getStringCellValue(XSSFCell cell) {
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(new BigDecimal(cell.getNumericCellValue()).longValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return StringUtils.deleteWhitespace(cell.getStringCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return "";
        }

        return null;
    }

    public static XSSFWorkbook convertToXSSFWorkbook(String[] titles, int[] width, List<List<String>> dataList, String error) throws Exception {
        if (titles == null || width == null) {
            throw new IllegalArgumentException("表头参数不能为空");
        }
        if (titles.length != width.length) {
            throw new IllegalArgumentException("参数不匹配");
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        int size = titles.length - 1;
        // 设置单元格边框样式
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);

        XSSFSheet sheet = workbook.createSheet();
        XSSFRow firstRow = XssfUtils.createRow(sheet, 0, size, cellStyle);
        firstRow.setHeightInPoints(20);
        for (int i = 0; i < titles.length; ++i) {
            sheet.setColumnWidth(i, width[i]);
            XssfUtils.setCellValue(firstRow, i, cellStyle, titles[i]);
        }

        int rowNum = 1;
        for (List<String> data : dataList) {
            XSSFRow xssfRow = XssfUtils.createRow(sheet, rowNum++, size, cellStyle);
            xssfRow.setHeightInPoints(20);
            for (int i = 0; i < data.size(); ++i) {
                XssfUtils.setCellValue(xssfRow, i, cellStyle, data.get(i));
            }
        }
        if (CollectionUtils.isEmpty(dataList)) {
            XSSFRow row = XssfUtils.createRow(sheet, 1, size, cellStyle);
            CellRangeAddress range = new CellRangeAddress(1, 1, 0, size);
            sheet.addMergedRegion(range);
            XssfUtils.setCellValue(row, 0, cellStyle, StringUtils.isBlank(error) ? "没有数据" : error);
        }
        return workbook;
    }

}

