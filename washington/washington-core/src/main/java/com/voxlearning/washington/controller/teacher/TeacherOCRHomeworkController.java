package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("teacher/ocr/homework")
public class TeacherOCRHomeworkController extends AbstractTeacherController {
    /**
     * 学科列表
     */
    @RequestMapping(value = "subjects.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTeacherSubjects() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        List<Subject> subjects = teacher.getSubjects();
        if (CollectionUtils.isEmpty(subjects)) {
            return MapMessage.errorMessage("老师信息错误");
        }
        List<Map<String, Object>> subjectMappers = subjects.stream()
                .filter(subject -> Subject.CHINESE != subject)
                .map(subject -> MapUtils.m("subject", subject.name(), "subjectName", subject.getValue()))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("subjects", subjectMappers);
    }

    /**
     * 班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadClazzList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        return newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.OCR), true);
    }

    /**
     * 口算练习册列表
     */
    @RequestMapping(value = "mental/booklist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadMentalBookList() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.MATH);
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        return newHomeworkContentServiceClient.loadOcrMentalBookList(teacher);
    }

    /**
     * 添加口算练习册
     */
    @RequestMapping(value = "mental/addbook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addBook() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.MATH);
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String bookName = getRequestString("bookName");
        if (StringUtils.isEmpty(bookName)) {
            return MapMessage.errorMessage("课本名不能为空");
        }
        return newHomeworkContentServiceClient.addOcrMentalBook(teacher, bookName);
    }

    /**
     * 删除口算练习册
     */
    @RequestMapping(value = "mental/deletebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteBook() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.MATH);
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String bookId = getRequestString("bookId");
        return newHomeworkContentServiceClient.deleteOcrMentalBook(teacher, bookId);
    }

    /**
     * 听写单元列表
     */
    @RequestMapping(value = "dictation/unitlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dictationUnitList() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.ENGLISH);
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String groupIds = getRequestString("groupIds");
        List<Long> groupIdList = StringUtils.toLongList(groupIds);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return MapMessage.errorMessage("班组ids不能为空");
        }
        return newHomeworkContentServiceClient.loadOcrDictationUnitList(teacher, groupIdList);
    }

    /**
     * 听写内容
     */
    @RequestMapping(value = "dictation/content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dictationContent() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String bookId = getRequestString("bookId");
        if (StringUtils.isEmpty(bookId)) {
            return MapMessage.errorMessage("教材id不能为空");
        }
        String unitId = getRequestString("unitId");
        if (StringUtils.isEmpty(unitId)) {
            return MapMessage.errorMessage("单元id不能为空");
        }
        return newHomeworkContentServiceClient.loadOcrDictationContent(teacher, bookId, unitId);
    }

    /**
     * 布置成功后的分享页面
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadHomeworkDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String homeworkIds = getRequestString("homeworkIds");
        List<String> homeworkIdList = StringUtils.toList(homeworkIds, String.class);
        if (CollectionUtils.isEmpty(homeworkIdList)) {
            return MapMessage.errorMessage("作业id错误");
        }
        return newHomeworkReportServiceClient.loadOcrHomeworkDetail(homeworkIdList);
    }

    /**
     * 分享，发送家长端push消息
     */
    @RequestMapping(value = "share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shareOcrHomework() {
        return MapMessage.successMessage();
    }
}
