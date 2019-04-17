/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.net.message.exam.CorrectQuestionRequest;
import com.voxlearning.washington.net.message.exam.SaveCorrectQuestionRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/new/homework")
public class TeacherNewHomeworkReportController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;

    /**
     * 老师批改试题，需要前段对老师未作批改动作的数据进行控制
     * xuesong.zhang
     */
    @RequestMapping(value = "batchcorrectquestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchCorrectQuestion() {
        SaveCorrectQuestionRequest reqObj = JsonUtils.fromJson(getRequestString("data"), SaveCorrectQuestionRequest.class);
        if (reqObj == null || StringUtils.isBlank(reqObj.getHomeworkId())) {
            return MapMessage.errorMessage("提交结果数据异常");
        }
        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(reqObj.getHomeworkId());
        if (homework == null) {
            return MapMessage.errorMessage("作业不存在或者已被删除");
        }
        if (homework.getCreateAt() != null && homework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            return MapMessage.errorMessage("此份作业已不允许批改");
        }
        if (Objects.equals(Boolean.TRUE, reqObj.getIsBatch())) {
            // 批量
            newHomeworkServiceClient.batchSaveHomeworkCorrect(reqObj.getHomeworkId(), currentUserId());
            return MapMessage.successMessage("批改成功");
        } else {
            // 非批量
            List<Boolean> booleanList = new ArrayList<>();
            List<CorrectQuestionRequest> corrections = reqObj.getCorrections();
            List<CorrectQuestionRequest> returnInfo = new ArrayList<>();
            for (CorrectQuestionRequest obj : corrections) {
                Boolean b = saveCorrectQuestionResult(obj.getUserId(), reqObj.getHomeworkId(), reqObj.getType(), reqObj.getQuestionId(), obj, reqObj.getIsBatch());
                booleanList.add(b);
                if (Objects.equals(Boolean.TRUE, b)) {
                    // 返回成功,用于回显
                    returnInfo.add(obj);
                }
            }
            long s = booleanList
                    .stream()
                    .filter(o -> Objects.equals(Boolean.FALSE, o))
                    .count();
            if (s > 0) {
                return MapMessage.errorMessage("有" + s + "道题未批改或批改失败").add("questioninfos", returnInfo);
            }
            return MapMessage.successMessage("批改成功").add("questioninfos", returnInfo);
        }
    }


    private Boolean saveCorrectQuestionResult(Long studentId, String homeworkId, ObjectiveConfigType type, String questionId, CorrectQuestionRequest correct, Boolean isBatch) {
        CorrectHomeworkContext context = new CorrectHomeworkContext();

        if (type == ObjectiveConfigType.NEW_READ_RECITE) {
            context.setQuestionBoxId(questionId);
        }
        context.setStudentId(studentId);
        context.setHomeworkId(homeworkId);
        context.setQuestionId(questionId);
        context.setReview(correct.getReview());
        context.setType(type);
        context.setCorrectType(correct.getCorrectType());
        context.setCorrection(correct.getCorrection());
        context.setTeacherMark(correct.getTeacherMark());
        context.setIsBatch(isBatch);
        return newHomeworkProcessResultLoaderClient.updateCorrection(context);
    }

    /**
     * NEW HOMEWORK 作业历史班级列表
     */
    @RequestMapping(value = "report/list.vpage", method = RequestMethod.GET)
    public String historyIndex(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<Map<String, Object>> clazzMaps = getClazzList();
        Set<Long> clazzIds = clazzMaps
                .stream()
                .map(clazz -> SafeConverter.toLong(clazz.get("id")))
                .collect(Collectors.toSet());
        Map<Long, List<GroupMapper>> groupMaps = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds);
        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getTeacherGroupIds(teacher.getId());
        LinkedHashMap<Integer, List<Map<String, Object>>> clazzLevelMap = new LinkedHashMap<>();
        for (Map<String, Object> clazzMap : clazzMaps) {
            Long clazzId = SafeConverter.toLong(clazzMap.get("id"));
            List<GroupMapper> groupMappers = groupMaps.get(clazzId);
            if (CollectionUtils.isNotEmpty(groupMappers)) {
                for (GroupMapper groupMapper : groupMappers) {
                    if (teacherGroupIds.contains(groupMapper.getId())) {
                        clazzMap.put("clazzGroupId", groupMapper.getId());
                        break;
                    }
                }
                Integer clazzLevel = SafeConverter.toInt(clazzMap.get("classLevel"));
                clazzLevelMap.computeIfAbsent(clazzLevel, k -> new ArrayList<>()).add(clazzMap);
            }
        }
        model.addAllAttributes(
                MapUtils.m(
                        "clazzLevelMap", JsonUtils.toJson(clazzLevelMap.values()),
                        "currentDayEndDate", DateUtils.dateToString(DayRange.current().getEndDate()),
                        "curSubject", teacher.getSubject()
                ));
        return "teacherv3/homeworkhistoryv3/list";
    }


    /**
     * NEW HOMEWORK 通过班组查作业列表
     */
    //1、获取班组ID
    //2、过滤班组
    //3、获取信息
    @RequestMapping(value = "report/homeworklist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkHistory(@RequestParam("groupIds") String groupIds) {
        try {
            Teacher teacher = getSubjectSpecifiedTeacher();
            List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .getTeacherGroupIds(teacher.getId());
            List<Long> clazzGroupIds = new ArrayList<>();
            for (String id : groupIds.split(",")) {
                Long groupId = SafeConverter.toLong(id);
                if (!teacherGroupIds.contains(groupId)) {
                    return MapMessage.errorMessage("The teacher does not have this groupId:{}", id);
                }
                clazzGroupIds.add(groupId);
            }
            Integer currentPage = getRequestInt("currentPage", 1);
            Pageable pageable = new PageRequest(currentPage - 1, 10);
            Page<Map<String, Object>> page = newHomeworkReportServiceClient.pageHomeworkReportListByGroupIds(clazzGroupIds, pageable, teacher.getSubject());
            return MapMessage.successMessage().add("page", page);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }


    @RequestMapping(value = "report/oldhomeworklist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage oldHomeworkHistory(@RequestParam("groupIds") String groupIds) {
        try {
            String beginStr = this.getRequestString("begin");
            if (StringUtils.isBlank(beginStr)) {
                return MapMessage.errorMessage("开始时间参数错误");
            }
            Date begin = DateUtils.stringToDate(beginStr, "yyyy-MM-dd");
            if (begin == null) {
                return MapMessage.errorMessage("开始时间参数错误");
            }
            Date end = DateUtils.nextDay(begin, NewHomeworkConstants.LIMIT_SELECT_OLD_HOMEWORK);
            Teacher teacher = getSubjectSpecifiedTeacher();
            List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .getTeacherGroupIds(teacher.getId());
            List<Long> clazzGroupIds = new ArrayList<>();
            for (String id : groupIds.split(",")) {
                Long groupId = SafeConverter.toLong(id);
                if (!teacherGroupIds.contains(groupId)) {
                    return MapMessage.errorMessage("The teacher does not have this groupId:{}", id);
                }
                clazzGroupIds.add(groupId);
            }
            Integer currentPage = getRequestInt("currentPage", 1);
            Pageable pageable = new PageRequest(currentPage - 1, 10);
            Page<Map<String, Object>> page = newHomeworkReportServiceClient.pageHomeworkReportListByGroupIds(clazzGroupIds, pageable, teacher.getSubject(), begin, end);
            return MapMessage.successMessage().add("page", page);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }


    /**
     * NEW HOMEWORK 作业历史详情首页
     */
    @RequestMapping(value = "report/detail.vpage", method = RequestMethod.GET)
    public String homeworkReportDetail(Model model) {
        String homeworkId = getRequestString("homeworkId");
        model.addAttribute("homeworkId", homeworkId);
        MapMessage mapMessage = newHomeworkReportServiceClient.reportDetailIndex(currentTeacher(), homeworkId);
        if (mapMessage.isSuccess()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> homeworkInfo = (Map<String, Object>) mapMessage.get("homeworkInfo");
            if (homeworkInfo != null) {
                model.addAllAttributes(
                        MapUtils.m(
                                "homeworkType", homeworkInfo.get("homeworkType"),
                                "includeSubjective", homeworkInfo.get("includeSubjective"),
                                "clazzId", homeworkInfo.get("clazzId"),
                                "curSubject", homeworkInfo.get("subject")
                        )
                );
            } else {
                logger.error("homeworkInfo is null");
            }
        }
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "teacherv3/homeworkhistoryv3/clazzreport";
    }


    @RequestMapping(value = "report/studentpart.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchStudentDetailInfo() {
        Teacher teacher = currentTeacher();
        String homeworkId = getRequestString("homeworkId");
        if (teacher == null || StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("老师未登入，或者参数作业ID不存在");
        }
        return newHomeworkReportServiceClient.fetchStudentDetailPart(teacher, homeworkId);
    }

    @RequestMapping(value = "report/questionpart.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchQuestionDetailPart() {
        Teacher teacher = currentTeacher();
        String homeworkId = getRequestString("homeworkId");
        if (teacher == null || StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("老师未登入，或者参数作业ID不存在");
        }
        return newHomeworkReportServiceClient.fetchQuestionDetailPart(teacher, homeworkId, getCdnBaseUrlAvatarWithSep());
    }


    @RequestMapping(value = "report/student.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkReportForStudent() {
        Teacher teacher = currentTeacher();
        String homeworkId = getRequestString("homeworkId");
        if (teacher == null || StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("老师未登入，或者参数作业ID不存在");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.homeworkReportForStudent(teacher, homeworkId, false);
        Date endDate = new Date(System.currentTimeMillis() + 300000);
        mapMessage.add("nowEndTime", DateUtils.dateToString(endDate, "HH:mm"));
        return mapMessage;
    }


    @RequestMapping(value = "report/pc/student.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentHomeworkReport() {
        Teacher teacher = currentTeacher();
        String homeworkId = getRequestString("homeworkId");
        if (teacher == null || StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("老师未登入，或者参数作业ID不存在");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.homeworkReportForStudent(teacher, homeworkId, true);
        Date endDate = new Date(System.currentTimeMillis() + 300000);
        mapMessage.add("nowEndTime", DateUtils.dateToString(endDate, "HH:mm"));
        return mapMessage;
    }

    /**
     * 教师APP-学生课文读背答题详情
     *
     * @return
     */
    @RequestMapping(value = "report/personalreadrecitewithscore.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadReciteWithScore() {
        String hid = getRequestString("hid");
        String questionBoxId = getRequestString("questionBoxId");
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.personalReadReciteWithScore(hid, questionBoxId, sid);
        mapMessage.putAll(
                MapUtils.m(
                        "questionUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", "")),
                        "completedUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions/answer" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", ""))
                ));
        return mapMessage;
    }

    @RequestMapping(value = "report/personalwordrecognitionandreading.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalWordRecognitionAndReading() {
        String hid = getRequestString("hid");
        String questionBoxId = getRequestString("questionBoxId");
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0) {
            return MapMessage.errorMessage("参数错误");
        }

        MapMessage mapMessage = newHomeworkReportServiceClient.personalWordRecognitionAndReading(hid, questionBoxId, sid);
        mapMessage.putAll(
                MapUtils.m(
                        "questionUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", "")),
                        "completedUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions/answer" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", ""))
                ));
        return mapMessage;
    }

    @RequestMapping(value = "report/personalocrmentalarithmetic.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalOcrMentalArithmetic() {
        String hid = getRequestString("hid");
        String ocrAnswerStr = getRequestString("ocrAnswers");//英文逗号分隔开，例如：5b5a89e077748738a058d14f-1532658953669，5b5a89e077748738a058d14f-1532658953660
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0 || StringUtils.isAnyBlank(ocrAnswerStr)) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.personalOcrMentalArithmetic(hid, ocrAnswerStr, sid);
        mapMessage.putAll(
                MapUtils.m(
                        "questionUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", "")),
                        "completedUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions/answer" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", ""))
                ));
        return mapMessage;
    }


    @RequestMapping(value = "report/personalreadingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadingDetail() {
        String homeworkId = getRequestString("homeworkId");
        String readingId = getRequestString("readingId");
        Long studentId = getRequestLong("studentId");
        if (StringUtils.isAnyBlank(homeworkId, readingId)
                || studentId <= 0) {
            return MapMessage.errorMessage("homeworkId or studentId or readingId is null");
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.READING.name()));
        MapMessage mapMessage = newHomeworkReportServiceClient.personalReadingDetail(homeworkId, studentId, readingId, currentUserId(), objectiveConfigType);
        mapMessage.putAll(
                MapUtils.m(
                        "questionUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "type", "")),
                        "completedUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions/answer" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "type", ""))
                ));
        return mapMessage;
    }

    /**
     * 趣味配音个人二级详情页面
     */
    @RequestMapping(value = "report/personaldubbingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalDubbingDetail() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请用老师帐号登录");
        }
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id为空");
        }
        try {
            return newHomeworkReportServiceClient.personalDubbingDetail(homeworkId, studentId, dubbingId, teacher.getId());
        } catch (Exception ex) {
            logger.error("Failed to load personalDubbingDetail homeworkId:{},studentId{},dubbingId{}", homeworkId, studentId, dubbingId, ex);
            return MapMessage.errorMessage("获取趣味配音个人二级详情异常");
        }
    }


    /**
     * 趣味配音( with score )个人二级详情页面
     */
    @RequestMapping(value = "report/personaldubbingwithscoredetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalDubbingWithScoreDetail() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请用老师帐号登录");
        }
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id为空");
        }
        try {
            return newHomeworkReportServiceClient.personalDubbingWithScoreDetail(homeworkId, studentId, dubbingId, teacher.getId());
        } catch (Exception ex) {
            logger.error("Failed to load personalDubbingWithScoreDetail homeworkId:{},studentId{},dubbingId{}", homeworkId, dubbingId, ex);
            return MapMessage.errorMessage("获取趣味配音个人二级详情异常");
        }
    }

    /**
     * 口语交际个人二级详情页面
     *
     * @return
     */
    @RequestMapping(value = "report/personaloralcommunicationdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalOralCommunicationScoreDetail() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请用老师帐号登录");
        }
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String stoneId = getRequestString("stoneId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(stoneId)) {
            return MapMessage.errorMessage("题包为空");
        }
        try {
            return newHomeworkReportServiceClient.personalOralCommunicationDetail(homeworkId, studentId, stoneId, teacher.getId());
        } catch (Exception ex) {
            logger.error("Failed to load personalOralCommunicationScoreDetail homeworkId:{},studentId{},stoneId{}", homeworkId, stoneId, ex);
            return MapMessage.errorMessage("获取口语交际个人二级详情异常");
        }

    }

    /**
     * base_app category type details
     * 基础类型的详情
     */
    @RequestMapping(value = "report/detailsbaseapp.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage detailsBaseApp() {
        String homeworkId = getRequestString("homeworkId");
        String categoryId = getRequestString("categoryId");
        String lessonId = getRequestString("lessonId");
        Long studentId = getRequestLong("studentId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.BASIC_APP.name()));
        Teacher teacher = currentTeacher();
        if (StringUtils.isAnyBlank(homeworkId, categoryId, lessonId)
                || Objects.isNull(teacher)
                || objectiveConfigType == null) {
            return MapMessage.errorMessage("fetch baseApp detail failed hid of {},categoryId of {},lessonId of {}", homeworkId, categoryId, lessonId);
        }
        return studentId == 0L ?
                newHomeworkReportServiceClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, objectiveConfigType) :
                newHomeworkReportServiceClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, studentId, objectiveConfigType);
    }

    @RequestMapping(value = "report/listofcorrectedstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage listOfCorrectedStudent() {
        return MapMessage.successMessage();
    }


    @RequestMapping(value = "report/test.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage test() {
        Teacher teacher = currentTeacher();
        ObjectiveConfigTypeParameter parameter = new ObjectiveConfigTypeParameter();
        parameter.setPictureBookId("PB_10300001587926-1");
        parameter.setCategoryId(10329);
        parameter.setLessonId("BKC_10300009625486");
        parameter.setQuestionBoxId("OCN_01821492399-5-476768");
        return newHomeworkReportServiceClient.fetchNewHomeworkCommonObjectiveConfigTypePart(teacher, "201801_5a5741f8e92b1b29f0cda7f7_1", ObjectiveConfigType.READ_RECITE_WITH_SCORE, parameter);

//        return newHomeworkReportServiceClient.fetchAppNewHomeworkTypeQuestion("201709_59ce16977774875dcf1b7ac9_1", teacher);
//        return newHomeworkReportServiceClient.fetchNewHomeworkCommonObjectiveConfigTypePart(teacher, "201708_599c1eb3ac74598265196317_1", ObjectiveConfigType.NEW_READ_RECITE, parameter);
//        return newHomeworkReportServiceClient.fetchAppNewHomeworkTypeQuestion("201708_599c1eb3ac74598265196317_1", teacher);
    }


    @RequestMapping(value = "report/singlequestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchNewHomeworkSingleQuestionPart() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登入");
        }
        String hid = this.getRequestString("hid");

        String typeStr = this.getRequestString("type");

        String qid = this.getRequestString("qid");
        if (StringUtils.isAnyBlank(typeStr, hid, qid)) {
            return MapMessage.errorMessage("参数缺失");
        }
        String stoneDataId = this.getRequestString("stoneDataId");
        return newHomeworkReportServiceClient.fetchNewHomeworkSingleQuestionPart(teacher, hid, ObjectiveConfigType.of(typeStr), qid, stoneDataId);
    }


    @RequestMapping(value = "report/studentdetailopentable.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchAppNewHomeworkStudentDetailOpenTable() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登入");
        }
        String hid = this.getRequestString("hid");
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("参数缺失");
        }
        return newHomeworkReportServiceClient.fetchAppNewHomeworkStudentDetailOpenTable(hid, teacher);
    }


    @RequestMapping(value = "report/fetchobjectiveconfigtypepart.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchObjectiveConfigTypePart() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登入");
        }
        String paramStr = this.getRequestString("param");
        String hid = this.getRequestString("hid");
        String typeStr = this.getRequestString("type");
        if (StringUtils.isAnyBlank(hid, paramStr, typeStr)) {
            return MapMessage.errorMessage("参数缺失");
        }
        ObjectiveConfigType type = ObjectiveConfigType.of(typeStr);

        ObjectiveConfigTypeParameter parameter = JsonUtils.fromJson(paramStr, ObjectiveConfigTypeParameter.class);
        if (parameter == null) {
            return MapMessage.errorMessage("参数缺失");
        }
        return newHomeworkReportServiceClient.fetchNewHomeworkCommonObjectiveConfigTypePart(teacher, hid, type, parameter);
    }

    /**
     * 班级报告批改详情页面
     */
    @RequestMapping(value = "report/clazzreportdetail.vpage", method = RequestMethod.GET)
    public String getClazzReportDetail(Model model, HttpServletRequest request) {

        String homeworkId = getRequestString("homeworkId");
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/login.vpage";
        }
        model.addAllAttributes(
                MapUtils.m(
                        "examUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/report/error/rate.vpage", MapUtils.m("homeworkId", homeworkId)),
                        "examPcUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/report/error/pc/rate.vpage", MapUtils.m("homeworkId", homeworkId)),
                        "homeworkId", homeworkId,
                        "imgDomain", getCdnBaseUrlStaticSharedWithSep(),
                        "readingFlashUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, "/resources/apps/flash/Reading.swf"),
                        "tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage"
                ));
        if (grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "PCHomework", "UseVenus")) {
            return "teacherv3/homeworkhistoryv5/clazzreportdetail";
        } else {
            return "teacherv3/homeworkhistoryv3/clazzreportdetail";
        }

    }

    /**
     * 班级基础练习应用某类别详情
     */
    @RequestMapping(value = "report/clazzbasicdetail.vpage", method = RequestMethod.GET)
    public String categoryDetail(Model model) {
        String homeworkId = getRequestString("homeworkId");
        model.addAllAttributes(
                MapUtils.m(
                        "homeworkId", homeworkId,
                        "detailUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/report/detailsbaseapp.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "lessonId", getRequestString("lessonId"),
                                        "categoryId", getRequestString("categoryId")
                                ))
                ));
        return "teacherv3/homeworkhistoryv3/clazzbasicdetail";
    }

    /**
     * 学生基础练习应用某类别详情
     */
    @RequestMapping(value = "report/stubasicdetail.vpage", method = RequestMethod.GET)
    public String studentCategoryDetail(Model model) {
        String homeworkId = getRequestString("hid");
        model.addAllAttributes(
                MapUtils.m(
                        "homeworkId", homeworkId,
                        "detailUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/report/detailsbaseapp.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "lessonId", getRequestString("lessonId"),
                                        "categoryId", getRequestString("categoryId"),
                                        "studentId", getRequestString("studentId"),
                                        "objectiveConfigType", getRequestString("objectiveConfigType"))
                        )
                ));
        return "teacherv3/homeworkhistoryv3/stubasicdetail";
    }

    /**
     * 学生报告详情页面
     */
    @RequestMapping(value = "report/studentreportdetail.vpage", method = RequestMethod.GET)
    public String getStudentReportDetail(Model model) {
        String homeworkId = getRequestString("homeworkId");
        String studentId = getRequestString("studentId");
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/login.vpage";
        }
        model.addAllAttributes(MapUtils.m(
                "examUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/report/error/rate.vpage",
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "studentId", studentId)),
                "homeworkId", homeworkId,
                "studentId", studentId,
                "imgDomain", getCdnBaseUrlStaticSharedWithSep()
        ));
        if (grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "PCHomework", "UseVenus")) {
            return "teacherv3/homeworkhistoryv5/studentreportdetail";
        } else {
            return "teacherv3/homeworkhistoryv3/studentreportdetail";
        }

    }

    /**
     * @return details information of exam or quiz type
     */
    @RequestMapping(value = "report/examandquiz/detailinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage detailInfo() {
        String questionId = this.getRequestString("questionId");
        String homeworkId = getRequestString("homeworkId");
        ObjectiveConfigType type = ObjectiveConfigType.of(getRequestString("objectiveConfigType"));
        Teacher teacher = currentTeacher();
        if (StringUtils.isAnyBlank(questionId, homeworkId)
                || Objects.isNull(teacher)) {
            logger.warn("fetch exam and quiz failed : hid {},qid {},objectiveConfigType {}", homeworkId, questionId, type);
            return MapMessage.errorMessage("参数错误");
        }
        return newHomeworkReportServiceClient.examAndQuizDetailInfo(questionId, homeworkId, type);
    }

    /**
     * 作业报告班级报告同步详情
     * xuesong.zhang
     */
    @RequestMapping(value = "report/error/pc/rate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage pcReportErrorRate() {
        String homeworkId = getRequestString("homeworkId");
        Long studentId = getRequestLong("studentId");
        MapMessage mapMessage;
        if (studentId == 0L) {
            mapMessage = newHomeworkReportServiceClient.loadNewHomeworkReportExamErrorRates(homeworkId, currentUserId(), true);
        } else {
            mapMessage = newHomeworkReportServiceClient.loadNewHomeworkReportExamErrorRates(homeworkId, studentId, currentUserId());
            mapMessage.putAll(
                    MapUtils.m(
                            "questionUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "homeworkId", homeworkId,
                                            "type", "")),
                            "completedUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions/answer" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "homeworkId", homeworkId,
                                            "type", ""))
                    ));
        }
        if (mapMessage.isSuccess()) {
            mapMessage.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        }
        return mapMessage;
    }


    /**
     * 作业报告班级报告同步详情
     * xuesong.zhang
     */
    @RequestMapping(value = "report/error/rate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage reportErrorRate() {
        String homeworkId = getRequestString("homeworkId");
        Long studentId = getRequestLong("studentId");
        MapMessage mapMessage;
        if (studentId == 0L) {
            mapMessage = newHomeworkReportServiceClient.loadNewHomeworkReportExamErrorRates(homeworkId, currentUserId(), false);
        } else {
            mapMessage = newHomeworkReportServiceClient.loadNewHomeworkReportExamErrorRates(homeworkId, studentId, currentUserId());
            mapMessage.putAll(
                    MapUtils.m(
                            "questionUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "homeworkId", homeworkId,
                                            "type", "")),
                            "completedUrl", UrlUtils.buildUrlQuery("/teacher/new/homework/questions/answer" + Constants.AntiHijackExt,
                                    MapUtils.m(
                                            "homeworkId", homeworkId,
                                            "type", ""))
                    )
            );
        }
        if (mapMessage.isSuccess()) {
            mapMessage.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        }
        return mapMessage;
    }

    /**
     * 字词讲练
     * 模块班级详情
     *
     * @return
     */
    @RequestMapping(value = "report/clazzWordTeachModuleDetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzWordTeachModuleDetail() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请用老师帐号登录");
        }
        String homeworkId = getRequestString("homeworkId");
        String stoneId = getRequestString("stoneId");
        String wordTeachModuleTypeStr = getRequestString("wordTeachModuleType");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (StringUtils.isBlank(stoneId)) {
            return MapMessage.errorMessage("字词讲练题包id为空");
        }
        if (StringUtils.isBlank(wordTeachModuleTypeStr)) {
            return MapMessage.errorMessage("字词讲练模块类型为空");
        }
        WordTeachModuleType wordTeachModuleType = WordTeachModuleType.of(wordTeachModuleTypeStr);
        try {
            return newHomeworkReportServiceClient.clazzWordTeachModuleDetail(teacher.getId(), homeworkId, stoneId, wordTeachModuleType);
        } catch (Exception ex) {
            logger.error("Failed to load clazzWordTeachModuleDetail homeworkId:{},stoneId{},wordTeachModuleType{}", homeworkId, stoneId, wordTeachModuleType, ex);
            return MapMessage.errorMessage("获取字词讲练班级二级详情异常");
        }
    }

    /**
     * 已做和未做作业的学生信息
     */
    @RequestMapping(value = "homeworkfinishinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkFinishInfo() {
        String homeworkId = getRequestString("homeworkId");
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId()).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<String, List<Map<String, Object>>> result = newHomeworkResultLoaderClient.homeworkCommentAndIntegralInfo(userMap, newHomework);
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(result.get("doUser"));
        all.addAll(result.get("undoUser"));
        Map<String, List<Map<String, Object>>> obj = new HashMap<>();
        obj.put("all", all);
        obj.put("list100", result.get("list100"));
        obj.put("list90to99", result.get("list90to99"));
        obj.put("list80to89", result.get("list80to89"));
        obj.put("list60to79", result.get("list60to79"));
        obj.put("list60", result.get("list60"));
        obj.put("finisheds", result.get("finisheds"));
        obj.put("unfinisheds", result.get("unfinisheds"));
        List<Map<String, Object>> title = new ArrayList<>();
        int subjectiveCount = 0;
        for (NewHomeworkPracticeContent practice : newHomework.getPractices()) {
            if (practice.getType().isSubjective()) {
                subjectiveCount++;
            }
        }
        if (subjectiveCount == newHomework.getPractices().size()) {
            title.add(MapUtils.m("name", "全部", "key", "all"));
            title.add(MapUtils.m("name", "已完成", "key", "finisheds"));
            title.add(MapUtils.m("name", "未完成", "key", "unfinisheds"));
        } else {
            title.add(MapUtils.m("name", "全部", "key", "all"));
            title.add(MapUtils.m("name", "100分", "key", "list100"));
            title.add(MapUtils.m("name", "99~90分", "key", "list90to99"));
            title.add(MapUtils.m("name", "89~80分", "key", "list80to89"));
            title.add(MapUtils.m("name", "79~60分", "key", "list60to79"));
            title.add(MapUtils.m("name", "小于60分", "key", "list60"));
            title.add(MapUtils.m("name", "已完成", "key", "finisheds"));
            title.add(MapUtils.m("name", "未完成", "key", "unfinisheds"));
        }

        return MapMessage.successMessage().add("result", obj).add("title", title);
    }

    /**
     * 评语
     * xuesong.zhang
     */
    @RequestMapping(value = "report/writehomeworkcomment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage writeHomeworkComment() {
        String comment = StringUtils.cleanXSS(getRequestString("comment"));
        String homeworkId = getRequestString("homeworkId");
        List<String> strUserIds = Arrays.asList(StringUtils.split(getRequestString("userIds"), ","));
        Set<Long> userIds = strUserIds
                .stream()
                .map(SafeConverter::toLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (StringUtils.isBlank(homeworkId)
                || CollectionUtils.isEmpty(userIds)
                || (StringUtils.isBlank(comment))) {
            return MapMessage.errorMessage("评语失败 commend of {} ,hid of {},useIds of {}", comment, homeworkId, strUserIds);
        }

        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登入老师账号");
        }
        return newHomeworkServiceClient.batchSaveNewHomeworkComment(teacher, homeworkId, userIds, comment, null);
    }

    /**
     * 批改作业-去批改
     * 这里只显示某次作业中尚未批改部分的题目及主观作答信息
     */
    @RequestMapping(value = "report/needcorrect.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage needCorrect() {
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业ID null");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.loadNewHomeworkNeedCorrect(homeworkId, currentUserId());
        mapMessage.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return mapMessage;
    }


    /**
     * 作业报告--催促或表扬学生，发送消息
     */
    @RequestMapping(value = "report/sendmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sendMessage() {
        return MapMessage.successMessage();
    }

    /**
     * 某学生某作业某绘本详情数据
     */
    @RequestMapping(value = "report/readingdetail.vpage", method = RequestMethod.GET)
    public String readingDetail(Model model) {
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "/teacherv3/homeworkhistoryv3/readingdetail";
    }


    /**
     * 作业报告/语音列表
     */
    @RequestMapping(value = "report/voicelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage voiceList() {
        String homeworkId = getRequestString("homeworkId");
        return newHomeworkReportServiceClient.loadEnglishHomeworkVoiceList(homeworkId);
    }

    /**
     * 作业报告/推荐语音
     */
    @RequestMapping(value = "report/voicerecommend.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitVoiceRecommend() {
        String homeworkId = getRequestString("homeworkId");
        String recommendVoiceListStr = getRequestString("recommendVoiceList");
        List<VoiceRecommend.RecommendVoice> recommendVoiceList = JsonUtils.fromJsonToList(recommendVoiceListStr, VoiceRecommend.RecommendVoice.class);
        String recommendComment = getRequestString("recommendComment");
        return newHomeworkServiceClient.submitVoiceRecommend(homeworkId, recommendVoiceList, recommendComment);
    }

    @RequestMapping(value = "report/fetchreadrecitedetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchReadReciteQuestionBoxIdDetail() {
        String hid = this.getRequestString("hid");
        String questionBoxId = this.getRequestString("questionBoxId");
        String typeStr = this.getRequestString("type");
        if (StringUtils.isAnyBlank(hid, questionBoxId, typeStr)) {
            return MapMessage.errorMessage("参数错误");
        }
        return newHomeworkReportServiceClient.fetchReadReciteQuestionBoxIdDetail(hid, questionBoxId, ObjectiveConfigType.of(typeStr));
    }

    /**
     * 优秀录音推荐，首次分享到微信/QQ 增加2个园丁豆
     */
    @RequestMapping(value = "share/addintegral.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addIntegral() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (UserType.TEACHER != currentUser().fetchUserType()) {
            return MapMessage.errorMessage("请用老师账号登录");
        }

        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("homeworkId is blank!");
        }
        try {
            return newHomeworkServiceClient.addIntegral(user.getId(), homeworkId);
        } catch (Exception ex) {
            return MapMessage.errorMessage("Failed to share voice recommend add integral  hid of {},useId of {}", homeworkId, user.getId(), ex);
        }
    }

    @RequestMapping(value = "sharepartreport.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage sharePartReport() {
        String hid = this.getRequestString("hid");
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("参数错误");
        }
        Teacher user = currentTeacher();
        if (user == null) {
            return MapMessage.errorMessage("请登入");
        }
        String recommendVoiceListStr = getRequestString("recommendVoiceList");
        String shareList = getRequestString("shareList");
        String excellentDubbingStuListStr = getRequestString("excellentDubbingStuList");
        String imageTextListStr = getRequestString("imageTextList");
        List<VoiceRecommend.RecommendVoice> recommendVoiceList = JsonUtils.fromJsonToList(recommendVoiceListStr, VoiceRecommend.RecommendVoice.class);
        List<VoiceRecommend.ReadReciteVoice> readReciteVoiceList = JsonUtils.fromJsonToList(recommendVoiceListStr, VoiceRecommend.ReadReciteVoice.class);
        List<BaseVoiceRecommend.DubbingWithScore> excellentDubbingStu = JsonUtils.fromJsonToList(excellentDubbingStuListStr, BaseVoiceRecommend.DubbingWithScore.class);
        List<BaseVoiceRecommend.ImageText> imageTextList = JsonUtils.fromJsonToList(imageTextListStr, BaseVoiceRecommend.ImageText.class);
        try {
            return newHomeworkReportServiceClient.shareReport(user, hid, recommendVoiceList, readReciteVoiceList, shareList, excellentDubbingStu, imageTextList);
        } catch (CannotAcquireLockException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("share part report failed : hid of {}", hid, ex);
            return MapMessage.errorMessage();
        }

    }


    /**
     * NEW HOMEWORK 作业历史班级列表
     */
    @RequestMapping(value = "report/earlylist.vpage", method = RequestMethod.GET)
    public String earlyHistoryIndex(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<Map<String, Object>> clazzMaps = getClazzList();
        Set<Long> clazzIds = clazzMaps
                .stream()
                .map(clazz -> SafeConverter.toLong(clazz.get("id")))
                .collect(Collectors.toSet());
        Map<Long, List<GroupMapper>> groupMaps = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds);
        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getTeacherGroupIds(teacher.getId());
        LinkedHashMap<Integer, List<Map<String, Object>>> clazzLevelMap = new LinkedHashMap<>();
        for (Map<String, Object> clazzMap : clazzMaps) {
            Long clazzId = SafeConverter.toLong(clazzMap.get("id"));
            List<GroupMapper> groupMappers = groupMaps.get(clazzId);
            if (CollectionUtils.isNotEmpty(groupMappers)) {
                for (GroupMapper groupMapper : groupMappers) {
                    if (teacherGroupIds.contains(groupMapper.getId())) {
                        clazzMap.put("clazzGroupId", groupMapper.getId());
                        break;
                    }
                }
                Integer clazzLevel = SafeConverter.toInt(clazzMap.get("classLevel"));
                clazzLevelMap.computeIfAbsent(clazzLevel, k -> new ArrayList<>()).add(clazzMap);
            }
        }
        model.addAllAttributes(
                MapUtils.m(
                        "clazzLevelMap", JsonUtils.toJson(clazzLevelMap.values()),
                        "currentDayEndDate", DateUtils.dateToString(DayRange.current().getEndDate()),
                        "curSubject", teacher.getSubject()
                ));

        Date endDate = NewHomeworkConstants.STUDENT_ALLOW_SEARCH_HOMEWORK_START_TIME;
        Date startDate = DateUtils.calculateDateDay(endDate, -30);
        model.addAttribute("startDate", DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE));
        return "teacherv3/homeworkhistoryv3/earlylist";
    }

    @RequestMapping(value = "report/type/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportForObjectiveConfigTypeResult() {
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigType = getRequestString("objectiveConfigType");
        Long studentId = getRequestLong("studentId");
        if (StringUtils.isEmpty(homeworkId) || StringUtils.isEmpty(objectiveConfigType)) {
            return MapMessage.errorMessage("参数错误");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生id错误");
        }
        return newHomeworkReportServiceClient.homeworkForObjectiveConfigTypeResult(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail);
    }

    /**
     * 推荐学生巩固
     * 入参：homeworkId
     *
     * @return
     */
    @RequestMapping(value = "save/correction/remind.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage saveCorrectionRemind() {
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isEmpty(homeworkId)) {
            return MapMessage.errorMessage("参数缺失");
        }
        return newHomeworkServiceClient.updateHomeworkRemindCorrection(homeworkId);
    }


    /**
     * 口语交际单个情景包详情
     */
    @RequestMapping(value = "report/singleoralcommunicationpackagedetail.vpage", method = RequestMethod.GET)
    public String getSingleOralCommunicationPackageDetail(Model model) {
        model.addAttribute("homeworkId", getRequestString("hid"));
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/login.vpage";
        }
        return "teacherv3/homeworkhistoryv5/singleoralcommunicationpackagedetail";
    }

    /**
     * 小语字词训练各个模块详情页面
     */
    @RequestMapping(value = "report/clazzwordteachmoduledetail.vpage", method = RequestMethod.GET)
    public String getClazzWordTeachModuleDetail(Model model) {
        model.addAttribute("homeworkId", getRequestString("homeworkId"));
        model.addAttribute("stoneId", getRequestString("stoneId"));
        model.addAttribute("wordTeachModuleType", getRequestString("wordTeachModuleType"));
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/login.vpage";
        }
        return "teacherv3/homeworkhistoryv5/clazzwordteachmoduledetail";
    }
}
