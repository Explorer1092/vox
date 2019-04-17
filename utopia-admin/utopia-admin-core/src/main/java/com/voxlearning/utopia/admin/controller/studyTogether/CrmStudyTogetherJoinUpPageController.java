package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherJoinPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/6/26
 */
@Controller
@RequestMapping("opmanager/studyTogether")
@Slf4j
public class CrmStudyTogetherJoinUpPageController extends AbstractStudyTogetherController {

    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient imgStorageClient;


    /**
     * 进入列表
     */
    @RequestMapping(value = "/joinUpPageList.vpage", method = RequestMethod.GET)
    public String joinUpPageList(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<StudyTogetherJoinPage> joinPageListContent = crmStudyTogetherService.getJoinPageListContent(pageRequest);
        model.addAttribute("content", joinPageListContent.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", joinPageListContent.getTotalPages());
        model.addAttribute("hasPrev", joinPageListContent.hasPrevious());
        model.addAttribute("hasNext", joinPageListContent.hasNext());
        return "opmanager/studyTogether/joinUpPageList";
    }


    /**
     * 进入详情页
     */
    @RequestMapping(value = "/joinUpPageDetail.vpage", method = RequestMethod.GET)
    public String joinUpPageDetail(Model model) {
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(lessonId)) {
            model.addAttribute("content", new StudyTogetherJoinPage());
            return "opmanager/studyTogether/joinUpPageDetail";
        }
        StudyTogetherJoinPage joinPageByLessonId = crmStudyTogetherService.getJoinPageByLessonId(lessonId);
        if (joinPageByLessonId == null) {
            model.addAttribute("content", new StudyTogetherJoinPage());
            return "opmanager/studyTogether/joinUpPageDetail";
        }
        model.addAttribute("headImgFile", joinPageByLessonId.getHeadImg());
        model.addAttribute("bannerBackgroundImgFile", joinPageByLessonId.getBannerBackgroundImg());
        model.addAttribute("contentImgFile", joinPageByLessonId.getLessonContentButtonImg());
        joinPageByLessonId.setHeadImg(getOssImgUrl(joinPageByLessonId.getHeadImg()));
        joinPageByLessonId.setLessonContentButtonImg(getOssImgUrl(joinPageByLessonId.getLessonContentButtonImg()));
        model.addAttribute("content", joinPageByLessonId);
        return "opmanager/studyTogether/joinUpPageDetail";
    }

    /**
     * 保存页面详细信息
     */
    @RequestMapping(value = "/saveJoinUpPageDetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveDetail() {
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("lessonId不能为空");
        }
        String bgColor = getRequestString("bgColor");
        String headImg = getRequestString("headImg");
        String firstContent = getRequestString("firstContent");
        String secondContent = getRequestString("secondContent");
        String lessonContent = getRequestString("lessonContent");
        String lessonContentImg = getRequestString("lessonContentImg");
        String bannerBackgroundImg = getRequestString("bannerBackgroundImg");
        String buttonColor = getRequestString("buttonColor");
        StudyTogetherJoinPage joinPageByLessonId = crmStudyTogetherService.getJoinPageByLessonId(lessonId);
        if (joinPageByLessonId != null) {
            joinPageByLessonId.setBgColor(bgColor);
            joinPageByLessonId.setHeadImg(headImg);
            joinPageByLessonId.setFirstContent(firstContent);
            joinPageByLessonId.setLessonContent(lessonContent);
            joinPageByLessonId.setLessonContentButtonImg(lessonContentImg);
            joinPageByLessonId.setSecondContent(secondContent);
            joinPageByLessonId.setButtonColor(buttonColor);
            joinPageByLessonId.setBannerBackgroundImg(bannerBackgroundImg);
        } else {
            joinPageByLessonId = new StudyTogetherJoinPage();
            joinPageByLessonId.setLessonId(lessonId);
            joinPageByLessonId.setBgColor(bgColor);
            joinPageByLessonId.setHeadImg(headImg);
            joinPageByLessonId.setFirstContent(firstContent);
            joinPageByLessonId.setLessonContent(lessonContent);
            joinPageByLessonId.setLessonContentButtonImg(lessonContentImg);
            joinPageByLessonId.setSecondContent(secondContent);
            joinPageByLessonId.setButtonColor(buttonColor);
            joinPageByLessonId.setBannerBackgroundImg(bannerBackgroundImg);
        }
        StudyTogetherJoinPage studyTogetherJoinPage = crmStudyTogetherService.upsertJoinPage(joinPageByLessonId);
        if (studyTogetherJoinPage != null) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    /**
     * 上传图片
     */
    @RequestMapping(value = "/uploadBgImg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadBgImg(MultipartFile inputFile) throws IOException {
        return uploadImg(inputFile);
    }


    @RequestMapping(value = "/ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorcontroller() throws IOException {
        String action = getRequestString("action");
        switch (action) {
            case "config":
                return MapMessage.successMessage()
                        .add("imageActionName", "uploadimage")
                        .add("imageFieldName", "upfile")
                        .add("imageInsertAlign", "none")
                        .add("imageMaxSize", 2048000)
                        .add("imageUrlPrefix", "");
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    MapMessage mapMessage = uploadImg(imgFile);
                    if (!mapMessage.isSuccess()) {
                        return mapMessage;
                    }
                    return MapMessage.successMessage()
                            .add("url", mapMessage.get("imgUrl"))
                            .add("title", mapMessage.get("imgName"))
                            .add("state", "SUCCESS")
                            .add("original", originalFileName);
                } catch (Exception ex) {
                    logger.error("上传图片异常： " + ex.getMessage(), ex);
                    return MapMessage.errorMessage("上传图片异常： " + ex.getMessage());
                }
            default:
                return MapMessage.successMessage();
        }
    }


    @RequestMapping(value = "/check_lesson.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage checkLesson() {
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("课程id不能为空");
        }
        List<String> lessonIds = getAllLessonId();
        if (!lessonIds.contains(lessonId)) {
            return MapMessage.errorMessage("错误的课程id");
        }
        return MapMessage.successMessage();
    }


    private MapMessage uploadImg(MultipartFile inputFile) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = "studyTogetherJoinUpImg/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "studyTogetherJoinUpImg/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String realName = imgStorageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("imgName", realName).add("imgUrl", fileUrl);
    }

    private String getOssImgUrl(String relativeUrl) {
        if (StringUtils.isBlank(relativeUrl)) {
            return "";
        }
        return ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host") + relativeUrl;
    }
}
