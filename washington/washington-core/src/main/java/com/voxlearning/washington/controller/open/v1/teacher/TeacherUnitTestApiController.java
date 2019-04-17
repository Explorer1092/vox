package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.REQ_NEWEXAM_ID;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * @Description: 单元检测
 * @author: Mr_VanGogh
 * @date: 2019/3/18 下午4:20
 */
@Controller
@RequestMapping(value = "/v1/teacher/unit/test")
public class TeacherUnitTestApiController extends AbstractTeacherApiController {

    @Inject
    private RaikouSDK raikouSDK;

    /**
     * 教师学科列表
     */
    @RequestMapping(value = "subjectlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage subjectList() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        try {
            List<Subject> subjects = teacher.getSubjects();
            List<Map<String, Object>> subjectList = subjects.stream()
                    .filter(subject -> Subject.MATH == subject)
                    .map(subject -> MapUtils.m("subject", subject.name(), "subjectName", subject.getValue()))
                    .collect(Collectors.toList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_SUBJECT_LIST, subjectList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NO_SUBJECT_MSG);
        }
        return resultMap;
    }


    /**
     * 班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        try {
            MapMessage message = newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.emptySet(), true);
            if (message.isSuccess()) {
                SchoolYear schoolYear = SchoolYear.newInstance();
                resultMap.add(RES_TERM, schoolYear.currentTerm().getKey());
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 获取默认教材
     */
    @RequestMapping(value = "clazzbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadClazzBook() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_CLAZZID_GROUPID_LIST, "班级id与组id列表");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZID_GROUPID_LIST);
            } else {
                validateRequest(REQ_CLAZZID_GROUPID_LIST);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String clazzGroupIds = getRequestString(REQ_CLAZZID_GROUPID_LIST);
        List<String> clazzIdGroupIdList = Arrays.asList(clazzGroupIds.trim().split(","));
        Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
        clazzIdGroupIdList.forEach(str -> {
            String[] strs = str.split("_");
            if (strs.length == 2) {
                long clazzId = SafeConverter.toLong(strs[0]);
                long groupId = SafeConverter.toLong(strs[1]);
                if (clazzId > 0 && groupId > 0) {
                    clazzIdGroupIdMap.put(clazzId, groupId);
                }
            }
        });

        if (MapUtils.isEmpty(clazzIdGroupIdMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "错误班级id与组id列表");
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadClazzBook(teacher, clazzIdGroupIdMap, false);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                Map<String, Object> clazzBook = (Map<String, Object>) message.get("clazzBook");
                if (MapUtils.isNotEmpty(clazzBook)) {
                    clazzBook.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());
                    //过滤Section
                    List<Map<String, Object>> unitMaps = (List<Map<String, Object>>)clazzBook.get("unitList");
                    for (Map<String, Object> unitMap : unitMaps) {
                        Iterator<String> iterator = unitMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (key.equals("sections")) {
                                iterator.remove();
                            }
                        }
                    }
                    clazzBook.put("unitList", unitMaps);
                }
                resultMap.add(RES_CLAZZ_BOOK, clazzBook);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 获取教材单元列表
     */
    @RequestMapping(value = "unitlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadUnitList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "教材id");
            if (StringUtils.isNotEmpty(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_BOOK_ID, REQ_SUBJECT);
            } else {
                validateRequest(REQ_BOOK_ID);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        Subject subject = teacher.getSubject();
        if (subject == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "您还没有设置学科及班级，请完成设置后再登录！");
            return resultMap;
        }

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        String bookId = getRequestString(REQ_BOOK_ID);

        try {
            MapMessage message = newHomeworkContentServiceClient.loadBookUnitList(bookId);
            if (message.isSuccess()) {
                Map<String, Object> bookMap = (Map<String, Object>) message.get("book");
                if (MapUtils.isNotEmpty(bookMap)) {
                    //过滤Section
                    List<Map<String, Object>> unitMaps = (List<Map<String, Object>>)bookMap.get("unitList");
                    for (Map<String, Object> unitMap : unitMaps) {
                        Iterator<String> iterator = unitMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (key.equals("sections")) {
                                iterator.remove();
                            }
                        }
                    }
                    bookMap.put("unitList", unitMaps);
                }
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_BOOK, bookMap);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }

        return resultMap;
    }

    /**
     * 试卷列表
     */
    @RequestMapping(value = "paperlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadUnitTestPaperList() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_UNIT_ID, "单元ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_UNIT_ID);
            } else {
                validateRequest(REQ_UNIT_ID);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }
        String unitId = getRequestString(REQ_UNIT_ID);
        try {
            MapMessage message = newExamServiceClient.loadUnitTestPaperList(unitId, teacher.getId());
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_UNIT_TEST_PAPER_INFO, message.get("unitTestPaperInfos"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 布置单元测试信息
     */
    @RequestMapping(value = "assign/time/info.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage unitTestAssignTimeInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        Map assignTimeInfo = new HashMap<>();
        Date currentDate = new Date();
        Date endData = DateUtils.nextDay(DateUtils.getTodayEnd(), 1);
        assignTimeInfo.put("currentDate", currentDate.getTime());
        assignTimeInfo.put("currentInfo", "");
        assignTimeInfo.put("endDate", endData.getTime());
        assignTimeInfo.put("endInfo", "");

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_UNIT_TEST_ASSIGN_TIME_INFO, assignTimeInfo);
        return resultMap;
    }

    /**
     * 布置考试
     */
    @RequestMapping(value = "assign.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage saveNewExam() {
        MapMessage message = new MapMessage();
        try {
            validateRequired(REQ_EXAM_DATA, "布置单元测数据");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_EXAM_DATA);
            } else {
                validateRequest(REQ_EXAM_DATA);
            }
        } catch (IllegalArgumentException e) {
            message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            message.add(RES_MESSAGE, e.getMessage());
            return message;
        }
        String examData = getRequestString(REQ_EXAM_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(examData);
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            message.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return message;
        }
        try {
            message = AtomicLockManager.instance().wrapAtomic(newExamServiceClient)
                    .keys(teacher.getId(), teacher.getSubject().getId())
                    .proxy()
                    .assignUnitTest(teacher, jsonMap);
            if (!message.isSuccess()) {
                message.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                message.add(RES_MESSAGE, message.getInfo() + "(" + message.getErrorCode() + ")");
            } else {
                message.add(RES_RESULT, RES_RESULT_SUCCESS);
                message.add(RES_OFFLINE_UNIT_TEST_URL, RES_OFFLINE_UNIT_TEST_URL_VALUE);
                String domain = getWebRequestContext().getWebAppBaseUrl();
                Subject subject = Subject.of(getRequestString(REQ_SUBJECT));
                message.add(RES_TEACHER_SUBJECT, subject);
                message.add(RES_DOMAIN, domain);
            }
        } catch (CannotAcquireLockException ex) {
            message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            message.add(RES_MESSAGE, RES_RESULT_DUPLICATE_NEWEXAM_ASSIGN);
            return message;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            message.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            message.add(RES_MESSAGE, RES_RESULT_NEWEXAM_ASSIGN_FAILE_MSG);
            return message;
        }
        return message;
    }

    /**
     * 删除考试
     */
    @RequestMapping(value = "delete.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteExam() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_NEWEXAM_ID, "考试ID");
            validateRequest(REQ_NEWEXAM_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String examId = getRequestString(REQ_NEWEXAM_ID);
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        MapMessage message = newExamServiceClient.deleteNewExam(teacher, examId);
        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE, RES_RESULT_DELETE_SUCCESS_MSG);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        }
        return resultMap;
    }

    /**
     * 调整考试
     */
    @RequestMapping(value = "adjust.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adjust() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_NEWEXAM_ID, "考试ID");
            // 为了方便数据传输，还是用long吧
            validateRequiredNumber(REQ_EXAM_DATE, "考斯结束时间");
            validateRequest(REQ_NEWEXAM_ID, REQ_EXAM_DATE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String examId = getRequestString(REQ_NEWEXAM_ID);
        Long endTime = getRequestLong(REQ_EXAM_DATE);
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        try {
            Date endDate = new Date(endTime);
            long currentTime = new Date().getTime();
            if (endDate.getTime() < currentTime) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_EXAM_ENDDATE_ERROR_MSG);
                return resultMap;
            }
            MapMessage message = newExamServiceClient.adjustUnitTest(currentUserId(), examId, endDate);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, RES_RESULT_ADJUST_SUCCESS_MSG);
                resultMap.putAll(message);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    @RequestMapping(value = "newclazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage newclazzlist() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }
        try {
            MapMessage message = newExamReportLoaderClient.fetchUnitTestTeacherClazzInfo(teacher);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get(RES_CLAZZ_LIST));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }


    /**
     * 获取测评列表
     */
    @RequestMapping(value = "report/exam/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadExamList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_PAGE_NUMBER, "页数");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZ_GROUP_IDS, REQ_PAGE_NUMBER);
            } else {
                validateRequest(REQ_CLAZZ_GROUP_IDS, REQ_PAGE_NUMBER);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String groupIds = getRequestString(REQ_CLAZZ_GROUP_IDS);
        Integer currentPage = getRequestInt(REQ_PAGE_NUMBER, 1);
        Teacher teacher = getCurrentTeacherBySubject();
        Subject subject = Subject.of(getRequestString(REQ_SUBJECT));
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_UNITTEST_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getTeacherGroupIds(teacher.getId());
        List<Long> groupIdList = new ArrayList<>();
        for (String id : groupIds.split(",")) {
            Long groupId = SafeConverter.toLong(id);
            if (!teacherGroupIds.contains(groupId)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "没有班组:{" + id + "}操作权限");
                return resultMap;
            }
            groupIdList.add(groupId);
        }

        try {
            MapMessage message = newExamReportLoaderClient.pageUnitTestList(teacher, subject, groupIdList, 10, 10 * (currentPage - 1));
            resultMap.add(RES_EXAM_LIST, message.get("pageable"));
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }
}
