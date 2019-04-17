package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewVacationHomeworkHistory;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkPackageLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/teacher/vacation/report")
public class TeacherVacationHomeworkReportController extends AbstractTeacherController {

    @Inject
    private VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient;
    @Inject
    private VacationHomeworkServiceClient vacationHomeworkServiceClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private VacationHomeworkLoaderClient vacationHomeworkLoaderClient;
    @Inject
    private VacationHomeworkPackageLoaderClient vacationHomeworkPackageLoaderClient;

    /**
     * 假期作业列表跳转
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String historyIndex(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return "redirect:/teacher/index.vpage";
        }
        model.addAttribute("subject", teacher.getSubject());
        return "teacherv3/vacationreport/list";
    }


    /**
     * 老师假期作业列表
     */
    @RequestMapping(value = "vacationhistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vacationHistory() {
        MapMessage mapMessage;
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null");
        }
        List<NewVacationHomeworkHistory> newVacationHomeworkHistoryList;
        try {
            newVacationHomeworkHistoryList = vacationHomeworkReportLoaderClient.newVacationHomeworkHistory(teacher);
        } catch (Exception e) {
            logger.error("fetch vacation history failed : tid of {}", teacher.getId(), e);
            return MapMessage.errorMessage("获取列表失败");
        }

