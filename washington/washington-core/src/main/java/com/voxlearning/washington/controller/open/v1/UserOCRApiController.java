package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningService;
import com.voxlearning.galaxy.service.studyplanning.api.constant.StudyPlanningType;
import com.voxlearning.galaxy.service.studyplanning.api.data.StudyPlanningItemMapper;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningItem;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.parent.api.DPParentOCRLoader;
import com.voxlearning.utopia.service.parent.api.DPParentOCRService;
import com.voxlearning.utopia.service.parent.api.entity.ParentOCREntity;
import com.voxlearning.utopia.service.parent.api.mapper.parentphotopractice.ParentOCRConfigMapper;
import com.voxlearning.utopia.service.parent.api.mapper.parentphotopractice.ParentOCRMapper;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author wei.jiang
 * @since 2018/11/12
 */
@Controller
@RequestMapping(value = "/v1/user/ocr")
@Slf4j
public class UserOCRApiController extends AbstractParentApiController {

    @ImportService(interfaceClass = DPParentOCRService.class)
    private DPParentOCRService dpParentOCRService;
    @ImportService(interfaceClass = DPParentOCRLoader.class)
    private DPParentOCRLoader dpParentOCRLoader;
    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @ImportService(interfaceClass = StudyPlanningService.class)
    private StudyPlanningService studyPlanningService;

    private static final String USER_OCR_CLIENT_CONFIG_PAGE = "USER_OCR_CLIENT_CONFIG_PAGE";

    private static final String USER_OCR_CLIENT_CONFIG_KEY = "USER_OCR_CLIENT_CONFIG_KEY";


    /**
     * 保存识别数据
     */
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage save() {
        long sid = getRequestLong(REQ_STUDENT_ID);
        String imgUrl = getRequestString(REQ_USER_IMG_URL);
        String ocrEntity = getRequestString(REQ_PARENT_OCR_ENTITY);
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        String data = getRequestString(REQ_NEW_SDK_OCR_DATA);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            if (VersionUtil.checkVersionConfig("<2.8.0", version)) {
                validateRequired(REQ_USER_IMG_URL, "图片地址");
                validateRequired(REQ_PARENT_OCR_ENTITY, "识别内容");
                validateRequest(REQ_STUDENT_ID, REQ_USER_IMG_URL, REQ_PARENT_OCR_ENTITY);
            } else {
                validateRequired(REQ_NEW_SDK_OCR_DATA, "识别内容");
                validateRequest(REQ_STUDENT_ID, REQ_NEW_SDK_OCR_DATA);
            }

        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getApiRequestUser();
        if (!studentIsParentChildren(parent.getId(), sid)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        Map<String, Object> entityMap = JsonUtils.fromJson(ocrEntity);
        Map<String, Object> newEntityMap = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(entityMap) && MapUtils.isEmpty(newEntityMap)) {
            return failMessage(RES_PARENT_OCR_ENTITY_ERROR);
        }
        List<Map<String, Object>> entityList = new ArrayList<>();
        String clientType = "";
        String clientName = "";
        if (MapUtils.isNotEmpty(newEntityMap)) {
            entityList = (List<Map<String, Object>>) newEntityMap.get(REQ_NEW_SDK_OCR_DATA_DETAIL);
            clientType = SafeConverter.toString(newEntityMap.get(REQ_NEW_SDK_OCR_DATA_CLIENT_TYPE));
            clientName = SafeConverter.toString(newEntityMap.get(REQ_NEW_SDK_OCR_DATA_CLIENT_NAME));
        }
        //这块的json解析如果放在壳里有风险，一旦改了其中的字段，壳的老版本会无法使用。放在这还有可改的机会。
        //2.8.0兼容老版本的拍照结构
        MapMessage mapMessage;
        if (MapUtils.isNotEmpty(entityMap)) {
            ParentOCRMapper parentOCRMapper = new ParentOCRMapper();
            parentOCRMapper.setStudentId(sid);
            parentOCRMapper.setImgUrl(imgUrl);
            parentOCRMapper.setEntity(entityMap);
            parentOCRMapper.setParentId(parent.getId());
            parentOCRMapper.setIsNewStruct(Boolean.FALSE);
            mapMessage = dpParentOCRService.saveUserOCR(parentOCRMapper);
        } else {
            List<ParentOCRMapper> mappers = new ArrayList<>();
            for (Map entity : entityList) {
                ParentOCRMapper parentOCRMapper = new ParentOCRMapper();
                parentOCRMapper.setStudentId(sid);
                parentOCRMapper.setEntity(entity);
                parentOCRMapper.setParentId(parent.getId());
                parentOCRMapper.setIsNewStruct(Boolean.TRUE);
                parentOCRMapper.setClientType(clientType);
                parentOCRMapper.setClientName(clientName);
                mappers.add(parentOCRMapper);
            }
            mapMessage = dpParentOCRService.saveUserOCRForList(mappers);
        }

        if (mapMessage.isSuccess()) {
            StudyPlanningItemMapper itemMapper = new StudyPlanningItemMapper();
            itemMapper.setId(StudyPlanningItem.generateId(sid, StudyPlanningType.PAPER_MENTAL_ARITHMETIC.name(), ""));
            itemMapper.setType(StudyPlanningType.PAPER_MENTAL_ARITHMETIC.name());
            studyPlanningService.finishPlanning(sid, currentUserId(), itemMapper);
            return successMessage();
        }
        return failMessage(mapMessage.getInfo());
    }

