package com.voxlearning.washington.controller.schoolmaster;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.washington.support.FileOssManageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/report")
public class ReportCreateController extends SchoolMasterBaseController{

    @RequestMapping(value = "/getReportDir.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getReportDir(){
        MapMessage result = new MapMessage();
        String dir = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3);
        result.add("result",true);
        result.add("dir","image"+dir);
        return result;
    }

    /**
     */
    @RequestMapping(value = "/createReport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createReport(String no,String content,String dir, String fileName,List<String> filePaths,String endflag){
        MapMessage result = new MapMessage();
        try {
            byte[] filebyte = org.apache.commons.codec.binary.Base64.decodeBase64(content);
            InputStream inputStream = new ByteArrayInputStream(filebyte);
            String filePathTemp = FileOssManageUtils.upload(inputStream,filebyte.length,no,dir,"jpg");

            if(StringUtils.isNotBlank(endflag)){
                Document document = new Document();
                document.setMargins(0, 0, 0, 0);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, byteArrayOutputStream);
                filePaths.add(filePathTemp);
                document.open();
                for(int i=0; i< filePaths.size(); i++){
                    InputStream is = FileOssManageUtils.downFile(filePaths.get(i));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    byte[] b = baos.toByteArray();
                    is.close();
                    baos.close();
                    Image img = Image.getInstance(b);
                    img.setAlignment(Image.ALIGN_CENTER);
                    // 根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
                    img.scalePercent(50);
                    document.setPageSize(new Rectangle(img.getWidth()/2f, img.getHeight()/2f));
                    document.newPage();
                    document.add(img);
                }
                document.close();
                InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                String filePath = FileOssManageUtils.upload(is,byteArrayOutputStream.toByteArray().length,fileName,"pdf");
                result.add("reportPath",filePath);
                byteArrayOutputStream.close();
            }

            result.add("filePathTemp",filePathTemp);
            result.add("result",true);
            return result;
        } catch (Exception ex) {
            logger.error("Failed to create exam report, ex={}", ex);
            result.add("result",false);
            result.add("info","报告生成失败");
            return result;
        }
    }

    /**
     */
    @RequestMapping(value = "/createReports.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createReports(List<String> nos,List<String> contents,String dir, String fileName,List<String> filePaths,String endflag){
        MapMessage result = new MapMessage();
        try {
            List filePathTemps = new LinkedList();
            for(int i=0; i<nos.size(); i++){
                String no = nos.get(i);
                String content = contents.get(i);
                byte[] filebyte = org.apache.commons.codec.binary.Base64.decodeBase64(content);
                InputStream inputStream = new ByteArrayInputStream(filebyte);
                String filePathTemp = FileOssManageUtils.upload(inputStream,filebyte.length,no,dir,"jpg");
                filePathTemps.add(filePathTemp);
            }
            result.add("filePathTemp",filePathTemps);
            result.add("result",true);

            if(StringUtils.isNotBlank(endflag)){
                Document document = new Document();
                document.setMargins(0, 0, 0, 0);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, byteArrayOutputStream);
                filePaths.addAll(filePathTemps);
                document.open();
                for(int i=0; i< filePaths.size(); i++){
                    InputStream is = FileOssManageUtils.downFile(filePaths.get(i));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    byte[] b = baos.toByteArray();
                    is.close();
                    baos.close();
                    Image img = Image.getInstance(b);
                    img.setAlignment(Image.ALIGN_CENTER);
                    // 根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
                    img.scalePercent(50);
                    document.setPageSize(new Rectangle(img.getWidth()/2f, img.getHeight()/2f));
                    document.newPage();
                    document.add(img);
                }
                document.close();
                InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                String filePath = FileOssManageUtils.upload(is,byteArrayOutputStream.toByteArray().length,fileName,"pdf");
                result.add("reportPath",filePath);
                byteArrayOutputStream.close();
            }
            return result;
        } catch (Exception ex) {
            logger.error("Failed to create exam report, ex={}", ex);
            result.add("result",false);
            result.add("info","报告生成失败");
            return result;
        }
    }

//    /**
//     * @param files
//     * @return
//     */
//    @RequestMapping(value = "/createReport.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage createPdf(List<String> files, String fileName) {
//        MapMessage result = new MapMessage();
//        try {
//            Document document = new Document();
//            document.setMargins(0, 0, 0, 0);
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            PdfWriter.getInstance(document, byteArrayOutputStream);
//
//            document.open();
//            for(int i=0; i<files.size(); i++){
//                String file = files.get(i);
//                byte[] filebyte = org.apache.commons.codec.binary.Base64.decodeBase64(file);
//                // 第四步：在文档中增加图片。
//                Image img = Image.getInstance(filebyte);
//                img.setAlignment(Image.ALIGN_CENTER);
//                // 根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
//                img.scalePercent(50);
//                document.setPageSize(new Rectangle(img.getWidth()/2f, img.getHeight()/2f));
//                document.newPage();
//                document.add(img);
//            }
//            document.close();
//
//            InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//            String filePath = FileOssManageUtils.upload(is,byteArrayOutputStream.toByteArray().length,fileName,"pdf");
//
//            result.add("result",true);
//            result.add("reportPath",filePath);
//            return result;
//        } catch (Exception ex) {
//            logger.error("Failed to create exam report, ex={}", ex);
//            result.add("result",false);
//            result.add("info","报告生成失败");
//            return result;
//        }
//    }

    @RequestMapping(value = "/downReport.vpage", method = RequestMethod.GET)
    public void downReport(String filePath,String fileName){
        try{
            InputStream inputStream = FileOssManageUtils.downFile(filePath);
            getResponse().reset();
            getResponse().setContentType("application/pdf");
            getResponse().setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            OutputStream out = getResponse().getOutputStream();
            byte buffer[] = new byte[1024];
            int len = 0;
            while((len=inputStream.read(buffer))>0){
                out.write(buffer, 0, len);
            }
            inputStream.close();
            out.close();

        }catch(Exception e){
            logger.error("下载报告失败",e);
        }

    }

}
