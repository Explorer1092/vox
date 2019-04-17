package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.FileUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.support.upload.UserImageUploader;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FAIL_MSG;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG;

/**
 * @author shiwe.liao
 * @since 2016-9-13
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/upload/")
public class ParentFileUploadApiV2Controller extends AbstractParentApiController {

    @Inject
    private UserImageUploader userImageUploader;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient pmcStorageClient;

    @RequestMapping(value = "do_upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadFile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_FILE_INFO, "上传文件");
            validateRequired(REQ_ACTIVITY, "业务模块");
            validateRequest(REQ_FILE_INFO, REQ_ACTIVITY);
        } catch (IllegalVendorUserException e) {
            resultMap.add(RES_RESULT, e.getCode());
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
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
            boolean result = true;
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                Map infoMap = fileInfoList.get(i);
                String type = (String) infoMap.get("type");
                if (file.getSize() != Long.valueOf((Integer) infoMap.get("size")) || NewHomeworkQuestionFile.FileType.of(type) == NewHomeworkQuestionFile.FileType.UNKNOWN) {
                    result = false;
                    break;
                }
                MapMessage mapMessage = doUploadResult(file, activityName);
                if (!mapMessage.isSuccess()) {
                    continue;
                }
                String fileUrl = SafeConverter.toString(mapMessage.get("fileUrl"));
                if (fileMap.get(type) == null) {
                    List<String> resourceList = new ArrayList<>();
                    resourceList.add(fileUrl);
                    fileMap.put(type, resourceList);
                } else {
                    fileMap.get(type).add(fileUrl);
                }
            }
            if (!result) {
                throw new RuntimeException(RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG);
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


    @RequestMapping(value = "do_upload_to_pmc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadFileToPmc() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_ACTIVITY, "业务模块");
            validateRequired(REQ_FILE, "上传文件");
            validateRequest(REQ_ACTIVITY, REQ_FILE);
        } catch (IllegalVendorUserException e) {
            resultMap.add(RES_RESULT, e.getCode());
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String activityName = getRequestString(REQ_ACTIVITY);
        String file = getRequestString(REQ_FILE);
        /*
         *2018-10-30：由于客户端给h5的通用上传图片的接口uploadImage使用的是传输文件base64的方式，
         *            所以这里的file传过来的base64需要转成byte[]。
         */
        if (StringUtils.isBlank(file)) {
            return failMessage("文件不能为空");
        }
        byte[] bytes = FileUtils.parseByteArrayFromFiledata(file);
        if (ArrayUtils.isEmpty(bytes)) {
            logger.warn("Upload image: no content");
            return null;
        }
        return doUploadResultToPmc(bytes, activityName);

    }

    @RequestMapping(value = "upload_avatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadAvatar() {
        return failMessage("头像服务升级中，敬请期待!");
//         #57353
//        return failMessage("暂时无法使用家长端修改头像哦");
//        User parent = getApiRequestUser();
//        if (parent == null) {
//            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
//        }
//        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
//        Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
//        Boolean uploadAvatar = userLevelLoader.hasPrivilegeForUploadAvatar(studentIds);
//        if (!uploadAvatar) {
//            return failMessage("家庭等级达到3级及以上时可自主修改头像哦");
//        }
//        String avatarDat = getRequestString(REQ_AVATAR_DAT);
//        try {
//            validateRequired(REQ_AVATAR_DAT, "头像");
//            validateRequest(REQ_AVATAR_DAT);
//        } catch (IllegalArgumentException e) {
//            return failMessage(e);
//        }
//        String gfsId = RandomUtils.nextObjectId();
//        //这里在C端孩子注册的时候传的userId其实是parentId。因为孩子还没注册。没有Id。
//        //后面有一个ObjectId用作唯一标识即可
//        String filename = userImageUploader.uploadImageFromFiledata(parent.getId(), gfsId, avatarDat);
//        if (StringUtils.isBlank(filename)) {
//            logger.warn("upload user avatar error {}", parent.getId());
//            return failMessage(RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG);
//        }
//        return successMessage().add(RES_FILE_NAME, filename).add(RES_USER_IMG_URL, getCdnBaseUrlAvatarWithSep() + "/gridfs/" +  filename).add(RES_USER_IMG_GFS_ID, gfsId);
    }

    private MapMessage doUploadResult(MultipartFile inputFile, String activityName) {
        if (inputFile == null || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage();
        }
        try {
            String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
            if (StringUtils.isBlank(suffix)) {
                suffix = "mp3";
            }
            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength(inputFile.getSize());
            String env = activityName + "/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = activityName + "/test/";
            }
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
            String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
            String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_homework_host")) + realName;
            return MapMessage.successMessage().add("fileUrl", fileUrl);
        } catch (Exception e) {
            logger.error("语音签字上传语音失败{}", e);
            return MapMessage.errorMessage("上传文件失败");
        }
    }

    private MapMessage doUploadResultToPmc(byte[] fileArray, String activityName) {
        if (ArrayUtils.isEmpty(fileArray) || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage();
        }

        try {
            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength(SafeConverter.toLong(fileArray.length));
            String env = activityName + "/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = activityName + "/test/";
            }
            @Cleanup ByteArrayInputStream inStream = new ByteArrayInputStream(fileArray);
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + ".jpg";
            String realName = pmcStorageClient.upload(inStream, fileName, path, storageMetadata);
            return successMessage().add("real_url", realName);
        } catch (Exception e) {
            logger.error("pmc上传文件失败{}", e);
            return failMessage("上传文件失败");
        }
    }
}
