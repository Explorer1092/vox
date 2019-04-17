package com.voxlearning.washington.controller.common;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.FileOssManageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 上传、下载文件
 *
 * @author Wenlong Meng
 * @since Jan 18, 2019
 */
@Slf4j
@Controller
@RequestMapping("/common/updownLoad")
public class UpDownLoadController extends AbstractController {

    //Logic

    /**
     * 批量上传图片文件
     *
     * @param contents 文件内容
     * @return
     */
    @RequestMapping(value = "/uploadImgs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploads(List<String> contents){
        try {
            List<String> filePathTemps = contents.stream().map(content ->{
                byte[] filebyte = Base64.decodeBase64(content);
                return FileOssManageUtils.host() + FileOssManageUtils.upload(filebyte, "jpg");
            }).collect(Collectors.toList());
            return MapMessage.successMessage().add("urls",filePathTemps);
        } catch (Exception ex) {
            logger.error("UpDownLoadController.uploadImgs.error", ex);
            return MapMessage.errorMessage("info","操作失败");
        }
    }

    /**
     * 转化img到pdf
     *
     * @param contents 文件内容
     * @param filePaths 中间文件路径，多次调用时，最后一次把每次
     * @param endflag 是否完成
     * @return
     */
    @RequestMapping(value = "/batch/imgs2pdf.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchImgs2pdf(List<String> contents,@RequestParam(required = false)List<String> filePaths,@RequestParam(required = false, defaultValue = "true") Boolean endflag){
        try {
            if(endflag){
                List<Image> images = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(filePaths)){
                    images.addAll(paths2Images(filePaths));
                }

                images.addAll(contents2Images(contents));

                return imgs2PDF(images);
            }else{
                List<String> filePathTemps = contents.stream().map(content ->{
                    byte[] filebyte = Base64.decodeBase64(content);
                    return FileOssManageUtils.upload(filebyte, "jpg");
                }).collect(Collectors.toList());
                return MapMessage.successMessage().add("filePaths",filePathTemps);
            }
        } catch (Exception ex) {
            logger.error("UpDownLoadController.img2pdf.error({},{})",filePaths, endflag, ex);
            return MapMessage.errorMessage("info","操作失败");
        }
    }

    /**
     * 转化img到pdf
     *
     * @param contents 文件内容
     * @return
     */
    @RequestMapping(value = "/imgs2pdf.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage imgs2pdf(List<String> contents){
        if(ObjectUtils.anyBlank(contents)){
            return MapMessage.errorMessage("内容为空");
        }
        LoggerUtils.info("UpDownLoadController.imgs2pdf", contents.size());
        try {
            List<Image> images = contents2Images(contents);
            return imgs2PDF(images);
        } catch (Exception ex) {
            logger.error("UpDownLoadController.img2pdf.error", ex);
            return MapMessage.errorMessage("info","操作失败");
        }
    }

    /**
     * 路径转图片
     *
     * @param paths
     * @return
     */
    private List<Image> paths2Images(List<String> paths){
        return paths.stream().map(p->{
            String filePath = FileOssManageUtils.host() + p;
            try {
                return Image.getInstance(filePath);
            }catch (Exception e){
                log.error("UpDownLoadController.batchImgs2pdf.filePath:{}", filePath);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * base64内容转图片
     *
     * @param contents
     * @return
     */
    private List<Image> contents2Images(List<String> contents){
        return contents.stream().map(content ->{
            try {
                return Image.getInstance(Base64.decodeBase64(content));
            }catch (Exception e){
                log.error("UpDownLoadController.imgs2pdf.content.error", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 图片转pdf
     *
     * @param images
     * @return
     */
    private MapMessage imgs2PDF(List<Image> images){
        Document document = new Document();;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            document.setMargins(0, 0, 0, 0);
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();
            images.stream().forEach(img ->{
                try {
                    img.setAlignment(Image.ALIGN_CENTER);
                    // 根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
                    img.scalePercent(50);
                    document.setPageSize(new Rectangle(img.getWidth() / 2f, img.getHeight() / 2f));
                    document.newPage();
                    document.add(img);
                }catch (Exception e){
                    log.error("UpDownLoadController.imgs2PDF.error", e);
                }
            });
            document.close();
            String url = FileOssManageUtils.upload(byteArrayOutputStream.toByteArray(),"pdf");
            return MapMessage.successMessage().add("url",FileOssManageUtils.host() + url);
        } catch (Exception ex) {
            logger.error("UpDownLoadController.imgs2PDF.error", ex);
            return MapMessage.errorMessage("info","操作失败");
        }finally {
            if(document != null && document.isOpen()){
                document.close();
            }
        }
    }

}
