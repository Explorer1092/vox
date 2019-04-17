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

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.cache.TeacherScholarshipRecordCache;
import com.voxlearning.utopia.entity.activity.MathActivityRecord;
import com.voxlearning.utopia.entity.activity.TangramActivityStudent;
import com.voxlearning.utopia.entity.activity.TeacherAssignTermReviewRecord;
import com.voxlearning.utopia.entity.activity.TeacherScholarshipRecord;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherVocationLottery;
import com.voxlearning.utopia.service.campaign.api.mapper.MathActivityConfig;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.campaign.client.TeacherLotteryServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.JxslpApplicationForm;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.temp.LuckyBagActivity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.alps.core.util.MapUtils.m;

/**
 * @author RuiBao
 * @version 0.1
 * @since 8/6/2015
 */
@Controller
@RequestMapping("/teacher/activity/")
public class TeacherActivityController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private TeacherLotteryServiceClient tchLotteryServiceCli;
    @Inject private CampaignLoaderClient campaignLoaderClient;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private VacationHomeworkServiceClient vocationHomeworkServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private TeacherActivityServiceClient teacherActivityServiceClient;

    @ImportService(interfaceClass = TeacherActivityService.class)
    private TeacherActivityService tchActSrv;

    public static Date TERM_REVIEW_START_DATE = DateUtils.stringToDate("2018-12-10 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
    public static Date TERM_REVIEW_END_DATE = DateUtils.stringToDate("2019-01-10 23:59:59", DateUtils.FORMAT_SQL_DATETIME);

    /**
     * 活动页面统配入口
     */
    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page) {
        if (page.equals("christmaslottery") || page.equals("christmas")) {
            return "redirect:/index.vpage";
        }
        return "teacherv3/activity/" + page;
    }

    // 一起作业学生手机版上线啦
    @RequestMapping(value = "recommendedapp.vpage", method = RequestMethod.GET)
    public String recommendedApp() {
        return "teacherv3/activity/recommendedapp";
    }

    /**
     * 教学实力派报名
     */
    @RequestMapping(value = "jxslp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage jxslp() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS)
            return MapMessage.errorMessage("只有认证教师才能报名");

        // 虚假老师直接返回
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacher.getId());
        if (teacherSummary != null && SafeConverter.toBoolean(teacherSummary.getFakeTeacher()) && CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(teacherSummary.getValidationType())) {
            return MapMessage.successMessage();
        }

        String qq = getRequestString("qq");
        String degree = getRequestString("degree");
        String grade = getRequestString("grade");
        String book = getRequestString("book");
        String goodAt = getRequestString("goodAt");
        String schoolLevel = getRequestString("schoolLevel");
        String duration = getRequestString("duration");
        String mitc = getRequestString("mitc");
        String honour = getRequestString("honour");
        Gender gender = Gender.fromCode(getRequestString("gender"));
        String mobile = getRequestString("mobile");
        if (StringUtils.isBlank(qq) || StringUtils.isBlank(degree) || StringUtils.isBlank(grade) ||
                StringUtils.isBlank(book) || StringUtils.isBlank(goodAt) || StringUtils.isBlank(schoolLevel) ||
                StringUtils.isBlank(duration) || StringUtils.isBlank(mitc) || StringUtils.isBlank(honour) ||
                gender == Gender.NOT_SURE || StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage();
        }
        if (!MobileRule.isMobile(mobile)) return MapMessage.errorMessage("手机号码不正确");
        if (!qq.matches("^\\d+$")) return MapMessage.errorMessage("QQ号码不正确");

        JxslpApplicationForm entity = new JxslpApplicationForm();
        entity.setCreateDatetime(new Date());
        entity.setUserId(teacher.getId());
        entity.setName(teacher.fetchRealname());
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
        String authMobile = ua == null || !ua.isMobileAuthenticated() ? "" : ua.getSensitiveMobile();
        entity.setSensitiveMobile(sensitiveUserDataServiceClient.encodeMobile(StringUtils.isNotBlank(authMobile) ? authMobile : mobile));
        entity.setSubject(teacher.getSubject());
        entity.setLeader(getRequestBool("leader"));
        entity.setWilling(getRequestBool("willing"));
        entity.setAmbassador(teacher.isSchoolAmbassador());
        entity.setSensitiveQq(sensitiveUserDataServiceClient.encodeQq(qq));
        entity.setDegree(degree);
        entity.setGrade(grade);
        entity.setBook(book);
        entity.setGoodAt(goodAt);
        entity.setSchoollevel(schoolLevel);
        entity.setDuration(duration);
        entity.setMitc(mitc);
        entity.setHonour(honour);
        entity.setGender(gender);
        return teacherServiceClient.applyJxslp(entity);
    }

    // 福袋活动页面
    @RequestMapping(value = "luckybag.vpage", method = RequestMethod.GET)
    public String luckyBag(Model model) {
        if (!LuckyBagActivity.isInPeriod()) {
            return "redirect:/teacher/index.vpage";
        }
        Long teacherId = currentUserId();
        // 没有班级的校验
        List<GroupMapper> groups = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, false);
        if (CollectionUtils.isEmpty(groups)) {
            return "redirect:/teacher/index.vpage";
        }
        Set<Long> clazzIds = groups.stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMapper group : groups) {
            Clazz clazz = clazzs.get(group.getClazzId());
            if (clazz == null || clazz.isTerminalClazz()) continue;
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", group.getId());
            map.put("clazzId", clazz.getId());
            map.put("clazzName", clazz.formalizeClazzName());
            result.add(map);
        }
        model.addAttribute("clazzs", result);
        return "teacherv3/activity/luckybag";
    }

    // 获取班级下的福袋情况
    @RequestMapping(value = "loadgroupluckybaginfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadGroupLuckyBagInfo() {
        if (!LuckyBagActivity.isInPeriod()) {
            return MapMessage.errorMessage("活动已结束");
        }
        Long groupId = getRequestLong("groupId");
        Long teacherId = currentUserId();
        if (groupId == 0 || teacherId == null) {
            return MapMessage.errorMessage("参数错误");
        }
        Map<String, Object> indexData = businessTeacherServiceClient.loadTeacherLuckyBagInfo(groupId, teacherId);
        return MapMessage.successMessage().add("data", indexData);
    }

    // 领取班级奖励
    @RequestMapping(value = "receiveclazzreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage receiveClazzReward() {
        if (!LuckyBagActivity.isInPeriod()) {
            return MapMessage.errorMessage("活动已结束");
        }
        Long groupId = getRequestLong("groupId");
        Long teacherId = currentUserId();
        if (groupId == 0 || teacherId == null) {
            return MapMessage.errorMessage("参数错误");
        }
        if (asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(UserBehaviorType.LUCKY_BAG_CLAZZ_REWARD_COUNT, groupId)
                .getUninterruptibly() > 0) {
            return MapMessage.errorMessage("已经领取过了");
        }
        try {
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("TEACHER_RECEIVE_LUCKY_BAG")
                    .keys(groupId)
                    .proxy()
                    .receiveLuckyBagClazzReward(groupId, teacherId);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请稍后重试");
        } catch (Exception ex) {
            logger.error("teacher receive lucky bag group reward error, groupId {}, error is {}", groupId, ex.getMessage());
            return MapMessage.errorMessage("提交失败，请重试");
        }
    }

    /**
     * 假期作业预热
     */
    @RequestMapping(value = "yurebavhstac.vpage", method = RequestMethod.GET)
    public String yuReBavhstac() {
        return "teacherv3/activity/yurebavhstac";
    }


    /* 2016教师节活动 pc活动页面  下线了*/
    @RequestMapping(value = "teachersday2016.vpage", method = RequestMethod.GET)
    public String teachersDay2016(Model model) {
        return "redirect:/index.vpage";
    }

    @RequestMapping(value = "newexamad.vpage", method = RequestMethod.GET)
    public String newExamAd() {
        Teacher teacher = currentTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return "redirect:/teacher/index.vpage";
        }
        Long englishTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), Subject.ENGLISH);
        if (englishTeacherId == null) {
            return "redirect:/teacher/index.vpage";
        }
        return "teacherv3/activity/newexamad";
    }

    /**
     * ---------------------------------泰安数学趣味周活动-------------------------------------------------
     */

    private List<ClazzLevel> loadTeacherClazzLvl(Long teacherId) {
        List<Long> allTchIds = new ArrayList<>();
        allTchIds.add(teacherId);
        allTchIds.addAll(teacherLoaderClient.loadSubTeacherIds(teacherId));

        return deprecatedGroupLoaderClient.loadTeacherGroups(allTchIds, false)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(gm -> gm.getSubject() == Subject.MATH)
                .map(gm -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(gm.getClazzId()))
                .map(Clazz::getClazzLevel)
                .distinct() // 去下重
                .collect(Collectors.toList());
    }

    private Comparator<MathActivityRecord> getTAMRecordComparator() {
        Function<String, Integer> getSortVal = clazzName -> {
            if (StringUtils.isEmpty(clazzName))
                return 0;

            int r1Val = 0;
            if (clazzName.endsWith("班")) {
                r1Val = SafeConverter.toInt(clazzName.substring(0, clazzName.length() - 1));
            }

            return r1Val;
        };

        return Comparator.comparing(r -> getSortVal.apply(r.getClazzName()));
    }

    @RequestMapping("/math/match/home.vpage")
    @ResponseBody
    public MapMessage home() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "未登录不能操作!");

            MathActivityConfig config = tchActSrv.loadActivityConfig();
            int currPhase = config.judgePhase(new Date());
            Date starTime = config.parseStartTime();
            // 11号布置作业按钮黑灰
            Date endTime = DateUtils.stringToDate("2018-05-11 00:00:00");
            Date now = new Date();


            // 如果正式进入第一期开始的时间，则点亮排行按钮
            return MapMessage.successMessage()
                    .add("openRank", currPhase >= 1)
                    .add("openAssign", starTime != null && now.after(starTime) && now.before(endTime))
                    .add("uid", teacher.getId());
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping("/math/match/my_info.vpage")
    @ResponseBody
    public MapMessage loadMyRankInfo() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "未登录不能操作!");

            Integer clazz = getRequestInt("clazz");
            Integer phase = getRequestInt("phase");
            Long teacherId = teacher.getId();

            // 获取上一次记录的班级， 如果这次传入新的则更新
            int selectedClazz = tchActSrv.gupSelectedClazz(teacherId, clazz);
            int queryClazz = clazz == 0 ? selectedClazz : clazz;
            int currPhase = tchActSrv.getCurrentPhase();
            // 如果未传入期数，则看的是最近一期的数据
            int queryPhase = phase == 0 ? currPhase : phase;

            // 获得老师教的年级列表
            List<Map<String, Object>> clazzList = loadTeacherClazzLvl(teacherId)
                    .stream()
                    .map(cl -> m("code", cl.getLevel(), "name", cl.getDescription()))
                    .collect(Collectors.toList());

            List<MathActivityRecord> myRecords = tchActSrv.loadTeacherRecords(teacherId)
                    .stream()
                    .filter(r -> Objects.equals(r.getPhase(), queryPhase))
                    .filter(r -> Objects.equals(r.getClazz(), queryClazz))
                    .sorted(getTAMRecordComparator())
                    .collect(Collectors.toList());

            return MapMessage.successMessage()
                    .add("myRecords", myRecords)
                    .add("rank", tchActSrv.loadActivityRank(queryPhase, queryClazz))
                    .add("clazzList", clazzList)
                    .add("currClazz", queryClazz)
                    .add("currPhase", currPhase)
                    .add("phase", queryPhase)
                    .add("teacherId", teacher.getId())
                    .add("openFinal", currPhase >= 3);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping("/math/match/final.vpage")
    @ResponseBody
    public MapMessage loadFinalRank() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "未登录不能操作!");

            Integer clazz = getRequestInt("clazz");
            Validate.inclusiveBetween((Integer) 1, (Integer) 6, clazz);
            Long teacherId = teacher.getId();

            int finalPhase = TeacherActivityService.TOTAL_RANK_PHASE;
            // 获得老师教的年级列表
            List<MathActivityRecord> myRecords = tchActSrv.loadTeacherRecords(teacherId)
                    .stream()
                    .filter(r -> Objects.equals(r.getPhase(), finalPhase))
                    .filter(r -> Objects.equals(r.getClazz(), clazz))
                    .sorted(getTAMRecordComparator())
                    .collect(Collectors.toList());

            return MapMessage.successMessage()
                    .add("myRecords", myRecords)
                    .add("rank", tchActSrv.loadActivityRank(finalPhase, clazz))
                    .add("currClazz", clazz)
                    .add("teacherId", teacher.getId());
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    //==========================================================
    //===============         七巧板活动        =================
    //==========================================================

    @RequestMapping(value = "/tangram/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage tangramStudentList() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.getTeacherSchoolId() == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (!checkTangramSchool(teacher.getId())) {
            return MapMessage.errorMessage("很抱歉您不在本活动参与范围");
        }
        try {
            List<Map<String, Object>> students = teacherActivityServiceClient.getRemoteReference()
                    .loadTangramTeacherStudents(teacher.getId())
                    .stream()
                    .map(TangramActivityStudent::snapshot)
                    .collect(Collectors.toList());

            return MapMessage.successMessage()
                    .add("schoolName", teacher.getTeacherSchoolName())
                    .add("studentList", students);
        } catch (Exception ex) {
            logger.error("Failed load tangram activity teacher students, teacherId={}", teacher.getId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/tangram/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage tangramStudentInfo() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.getTeacherSchoolId() == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (!checkTangramSchool(teacher.getId())) {
            return MapMessage.errorMessage("很抱歉您不在本活动参与范围");
        }
        Long studentId = getRequestLong("studentId");
        try {
            TangramActivityStudent student = null;
            if (studentId != 0L) {
                student = teacherActivityServiceClient.getRemoteReference()
                        .loadTangramStudent(studentId);
                if (student == null || student.isDisabledTrue()) {
                    return MapMessage.errorMessage("该学生不存在");
                }
            }

            return MapMessage.successMessage()
                    .add("schoolName", teacher.getTeacherSchoolName())
                    .add("studentInfo", student == null ? Collections.emptyMap() : student.snapshot());
        } catch (Exception ex) {
            logger.error("Failed load tangram activity student info , teacherId={}, studentId={}", teacher.getId(), studentId, ex);
            return MapMessage.errorMessage("页面异常，请检查后重试");
        }
    }

    @RequestMapping(value = "/tangram/student/add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addTangramStudent() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.getTeacherSchoolId() == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (!checkTangramSchool(teacher.getId())) {
            return MapMessage.errorMessage("很抱歉您不在本活动参与范围");
        }
        String studentCode = getRequestString("studentCode");
        String studentName = getRequestString("studentName");
        String className = getRequestString("className");
        String masterpieceUrls = getRequestString("masterpieces");
        if (StringUtils.isAnyBlank(studentCode, studentName, className, masterpieceUrls)) {
            return MapMessage.errorMessage("参数为空");
        }
        List<String> masterpieces = Stream.of(masterpieceUrls.split(","))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        try {
            return teacherActivityServiceClient.addTangramStudent(teacher.getId(), studentName, studentCode, className, masterpieces);
        } catch (Exception ex) {
            logger.error("Failed add tangram activity student, teacherId={}", teacher.getId(), ex);
            return MapMessage.errorMessage("页面异常，请检查后重试");
        }
    }

    @RequestMapping(value = "/tangram/student/modify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyTangramStudent() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.getTeacherSchoolId() == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (!checkTangramSchool(teacher.getId())) {
            return MapMessage.errorMessage("很抱歉您不在本活动参与范围");
        }
        Long studentId = getRequestLong("studentId");
        String studentCode = getRequestString("studentCode");
        String studentName = getRequestString("studentName");
        String className = getRequestString("className");
        String masterpieceUrls = getRequestString("masterpieces");
        if (StringUtils.isAnyBlank(studentCode, studentName, className, masterpieceUrls)) {
            return MapMessage.errorMessage("参数为空");
        }
        List<String> masterpieces = Stream.of(masterpieceUrls.split(","))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        try {
            TangramActivityStudent student = teacherActivityServiceClient.getRemoteReference()
                    .loadTangramStudent(studentId);
            if (student == null || student.isDisabledTrue()) {
                return MapMessage.errorMessage("该学生不存在");
            }

            return teacherActivityServiceClient.modifyTangramStudent(studentId, studentName, studentCode, className, masterpieces);
        } catch (Exception ex) {
            logger.error("Failed modify tangram activity student, studentId={}", studentId, ex);
            return MapMessage.errorMessage("页面异常，请检查后重试");
        }
    }

    private boolean checkTangramSchool(Long teacherId) {
        if (teacherId == null || teacherId == 0L) {
            return false;
        }
        // 测试环境放行
        if (RuntimeMode.le(Mode.TEST)) {
            return true;
        }
        try {
            String allTeachers = commonConfigServiceClient.getCommonConfigBuffer()
                    .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "TANGRAM_INVITATION");

            return StringUtils.isNotBlank(allTeachers)
                    && Stream.of(allTeachers.split(","))
                    .map(SafeConverter::toLong)
                    .anyMatch(t -> Objects.equals(t, teacherId));
        } catch (Exception ignored) {
            return false;
        }
    }


    /**
     * ---------------------------------2018-2019学年上学期期末奖学金活动-------------------------------------------------
     * //1，页面展示平均完成率
     * //2，每天布置期末复习作业，页面按钮可以点击，当前结束，按钮重新可以点击
     * //3，每周布置两次期末复习作业，页面按钮可以点击，本周结束,按钮重新可以点击
     * //4，三周布置了期末复习作业的，页面按钮可以点击，按钮持续到活动结束
     * //5，页面签到领取园丁豆，不需要累计，只需要每天点击一下即可
     *
     * @return
     */
    @RequestMapping(value = "/scholarship/detail2019.vpage")
    @ResponseBody
    public MapMessage scholarShipDetailPage2() {
        MapMessage resultMsg = MapMessage.successMessage();
        Teacher teacher = currentTeacher();
//        Long teacherId = 11448670L;
//        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        Validate.notNull(teacher, "需要登陆才能操作!");
        Long teacherId = teacher.getId();
        //查询平均完成率
        //考虑包班制的情况
        List<Long> allTeacherIds = new ArrayList<>();
        allTeacherIds.add(teacher.getId());
        List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacher.getId());
        allTeacherIds.addAll(subTeacherIds);

        boolean assignDayTermReview = false;
        List<TeacherScholarshipRecord> shipRecords = new ArrayList<>();
        int basicReviewNum = 0;//基础必过取累计次数。
        for (Long tempId : allTeacherIds) {
            TeacherScholarshipRecord tmp = teacherActivityServiceClient.loadTeacherScholarshipRecord(tempId);
            if (Objects.nonNull(tmp)) {
                basicReviewNum += tmp.getBasicReviewNum();
                shipRecords.add(tmp);
            }
        }

        TeacherScholarshipRecord record = null;
        if (CollectionUtils.isNotEmpty(shipRecords)) {
            Collections.sort(shipRecords, (o1, o2) -> o2.getFinishRate().compareTo(o1.getFinishRate()));
            record = shipRecords.get(0);
        }

        boolean basicReview = false;
        String finishRateStr = "0";
        if (Objects.nonNull(record)) {
            //完成率进行百分比计算
            if (Objects.nonNull(record.getFinishRate())) {
                BigDecimal finishRate = new BigDecimal(record.getFinishRate()).multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
                finishRateStr = finishRate.toString();
            }
            if (basicReviewNum > 0) {
                //布置了基础必过标志
                basicReview = true;
            }
        }
        resultMsg.set("finishRate", finishRateStr);
        resultMsg.add("basicReview", basicReview ? "已完成" : "未完成");
        boolean yuandingdou = TeacherScholarshipRecordCache.getApplyYuandingdouCache(teacherId);
        resultMsg.add("yuandingdou", yuandingdou);

        //每日点击的标志，在缓存中查找当日是否点击过
        boolean dayTermReview = TeacherScholarshipRecordCache.getApplyDayTermReviewCache(teacherId);
        if (Objects.nonNull(record)) {
            boolean dailyLottery = record.getDailyLottery();
            resultMsg.add("dayTermReview", dailyLottery || dayTermReview);
        } else {
            resultMsg.add("dayTermReview", false);
        }

        //每周点击两次，当前时间所属的周是否布置了两次期末复习作业
        boolean weekTermReview = TeacherScholarshipRecordCache.getApplyWeekTermReviewCache(teacherId);
        Map<String, Object> weekTermReviewMap = new LinkedHashMap<>();
        if (Objects.nonNull(record)) {
            boolean weekLottery = record.getWeekLottery();
            weekTermReviewMap.put("weekTermReview", weekLottery || weekTermReview);
        } else {
            weekTermReviewMap.put("weekTermReview", false);
        }

        Date now = new Date();
        List<TeacherAssignTermReviewRecord> records = TeacherScholarshipRecordCache.loadRecords(allTeacherIds);
        //判断今日是否布置过期末复习,从缓存中去判断,如果有当天的记录证明当天布置过
        String currDate = DateUtils.dateToString(now, DateUtils.FORMAT_SQL_DATE);
        records = records.stream().filter(o -> Objects.equals(currDate, DateUtils.dateToString(o.getAssignDate(), DateUtils.FORMAT_SQL_DATE))).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(records)) {
            assignDayTermReview = true;
        }
        resultMsg.add("assignDayTermReview", assignDayTermReview);


        Integer curWeek = getWeek(now);
        int weekTermReviewData = TeacherScholarshipRecordCache.loadRecords(allTeacherIds).stream().filter(o -> Objects.equals(curWeek, o.getWeek())).collect(Collectors.toList()).size();
        if (weekTermReviewData > 1) {
            weekTermReviewMap.put("weekTermReviewData", 2);//1代表本周完成了一次，亮一个点，2代表亮两个点
        } else {
            weekTermReviewMap.put("weekTermReviewData", weekTermReviewData);
        }
        resultMsg.add("weekTermReviewMap", weekTermReviewMap);

        //每周点击两次，当前时间所属的周是否布置了两次期末复习作业
        boolean monthTermReview = TeacherScholarshipRecordCache.getApplyMonthTermReviewCache(teacherId);
        Map<String, Object> monthTermReviewMap = new LinkedHashMap<>();
        monthTermReviewMap.put("monthTermReview", monthTermReview);
        int monthTermReviewData = TeacherScholarshipRecordCache.loadRecords(allTeacherIds).stream().filter(o -> !Objects.equals(o.getWeek(), 0)).collect(Collectors.groupingBy(TeacherAssignTermReviewRecord::getWeek)).size();
        monthTermReviewMap.put("monthTermReviewData", monthTermReviewData > 3 ? 3 : monthTermReviewData); //0代表一周都没有布置作业，1代表布置了一周，2，代表布置了2周
        resultMsg.add("monthTermReviewMap", monthTermReviewMap);

        resultMsg.add("userName", teacher.fetchRealname().substring(0, 1) + "老师");
        resultMsg.add("userAvatar", getUserAvatarImgUrl(teacher));
        //申领每周奖品的人数,和展示的日期,下周周一公布
        Integer weekScholarshipsCount = TeacherScholarshipRecordCache.loadApplyWeekTermReviewTeacher(curWeek);
        resultMsg.add("weekScholarshipsCount", weekScholarshipsCount);
        //下周周一
        Date nextMonday;
        if (now.before(TERM_REVIEW_START_DATE)) {
            nextMonday = WeekRange.newInstance(TERM_REVIEW_START_DATE.getTime()).next().getStartDate();
        } else {
            nextMonday = WeekRange.newInstance(now.getTime()).next().getStartDate();
        }
        String weekScholarshipsCountDate = DateUtils.dateToString(nextMonday, "MM月dd");
        resultMsg.add("weekScholarshipsCountDate", weekScholarshipsCountDate);

        Integer monthScholarshipsCount = TeacherScholarshipRecordCache.loadApplyMonthTermReviewTeacher();
        resultMsg.add("monthScholarshipsCount", monthScholarshipsCount);
        resultMsg.add("monthScholarshipsCountDate", "1月11号");

        //每日获奖的十个人
        List<Map<String, Object>> dailyList = TeacherScholarshipRecordCache.loadDayScholarshipTeachers();
        resultMsg.add("dailyList", dailyList);

        return resultMsg;
    }

    public static void main(String[] args) {
        Date nextMonday = WeekRange.newInstance(new Date().getTime()).next().getStartDate();
        String weekScholarshipsCountDate = DateUtils.dateToString(nextMonday, "MM-dd");
        System.out.println(weekScholarshipsCountDate);
    }

    private Integer getWeek(Date curDate) {
        int week = 0;
        if (curDate.after(TERM_REVIEW_START_DATE) && curDate.before(TERM_REVIEW_END_DATE)) {
            week = (int) DateUtils.dayDiff(curDate, TERM_REVIEW_START_DATE) / 7;
            week = week + 1;
        }
        return week;
    }

    /**
     * 点击领取园丁豆
     *
     * @return
     */
    @RequestMapping(value = "/scholarship/apply_yuanDingDou.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage applyYuandingdou() {
        MapMessage resultMsg = new MapMessage();
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "需要登陆才能操作!");

            boolean authResult = teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState();
            resultMsg.add("authState", authResult);
            Validate.isTrue(authResult, "不是认证老师，不能参加!");

            boolean data = TeacherScholarshipRecordCache.getApplyYuandingdouCache(teacher.getId());
            if (data) {
                return resultMsg.setSuccess(true).setInfo("已经领取!");
            }

            IntegralHistory integralHistory = new IntegralHistory(teacher.getId(), IntegralType.TEACHER_SCHOLARSHIP_ACTIVITY_REWARD, 10);
            integralHistory.setComment("布置复习作业活动");
            MapMessage incIntegralMsg = userIntegralService.changeIntegral(integralHistory);
            if (!incIntegralMsg.isSuccess()) {
                return MapMessage.errorMessage("领取失败!增加园丁豆失败!" + incIntegralMsg.getInfo());
            }
            TeacherScholarshipRecordCache.setApplyYuandingdouCache(teacher.getId());

            return resultMsg.setSuccess(true).setInfo("领取成功!");
        } catch (Exception e) {
            resultMsg.setSuccess(false).setInfo(e.getMessage());
            return resultMsg;
        }
    }

    /**
     * 每日“期末教师先锋奖”
     *
     * @return
     */
    @RequestMapping(value = "/scholarship/apply_dayTermReview.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage applyDayTermReview() {
        MapMessage resultMsg = new MapMessage();
        try {
            //每天点击，缓存保持一天有效期
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "需要登陆才能操作!");

            boolean authResult = teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState();
            if (!authResult) {
                return resultMsg.setSuccess(true).set("code", 2).setInfo("你还不是认证老师");
            }

            //考虑包班制的情况
            List<Long> allTeacherIds = new ArrayList<>();
            allTeacherIds.add(teacher.getId());
            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacher.getId());
            allTeacherIds.addAll(subTeacherIds);

            BinaryOperator<TeacherScholarshipRecord> reduce = (acc, item) -> {
                if (acc == null)
                    return item;
                acc.addTermReviewNum(item.getTermReviewNum());
                acc.addBasicReviewNum(item.getBasicReviewNum());
                return acc;
            };

            TeacherScholarshipRecord record = allTeacherIds.stream()
                    .map(id -> teacherActivityServiceClient.loadTeacherScholarshipRecord(id))
                    .filter(Objects::nonNull)
                    .reduce(reduce)
                    .orElse(null);

            if (Objects.isNull(record) || record.getBasicReviewNum() < 1) {
                return resultMsg.setSuccess(true).set("code", 3).setInfo("尚未布置基础必过");
            }
            //当前是否布置了期末复习,需要循环判断，因为是判断是否是当天的日期
            List<TeacherScholarshipRecord> shipRecords = new ArrayList<>();
            for (Long tempId : allTeacherIds) {
                TeacherScholarshipRecord tmp = teacherActivityServiceClient.loadTeacherScholarshipRecord(tempId);
                if (Objects.nonNull(tmp)) {
                    shipRecords.add(tmp);
                }
            }

            boolean assignDayTermReview = false;
            List<TeacherAssignTermReviewRecord> records = TeacherScholarshipRecordCache.loadRecords(allTeacherIds);
            //判断今日是否布置过期末复习,从缓存中去判断,如果有当天的记录证明当天布置过
            String currDate = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
            records = records.stream().filter(o -> Objects.equals(currDate, DateUtils.dateToString(o.getAssignDate(), DateUtils.FORMAT_SQL_DATE))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(records)) {
                assignDayTermReview = true;
            }

            if (assignDayTermReview) {
                TeacherScholarshipRecordCache.setApplyDayTermReviewCache(teacher.getId());
                //更新数据库的标志,考虑包班制，副账号也置上状态
                for (TeacherScholarshipRecord record1 : shipRecords) {
                    teacherActivityServiceClient.applyDailyScholarship(record1.getTeacherId());
                }
                return resultMsg.setSuccess(true).add("code", 0).setInfo("申领成功，已进入评奖名单");
            } else {
                if (record.getBasicReviewNum() < 1) {
                    return resultMsg.setSuccess(true).set("code", 5).setInfo("尚未布置期末复习和基础必过");
                }
                return resultMsg.setSuccess(true).set("code", 4).setInfo("今日尚未布置期末复习");
            }
        } catch (Exception e) {
            resultMsg.setSuccess(false).setInfo(e.getMessage());
            return resultMsg;
        }
    }

    /**
     * 每周“期末先锋班级奖”
     *
     * @return
     */
    @RequestMapping(value = "/scholarship/apply_weekTermReview.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage applyWeekTermReview() {
        MapMessage resultMsg = new MapMessage();
        try {
            //每天点击，缓存保持一天有效期
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "需要登陆才能操作!");

            boolean authResult = teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState();
            if (!authResult) {
                return resultMsg.setSuccess(true).set("code", 2).setInfo("你还不是认证老师");
            }
            //完成基础必过，当天不布置过期末复习作业
            //考虑包班制的情况
            List<Long> allTeacherIds = new ArrayList<>();
            allTeacherIds.add(teacher.getId());
            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacher.getId());
            allTeacherIds.addAll(subTeacherIds);

            BinaryOperator<TeacherScholarshipRecord> reduce = (acc, item) -> {
                if (acc == null)
                    return item;
                acc.addTermReviewNum(item.getTermReviewNum());
                acc.addBasicReviewNum(item.getBasicReviewNum());
                return acc;
            };

            TeacherScholarshipRecord record = allTeacherIds.stream()
                    .map(id -> teacherActivityServiceClient.loadTeacherScholarshipRecord(id))
                    .filter(Objects::nonNull)
                    .reduce(reduce)
                    .orElse(null);

            if (Objects.isNull(record) || record.getBasicReviewNum() < 1) {
                return resultMsg.setSuccess(true).set("code", 3).setInfo("尚未布置基础必过");
            }
            //本周是否布置了两次期末复习
            Integer curWeek = getWeek(new Date());
            List<TeacherAssignTermReviewRecord> records = TeacherScholarshipRecordCache.loadRecords(allTeacherIds);
            records = records.stream().filter(o -> Objects.equals(curWeek, o.getWeek())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(records)) {
                Integer termReviews = records.size();
                if (termReviews == 1) {
                    return resultMsg.setSuccess(true).set("code", 5).setInfo("本周还需布置一次期末复习");
                }
            } else {
                return resultMsg.setSuccess(true).set("code", 4).setInfo("本周尚未布置期末复习");
            }
            TeacherScholarshipRecordCache.setApplyWeekTermReviewCache(teacher.getId());
            TeacherScholarshipRecordCache.incrApplyWeekTermReviewTeacher(curWeek);
            //更新数据库的标志
            Set<Long> teaIds = records.stream().map(TeacherAssignTermReviewRecord::getTeacherId).collect(Collectors.toSet());
            for (Long teaId : teaIds) {
                teacherActivityServiceClient.applyWeekScholarShip(teaId);
            }
            return resultMsg.setSuccess(true).set("code", 0).setInfo("申领成功，已进入评奖名单");
        } catch (Exception e) {
            resultMsg.setSuccess(false).setInfo(e.getMessage());
            return resultMsg;
        }
    }

    /**
     * “期末先锋班级奖”
     *
     * @return
     */
    @RequestMapping(value = "/scholarship/apply_FinalTermReview.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage applyFinalTermReview() {
        MapMessage resultMsg = new MapMessage();
        try {
            //每天点击，缓存保持一天有效期
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "需要登陆才能操作!");

            boolean authResult = teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState();
            if (!authResult) {
                return resultMsg.setSuccess(true).set("code", 2).setInfo("你还不是认证老师");
            }
            //完成基础必过，当天不布置过期末复习作业
            //考虑包班制的情况
            List<Long> allTeacherIds = new ArrayList<>();
            allTeacherIds.add(teacher.getId());
            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacher.getId());
            allTeacherIds.addAll(subTeacherIds);

            BinaryOperator<TeacherScholarshipRecord> reduce = (acc, item) -> {
                if (acc == null)
                    return item;
                acc.addTermReviewNum(item.getTermReviewNum());
                acc.addBasicReviewNum(item.getBasicReviewNum());
                return acc;
            };

            TeacherScholarshipRecord record = allTeacherIds.stream()
                    .map(id -> teacherActivityServiceClient.loadTeacherScholarshipRecord(id))
                    .filter(Objects::nonNull)
                    .reduce(reduce)
                    .orElse(null);
            if (Objects.isNull(record) || record.getBasicReviewNum() < 1) {
                return resultMsg.setSuccess(true).set("code", 3).setInfo("尚未布置基础必过");
            }
            //累计布置了3次作业
            Integer curWeek = getWeek(new Date());
            List<TeacherAssignTermReviewRecord> records = TeacherScholarshipRecordCache.loadRecords(allTeacherIds);
            //过滤掉第0周的数据
            records = records.stream().filter(o -> !Objects.equals(o.getWeek(), 0)).collect(Collectors.toList());
            //累计布置了多少周的作业
            if (CollectionUtils.isNotEmpty(records)) {
                //布置了多少周
                Integer weeks = records.stream().collect(Collectors.groupingBy(TeacherAssignTermReviewRecord::getWeek)).size();
                //本周是否布置
                records = records.stream().filter(o -> Objects.equals(curWeek, o.getWeek())).collect(Collectors.toList());
                boolean curWeekTermReview = false;//本周是否布置作业标志
                if (CollectionUtils.isNotEmpty(records)) {
                    curWeekTermReview = true;
                }
                if (weeks.intValue() < 3) {
                    if (curWeekTermReview) {
                        return resultMsg.setSuccess(true).set("code", 4).setInfo("还需再布置" + (3 - weeks) + "周期末复习");
                    } else {
                        return resultMsg.setSuccess(true).set("code", 5).setInfo("需要累计3周布置过期末复习");
                    }
                }
            } else {
                return resultMsg.setSuccess(true).set("code", 5).setInfo("需要累计3周布置过期末复习");
            }
            TeacherScholarshipRecordCache.setApplyMonthTermReviewCache(teacher.getId());
            TeacherScholarshipRecordCache.incrApplyMonthTermReviewTeacher();
            //更新数据库的标志
            Set<Long> teaIds = records.stream().map(TeacherAssignTermReviewRecord::getTeacherId).collect(Collectors.toSet());
            for (Long teaId : teaIds) {
                teacherActivityServiceClient.applyFinalScholarShip(teaId);
            }
            return resultMsg.setSuccess(true).set("code", 0).setInfo("申领成功，已进入评奖名单");
        } catch (Exception e) {
            resultMsg.setSuccess(false).setInfo(e.getMessage());
            return resultMsg;
        }
    }

    /**
     * 获得每日榜单,点击查看更多的时候获取此信息
     *
     * @return
     */
    @RequestMapping(value = "/scholarship/loadDailyList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadDailyList() {
        MapMessage resultMsg = new MapMessage();
        List<Map<String, Object>> dailyList = TeacherScholarshipRecordCache.loadDayScholarshipTeachers();
        if (CollectionUtils.isNotEmpty(dailyList)) {
            resultMsg.setSuccess(true);
            resultMsg.add("dailyList", dailyList);
        } else {
            resultMsg.setSuccess(false);
            resultMsg.setInfo("暂无数据");
        }
        return resultMsg;
    }

    /**
     * ---------------------------------2017-2018下学期期末奖学金活动-------------------------------------------------
     *
     * @return
     */

