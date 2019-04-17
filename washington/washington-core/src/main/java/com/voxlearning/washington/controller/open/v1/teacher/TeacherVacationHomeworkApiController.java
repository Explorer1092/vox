package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_BOOK;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.REQ_HOMEWORK_ID;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * Created by tanguohong on 2016/11/30.
 */
@Controller
@RequestMapping(value = "/v1/teacher/vacation")
public class TeacherVacationHomeworkApiController extends AbstractTeacherApiController {
    @Inject
    private VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient;

    /**
     * 假期作业学生个人报告
     *
     * @return
     */
    @RequestMapping(value = "student/report.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentPackageReport() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PACKAGE_ID);
            validateRequired(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            MapMessage message;
            try {
                message = vacationHomeworkReportLoaderClient.studentPackageReport(this.getRequestString(REQ_PACKAGE_ID), this.getRequestLong(REQ_STUDENT_ID));
            } catch (Exception e) {
                logger.error("get student package report failed : packageId of {},studentId of {}", this.getRequestString(REQ_PACKAGE_ID), this.getRequestLong(REQ_STUDENT_ID), e);
                return MapMessage.errorMessage();
            }
            if (message.isSuccess()) {
                String domain = getWebRequestContext().getWebAppBaseUrl();
                String html5Url = domain + "/view/vacationhomework/answerdetail";
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_HTML5URL, html5Url);
                resultMap.add(RES_WEEK_PLANS, message.get(RES_WEEK_PLANS));
                resultMap.add(RES_BOOK, message.get(RES_BOOK));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

    /**
     * 假期作业首页Index
     *
     * @return
     */
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadClazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        if (teacher.isJuniorTeacher()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_ANSWER_EXAM);
            return resultMap;
        }
        try {
            Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            String domain = getWebRequestContext().getWebAppBaseUrl();
            MapMessage message = vacationHomeworkLoaderClient.loadTeachersClazzListForApp(teacherIds, domain);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get("items"));
                resultMap.add("sharePackageInfo", message.get("sharePackageInfo"));
                resultMap.add("plannedDays", message.get("plannedDays"));
                Date date = new Date();
                Date earliestStartDate = NewHomeworkConstants.earliestVacationHomeworkStartDate(RuntimeMode.current());
                if (date.after(earliestStartDate)) {
                    earliestStartDate = date;
                }
                Date defaultStartDate = NewHomeworkConstants.VH_START_DATE_DEFAULT;
                if (date.after(defaultStartDate)) {
                    defaultStartDate = date;
                }
                boolean showLotteryEntrance = SafeConverter.toBoolean(message.get("showLotteryEntrance"));
                resultMap.add(RES_EARLIEST_START_TIME, earliestStartDate.getTime());
                resultMap.add(RES_DEFAULT_START_TIME, defaultStartDate.getTime());
                resultMap.add(RES_LATEST_START_TIME, NewHomeworkConstants.VH_START_DATE_LATEST.getTime());
                resultMap.add(RES_DEFAULT_END_TIME, NewHomeworkConstants.VH_END_DATE_DEFAULT.getTime());
                resultMap.add(RES_EARLIEST_END_TIME, NewHomeworkConstants.VH_END_DATE_EARLIEST.getTime());
                resultMap.add(RES_LATEST_END_TIME, NewHomeworkConstants.VH_END_DATE_LATEST.getTime());
                resultMap.add(RES_CONTENT_DESCRIPTION, message.get("contentDescription"));
                if (showLotteryEntrance) {
                    resultMap.add(RES_LOTTERY_NUMBER, SafeConverter.toInt(message.get("lotteryNumber")));
                    resultMap.add(RES_LOTTERY_URL, TopLevelDomain.getHttpsMainSiteBaseUrl() + "/view/mobile/teacher/activity2018/teacheraward_summer/index");
                }
                resultMap.add(RES_BANNER_URL, getCdnBaseUrlStaticSharedWithSep() + SafeConverter.toString(message.get("bannerUrl")));
                resultMap.add(RES_SHOW_LOTTERY_ENTRANCE, showLotteryEntrance);

                String bannerSlotId = "120109";
                String floatingAdSlotId = "120108";
                String ver = getRequestString(REQ_APP_NATIVE_VERSION);
                String sys = getRequestString(REQ_SYS);
                String ua = getRequest().getHeader("User-Agent");

                // banner广告位
                List<NewAdMapper> bannerAdList = userAdvertisementServiceClient.getUserAdvertisementService()
                        .loadNewAdvertisementData(teacher.getId(), bannerSlotId, getRequestString(REQ_SYS), ver);
                List<Map<String, Object>> bannerMapperList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(bannerAdList)) {
                    for (int index = 0; index < bannerAdList.size(); index++) {
                        NewAdMapper p = bannerAdList.get(index);
                        bannerMapperList.add(MapUtils.m(RES_BANNER_URL, combineCdbUrl(p.getImg()), RES_BANNER_JUMP_URL, AdvertiseRedirectUtils.redirectUrl(p.getId(), index, ver, sys, "", 0L)));

                        //曝光打点
                        if (Boolean.TRUE.equals(p.getLogCollected())) {
                            LogCollector.info("sys_new_ad_show_logs",
                                    MapUtils.map(
                                            "user_id", teacher.getId(),
                                            "env", RuntimeMode.getCurrentStage(),
                                            "version", ver,
                                            "aid", p.getId(),
                                            "acode", SafeConverter.toString(p.getCode()),
                                            "index", index,
                                            "slotId", bannerSlotId,
                                            "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                            "time", DateUtils.dateToString(new Date()),
                                            "agent", ua,
                                            "system", sys
                                    ));
                        }
                    }
                    resultMap.add(RES_BANNER_LIST, bannerMapperList);
                }