        mapMessage = MapMessage.successMessage();
        mapMessage.add("newVacationHomeworkHistoryList", newVacationHomeworkHistoryList);
        return mapMessage;
    }

    /**
     * 假期作业班级报告详情跳转
     */
    @RequestMapping(value = "clazzreport.vpage", method = RequestMethod.GET)
    public String clazzReport(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return "redirect:/teacher/index.vpage";
        }
        String packageId = getRequestString("packageId");
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageLoaderClient.loadVacationHomeworkPackageById(packageId);
        if (vacationHomeworkPackage == null) {
            logger.error("vacationHomeworkPackage is null");
            return "redirect:/teacher/index.vpage";
        }
        //不属于该老师的假期作业
        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), vacationHomeworkPackage.getClazzGroupId())) {
            return "redirect:/teacher/index.vpage";
        }

        if (vacationHomeworkPackage.isDisabledTrue()) {
            logger.error("vacationHomeworkPackage is deleted");
            return "redirect:/teacher/index.vpage";
        }

        //不属于该老师的假期作业
        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), vacationHomeworkPackage.getClazzGroupId())) {
            return "redirect:/teacher/index.vpage";
        }

        model.addAttribute("subject", vacationHomeworkPackage.getSubject());
        return "teacherv3/vacationreport/clazzreport";
    }

    /**
     * 某个学生假期作业每周详情跳转
     */
    @RequestMapping(value = "studentweekdetail.vpage", method = RequestMethod.GET)
    public String studentWeekDetail(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return "redirect:/teacher/index.vpage";
        }
        String packageId = this.getRequestString("packageId");
        if (StringUtils.isBlank(packageId)) {
            logger.error("package is blank");
            return "redirect:/teacher/index.vpage";
        }
        Long studentId = this.getRequestLong("studentId");
        if (studentId == 0L) {
            logger.error("studentId is 0L");
            return "redirect:/teacher/index.vpage";
        }
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageLoaderClient.loadVacationHomeworkPackageById(packageId);
        if (vacationHomeworkPackage == null) {
            logger.error("vacationHomeworkPackage is null");
            return "redirect:/teacher/index.vpage";
        }
        //不属于该老师的假期作业
        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), vacationHomeworkPackage.getClazzGroupId())) {
            return "redirect:/teacher/index.vpage";
        }
        model.addAttribute("subject", vacationHomeworkPackage.getSubject());
        if (vacationHomeworkPackage.isDisabledTrue()) {
            logger.error("vacationHomeworkPackage is deleted");
            return "redirect:/teacher/index.vpage";
        }
        //不属于该老师的假期作业
        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), vacationHomeworkPackage.getClazzGroupId())) {
            return "redirect:/teacher/index.vpage";
        }
        return "teacherv3/vacationreport/studentweekdetail";
    }

    /**
     * 奖励积分
     */
    @RequestMapping(value = "rewardintegral.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vacationHomeworkCommentRewardIntegral() {
        String homeworkId = this.getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            logger.error("homeworkId is blank");
            return MapMessage.errorMessage("homeworkId is blank").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        int rewardIntegral = this.getRequestInt("rewardIntegral");
        Teacher teacher = this.currentTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        try {
            return vacationHomeworkServiceClient.vacationHomeworkCommentRewardIntegral(teacherDetail, homeworkId, rewardIntegral);
        } catch (Exception e) {
            logger.error("vacation rewardintegral failed : hid of {} ,tid of {},rewardIntegral of {}", homeworkId, teacher.getId(), rewardIntegral, e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 评语接口
     */
    @RequestMapping(value = "comment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vacationHomeworkComment() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String homeworkId = this.getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            logger.error("homeworkId is blank");
            return MapMessage.errorMessage("homeworkId is blank").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String comment = this.getRequestString("comment");
        if (StringUtils.isBlank(comment)) {
            logger.error("comment is blank");
            return MapMessage.errorMessage("comment is blank").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        return vacationHomeworkServiceClient.vacationHomeworkComment(homeworkId, comment, null);
    }

    /**
     * 某个学生假期作业每天详情跳转
     */
    @RequestMapping(value = "studentdaydetail.vpage", method = RequestMethod.GET)
    public String studentDayDetail(Model model) {
        String homeworkId = this.getRequestString("homeworkId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return "redirect:/teacher/index.vpage";
        }
        VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkIncludeDisabled(homeworkId);
        if (vacationHomework == null) {
            logger.error("vacationHomework is null");
            return "redirect:/teacher/index.vpage";
        }
        if (vacationHomework.isDisabledTrue()) {
            logger.error("vacationHomework is disabledTrue()");
            return "redirect:/teacher/index.vpage";
        }

        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), vacationHomework.getClazzGroupId())) {
            return "redirect:/teacher/index.vpage";
        }

        if (StringUtils.isBlank(homeworkId)) {
            logger.error("homeworkId is blank");
            return "redirect:/teacher/index.vpage";
        }
        model.addAttribute("homeworkId", homeworkId);
        model.addAttribute("subject", teacher.getSubject());
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        model.addAttribute("examPcUrl", UrlUtils.buildUrlQuery("/teacher/vacation/report/detail.vpage", MapUtils.m("homeworkId", homeworkId)));
        return "teacherv3/vacationreport/studentdaydetail";
    }

    /**
     * 一份假期作业每个包信息展示
     */
    @RequestMapping(value = "packagereport.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage packageReport() {
        String packageId = this.getRequestString("packageId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (StringUtils.isBlank(packageId)) {
            logger.error("packageId is blank");
            return MapMessage.errorMessage("packageId is blank").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }

        try {
            return vacationHomeworkReportLoaderClient.packageReport(packageId, null, null, false);
        } catch (Exception e) {
            logger.error("fetch package report failed :packageId of {} ", packageId, e);
            return MapMessage.errorMessage();
        }
    }


    /**
     * 个人整份作业报告接口
     */
    @RequestMapping(value = "studentpackagereport.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage studentPackageReport() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String packageId = this.getRequestString("packageId");
        if (StringUtils.isBlank(packageId)) {
            logger.error("packageId is blank");
            return MapMessage.errorMessage("packageId is blank");
        }
        Long studentId = this.getRequestLong("studentId");
        if (studentId == 0L) {
            logger.error("studentId is 0L");
            return MapMessage.errorMessage("studentId is 0L");
        }
        try {
            return vacationHomeworkReportLoaderClient.studentPackageReport(packageId, studentId);
        } catch (Exception e) {
            logger.error("get student package report failed : packageId of {},studentId of {}", packageId, studentId, e);
            return MapMessage.errorMessage();
        }
    }


    /**
     * 假期作业学生个人的一份作业情况
     */
    @RequestMapping(value = "detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vacationReportDetailInformation() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String homeworkId = this.getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            logger.error("homeworkId is blank");
            return MapMessage.errorMessage("homeworkId is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        MapMessage mapMessage;
        try {
            mapMessage = vacationHomeworkReportLoaderClient.vacationReportDetailInformation(homeworkId);
        } catch (Exception e) {
            logger.error("fetch vacation homework info failed : id of {}", homeworkId, e);
            mapMessage = MapMessage.errorMessage();
        }


        mapMessage.putAll(MapUtils.m(
                "questionUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "type", "")),
                "completedUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "objectiveConfigType", ""))
        ));
        return mapMessage;
    }

    /**
     * 假期作业绘本类型
     */
    @RequestMapping(value = "personalreadingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadingDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String homeworkId = getRequestString("homeworkId");
        String readingId = getRequestString("readingId");
        if (StringUtils.isBlank(homeworkId) || StringUtils.isBlank(readingId)) {
            logger.error("homeworkId or readingId is blank");
            return MapMessage.errorMessage("homeworkId or readingId is null");
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.READING.name()));
        try {
            return vacationHomeworkReportLoaderClient.personalReadingDetail(homeworkId, readingId, objectiveConfigType);
        } catch (Exception e) {
            logger.error("get personal reading detail failed : hid of {},reading id of {}", homeworkId, readingId, e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 课文读背(打分)
     * @return
     */
    @RequestMapping(value = "personalreadrecitewithscore.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadReciteWithScore() {
        String hid = getRequestString("hid");
        String questionBoxId = getRequestString("questionBoxId");
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = vacationHomeworkReportLoaderClient.personalReadReciteWithScore(hid, questionBoxId, sid);
        mapMessage.putAll(
                MapUtils.m(
                        "questionUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", "")),
                        "completedUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", ""))
                ));
        return mapMessage;
    }

    /**
     * 趣味配音个人二级详情页面
     */
    @RequestMapping(value = "personaldubbingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalDubbingDetail() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请用老师帐号登录");
        }
        String homeworkId = getRequestString("homeworkId");
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }

        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id为空");
        }
        try {
            return vacationHomeworkReportLoaderClient.personalDubbingDetail(homeworkId, dubbingId);
        } catch (Exception ex) {
            logger.error("Failed to load personalDubbingDetail homeworkId:{},studentId{},dubbingId{}", homeworkId, dubbingId, ex);
            return MapMessage.errorMessage("获取趣味配音个人二级详情异常");
        }
    }


    /**
     * base_app category type details
     * 基础类型的详情
     */
    @RequestMapping(value = "detailsbaseapp.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage detailsBaseApp() {
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            logger.error("homeworkId is blank");
            return MapMessage.errorMessage("homeworkId is blank");
        }
        String categoryId = getRequestString("categoryId");
        if (StringUtils.isBlank(categoryId)) {
            logger.error("categoryId is blank");
            return MapMessage.errorMessage("categoryId is blank");
        }
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(lessonId)) {
            logger.error("lessonId is blank");
            return MapMessage.errorMessage("lessonId is blank");
        }
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            logger.error("teacher is null");
            return MapMessage.errorMessage("teacher is null");
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.BASIC_APP.name()));
        if (objectiveConfigType == null) {
            logger.error("objectiveConfigType is null");
            return MapMessage.errorMessage("objectiveConfigType is null");
        }
        try {
            return vacationHomeworkReportLoaderClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, objectiveConfigType);
        } catch (Exception e) {

            logger.error("get details from base app failed : hid of {},categoryId of {},lessonId of {},objectiveConfigType of {}", homeworkId, categoryId, lessonId, objectiveConfigType, e);
            return MapMessage.errorMessage();
        }

    }

    /**
     * 家长端发送消息
     * @return
     */
    @RequestMapping(value = "sharejztmsg.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage pushShareJztMsg() {

        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师未登入");
        }
        try {
            String packageIdsStr = getRequestString("packages");
            String[] split = StringUtils.split(packageIdsStr, ",");
            List<String> packageIds = Arrays.asList(split);
            MapMessage mapMessage = vacationHomeworkReportLoaderClient.pushShareJztMsg(packageIds, teacher);
            //Date current = new Date();
            //if (mapMessage.isSuccess() && current.after(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_ONLINE_DATE) && current.before(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_OFFLINE_DATE)) {
            //    mapMessage.add("lotteryUrl", TopLevelDomain.getHttpsMainSiteBaseUrl() + "/view/mobile/teacher/activity2018/teacheraward_summer/index");
            //}
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 分享
     * @return
     */
    @RequestMapping(value = "shareweixin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage shareReportWeiXin() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师未登入");
        }
        try {
            String packageIdsStr = getRequestString("packages");
            String[] split = StringUtils.split(packageIdsStr, ",");
            List<String> packageIds = Arrays.asList(split);
            return vacationHomeworkReportLoaderClient.shareReportWeiXin(packageIds, teacher);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 学生端发送消息
     * @return
     */
    @RequestMapping(value = "remindstudentmsg.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage remindStudentMsg() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师未登入");
        }
        try {
            String packageId = getRequestString("packageId");
            MapMessage mapMessage = vacationHomeworkReportLoaderClient.remindStudentMsg(packageId, teacher);
            //Date current = new Date();
            //if (mapMessage.isSuccess() && current.after(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_ONLINE_DATE) && current.before(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_OFFLINE_DATE)) {
            //    mapMessage.add("lotteryUrl", TopLevelDomain.getHttpsMainSiteBaseUrl() + "/view/mobile/teacher/activity2018/teacheraward_summer/index");
            //}
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }
}
