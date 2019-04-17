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

package com.voxlearning.washington.controller.flash;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.LinkedMap;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.support.flash.FlashVars;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alex
 * @version 0.1
 * @since 14-10-17
 */
@Controller
@RequestMapping("/flash/loader")
@Slf4j
public class MobileFlashLoaderController extends AbstractFlashLoaderController {
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    // 说明： userId=0的时候，表示试用。会跳过结果保存的步骤。具体实现请参考 AbstractScoreCalculatorTemplate
    @RequestMapping(value = "mobileselfstudy-{type}-{userId}-{bookId}-{unitId}-{lessonId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage selfStudyForMobile(@PathVariable("type") Long type,
                                         @PathVariable("userId") Long userId,
                                         @PathVariable("bookId") Long bookId,
                                         @PathVariable("unitId") Long unitId,
                                         @PathVariable("lessonId") Long lessonId) {

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mobilehomework-{type}-{userId}-{bookId}-{unitId}-{lessonId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkForMobile(@PathVariable("type") Long type,
                                        @PathVariable("userId") String userId,
                                        @PathVariable("bookId") Long bookId,
                                        @PathVariable("unitId") Long unitId,
                                        @PathVariable("lessonId") Long lessonId,
                                        @RequestParam(value = "hid", required = false, defaultValue = "0") String hid,
                                        HttpServletRequest request) {
        return MapMessage.successMessage();
    }

    // 说明： userId=0的时候，表示试用。会跳过结果保存的步骤。具体实现请参考 AbstractScoreCalculatorTemplate
    @RequestMapping(value = "mobilemathselfstudy-{type}-{userId}-{bookId}-{unitId}-{lessonId}-{pointId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mathSelfStudyForMobile(@PathVariable("type") Long type,
                                             @PathVariable("userId") Long userId,
                                             @PathVariable("bookId") Long bookId,
                                             @PathVariable("unitId") Long unitId,
                                             @PathVariable("lessonId") Long lessonId,
                                             @PathVariable("pointId") Long pointId,
                                             @RequestParam(value = "dataType", required = false, defaultValue = "1") String dataType) {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mobilemathhomework-{type}-{userId}-{bookId}-{unitId}-{lessonId}-{pointId}-{questionNum}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mathHomeworkForMobile(@PathVariable("type") Long type,
                                            @PathVariable("userId") Long userId,
                                            @PathVariable("bookId") Long bookId,
                                            @PathVariable("unitId") Long unitId,
                                            @PathVariable("lessonId") Long lessonId,
                                            @PathVariable("pointId") Long pointId,
                                            @PathVariable("questionNum") Integer questionNum,
                                            @RequestParam(value = "hid", required = false, defaultValue = "0") String hid,
                                            @RequestParam(value = "cid", required = false, defaultValue = "0") String cid,
                                            @RequestParam(value = "dataType", required = false, defaultValue = "1") String dataType,
                                            @RequestParam(value = "homeworkType", required = false, defaultValue = "MATH") String homeworkType,
                                            HttpServletRequest request) {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mobilechineseselfstudy-{type}-{userId}-{bookId}-{unitId}-{lessonId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage chineseSelfstudyForMobile(@PathVariable("type") Long type,
                                                @PathVariable("userId") Long userId,
                                                @PathVariable("bookId") Long bookId,
                                                @PathVariable("unitId") Long unitId,
                                                @PathVariable("lessonId") Long lessonId) {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mobilechinesehomework-{type}-{userId}-{hid}-{bookId}-{unitId}-{lessonId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage chineseHomeworkForMobile(@PathVariable("type") Long type,
                                               @PathVariable("userId") Long userId,
                                               @PathVariable("bookId") Long bookId,
                                               @PathVariable("unitId") Long unitId,
                                               @PathVariable("lessonId") Long lessonId,
                                               @PathVariable("hid") Long hid,
                                               @RequestParam(value = "cid", required = false, defaultValue = "0") String cid,
                                               HttpServletRequest request) {

        return MapMessage.successMessage();

    }

    @RequestMapping(value = "mobilequiz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage quizMobile(
            @RequestParam(value = "quiz_id", required = true) Long quizId,
            HttpServletRequest request) {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mobileevh.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage englishVacationHomeworkForMobile(HttpServletRequest request) {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mobilemvh.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mathVacationHomeworkForMobile(HttpServletRequest request) {
        return MapMessage.successMessage();
    }

    /**
     * 学生端：做作业首页(H5)
     * PC & APP
     * @return
     */
    @RequestMapping(value = "newhomework/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage index() {
        User user = getHomeworkUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        String homeworkId = getRequestParameter("homeworkId", "");

        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED)
                    .setErrorUrl("/student/index" + Constants.AntiHijackExt);
        }
        // 期末基础复习作业判断逻辑
        if (NewHomeworkType.BasicReview == newHomework.getType()) {
            // 当前时间是否已经超过基础复习截止时间
//            Date currentDate = new Date();
//            if (currentDate.after(NewHomeworkConstants.BASIC_REVIEW_END_DATE)) {
//                return MapMessage.errorMessage("期末基础复习作业已截止");
//            }
            String packageId = newHomework.getBasicReviewPackageId();
            if (StringUtils.isBlank(packageId)) {
                return MapMessage.errorMessage("作业内容错误");
            }
            BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkLoaderClient.load(packageId);
            if (basicReviewHomeworkPackage == null || basicReviewHomeworkPackage.isDisabledTrue()) {
                return MapMessage.errorMessage("作业不存在或已被老师删除");
            }
            List<BasicReviewStage> stageList = basicReviewHomeworkPackage.getStages();
            if (CollectionUtils.isEmpty(stageList)) {
                return MapMessage.errorMessage("作业内容错误");
            }
            // 校验当前关卡是否真的已解锁
            MapMessage mapMessage = basicReviewHomeworkLoaderClient.loadStudentDayPackages(packageId, user.getId());
            if (mapMessage.isSuccess()) {
                List<Map<String, Object>> packageStageList = (List<Map<String, Object>>) mapMessage.get("stageList");
                if (CollectionUtils.isNotEmpty(packageStageList)) {
                    for (Map<String, Object> stageMapper : packageStageList) {
                        if (StringUtils.equals(homeworkId, SafeConverter.toString(stageMapper.get("homeworkId"))) && SafeConverter.toBoolean(stageMapper.get("locked"))) {
                            LogCollector.info("backend-general", MapUtils.map(
                                    "env", RuntimeMode.getCurrentStage(),
                                    "usertoken", user.getId(),
                                    "mod1", homeworkId,
                                    "mod2", "BasicReview",
                                    "op", "open locked homework"
                            ));
                            return MapMessage.errorMessage("当前关卡未解锁");
                        }
                    }
                }
            }
        }
        Map<String, Object> homeworkList = newHomeworkLoaderClient.indexData(homeworkId, user.getId());
        if (homeworkList.isEmpty()) {
            return MapMessage.errorMessage("作业已经不存在了");
        }
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(user.getId());
        homeworkList.put("isBindParents", Boolean.FALSE);
        if (CollectionUtils.isNotEmpty(studentParentRefs)) {
            homeworkList.put("isBindParents", Boolean.TRUE); // 是否绑定家长通
        }
        homeworkList.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        homeworkList.put("userId", user.getId());
        return MapMessage.successMessage().add("homeworkList", homeworkList);
    }


    private List<String> naturalSpellingNoSupportList() {
        //读取页面内容的配置信息
        String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "NATURAL_SPELLING_NO_SUPPERT");
        if (config == null) {
            return Collections.emptyList();
        }
        return JsonUtils.fromJsonToList(config, String.class);
    }

    /**
     * 学生端：做作业
     * PC & APP
     * @return
     */
    @RequestMapping(value = "newhomework/do.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage newHomeworkForMobile(@RequestParam("objectiveConfigType") String objectiveConfigType,
                                           @RequestParam("homeworkId") String homeworkId,
                                           HttpServletRequest request) {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }

        String lessonId = getRequestParameter("lessonId", "");
        String categoryId = getRequestParameter("categoryId", "");
        String practiceId = getRequestParameter("practiceId", "");

        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }
        ObjectiveConfigType type = ObjectiveConfigType.of(objectiveConfigType);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        if ((studentDetail != null && studentDetail.getClazz() != null)
                || (studentDetail != null && StringUtils.equalsIgnoreCase(newHomework.getType().name(), NewHomeworkType.USTalk.name()))) {
            FlashVars vars = new FlashVars(request);
            vars.add("uid", studentDetail.getId());
            //vars.add("cid", studentDetail.getClazzId() == null ? 0 : studentDetail.getClazzId());
            vars.add("hid", homeworkId);
            vars.add("userId", studentDetail.getId());
            vars.add("homeworkId", homeworkId);
            vars.add("objectiveConfigType", objectiveConfigType);
            vars.add("objectiveConfigTypeName", type != null ? type.getValue() : "");
            vars.add("subject", newHomework.getSubject());
            vars.add("ipAddress", HttpRequestContextUtils.getWebAppBaseUrl());
            vars.add("learningType", StudyType.homework);
            vars.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            if (ObjectiveConfigType.BASIC_APP.name().equals(objectiveConfigType)
                    || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.name().equals(objectiveConfigType)
                    || ObjectiveConfigType.NATURAL_SPELLING.name().equals(objectiveConfigType)) {
                vars.add("unisound8", true);
                vars.add("unisound8WordscoreLevels", newHomeworkContentServiceClient.loadUniSoundWordScoreLevels(studentDetail));
                vars.add("unisound8SentencescoreLevels", newHomeworkContentServiceClient.loadUniSoundSentenceScoreLevels(studentDetail));
                if (ObjectiveConfigType.BASIC_APP.name().equals(objectiveConfigType)) {
                    vars.add("use17VoiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "BasicApp", "17VoiceEngine"));
                    vars.add("voiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "BasicApp", "17VoiceEngine") ? "yiqi" : "normal");
                }
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);

                List<PracticeType> practiceTypes = practiceLoaderClient.loadCategoriedIdPractices(SafeConverter.toInt(categoryId));

                List<Map> practices = new ArrayList<>();
                for (PracticeType practiceType : practiceTypes) {
                    //practiceType.getPracticeType()和practiceType.getId()的值是一样的
                    if (!PracticeCategory.categoryPracticeTypesMap.get(SafeConverter.toInt(categoryId)).contains(practiceType.getId())) {
                        continue;
                    }
                    String appUrl = UrlUtils.buildUrlQuery("/flash/loader/newhomework" + Constants.AntiHijackExt, MiscUtils.m("practiceId", practiceType.getId(), "hid", homeworkId, "lessonId", lessonId, "newHomeworkType", newHomework.getType(), "objectiveConfigType", objectiveConfigType, "sid", user.getId()));
                    if (ObjectiveConfigType.NATURAL_SPELLING.name().equals(objectiveConfigType)) {
                        appUrl = "/resources/apps/hwh5/homework/V2_5_0/phonics/" + practiceType.getMobileVersion() + "/index.html";
                        if (!RuntimeMode.isDevelopment()) {
                            appUrl = cdnResourceVersionCollector.getVersionedUrlPath(appUrl);
                        }
                    }
                    practices.add(MiscUtils.m(
                            "appUrl", appUrl,
                            "appMobileUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomeworkmobile" + Constants.AntiHijackExt, MiscUtils.m("practiceId", practiceType.getId(), "hid", homeworkId, "lessonId", lessonId, "newHomeworkType", newHomework.getType(), "objectiveConfigType", objectiveConfigType, "sid", user.getId())),
                            "fileName", practiceType.getFilename(),
                            "practiceId", practiceType.getId(),
                            "practiceName", practiceType.getPracticeName(),
                            "categoryId", practiceType.getCategoryId(),
                            "lessonId", lessonId,
                            "categoryName", practiceType.getCategoryName(),
                            "needRecord", practiceType.getNeedRecord(),
                            "checked", practiceType.getId().equals(SafeConverter.toLong(practiceId)),
                            "questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "lessonId", lessonId, "categoryId", practiceType.getCategoryId(), "sid", user.getId())),
                            "completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "lessonId", lessonId, "categoryId", practiceType.getCategoryId(), "sid", user.getId())),
                            "processResultUrl", UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", user.getId()))
                    ));
                }
                vars.add("practices", practices);
                vars.add("objectiveConfigType", objectiveConfigType);
                vars.add("nsNoSupportList", naturalSpellingNoSupportList()); //不支持自然拼读的机型
            } else if (ObjectiveConfigType.READING.name().equals(objectiveConfigType)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.READING);
                if (newHomeworkPracticeContent == null || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
                    return MapMessage.errorMessage("作业内容错误");
                }
                List<String> picBookIds = new ArrayList<>();
                for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
                    if (StringUtils.isNotBlank(newHomeworkApp.getPictureBookId())) {
                        picBookIds.add(newHomeworkApp.getPictureBookId());
                    }
                }
                List<PictureBookSummaryResult> picBookResult = pictureBookHomeworkServiceClient.getPictureBookSummaryInfo(homeworkId, picBookIds, studentDetail.getId());
                vars.add("practices", picBookResult);
                vars.add("objectiveConfigType", objectiveConfigType);
            } else if (ObjectiveConfigType.LEVEL_READINGS.name().equalsIgnoreCase(objectiveConfigType)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
                if (newHomeworkPracticeContent == null || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
                    return MapMessage.errorMessage("作业内容错误");
                }
                List<String> picBookIds = new ArrayList<>();
                for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
                    if (StringUtils.isNotBlank(newHomeworkApp.getPictureBookId())) {
                        picBookIds.add(newHomeworkApp.getPictureBookId());
                    }
                }
                List<PictureBookPlusSummaryResult> picBookResult = pictureBookHomeworkServiceClient.getPictureBookPlusSummaryInfo(homeworkId, picBookIds, studentDetail.getId());
                List<PictureBookNewClazzLevel> clazzLevels = PictureBookNewClazzLevel.primarySchoolLevels();
                List<Map<String, Object>> clazzLevelDescriptions = clazzLevels.stream()
                        .map(level -> {
                            List<Map<String, Object>> descriptions = new ArrayList<>();
                            descriptions.add(MapUtils.m("title", "读物难度", "description", level.getReadingDifficulty()));
                            descriptions.add(MapUtils.m("title", "读物文体", "description", level.getReadingStyle()));
                            descriptions.add(MapUtils.m("title", "阅读习惯", "description", level.getReadingHabits()));
                            descriptions.add(MapUtils.m("title", "阅读能力", "description", level.getReadingAbility()));
                            descriptions.add(MapUtils.m("title", "阅读体验", "description", level.getReadingExperience()));
                            descriptions.add(MapUtils.m("title", "累计阅读量", "description", level.getReadingAmount()));
                            return MapUtils.m(
                                    "level", level.name(),
                                    "levelName", level.getLevelName(),
                                    "descriptions", descriptions);
                        })
                        .collect(Collectors.toList());
                vars.add("practices", picBookResult);
                vars.add("clazzLevelDescriptions", clazzLevelDescriptions);
                vars.add("objectiveConfigType", objectiveConfigType);
                vars.add("unisound8WordscoreLevels", newHomeworkContentServiceClient.loadUniSoundWordScoreLevels(studentDetail));
                vars.add("unisound8SentencescoreLevels", newHomeworkContentServiceClient.loadUniSoundSentenceScoreLevels(studentDetail));

                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);
            } else if (ObjectiveConfigType.KEY_POINTS.name().equals(objectiveConfigType)) {
                List<VideoSummaryResult> videoResults = videoHomeworkServiceClient.getVideoSummaryInfo(homeworkId, studentDetail.getId());
                vars.add("practices", videoResults);
            } else if (ObjectiveConfigType.NEW_READ_RECITE.name().equals(objectiveConfigType) || ObjectiveConfigType.READ_RECITE_WITH_SCORE.name().equals(objectiveConfigType)) {
                List<ReadReciteSummaryResult> readReciteSummaryResults = readReciteHomeworkServiceClient.getReadReciteSummaryInfo(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail.getId());
                vars.add("practices", readReciteSummaryResults);
                vars.add("voiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ReadReciteWithScore", "SingSoundVoiceEngine") ? "yiqi" : "normal");
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);
                vars.add("wordStandScore", 3);
                vars.add("standScore", StringUtils.equalsIgnoreCase(submitVoiceEngine, "UniSound") ? 3 : 4);
                vars.add("paragraphStandPercentage", 0.6);
            } else if (ObjectiveConfigType.DUBBING.name().equalsIgnoreCase(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
                List<DubbingSummaryResult> dubbingSummaryResults = dubbingHomeworkServiceClient.getDubbingSummerInfo(homeworkId, studentDetail.getId(), objectiveConfigType);
                vars.add("practices", dubbingSummaryResults);
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);
            } else if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name().equalsIgnoreCase(objectiveConfigType)) {
                String imageWidthStr = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "OCR_MENTAL_IMAGE_WIDTH");
                String imageQualityStr = newHomeworkContentServiceClient.loadImageQualityStr(studentDetail);
                NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                if (newHomeworkPracticeContent == null) {
                    return MapMessage.errorMessage("作业内容错误");
                }
                NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), false);
                boolean finished = newHomeworkResult != null
                        && newHomeworkResult.getPractices() != null
                        && newHomeworkResult.getPractices().get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) != null
                        && newHomeworkResult.getPractices().get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC).getFinishAt() != null;
                String workBookName = newHomeworkPracticeContent.getWorkBookName();
                String homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
                List<String> bookNameList = Arrays.asList(StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                List<String> homeworkDetailList = Arrays.asList(StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
                List<Map<String, Object>> practices = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    practices.add(MapUtils.m("bookName", bookNameList.get(i), "homeworkDetail", homeworkDetailList.get(i) + "（页码）"));
                }
                if (length > 1) {
                    workBookName = bookNameList.get(0);
                    homeworkDetail = homeworkDetailList.get(0);
                }
                vars.add("workBookName", workBookName);
                vars.add("homeworkDetail", homeworkDetail);
                vars.add("practices", practices);
                vars.add("finished", finished);
                vars.add("processResultUrl", UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", user.getId())));
                vars.add("imageWidth", SafeConverter.toInt(imageWidthStr, 1080));
                vars.add("imageQuality", SafeConverter.toFloat(imageQualityStr, 0.8f));
            } else if (ObjectiveConfigType.WORD_RECOGNITION_AND_READING.name().equalsIgnoreCase(objectiveConfigType)) {
                List<WordRecognitionSummaryResult> wordRecognitionSummaryResults = wordRecognitionHomeworkServiceClient.getWordRecognitionSummaryInfo(homeworkId, studentDetail.getId());
                vars.add("practices", wordRecognitionSummaryResults);
                vars.add("voiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "WordRecognitionAndReading", "SingSoundVoiceEngine") ? "yiqi" : "normal");
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);
            } else if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.name().equalsIgnoreCase(objectiveConfigType)) {
                List<Map> practices = wordTeachHomeworkServiceClient.getWordTeachSummaryInfo(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail.getId());
                vars.add("practices", practices);
            } else if (ObjectiveConfigType.ORAL_COMMUNICATION.name().equalsIgnoreCase(objectiveConfigType)) {
               List<Map> oralPractise= oralCommunicationClient.getHomeworkSummaryGroupByType(homeworkId,objectiveConfigType);
               vars.add("practices",oralPractise);
            } else {
                if (ObjectiveConfigType.ORAL_PRACTICE.name().equals(objectiveConfigType) || ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING.name().equals(objectiveConfigType)) {
                    NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
                    if (newHomeworkPracticeContent == null) {
                        return MapMessage.errorMessage("作业内容错误");
                    }
                    NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), false);
                    boolean finished = newHomeworkResult != null
                            && newHomeworkResult.getPractices() != null
                            && newHomeworkResult.getPractices().get(type) != null
                            && newHomeworkResult.getPractices().get(type).getFinishAt() != null;
                    vars.add("oralScoreIntervals", OralScoreInterval.oralScoreIntervals);
                    vars.add("finished", finished);
                    vars.add("unisound8WordscoreLevels", newHomeworkContentServiceClient.loadUniSoundWordScoreLevels(studentDetail));
                    vars.add("unisound8SentencescoreLevels", newHomeworkContentServiceClient.loadUniSoundSentenceScoreLevels(studentDetail));
                }
                vars.add("questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "sid", user.getId())));
                vars.add("completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "sid", user.getId())));
                vars.add("processResultUrl", UrlUtils.buildUrlQuery("/exam/flash/newhomework/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", user.getId())));
                if (ObjectiveConfigType.MENTAL_ARITHMETIC.name().equals(objectiveConfigType)) {
                    String appUrl = "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-mental-arithmetic/index.html";
                    vars.add("appUrl", appUrl);
                    vars.add("processResultUrl", UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", user.getId())));
                }
            }
            vars.add("sendDetailLog", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "SendDetailLog"));
            String flashVars = vars.getJsonParam();
            Map<String, Object> data = new HashMap<>();
            data.put("flashVars", flashVars);
            return MapMessage.successMessage().add("data", data);
        } else {
            //爬虫爬内容太厉害了，不用输出日志了。
            //log.error("LoaderController.homework can not get current student, need login");
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    /**
     * 错题订正作业开始，不是一个作业仅仅是做题
     */
    @RequestMapping(value = "newhomework/docorrect.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newHomeworkDoCorrect(@RequestParam("objectiveConfigType") String objectiveConfigType,
                                           @RequestParam("homeworkId") String homeworkId,
                                           HttpServletRequest request) {
        return MapMessage.successMessage();
    }

    /**
     * 作业中间结果
     * @return
     */
    @RequestMapping(value = "newhomework/type/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkForObjectiveConfigTypeResult(
            @RequestParam("objectiveConfigType") String objectiveConfigType,
            @RequestParam("homeworkId") String homeworkId) {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        if (studentDetail != null && studentDetail.getClazz() != null) {
            return newHomeworkReportServiceClient.homeworkForObjectiveConfigTypeResult(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail);
        } else {
            //爬虫爬内容太厉害了，不用输出日志了。
            //log.error("LoaderController.homework can not get current student, need login");
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }


    // 考试首页
    @RequestMapping(value = "newexam/index.vpage")
    @ResponseBody
    public MapMessage index(@RequestParam("newExamId") String newExamId) {
        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        MapMessage mapMessage = newExamServiceClient.index(newExamId, user.getId());

        if (mapMessage.isSuccess()) {
            Map<String, Object> result = (Map<String, Object>) mapMessage.getOrDefault("result", new LinkedMap<String, Object>());
            String imageUrl = getUserAvatarImgUrl(SafeConverter.toString(result.get("imageUrl")));
            result.put("imageUrl", imageUrl);
        }
        return mapMessage;
    }


    //进入考试
    @RequestMapping(value = "newexam/do.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newExamDo(@RequestParam("newExamId") String newExamId, @RequestParam("clientType") String clientType, @RequestParam("clientName") String clientName) {
        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        StudentDetail studentDetail = currentStudentDetail();
        try {
            return atomicLockManager.wrapAtomic(newExamServiceClient)
                    .keys(newExamId, studentDetail.getId())
                    .proxy()
                    .enterExam(newExamId, studentDetail, getCdnBaseUrlStaticSharedWithSep(), clientType, clientName);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.errorMessage("数据处理中请稍等").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
            }
            return MapMessage.errorMessage("提交结果失败").setErrorCode(ex.getMessage());
        }
    }


    // 获取考试试题答案信息
    @RequestMapping(value = "newexam/questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map questionsAnswer(@RequestParam("newExamId") String newExamId, @RequestParam("userId") Long userId) {
        User user = currentUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        return newExamServiceClient.loadQuestionAnswer(newExamId, userId, false);
    }

    // 获取考试试题答案信息
    @RequestMapping(value = "newexam/questions/view/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map questionsViewAnswer(@RequestParam("newExamId") String newExamId, @RequestParam("userId") Long userId) {
        User user = currentUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        return newExamServiceClient.loadQuestionAnswer(newExamId, userId, true);
    }

    /**
     * 预览考试
     * 老师、学生公用
     */
    @RequestMapping(value = "newexam/view.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newExamView(@RequestParam("newExamId") String newExamId, @RequestParam("userId") Long userId) {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (newExamId == null) {
            return MapMessage.errorMessage("考试信息不存在");
        }

        if (user.isStudent()) {
            //打点一起测学生pc查看报告
            newExamReportLoaderClient.studentViewExamReportKafka(newExamId, userId, "app");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生信息不存在");
        }
        return newExamServiceClient.viewExam(newExamId, studentDetail, getCdnBaseUrlStaticSharedWithSep());
    }


    /**
     * 期末复习错题本，开始消灭错题
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "do.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage wrongQuestionForMobile(HttpServletRequest request) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 期末复习错题本，保存错题本做题结果
     */
    @RequestMapping(value = "processresult.vpage")
    @ResponseBody
    public MapMessage processResult() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 假期作业任务包
     * PC端 && APP端 共用
     *
     * @return
     */
    @RequestMapping(value = "vacation/homework/package.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadPackage() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        String packageId = getRequestString("packageId");
        return vacationHomeworkLoaderClient.loadStudentDayPackages(packageId, currentUserId());
    }

    /**
     * 假期作业答题首页
     */
    @RequestMapping(value = "vacation/homework/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vacationHomeworkIndex() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        String homeworkId = getRequestParameter("homeworkId", "");
        Map<String, Object> vacationHomeworkList = vacationHomeworkLoaderClient.indexData(homeworkId, user.getId());
        if (vacationHomeworkList.isEmpty()) {
            return MapMessage.errorMessage("假期作业不存在，或者作业已被老师删除")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_NOT_EXIST)
                    .setErrorUrl("/student/index" + Constants.AntiHijackExt);
        }
        if (new Date().after(NewHomeworkConstants.VH_END_DATE_LATEST)) {
            return MapMessage.errorMessage("假期作业已经结束")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_NOT_EXIST)
                    .setErrorUrl("/student/index" + Constants.AntiHijackExt);
        }
        vacationHomeworkList.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        vacationHomeworkList.put("userId", user.getId());
        return MapMessage.successMessage().add("homeworkList", vacationHomeworkList);
    }

    /**
     * 学生做假期作业
     */
    @RequestMapping(value = "vacation/homework/do.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vacationHomeworkForMobile(@RequestParam("objectiveConfigType") String objectiveConfigType,
                                                @RequestParam("homeworkId") String homeworkId,
                                                HttpServletRequest request) {
        Date currentDate = new Date();
        if (currentDate.after(NewHomeworkConstants.VH_END_DATE_LATEST)) {
            return MapMessage.errorMessage("作业已结束").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_END);
        }
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }

        String lessonId = getRequestParameter("lessonId", "");
        String categoryId = getRequestParameter("categoryId", "");
        String practiceId = getRequestParameter("practiceId", "");

        String pictureBookIds = getRequestString("pictureBookIds");
        VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
        if (vacationHomework == null) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_NOT_EXIST);
        }
        ObjectiveConfigType type = ObjectiveConfigType.of(objectiveConfigType);
        if (new Date().after(NewHomeworkConstants.VH_END_DATE_LATEST)) {
            return MapMessage.errorMessage("假期作业已经结束").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_NOT_EXIST);
        }
        StudentDetail studentDetail = currentStudentDetail();
        if ((studentDetail != null && studentDetail.getClazz() != null)
                || (studentDetail != null && StringUtils.equalsIgnoreCase(vacationHomework.getType().name(), NewHomeworkType.USTalk.name()))) {
            FlashVars vars = new FlashVars(request);
            vars.add("nsNoSupportList", naturalSpellingNoSupportList());//不支持自然拼读的机型
            vars.add("uid", studentDetail.getId());
//            vars.add("cid", studentDetail.getClazzId() == null ? 0 : studentDetail.getClazzId());
            vars.add("hid", homeworkId);
            vars.add("userId", studentDetail.getId());
            vars.add("homeworkId", homeworkId);
            vars.add("objectiveConfigType", objectiveConfigType);
            vars.add("objectiveConfigTypeName", type != null ? type.getValue() : "");
            vars.add("subject", vacationHomework.getSubject());
            vars.add("ipAddress", HttpRequestContextUtils.getWebAppBaseUrl());
            vars.add("learningType", StudyType.vacationHomework);
            vars.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            if (ObjectiveConfigType.BASIC_APP.name().equals(objectiveConfigType)
                    || ObjectiveConfigType.NATURAL_SPELLING.name().equals(objectiveConfigType)) {
                vars.add("scoreLevels", UnisoundScoreLevel.levels);
                vars.add("unisound8", true);
                vars.add("unisound8WordscoreLevels", newHomeworkContentServiceClient.loadUniSoundWordScoreLevels(studentDetail));
                vars.add("unisound8SentencescoreLevels", newHomeworkContentServiceClient.loadUniSoundSentenceScoreLevels(studentDetail));
                if (ObjectiveConfigType.BASIC_APP.name().equals(objectiveConfigType)) {
                    vars.add("use17VoiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "BasicApp", "17VoiceEngine"));
                    vars.add("voiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "BasicApp", "17VoiceEngine") ? "yiqi" : "normal");
                }
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);

                List<PracticeType> practiceTypes = practiceLoaderClient.loadCategoriedIdPractices(SafeConverter.toInt(categoryId));

                List<Map> practices = new ArrayList<>();
                for (PracticeType practiceType : practiceTypes) {
                    //practiceType.getPracticeType()和practiceType.getId()的值是一样的
                    if (!PracticeCategory.categoryPracticeTypesMap.get(SafeConverter.toInt(categoryId)).contains(practiceType.getId())) {
                        continue;
                    }
                    String appUrl = UrlUtils.buildUrlQuery("/flash/loader/newhomework" + Constants.AntiHijackExt, MiscUtils.m("practiceId", practiceType.getId(), "hid", homeworkId, "lessonId", lessonId, "newHomeworkType", vacationHomework.getType(), "objectiveConfigType", objectiveConfigType));
                    if (ObjectiveConfigType.NATURAL_SPELLING.name().equals(objectiveConfigType)) {
                        appUrl = "/resources/apps/hwh5/homework/V2_5_0/phonics/" + practiceType.getMobileVersion() + "/index.html";
                        if (!RuntimeMode.isDevelopment()) {
                            appUrl = cdnResourceVersionCollector.getVersionedUrlPath(appUrl);
                        }
                    }
                    practices.add(MiscUtils.m(
                            "appUrl", appUrl,
                            "appMobileUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomeworkmobile" + Constants.AntiHijackExt, MiscUtils.m("practiceId", practiceType.getId(), "hid", homeworkId, "lessonId", lessonId, "newHomeworkType", vacationHomework.getType(), "objectiveConfigType", objectiveConfigType)),
                            "fileName", practiceType.getFilename(),
                            "practiceId", practiceType.getId(),
                            "practiceName", practiceType.getPracticeName(),
                            "categoryId", practiceType.getCategoryId(),
                            "categoryName", practiceType.getCategoryName(),
                            "lessonId", lessonId,
                            "needRecord", practiceType.getNeedRecord(),
                            "checked", practiceType.getId().equals(SafeConverter.toLong(practiceId)),
                            "questionUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "lessonId", lessonId, "categoryId", practiceType.getCategoryId())),
                            "completedUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "lessonId", lessonId, "categoryId", practiceType.getCategoryId())),
                            "processResultUrl", "/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt
                    ));
                }
                vars.add("practices", practices);
            } else if (ObjectiveConfigType.READING.name().equals(objectiveConfigType)) {
                // 其实这个picBookIds可以不用传过来
                String[] picBookIds = StringUtils.split(pictureBookIds, ",");
                if (picBookIds == null || picBookIds.length <= 0) {
                    return MapMessage.errorMessage("阅读绘本不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_PICTURE_BOOK_IS_NULL);
                }
                List<PictureBookSummaryResult> picBookResult = pictureBookHomeworkServiceClient.getVacationPictureBookSummaryInfo(homeworkId, Arrays.asList(picBookIds), studentDetail.getId());
                vars.add("practices", picBookResult);
            } else if (ObjectiveConfigType.LEVEL_READINGS.name().equalsIgnoreCase(objectiveConfigType)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = vacationHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
                if (newHomeworkPracticeContent == null || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
                    return MapMessage.errorMessage("作业内容错误");
                }
                List<String> picBookIds = new ArrayList<>();
                for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
                    if (StringUtils.isNotBlank(newHomeworkApp.getPictureBookId())) {
                        picBookIds.add(newHomeworkApp.getPictureBookId());
                    }
                }
                List<PictureBookPlusSummaryResult> picBookResult = pictureBookHomeworkServiceClient.getVacationPictureBookPlusSummaryInfo(homeworkId, picBookIds, studentDetail.getId());
                List<PictureBookNewClazzLevel> clazzLevels = PictureBookNewClazzLevel.primarySchoolLevels();
                List<Map<String, Object>> clazzLevelDescriptions = clazzLevels.stream()
                        .map(level -> {
                            List<Map<String, Object>> descriptions = new ArrayList<>();
                            descriptions.add(MapUtils.m("title", "读物难度", "description", level.getReadingDifficulty()));
                            descriptions.add(MapUtils.m("title", "读物文体", "description", level.getReadingStyle()));
                            descriptions.add(MapUtils.m("title", "阅读习惯", "description", level.getReadingHabits()));
                            descriptions.add(MapUtils.m("title", "阅读能力", "description", level.getReadingAbility()));
                            descriptions.add(MapUtils.m("title", "阅读体验", "description", level.getReadingExperience()));
                            descriptions.add(MapUtils.m("title", "累计阅读量", "description", level.getReadingAmount()));
                            return MapUtils.m(
                                    "level", level.name(),
                                    "levelName", level.getLevelName(),
                                    "descriptions", descriptions);
                        })
                        .collect(Collectors.toList());
                vars.add("practices", picBookResult);
                vars.add("clazzLevelDescriptions", clazzLevelDescriptions);
                vars.add("objectiveConfigType", objectiveConfigType);
                vars.add("unisound8WordscoreLevels", newHomeworkContentServiceClient.loadUniSoundWordScoreLevels(studentDetail));
                vars.add("unisound8SentencescoreLevels", newHomeworkContentServiceClient.loadUniSoundSentenceScoreLevels(studentDetail));

                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);
            } else if (ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
                List<DubbingSummaryResult> dubbingSummaryResultList = dubbingHomeworkServiceClient.getVacationDubbingSummerInfo(homeworkId, studentDetail.getId(), objectiveConfigType);
                vars.add("practices", dubbingSummaryResultList);
                vars.add("voiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ReadReciteWithScore", "SingSoundVoiceEngine") ? "yiqi" : "normal");
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);
            } else if (ObjectiveConfigType.KEY_POINTS.name().equals(objectiveConfigType)) {
                List<VideoSummaryResult> videoResult = videoHomeworkServiceClient.getVacationVideoSummaryInfo(homeworkId, studentDetail.getId());
                vars.add("practices", videoResult);
            } else if (ObjectiveConfigType.NEW_READ_RECITE.name().equals(objectiveConfigType) || ObjectiveConfigType.READ_RECITE_WITH_SCORE.name().equals(objectiveConfigType)) {
                List<ReadReciteSummaryResult> readReciteSummaryResults = readReciteHomeworkServiceClient.getVacationReadReciteSummaryInfo(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail.getId());
                vars.add("practices", readReciteSummaryResults);
                vars.add("voiceEngine", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ReadReciteWithScore", "SingSoundVoiceEngine") ? "yiqi" : "normal");
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.add("processVoiceEngine", processVoiceEngine);
                vars.add("submitVoiceEngine", submitVoiceEngine);
            } else {
                if (ObjectiveConfigType.ORAL_PRACTICE.name().equals(objectiveConfigType)) {
                    vars.add("oralScoreIntervals", OralScoreInterval.oralScoreIntervals);
                }
                vars.add("questionUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId)));
                vars.add("completedUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId)));
                vars.add("processResultUrl", "/exam/flash/newhomework/processresult" + Constants.AntiHijackExt);
                if (ObjectiveConfigType.MENTAL_ARITHMETIC.name().equals(objectiveConfigType)) {
                    String appUrl = "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-mental-arithmetic/index.html";
                    vars.add("appUrl", appUrl);
                    vars.add("processResultUrl", "/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt);
                }
            }

            String flashVars = vars.getJsonParam();
            Map<String, Object> data = new HashMap<>();
            data.put("flashVars", flashVars);
            return MapMessage.successMessage().add("data", data);
        } else {
            //爬虫爬内容太厉害了，不用输出日志了。
            //log.error("LoaderController.homework can not get current student, need login");
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    /**
     * 获取假期作业结果信息
     *
     * @param objectiveConfigType
     * @param homeworkId
     * @return
     */
    @RequestMapping(value = "vacation/homework/type/result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage vacationHomeworkForObjectiveConfigTypeResult(
            @RequestParam("objectiveConfigType") String objectiveConfigType,
            @RequestParam("homeworkId") String homeworkId) {
        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail != null && studentDetail.getClazz() != null) {
            return newHomeworkReportServiceClient.vacationHomeworkForObjectiveConfigTypeResult(homeworkId, ObjectiveConfigType.of(objectiveConfigType));
        } else {
            //爬虫爬内容太厉害了，不用输出日志了。
            //log.error("LoaderController.homework can not get current student, need login");
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    /**
     * 获取假期作业题目信息
     */
    @RequestMapping(value = "vacation/homework/questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map newexams(@RequestParam("objectiveConfigType") String objectiveConfigTypeStr, @RequestParam("homeworkId") String homeworkId) {
        Integer categoryId = getRequestInt("categoryId", 0);
        String lessonId = getRequestString("lessonId");
        String videoId = getRequestString("videoId");
        String questionBoxId = getRequestString("questionBoxId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        User user = currentUser();
        if (user != null) {
            return MapMessage.successMessage().add("result", vacationHomeworkLoaderClient.loadHomeworkQuestions(homeworkId, objectiveConfigType, categoryId, lessonId, videoId, questionBoxId));
        } else {
            return MapMessage.errorMessage("请登录");
        }
    }

    /**
     * 获取假期作业答案信息
     *
     * @param objectiveConfigTypeStr
     * @param homeworkId
     * @return
     */
    @RequestMapping(value = "vacation/homework/questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map questionsAnswer(@RequestParam("objectiveConfigType") String objectiveConfigTypeStr, @RequestParam("homeworkId") String homeworkId) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        User user = currentUser();
        Integer categoryId = getRequestInt("categoryId", 0);
        String lessonId = getRequestString("lessonId");
        String videoId = getRequestString("videoId");
        String questionBoxId = getRequestString("questionBoxId");
        if (user != null) {
            return MapMessage.successMessage().add("result", vacationHomeworkLoaderClient.loadQuestionAnswer(objectiveConfigType, homeworkId, categoryId, lessonId, videoId, questionBoxId));
        } else {
            return MapMessage.errorMessage("请登录");
        }
    }

    // 学生做期末基础复习作业任务包
    @RequestMapping(value = "basicreview/homework/package.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadBasicReviewHomeworkPackage() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        String packageId = getRequestString("packageId");
        MapMessage message = basicReviewHomeworkLoaderClient.loadStudentDayPackages(packageId, user.getId());
        if (message.isSuccess()) {
            message.put("teacherImageUrl", getUserAvatarImgUrl(SafeConverter.toString(message.get("teacherImageUrl"))));
        }
        return message;
    }
}
