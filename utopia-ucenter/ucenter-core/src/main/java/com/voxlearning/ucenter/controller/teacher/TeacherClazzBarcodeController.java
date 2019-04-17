/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.ucenter.controller.teacher;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.ucenter.utils.BarcodeUtil;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * @Author:XiaochaoWei
 * @Description:
 * @CreateTime: 2017/6/12
 */
@Controller
@RequestMapping("teacher/clazz")
public class TeacherClazzBarcodeController extends AbstractWebController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    /**
     * 班级管理主页批量生成条形码
     */
    @RequestMapping(value = "batchgeneratebarcode.vpage", method = RequestMethod.POST)
    public void batchGenerateBarcode(HttpServletRequest request, HttpServletResponse response) {
        String data = getRequestString("students");
        List<Map> students = JsonUtils.fromJsonToList(data, Map.class);
        Integer codeNum = getRequestInt("codeNum");
        Long clazzId = getRequestLong("clazzName");
        try {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            String clazzName = clazz.getClassName();
            OutputStream os = response.getOutputStream();
            response.setContentType("application/pdf");
            String userAgent = request.getHeader("user-agent").toLowerCase();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String date = format.format(System.currentTimeMillis());
            String fileName = clazz.formalizeClazzName() + "条形码" + date + ".pdf";

            if (userAgent.contains("msie") || userAgent.contains("like gecko")) {
                // win10 IE edge 浏览器 和其他系统的IE
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                // 非IE
                fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            }
            response.setHeader("Content-disposition", "attachment; filename = " + fileName);
            //创建文件 设定页边距
            Document document = new Document(PageSize.A4, (595 / 210) * 8, (595 / 210) * 8, (842 / 297) * 8, (842 / 297) * 8);
            //建立一个书写器
            PdfWriter writer = PdfWriter.getInstance(document, os);
            //支持中文输出
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font FontChinese = new Font(bfChinese, 11, Font.NORMAL);
            //打开文件
            document.open();

            //创建四列的表格
            PdfPTable table = new PdfPTable(4);
            //不显示边框
            table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            //表格铺满到页边距
            table.setWidthPercentage(100);

            for (int i = 0; i < students.size(); i++) {
                //制作内嵌表格
                PdfPTable nested = new PdfPTable(1);
                PdfPTable nestedTop = new PdfPTable(1);
                //设定单元格
                PdfPCell cell = new PdfPCell();
                PdfPCell cellTop = new PdfPCell();
                //取消边框
                cell.setBorderWidth(0);
                cellTop.setBorderWidth(0);

                //每一页的第一张条码上打印班级
                Paragraph pTop = new Paragraph(clazzName + "    " + ConversionUtils.toString(students.get(i).get("name")), FontChinese);
                //设定居中
                pTop.setAlignment(Element.ALIGN_CENTER);
                //设置行间距
                pTop.setLeading(14, 0);
                cellTop.addElement(pTop);

                //每页非第一张条码
                Paragraph p = new Paragraph(ConversionUtils.toString(students.get(i).get("name")), FontChinese);
                //设定居中
                p.setAlignment(Element.ALIGN_CENTER);
                //设置行间距
                p.setLeading(14, 0);
                cell.addElement(p);
                //插入图片
                String scanNum = ConversionUtils.toString(students.get(i).get("scanNum"));
                Image image = Image.getInstance(BarcodeUtil.generateCode(StringUtils.isEmpty(scanNum) ? "0000" : scanNum));
                cell.addElement(image);
                cellTop.addElement(image);

                Paragraph p1 = new Paragraph("填涂号：" + scanNum, FontChinese);
                p1.setAlignment(Element.ALIGN_CENTER);
                //设置行间距
                p1.setLeading(11, 0);
                cell.addElement(p1);
                cellTop.addElement(p1);
                //设定底部距离
                cell.setPaddingBottom(6);
                nested.addCell(cell);
                nestedTop.addCell(cellTop);
                //一个填涂号打印几张
                for (int j = 0; j < codeNum; j++) {
                    if ((i == 0 && j == 0) || (i != 0 && (i * codeNum + j) % 56 == 0)) {
                        //每页左上角第一个条码打印班级
                        table.addCell(nestedTop);
                    } else {
                        table.addCell(nested);
                    }
                }
                //用于补齐表格，填不满itext会自动舍弃
                if (i == students.size() - 1 && (students.size() * codeNum) % 4 != 0) {
                    //计算要补齐几个单元格
                    for (int k = 0; k < (4 - (students.size() * codeNum) % 4); k++) {
                        table.addCell("");
                    }
                }
            }
//            table.setWidthPercentage(100);
            document.add(table);
            //关闭文档
            document.close();
            //关闭书写器
            writer.close();
            //关闭流
            os.flush();
            os.close();
        } catch (Exception e) {
            logger.error("generate pdf error,", e);
        }
    }

    /**
     * 班级管理主页批量生成条形码校验
     */
    @RequestMapping(value = "batchgeneratebarcodecheck.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage batchGenerateBarcodeCheck() {
        Long clazzId = getRequestLong("clazzId");
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(clazz.getSchoolId())
                .getUninterruptibly();

        if (schoolExtInfo != null && schoolExtInfo.isBarcodeAnswerQuestionFlag()) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    public static void main(String[] args) {

//        try {
//            //创建文件 设定页边距
//            Document document = new Document(PageSize.A4,(595/210)*8,(595/210)*8,(842/297)*8,(842/297)*8);
//            //建立一个书写器
//            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("D:\\test.pdf"));
//            //支持中文输出
//            BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
//            Font FontChinese = new Font(bfChinese, 12, Font.NORMAL);
//            //设置页眉
////            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(header, fontDetail), document.left(), document.top() + 20, 0);
//
//            //打开文件
//            document.open();
//
//            //创建四列的表格
//            PdfPTable table = new PdfPTable(4);
//            //不显示边框
////            table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
//            //表格铺满到页边距
//            table.setWidthPercentage(100);
//            //制作内嵌表格
//            PdfPTable nested = new PdfPTable(1);
//            //设定单元格
//            PdfPCell cell = new PdfPCell();
//            //取消边框
//            cell.setBorderWidth(0);
//
//            Paragraph p = new Paragraph("卡卡西",FontChinese);
//            //设定居中
//            p.setAlignment(Element.ALIGN_CENTER);
//            p.setLeading(12,0);
//            cell.addElement(p);
//            //插入图片
//            Image image1 = Image.getInstance(BarcodeUtil.generateCode("1234567890"));
//            cell.addElement(image1);
//
//            Paragraph p1 = new Paragraph("填涂号:" + "1234567890",FontChinese);
//            p1.setAlignment(Element.ALIGN_CENTER);
//            //设置行间距
//            p1.setLeading(11,0);
//            cell.addElement(p1);
//            //设定底部距离
//            cell.setPaddingBottom(8);
//
//            nested.addCell(cell);
////            nested.addCell("");
//            for (int i=0; i<120; i++) {
//                table.addCell(nested);
//            }
////            table.setWidthPercentage(100);
//            document.add(table);
//
//            //关闭文档
//            document.close();
//            //关闭书写器
//            writer.close();
//        }catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
