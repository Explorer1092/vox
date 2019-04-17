package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitLoader;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.MonitorRecruit;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudentJoinStatistics;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.MonitorRecruitVO;
import com.voxlearning.utopia.service.parent.constant.ApplyStatusEnum;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuerui.zhang
 * @since 2018/6/7 下午4:02
 **/
@Log4j
@Controller
@RequestMapping(value = "/parentMobile/monitor_recruit")
public class MobileParentMonitorRecruitController extends AbstractMobileParentController {

    @ImportService(interfaceClass = MonitorRecruitService.class)
    private MonitorRecruitService monitorRecruitService;
    @ImportService(interfaceClass = MonitorRecruitLoader.class)
    private MonitorRecruitLoader monitorRecruitLoader;

    /**
     * app:tab显示接口
     */
    @ResponseBody
    @RequestMapping(value = "/app/tab_show.vpage", method = RequestMethod.GET)
    public MapMessage tabShowInApp() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("student_id");
        if (0L == studentId) {
            return MapMessage.errorMessage("参数错误");
        }
        return monitorRecruitService.appTabShowInApp(parent.getId(), studentId);
    }


    /**
     * app:申请状态
     */
    @ResponseBody
    @RequestMapping(value = "/app/apply_status.vpage", method = RequestMethod.GET)
    public MapMessage applyStatusInApp() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("student_id");
        if (0L == studentId) {
            return MapMessage.errorMessage("参数错误");
        }
        return monitorRecruitService.checkApplyStatusInApp(parent.getId(), studentId);
    }

    /**
     * app:申请成为班长
     */
    @ResponseBody
    @RequestMapping(value = "/app/start_apply.vpage", method = RequestMethod.GET)
    public MapMessage startApplyInApp() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("student_id");
        if (0L == studentId) {
            return MapMessage.errorMessage("参数错误");
        }
        return monitorRecruitService.startApplyInApp(parent.getId(), studentId);
    }

    /**
     * wechat申请状态
     */
    @ResponseBody
    @RequestMapping(value = "/wechat/apply_status.vpage", method = RequestMethod.GET)
    public MapMessage applyStatus() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.successMessage(ApplyStatusEnum.NO_LOGIN.getMsg())
                    .add("status", ApplyStatusEnum.NO_LOGIN.getStatus());
        }
        return monitorRecruitService.checkApplyStatus(parent.getId());
    }

    /**
     * wechat申请成为班长
     */
    @ResponseBody
    @RequestMapping(value = "/wechat/start_apply.vpage", method = RequestMethod.GET)
    public MapMessage startApply() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode("900");
        }
        return monitorRecruitService.startApply(parent.getId());
    }

    /**
     * 添加招募信息
     */
    @ResponseBody
    @RequestMapping(value = "/add_recruit_info.vpage", method = RequestMethod.POST)
    public MapMessage addRecruitDetails() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode("900");
        }

        String birthday = getRequestString("birthday");
        String inputWechatId = getRequestString("inputWechatId");
        String province = getRequestString("province");
        String city = getRequestString("city");
        String profession = getRequestString("profession");
        String time = getRequestString("time");
        String education = getRequestString("education");
        String advantage = getRequestString("advantage");
        String character = getRequestString("character");
        String idea = getRequestString("idea");
        String lessonId = getRequestString("lessonId");
        Long studentId = getRequestLong("studentId");

        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isEmpty(students)) {
            return MapMessage.errorMessage("您还未绑定孩子");
        }
        Set<Long> sids = students.stream().map(User::getId).collect(Collectors.toSet());
        if (!sids.contains(studentId)) {
            return MapMessage.errorMessage("绑定关系错误");
        }
        MonitorRecruitVO monitorRecruit = new MonitorRecruitVO();
        monitorRecruit.setBirthday(birthday);
        monitorRecruit.setInputWechatId(inputWechatId);
        monitorRecruit.setProvince(province);
        monitorRecruit.setCity(city);
        monitorRecruit.setProfession(profession);
        monitorRecruit.setTime(time);
        monitorRecruit.setEducation(education);
        monitorRecruit.setAdvantage(advantage);
        monitorRecruit.setCharacter(character);
        monitorRecruit.setIdea(idea);
        monitorRecruit.setLessonId(lessonId);
        monitorRecruit.setStudentId(studentId);

        return AtomicLockManager.getInstance().wrapAtomic(monitorRecruitService)
                .keyPrefix("saveUploadRecord")
                .keys(studentId, lessonId)
                .proxy()
                .addRecruitDetails(monitorRecruit, parent.getId());
    }

    /**
     * wechat班级列表
     */
    @ResponseBody
    @RequestMapping(value = "/class_list.vpage", method = RequestMethod.GET)
    public MapMessage classList() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode("900");
        }
        return monitorRecruitService.getClassList(parent.getId());
    }

    /**
     * 查询是否是大班长
     */
    @ResponseBody
    @RequestMapping(value = "/is_big_monitor.vpage", method = RequestMethod.GET)
    public MapMessage isBigMonitor() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode("900");
        }

        return monitorRecruitService.isBigMonitor(parent.getId());
    }

    /**
     * 是否激活课程，是否报名课程，是否是班长
     */
    @ResponseBody
    @RequestMapping(value = "/qualificationa_auth.vpage", method = RequestMethod.GET)
    public MapMessage qualificationAuth() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode("900");
        }
        return monitorRecruitService.qualificationAuth(parent.getId());
    }

    /**
     * 辅导员更新选课信息
     */
    @ResponseBody
    @RequestMapping(value = "/update.vpage", method = RequestMethod.POST)
    public MapMessage updateMonitorRecruit() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode("900");
        }

        String lessonId = getRequestString("lesson_id");
        Long studentId = getRequestLong("student_id");
        if (null == lessonId || 0L == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        MonitorRecruit monitorRecruit = new MonitorRecruit();
        monitorRecruit.setLessonId(lessonId);
        monitorRecruit.setStudentId(studentId);

        return AtomicLockManager.getInstance().wrapAtomic(monitorRecruitService)
                .keyPrefix("updateMonitorRecruit")
                .keys(studentId, lessonId)
                .proxy()
                .updateMonitorRecruit(parent.getId(), monitorRecruit);
    }

    @RequestMapping(value = "join_lesson_statistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJoinLessonStatistics() {
        String studyGroupId = getRequestString("group_id");
        if (StringUtils.isBlank(studyGroupId)) {
            return MapMessage.errorMessage("班级ID不能为空");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Boolean isRest = Boolean.FALSE;
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            isRest = Boolean.TRUE;
        }
        List<StudentJoinStatistics> joinStatistics = monitorRecruitLoader.loadByGroupId(studyGroupId);
        List<Map<String, Object>> mapList = new ArrayList<>();
        joinStatistics.forEach(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("open_date", DateUtils.dateToString(DayRange.parse(p.getDayRange()).getStartDate(), "MM月dd日"));
            map.put("join_ratio", SafeConverter.toString(p.getStudentJoinRate()) + "%");
            map.put("total", p.getTotalClassCount());
            map.put("self_rank", p.getRank());
            mapList.add(map);
        });
        return MapMessage.successMessage().add("statistics_list", mapList).add("is_rest", isRest);
    }
}
