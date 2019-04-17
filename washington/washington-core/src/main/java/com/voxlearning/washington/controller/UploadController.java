/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

// $Id: UploadController.java 16020 2013-01-16 05:33:50Z xiaohai.zhang $
package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.utopia.api.constant.GridFileType;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.business.api.entity.GridFileTag;
import com.voxlearning.utopia.service.newexam.api.client.NewExamStorageServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.storage.api.client.StorageServiceClient;
import com.voxlearning.washington.mapper.GridFileTagMapper;
import com.voxlearning.washington.support.AbstractController;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/uploadfile")
public class UploadController extends AbstractController {

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-scquestion")
    private StorageClient fsScquestion;

    @Inject private NewExamStorageServiceClient newExamStorageServiceClient;
    @Inject private StorageServiceClient storageServiceClient;

    @RequestMapping(value = "avatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage avatar(HttpServletRequest request) {
        return MapMessage.errorMessage("改头像功能暂停使用了哦");
        // return updateUserAvatar(currentUser(), request.getParameter("filedata"));
    }

    @RequestMapping(value = "voice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String voice(HttpServletRequest request) {
        String filename = request.getParameter("filename");
        if (!StringUtils.startsWith(filename, "voice-result-")) {
            logger.error("upload voice failed: bad filename");
            return "failure";
        }
        filename = voiceUploader.uploadVoiceFromHttpServletRequest(request, filename);
        return filename != null ? "success" : "failure";
    }

    @RequestMapping(value = "teacherresource.vpage", method = RequestMethod.POST)
    @ResponseBody
    // 注意！ SWFUpload 发过来的是 Accept: text/* ，所以我们只能提供编码后的json
    public String teacherresource(MultipartFile filedata,
                                  @RequestParam(value = "t", required = false, defaultValue = "0") int type
    ) {
        if (filedata.isEmpty()) {
            return JsonUtils.toJson(MapMessage.errorMessage("没有文件上传"));
        }

        String originalFileName = filedata.getOriginalFilename();
        GridFileType gridFileType = GridFileType.of(type);
        if (gridFileType == null) {
            return JsonUtils.toJson(MapMessage.errorMessage("Unrecognized grid file type"));
        }
        try {
            @Cleanup InputStream inStream = filedata.getInputStream();
            String fileId = teacherResourceUploader.uploadTeacherResource(currentUserId(), originalFileName, gridFileType, inStream);
            return JsonUtils.toJson(MapMessage.successMessage().add("fileId", fileId).add("userId", currentUserId()));
        } catch (Exception ex) {
            return JsonUtils.toJson(MapMessage.errorMessage("不能上传文件:" + ex.getMessage()));
        }
    }

