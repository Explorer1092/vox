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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.business.api.constant.IActivityLevel;
import com.voxlearning.utopia.business.api.constant.LevelOfSchool;
import com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory;
import com.voxlearning.utopia.entity.activity.TeacherActivityProgress;
import com.voxlearning.utopia.entity.activity.TeacherNewTermActivityProgress;
import com.voxlearning.utopia.entity.activity.tuckerhomework.ClazzTuckerHomeworkInfo;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerActivityRecord;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerWeeklyHomeworkReport;
import com.voxlearning.utopia.service.business.constant.Teacher51ActRegion;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory.JuniorEnglishStudent;
import static com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory.JuniorEnglishStudent_TMP;

/**
 * 2017秋季开学老师端活动
 *
 * @author yuechen.wang
 * @since 2017-08-14
 */
@Controller
@RequestMapping("/teacher/activity/term2017")
public class TeacherActivityTerm2017Controller extends AbstractTeacherController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private TeacherActivityServiceClient teacherActivityServiceClient;

    private static final String[] mapping = new String[]{
            "一", "二", "三", "四", "五"
    };

    //-------------------------------------------------------------------------
    //---------------------     活动一 - 老师带新认证学生      ------------------
    //-------------------------------------------------------------------------

    private static final Date deadline = DateUtils.stringToDate("2018-04-15 23:59:59");

    private static final Map<Long, String> ftlMap = MapUtils.map(
            JuniorEnglishStudent.getId(), "activity/englishapprove",
            JuniorEnglishStudent_TMP.getId(), "activity/activityapprove",
            TeacherNewTermActivityCategory.PrimaryEnglishStudent.getId(), "activity/activityapprove",
            TeacherNewTermActivityCategory.PrimaryChineseStudent.getId(), "activity/activityapprove"
    );

    // #63920 该区域已参加活动但未获得奖励的老师，不再计入奖励计算。
    private static final List<Integer> blackRegionCode = Arrays.asList(
            230102, 230103, 230104, 230106, 230108
    );

    /**
     * 主页页面
     */
    @RequestMapping(value = "actone/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/index.vpage";
        }

        return "activity/newstudapprove";
    }

    /**
     * 主页页面
     */
    @RequestMapping(value = "actone/index_activity.vpage", method = RequestMethod.GET)
    public String indexWithActivity(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/index.vpage";
        }
        return ftlMap.getOrDefault(getRequestLong("activityId"), "redirect:/index.vpage");
    }

    /**
     * 初始化
     */
    @RequestMapping(value = "actone/init.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage init() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        Long schoolId = teacher.getTeacherSchoolId();
        LevelOfSchool schoolLevel = teacherActivityServiceClient.getRemoteReference().getSchoolLevel(schoolId);
        if (schoolLevel == null) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }
        // 查看老师有没有参与活动
        TeacherActivityProgress progress = teacherActivityServiceClient.getRemoteReference().findTeacherProgress(teacher.getId());
        return MapMessage.successMessage()
                .add("participate", progress != null) // 是否已经参与
                .add("schoolLevel", schoolLevel.getLevel())   // 学校等级 A/B/C/D
                .add("students", progress == null ? 0 : progress.getAuthStuCnt()) // 学生人数
                .add("rank", progress == null ? 0 : progress.getRank()) // 我的排名
                .add("participateDate", progress == null ? null : DateUtils.dateToString(progress.getCreateTime(), "yyyy-MM-dd HH:mm")) // 我的参与时间
                .add("auth", AuthenticationState.SUCCESS == AuthenticationState.safeParse(teacher.getAuthenticationState())) // 认证状态
                ;
    }

    /**
     * 初始化
     */
    @RequestMapping(value = {"actone/init_activity.vpage"}, method = RequestMethod.GET)
    @ResponseBody
    public MapMessage initWithActivity() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long activityId = getRequestLong("activityId");
        TeacherNewTermActivityCategory activity = TeacherNewTermActivityCategory.parse(activityId);
        // 老师匹配活动咯
        if (activity == null || !activity.match(teacher.getKtwelve(), teacher.getSubjects())) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }
        IActivityLevel level;
        // 这个是按城市判断等级的
        if (JuniorEnglishStudent.getId().equals(activityId) || JuniorEnglishStudent_TMP.equals(activity)) {
            level = teacherActivityServiceClient.getRemoteReference().getCityLevelNew(teacher.getCityCode(), activityId);
        } else {
            level = teacherActivityServiceClient.getRemoteReference().loadActivitySchoolLevel(activityId, teacher.getTeacherSchoolId());
        }
        if (level == null) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }

        // 查看老师有没有参与活动
        TeacherNewTermActivityProgress progress = teacherActivityServiceClient.getRemoteReference().findTeacherActivityProgress(activityId, teacher.getId());

        return MapMessage.successMessage()
                .add("participate", progress != null) // 是否已经参与
                .add("level", level.fetchLevel())
                .add("students", progress == null ? 0 : progress.getAuthStuCnt()) // 学生人数
                .add("rank", progress == null ? 0 : progress.getRank()) // 我的排名
                .add("participateDate", progress == null ? null : DateUtils.dateToString(progress.getCreateTime(), "yyyy-MM-dd HH:mm")) // 我的参与时间
                .add("auth", AuthenticationState.SUCCESS == AuthenticationState.safeParse(teacher.getAuthenticationState())) // 认证状态
                ;
    }

    /**
     * 排行榜
     */
    @RequestMapping(value = "actone/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rank() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        List<TeacherActivityProgress> top100 = teacherActivityServiceClient.getRemoteReference().loadRankTop100();
        List<Map<String, Object>> rankList = new LinkedList<>();
        for (TeacherActivityProgress progress : top100) {
            TeacherDetail participant = teacherLoaderClient.loadTeacherDetail(progress.getId());
            Map<String, Object> info = new HashMap<>();
            info.put("rank", progress.getRank());
            info.put("schoolName", participant != null ? participant.getTeacherSchoolName() : null);
            info.put("regionName", participant != null ? participant.getRootRegionName() : null);
            info.put("teacherName", participant != null ? participant.respectfulName() : null);
            info.put("studentCount", progress.getAuthStuCnt());
            info.put("participateDate", DateUtils.dateToString(progress.getCreateTime(), "MM-dd HH:mm"));
            rankList.add(info);
        }
        return MapMessage.successMessage().add("rankList", rankList);
    }

    /**
     * 排行榜
     */
    @RequestMapping(value = "actone/rank_activity.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rankWithActivity() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long activityId = getRequestLong("activityId");
        TeacherNewTermActivityCategory activity = TeacherNewTermActivityCategory.parse(activityId);
        // 老师匹配活动咯
        if (activity == null || !activity.match(teacher.getKtwelve(), teacher.getSubjects())) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }
        List<TeacherNewTermActivityProgress> top100 = teacherActivityServiceClient.getRemoteReference().loadRankTop100ByActivity(activityId);
        List<Map<String, Object>> rankList = new LinkedList<>();
        for (TeacherNewTermActivityProgress progress : top100) {
            TeacherDetail participant = teacherLoaderClient.loadTeacherDetail(progress.getTeacherId());
            Map<String, Object> info = new HashMap<>();
            info.put("rank", progress.getRank());
            info.put("schoolName", participant != null ? participant.getTeacherSchoolName() : null);
            info.put("regionName", participant != null ? participant.getRootRegionName() : null);
            info.put("teacherName", participant != null ? participant.respectfulName() : null);
            info.put("studentCount", progress.getAuthStuCnt());
            info.put("participateDate", DateUtils.dateToString(progress.getCreateTime(), "MM-dd HH:mm"));
            rankList.add(info);
        }
        return MapMessage.successMessage().add("rankList", rankList);
    }

    /**
     * 参与活动
     */
    @RequestMapping(value = "actone/participate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage participate() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (deadline.before(new Date())) {
            return MapMessage.errorMessage("活动已经结束");
        }
        // 先查看是否符合小学数学，包括包班副账号是数学老师
        Long mathTeacher = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), Subject.MATH);
        if (mathTeacher == null) {
            return MapMessage.errorMessage("该活动暂时限数学老师参加，请您关注其他活动哦~");
        }
        Long schoolId = teacher.getTeacherSchoolId();
        LevelOfSchool level = teacherActivityServiceClient.getRemoteReference().getSchoolLevel(schoolId);
        if (level == null) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }

        // 查看老师有没有参与活动
        TeacherActivityProgress progress = teacherActivityServiceClient.getRemoteReference().findTeacherProgress(teacher.getId());
        if (progress != null) {
            return MapMessage.successMessage();
        }
        // 参与活动
        return teacherActivityServiceClient.participate(schoolId, teacher.getId());
    }

    /**
     * 参与活动
     */
    @RequestMapping(value = {"actone/participate_activity.vpage"}, method = RequestMethod.POST)
    @ResponseBody
    public MapMessage participateWithActivity() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long activityId = getRequestLong("activityId");
        TeacherNewTermActivityCategory activity = TeacherNewTermActivityCategory.parse(activityId);
        // 老师匹配活动咯
        if (activity == null || !activity.match(teacher.getKtwelve(), teacher.getSubjects())) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }
        if (activity.expire(new Date())) {
            return MapMessage.errorMessage("活动已经结束");
        }
        IActivityLevel level;
        // 这个是按城市判断等级的
        if (JuniorEnglishStudent.getId().equals(activityId) || JuniorEnglishStudent_TMP.getId().equals(activityId)) {
            if (blackRegionCode.contains(teacher.getRegionCode())) {
                return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
            }
            level = teacherActivityServiceClient.getRemoteReference().getCityLevelNew(teacher.getCityCode(),activityId);
        } else {
            level = teacherActivityServiceClient.getRemoteReference().loadActivitySchoolLevel(activityId, teacher.getTeacherSchoolId());
        }
        if (level == null) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }
        // 先查看是否符合小学数学，包括包班副账号是数学老师
        Long subTeacher = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), activity.getSubject());
        if (subTeacher == null) {
            return MapMessage.errorMessage("该活动暂时限数学老师参加，请您关注其他活动哦~");
        }

        // 查看老师有没有参与活动
        TeacherNewTermActivityProgress progress = teacherActivityServiceClient.getRemoteReference().findTeacherActivityProgress(activityId, teacher.getId());
        if (progress != null) {
            return MapMessage.successMessage();
        }
        // 参与活动
        return teacherActivityServiceClient.participateActivity(activityId, teacher.getTeacherSchoolId(), teacher.getId());
    }

    //-------------------------------------------------------------------------
    //-------------------     活动二 - 数学老师每周作业任务      -----------------
    //-------------------------------------------------------------------------

    /**
     * 主页页面
     */
    @RequestMapping(value = "acttwo/index.vpage", method = RequestMethod.GET)
    public String index2(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/index.vpage";
        }
        return "activity/weektask";
    }

    /**
     * 初始化
     */
    @RequestMapping(value = "acttwo/init.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage init2() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        Long mathTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), Subject.MATH);
        if (mathTeacherId == null) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }

        // 根据地区来判断奖励的话费金额
        int awardMoney = 0;
        for(Teacher51ActRegion region:Teacher51ActRegion.values()){
            Set<Integer> regionList = raikouSystem.getRegionBuffer().findByTag(region.name());
            if(regionList.contains(teacher.getRegionCode())){
                awardMoney = region.getAwardMoney();
                break;
            }
        }

        if(awardMoney <= 0)
            return MapMessage.errorMessage("抱歉，您所在的城市暂不在活动范围内，请您参加其他活动哦～");

        // 查看老师有没有参与活动
        TuckerActivityRecord record = teacherActivityServiceClient.getRemoteReference().findTuckerActivityRecord(teacher.getId());
        TuckerWeeklyHomeworkReport report = Optional.ofNullable(teacherActivityServiceClient.getRemoteReference()
                .findTuckerWeeklyHomeworkReport(teacher.getId()))
                .orElseGet(() -> {
                    TuckerWeeklyHomeworkReport newReport = new TuckerWeeklyHomeworkReport();
                    newReport.setClassHomeworkInfo(Collections.emptyList());
                    return newReport;
                });

        List<Map<String,Object>> hwInfoList = new ArrayList<>();
        for(ClazzTuckerHomeworkInfo cthInfo : report.getClassHomeworkInfo()){
            cthInfo.getHomeworkList()
                    .stream()
                    .map(hw -> MapUtils.m(
                            "clazzName",cthInfo.getClazzName(),
                            "accomplishCount",hw.getAccomplishCount(),
                            "timeStr",DateUtils.dateToString(hw.getAssignTime()),
                            "time", hw.getAssignTime().getTime()))
                    .forEach(hwInfoList::add);
        }

        // 按照时间由近及远
        hwInfoList.sort((hw1,hw2) -> {
            Long time1 = MapUtils.getLong(hw1,"time");
            Long time2 = MapUtils.getLong(hw2,"time");
            return Long.compare(time2,time1);
        });

        return MapMessage.successMessage()
                .add("participate", record != null) // 是否已经参与
                .add("participateDate", record == null ? null : DateUtils.dateToString(record.getCreateTime(), "yyyy-MM-dd HH:mm"))
                .add("progress", Math.min(report.getAccomplishNum(),3)) // 进度，最多点亮3个
                .add("groupHomeworkList", hwInfoList)
                .add("auth", AuthenticationState.SUCCESS == AuthenticationState.safeParse(teacher.getAuthenticationState())) // 认证状态
                .add("awardMoney", awardMoney)
                ;
    }

    /**
     * 参与活动
     */
    @RequestMapping(value = "acttwo/participate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage participate2() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        Date now = new Date();
        DateRange range = TuckerActivityRecord.ActivityRange;
        if (range.getStartDate().after(now)) {
            return MapMessage.errorMessage("活动尚未开始，敬请期待");
        }
        if (range.getEndDate().before(now)) {
            return MapMessage.errorMessage("您来晚了，活动已经结束，请您关注其他活动吧～");
        }
        // 先查看是否符合小学数学老师，包括包班副账号是数学的老师
        Long mathTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), Subject.MATH);
        if (mathTeacherId == null) {
            return MapMessage.errorMessage("该活动暂时限数学老师参加，请您关注其他活动哦~");
        }

        boolean satisfy = false;
        for(Teacher51ActRegion region:Teacher51ActRegion.values()){
            Set<Integer> regionList = raikouSystem.getRegionBuffer().findByTag(region.name());
            if(regionList.contains(teacher.getRegionCode())){
                satisfy = true;
                break;
            }
        }

        if(!satisfy){
            return MapMessage.errorMessage("抱歉，您所在的城市暂不在活动范围内，请您参加其他活动哦～");
        }

        // 查看老师有没有参与活动
        TuckerActivityRecord record = teacherActivityServiceClient.getRemoteReference().findTuckerActivityRecord(teacher.getId());
        if (record != null) {
            return MapMessage.successMessage();
        }
        // 参与活动
        return teacherActivityServiceClient.participateTuckerActivity(teacher.getTeacherSchoolId(), teacher.getId());
    }

    /**
     * 周报告
     */
    @RequestMapping(value = "acttwo/weektask_report.vpage", method = RequestMethod.GET)
    public String weekTaskReport(Model model) {
        return "redirect:/index.vpage";
    }

    /**
     * 作业报告
     */
    @RequestMapping(value = "acttwo/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage report() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = teacher.getTeacherSchoolId();
        Boolean validSchool = teacherActivityServiceClient.getRemoteReference().checkTuckerSchool(schoolId);
        // 检查学校
        if (!Boolean.TRUE.equals(validSchool)) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }

        Long mathTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), Subject.MATH);
        if (mathTeacherId == null) {
            return MapMessage.errorMessage("该活动暂时仅限部分学校老师参与，请您关注其他活动哦~");
        }

        TuckerWeeklyHomeworkReport report = teacherActivityServiceClient.getRemoteReference().findTuckerWeeklyHomeworkReport(teacher.getId());
        if (report == null) {
            return MapMessage.errorMessage("周报告已过期");
        }
        Map<String, Object> reportInfo = new HashMap<>();
        reportInfo.put("currentTitle", report.currentTitle().getTitle());
        reportInfo.put("currentDesc", report.currentTitle().getDescription());
        reportInfo.put("levelUp", report.hasLevelUp());
        reportInfo.put("groupHomeworkList", report.getClassHomeworkInfo());
        return MapMessage.successMessage().add("report", reportInfo);
    }


}
