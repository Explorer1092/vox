/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.agent.support;

import com.aspose.words.*;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Cleanup;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author Junjie Zhang
 * @since 2014-04-18
 */
@SuppressWarnings("unused")
public class WordBuilder {

    private DocumentBuilder builder;
    private Document document;

    public WordBuilder(String strFileName) throws Exception {
        if (!StringUtils.isEmpty(strFileName)) {
            @Cleanup InputStream is = getClass().getResourceAsStream(strFileName);
            this.document = new Document(is);
        }
    }

    public WordBuilder() throws Exception {
        document = new Document();
    }

    public void builder() throws Exception {
        builder = new DocumentBuilder(document);
    }

    public void saveAs(String strFileName) throws Exception {
        document.save(strFileName);
    }


    /**
     * 保存为字节数组
     *
     * @param saveFormat SaveFormat数值
     * @return byte[]
     * @throws Exception
     */
    public byte[] saveAsByteArray(int saveFormat) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        document.save(byteArrayOutputStream, saveFormat);
        return byteArrayOutputStream.toByteArray();
    }

    public void writeText(String strText, String fontName, double conSize, boolean conBold, int paragraphAlignment) throws Exception {
        builder.setBold(conBold);
        builder.getFont().setName(fontName);
        builder.getFont().setSize(conSize);
        builder.getParagraphFormat().setAlignment(paragraphAlignment);
        builder.write(strText);
    }

    public void writelnText(String strText, String fontName, double conSize, boolean conBold, int paragraphAlignment) throws Exception {
        builder.setBold(conBold);
        builder.getFont().setName(fontName);
        builder.getFont().setSize(conSize);
        builder.insertParagraph().getParagraphFormat().setAlignment(paragraphAlignment);
        builder.writeln(strText);
    }

    /**
     * 设置纸张
     */
    public void setPaperSize(String paperSize) {

        switch (paperSize) {
            case "A4":
                for (Section section : document.getSections()) {
                    section.getPageSetup().setPaperSize(PaperSize.A4);
                    section.getPageSetup().setOrientation(Orientation.PORTRAIT);
                    section.getPageSetup().setVerticalAlignment(PageVerticalAlignment.TOP);
                }
                break;
            case "A4H"://A4横向
                for (Section section : document.getSections()) {
                    section.getPageSetup().setPaperSize(PaperSize.A4);
                    section.getPageSetup().setOrientation(Orientation.LANDSCAPE);
                    section.getPageSetup().getTextColumns().setCount(2);
                    section.getPageSetup().getTextColumns().setEvenlySpaced(true);
                    section.getPageSetup().getTextColumns().setLineBetween(true);

                }
                break;
            case "A3":
                for (Section section : document.getSections()) {
                    section.getPageSetup().setPaperSize(PaperSize.A3);
                    section.getPageSetup().setOrientation(Orientation.PORTRAIT);
                }

                break;
            case "A3H"://A3横向

                for (Section section : document.getSections()) {
                    section.getPageSetup().setPaperSize(PaperSize.A3);
                    section.getPageSetup().setOrientation(Orientation.LANDSCAPE);
                    section.getPageSetup().getTextColumns().setCount(2);
                    section.getPageSetup().getTextColumns().setEvenlySpaced(true);
                    section.getPageSetup().getTextColumns().setLineBetween(true);

                }

                break;

            case "16K":
                for (Section section : document.getSections()) {
                    section.getPageSetup().setPaperSize(PaperSize.B5);
                    section.getPageSetup().setOrientation(Orientation.PORTRAIT);

                }


                break;

            case "8KH":
                for (Section section : document.getSections()) {
                    section.getPageSetup().setPageWidth(36.4);//纸张宽度
                    section.getPageSetup().setPageHeight(25.7);//纸张宽度
                    section.getPageSetup().setOrientation(Orientation.LANDSCAPE);
                    section.getPageSetup().getTextColumns().setCount(2);
                    section.getPageSetup().getTextColumns().setEvenlySpaced(true);
                    section.getPageSetup().getTextColumns().setLineBetween(true);

                }

                break;
        }
    }

    public void setLineSpacing(double lineSpacing) throws Exception {
        builder.getParagraphFormat().setLineSpacing(lineSpacing);

    }

    public void setHeader(String strBookmarkName, String text) throws Exception {
        if (document.getRange().getBookmarks() != null) {
            Bookmark mark = document.getRange().getBookmarks().get(strBookmarkName);
            mark.setText(text);
        }
    }

    public void insertHtml(String html) throws Exception {
        builder.insertHtml(html);
    }

    /**
     * 插入换行
     */
    public void insertLineBreak() throws Exception {
        builder.insertBreak(BreakType.LINE_BREAK);
    }

    /**
     * 插入换行
     */
    public void insertLineBreak(int nline) throws Exception {
        for (int i = 0; i < nline; i++)
            builder.insertBreak(BreakType.LINE_BREAK);
    }

    public boolean insertScoreTable(boolean dishand, boolean distab, String handText) throws Exception {

        builder.startTable();//开始画Table
        builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
        //添加Word表格
        builder.insertCell();
        builder.getCellFormat().setWidth(115.0);

        builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPoints(115));
        builder.getCellFormat().getBorders().setLineStyle(LineStyle.NONE);

        builder.startTable();//开始画Table
        builder.getRowFormat().setHeight(20.2);
        builder.insertCell();
        builder.getCellFormat().getBorders().setLineStyle(LineStyle.SINGLE);
        builder.getFont().setSize(10.5);
        builder.setBold(false);
        builder.write("评卷人");

        builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);//垂直居中对齐
        builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
        builder.getCellFormat().setWidth(50.0);
        builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPoints(50));
        builder.getRowFormat().setHeight(20.0);
        builder.insertCell();
        builder.getCellFormat().getBorders().setLineStyle(LineStyle.SINGLE);
        builder.getFont().setSize(10.5);
        builder.setBold(false);
        builder.write("得分");
        builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);//垂直居中对齐
        builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
        builder.getCellFormat().setWidth(50.0);
        builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPoints(50));
        builder.endRow();
        builder.getRowFormat().setHeight(25.0);
        builder.insertCell();
        builder.getRowFormat().setHeight(25.0);
        builder.insertCell();
        builder.endRow();
        builder.endTable();

        builder.insertCell();
        builder.getCellFormat().setWidth(300.0);
        builder.getCellFormat().setPreferredWidth(PreferredWidth.AUTO);
        builder.getCellFormat().getBorders().setLineStyle(LineStyle.NONE);

        builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);//垂直居中对齐
        builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
        builder.getFont().setSize(11);
        builder.setBold(true);
        builder.write(handText);
        builder.endRow();
        builder.getRowFormat().setHeight(28);
        builder.endTable();
        return true;
    }

    public boolean insertTable(Object[][] dt, boolean haveBorder, boolean isHtml, boolean autoFit, Integer alignment) throws Exception {
        Table table = builder.startTable();//开始画Table
        int paragraphAlignmentValue = builder.getParagraphFormat().getAlignment();
        builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
        //添加Word表格
        for (Object[] row : dt) {
            builder.getRowFormat().setHeight(25);
            for (Object aRow : row) {
                builder.insertCell();
                builder.getFont().setSize(10.5);
                builder.getFont().setName("宋体");
                builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);//垂直居中对齐
                builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);//水平居中对齐
                builder.getCellFormat().setWidth(50.0);
                builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPoints(50));
                if (haveBorder) {
                    //设置外框样式
                    builder.getCellFormat().getBorders().setLineStyle(LineStyle.SINGLE);
                    //样式设置结束
                } else {
                    builder.getCellFormat().getBorders().setLineStyle(LineStyle.NONE);
                }

                if (isHtml) {
                    builder.insertHtml(aRow.toString());
                } else {
                    builder.write(aRow.toString());
                }
            }

            builder.endRow();

        }
        builder.endTable();
        builder.getParagraphFormat().setAlignment(paragraphAlignmentValue);
