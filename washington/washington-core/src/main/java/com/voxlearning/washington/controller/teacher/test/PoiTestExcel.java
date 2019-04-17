package com.voxlearning.washington.controller.teacher.test;

import java.io.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @author zhangbin
 * @since 2016/11/6 17:29
 */
public class PoiTestExcel {
    /**
     * @param args
     */
    public static void main(String[] args) {
        getUnitReport();
    }

    private static HSSFWorkbook getUnitReport() {
        try {
            InputStream in = new FileInputStream("D:\\report.xls");
            HSSFWorkbook work = new HSSFWorkbook(in);
            // 得到excel的第0张表
            Sheet sheet = work.getSheetAt(0);
            // 得到第1行的第一个单元格的样式
            Row rowCellStyle = sheet.getRow(1);
            CellStyle columnOne = rowCellStyle.getCell(0).getCellStyle();
            // 这里面的行和列的数法与计算机里的一样，从0开始是第一
            // 填充title数据
            Row row = sheet.getRow(0);
            Cell cell = row.getCell(0);
            cell.setCellValue("2010年花名测");
            int i = 2;//计数器
            int number = 0;
            // 得到行，并填充数据和表格样式
            for (; i < 10; i++) {
                row = sheet.createRow(i);// 得到行
                cell = row.createCell(0);// 得到第0个单元格
                cell.setCellValue("琳" + i);// 填充值
                cell.setCellStyle(columnOne);// 填充样式
                cell = row.createCell(1);
                cell.setCellValue("女");
                cell.setCellStyle(columnOne);// 填充样式
                cell = row.createCell(2);
                cell.setCellValue(i + 20);
                cell.setCellStyle(columnOne);// 填充样式
                // .....给每个单元格填充数据和样式
                number++;
            }
            //创建每个单元格，添加样式，最后合并
            row = sheet.createRow(i);
            cell = row.createCell(0);
            cell.setCellValue("总计：" + number + "个学生");// 填充值
            cell.setCellStyle(columnOne);
            cell = row.createCell(1);
            cell.setCellStyle(columnOne);
            cell = row.createCell(2);
            cell.setCellStyle(columnOne);
            // 合并单元格
            sheet.addMergedRegion(new CellRangeAddress(i, i, 0, 2));
            FileOutputStream os = new FileOutputStream("D:\\workbook.xls");
            work.write(os);
            os.close();
            return work;
        } catch (FileNotFoundException e) {
            System.out.println("文件路径错误");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("文件输入流错误");
            e.printStackTrace();
        }
        return null;
    }

    private static HSSFWorkbook getTermReport() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        HSSFRow row = sheet.createRow(0);
        row.setHeight((short) 1000);
        sheet.setColumnWidth(0, 8000);

        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setAlignment((short) 2);
        cellStyle.setVerticalAlignment((short) 1);

        HSSFCell cell = row.createCell(0);
        cell.setCellValue("姓名       完成次数              布置次数");
        cell.setCellStyle(cellStyle);

        //画线(由左上到右下的斜线)
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        HSSFClientAnchor a = new HSSFClientAnchor(0, 0, 1000, 200, (short) 0, 0, (short) 0, 0);
        HSSFClientAnchor b = new HSSFClientAnchor(0, 0, 400, 255, (short) 0, 0, (short) 0, 0);
        HSSFSimpleShape shape1 = patriarch.createSimpleShape(a);
        HSSFSimpleShape shape2 = patriarch.createSimpleShape(b);
        shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
        shape2.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
        shape1.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);
        shape2.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("D:\\test.xls");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("generate  excel success ! ");
        return workbook;
    }
}
