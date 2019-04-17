package com.voxlearning.utopia.service.newexam.api.client;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForClazz;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForStudent;
import com.voxlearning.utopia.service.newexam.api.mapper.report.TamExamInfo;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;

public interface INewExamReportLoaderClient extends IPingable {

    MapMessage pageUnifyExamList(Long teacherId, Long clazzId, Subject subject, Integer iDisplayLength, Integer iDisplayStart);

    MapMessage newPageUnifyExamList(Teacher teacher, Long groupId, Integer iDisplayLength, Integer iDisplayStart);

    MapMessage pageUnitTestList(Teacher teacher, Subject subject, List<Long> groupIds, Integer iDisplayLength, Integer iDisplayStart);

    MapMessage crmUnifyExamList(Long teacherId, Long clazzId, Subject subject, Long groupId);

    MapMessage examDetailForClazz(Teacher teacher, String newExamId, Long clazzId);

    MapMessage examDetailForStudent(Teacher teacher, String newExamId, Long clazzId);

    MapMessage independentExamDetailForClazz(Teacher teacher, String newExamId);

    MapMessage independentExamDetailForStudent(Teacher teacher, String newExamId);

    MapMessage fetchTeacherClazzInfo(Teacher teacher);

    MapMessage independentExamDetailForParent(String newExamId, Long studentId);

    MapMessage independentExamDetailForShare(String newExamId);

    MapMessage loadNewExamParentReport(String newExamId, Long studentId);

    List<RptMockNewExamStudent> getStudentAchievement(String examId);

    List<RptMockNewExamStudent> getStudentAchievement(ExRegion region, String examId);

    List<RptMockNewExamStudent> getStudentAchievement(String examId,Integer clazzId);

    List<Map<String, Object>> getRegionStatistic(String examId, ExRegion exRegion, String paperId);

    List<Map<String, Object>> getSchoolStatistic(String examId, String paperId, ExRegion exRegion, String countyId);

    List<Map<String, Object>> getClassStatistic(String examId, String paperId, ExRegion exRegion, String schoolId);

    NewExamReportForClazz crmReceiveNewExamReportForClazz(Teacher teacher, String newExamId, Long clazzId);

    NewExamReportForStudent crmReceiveNewExamReportForStudent(String newExamId);

    //h5 单个题接口
    MapMessage fetchNewExamSingleQuestionDimension(Teacher teacher, String newExamId, Long clazzId, String paperId, String questionId, int subIndex);

    //h5 获取考试试卷
    MapMessage fetchNewExamPaperInfo(String newExamId, Long clazzId);

    //h5 获取试卷信息(市场试卷预览)
    MapMessage fetchPaperInfo(List<String> paperIds);

    //h5 学生个人获取试卷答题卡
    MapMessage fetchNewExamPaperQuestionInfo(String newExamId, Long sid);

    //h5 答题表格
    MapMessage fetchNewExamPaperQuestionInfo(String newExamId, String paperId);

    //h5 答题表格((市场试卷预览))
    MapMessage fetchPaperQuestionInfo(String paperId);

    //h5 每份试卷答题的成绩
    MapMessage fetchNewExamPaperQuestionAnswerInfo(Teacher teacher, String newExamId, Long clazzId, String paperId);

    //h5 试卷班级作答详情
    MapMessage paperClazzAnswerDetail(Teacher teacher, String newExamId, Long clazzId, String paperId);

    //h5 每份试卷个人答题的成绩
    MapMessage fetchNewExamPaperQuestionPersonalAnswerInfo(String newExamId, Long userId);

    //h5 每份试卷个人答题的成绩
    MapMessage fetchNewExamPaperStudentAnswerInfo(String newExamId, Long userId);

    //题目分析
    MapMessage fetchNewExamQuestionReport(Teacher teacher, String newExamId, Long clazzId);

    //考试考勤
    MapMessage fetchNewExamAttendanceReport(Teacher teacher, String newExamId, Long clazzId);

    //学生分析
    MapMessage fetchNewExamStudentReport(Teacher teacher, String newExamId, Long clazzId);

    //试卷分析
    MapMessage fetchNewExamStatisticsReport(Teacher teacher, String newExamId, Long clazzId);


    TamExamInfo fetchTamExamInfo(String newExamId);

    //学生查看考试详情上报
    void studentViewExamReportKafka(String examId, Long userId, String actionRefer);

    //报告分享
    MapMessage shareReport(String newExamId, Long clazzId);

    MapMessage loadUnitTestDetail(List<String> newExamIds);

    MapMessage loadUnitTestAdjustDetail(String examId, Long teacherId);

    MapMessage fetchUnitTestTeacherClazzInfo(Teacher teacher);
}
