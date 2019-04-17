package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.newhomework.api.WeekReportLoader;
import com.voxlearning.utopia.service.newhomework.consumer.WeekReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.WeekReportServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/teacher/week/report")
public class TeacherWeekReportController extends AbstractTeacherController {

    @Inject
    private WeekReportLoaderClient weekReportLoaderClient;

    @Inject
    private WeekReportServiceClient weekReportServiceClient;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage historyIndex() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("teacher is null");
        }
        return weekReportLoaderClient.fetchWeekReportBrief(teacher);
    }


    @RequestMapping(value = "clazzinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchClazzInfo() {
        String groupIdAndReportIdStr = this.getRequestString("groupIdAndReportIds");
        String teacherIdReportEndTime = this.getRequestString("teacherIdReportEndTime");
        if (StringUtils.isBlank(teacherIdReportEndTime)) {
            return MapMessage.errorMessage("请通过首页-作业周报入口查看详情哦～");
        }
        List<String> groupIdAndReportIds = new LinkedList<>();
        if (StringUtils.isNotBlank(groupIdAndReportIdStr)) {
            String[] ss = StringUtils.split(groupIdAndReportIdStr, ",");
            Collections.addAll(groupIdAndReportIds, ss);
        } else {
            logger.warn("fetch clazz info failed: groupIdAndReportIds {},teacherIdReportEndTime{}", groupIdAndReportIdStr, teacherIdReportEndTime);
            return MapMessage.errorMessage("参数错误");
        }
        return weekReportLoaderClient.fetchWeekClazzInfo(groupIdAndReportIds, teacherIdReportEndTime);
    }


    @RequestMapping(value = "sharewholereport.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage shareWholeReport() {
        String groupIdAndReportIdStr = this.getRequestString("groupIdAndReportIds");
        List<String> groupIdAndReportIds = new LinkedList<>();
        if (StringUtils.isNotBlank(groupIdAndReportIdStr)) {
            String[] ss = StringUtils.split(groupIdAndReportIdStr, ",");
            for (String s : ss) {
                groupIdAndReportIds.add(s);
            }
        } else {
            return MapMessage.errorMessage();
        }
        String teacherIdReportEndTime = this.getRequestString("teacherIdReportEndTime");
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登入");
        }
        if (StringUtils.isBlank(teacherIdReportEndTime)) {
            return MapMessage.errorMessage("参数错误");
        }
        String[] teacherIdReportStartTimes = StringUtils.split(teacherIdReportEndTime, "|");
        if (teacherIdReportStartTimes == null || teacherIdReportStartTimes.length != 2) {
            return MapMessage.errorMessage("参数错误");
        }
        Long tid = SafeConverter.toLong(teacherIdReportStartTimes[0]);
        String endTime = teacherIdReportStartTimes[1];
        try {
//            return MapMessage.successMessage();
            return atomicLockManager.wrapAtomic(weekReportServiceClient)
                    .keyPrefix("sharewholereport")
                    .keys(tid, endTime)
                    .proxy()
                    .shareWholeReport(tid, endTime, groupIdAndReportIds, teacher.getId());
        } catch (CannotAcquireLockException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("teacher {} failed share week report{}", teacher.getId(), groupIdAndReportIds, ex);
            return MapMessage.errorMessage();
        }

    }


    @RequestMapping(value = "sharepartreport.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage sharePartReport() {
        String groupIdAndReportId = this.getRequestString("groupIdAndReportId");
        if (StringUtils.isBlank(groupIdAndReportId)) {
            return MapMessage.errorMessage("参数错误");
        }
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return MapMessage.successMessage();
//            return atomicLockManager.wrapAtomic(weekReportServiceClient)
//                    .keys(groupIdAndReportId, user.getId())
//                    .proxy()
//                    .sharePartReport(groupIdAndReportId, user.getId());
        } catch (CannotAcquireLockException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("share part report failed : groupIdAndReportId of {}", groupIdAndReportId, ex);
            return MapMessage.errorMessage();
        }

    }
}
