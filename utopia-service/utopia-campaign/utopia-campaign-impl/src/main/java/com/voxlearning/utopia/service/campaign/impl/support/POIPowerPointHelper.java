package com.voxlearning.utopia.service.campaign.impl.support;

import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageSystem;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.xslf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Named;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Named
public class POIPowerPointHelper {
    private static final Logger logger = LoggerFactory.getLogger(POIPowerPointHelper.class);

    private static String IMAGE_PREFIX = "teacher/courseware/image";

    @StorageClientLocation(system = StorageSystem.OSS, storage = "17-pmc")
    private StorageClient storageClient;

    private static String HOST = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host"));

    public List<String> converPPTXtoImage(String pptFile, String imageSuffix) {
        List<String> imageList = new ArrayList<>();
        try (InputStream pptStream = getRemotePPTStream(pptFile); XMLSlideShow oneSlideShow = new XMLSlideShow(pptStream)) {
            String xmlFontFormat = "<xml-fragment xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" " +
                    "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">" +
                    "<a:rPr lang=\"zh-CN\" altLang=\"en-US\" dirty=\"0\" smtClean=\"0\"> " +
                    "<a:latin typeface=\"+mj-ea\"/> " +
                    "</a:rPr>" +
                    "</xml-fragment>";
            List<XSLFSlide> pptPageXSLFSLiseList = oneSlideShow.getSlides();
            String pre = RandomUtils.nextObjectId();
            for (int i = 0; i < pptPageXSLFSLiseList.size(); i++) {
                //设置字体，解决中文乱码问题
                CTGroupShape oneCTGroupShape = pptPageXSLFSLiseList.get(i).getXmlObject().getCSld().getSpTree();
                for (CTShape ctShape : oneCTGroupShape.getSpList()) {
                    CTTextBody oneCTTextBody = ctShape.getTxBody();
                    if (null == oneCTTextBody) {
                        continue;
                    }
                    CTTextParagraph[] oneCTTextParagraph = oneCTTextBody.getPArray();
                    CTTextFont oneCTTextFont = null;
                    try {
                        oneCTTextFont = CTTextFont.Factory.parse(xmlFontFormat);
                    } catch (XmlException e) {

                    }
                    if (oneCTTextFont == null) {
                        continue;
                    }
                    for (CTTextParagraph ctTextParagraph : oneCTTextParagraph) {
                        CTRegularTextRun[] onrCTRegularTextRunArray = ctTextParagraph.getRArray();
                        for (CTRegularTextRun ctRegularTextRun : onrCTRegularTextRunArray) {
                            CTTextCharacterProperties oneCTTextCharacterProperties = ctRegularTextRun.getRPr();
                            oneCTTextCharacterProperties.setLatin(oneCTTextFont);
                        }
                    }
                }

                for(XSLFShape shape : pptPageXSLFSLiseList.get(i).getShapes() ){
                    if (shape instanceof XSLFTextShape){
                        XSLFTextShape txtshape = (XSLFTextShape)shape ;
                        for ( XSLFTextParagraph textPara : txtshape.getTextParagraphs() ){
                            List<XSLFTextRun> textRunList = textPara.getTextRuns();
                            for(XSLFTextRun textRun: textRunList) {
                                textRun.setFontFamily("simsun");
                            }
                        }
                    }
                }
                String imgName = pre + "_" + (i + 1) + "." + imageSuffix;
                //生成图片
                boolean res = genImage(oneSlideShow.getPageSize(), pptPageXSLFSLiseList.get(i), imgName, imageSuffix);
                if (res) {
                    imageList.add(HOST + IMAGE_PREFIX + "/" + imgName);
                }
            }
        } catch (Exception e) {
            logger.warn("conver to image error. ", e);
        }
        return imageList;
    }

    public List<String> converPPTtoImage(String pptFile, String imageSuffix) {
        List<String> imageList = new ArrayList<>();
        try (InputStream pptStream = getRemotePPTStream(pptFile); HSLFSlideShow oneSlideShow = new HSLFSlideShow(pptStream)) {
            String pre = RandomUtils.nextObjectId();
            List<HSLFSlide> pptPageXSLFSLiseList = oneSlideShow.getSlides();
            for (int i = 0; i < pptPageXSLFSLiseList.size(); i++) {
                //设置字体，解决中文乱码问题
                for (List<HSLFTextParagraph> list : pptPageXSLFSLiseList.get(i).getTextParagraphs()) {
                    for (HSLFTextParagraph hslfTextParagraph : list) {
                        for (HSLFTextRun textRun : hslfTextParagraph.getTextRuns()) {
                            Double size = textRun.getFontSize();
                            if ((size <= 0) || (size >= 26040)) {
                                textRun.setFontSize(20.0);
                            }
                            textRun.setFontFamily("simsun");
                        }
                    }
                }

                String imgName = pre + "_" + (i + 1) + "." + imageSuffix;
                //生成图片
                boolean res = genImage(oneSlideShow.getPageSize(), pptPageXSLFSLiseList.get(i), imgName, imageSuffix);
                if (res) {
                    imageList.add(HOST + IMAGE_PREFIX + "/" + imgName);
                }
            }
        } catch (Exception e) {
            logger.warn("conver to image error. ", e);
        }
        return imageList;
    }

    private InputStream getRemotePPTStream(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(300 * 1000);
        return conn.getInputStream();
    }

    private  boolean genImage(Dimension dimension, Sheet sheet, String imgName, String imageSuffix) {
        try (ByteArrayOutputStream imageOut = new ByteArrayOutputStream()) {
            BufferedImage oneBufferedImage = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D oneGraphics2D = oneBufferedImage.createGraphics();
            sheet.draw(oneGraphics2D);
            ImageIO.write(oneBufferedImage, imageSuffix, imageOut);
            storageClient.upload(new ByteArrayInputStream(imageOut.toByteArray()), imgName, IMAGE_PREFIX);
            return true;
        } catch (Exception e) {
            logger.warn("conver to image error. ", e);
            return false;
        }
    }

//    public static void main(String[] args) {
//        converPPTtoImage("https://oss-data.17zuoye.com/teacher/courseware/test2018/03/17/20180317132138160616.ppt", "C", "png");
//    }
}