                // 浮窗广告位
                List<NewAdMapper> floatingAdList = userAdvertisementServiceClient.getUserAdvertisementService()
                        .loadNewAdvertisementData(teacher.getId(), floatingAdSlotId, getRequestString(REQ_SYS), ver);
                if (CollectionUtils.isNotEmpty(floatingAdList)) {
                    NewAdMapper p = floatingAdList.get(0);
                    resultMap.add(RES_FLOATING_AD_IMG_URL, combineCdbUrl(p.getImg()));
                    resultMap.add(RES_FLOATING_AD_JUMP_URL, AdvertiseRedirectUtils.redirectUrl(p.getId(), 0, ver, sys, "", 0L));

                    //曝光打点
                    if (Boolean.TRUE.equals(p.getLogCollected())) {
                        LogCollector.info("sys_new_ad_show_logs",
                                MapUtils.map(
                                        "user_id", teacher.getId(),
                                        "env", RuntimeMode.getCurrentStage(),
                                        "version", ver,
                                        "aid", p.getId(),
                                        "acode", SafeConverter.toString(p.getCode()),
                                        "index", 0,
                                        "slotId", floatingAdSlotId,
                                        "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                        "time", DateUtils.dateToString(new Date()),
                                        "agent", ua,
                                        "system", sys
                                ));
                    }
                }
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

    /**
     * 假期作业内容预览
     *
     * @return
     */
    @RequestMapping(value = "book/planinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage bookPlanInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "课本ID");
            validateRequest(REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            String bookId = getRequestString(REQ_BOOK_ID);
            MapMessage message = vacationHomeworkLoaderClient.loadBookPlanInfo(bookId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_BOOK, message.get(RES_BOOK));
                resultMap.add(RES_WEEK_PLANS, message.get(RES_WEEK_PLANS));
                resultMap.add(RES_PREVIEW_URL, "/view/vacationhomework/vacationdetail");
                resultMap.add(RES_DOMAIN, TopLevelDomain.getHttpsMainSiteBaseUrl());
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

    /**
     * 按天:查看作业内容
     * 暂时没发现用到
     *
     * @return
     */
    @RequestMapping(value = "day/planelements.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dayPlanElements() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "课本ID");
            validateRequired(REQ_WEEK_RANK, "第几周");
            validateRequired(REQ_DAY_RANK, "第几天");
            validateRequest(REQ_BOOK_ID, REQ_WEEK_RANK, REQ_DAY_RANK);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            String bookId = getRequestString(REQ_BOOK_ID);
            String weekRank = getRequestString(REQ_WEEK_RANK);
            String dayRank = getRequestString(REQ_DAY_RANK);
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = vacationHomeworkLoaderClient.loadDayPlanElements(teacherDetail, bookId, SafeConverter.toInt(weekRank), SafeConverter.toInt(dayRank));
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(REQ_DAY_PLAN_ELEMENTS, message.get("dayPlanElements"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

    /**
     * 布置假期作业
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveHomework() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkDate = getRequestString(REQ_HOMEWORK_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(homeworkDate);
        //添加布置作业ip
        String ip = getWebRequestContext().getRealRemoteAddress();
        jsonMap.put("ip", ip);
        Teacher teacher = getCurrentTeacher();

        HomeworkSource source = HomeworkSource.newInstance(jsonMap);
        MapMessage message = vacationHomeworkServiceClient.assignHomework(teacher, source, HomeworkSourceType.App);
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            StringUtils.formatMessage(message.getInfo() + "()");
            resultMap.add(RES_MESSAGE, message.getInfo() + "(" + message.getErrorCode() + ")");
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            //Date now = new Date();
            //if (now.after(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_ONLINE_DATE) && now.before(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_OFFLINE_DATE)) {
            //    resultMap.add(RES_LOTTERY_NUMBER, SafeConverter.toInt(message.get("lotteryNumber")));
            //    resultMap.add(RES_LOTTERY_URL, TopLevelDomain.getHttpsMainSiteBaseUrl() + "/view/mobile/teacher/activity2018/teacheraward_summer/index");
            //    resultMap.add(RES_INTEGRAL, SafeConverter.toInt(message.get("integral")));
            //}
        }
        return resultMap;

    }

    /**
     * 删除假期作业
     */
    @RequestMapping(value = "delete.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkDelete() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        MapMessage message = vacationHomeworkServiceClient.deleteHomework(teacher.getId(), homeworkId);
        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE, RES_RESULT_DELETE_SUCCESS_MSG);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        }
        return resultMap;
    }


    /**
     * 假期作业评语
     */
    @RequestMapping(value = "comment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkComment() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequired(REQ_HOMEWORK_COMMENT, "评语内容");
            validateRequest(REQ_HOMEWORK_ID, REQ_HOMEWORK_COMMENT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String comment = getRequestString(REQ_HOMEWORK_COMMENT);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        if (comment.length() > 100) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_COMMENT_TOO_LONG_MSG);
            return resultMap;
        }

        MapMessage message = vacationHomeworkServiceClient.vacationHomeworkComment(homeworkId, comment, null);
        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        }
        return resultMap;
    }

    /**
     * 奖励学豆
     */
    @RequestMapping(value = "rewardintegral.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkRewardIntegral() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequired(REQ_INTEGRAL, "学豆数");
            validateRequest(REQ_HOMEWORK_ID, REQ_INTEGRAL);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Integer integral = getRequestInt(REQ_INTEGRAL);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        MapMessage message = vacationHomeworkServiceClient.vacationHomeworkCommentRewardIntegral(teacherDetail, homeworkId, integral);
        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        }
        return resultMap;
    }

