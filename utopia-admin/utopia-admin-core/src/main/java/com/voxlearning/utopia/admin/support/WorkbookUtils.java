package com.voxlearning.utopia.admin.support;

import com.voxlearning.alps.core.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;


public class WorkbookUtils {
    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(new BigDecimal(cell.getNumericCellValue()).longValue());
        }
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return StringUtils.deleteWhitespace(cell.getStringCellValue());
        }
        if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return StringUtils.EMPTY;
        }
        return null;
    }

    public static String getCellValueTrim(Cell cell) {
        String value = getCellValue(cell);
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return value.replaceAll(String.valueOf((char) 160), "").replaceAll("\\*s", "");
    }

    public static Row createRow(Sheet sheet, int rowNum, int column, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < column; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }
        return row;
    }

    public static void setCellValue(Row row, int column, CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    /**
     * 获取合并单元格的值
     */
    public static String getMergedRegionValue(Sheet sheet, int row, int column) {
        for (int i = 0; i < sheet.getNumMergedRegions(); ++i) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    Cell cell = sheet.getRow(firstRow).getCell(firstColumn);
                    return getCellValue(cell);
                }
            }
        }
        return null;
    }

    /**
     * 判断是否合并了行
     */
    public static boolean isMergedRegion(Sheet sheet, int row, int column) {
        for (int i = 0; i < sheet.getNumMergedRegions(); ++i) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    return true;
                }
            }
        }
        return false;
    }

}