//        table.setAlignment(TableAlignment.CENTER);
        table.setAlignment(alignment);
        table.setPreferredWidth(PreferredWidth.AUTO);
        if (autoFit) {
            table.autoFit(AutoFitBehavior.AUTO_FIT_TO_WINDOW);
        }
        return true;
    }

    /**
     * 插入选择题答题表
     *
     * @param start 开始题号
     * @param end   结束题号
     * @throws Exception
     */
    public void insertChoiceAnswerTable(int start, int end) throws Exception {
        Table table = builder.startTable();//开始画Table
        int paragraphAlignmentValue = builder.getParagraphFormat().getAlignment();
        builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
        int total = end - start + 1;
        //每行10个
        final int columns = 10;
        int rows = total % columns == 0 ? total / columns : total / columns + 1;
        //添加Word表格
        for (int i = 0; i < rows; i++) {
            builder.getRowFormat().setHeight(15);
            for (int j = 0; j < columns; j++) {
                int index = i * columns + j + start;
                builder.insertCell();
                builder.getFont().setSize(10.5);
                builder.getFont().setName("宋体");
                builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);//垂直居中对齐
                builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);//水平居中对齐
                builder.getCellFormat().setWidth(50);
                builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPoints(50));
                builder.getCellFormat().getBorders().setLineStyle(LineStyle.SINGLE);
                builder.getCellFormat().getShading().setBackgroundPatternColor(Color.decode("#F0F0F0"));
                if (index <= end)
                    builder.write("" + index);
                else
                    builder.write("");
            }
            builder.endRow();
            builder.getRowFormat().setHeight(25);
            for (int j = 0; j < columns; j++) {
                builder.insertCell();
                builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);//垂直居中对齐
                builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);//水平居中对齐
                builder.getCellFormat().getBorders().setLineStyle(LineStyle.SINGLE);
                builder.getCellFormat().getShading().setBackgroundPatternColor(Color.white);
                builder.getCellFormat().setWidth(50);
                builder.getCellFormat().setPreferredWidth(PreferredWidth.fromPoints(50));
            }
            builder.endRow();
        }
        builder.endTable();
        builder.getParagraphFormat().setAlignment(paragraphAlignmentValue);
        table.setAlignment(TableAlignment.CENTER);
        table.setPreferredWidth(PreferredWidth.AUTO);
    }

    /**
     * 插入非选择题答题表
     *
     * @param start 开始题号
     * @param end   结束题号
     * @throws Exception
     */
    public void insertOtherAnswerTable(int start, int end) throws Exception {
        Table table = builder.startTable();//开始画Table
        int paragraphAlignmentValue = builder.getParagraphFormat().getAlignment();
        builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
        //添加Word表格
        for (int i = start; i <= end; i++) {
            builder.insertCell();
            builder.getFont().setSize(10.5);
            builder.getFont().setName("宋体");
            builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.TOP);//垂直居中对齐
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);//水平居中对齐
            builder.getCellFormat().getBorders().setLineStyle(LineStyle.SINGLE);
            builder.getCellFormat().getShading().setBackgroundPatternColor(Color.white);
            builder.writeln("" + i + "题、");
            builder.writeln("");
            builder.writeln("");
            builder.writeln("");
            builder.endRow();
        }
        builder.endTable();
        builder.getParagraphFormat().setAlignment(paragraphAlignmentValue);
        table.setAlignment(TableAlignment.CENTER);
        table.setPreferredWidth(PreferredWidth.AUTO);
        table.autoFit(AutoFitBehavior.AUTO_FIT_TO_WINDOW);
    }

    public void insertPagebreak() throws Exception {
        builder.insertBreak(BreakType.PAGE_BREAK);

    }

    public void insertBookMark(String BookMark) throws Exception {
        builder.startBookmark(BookMark);
        builder.endBookmark(BookMark);

    }

    public void gotoBookMark(String strBookMarkName) throws Exception {
        builder.moveToBookmark(strBookMarkName);
    }

    public void clearBookMark() throws Exception {
        document.getRange().getBookmarks().clear();
    }

    public void replaceText(String oleText, String newText) throws Exception {
        document.getRange().replace(oleText, newText, false, false);
    }

    public void gotoDocumentEnd() throws Exception {
        builder.moveToDocumentEnd();
    }

    public void gotoDocumentStart() throws Exception {
        builder.moveToDocumentStart();
    }

    public void close() {
        //TODO
        builder = null;
        document = null;
    }