    /**
     * 获取评语模板
     */
    @RequestMapping(value = "loadcommenttemplates.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadCommentTemplates() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
        if (vacationHomework == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
            return resultMap;
        }

        List<String> commentTemplates = NewHomeworkConstants.commentTemplate(vacationHomework.getSubject());
        List<String> userComments = newHomeworkCacheServiceClient.getNewHomeworkCacheService().teacherNewHomeworkCommentLibraryManager_find(teacher.getId());
        List<Map<String, Object>> comments = new ArrayList<>();
        userComments.forEach(c -> comments.add(MapUtils.m("comment", c, "deletable", true)));
        commentTemplates.forEach(c -> comments.add(MapUtils.m("comment", c, "deletable", false)));
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.put(RES_HOMEWORK_COMMENT_TEMPLATE, comments);
        return resultMap;
    }

    /**
     * 老师写（文字评语、音频评语）同时奖励学豆
     */
    @RequestMapping(value = "writecommentaddintegral.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage writeCommentAddIntegral() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            //其余几个参数可以为空字符串
            validateRequest(
                    REQ_HOMEWORK_ID,
                    REQ_HOMEWORK_COMMENT,
                    REQ_HOMEWORK_AUDIO_COMMENT,
                    REQ_INTEGRAL
            );
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        int integral = getRequestInt(REQ_INTEGRAL);
        String comment = getRequestString(REQ_HOMEWORK_COMMENT);
        String audioComment = getRequestString(REQ_HOMEWORK_AUDIO_COMMENT);

        boolean needComment = StringUtils.isNotBlank(comment) || StringUtils.isNotBlank(audioComment);
        boolean needIntegral = integral > 0;

        if (StringUtils.isNotBlank(comment) && comment.length() > 100) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_COMMENT_TOO_LONG_MSG);
            return resultMap;
        }

        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        if (!needComment && !needIntegral) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "请选择奖励或评语内容");
            return resultMap;
        }

        VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
        if (vacationHomework == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
            return resultMap;
        }

        if (needIntegral) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = vacationHomeworkServiceClient.vacationHomeworkCommentRewardIntegral(teacherDetail, homeworkId, integral);
            if (!message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
                return resultMap;
            }
        }

        if (needComment) {
            MapMessage message = vacationHomeworkServiceClient.vacationHomeworkComment(homeworkId, comment, audioComment);
            if (!message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
                return resultMap;
            } else {
                List<String> commentTemplates = NewHomeworkConstants.commentTemplate(vacationHomework.getSubject());
                if (StringUtils.isNotBlank(comment) && !commentTemplates.contains(comment)) {
                    newHomeworkCacheServiceClient.getNewHomeworkCacheService().teacherNewHomeworkCommentLibraryManager_addComment(teacher.getId(), comment);
                }
            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}
