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

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;


/**
 * @author tanguohong
 * @since 下午4:03,13-11-22.
 */
@Controller
@RequestMapping
public class LoaderFlashGameDataController extends AbstractController {

    //for english  因为jsonp不用@ResponseBody注解的话返回值，前台js无法就收，所以暂时这样处理，
    @Deprecated
    @RequestMapping(value = "appdata/flash/{flashGameName}/obtain-ENGLISH-{lessonId}.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String obtainEnglishJsonData(@PathVariable("flashGameName") String flashGameName, @PathVariable("lessonId") Long lessonId) {
        String jsonpCallback = getRequestParameter("callback", "");
        String dadainfo = JsonUtils.toJson(MapMessage.errorMessage("功能已下线"));
        if (StringUtils.isNotBlank(jsonpCallback)) {
            return jsonpCallback + "(" + dadainfo + ")";
        } else {
            return dadainfo;
        }
    }

    @Deprecated
    @RequestMapping(value = "appdata/flash/{flashGameName}/obtain-MATH-{pointId}-{amount}.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String obtainMathData(@PathVariable("flashGameName") String flashGameName, @PathVariable("pointId") Long pointId, @PathVariable("amount") Integer amount, Model model) {
        String jsonpCallback = getRequestParameter("callback", "");
        String dadainfo = JsonUtils.toJson(MapMessage.errorMessage("功能已下线"));
        if (StringUtils.isNotBlank(jsonpCallback)) {
            return jsonpCallback + "(" + dadainfo + ")";
        } else {
            return dadainfo;
        }
    }


    @RequestMapping(value = "appdata/flash/{flashGameName}/obtain-MATHDATATYPES-{pointId}.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String obtainMathDatTypes(@PathVariable("flashGameName") String flashGameName, @PathVariable("pointId") Long pointId, Model model) {

        List<Map<String, List<Integer>>> dataTypes = mathContentLoaderClient.getExtension().loadPointBaseTypeAndCount(pointId);
        if (dataTypes == null || dataTypes.size() == 0) {
            return "";
        }

        List<String> retDataTypes = new ArrayList<>();
        for (Map<String, List<Integer>> dataItem : dataTypes) {
            retDataTypes.addAll(dataItem.keySet());
        }

        return JsonUtils.toJson(retDataTypes);
    }

    @Deprecated
    @RequestMapping(value = "appdata/flash/{flashGameName}/obtain-CHINESE-{lessonId}.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String obtainChineseData(@PathVariable("flashGameName") String flashGameName, @PathVariable("lessonId") Long lessonId) {
        String jsonpCallback = getRequestParameter("callback", "");
        String dadainfo = JsonUtils.toJson(MapMessage.errorMessage("功能已下线"));
        if (StringUtils.isNotBlank(jsonpCallback)) {
            return jsonpCallback + "(" + dadainfo + ")";
        } else {
            return dadainfo;
        }
    }