    @RequestMapping(value = "addtag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map addTagToResource(@RequestBody GridFileTagMapper tagMapper) {
        for (String id : tagMapper.getFileIdList()) {
            GridFileTag tag = tagMapper.toGridFileTag();
            tag.setUserId(currentUserId());
            tag.setGfsId(id);

            //如果上传的试卷已存在，则做一下标记
            if (tag.getFileType() == GridFileType.ENGLISH_EXAMINATION_PAPER
                    || tag.getFileType() == GridFileType.MATHEMATICS_EXAMINATION_PAPER
                    || tag.getFileType() == GridFileType.ENGLISH_SPOKEN_LANGUAGE_PAPER) {
                if (newExamStorageServiceClient.getNewExamStorageService()
                        .existByBookIdCountyCodeGridFileTypePaperTypeYear(tag)) {
                    Map<String, Object> map = tag.getExtensionAttribute();
                    if (null == map) {
                        map = new HashMap<>();
                    }
                    map.put("unique", "false");
                    tag.setExtensionAttribute(map);
                }
            }
            storageServiceClient.getStorageService()
                    .insertGridFileTag(tag)
                    .awaitUninterruptibly();
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mdcardphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mothersDayCardPhoto(HttpServletRequest request) {
        String filedata = request.getParameter("filedata");
        if (StringUtils.isEmpty(filedata)) {
            return MapMessage.errorMessage();
        }
        User student = currentStudent();
        if (!User.isStudentUser(student)) {
            return MapMessage.errorMessage();
        }
        try {
            String gfsId = RandomUtils.nextObjectId();
            String filename = mothersDayCardPhotoUploader.upload(student.getId(), gfsId, filedata);
            if (filename == null) {
                logger.warn("User '{}' failed to upload mdc image", student.getId());
                return MapMessage.errorMessage();
            }
            return MapMessage.successMessage().add("filename", filename);
        } catch (Exception ex) {
            logger.warn("Upload monthers day card photo failed. studentId {}", ex);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "deletemdcphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String deleteMothersDayCardPhoto(@RequestParam("filename") String filename) {
        if (StringUtils.isEmpty(filename)) {
            return JsonUtils.toJson(MapMessage.errorMessage("文件名不能为空"));
        }
        try {
            mothersDayCardPhotoUploader.delete(filename);
            return JsonUtils.toJson(MapMessage.successMessage("删除成功"));
        } catch (Exception ex) {
            return JsonUtils.toJson(MapMessage.errorMessage("删除失败:" + ex.getMessage()));
        }
    }

    @RequestMapping(value = "clazzjournalphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String clazzJournalPhoto(MultipartFile filedata) {
        if (filedata.isEmpty()) {
            return JsonUtils.toJson(MapMessage.errorMessage("没有文件上传"));
        }
        String originalFileName = filedata.getOriginalFilename();
        try {
            @Cleanup InputStream inStream = filedata.getInputStream();
            String filename = clazzJournalPhotoUploader.uploadClazzJournalImageFromFileData(currentUserId(), originalFileName, inStream);
            return JsonUtils.toJson(MapMessage.successMessage().add("filename", filename));
        } catch (Exception ex) {
            return JsonUtils.toJson(MapMessage.errorMessage("不能上传文件:" + ex.getMessage()));
        }
    }

    @RequestMapping(value = "deletephoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String deletePhoto(@RequestParam("filename") String filename) {
        if (filename == null || filename.equals("")) {
            return JsonUtils.toJson(MapMessage.errorMessage("文件名不能为空"));
        }
        try {
            String[] fileNames = filename.split(",");
            for (String name : fileNames) {
                clazzJournalPhotoUploader.deletePhotoByFilename(name);
            }
            return JsonUtils.toJson(MapMessage.successMessage("删除成功"));
        } catch (Exception ex) {
            return JsonUtils.toJson(MapMessage.errorMessage("删除失败:" + ex.getMessage()));
        }
    }

    @RequestMapping(value = "teacherreading.vpage", method = RequestMethod.POST)
    @ResponseBody
    // 注意！ SWFUpload 发过来的是 Accept: text/* ，所以我们只能提供编码后的json
    public MapMessage teacherReading(MultipartFile filedata) {
        if (filedata.isEmpty()) {
            logger.warn("Upload readingFile: user id is null or filedata is blank");
            return MapMessage.errorMessage();
        }
        String originalFileName = filedata.getOriginalFilename();
        long fileSize = filedata.getSize();
        if (fileSize > 10240000L) {
            logger.warn("Upload readingFile:  file size too long");
            return MapMessage.errorMessage();
        }
        try {
            @Cleanup InputStream inStream = filedata.getInputStream();
            String fileName = teacherResourceUploader.uploadTeacherReadingUgc(originalFileName, inStream);
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.warn("Upload readingFile: failed writing into mongo gfs", ex);
            return MapMessage.errorMessage().add("errorInfo", ex);
        }
    }

    @RequestMapping(value = "teacherreadingcover.vpage", method = RequestMethod.POST)
    @ResponseBody
    protected MapMessage updateReadingcover(HttpServletRequest request) {
        Long userId = currentUserId();
        String coverData = request.getParameter("filedata");
        if (userId == null || StringUtils.isEmpty(coverData)) {
            logger.warn("Upload Readingcover: user id is null or filedata is blank");
            return MapMessage.errorMessage();
        }
        try {
            String fileName = teacherResourceUploader.uploadReadingCoverFromFiledata(coverData);
            return MapMessage.successMessage().add("fileName", fileName);

        } catch (Exception ex) {
            logger.warn("Upload Readingcover: failed writing into mongo gfs", ex);
            return MapMessage.errorMessage().add("errorInfo", ex);
        }

    }


    /**
     * UEditor 获取后台配置
     * KEY值不能随意更改
     */
    @RequestMapping(value = "uploadueditorimg.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage uploadUEditorConfig() {
        MapMessage mapMessage = MapMessage.successMessage();
        /* 上传图片配置项 */
        /* 执行上传图片的action名称 */
        mapMessage.add("imageActionName", "uploadimage");
        /* 提交的图片表单名称 */
        mapMessage.add("imageFieldName", "fileData");
        /* 上传大小限制，单位B */
        mapMessage.add("imageMaxSize", "1024000");
        /* 上传图片格式显示 */
        mapMessage.add("imageAllowFiles", new String[]{".png", ".jpg", ".jpeg", ".gif", ".bmp"});
        /* 是否压缩图片,默认是true */
        mapMessage.add("imageCompressEnable", true);
        /* 图片压缩最长边限制 */
        mapMessage.add("imageCompressBorder", 1600);
        /* 图片访问路径前缀 */
        String urlPrefix = "//cdn.test.17zuoye.net";
        if (RuntimeMode.ge(Mode.STAGING)) {
            urlPrefix = "//cdn.17zuoye.com";
        }
        mapMessage.add("imageUrlPrefix", StringUtils.join(urlPrefix, "/fs-scquestion/"));
        return mapMessage;
    }


    /**
     * UEditor上传图片
     */
    @RequestMapping(value = "uploadueditorimg.vpage", method = RequestMethod.POST)
    public String uploadUEditorImg(MultipartFile fileData, Model model) {
        Teacher teacher = currentTeacher();
        String state = "SUCCESS";
        String gfsId = RandomUtils.nextObjectId();
        String fileName = "smartclazz-img-" + teacher.getSubject() + "-" + gfsId;
        String originalFileName = null;
        String contentType = "image/jpeg";

        try {
            if (fileData.isEmpty()) {
                model.addAttribute("message", JsonUtils.toJson(MapMessage.errorMessage("Upload image: no content")));
                return "teacherv3/smartclazz/iwen/htmlchip/process";
            }
            originalFileName = fileData.getOriginalFilename();

            String ext = StringUtils.substringAfterLast(originalFileName, ".");
            ext = StringUtils.defaultString(ext).trim().toLowerCase();
            SupportedFileType fileType;
            try {
                fileType = SupportedFileType.valueOf(ext);
                contentType = fileType.getContentType();
            } catch (Exception ex) {
                model.addAttribute("message", JsonUtils.toJson(MapMessage.errorMessage("格式不对").add("state", "FAIL")
                        .add("original", originalFileName).add("url", fileName)));
                return "teacherv3/smartclazz/iwen/htmlchip/process";
            }

            @Cleanup InputStream inStream = fileData.getInputStream();
            StorageMetadata metadata = new StorageMetadata();
            metadata.setContentType(StringUtils.defaultString(contentType, "image/jpeg"));
            fsScquestion.uploadWithId(inStream, gfsId, fileName, null, metadata);

            //上传成功后，前端直接显示，报访问不到图片,前端使用第三方插件ueditor,无法修改前端逻辑代码,延迟800ms
            ThreadUtils.sleepCurrentThread(800);
        } catch (Exception ex) {
            //logger.error("上传编辑图片失败", ex);
            state = ex.getMessage();
        }
        MapMessage mapMessage = MapMessage.successMessage("上传成功");
        //因前端UEeditor内部使用url 和 state 来取文件名，不能随意修改
        mapMessage.add("original", originalFileName).add("url", fileName)
                .add("state", state).add("ContentType", contentType);
        model.addAttribute("message", JsonUtils.toJson(mapMessage));
        return "teacherv3/smartclazz/iwen/htmlchip/process";
    }
}
