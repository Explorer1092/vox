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

package com.voxlearning.wechat.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookClazzLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkReportServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.mapper.PictureBookQuery;
import com.voxlearning.utopia.service.question.api.mapper.QuestionMapper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.wechat.context.CorrectHomeworkContext;
import com.voxlearning.wechat.context.CorrectQuestionContext;
import com.voxlearning.wechat.controller.AbstractTeacherWebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 18/1/2016
 */
@Controller
@RequestMapping(value = "/teacher/homework")
public class TeacherHomeworkController extends AbstractTeacherWebController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private NewHomeworkReportServiceClient newHomeworkReportServiceClient;

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public String index(Model model) {
        Set<Subject> canBeAssignedSubjects = Collections.emptySet();
        Long teacherId = getTeacherIdBySubject();
        if (teacherId != null) {
            try {
                MapMessage message = newHomeworkContentServiceClient.loadTeachersClazzList(teacherLoaderClient.loadRelTeacherIds(teacherId), Collections.singleton(NewHomeworkType.Normal), true);
                canBeAssignedSubjects = (Set<Subject>) message.get("canBeAssignedSubjects");
            } catch (Exception ex) {
                logger.error("Get teacher canBeAssignedSubjects error, tid : {}", ex);
            }
        }

        model.addAttribute("canBeAssignedSubjects", JsonUtils.toJson(canBeAssignedSubjects
                .stream()
                .map(subject -> MiscUtils.m("name", subject, "value", subject.getValue()))
                .collect(Collectors.toList())));
        return "teacher/homework/index";
    }

    /**
     * 作业包
     */
    @RequestMapping(value = "/package.vpage", method = RequestMethod.GET)
    public String MathPackage(Model model) {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String sections = getRequestString("sections");
        String clazzIds = getRequestString("clazzIds");
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("sections", sections);
        model.addAttribute("clazzIds", clazzIds);
        return "teacher/homework/package";
    }

    @RequestMapping(value = "/report/history.vpage", method = RequestMethod.GET)
    public String history() {
        return "teacher/homework/report/history";
    }

    @RequestMapping(value = "/report/getClazzs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAllClazzs() {
        List<Clazz> clazzs = getTeacherClazzs();
        return MapMessage.successMessage().add("clazzs", clazzs);
    }

    //布置作业--查询作业类型
    @RequestMapping(value = "/packagetype.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getPackageType(@RequestParam String bookId, @RequestParam String unitId, @RequestParam String sections) {
        List<String> sectionIds = StringUtils.toList(sections, String.class);

        if (CollectionUtils.isEmpty(sectionIds)) {
            return MapMessage.errorMessage("参数错误");
        }

        Long teacherId = getTeacherIdBySubject();
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

            MapMessage message = newHomeworkContentServiceClient.getHomeworkType(teacher, sectionIds, unitId, bookId, "wechat", null, null);
            if (message.isSuccess()) {
                return message;
            }
        } catch (Exception ex) {
            logger.error("Get math homework package type error,bookId:{},unitId:{},sectionIds:{}", bookId, unitId, sections, ex);
        }
        return MapMessage.errorMessage("查询作业包类型失败");
    }

    //布置作业--查询作业包内的题目
    @RequestMapping(value = "/content.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage getHomeworkContent(@RequestParam String bookId, @RequestParam String unitId, @RequestParam String sections, @RequestParam Integer type) {
        List<String> sectionIds = StringUtils.toList(sections, String.class);
        ObjectiveConfigType packageType = ObjectiveConfigType.of(type);

        if (CollectionUtils.isEmpty(sectionIds) || Objects.isNull(packageType)) {
            return MapMessage.errorMessage("参数错误");
        }

        Long teacherId = getTeacherIdBySubject();

        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

            MapMessage contentMsg = newHomeworkContentServiceClient.getHomeworkContent(teacher, Collections.emptySet(), sectionIds, unitId,
                    bookId, packageType, null);
            if (contentMsg.isSuccess()) {
                // 对口算的特殊处理，微信端需要把新的数据结构转为老的
                if (packageType == ObjectiveConfigType.MENTAL) {
                    List<Map<String, Object>> mentalContent = (List<Map<String, Object>>) contentMsg.get("content");
                    if (CollectionUtils.isNotEmpty(mentalContent)) {
                        List<Map<String, Object>> kpList = (List<Map<String, Object>>) mentalContent.get(0).get("kpList");
                        if (CollectionUtils.isNotEmpty(kpList)) {
                            kpList = kpList.stream()
                                    .filter(map -> "normal".equals(map.get("kpType")))
                                    .map(map -> {
                                        Map<String, Object> kpInfo = new LinkedHashMap<>();
                                        kpInfo.put("book", map.get("book"));
                                        kpInfo.put("content_type_id", map.getOrDefault("contentTypeId", 0));
                                        kpInfo.put("kp_id", map.get("kpId"));
                                        kpInfo.put("kp_name", map.get("kpName"));
                                        kpInfo.put("question_count", map.get("questionCount"));
                                        kpInfo.put("teacherAssignTimes", map.get("teacherAssignTimes"));
                                        return kpInfo;
                                    })
                                    .collect(Collectors.toList());
                            contentMsg.put("content", kpList);
                        }
                    }
                }
                return contentMsg;
            }

        } catch (Exception ex) {
            logger.error("Get math package content error", ex);
        }
        return MapMessage.errorMessage("查询作业包内容失败");
    }

    //布置作业--取口算题
    @RequestMapping(value = "/mental/questions.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getMentalQuestions(@RequestParam String knowledgePoint, @RequestParam Integer contentTypeId, @RequestParam(required = false) String questionIds, @RequestParam Integer questionCount) {
        if (StringUtils.isBlank(knowledgePoint) || null == questionCount || null == contentTypeId) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            List<String> qids = new ArrayList<>();
            if (StringUtils.isNotBlank(questionIds)) {
                qids = StringUtils.toList(questionIds, String.class);
            }

            MapMessage message = newHomeworkContentServiceClient.getMentalQuestion(knowledgePoint, contentTypeId, qids, questionCount);
            if (message.isSuccess()) {
                return message;
            }
        } catch (Exception ex) {
            logger.error("Get mental questions error,knowledgePoint:{},count:{}", knowledgePoint, questionCount, ex);
        }
        return MapMessage.errorMessage("查询口算题失败");
    }

    // 获取作业应试试题信息
    @RequestMapping(value = "/query/questions.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage queryQuestions() {
        String data = getRequestString("data");
        if (StringUtils.isBlank(data)) return MapMessage.errorMessage("参数错误");

        Map<String, List> map = JsonUtils.fromJsonToMap(data, String.class, List.class);
        if (MapUtils.isEmpty(map)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            @SuppressWarnings("unchecked")
            List<String> ids = map.get("ids");
            // 处理给前端的数据不做任何过滤处理
            // 在做数据提交的时候做一下处理
            Map<String, QuestionMapper> resultMap = questionLoaderClient.loadQuestionMapperByQids(ids, false, false, true);
            /*Map<String, QuestionMapper> tempMap = questionLoaderClient.loadQuestionMapperByQids(ids, true, true, true);
            if (MapUtils.isEmpty(tempMap)) {
                logger.error("QuestionBankException Error Question Id:{}", ids);
            }*/
            return MapMessage.successMessage().add("result", resultMap);
        } catch (Exception ex) {
            logger.error("Query questions error,ids:{}", data, ex);
        }
        return MapMessage.errorMessage("查询题目失败");
    }

    // 根据班级Ids获取可用学豆最大值
    @RequestMapping(value = "maxic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage calculateMaxIntegralCount() {
        String clazzIds = getRequestString("clazzIds");
        int dc = 0; // 默认值
        int mc = 0; // 最大值
        Object overTimeGids = null;

        Long teacherId = getTeacherIdBySubject();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacherDetail) {
            return MapMessage.errorMessage("未查询到老师信息");
        }

        if (StringUtils.isNotBlank(clazzIds)) {
            Set<Long> cids = Arrays.stream(StringUtils.split(clazzIds, ","))
                    .map(e -> ConversionUtils.toLong(StringUtils.split(e, "_")[0])).collect(Collectors.toSet());
            Map<String, Object> map = new HashMap<>();
            try {
                map = businessTeacherServiceClient.calculateHomeworkMaxIntegralCount(teacherDetail, cids);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            dc = SafeConverter.toInt(map.get("dc"));
            mc = SafeConverter.toInt(map.get("mc"));
            overTimeGids = map.get("overTimeGids");
        }

        return MapMessage.successMessage().add("dc", dc).add("mc", mc).add("overTimeGids", overTimeGids);
    }

    //布置作业-提交布置作业
    @RequestMapping(value = "/assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage assignHomework(@RequestBody Map<String, Object> map) {
        map.put("User-Agent", getRequest().getHeader("User-Agent"));
        map.put("ip", getRequestContext().getRealRemoteAddress());
        Teacher teacher = getTeacherBySubject(Subject.of(SafeConverter.toString(map.get("subject"))));
        if (null == teacher) {
            return MapMessage.errorMessage("未查询到老师信息");
        }
        try {
            Date date = DateUtils.stringToDate(SafeConverter.toString(map.get("endTime")));

            if (date == null) {
                return MapMessage.errorMessage("作业截止时间错误");
            }

            if (date.toInstant().isBefore(Instant.now())) return MapMessage.errorMessage("作业截止时间必须晚于当前时间！");

            HomeworkSource source = HomeworkSource.newInstance(map);
            NewHomeworkType newHomeworkType = NewHomeworkType.of(SafeConverter.toString(source.get("homeworkType")));
            if (NewHomeworkType.Unknown.equals(newHomeworkType)) {
                return MapMessage.errorMessage().setInfo("没有homeworkType参数。");
            }
            HomeworkTag homeworkTag = HomeworkTag.of(SafeConverter.toString(source.get("homeworkTag")));
            return newHomeworkServiceClient.assignHomework(teacher, source, HomeworkSourceType.Wechat, newHomeworkType, homeworkTag);
        } catch (Exception ex) {
            logger.error("Assign homework error,param:{}", JsonUtils.toJson(map), ex);
        }
        return MapMessage.errorMessage("布置作业失败");
    }

    //作业报告-查询作业历史
    @RequestMapping(value = "/history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage homeworkHistory(@RequestParam String clazzIds, @RequestParam Integer page, @RequestParam Integer size) {
        if (StringUtils.isBlank(clazzIds)) return MapMessage.errorMessage("请选择班级");
        Set<Long> clzIds = new HashSet<>(StringUtils.toLongList(clazzIds));
        if (CollectionUtils.isEmpty(clzIds)) return MapMessage.errorMessage("请选择班级");

        Teacher teacher = getTeacherBySubject();
        if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

        Long teacherId = teacher.getId();
        try {
            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
            if (CollectionUtils.isEmpty(clazzs)) return MapMessage.errorMessage("您还未创建班级");

            if (!clazzs.stream().map(Clazz::getId).collect(Collectors.toList()).containsAll(clzIds)) {
                return MapMessage.errorMessage("您没有权限");
            }

            Set<Long> groupIds = new HashSet<>();
            //查出groups
            List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherId(teacherId)
                    .stream()
                    .map(GroupTeacherTuple::getGroupId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, List<GroupMapper>> groups = groupLoaderClient.loadClazzGroups(clzIds);
            if (MapUtils.isNotEmpty(groups)) {
                groups.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v)) {
                        v.forEach(m -> {
                            if (teacherGroupIds.contains(m.getId())) {
                                groupIds.add(m.getId());
                            }
                        });
                    }
                });
            }

            //查作业
            if (CollectionUtils.isNotEmpty(groupIds)) {
                if (null == page || page <= 0 || page >= Integer.MAX_VALUE) page = 1;
                if (null == size || size <= 0 || size >= 50) size = 10;

                Pageable pageable = new PageRequest(page - 1, size);
                Page<Map<String, Object>> historyPage = newHomeworkReportServiceClient.pageHomeworkReportListByGroupIds(groupIds, pageable, teacher.getSubject());
                return MapMessage.successMessage().add("page", historyPage);
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Get homework history error,clzids:{},page:{},size:{},tid:{}", clazzIds, page, size, teacherId, ex);
        }
        return MapMessage.errorMessage("查询作业历史失败");
    }

    @RequestMapping(value = "/report/detail.vpage", method = RequestMethod.GET)
    public String ReportDetail(Model model) {
        String homeworkId = getRequestString("homeworkId");
        Long teacherId = getRequestContext().getUserId();

        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null != teacher) {
                MapMessage mapMessage = newHomeworkReportServiceClient.reportDetailIndex(teacher, homeworkId);
                if (mapMessage.isSuccess()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> homeworkInfo = (Map<String, Object>) mapMessage.get("homeworkInfo");
                    model.addAttribute("homeworkId", homeworkId);
                    model.addAttribute("includeSubjective", homeworkInfo.get("includeSubjective"));
                    model.addAttribute("corrected", homeworkInfo.get("corrected"));
                    model.addAttribute("createAt", homeworkInfo.get("createAt"));
                    model.addAttribute("clazzId", homeworkInfo.get("clazzId"));
                    model.addAttribute("clazzName", homeworkInfo.get("clazzName"));
                    model.addAttribute("finishedCount", homeworkInfo.get("finishedCount"));
                }
            }
        } catch (Exception ex) {
            logger.error("Get homework report info error,hid:{},tid:{}", homeworkId, teacherId, ex);
        }

        return "/teacher/homework/report/detail";
    }

    //作业报告-查看学生作业完成情况
    @RequestMapping(value = "/report/completion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage homeworkReportCompletion(@RequestParam String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("请选择要查看的作业");
        }
        Long teacherId = getRequestContext().getUserId();

        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");
            return newHomeworkReportServiceClient.homeworkReportForStudent(teacher, homeworkId, false);
        } catch (Exception ex) {
            logger.error("Get homework complete status error,tid:{},hid:{}", teacherId, homeworkId, ex);
        }
        return MapMessage.errorMessage("查询学生完成情况失败");
    }

    /**
     * 基础练习单个类型详情
     */
    @RequestMapping(value = "report/basicappreportdetail.vpage", method = RequestMethod.GET)
    public String detailsBaseAppForHomeworkMenu() {

        return "/teacher/homework/report/basicappreportdetail";
    }


    @RequestMapping(value = "report/personalreadingdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage personalReadingDetail() {
        String homeworkId = getRequestString("homeworkId");
        String readingId = getRequestString("readingId");
        Long studentId = getRequestLong("studentId");
        Long teacherId = getTeacherIdBySubject();
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.READING.name()));
        if (StringUtils.isBlank(homeworkId) || studentId <= 0) {
            return MapMessage.errorMessage("homeworkId or studentId is null");
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
            return MapMessage.errorMessage("老师权限错误");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.personalReadingDetail(homeworkId, studentId, readingId, getRequestContext().getUserId(), objectiveConfigType);
        mapMessage.add("questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt,
                MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", "")));

        mapMessage.add("completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt,
                MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", "")));
        return mapMessage;
    }

    /**
     * @return base_app category type details
     */
    @RequestMapping(value = "report/detailsbaseapp.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage detailsBaseApp() {
        String homeworkId = getRequestString("homeworkId");
        String categoryId = getRequestString("categoryId");
        String lessonId = getRequestString("lessonId");
        Long studentId = getRequestLong("studentId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.BASIC_APP.name()));
        Long teacherId = getRequestContext().getUserId();
        if (StringUtils.isBlank(homeworkId) || StringUtils.isBlank(categoryId) || StringUtils.isBlank(lessonId) || Objects.isNull(teacherId) || objectiveConfigType == null) {
            return MapMessage.errorMessage();
        }
        MapMessage mapMessage;
        if (studentId == 0L) {
            mapMessage = newHomeworkReportServiceClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, objectiveConfigType);
        } else {
            mapMessage = newHomeworkReportServiceClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, studentId, objectiveConfigType);
        }
        return mapMessage;
    }


    @RequestMapping(value = "report/listofcorrectedstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage listOfCorrectedStudent() {
        return MapMessage.successMessage();
    }


    /**
     * 作业报告--催促或表扬学生，发送消息
     */
    @RequestMapping(value = "report/sendmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sendmessage() {
        return MapMessage.successMessage();
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
        Long teacherId = getRequestContext().getUserId();
        if (StringUtils.isBlank(questionId) || StringUtils.isBlank(homeworkId) || Objects.isNull(teacherId)) {
            return MapMessage.errorMessage();
        }
        return newHomeworkReportServiceClient.examAndQuizDetailInfo(questionId, homeworkId, type);
    }


    //作业报告-查看作业数据汇总
    @RequestMapping(value = "/report/summary.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage homeworkReportSummary(@RequestParam String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("请选择要查看的作业");
        }
        Long teacherId = getRequestContext().getUserId();

        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

//            return newHomeworkReportLoadClient.homeworkReportForType(teacher, homeworkId);
        } catch (Exception ex) {
            logger.error("Get homework report summary error,tid:{},hid:{}", teacherId, homeworkId);
        }
        return MapMessage.errorMessage("查询作业汇总数据失败");
    }

    @RequestMapping(value = "/report/clazzreportdetail.vpage", method = RequestMethod.GET)
    public String clazzReportDetail() {
        return "/teacher/homework/report/clazzreportdetail";
    }

    //一键批改
    @RequestMapping(value = "/report/quickremarks.vpage", method = RequestMethod.GET)
    public String quickRemarks() {
        return "/teacher/homework/report/quickremarks";
    }

    //一键奖励
    @RequestMapping(value = "/report/quickaward.vpage", method = RequestMethod.GET)
    public String quickAward() {
        return "/teacher/homework/report/quickaward";
    }

    //作业报告-全班的答题详情
    @RequestMapping(value = "/report/answer/detail/clazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage homeworkReportAnswerDetailOfClazz(@RequestParam String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) return MapMessage.errorMessage("请选择作业");

        try {
            MapMessage message = newHomeworkReportServiceClient.loadNewHomeworkReportExamErrorRates(homeworkId, getRequestContext().getUserId(), false);
            if (message.isSuccess()) return message;

            return MapMessage.errorMessage("未查询到作业详情");
        } catch (Exception ex) {
            logger.error("Get homework answer detail error,hid:{}", homeworkId, ex);
        }
        return MapMessage.errorMessage("查询作业答题详情失败");
    }

    //作业报告-写评语(支持批量或单个)
    @RequestMapping(value = "/report/comment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage homeworkComment(@RequestParam String homeworkId, @RequestParam String userIds, @RequestParam String comment) {
        if (StringUtils.isBlank(homeworkId) || StringUtils.isBlank(userIds) || (StringUtils.isBlank(comment)))
            return MapMessage.errorMessage("参数错误");

        Set<Long> uids = new HashSet<>(StringUtils.toLongList(userIds));
        if (CollectionUtils.isEmpty(uids)) return MapMessage.errorMessage("请选择学生");

        Long teacherId = getRequestContext().getUserId();

        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

            //保存评语
            return newHomeworkServiceClient.batchSaveNewHomeworkComment(teacher, homeworkId, uids, comment, null);
        } catch (Exception ex) {
            logger.error("Batch comment error,hid:{},tid:{},uids:{},comment:{}", homeworkId, teacherId, userIds, comment, ex);
        }
        return MapMessage.errorMessage("保存评语失败");
    }

    //作业报告-批量发学豆
    @RequestMapping(value = "/report/batchsendintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchSendIntegral(@RequestBody Map<String, Object> jsonMap) {
        String homeworkId = SafeConverter.toString(jsonMap.get("homeworkId"));
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);

        Long teacherId = getRequestContext().getUserId();
        if (newHomework != null) {
            teacherId = getTeacherIdBySubject(newHomework.getSubject());
        }

        try {
            return AtomicLockManager.instance().wrapAtomic(newHomeworkServiceClient)
                    .keys(teacherId)
                    .proxy()
                    .batchRewardStudentIntegral(teacherId, jsonMap);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中,请不要重复点击");
        } catch (Exception ex) {
            logger.error("Batch send integral error,tid:{},jsonMap:{}", teacherId, jsonMap, ex);
            return MapMessage.errorMessage("操作失败，请重试");
        }
    }

    //作业报告-批量批改作业
    @RequestMapping(value = "/report/batchcorrect.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchCorrect(@RequestParam String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) return MapMessage.errorMessage("请选择要批改的作业");

        Long teacherId = getRequestContext().getUserId();

        try {
            NewHomework homework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (null == homework) return MapMessage.errorMessage("未查询到该作业");
            // 权限检查
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, homework.getClazzGroupId())) {
                return MapMessage.errorMessage("您没有权限批改");
            }
            newHomeworkServiceClient.batchSaveHomeworkCorrect(homework.getId(), teacherId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Batch correct homework error, hid:{}, tid:{}", homeworkId, teacherId, ex);
        }
        return MapMessage.errorMessage("批改作业失败");
    }

    //作业报告-批改作业
    @RequestMapping(value = "/report/correct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage correct(@RequestParam String data) {
        if (StringUtils.isBlank(data)) return MapMessage.errorMessage("参数错误");

        CorrectHomeworkContext context = JsonUtils.fromJson(data, CorrectHomeworkContext.class);
        if (null == context) return MapMessage.errorMessage("参数错误");

        try {
            List<CorrectQuestionContext> questionContexts = context.getCorrections();
            List<CorrectQuestionContext> successResults = new LinkedList<>();
            int failureCount = 0;

            for (CorrectQuestionContext cxt : questionContexts) {
                NewHomework newhomework = newHomeworkLoaderClient.loadNewHomework(context.getHomeworkId());
                Boolean b = saveCorrectQuestionResult(cxt.getUserId(), newhomework, context.getType(), context.getQuestionId(), cxt);
                if (b) {
                    successResults.add(cxt);
                } else {
                    failureCount++;
                }
            }

            if (failureCount > 0) {
                return MapMessage.errorMessage("有" + failureCount + "道题未批改或批改失败").add("questioninfos", successResults);
            }

            return MapMessage.successMessage("批改成功").add("questioninfos", successResults);
        } catch (Exception ex) {
            logger.error("Correct error,data:{}", data, ex);
        }
        return MapMessage.errorMessage("批改失败");
    }

    //作业报告-一键检查作业
    @RequestMapping(value = "/report/check.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkHomework(@RequestParam String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) return MapMessage.errorMessage("请选择要检查的作业");
        Long teacherId = getRequestContext().getUserId();

        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

            return newHomeworkServiceClient.checkHomework(teacher, homeworkId, HomeworkSourceType.Wechat);
        } catch (Exception ex) {
            logger.error("Check homework error,hid:{},tid:{}", homeworkId, teacherId, ex);
        }
        return MapMessage.errorMessage("检查作业失败");
    }

    //作业报告-预览作业
    @RequestMapping(value = "/report/preview.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage previewHomework(@RequestParam String content) {
        if (StringUtils.isBlank(content)) return MapMessage.errorMessage("请选择预览作业");
        Long teacherId = getTeacherIdBySubject();
        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

            Map<String, List> contentMap = JsonUtils.fromJsonToMap(content, String.class, List.class);
            String bookId = getRequestString("bookId");
            MapMessage message = newHomeworkContentServiceClient.previewContent(teacher, bookId, contentMap);
            if (message.isSuccess()) {
                return MapMessage.successMessage().add("content", message.get("contents"));
            } else {
                return message;
            }
        } catch (Exception ex) {
            logger.error("Preview homework error,content:{}", content, ex);
        }
        return MapMessage.errorMessage("预览作业失败");
    }

    //作业报告-查询作业已有评论
    @RequestMapping(value = "/comments.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getHomeworkComment(@RequestParam String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("请选择要查询的作业");
        }
        Long teacherId = getRequestContext().getUserId();

        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) {
                return MapMessage.errorMessage("未查询到老师信息");
            }

            MapMessage msg = MapMessage.successMessage();
            List<HomeworkComment> comments = homeworkCommentLoaderClient.loadHomeworkComments(Collections.singleton(homeworkId)).toList();
            msg.add("comments", comments);
            msg.add("states", newHomeworkReportServiceClient.homeworkReportForStudentInfo(homeworkId));
            return msg;
        } catch (Exception ex) {
            logger.error("Get homeowrk comment error,hid:{}", homeworkId, ex);
            return MapMessage.errorMessage("查询评语失败");
        }
    }

    /**
     * 查询绘本
     */
    @RequestMapping(value = "reading/search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readingSearch() {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String readingName = getRequestString("readingName");
        String clazzLevels = getRequestString("clazzLevels");
        String topicIds = getRequestString("topicIds");
        String seriesIds = getRequestString("seriesIds");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);

        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("信息不全");
        }
        Teacher teacher = getTeacherBySubject();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }

        List<Integer> clazzLevelList = StringUtils.toList(clazzLevels, String.class)
                .stream()
                .filter(str -> PictureBookClazzLevel.of(str) != null)
                .map(str -> PictureBookClazzLevel.valueOf(str).getClazzLevel())
                .collect(Collectors.toList());
        List<String> topicIdList = StringUtils.toList(topicIds, String.class)
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> seriesIdList = StringUtils.toList(seriesIds, String.class)
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        PictureBookQuery pictureBookQuery = new PictureBookQuery();
        pictureBookQuery.setName(readingName);
        pictureBookQuery.setClazzLevels(clazzLevelList);
        pictureBookQuery.setTopicIds(topicIdList);
        pictureBookQuery.setSeriesIds(seriesIdList);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        return newHomeworkContentServiceClient.searchReading(pictureBookQuery, pageable, bookId, unitId, teacher);
    }

    @RequestMapping(value = "report/voicerecommend.vpage", method = RequestMethod.GET)
    public String voiceRecommend() {
        return "/teacher/homework/report/voicerecommend";
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

    private Boolean saveCorrectQuestionResult(Long studentId, NewHomework newHomework, ObjectiveConfigType type, String questionId, CorrectQuestionContext correct) {

        if (Objects.isNull(studentId) || Objects.isNull(correct) || Objects.isNull(newHomework) || StringUtils.isBlank(questionId)) {
            return false;
        }

        // 校验批改的作业是否存在
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        if (Objects.isNull(newHomeworkResult)) {
            return false;
        }

        // 准备更新数据
        NewHomeworkResultAnswer answer = newHomeworkResult.getPractices().getOrDefault(type, null);
        if (Objects.isNull(answer)) {
            return false;
        }

        // 做题明细id
        String processResultId = answer.getAnswers().getOrDefault(questionId, "");
        if (StringUtils.isBlank(processResultId)) {
            return false;
        }

        com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext context = new com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext();
        context.setStudentId(studentId);
        context.setHomeworkId(newHomework.getId());
        context.setQuestionId(questionId);
        context.setReview(correct.getReview());
        context.setType(type);
        context.setCorrectType(correct.getCorrectType());
        context.setCorrection(correct.getCorrection());
        context.setTeacherMark(correct.getTeacherMark());
        context.setIsBatch(false);
        context.setProcessResultId(processResultId);

        return newHomeworkProcessResultLoaderClient.updateCorrection(context);

    }

    @RequestMapping(value = "/report/scorerule.vpage", method = RequestMethod.GET)
    public String scoreRule() {
        return "teacher/homework/report/scorerule";
    }

    @RequestMapping(value = "/offlinehomework/index.vpage", method = RequestMethod.GET)
    public String offlineHomeworkIndex() {
        return "teacher/homework/offlinehomework/index";
    }

    @RequestMapping(value = "/offlinehomework/share.vpage", method = RequestMethod.GET)
    public String offlineHomeworkShare() {
        return "teacher/homework/offlinehomework/share";
    }

    @RequestMapping(value = "/offlinehomework/detail.vpage", method = RequestMethod.GET)
    public String offlineHomeworkDetail() {
        return "teacher/homework/offlinehomework/detail";
    }
}