    /**
     * 获取客户端相关信息
     */
    @RequestMapping(value = "ocr_client_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getOCRClientInfo() {
        try {
            if (hasSessionKey()) {
                validateRequest();
            } else {
                validateRequestNoSessionKey();
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e.getMessage());
        }
        ParentOCRConfigMapper parentOCRConfigMapper = pageBlockContentServiceClient.loadConfigObject(USER_OCR_CLIENT_CONFIG_PAGE, USER_OCR_CLIENT_CONFIG_KEY, ParentOCRConfigMapper.class);
        return successMessage()
                .add(RES_PARENT_OCR_USER_RECORD_URL, parentOCRConfigMapper.getUserRecordUrl())
                .add(RES_PARENT_OCR_AVAILABLE_QUESTION_URL, parentOCRConfigMapper.getAvailableQuestionUrl())
                .add(RES_PARENT_OCR_USER_SHARE_URL, parentOCRConfigMapper.getUserShareUrl())
                .add(RES_PARENT_OCR_RESULT_PAGE_URL, parentOCRConfigMapper.getResultPageUrl())
                .add(RES_PARENT_OCR_EXAMPLE_URL, parentOCRConfigMapper.getExampleUrl())
                .add(RES_PARENT_OCR_IMAGE_BRIGHT_TH, parentOCRConfigMapper.getImageBrightTh())
                .add(RES_PARENT_OCR_IMAGE_DARK_TH, parentOCRConfigMapper.getImageDarkTh())
                .add(RES_PARENT_OCR_IMAGE_CLEAR_TH, parentOCRConfigMapper.getImageClearTh())
                .add(RES_PARENT_OCR_IMAGE_WIDTH, parentOCRConfigMapper.getImageWidth())
                .add(RES_PARENT_OCR_IMAGE_QUALITY, parentOCRConfigMapper.getImageQuality())
                .add(RES_PARENT_OCR_EXAMPLE_VIDEO_URL, parentOCRConfigMapper.getExampleVideoUrl());
    }

