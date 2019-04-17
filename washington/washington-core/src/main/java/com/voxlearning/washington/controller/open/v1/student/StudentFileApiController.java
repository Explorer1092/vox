package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.support.upload.OSSManageUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.InputStream;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FAIL_MSG;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG;

/**
 * 文件上传
 *
 * @author xuesong.zhang
 * @since 2015-12-18
 */
@Controller
@RequestMapping(value = "/v1/student/file/")
@Slf4j
public class StudentFileApiController extends AbstractStudentApiController {


    /**
     * 学生端上传文件
     */
    @RequestMapping(value = "upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upload() {
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

        String fileInfo = getRequestString(REQ_FILE_INFO);
        List<Map> fileInfoList = JsonUtils.fromJsonToList(fileInfo, Map.class);
        if (CollectionUtils.isEmpty(fileInfoList)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
            return resultMap;
        }

        Long studentId = getCurrentStudent().getId();

        // 上传文件
        Map<String, List<String>> fileMap = new HashMap<>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            List<MultipartFile> files = multipartRequest.getFiles(REQ_FILES);
            if (CollectionUtils.isNotEmpty(files)) {
                fileMap = atomicLockManager.wrapAtomic(this)
                        .keys(studentId)
                        .proxy()
                        .duUploadResults(studentId, files, fileInfoList);
            }
        } catch (DuplicatedOperationException ex) {
            logger.warn("Upload file writing (DUPLICATED OPERATION): (studentId={})", studentId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (RuntimeException ex) {
            logger.warn("Upload file failed writing: (studentId={})", studentId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, ex.getMessage());
            return resultMap;
        } catch (Exception ex) {
            logger.warn("Upload file failed writing: (studentId={})", studentId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
            return resultMap;
        }

        resultMap.add(RES_FILES, fileMap);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 学生端上传纸质口算图片
     */
    @RequestMapping(value = "/ocrmental/upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadOcrMentalImage() {
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

        String fileInfo = getRequestString(REQ_FILE_INFO);
        List<Map> fileInfoList = JsonUtils.fromJsonToList(fileInfo, Map.class);
        if (CollectionUtils.isEmpty(fileInfoList)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
            return resultMap;
        }

        Long studentId = getCurrentStudent().getId();
        String sys = getRequestString(REQ_SYS);
        String uuid = getRequestString(REQ_UUID);

        // 上传文件
        Map<String, List<String>> fileMap = new HashMap<>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            List<MultipartFile> files = multipartRequest.getFiles(REQ_FILES);
            if (CollectionUtils.isNotEmpty(files)) {
                fileMap = atomicLockManager.wrapAtomic(this)
                        .keys(studentId)
                        .proxy()
                        .doUploadImages(studentId, sys, uuid, files, fileInfoList);
            }
        } catch (DuplicatedOperationException ex) {
            logger.warn("Upload file writing (DUPLICATED OPERATION): (studentId={})", studentId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (RuntimeException ex) {
            logger.warn("Upload file failed writing: (studentId={})", studentId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, ex.getMessage());
            return resultMap;
        } catch (Exception ex) {
            logger.warn("Upload file failed writing: (studentId={})", studentId, ex.getMessage());
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
            return resultMap;
        }

        resultMap.add(RES_FILES, fileMap);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 多文件上传
     *
     * @param files        文件s
     * @param fileInfoList 文件json
     * @return map
     */
    public Map<String, List<String>> doUploadImages(Long userId, String sys, String uuid, List<MultipartFile> files, List<Map> fileInfoList) {
        Map<String, List<String>> fileMap = new HashMap<>();
        boolean result = true;
        List<String> imageUrls = new ArrayList<>();

        Map<String, String> headers = new HashMap<>();
        headers.put("appkey", "c7f8aeb8c76047838e8f3e404587c7ff");
        headers.put("device-id", uuid);
        headers.put("sys", sys);
        headers.put("protocol", "http");

        String uploadUrl = NewHomeworkConstants.OCR_MENTAL_IMAGE_UPLOAD_URL_TEST;
        String computeUrl = NewHomeworkConstants.OCR_MENTAL_IMAGE_COMPUTE_URL_TEST;
        if (RuntimeMode.isUsingProductionData()) {
            uploadUrl = NewHomeworkConstants.OCR_MENTAL_IMAGE_UPLOAD_URL_PRODUCT;
            computeUrl = NewHomeworkConstants.OCR_MENTAL_IMAGE_COMPUTE_URL_PRODUCT;
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Map infoMap = fileInfoList.get(i);
            String type = (String) infoMap.get("type");
            if (file.getSize() != Long.valueOf((Integer) infoMap.get("size")) || NewHomeworkQuestionFile.FileType.of(type) == NewHomeworkQuestionFile.FileType.UNKNOWN) {
                result = false;
                break;
            }
            try {
                @Cleanup InputStream in = file.getInputStream();
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("image", in, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8));

                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                        .post(uploadUrl)
                        .headers(headers)
                        .entity(builder.build())
                        .execute();

                String responseResult = response.getResponseString();
                Map<String, Object> jsonResult = JsonUtils.fromJson(responseResult);
                if (MapUtils.isNotEmpty(jsonResult) && SafeConverter.toInt(jsonResult.get("code")) == 200) {
                    String imgUrl = SafeConverter.toString(jsonResult.get("imgurl"));
                    imageUrls.add(imgUrl);
                    if (fileMap.get(type) == null) {
                        List<String> resourceList = new ArrayList<>();
                        resourceList.add(imgUrl);
                        fileMap.put(type, resourceList);
                    } else {
                        fileMap.get(type).add(imgUrl);
                    }
                } else {
                    logger.warn("Upload image failed (studentId={} response=({}))", userId, responseResult);
                }
            } catch (Exception e) {
                logger.warn("Upload image failed (studentId={})", userId, e.getMessage());
            }
        }
        if (CollectionUtils.isNotEmpty(imageUrls)) {
            try {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody("user_name", "" + userId);
                builder.addTextBody("city", studentDetail != null ? SafeConverter.toString(studentDetail.getCityCode()) : "");
                builder.addTextBody("images", StringUtils.join(imageUrls, ";"));
                builder.addTextBody("data", DateUtils.dateToString(new Date()));
                builder.addTextBody("extra", JsonUtils.toJson(MapUtils.m("env", RuntimeMode.current())));

                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                        .post(computeUrl)
                        .headers(headers)
                        .entity(builder.build())
                        .execute();

                String responseResult = response.getResponseString();
                Map<String, Object> jsonResult = JsonUtils.fromJson(response.getResponseString());
                if (MapUtils.isNotEmpty(jsonResult) && SafeConverter.toInt(jsonResult.get("code")) == 200) {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", userId,
                            "images", StringUtils.join(imageUrls, ";")
                    ));
                } else {
                    result = false;
                    logger.warn("Compute image failed (studentId={} response=({}))", userId, responseResult);
                }
            } catch (Exception e) {
                logger.warn("Compute image failed (studentId={})", userId, e.getMessage());
            }
        } else {
            logger.warn("Upload image failed, empty imageUrls (studentId={})", userId);
        }
        if (!result) {
            throw new RuntimeException(RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG);
        }
        return fileMap;
    }

    /**
     * 多文件上传
     *
     * @param files        文件s
     * @param fileInfoList 文件json
     * @return map
     * @throws Exception
     */
    public Map<String, List<String>> duUploadResults(Long userId, List<MultipartFile> files, List<Map> fileInfoList) {
        Map<String, List<String>> fileMap = new HashMap<>();
        boolean result = true;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Map infoMap = fileInfoList.get(i);
            String type = (String) infoMap.get("type");
            if (file.getSize() != Long.valueOf((Integer) infoMap.get("size")) || NewHomeworkQuestionFile.FileType.of(type) == NewHomeworkQuestionFile.FileType.UNKNOWN) {
                result = false;
                break;
            }

            String fileName = doUploadResult(type, file);
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

    public String doUploadResult(String type, MultipartFile file) {
        return OSSManageUtils.uploadByType(type, file);
    }

}
