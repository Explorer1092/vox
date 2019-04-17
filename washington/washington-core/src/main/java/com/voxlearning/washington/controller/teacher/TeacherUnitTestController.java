package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * @Description: 单元检测
 * @author: Mr_VanGogh
 * @date: 2019/3/18 下午4:20
 */
@Controller
@RequestMapping("/teacher/unit/test")
public class TeacherUnitTestController extends AbstractTeacherController{

    @Inject
    private RaikouSDK raikouSDK;
    /**
     * 班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzList() {
        MapMessage mapMessage;
        try {
            Teacher teacher = getSubjectSpecifiedTeacher();
            if (teacher == null || teacher.getSubject() == null) {
                return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
            }
            List<Subject> subjects = teacher.getSubjects();
            if (!subjects.contains(Subject.MATH)) {
                return MapMessage.errorMessage("只支持数学学科");
            }
            mapMessage = newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.emptySet(), true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
        return mapMessage;
    }

    /**
     * 获取教材单元列表
     */
    @RequestMapping(value = "unitlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadUnitList() {
        MapMessage mapMessage;
        try {
            String bookId = getRequestString("bookId");
            if (StringUtils.isBlank(bookId)) {
                return MapMessage.errorMessage().setInfo("教材ID为空");
            }
            mapMessage = newHomeworkContentServiceClient.loadBookUnitList(bookId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
        return mapMessage;
    }

    /**
     * 试卷列表
     */
    @RequestMapping(value = "paperlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadUnitTestPaperList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
        }
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("单元id为空");
        }
        return newExamServiceClient.loadUnitTestPaperList(unitId, teacher.getId());
    }

    /**
     * 预览试卷
     */
    @RequestMapping(value = "preview.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage previewPaper() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
        }
        String paperId = getRequestString("paperId");
        if (StringUtils.isBlank(paperId)) {
            return MapMessage.errorMessage("试卷id不能为空");
        }
        return newExamServiceClient.previewUnitTest(paperId);
    }

    /**
     * 布置测评
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveNewExam() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
        }
        String examData = getRequestString("examData");
        Map<String, Object> jsonMap = JsonUtils.fromJson(examData);
        try {
            return AtomicLockManager.instance().wrapAtomic(newExamServiceClient)
                    .keys(teacher.getId(), teacher.getSubject().getId())
                    .proxy()
                    .assignUnitTest(teacher, jsonMap);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("考试发布中，请不要重复发布");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("发布失败，请稍候重试");
        }
    }

    /**
     * 作业单
     * 布置成功后的分享页面
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadUnitTestDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String examIds = getRequestString("examIds");
        List<String> examIdList = StringUtils.toList(examIds, String.class);
        if (CollectionUtils.isEmpty(examIdList)) {
            return MapMessage.errorMessage("考试id错误");
        }
        MapMessage mapMessage = newExamReportLoaderClient.loadUnitTestDetail(examIdList);
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        if(teacherDetail != null){
            List<Integer> channels = newHomeworkContentServiceClient.loadHomeworkReportShareChannel(teacherDetail);
            mapMessage.put("shareUnitTestChannels", channels);
        }
        return mapMessage;
    }

    /**
     * 删除考试
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteNewExam() {
        try {
            Teacher teacher = getSubjectSpecifiedTeacher();
            if (teacher == null || teacher.getSubject() == null) {
                return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
            }
            String newExamId = getRequestString("newExamId");
            if (StringUtils.isBlank(newExamId)) {
                return MapMessage.errorMessage("试卷id为空");
            }
            return newExamServiceClient.deleteNewExam(teacher, newExamId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return MapMessage.errorMessage("删除失败");
        }
    }

    /**
     * 调整考试弹框
     */
    @RequestMapping(value = "adjust/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage adjustHomeworkInfo() {
        try {
            String newExamId = getRequestString("newExamId");
            if (StringUtils.isBlank(newExamId)) {
                return MapMessage.errorMessage("试卷id为空");
            }
            return newExamReportLoaderClient.loadUnitTestAdjustDetail(newExamId, currentUserId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 调整考试
     */
    @RequestMapping(value = "adjust.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adjust() {
        try {
            String newExamId = getRequestString("newExamId");
            String endDate = getRequestString("endDate");
            Date endDateTime = DateUtils.stringToDate(endDate);
            long currentTime = new Date().getTime();
            if (endDateTime == null || endDateTime.getTime() < currentTime) {
                return MapMessage.errorMessage("截止时间错误");
            }
            return newExamServiceClient.adjustUnitTest(currentUserId(), newExamId, endDateTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 获取测评列表
     */
    @RequestMapping(value = "report/exam/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadExamList() {
        Subject subject = currentSubject();
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getTeacherGroupIds(teacher.getId());
        List<Long> groupIdList = new ArrayList<>();
        String groupIds = getRequestString("groupIds");
        for (String id : groupIds.split(",")) {
            Long groupId = SafeConverter.toLong(id);
            if (!teacherGroupIds.contains(groupId)) {
                return MapMessage.errorMessage("The teacher does not have this groupId:{}", id);
            }
            groupIdList.add(groupId);
        }
        Integer currentPage = getRequestInt("currentPage", 1);
        return newExamReportLoaderClient.pageUnitTestList(teacher, subject, groupIdList, 10, 10 * (currentPage - 1));
    }

    /**
     * 分享，发送家长端push消息
     */
    @RequestMapping(value = "share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shareNewExam() {
        return MapMessage.successMessage();
    }
}