    /**
     * ocr灰度
     */
    @RequestMapping(value = "ocr_gray.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ocrGray() {
        try {
            if (hasSessionKey()) {
                validateRequest();
            } else {
                validateRequestNoSessionKey();
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e.getMessage());
        }
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "USER_OCR_GRAY_LIMIT");
        Integer grayLimit = SafeConverter.toInt(configValue, 0);
        if (OCRGrayStatus.AllNotAllow.getStatusCode().equals(grayLimit)) {
            return failMessage("不在灰度内");
        }
        if (OCRGrayStatus.AllAllow.getStatusCode().equals(grayLimit)) {
            return successMessage();
        }
        User parent = getCurrentParent();
        if (OCRGrayStatus.GrayAllow.getStatusCode().equals(grayLimit)) {
            if (parent == null) {
                return failMessage("不在灰度内");
            } else {
                List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
                if (CollectionUtils.isEmpty(studentParentRefs)) {
                    return failMessage("不在灰度内");
                }
                Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
                List<StudentDetail> studentDetails = new ArrayList<>(studentLoaderClient.loadStudentDetails(studentIds).values());
                boolean grayFlag = studentDetails.stream()
                        .anyMatch(s -> grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(s, "Parent", "OCR"));
                if (grayFlag) {
                    return successMessage();
                }
            }
        }
        return failMessage("不在灰度内");
    }


    /**
     * 识别报错
     */
    @RequestMapping(value = "report_error.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reportError() {
        //TODO:这块可能需要加参数
        User parent = getApiRequestUser();
        if (parent == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE);
        }
        String questionId = getRequestString(REQ_OCR_QUESTION_ID);
        String imgUrl = getRequestString(REQ_OCR_IMG_URL);
        String coordinate = getRequestString(REQ_OCR_COORDINATE);
        long sid = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_OCR_IMG_URL, "图片地址");
            validateRequired(REQ_OCR_COORDINATE, "识别内容");
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        ParentOCREntity entity = dpParentOCRLoader.getUserPhotoEntityByUrlAndId(sid, imgUrl);
        if (entity == null || MapUtils.isEmpty(entity.getEntity())) {
            return failMessage("未找到对应的识别信息");
        }
        Map<String, Object> map = generateUserSelect(entity, coordinate);
        if (MapUtils.isNotEmpty(map)) {
            LogCollector.info("backend-general", MapUtils.map(
                    "processCreateTime", entity.getCreateTime(),
                    "processId", entity.getId(),
                    "usertoken", sid,
                    "parentId", parent.getId(),
                    "questionId", questionId,
                    "imgUrl", imgUrl,
                    "imgWidth", SafeConverter.toInt(entity.getEntity().get("img_width")),
                    "imgHeight", SafeConverter.toInt(entity.getEntity().get("img_width")),
                    "form", map,
                    "ip", getRequestContext().getRealRemoteAddress(),
                    "op", "aiImageLogs",
                    "clientName", "17Parent",
                    "logType", "REPORT_ERROR",
                    "coordinate", coordinate,
                    "imageId", SafeConverter.toString(entity.getEntity().get("img_id")),
                    "env", RuntimeMode.current().getStageMode()
            ));
            dpParentOCRService.upsertOCREntityCheckTime(entity, new Date(), coordinate);
        }
        return successMessage();
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> generateUserSelect(ParentOCREntity entity, String coordinate) {
        if (entity == null || StringUtils.isBlank(coordinate)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) entity.getEntity().get("forms");
        if (CollectionUtils.isEmpty(mapList)) {
            return Collections.emptyMap();
        }
        for (Map<String, Object> map : mapList) {
            List<Map<String, Object>> coordinateList = (List<Map<String, Object>>) map.get("coordinate");
            if (CollectionUtils.isEmpty(coordinateList)) {
                continue;
            }
            String coordinateJson = JsonUtils.toJson(coordinateList);
            if (!StringUtils.equals(coordinate, coordinateJson)) {
                continue;
            }
            return map;
        }

        return Collections.emptyMap();

    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum OCRGrayStatus {
        AllNotAllow(0, "全部关闭"),
        GrayAllow(1, "灰度开放"),
        AllAllow(2, "全部开放");

        private final Integer statusCode;
        private final String desc;
    }
}
