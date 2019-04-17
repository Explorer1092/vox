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

package com.voxlearning.utopia.agent;

/**
 * Created by Alex on 2015/9/12.
 */

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.math.BigDecimal;

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

    public static void setCellValue(Row row, int column,  CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static void setCellValue(Row row, int column,  CellStyle style, Long value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }

        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static void setCellValue(Row row, int column,  CellStyle style, Integer value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static void setCellValue(Row row, int column,  CellStyle style, Double value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    public static void setCellValue(Row row, int column,  CellStyle style, Float value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        BigDecimal bigDecimalValue = new BigDecimal(Float.toString(value));
        cell.setCellValue(bigDecimalValue.doubleValue());
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
            return String.valueOf(new BigDecimal(cell.getNumericCellValue())).trim();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return StringUtils.deleteWhitespace(cell.getStringCellValue()).trim();
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return "";
        }

        return null;
    }

    public static Float getFloatCellValue(XSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).floatValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return SafeConverter.toFloat(cell.getStringCellValue().trim());
        }

        return null;
    }


    public static Double getDoubleCellValue(XSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return SafeConverter.toDouble(cell.getStringCellValue().trim());
        }
        return null;
    }


    public static String getCellStringValue(XSSFCell cell){
        if(cell == null){
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    public static String getCellStringValue(XSSFCell cell, FormulaEvaluator evaluator){
        if(cell == null){
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        String value = "";
        try{
            value = formatter.formatCellValue(cell, evaluator);
        }catch (Exception e){
        }
        return value;
    }

}

