package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLivecastLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusDubbing;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.service.LiveCastGenerateDataService;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkLivecastService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.net.message.exam.SaveHomeworkResultRequest;
import com.voxlearning.washington.net.message.exam.SaveNewHomeworkResultRequest;
import com.voxlearning.washington.service.LoadFlashGameContext;
import com.voxlearning.washington.service.LoadFlashGameContextFactory;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.flash.FlashVars;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.ErrorCodeConstants.ERROR_CODE_PARAMETER;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.AllowUserTokenTypes;

/**
 * 注：这个controller的RequestMapping严禁修改
 *
 * @author xuesong.zhang
 * @since 2016/12/28
 */
@Controller
@RequestMapping("/livecast/student/homework")
//@RequestMapping("/lc/student/homework")
public class StudentLiveCastHomeworkController extends AbstractController {

    @Getter
    @ImportService(interfaceClass = NewHomeworkLivecastLoader.class)
    private NewHomeworkLivecastLoader newHomeworkLivecastLoader;

    @Getter
    @ImportService(interfaceClass = NewHomeworkLivecastService.class)
    private NewHomeworkLivecastService newHomeworkLivecastService;

    @Getter
    @ImportService(interfaceClass = LiveCastGenerateDataService.class)
    private LiveCastGenerateDataService liveCastGenerateDataService;

    // 作业首页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {

