package com.voxlearning.utopia.service.newexam.api.client;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tanguohong on 2016/3/7.
 */
public interface INewExamServiceClient extends IPingable {

    /**
     * 获取能进入的考试
     * @param beforeExamStartMinutes 考试开始前多少分钟
     */
    List<Map<String, Object>> loadExamsCanBeEntered(StudentDetail studentDetail, School school, ExRegion exRegion, Integer beforeExamStartMinutes);

    /**
     * 通过学生id获取能进入的考试
     */
    List<Map<String, Object>> loadExamsCanBeEnteredByStudentId(Long studentId);

    /**
     * 获取所有满足学校，地区，学年等条件的考试
     */
    MapMessage loadAllExams(StudentDetail studentDetail, School school, ExRegion exRegion);

    /**
     * 根据学生id获取所有满足学校，地区，学年等条件的考试
     */
    MapMessage loadAllExamsByStudentId(Long studentId);

    /**
     * 报名
     */
    MapMessage registerNewExam(StudentDetail studentDetail, School school, ExRegion exRegion, String newExamId, String clientType, String clientName);

    /**
     * 暂不报名
     */
    MapMessage unRegisterNewExam(StudentDetail studentDetail, School school, ExRegion exRegion, String newExamId, String clientType, String clientName);

    MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId);


    MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId, boolean makeUp);

    MapMessage processorNewExamResult(NewExamResultContext newExamResultContext);

    MapMessage submitNewExam(String newExamId, Long userId, String clientType, String clientName);


    //crm 调用 学生提交作业
    MapMessage crmSubmitNewExam(String newExamId, Long userId);

    /**
     * 获取本次作业学生考试答案详情
     */
    MapMessage loadQuestionAnswer(String newExamId, Long studentId, Boolean includeStandardAnswer);

    /**
     * 考试首页信息
     */
    MapMessage index(String newExamId, Long studentId);

    /**
     * 进入考试
     */
    MapMessage enterExam(String newExamId, StudentDetail studentDetail, String cdnUrl, String clientType, String clientName);

    MapMessage viewExam(String newExamId, StudentDetail studentDetail, String cdnUrl);

    /**
     * 获取考试详情
     */
    MapMessage loadNewExamDetail(String newExamId, StudentDetail studentDetail);

    MapMessage loadTeacherClazzListNew(Set<Long> teacherIds);


    MapMessage correctNewExam(CorrectNewExamContext correctNewExamContext);

    /**
     * {"newExamId":"E_10300000886348", "questionDocId":"Q_10205987483894", "answer":[["17"]], "errorAnswer":[["=17"]], "allUser":true}
     * {"newExamId":"E_10300000886348", "questionDocId":"Q_10205987483894", "answer":[["17"]], "errorAnswer":[["=17"]], "studentIds":[354093642]}
     * 重置某道题答案及分数
     *
     * @param pram
     * @return
     */
    MapMessage resetScore(Map<String, Object> pram);

    MapMessage newResetScore(String param);

    MapMessage loadPaperList(String bookId, Teacher teacher);

    MapMessage assignNewExam(Teacher teacher, Map<String, Object> source);

    MapMessage loadTeacherClazzList(Set<Long> teacherIds);

    MapMessage deleteNewExam(Teacher teacher, String newExamId);

    MapMessage loadAppIndexData(Teacher teacher);

    MapMessage shareIndependentReport(Teacher teacher, String newExamId);

    MapMessage restoreData(List<String> newExamResultIds);

    MapMessage resetOralQuestionScoreV2(String newExamId, String questionId,String paperDocId, List<Long> userId);

    MapMessage resetNewExamResultScore(String newExamResultId, Double score, Double correctScore);

    MapMessage loadUnitTestPaperList(String unitId, Long teacherId);

    MapMessage previewUnitTest(String paperId);

    MapMessage assignUnitTest(Teacher teacher, Map<String, Object> source);

    MapMessage adjustUnitTest(Long teacherId, String newExamId, Date end);

    MapMessage loadStudentUnitTestHistoryList(StudentDetail studentDetail);

    MapMessage loadStudentIndexUnitTestList(StudentDetail studentDetail);
}
