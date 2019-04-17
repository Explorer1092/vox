package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.support.upload.OSSManageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.REQ_HOMEWORK_ID;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FAIL_MSG;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG;

/**
 * @author shiwe.liao
 * @since 2016/4/18
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/user/file/")
public class UserFileApiController extends AbstractApiController {

    public static final String defaultFolder = "app";

    /**
     * 通用APP上传文件
     */
    @RequestMapping(value = "upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upload() {
        MapMessage resultMap = new MapMessage();
        try {
            if(StringUtils.isNoneBlank(getRequestString(REQ_ACTIVITY)))
                validateRequest(REQ_FILE_INFO,REQ_ACTIVITY);
            else
                validateRequest(REQ_FILE_INFO);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String activityName = getRequestString(REQ_ACTIVITY);

        String fileInfo = getRequestString(REQ_FILE_INFO);
        List<Map> fileInfoList = JsonUtils.fromJsonToList(fileInfo, Map.class);
        if (CollectionUtils.isEmpty(fileInfoList)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
            return resultMap;
        }

        Long userId = getApiRequestUser().getId();

        // 上传文件
        Map<String, List<String>> fileMap = new HashMap<>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            List<MultipartFile> files = multipartRequest.getFiles(REQ_FILES);
            if (CollectionUtils.isNotEmpty(files)) {
                fileMap = atomicLockManager.wrapAtomic(this)
                        .keys(userId)
                        .proxy()
                        .duUploadResults(userId, files, fileInfoList,activityName);
            }
        } catch (DuplicatedOperationException ex) {
            logger.warn("Upload file writing (DUPLICATED OPERATION): (userId={})", userId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (RuntimeException ex) {
            logger.warn("Upload file failed writing: (userId={})", userId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, ex.getMessage());
            return resultMap;
        } catch (Exception ex) {
            logger.warn("Upload file failed writing: (userId={})", userId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
            return resultMap;
        }

        resultMap.add(RES_FILES, fileMap);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * APP上传头像文件
     */
    @RequestMapping(value = "uploadAvatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadAvatar() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_FILE_INFO);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User apiUser = getApiRequestUser();
        if (apiUser.isStudent()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(apiUser.getId());
            if (studentDetail.isInfantStudent()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "“海老师为保护孩子们隐私，升级了头像功能，停止自主上传。升级新版本后会有更多头像让大家选择。");
                return resultMap;
            } else if (studentDetail.isPrimaryStudent() && !userLevelLoader.hasPrivilegeForUploadAvatar(Collections.singleton(apiUser.getId()))) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "家庭等级达到3级及以上时可自主修改头像哦");
                return resultMap;
            } else if (studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "暂时无法修改头像哦");
                return resultMap;
            }
        } else if (apiUser.isParent()) {
            List<StudentParentRef> studentRefs = parentLoaderClient.loadParentStudentRefs(apiUser.getId());
            Set<Long> studentIds = studentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            if (!userLevelLoader.hasPrivilegeForUploadAvatar(studentIds)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "家庭等级达到3级及以上时可自主修改头像哦");
                return resultMap;
            }
        }

        try {

            String fileInfo = getRequestString(REQ_FILE_INFO);
            List<Map> fileInfoList = JsonUtils.fromJsonToList(fileInfo, Map.class);
            if (CollectionUtils.isEmpty(fileInfoList)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
                return resultMap;
            }

            // 上传文件
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            List<MultipartFile> files = multipartRequest.getFiles(REQ_FILES);
            if (CollectionUtils.isEmpty(files) || files.size() != 1) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
            }

            MultipartFile avatarFile = files.get(0);
            String imgFile = userImageUploader.uploadAvatarFromMultipartFile(apiUser.getId(), avatarFile);
            if (StringUtils.isBlank(imgFile)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
            }

            userServiceClient.userImageUploaded(apiUser.getId(), imgFile, imgFile.replace(".jpg", "").replace(".png", ""));
            Map<String, Object> filesMap = new HashMap<>();
            filesMap.put("img", Arrays.asList(imgFile));
            resultMap.add(RES_FILES, filesMap);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;

        } catch (DuplicatedOperationException ex) {
            logger.warn("Upload file writing (DUPLICATED OPERATION): (userId={})", apiUser.getId(), ex);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (Exception ex) {
            logger.warn("Upload file failed writing: (userId={})", apiUser.getId(), ex);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
            return resultMap;
        }
    }

    /**
     * 多文件上传
     *
     * @param files        文件s
     * @param fileInfoList 文件json
     * @return map
     * @throws Exception
     */
    public Map<String, List<String>> duUploadResults(Long userId, List<MultipartFile> files, List<Map> fileInfoList,String activityName) {
        Map<String, List<String>> fileMap = new HashMap<>();
        boolean result = true;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Map infoMap = fileInfoList.get(i);
            String type = (String) infoMap.get("type");
            if (!StringUtils.equalsIgnoreCase(activityName,"dubbing") && (file.getSize() != Long.valueOf((Integer) infoMap.get("size")) || NewHomeworkQuestionFile.FileType.of(type) == NewHomeworkQuestionFile.FileType.UNKNOWN)) {
                result = false;
                break;
            }

            String fileName = doUploadResult(userId, file,activityName);
            if (fileMap.get(type) == null) {
                List<String> resourceList = new ArrayList<>();
                resourceList.add(fileName);
                fileMap.put(type, resourceList);
            } else {
                fileMap.get(type).add(fileName);
            }
        }
        if (!result) {
            throw new RuntimeException(RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG);
        }
        return fileMap;
    }

    public String doUploadResult(Long userId, MultipartFile file,String activityName) {
        String result;
        if(StringUtils.isNotBlank(activityName) && "teacherday2016".equals(activityName)) {
            //教师节活动,有的上传图片没有后缀名。。。。现把教师节活动的图片,如果有没后缀名,加个jpg。
            String prefix = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            if (StringUtils.isBlank(prefix))
                prefix = "jpg";
            else
                prefix = null;
            return OSSManageUtils.upload(file, StringUtils.isBlank(activityName) ? defaultFolder : activityName, prefix);
        } else{
            return OSSManageUtils.upload(file, StringUtils.isBlank(activityName) ? defaultFolder : activityName, null);
        }
    }


    /**
     * 配音视频上传aliyun失败后则把合成好的音频mp3传到后端，由后端合并上传到aliyun
     * @return
     */
    @RequestMapping(value = "dubbing/upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage dubbingUpload() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_HOMEWORK_ID, REQ_DUBBING_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        Long userId = getApiRequestUser().getId();
        // 上传文件
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile file = multipartRequest.getFile(REQ_FILE);
            MapMessage mapMessage = atomicLockManager.wrapAtomic(this)
                    .keys(userId, homeworkId, dubbingId)
                    .proxy()
                    .uploadDubbing(userId, file, homeworkId, dubbingId);
            if(mapMessage.isSuccess()){
                resultMap.add(REQ_DUBBING_VIDEO_URL, mapMessage.get("ossVideoUrl"));
            }else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG + mapMessage.getInfo());
                return resultMap;
            }

        } catch (DuplicatedOperationException ex) {
            logger.warn("Upload file writing (DUPLICATED OPERATION): (userId={})", userId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (RuntimeException ex) {
            logger.warn("Upload file failed writing: (userId={})", userId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, ex.getMessage());
            return resultMap;
        } catch (Exception ex) {
            logger.warn("Upload file failed writing: (userId={})", userId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    public MapMessage uploadDubbing(Long userId, MultipartFile filedata, String homeworkId, String dubbingId) {
        if (filedata.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }
        try {
            return dubbingUploader.uploadDubbing(userId, filedata, homeworkId, dubbingId);
        } catch (Exception ex) {
            return MapMessage.errorMessage("不能上传文件:" + ex.getMessage());
        }
    }
}
