package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.consumer.OutsideReadingLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.OutsideReadingServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

@Controller
@RequestMapping("/teacher/outside/reading")
public class TeacherOutsideReadingController extends AbstractTeacherController {

    @Inject private OutsideReadingServiceClient outsideReadingServiceClient;
    @Inject private OutsideReadingLoaderClient outsideReadingLoaderClient;

    /**
     * 获取班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadTeacherClazzList() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        return outsideReadingServiceClient.loadTeacherClazzList(teacher.getId());
    }

    /**
     * 获取图书类型筛选项列表
     */
    @RequestMapping(value = "booktype/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadBookTypeList() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        return outsideReadingServiceClient.loadBookTypeList();
    }

    /**
     * 图书列表
     */
    @RequestMapping(value = "book/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadBookList() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            return MapMessage.errorMessage("班组id错误");
        }
        int clazzLevel = getRequestInt("clazzLevel");
        String bookType = getRequestString("bookType");
        int pageNumber = getRequestInt("pageNumber", 1);
        int pageSize = getRequestInt("pageSize", 10);
        return outsideReadingServiceClient.loadBookList(teacher.getId(), groupId, clazzLevel, bookType, pageNumber, pageSize);
    }

    /**
     * 图书详情
     */
    @RequestMapping(value = "book/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadBookDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        long groupId = getRequestLong("groupId");
        String bookId = getRequestString("bookId");
        return outsideReadingServiceClient.loadBookDetail(teacher.getId(), groupId, bookId);
    }

    /**
     * 推荐前确认当前班级是否有进行中的任务，如果有弹窗提示
     */
    @RequestMapping(value = "confirm.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage confirm() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        long groupId = getRequestLong("groupId");
        return outsideReadingServiceClient.confirm(teacher.getId(), groupId);
    }

    /**
     * 推荐
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage assign() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        long groupId = getRequestLong("groupId");
        String bookIds = getRequestString("bookIds");
        int planDays = getRequestInt("planDays");
        String endDate = getRequestString("endDate");
        try {
            return AtomicLockManager.instance().wrapAtomic(outsideReadingServiceClient)
                    .keys(teacher.getId(), groupId)
                    .proxy()
                    .assign(teacher.getId(), groupId, bookIds, planDays, endDate);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中，请不要重复提交!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to assign outside reading, teacherId {}, groupId {}, exception {}", teacher.getId(), groupId, ex);
            return MapMessage.errorMessage("推荐失败！").setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
        }
    }

    /**
     * 图书阅读报告列表
     */
    @RequestMapping(value = "report/book/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadReportBookList() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        long groupId = getRequestLong("groupId");

        return outsideReadingLoaderClient.loadReportBookList(teacher.getId(), groupId);
    }

    /**
     * 修改阅读任务截止时间
     */
    @RequestMapping(value = "modify/endtime.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyEndTime() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        String readingId = getRequestString("readingId");
        String endTimeStr = getRequestString("endTime");
        Date endTime = DateUtils.stringToDate(endTimeStr, FORMAT_SQL_DATE);// yyyy-MM-dd
        if (endTime == null) {
            return MapMessage.errorMessage("截止时间为空或格式错误");
        }

        endTime = DateUtils.addMilliseconds(endTime, SafeConverter.toInt(DateUtils.DAY_TIME_LENGTH_IN_MILLIS - 1));
        Date currentTime = new Date();
        if (endTime.before(currentTime)) {
            return MapMessage.errorMessage("截止时间不能早于当前时间");
        }

        if (endTime.getTime() - currentTime.getTime() > DateUtils.DAY_TIME_LENGTH_IN_MILLIS * 365) {
            return MapMessage.errorMessage("截止时间不要超过一年哦~");
        }

        return outsideReadingServiceClient.modifyEndTime(readingId, endTime);
    }

    /**
     * 查询学生主观题答题详情
     */
    @RequestMapping(value = "answer/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchAnswerDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        String readingId = getRequestString("readingId");

        return outsideReadingLoaderClient.fetchAnswerDetail(readingId, getCdnBaseUrlAvatarWithSep());
    }

    /**
     * 获取报告班级列表
     */
    @RequestMapping(value = "report/clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadReportClazzList() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        return outsideReadingServiceClient.loadReportClazzList(teacher.getId());
    }

    /**
     * 阅读报告-班级成就
     */
    @RequestMapping(value = "clazz/achievement.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadReportClazzAchievement() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        Long groupId = getRequestLong("groupId");
        return outsideReadingLoaderClient.loadReportClazzAchievement(groupId);
    }

    /**
     * 阅读报告-个人成就
     */
    @RequestMapping(value = "student/achievement.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadStudentAchievement() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        Long studentId = getRequestLong("studentId");
        return outsideReadingLoaderClient.loadBookshelf(studentId, getCdnBaseUrlAvatarWithSep());
    }

}