//    public static void main(String[] args) throws Exception {
//
//        WordBuilder wordBuilder = new WordBuilder();
//        wordBuilder.openWithTemplate("/template/answer_sheet.docx");
//        wordBuilder.builder();
//        wordBuilder.writelnText("试卷标题","黑体",14,false,ParagraphAlignment.CENTER);
//        wordBuilder.insertLineBreak();
//        wordBuilder.writelnText("学校：___________姓名：___________班级：___________考号：___________","黑体",10.5,false,ParagraphAlignment.CENTER);
//        wordBuilder.insertLineBreak();
//        wordBuilder.writelnText("选择题","黑体",12,false,ParagraphAlignment.LEFT);
//        Object[][] dt =new Object[2][10];
//        for (int i=0;i<2;i++){
//            for (int j=0;j<10;j++){
//                if (i<1)
//                    dt[i][j]=(j+1);
//                else
//                    dt[i][j]="";
//            }
//        }
//        wordBuilder.insertTable(dt,true);

//        wordBuilder.writelnText("选择题", "黑体", 12, false, ParagraphAlignment.LEFT);
//        wordBuilder.insertChoiceAnswerTable(1, 150);
//        wordBuilder.insertLineBreak();
//        wordBuilder.writelnText("非选择题（请在各试题的答题区内作答）", "黑体", 12, false, ParagraphAlignment.LEFT);
//        wordBuilder.insertOtherAnswerTable(16, 20);
//        wordBuilder.saveAs("D:/1.docx");
//
//    }
}
