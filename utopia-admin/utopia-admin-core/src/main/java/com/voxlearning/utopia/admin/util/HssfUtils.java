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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Alex on 15-3-13.
 */
public class HssfUtils {

    private HssfUtils() {
    }

    public static HSSFRow createRow(HSSFSheet sheet, int rowNum, int column, CellStyle style) {
        HSSFRow row = sheet.createRow(rowNum);
        for (int i = 0; i <= column; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }
        return row;
    }

    public static void setCellValue(HSSFRow row, int column, CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value == null ? "" : value);
    }

    public static void setCellValue(HSSFRow row, int column, CellStyle style, Long value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }

        cell.setCellStyle(style);
        cell.setCellValue(value == null ? 0 : value);
    }

    public static void setCellValue(HSSFRow row, int column, CellStyle style, Integer value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value == null ? 0 : value);
    }

    public static void setCellValue(HSSFRow row, int column, CellStyle style, Double value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value == null ? 0 : value);
    }

    public static void setCellValue(HSSFRow row, int column, CellStyle style, Date value, String dateFormat) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value == null ? "-" : DateUtils.dateToString(value, dateFormat));
    }

    public static Integer getIntCellValue(HSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).intValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toInt(cell.getStringCellValue().trim());
        }

        return null;
    }

    public static Long getLongCellValue(HSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).longValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toLong(cell.getStringCellValue().trim());
        }

        return null;
    }

    public static String getStringCellValue(HSSFCell cell) {
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

}