    @RequestMapping(value = "appdata/obtain.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String obtainMobileData() {
        User user = currentUser();
        if (user == null) return JsonUtils.toJson(MapMessage.errorMessage("请重新登录"));

        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "APP_DATA", "appdata/obtain.vpage", 40)) {
            return JsonUtils.toJson(MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。"));
        }

        String sb = getRequestString("subject");
        Subject subject = Subject.ofWithUnknown(sb);
        String flashGameName = getRequestString("practiceName");
        String newLessonId = getRequestString("lessonId"); //英语、语文课程id
        String jsonpCallback = getRequestParameter("callback", "");
        String hid = getRequestString("hid");
        Integer categoryId = getRequestInt("categoryId");
        String qids = getRequestString("qids");
        String pictureBookId = getRequestString("pictureBookId");
        String objectiveConfigType = getRequestString("objectiveConfigType");
        Long userId = currentUserId();
        NewHomeworkType newHomeworkType = NewHomeworkType.of(getRequestString("newHomeworkType"));
        PracticeType practiceType = practiceLoaderClient.loadNamedPractice(flashGameName);
        String bookId = getRequestString("bookId");
        Long oldLessonId = 0L;
        if (StringUtils.isNumeric(newLessonId)) {
            oldLessonId = SafeConverter.toLong(newLessonId, 0L);
        } else {
            NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newLessonId);
            if (newBookCatalog != null) {
                oldLessonId = newBookCatalog.getOldId();
            }
        }

        Map<String, Object> gameData;
        switch (subject) {
            case ENGLISH:
                if (StringUtils.equals(practiceType.getDataType(), Constants.GameDataTemplate_ReadingData)) {
                    // 阅读绘本
                    MapMessage message;
                    if (StringUtils.isNotBlank(pictureBookId)) {
                        // 新的阅读绘本
                        message = pictureBookHomeworkServiceClient.getPictureBookDraftByPicBookId(pictureBookId);
                    } else {
                        message = MapMessage.errorMessage("绘本ID为空");
                    }
                    if (StringUtils.isNotBlank(jsonpCallback)) {
                        return jsonpCallback + "(" + JsonUtils.toJson(message) + ")";
                    } else {
                        return JsonUtils.toJson(message);
                    }
                }
                String ktwelve = getRequestParameter("ktwelve", Ktwelve.PRIMARY_SCHOOL.name());
                String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
                Ktwelve k12;
                try {
                    k12 = Ktwelve.valueOf(ktwelve);
                } catch (Exception ex) {
                    k12 = Ktwelve.PRIMARY_SCHOOL;
                }

                if (StringUtils.isNumeric(newLessonId)) {
                    // 老数据
                    gameData = flashGameServiceClient.loadData(userId, cdnUrl, oldLessonId, practiceType, k12, false);
                } else {
                    // qids为预览特有属性
                    if (StringUtils.isNotBlank(qids)) {
                        String[] questionIds = StringUtils.split(qids, ",");
                        if (questionIds != null && questionIds.length > 0) {
                            gameData = flashGameServiceClient.loadPreviewNewDate(userId, cdnUrl, newLessonId, practiceType, k12, Arrays.asList(questionIds), true, bookId);
                        } else {
                            gameData = MapMessage.errorMessage("题目不存在");
                        }
                    } else {
                        gameData = flashGameServiceClient.loadNewData(userId, cdnUrl, newLessonId, practiceType, k12, hid, categoryId, true, newHomeworkType, objectiveConfigType);
                    }
                }
                break;
            default:
                gameData = MapMessage.errorMessage("subject不存在").add("subject", sb);
                break;
        }

        String datainfo = JsonUtils.toJson(gameData);
        if (StringUtils.isNotBlank(jsonpCallback)) {
            return jsonpCallback + "(" + datainfo + ")";
        } else {
            return datainfo;
        }
    }

    @RequestMapping(value = "appdata/obtain/picturebook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage obtainPictureBookData() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "APP_DATA", "appdata/obtain/picturebook.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        String pictureBookId = getRequestString("pictureBookId");
        if (StringUtils.isBlank(pictureBookId)) {
            return MapMessage.errorMessage("绘本id错误");
        }
        //一起学绘本专用字段l表示低配h表示高配
        String pbVersion = getRequestString("pbVersion");
        MapMessage mapMessage = pictureBookHomeworkServiceClient.getPictureBookPlusDraft(pictureBookId);
        if(StringUtils.isBlank(pbVersion)){
            return mapMessage;
        }else{
            if(mapMessage.isSuccess()){
                Map<String, Object> pictureBook = JsonUtils.safeConvertObjectToMap(mapMessage.get("pictureBook"));
                List<String> oralQuestions  = JsonUtils.fromJsonToList(JsonUtils.toJson(pictureBook.get("oralQuestions")), String.class);
                List<String> practiceQuestions = JsonUtils.fromJsonToList(JsonUtils.toJson(pictureBook.get("practiceQuestions")), String.class);
                if("l".equals(pbVersion)){
                    if(CollectionUtils.isNotEmpty(oralQuestions) && oralQuestions.size() > 1){
                        pictureBook.put("oralQuestions", oralQuestions.subList(0,1));
                    }
                    if(CollectionUtils.isNotEmpty(practiceQuestions)){
                        pictureBook.put("practiceQuestions", Collections.emptyList());
                    }
                }else {
                    if(CollectionUtils.isNotEmpty(oralQuestions) && oralQuestions.size() > 2){
                        pictureBook.put("oralQuestions", oralQuestions.subList(0,2));
                    }
                    if(CollectionUtils.isNotEmpty(practiceQuestions) && practiceQuestions.size() > 1){
                        pictureBook.put("practiceQuestions", practiceQuestions.subList(0,1));
                    }
                }
                mapMessage.put("pictureBook", pictureBook);
                return mapMessage;
            }else {
                return mapMessage;
            }
        }
    }

    @RequestMapping(value = "appdata/obtain/picturebook/dubbing.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage obtainPictureBookDubbingData() {
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id错误");
        }
        return pictureBookHomeworkServiceClient.getPictureBookPlusDubbingDraft(dubbingId);
    }

    /**
     * 字词讲练-模块数据
     * @return
     */
    @RequestMapping(value = "appdata/obtain/wordteach/module.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage obtainWordTeachModuleData() {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (UserType.STUDENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }

        String homeworkId = getRequestParameter("homeworkId", "");
        String stoneDataId = getRequestParameter("stoneDataId", "");
        String wordTeachModuleType = getRequestParameter("wordTeachModuleType", "");
        WordTeachModuleType practiceType = WordTeachModuleType.of(wordTeachModuleType);

        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        ObjectiveConfigType type = ObjectiveConfigType.WORD_TEACH_AND_PRACTICE;
        if (studentDetail != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("homeworkId", homeworkId);
            vars.put("userId", studentDetail.getId());
            vars.put("userName", studentDetail.fetchRealnameIfBlankId());
            vars.put("lessonId", newHomework.findPracticeContents().get(type).getApps().get(0).getLessonId());
            vars.put("objectiveConfigType", type);
            vars.put("objectiveConfigTypeName", type.getValue());
            vars.put("subject", newHomework.getSubject());
            vars.put("ipAddress", HttpRequestContextUtils.getWebAppBaseUrl());
            vars.put("learningType", StudyType.homework);
            vars.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            vars.put("stoneDataId", stoneDataId);
            vars.put("wordTeachModuleType", wordTeachModuleType);
            // 模块详情
            List<Map> moduleSummaryResult = wordTeachHomeworkServiceClient.getModuleSummaryInfo(homeworkId, studentDetail.getId(), stoneDataId, practiceType);
            vars.put("practices", moduleSummaryResult);
            if (WordTeachModuleType.IMAGETEXTRHYME == practiceType) {
                vars.put("voiceEngine", VoiceEngineType.Unisound.name());
                vars.put("unisound7SentencescoreLevels", newHomeworkContentServiceClient.loadUniSoundWordTeachSentenceScoreLevels(studentDetail));
                MapMessage voiceEngineConfigMessage = newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, type);
                String processVoiceEngine = "normal";
                String submitVoiceEngine = VoiceEngineType.Unisound.name();
                if (voiceEngineConfigMessage.isSuccess()) {
                    processVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("processVoiceEngine"));
                    submitVoiceEngine = SafeConverter.toString(voiceEngineConfigMessage.get("submitVoiceEngine"));
                }
                vars.put("processVoiceEngine", processVoiceEngine);
                vars.put("submitVoiceEngine", submitVoiceEngine);
            }
            return MapMessage.successMessage().add("data", vars);
        } else {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }
}