//    @RequestMapping(value = "/scholarship/detail.vpage")
//    @ResponseBody
//    public MapMessage scholarShipDetailPage() {
//        try {
//            MapMessage resultMsg = MapMessage.successMessage();
//
//            Teacher teacher = currentTeacher();
//            Validate.notNull(teacher, "需要登陆才能操作!");
//
//            Subject subject = teacher.getSubject();
//            resultMsg.add("subject", subject == null ? Subject.UNKNOWN.name() : subject.name());
//
//            // 考虑包班制的情况
//            List<Long> allTeacherIds = new ArrayList<>();
//            allTeacherIds.add(teacher.getId());
//
//            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacher.getId());
//            allTeacherIds.addAll(subTeacherIds);
//
//            // 把布置作业的次数给汇总
//            BinaryOperator<TeacherScholarshipRecord> reduce = (acc, item) -> {
//                if (acc == null)
//                    return item;
//
//                acc.addTermReviewNum(item.getTermReviewNum());
//                acc.addBasicReviewNum(item.getBasicReviewNum());
//                return acc;
//            };
//
//            TeacherScholarshipRecord record = allTeacherIds.stream()
//                    .map(id -> teacherActivityServiceClient.loadTeacherScholarshipRecord(id))
//                    .filter(Objects::nonNull)
//                    .reduce(reduce)
//                    .orElse(null);
//            resultMsg.add("detail", record);
//
//            List<Map<String, Object>> dailyList = teacherActivityServiceClient.loadScholarshipDailyList();
//            // 拼接图片的前缀
//            dailyList.forEach(d -> {
//                String imgUrlPart = MapUtils.getString(d, "img");
//                d.put("img", getUserAvatarImgUrl(imgUrlPart));
//            });
//
//            resultMsg.add("dailyList", dailyList);
//
//            Map<String, Object> dailyWinner = dailyList.stream().findFirst().orElse(null);
//            resultMsg.add("dailyWinner", dailyWinner);
//
//            // 总决赛参与人次
//            resultMsg.add("finalAttendNum", teacherActivityServiceClient.loadScholarshipFinalAttendNum());
//            return resultMsg;
//        } catch (Exception e) {
//            return MapMessage.errorMessage(e.getMessage());
//        }
//    }
    @RequestMapping(value = "/scholarship/apply_daily.vpage")
    @ResponseBody
    public MapMessage applyDailyScholarshipActivity() {
        MapMessage resultMsg = new MapMessage();

        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "需要登陆才能操作!");

            boolean authResult = teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState();
            resultMsg.add("authState", authResult);
            Validate.isTrue(authResult, "不是认证老师，不能参加!");

            return teacherActivityServiceClient.applyDailyScholarshipEntrance(teacher.getId());
        } catch (Exception e) {
            resultMsg.setSuccess(false).setInfo(e.getMessage());
            return resultMsg;
        }
    }

    @RequestMapping(value = "/scholarship/apply_final.vpage")
    @ResponseBody
    public MapMessage applyFinalScholarship() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "需要登陆才能操作!");

            return teacherActivityServiceClient.applyFinalScholarshipEntrance(teacher.getId());
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * --------------------------2017 寒假抽奖 活动-----------------------------------------------------
     */
    @RequestMapping(value = "/summer/2018/lottery.vpage", method = RequestMethod.GET)
    public String teacherAwardSummer() {
        return "project/teacherawardsummer/index";
    }

    @RequestMapping(value = "/summer/2018/detail.vpage")
    @ResponseBody
    public MapMessage lotteryDetail() {
        try {
            MapMessage resultMsg = MapMessage.successMessage();

            Teacher teacher = currentTeacher();
            org.jsoup.helper.Validate.notNull(teacher, "需要登陆才能操作!");

            Subject subject = teacher.getSubject();
            resultMsg.add("subject", subject == null ? Subject.UNKNOWN.name() : subject.name());

            int assignTime = 0;
            TeacherVocationLottery lotteryRecord = tchLotteryServiceCli.loadTeacherVocationLottery(teacher.getId());
            if (lotteryRecord != null) {
                assignTime = SafeConverter.toInt(lotteryRecord.getAssignTime());
            }

            resultMsg.add("assigned", assignTime > 0);
            CampaignType type = CampaignType.SUMMER_VOCATION_LOTTERY_2018;

            resultMsg.add("bigAward", campaignLoaderClient.loadCampaignLotteryResultBigForTime(type.getId()));

            int freeChance = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(type, teacher.getId());
            resultMsg.add("drawTime", freeChance);

            // 认证状态
            resultMsg.add("authState", teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState());

            return resultMsg;
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/summer/2018/draw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage drawWinter2017() {
        try {
            Teacher teacher = currentTeacher();
            org.jsoup.helper.Validate.notNull(teacher, "需要登陆才能操作!");

            CampaignType ct = CampaignType.SUMMER_VOCATION_LOTTERY_2018;
            Date startTime = DateUtils.stringToDate(ct.getAwardStartTime());
            Date now = new Date();

            //if(now.before(startTime))
            //    return MapMessage.errorMessage("抽奖暂未开始，请6月5日再来吧~");

            return campaignServiceClient.getCampaignService().drawLottery(ct, teacher, LotteryClientType.APP);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/summer/2018/history.vpage")
    @ResponseBody
    public MapMessage winter2017History() {
        try {
            MapMessage resultMsg = MapMessage.successMessage();

            Teacher teacher = currentTeacher();
            org.jsoup.helper.Validate.notNull(teacher, "需要登陆才能操作!");

            int campaignId = CampaignType.SUMMER_VOCATION_LOTTERY_2018.getId();
            List<CampaignLotteryHistory> historyEntries = campaignLoaderClient.findCampaignLotteryHistories(campaignId, teacher.getId());

            Map<Integer, CampaignLottery> campaignLotteryMap = campaignLoaderClient.getCampaignLoader()
                    .findCampaignLotteries(campaignId)
                    .stream()
                    .collect(Collectors.toMap(cl -> cl.getAwardId(), cl -> cl));

            AtomicInteger drawNo = new AtomicInteger(historyEntries.size());
            List<Map<String, Object>> mapper = historyEntries.stream()
                    .sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()))
                    .map(he -> {
                        CampaignLottery cl = campaignLotteryMap.get(he.getAwardId());
                        if (cl == null) return null;

                        Map<String, Object> m = new HashMap<>();
                        String timeStr = DateUtils.dateToString(he.getCreateDatetime(), "MM月dd日 HH:mm");

                        m.put("no", "第" + drawNo.getAndDecrement() + "次");
                        m.put("time", timeStr);
                        m.put("award", cl.getAwardName());

                        return m;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return resultMsg.add("history", mapper);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/summer/2018/onekey_assign.vpage")
    @ResponseBody
    public MapMessage oneKeyAssign() {
        try {
            Teacher teacher = currentTeacher();
            org.jsoup.helper.Validate.notNull(teacher, "需要登陆才能操作!");

            List<Teacher> allTeachers = teacherLoaderClient.loadRelTeacherIds(teacher.getId())
                    .stream()
                    .map(tId -> teacherLoaderClient.loadTeacher(tId))
                    .collect(Collectors.toList());

            boolean successOnce = false;
            MapMessage errMsg = null;
            for (Teacher t : allTeachers) {
                MapMessage m = vocationHomeworkServiceClient.autoAssign(t);
                if (!m.isSuccess()) {
                    errMsg = m;
                } else
                    successOnce = true;
            }

            // 全失败，才弹窗，但凡成功一次，都算成功...
            if (errMsg != null && !successOnce)
                return errMsg;

            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

}
