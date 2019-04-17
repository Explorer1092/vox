package com.voxlearning.washington.controller.specialteacher;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.KlxStudent;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教务系统条形码相关
 * FIXME 二期要做整个年级一起打印，所以单独放一个controller吧
 */
@Controller
@RequestMapping("/specialteacher")
public class PrintBarcodeController extends AbstractSpecialTeacherController {

    @Inject private RaikouSDK raikouSDK;

    /**
     * 教务老师批量生成条形码校验
     */
    @RequestMapping(value = "batchgeneratebarcodecheck.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage batchGenerateBarcodeCheck() {
        User specialTeacher = currentSpecialTeacher();
        Long schoolId = currentSchoolId();
        if (specialTeacher == null || schoolId == null) {
            return MapMessage.errorMessage();
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        if (schoolExtInfo != null && schoolExtInfo.isBarcodeAnswerQuestionFlag()) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "getstudentswithoutscannum.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getStudentsWithoutScanNum() {
        String clazzIdStr = getRequestString("clazzIds");
        String[] clazzIdArray = clazzIdStr.split(",");
        List<String> clazzIdList = Arrays.asList(clazzIdArray);
        List<Long> clazzIds = new ArrayList<>();
        clazzIdList.forEach(p -> {
            clazzIds.add(ConversionUtils.toLong(p));
        });
        Map<Long, List<KlxStudent>> clazzKlxStudentsMap = specialTeacherLoaderClient.loadKlxStudentsByClazzIds(clazzIds);
        List<KlxStudent> klxStudents = clazzKlxStudentsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        int studentTotalNum = klxStudents.size();
        int studentWithoutScanNumCount = klxStudents.stream().filter(p -> StringUtils.isEmpty(p.getScanNumber())).collect(Collectors.toList()).size();
        return MapMessage.successMessage().add("studentTotalNum", studentTotalNum).add("studentWithoutScanNumCount", studentWithoutScanNumCount);
    }

    /**
     * 批量生成条形码
     */
    @ResponseBody
    @RequestMapping(value = "batchgeneratebarcode.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public void batchGenerateBarcode(HttpServletRequest request, HttpServletResponse response) {

        Integer codeNum = getRequestInt("codeNum");
        Long clazzId = getRequestLong("clazzId");
        try {
            List<KlxStudent> klxStudentList = specialTeacherLoaderClient.loadKlxStudentsByClazzId(clazzId);
            List<KlxStudent> klxStudents = klxStudentList.stream().sorted(Comparator.comparing(KlxStudent::getScanNumber)).collect(Collectors.toList());
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (Objects.isNull(clazz)) {
                logger.error("clazz is null clazzId:" + clazzId);
                return;
            }
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

            for (int i = 0; i < klxStudents.size(); i++) {
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
                Paragraph pTop = new Paragraph(clazzName + "    " + ConversionUtils.toString(klxStudents.get(i).getName()), FontChinese);
                //设定居中
                pTop.setAlignment(Element.ALIGN_CENTER);
                //设置行间距
                pTop.setLeading(14, 0);
                cellTop.addElement(pTop);

                //每页非第一张条码
                Paragraph p = new Paragraph(ConversionUtils.toString(klxStudents.get(i).getName()), FontChinese);
                //设定居中
                p.setAlignment(Element.ALIGN_CENTER);
                //设置行间距
                p.setLeading(14, 0);
                cell.addElement(p);
                //插入图片
                String scanNum = ConversionUtils.toString(klxStudents.get(i).getScanNumber());
                Image image = Image.getInstance(this.generateCode(StringUtils.isEmpty(scanNum) ? "0000" : scanNum));
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
                if (i == klxStudents.size() - 1 && (klxStudents.size() * codeNum) % 4 != 0) {
                    //计算要补齐几个单元格
                    for (int k = 0; k < (4 - (klxStudents.size() * codeNum) % 4); k++) {
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

    private byte[] generateCode(String content) {
        int BLACK = -16777216;
        int WHITE = -1;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int width = 111;
            int height = 16;
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(content.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.CODE_128,
                    width,
                    height,
                    hints);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
                }
            }
            ImageIO.write(image, "png", out);
        } catch (Exception e) {
            logger.error("generate barcode error,", e);
        }
        return out.toByteArray();
    }
}
