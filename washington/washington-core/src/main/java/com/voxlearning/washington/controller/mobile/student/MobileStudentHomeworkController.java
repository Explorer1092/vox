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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.DisplayStudentHomeWorkHistoryMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.open.v1.util.AppHomeworkCardFilter;
import com.voxlearning.washington.mapper.StudentHomeworkResultItemMapper;
import com.voxlearning.washington.mapper.StudentHomeworkResultMapper;
import com.voxlearning.washington.service.homework.LoadHomeworkHelper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 14-10-21.
 */
@Controller
@RequestMapping("/studentMobile/homework")
@NoArgsConstructor
@Slf4j
public class MobileStudentHomeworkController extends AbstractMobileController {

    @Inject
    private LoadHomeworkHelper loadHomeworkHelper;

    /**
     * 查询班级内学生完成作业的信息
     */
    @RequestMapping(value = "/finishinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage finishInfo(@RequestParam String hid) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(hid);
            if (null == newHomework) {
                return MapMessage.errorMessage("未查询到作业");
            }

            Map<String, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoaderClient.findByHomeworkForReport(newHomework);
            if (MapUtils.isEmpty(homeworkResultMap)) {
                return MapMessage.errorMessage("未查询到作业结果");
            }

            NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(newHomework.getId());
            String title = "";
            if (newHomeworkBook != null) {
                Set<String> unitNames = newHomeworkBook.processUnitNameList();
                title = StringUtils.join(unitNames, ",");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日 HH:MM");

            StudentHomeworkResultMapper mapper = new StudentHomeworkResultMapper();
            mapper.setTitle(title);
            mapper.setEndDate(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(newHomework.getEndTime().getTime()), ZoneId.systemDefault())));
            mapper.setItems(new ArrayList<>());