        String homeworkId = getRequestParameter("homeworkId", "");
        String token = getRequestString("token");

        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
        if (liveCastHomework == null) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }
        Map<String, Object> homeworkList = liveCastGenerateDataService.generateIndexData(homeworkId, user.getId(), token);
        if (homeworkList.isEmpty()) {
            return MapMessage.errorMessage("作业已经不存在了");
        }
        return MapMessage.successMessage().add("homeworkList", homeworkList);
    }


    @RequestMapping(value = "do.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage doHomework(HttpServletRequest request) {

        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        String token = getRequestString("token");
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigType = getRequestString("objectiveConfigType");
        String lessonId = getRequestParameter("lessonId", "");
        String categoryId = getRequestParameter("categoryId", "");
        String practiceId = getRequestParameter("practiceId", "");
        String pictureBookIds = getRequestString("pictureBookIds");

        LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
        if (liveCastHomework == null) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }
        ObjectiveConfigType type = ObjectiveConfigType.of(objectiveConfigType);
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail != null) {

            FlashVars vars = new FlashVars(request);
            vars.add("uid", studentDetail.getId());
            vars.add("hid", homeworkId);
            vars.add("userId", studentDetail.getId());
            vars.add("homeworkId", homeworkId);
            vars.add("objectiveConfigType", objectiveConfigType);
            vars.add("objectiveConfigTypeName", type != null ? type.getValue() : "");
            vars.add("subject", liveCastHomework.getSubject());
            vars.add("ipAddress", HttpRequestContextUtils.getWebAppBaseUrl());
            vars.add("learningType", StudyType.livecastHomework);
            vars.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());


            if (ObjectiveConfigType.BASIC_APP.name().equals(objectiveConfigType)) {
                // 8分制一起保留下来吧
                vars.add("unisound8", true);
                vars.add("unisound8WordscoreLevels", newHomeworkContentServiceClient.loadUniSoundWordScoreLevels(studentDetail));
                vars.add("unisound8SentencescoreLevels", newHomeworkContentServiceClient.loadUniSoundSentenceScoreLevels(studentDetail));

                List<PracticeType> practiceTypes = practiceServiceClient.getPracticeBuffer().loadCategoriedIdPractices(SafeConverter.toInt(categoryId));

                List<Map> practices = new ArrayList<>();
                for (PracticeType practiceType : practiceTypes) {
                    if (!PracticeCategory.categoryPracticeTypesMap.get(SafeConverter.toInt(categoryId)).contains(practiceType.getId())) {
                        continue;
                    }
                    practices.add(MapUtils.m(
                            "appUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/pc" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "practiceId", practiceType.getId(),
                                            "hid", homeworkId,
                                            "lessonId", lessonId,
                                            "newHomeworkType", liveCastHomework.getType(),
                                            "objectiveConfigType", objectiveConfigType,
                                            "token", token)),

                            "appMobileUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/mobile" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "practiceId", practiceType.getId(),
                                            "hid", homeworkId,
                                            "lessonId", lessonId,
                                            "newHomeworkType", liveCastHomework.getType(),
                                            "objectiveConfigType", objectiveConfigType,
                                            "token", token)),

                            "fileName", practiceType.getFilename(),
                            "practiceId", practiceType.getId(),
                            "practiceName", practiceType.getPracticeName(),
                            "categoryId", practiceType.getCategoryId(),
                            "categoryName", practiceType.getCategoryName(),
                            "lessonId", lessonId,
                            "needRecord", practiceType.getNeedRecord(),
                            "checked", practiceType.getId().equals(SafeConverter.toLong(practiceId)),
                            "questionUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/questions" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "objectiveConfigType", objectiveConfigType,
                                            "homeworkId", homeworkId,
                                            "lessonId", lessonId,
                                            "categoryId", practiceType.getCategoryId(),
                                            "token", token)),

                            "completedUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/questions/answer" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "objectiveConfigType", objectiveConfigType,
                                            "homeworkId", homeworkId,
                                            "lessonId", lessonId,
                                            "categoryId", practiceType.getCategoryId(),
                                            "token", token))
                    ));
                }
                vars.add("practices", practices);
                vars.add("objectiveConfigType", objectiveConfigType);
            } else if (StringUtils.equalsIgnoreCase(ObjectiveConfigType.READING.name(), objectiveConfigType)) {
                String[] picBookIds = StringUtils.split(pictureBookIds, ",");
                if (picBookIds == null || picBookIds.length <= 0) {
                    return MapMessage.errorMessage("阅读绘本不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_PICTURE_BOOK_IS_NULL);
                }
                List<PictureBookSummaryResult> picBookResult = pictureBookHomeworkServiceClient.getLiveCastPictureBookSummaryInfo(liveCastHomework, Arrays.asList(picBookIds), studentDetail.getId(), token);

                vars.add("practices", picBookResult);
                vars.add("objectiveConfigType", objectiveConfigType);
            } else if (ObjectiveConfigType.LEVEL_READINGS.name().equalsIgnoreCase(objectiveConfigType)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
                if (newHomeworkPracticeContent == null || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
                    return MapMessage.errorMessage("作业内容错误");
                }
                List<String> picBookIds = new ArrayList<>();
                for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
                    if (StringUtils.isNotBlank(newHomeworkApp.getPictureBookId())) {
                        picBookIds.add(newHomeworkApp.getPictureBookId());
                    }
                }
                List<PictureBookPlusSummaryResult> picBookResult = pictureBookHomeworkServiceClient.getLiveCastPictureBookPlusSummaryInfo(liveCastHomework, picBookIds, studentDetail.getId(), token);
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
            } else if (ObjectiveConfigType.DUBBING.name().equalsIgnoreCase(objectiveConfigType)) {
                List<DubbingSummaryResult> dubbingSummaryResults = dubbingHomeworkServiceClient.getLiveCastDubbingSummerInfo(homeworkId, studentDetail.getId());
                vars.add("practices", dubbingSummaryResults);
            } else {
                vars.add("questionUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/questions" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "objectiveConfigType", objectiveConfigType,
                                "homeworkId", homeworkId,
                                "token", token)));

                vars.add("completedUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/questions/answer" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "objectiveConfigType", objectiveConfigType,
                                "homeworkId", homeworkId,
                                "token", token)));

                vars.add("processResultUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/processresult" + Constants.AntiHijackExt,
                        MapUtils.m("token", token)));
            }

            String flashVars = vars.getJsonParam();
            Map<String, Object> data = new HashMap<>();
            data.put("flashVars", flashVars);
            return MapMessage.successMessage().add("data", data);
        } else {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }


    @RequestMapping(value = "type/result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage typeResult() {

        String token = getRequestString("token");
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigType = getRequestString("objectiveConfigType");

        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);

        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail != null) {
            return liveCastGenerateDataService.homeworkForObjectiveConfigTypeResult(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail.getId());
        } else {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map getQuestions() {

        String token = getRequestString("token");
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigTypeStr = getRequestString("objectiveConfigType");
        Integer categoryId = getRequestInt("categoryId", 0);
        String lessonId = getRequestString("lessonId");
        String videoId = getRequestString("videoId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        return MapMessage.successMessage().add("result", liveCastGenerateDataService.loadHomeworkQuestions(homeworkId, objectiveConfigType, categoryId, lessonId, videoId));
    }

    @RequestMapping(value = "questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map getQuestionsAnswer() {

        String token = getRequestString("token");
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigTypeStr = getRequestString("objectiveConfigType");
        Integer categoryId = getRequestInt("categoryId", 0);
        String lessonId = getRequestString("lessonId");
        String videoId = getRequestString("videoId");

        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        User student = currentStudent();
        if (student != null) {
            return MapMessage.successMessage().add("result", liveCastGenerateDataService.loadHomeworkQuestionsAnswer(objectiveConfigType, homeworkId, student.getId(), categoryId, lessonId, videoId));
        } else {
            return MapMessage.errorMessage("请登录");
        }
    }

    @RequestMapping(value = "processresult.vpage")
    @ResponseBody
    public MapMessage processResult() {
        User user = currentUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PROCESS_OLYMPIC_RESULT", "/livecast/student/homework/processresult.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        String token = getRequestString("token");
        String data = getRequestParameter("data", "");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }

        try {
            SaveHomeworkResultRequest result = JsonUtils.fromJson(data, SaveHomeworkResultRequest.class);
            if (result == null || StringUtils.isBlank(result.getHomeworkId()) || StringUtils.isBlank(result.getQuestionId())) {
                return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
            }

            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(result.getObjectiveConfigType());
            if (objectiveConfigType == null) return MapMessage.errorMessage("作业形式为空" + JsonUtils.toJson(result));
            StudyType studyType = StudyType.of(result.getLearningType());
            if (studyType == null) {
                return MapMessage.errorMessage("学习类型异常" + JsonUtils.toJson(result));
            }

            LiveCastHomeworkResultContext context = new LiveCastHomeworkResultContext();
            context.setUserId(user.getId());
            context.setUser(user);
            context.setHomeworkId(result.getHomeworkId());
            context.setLearningType(studyType);
            context.setObjectiveConfigType(objectiveConfigType);
            context.setBookId(result.getBookId());
            context.setUnitId(result.getUnitId());
            context.setUnitGroupId(result.getUnitGroupId());
            context.setLessonId(result.getLessonId());
            context.setSectionId(result.getSectionId());
            context.setClientType(result.getClientType());
            context.setClientName(result.getClientName());
            context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : getWebRequestContext().getRealRemoteAddress());
            context.setUserAgent(getRequest().getHeader("User-Agent"));
            StudentHomeworkAnswer sha = new StudentHomeworkAnswer();
            sha.setAnswer(result.getAnswer());
            sha.setDurationMilliseconds(NewHomeworkUtils.processDuration(result.getDuration()));
            sha.setFileUrls(result.getFileUrls());
            sha.setQuestionId(result.getQuestionId());
            context.setStudentHomeworkAnswers(Collections.singletonList(sha));

            MapMessage mapMessage = newHomeworkLivecastService.processorHomeworkResult(context);
            if (mapMessage.isSuccess()) {
                return MapMessage.successMessage().add("result", mapMessage.get("result"));
            } else {
                return MapMessage.errorMessage("提交结果失败").setErrorCode(mapMessage.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error("Failed to save user {} LiveCastHomework result, msg:{}", user.getId(), ex.getMessage());
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    @RequestMapping(value = "batch/processresult.vpage")
    @ResponseBody
    public MapMessage batchProcessResult() {
        User user = currentUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PROCESS_OLYMPIC_RESULT", "/livecast/student/homework/batch/processresult.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        String token = getRequestString("token");
        String data = getRequestParameter("data", "");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }

        try {
            SaveNewHomeworkResultRequest result = JsonUtils.fromJson(data, SaveNewHomeworkResultRequest.class);
            if (result == null
                    || StringUtils.isBlank(result.getHomeworkId())
                    || (CollectionUtils.isEmpty(result.getStudentHomeworkAnswers()) && !StringUtils.equalsIgnoreCase(ObjectiveConfigType.LEVEL_READINGS.name(), result.getObjectiveConfigType()))) {
                return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
            }

            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(result.getObjectiveConfigType());
            if (objectiveConfigType == null) return MapMessage.errorMessage("作业形式为空" + JsonUtils.toJson(result));
            StudyType studyType = StudyType.of(result.getLearningType());
            if (studyType == null) {
                return MapMessage.errorMessage("学习类型异常" + JsonUtils.toJson(result));
            }
            LiveCastHomeworkResultContext context = new LiveCastHomeworkResultContext();
            context.setUserId(user.getId());
            context.setUser(user);
            context.setHomeworkId(result.getHomeworkId());
            context.setLearningType(studyType);
            context.setObjectiveConfigType(objectiveConfigType);
            context.setBookId(result.getBookId());
            context.setUnitId(result.getUnitId());
            context.setUnitGroupId(result.getUnitGroupId());
            context.setLessonId(result.getLessonId());
            context.setSectionId(result.getSectionId());
            context.setPracticeId(result.getPracticeId());
            context.setPictureBookId(result.getPictureBookId());
            context.setDubbingId(result.getDubbingId());
            context.setVideoUrl(result.getVideoUrl());
            context.setDurations(result.getDurations());
            context.setClientType(result.getClientType());
            context.setClientName(result.getClientName());
            context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : getWebRequestContext().getRealRemoteAddress());
            context.setUserAgent(getRequest().getHeader("User-Agent"));
            context.setStudentHomeworkAnswers(result.getStudentHomeworkAnswers());

            if (Objects.equals(objectiveConfigType, ObjectiveConfigType.READING)) {
                // 绘本特有
                context.setStudentHomeworkOralAnswers(result.getStudentHomeworkOralAnswers());
                // 绘本的总耗时，超过1个小时，按1个小时处理
                if (result.getConsumeTime() != null) {
                    Long doQuestionTime = result.getStudentHomeworkAnswers()
                            .stream()
                            .filter(o -> o.getDurationMilliseconds() != null && o.getDurationMilliseconds() > 0)
                            .mapToLong(StudentHomeworkAnswer::getDurationMilliseconds)
                            .sum();

                    Long doOralQuestionTime = 0L;
                    if (CollectionUtils.isNotEmpty(result.getStudentHomeworkOralAnswers())) {
                        doOralQuestionTime = result.getStudentHomeworkOralAnswers()
                                .stream()
                                .filter(o -> o.getDurationMilliseconds() != null && o.getDurationMilliseconds() > 0)
                                .mapToLong(StudentHomeworkAnswer::getDurationMilliseconds)
                                .sum();
                    }
                    Long totalDoQuestionTime = doQuestionTime + doOralQuestionTime;
                    Long maxDuration = 3600000L;
                    if (result.getConsumeTime() > maxDuration) {
                        context.setConsumeTime(maxDuration);
                    } else if (result.getConsumeTime() < totalDoQuestionTime && totalDoQuestionTime < maxDuration) {
                        context.setConsumeTime(totalDoQuestionTime);
                    } else {
                        context.setConsumeTime(result.getConsumeTime());
                    }
                }
            } else if (Objects.equals(objectiveConfigType, ObjectiveConfigType.DUBBING)) {
                context.setConsumeTime(result.getConsumeTime());
            } else if (Objects.equals(objectiveConfigType, ObjectiveConfigType.LEVEL_READINGS)) {
                context.setStudentHomeworkOralAnswers(result.getStudentHomeworkOralAnswers());
            }

            MapMessage mapMessage = newHomeworkLivecastService.processorHomeworkResult(context);
            if (mapMessage.isSuccess()) {
                return MapMessage.successMessage().add("result", mapMessage.get("result"));
            } else {
                return MapMessage.errorMessage("提交结果失败").setErrorCode(mapMessage.getErrorCode());
            }
        } catch (Exception e) {
            logger.error("Failed to batch save user {} LiveCastHomework result, msg:{}", user.getId(), e.getMessage());
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    @RequestMapping(value = "pc.vpage", method = RequestMethod.GET)
    public String newHomeworkMobile(Model model) {
        if (currentUserId() == null || !getWebRequestContext().isCurrentUserStudent())
            return "redirect:/";
        Long practiceId = getRequestLong("practiceId");
        String lessonId = getRequestString("lessonId");
        String hid = getRequestParameter("hid", "0");
        String pictureBookId = getRequestString("pictureBookId");
        String homeworkType = getRequestString("newHomeworkType");
        String objectiveConfigType = getRequestParameter("objectiveConfigType", StringUtils.isNotBlank(pictureBookId) ? ObjectiveConfigType.READING.name() : ObjectiveConfigType.BASIC_APP.name());
        StudentDetail studentDetail = currentStudentDetail();
        String token = getRequestString("token");
        //在套壳的情况下会出现参数异常，一般情况下判断这俩就行
        if (practiceId == 0L || "0".equals(hid)) return "redirect:/";

        if ((studentDetail != null && studentDetail.getClazz() != null) || (studentDetail != null && AllowUserTokenTypes.contains(NewHomeworkType.valueOf(homeworkType)))) {
            model.addAllAttributes(newHomework(studentDetail, practiceId, lessonId, pictureBookId, hid, homeworkType, objectiveConfigType, token));
            return "flash/loader";
            //return displayFlashLoader(model);
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping(value = "mobile.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newHomeworkMobile() {
        if (currentUserId() == null || !getWebRequestContext().isCurrentUserStudent()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long practiceId = getRequestLong("practiceId");
        String lessonId = getRequestString("lessonId");
        String hid = getRequestParameter("hid", "0");
        String pictureBookId = getRequestString("pictureBookId");
        String homeworkType = getRequestString("newHomeworkType");
        String objectiveConfigType = getRequestParameter("objectiveConfigType", StringUtils.isNotBlank(pictureBookId) ? ObjectiveConfigType.READING.name() : ObjectiveConfigType.BASIC_APP.name());
        StudentDetail studentDetail = currentStudentDetail();
        String token = getRequestString("token");

        if ((studentDetail != null && studentDetail.getClazz() != null) || (studentDetail != null && NewHomeworkConstants.AllowUserTokenTypes.contains(NewHomeworkType.valueOf(homeworkType)))) {

            Map data = newHomework(studentDetail, practiceId, lessonId, pictureBookId, hid, homeworkType, objectiveConfigType, token);
            if (MapUtils.isNotEmpty(data)) {
                data.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            }
            return MapMessage.successMessage().add("data", data);
        } else {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    private Map newHomework(StudentDetail studentDetail, Long practiceId, String lessonId, String pictureBookId, String hid, String homeworkType, String objectiveConfigType, String token) {
        Clazz clazz = studentDetail.getClazz();
        Long clazzId = clazz != null ? clazz.getId() : 0;
        PracticeType practice = practiceServiceClient.getPracticeBuffer().loadPractice(practiceId);
        LoadFlashGameContext context = LoadFlashGameContextFactory.newHomework(practice, studentDetail.getId(), clazzId, hid, lessonId, pictureBookId, homeworkType, objectiveConfigType);
        context.setNewHomeworkType(homeworkType);
        context.setToken(token);

        // 作业上传录音的游戏需要提供数据分析参数年级和地理信息,这段代码还有用么？没用就删了吧，xuesong.zhang 2016-10-19
//        if (clazz != null && !StringUtils.equalsIgnoreCase(NewHomeworkType.USTalk.name(), homeworkType)) {
//            if (context.getEnglishPractice().fetchNeedRecord()) {
//                context.setClazzLevel(clazz.getClazzLevel().getLevel());
//                if (studentDetail.getStudentSchoolRegionCode() != null && studentDetail.getStudentSchoolRegionCode() != 0) {
//                    ExRegion exRegion = regionServiceClient.getExRegionBuffer().loadRegion(studentDetail.getStudentSchoolRegionCode());
//                    if (exRegion != null) {
//                        context.setPrvRgnCode(exRegion.getProvinceCode());
//                    }
//                }
//            }
//        }

        return loadNewHomeworkGameFlash(context);
    }

    @RequestMapping(value = "appdata/obtain.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String obtainMobileData() {
        User user = currentUser();
        if (user == null) return JsonUtils.toJson(MapMessage.errorMessage("请重新登录"));

        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "APP_DATA", "livecast/student/homework/appdata/obtain.vpage", 40)) {
            return JsonUtils.toJson(MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。"));
        }

        String sb = getRequestString("subject");
        Subject subject = Subject.ofWithUnknown(sb);
        String flashGameName = getRequestString("practiceName");
        String lessonId = getRequestString("lessonId");
        String jsonpCallback = getRequestParameter("callback", "");
        String hid = getRequestString("hid");
        Integer categoryId = getRequestInt("categoryId");
        String qids = getRequestString("qids");
        String pictureBookId = getRequestString("pictureBookId");
        String objectiveConfigType = getRequestString("objectiveConfigType");
        Long userId = currentUserId();
        NewHomeworkType newHomeworkType = NewHomeworkType.of(getRequestString("newHomeworkType"));
        PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadNamedPractice(flashGameName);
        String bookId = getRequestString("bookId");

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
                String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
                Ktwelve k12 = Ktwelve.PRIMARY_SCHOOL;
                // qids为预览特有属性
                if (StringUtils.isNotBlank(qids)) {
                    String[] questionIds = StringUtils.split(qids, ",");
                    if (questionIds != null && questionIds.length > 0) {
                        gameData = flashGameServiceClient.loadPreviewNewDate(userId, cdnUrl, lessonId, practiceType, k12, Arrays.asList(questionIds), true, bookId);
                    } else {
                        gameData = MapMessage.errorMessage("题目不存在");
                    }
                } else {
                    gameData = flashGameServiceClient.loadNewData(userId, cdnUrl, lessonId, practiceType, k12, hid, categoryId, true, newHomeworkType, objectiveConfigType);
                }
//                }
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

    /**
     * 上传新绘本配音
     */
    @RequestMapping(value = "picturebookplus/uploaddubbing.vpage")
    @ResponseBody
    public MapMessage uploadPictureBookPlusDubbing() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PICTURE_BOOK_PLUS_UPLOAD_DUBBING", "livecast/student/homework/picturebookplus/uploaddubbing.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != currentUser().fetchUserType()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        String homeworkId = getRequestString("homeworkId");
        String pictureBookId = getRequestString("pictureBookId");
        String screenMode = getRequestString("screenMode");
        String contentsJson = getRequestString("contents");
        if (StringUtils.isEmpty(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (StringUtils.isEmpty(pictureBookId)) {
            return MapMessage.errorMessage("绘本id为空");
        }
        List<PictureBookPlusDubbing.Content> contents = JsonUtils.fromJsonToList(contentsJson, PictureBookPlusDubbing.Content.class);
        if (CollectionUtils.isEmpty(contents)) {
            return MapMessage.errorMessage("绘本内容为空");
        }
        return newHomeworkServiceClient.uploadLiveCastPictureBookPlusDubbing(homeworkId, pictureBookId, user.getId(), contents, screenMode);
    }

    @RequestMapping(value = "picturebook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage obtainPictureBookData() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "APP_DATA", "livecast/student/homework/picturebook.vpage", 40)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        String pictureBookId = getRequestString("pictureBookId");
        if (StringUtils.isBlank(pictureBookId)) {
            return MapMessage.errorMessage("绘本id错误");
        }

        return pictureBookHomeworkServiceClient.getPictureBookPlusDraft(pictureBookId);
    }

}