            Set<Long> userIds = new HashSet<>();
            formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            for (NewHomeworkResult result : homeworkResultMap.values()) {
                if (null == result.getFinishAt()) continue;
                StudentHomeworkResultItemMapper m = new StudentHomeworkResultItemMapper();
                m.setUserId(result.getUserId());
                m.setFinishDate(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(result.getFinishAt().getTime()), ZoneId.systemDefault())));
                userIds.add(result.getUserId());
                mapper.getItems().add(m);
            }

            Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
            for (StudentHomeworkResultItemMapper m : mapper.getItems()) {
                if (userMap.containsKey(m.getUserId())) {
                    m.setUserName(userMap.get(m.getUserId()).fetchRealname());
                    m.setUserImg(userMap.get(m.getUserId()).fetchImageUrl());
                }
            }

            return MapMessage.successMessage().add("finish_info", mapper);
        } catch (Exception ex) {
            logger.error("Get homework {} finishinfo failed", hid, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "historylist.vpage", method = RequestMethod.GET)
    public String historyList(Model model) {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }
        String version = getRequestString("app_version");
        model.addAttribute("app_version", version);
        if (StringUtils.isNotBlank(version) && VersionUtil.compareVersion(version, "2.7.0.0") >= 0) {
            return "redirect:/view/mobile/student/homework/historylist";
        } else {
            return "studentmobile/homework/historylist";
        }
    }

    //作业题量
    @RequestMapping(value = "app/homework/category/practicenum.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String homeworkCategoryNum(Model model) {
        return "studentmobile/homework/homeworkinvalid";
    }


    //作业题量
    @RequestMapping(value = "app/homework/category/newpracticenum.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String newHomeworkCategoryNum(Model model) {

//        if (studentUnLogin()) {
//            return "studentmobile/logininvalid";
//        }
        User user = getHomeworkUser();
        if (user == null) {
            return "studentmobile/logininvalid";
        }
        String homeworkId = getRequest().getParameter("homeworkId");

        model.addAttribute("homeworkId", homeworkId);
        String version = getRequestString("app_version");

        String sys = "Android";
        if (isIOSRequest(getRequest())) {
            sys = "iOS";
        }

        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);

        model.addAttribute("valid", false);
        if (newHomework != null) {
            model.addAttribute("homeworkType", newHomework.getSubject());
            model.addAttribute("valid", true);
            List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
            AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator()), newHomework.getSubject().name(), types, version, sys);
            model.addAttribute("supportType", info == null ? AppHomeworkCardFilter.HomeworkCardSupportType.NOT_SUPPORTED : info.getSupportType());
            model.addAttribute("info", info);
            //新老版本都需要支持配置
            if (StringUtils.isBlank(version) || VersionUtil.compareVersion(version, "2.7.0.0") < 0) {
                model.addAttribute("newVersion", false);
            } else {
                model.addAttribute("newVersion", true);
                return "studentmobile/homework/skip";
            }
            NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(homeworkId);
            Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContentLinkedHashMap = newHomework.findPracticeContents();

            int totalCount = practiceContentLinkedHashMap.size();
            Map<String, Integer> englishCategoryPracticeCountMap = new LinkedHashMap<>();
            for (NewHomeworkPracticeContent npc : practiceContentLinkedHashMap.values()) {
                ObjectiveConfigType type = npc.getType();
                if (npc.getQuestions() != null || npc.getApps() != null) {
                    englishCategoryPracticeCountMap.put(type.getValue(), npc.getQuestions() != null ? npc.getQuestions().size() : npc.getApps().size());
                }
            }
            Teacher teacher = teacherLoaderClient.loadTeacher(newHomework.getTeacherId());
            model.addAttribute("teacher", teacher);
            model.addAttribute("note", newHomework.getRemark());
            LinkedHashSet<String> unitNames = new LinkedHashSet<>();
            if (newHomeworkBook != null && newHomeworkBook.getPractices() != null) {
                for (ObjectiveConfigType objectiveConfigType : newHomeworkBook.getPractices().keySet()) {
                    unitNames.addAll(newHomeworkBook.getPractices().get(objectiveConfigType).stream().map(NewHomeworkBookInfo::getUnitName).collect(Collectors.toList()));
                }
            }

            model.addAttribute("units", CollectionUtils.isEmpty(unitNames) ? "" : StringUtils.join(unitNames, "\n"));
            model.addAttribute("startDate", DateUtils.dateToString(newHomework.getStartTime(), "MM月dd日"));
            model.addAttribute("startDateTime", newHomework.getStartTime());
            model.addAttribute("allowStartHomework", allowStartNewHomework(newHomework, user.getId()));
            model.addAttribute("categoryPracticeCount", englishCategoryPracticeCountMap);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("sid", getRequestString("sid"));
        }
        return "studentmobile/homework/detail";
    }

    /**
     * 自主考试
     */
    @RequestMapping(value = "app/newexam.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String independentExam(Model model) {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }
        String newExamId = getRequestString("newExamId");
        model.addAttribute("newExamId", newExamId);
        return "studentmobile/newexam/skip";
    }

    @RequestMapping(value = "app/currentmonth/history.vpage", method = RequestMethod.GET)
    public String currentMonthHistory(Model model) {
        return "studentmobile/logininvalid";
    }

    //作业历史
    @RequestMapping(value = "app/currentmonth/history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage currentMonthHistory() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * @return
     */
    @RequestMapping(value = "app/currentmonth/history/detail.vpage", method = RequestMethod.GET)
    @Deprecated
    public String homeworkDetail(Model model) {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }
        String homeworkId = getRequestString("homeworkId"); //作业ID
        String version = getRequestString("app_version");
        String sys = "Android";
        if (isIOSRequest(getRequest())) {
            sys = "iOS";
        }
        if (StringUtils.isNotBlank(version) && VersionUtil.compareVersion(version, "2.7.0.0") >= 0) {
            //新老版本都需要支持配置
            model.addAttribute(AppHomeworkCardFilter.HomeworkCardSupportType.SUPPORTED);
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            model.addAttribute("valid", false);
            if (newHomework != null) {
                List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
                AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator()), newHomework.getSubject().name(), types, version, sys);
                model.addAttribute("supportType", info == null ? AppHomeworkCardFilter.HomeworkCardSupportType.NOT_SUPPORTED : info.getSupportType());
                model.addAttribute("info", info);
                model.addAttribute("valid", true);
            }
            model.addAttribute("newVersion", true);
            model.addAttribute("homeworkId", homeworkId);
            return "studentmobile/homework/skip";
        }
        Subject subject = Subject.of(getRequestString("subject"));  //学科
        model.addAttribute("subject", subject);
        User user = currentUser();
        Long studentId = user.getId();
        MapMessage message = newHomeworkReportServiceClient.loadStudentNewHomeworkHistoryDetail(homeworkId, studentId);
        if (message.isSuccess()) {
            Map result = JsonUtils.fromJson(JsonUtils.toJson(message.get("result")));
            Map<String, Object> homeworkModuleScore = new LinkedHashMap<>();
            model.addAttribute("createDate", DateUtils.dateToString(DateUtils.stringToDate(SafeConverter.toString(result.get("createDate"))), "MM月dd日"));
            model.addAttribute("finished", result.get("finished")); // 是否完成
            List<Map> details = JsonUtils.fromJsonToList(JsonUtils.toJson(result.get("objectiveConfigTypes")), Map.class);
            List<Map> scoreList = new ArrayList<>();
            for (Map detail : details) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("practiceType", detail.get("objectiveConfigTypeName"));
                obj.put("score", detail.get("score"));
                obj.put("isSubjective", detail.get("isSubjective"));
                obj.put("finished", detail.get("finished"));
                obj.put("corrected", detail.get("corrected"));
                obj.put("corrections", detail.get("corrections"));
                scoreList.add(obj);
            }
            model.addAttribute("scoreList", scoreList);
            homeworkModuleScore.put("studentAvgScore", result.get("avgScore"));
            model.addAttribute("homeworkModuleScore", homeworkModuleScore);
            Map<String, Object> comment = new HashMap<>();
            if (StringUtils.isNoneBlank(SafeConverter.toString(result.get("comment")))) {
                Long teacherId = SafeConverter.toLong(result.get("teacherId"));
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                String teacherName = teacher == null ? null : teacher.fetchRealname();
                String imgUrl = teacher == null ? "" : teacher.fetchImageUrl();
                comment.put("teacherName", teacherName);
                comment.put("teacherImg", imgUrl);
                comment.put("commentContent", result.get("comment"));
            }
            model.addAttribute("comment", comment);
        }
        return "studentmobile/homework/currentmonthdetail";
    }

    @RequestMapping(value = "app/history.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @Deprecated
    public MapMessage appHistory() {
        return MapMessage.errorMessage("功能已下线");
    }


    @RequestMapping(value = "history.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage pageHistory() {

        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        StudentDetail studentDetail = currentStudentDetail();
        List<Map<String, Object>> resultList = new ArrayList<>();
        String ver = getRequestString("app_version");
        String sys = "Android";
        if (isIOSRequest(getRequest())) {
            sys = "iOS";
        }

        Boolean showScore = true;
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList")) {
            showScore = false;
        }
        Map<String, Object> nativeHomeworkCardMapping = AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator());
        Date currentDate = new Date();
        Date startDate = NewHomeworkConstants.STUDENT_ALLOW_SEARCH_HOMEWORK_START_TIME;
        // 页数
        int currentPage = getRequestInt("page", 1);
        Pageable page = new PageRequest((currentPage < 1) ? 0 : currentPage - 1, 10);

        Page<DisplayStudentHomeWorkHistoryMapper> newHomeworkHistorys = newHomeworkReportServiceClient.loadStudentNewHomeworkHistory(studentDetail, startDate, currentDate, page);

        for (DisplayStudentHomeWorkHistoryMapper obj : newHomeworkHistorys.getContent()) {
            AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(nativeHomeworkCardMapping, obj.getSubject().name(), obj.getTypes(), ver, sys);
//            if (info.getSupportType() != AppHomeworkCardFilter.HomeworkCardSupportType.SUPPORTED) {
//                continue;
//            }
            Map<String, Object> m = new HashMap<>();
            m.put("clazzId", obj.getClazzId());
            m.put("bookName", obj.getBookName());
            m.put("unitName", obj.getUnitNames());
            m.put("checked", obj.getChecked());
            m.put("commentTeacherImg", obj.getCommentTeacherImg());
            m.put("commentTeacherName", obj.getCommentTeacherName());
            m.put("clazzName", obj.getClazzName());
            m.put("createTime", obj.getCreateTime());
            m.put("startDate", obj.getStartDate());
            m.put("endDate", obj.getEndDate());
            m.put("homeworkId", obj.getHomeworkId());
            if (showScore) {
                m.put("score", obj.getHomeworkScore() != null ? obj.getHomeworkScore() + "分" : obj.getCorrectedType());
            } else {
                m.put("score", obj.getHomeworkScore() == null ? null : ScoreLevel.processLevel(obj.getHomeworkScore()).getLevel());
            }
            m.put("note", obj.getNote());
            m.put("state", obj.getState());
            m.put("userId", obj.getUserId());
            m.put("userName", obj.getUserName());
            m.put("subject", obj.getSubject());
            m.put("correctedType", obj.getCorrectedType());
            m.put("homeworkCardSource", info.getSourceType());
            m.put("homeworkCardVariety", info.getHomeworkOrQuiz());
            m.put("homeworkType", obj.getHomeworkType());
            m.put("timeLimit", obj.getTimeLimit());
            m.put("hCorrectStatus", obj.getHCorrectStatus());
            resultList.add(m);
        }
        Map<String, List<Map>> dayMaps = new LinkedHashMap<>();
        for (Map map : resultList) {
            String day = SafeConverter.toString(map.get("startDate"));
            List<Map> days = dayMaps.get(day);
            if (days == null) {
                days = new ArrayList<>();
            }
            days.add(map);
            dayMaps.put(day, days);
        }
        return MapMessage.successMessage().add("homeworkHistory", newHomeworkHistorys).add("homeworkHistoryGroupByDay", dayMaps);
    }

    // 口算训练榜单页面
    @RequestMapping(value = "mentalArithmetic/chart.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage mentalArithmeticChart() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        try {
            StudentDetail studentDetail = currentStudentDetail();
            return newHomeworkReportServiceClient.loadMentalArithmeticChart(homeworkId, studentDetail.getId());
        } catch (Exception ex) {
            logger.error("Get student mentalArithmetic chart failed, homeworkID:{}", homeworkId, ex);
            return MapMessage.errorMessage("获取口算榜单异常");
        }
    }

    //是否有未完成的作业
    @RequestMapping(value = "undone.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage hasUndoneHomework() {

        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        StudentDetail studentDetail = currentStudentDetail();
        boolean homeworkUndoneFlag = loadHomeworkHelper.hasUndoneHomework(studentDetail);
        return MapMessage.successMessage().add("undone", homeworkUndoneFlag);
    }

    // 完成作业得分
    @RequestMapping(value = "score.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkScore() {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * 是否允许开始新作业体系作业
     *
     * @param newHomework 新作业体系作业
     * @param studentId   学生id
     * @return
     */
    private boolean allowStartNewHomework(NewHomework newHomework, Long studentId) {
        // 已检查或者过期作业不显示开始按钮
        if (newHomework == null || newHomework.isHomeworkTerminated()) {
            return false;
        }
        // 不是自己的作业不显示开始按钮
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false)
                .stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        if (!groupIds.contains(newHomework.getClazzGroupId())) {
            return false;
        }

        // 已完成的不显示
        NewAccomplishment newAccomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(newHomework.toLocation());
        if (newAccomplishment != null && MapUtils.isNotEmpty(newAccomplishment.getDetails()) && newAccomplishment.getDetails().containsKey(String.valueOf(studentId))) {
            return false;
        }

        return true;
    }

    @RequestMapping(value = "app/skip.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String message(Model model) {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }
        String homeworkId = getRequest().getParameter("homeworkId");
        String version = getRequestString("app_version");
        String sys = "Android";
        if (isIOSRequest(getRequest())) {
            sys = "iOS";
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        model.addAttribute("valid", false);
        if (newHomework != null) {
            List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
            AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator()), newHomework.getSubject().name(), types, version, sys);
            model.addAttribute("supportType", info == null ? AppHomeworkCardFilter.HomeworkCardSupportType.NOT_SUPPORTED : info.getSupportType());
            model.addAttribute("info", info);
            model.addAttribute("valid", true);
        }
        model.addAttribute("homeworkId", homeworkId);
        if (StringUtils.isBlank(version) || VersionUtil.compareVersion(version, "2.7.0.0") < 0) {
            model.addAttribute("newVersion", false);
        } else {
            model.addAttribute("newVersion", true);
        }
        return "studentmobile/homework/skip";
    }

    @RequestMapping(value = "vacation/packagelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String vacationPackages(Model model) {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }
        String packageId = getRequest().getParameter("packageId");
        String version = getRequestString("app_version");
        String sys = "Android";
        if (isIOSRequest(getRequest())) {
            sys = "iOS";
        }
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkLoaderClient.loadVacationHomeworkPackageById(packageId);
        if (vacationHomeworkPackage != null) {
            String homeworkType = HomeworkType.VACATION_ENGLISH.name();
            switch (vacationHomeworkPackage.getSubject()) {
                case ENGLISH:
                    break;
                case MATH:
                    homeworkType = HomeworkType.VACATION_MATH.name();
                    break;
                case CHINESE:
                    homeworkType = HomeworkType.VACATION_CHINESE.name();
                    break;
                default:
                    break;
            }
            List<String> types = Collections.singletonList("VACATION");
            AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator()), "VACATION_" + vacationHomeworkPackage.getSubject().name(), types, version, sys);
            model.addAttribute("supportType", info == null ? AppHomeworkCardFilter.HomeworkCardSupportType.NOT_SUPPORTED : info.getSupportType());
            model.addAttribute("info", info);
            model.addAttribute("homeworkType", homeworkType);
            model.addAttribute("packageId", packageId);
        } else {
            return "studentmobile/logininvalid";
        }
        return "studentmobile/vacation/skip";
    }

    @RequestMapping(value = "vacation/skip.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String vacationHomework(Model model) {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }
        String homeworkId = getRequestString("homeworkId");
        String version = getRequestString("app_version");
        String sys = "Android";
        if (isIOSRequest(getRequest())) {
            sys = "iOS";
        }
        VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
        if (vacationHomework != null) {
            List<String> types = Collections.singletonList("VACATION");
            AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator()), "VACATION_" + vacationHomework.getSubject().name(), types, version, sys);
            model.addAttribute("supportType", info == null ? AppHomeworkCardFilter.HomeworkCardSupportType.NOT_SUPPORTED : info.getSupportType());
            model.addAttribute("info", info);
            model.addAttribute("packageId", vacationHomework.getPackageId());
            model.addAttribute("homeworkId", homeworkId);
            model.addAttribute("newProcess", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(currentStudentDetail(), "StudentHomework", "NewIndexUrl"));
        } else {
            return "studentmobile/logininvalid";
        }
        return "studentmobile/vacation/homework/skip";
    }

}
