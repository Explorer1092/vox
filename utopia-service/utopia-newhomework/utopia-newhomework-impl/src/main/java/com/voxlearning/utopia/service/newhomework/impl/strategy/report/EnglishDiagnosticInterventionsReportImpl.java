package com.voxlearning.utopia.service.newhomework.impl.strategy.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.DiagnoseReportDetailResp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/7/24
 */
@Named
public class EnglishDiagnosticInterventionsReportImpl extends BaseDiagnoseReportStrategy {

    @Override
    public DiagnoseReportDetailResp getDiagnoseReportDetail(String courseId, String questionBoxId, List<String> questionBoxQids, Map<String, NewQuestion> newQuestionMap,
                                                            Map<Long, User> studentMap, Map<Long, NewHomeworkResult> newHomeworkResultMap, Map<String, SubHomeworkProcessResult> processResultMap,
                                                            Map<String, SelfStudyHomework> selfStudyHomeworkMap, Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap) {

        ObjectiveConfigType configType = ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS;
        Map<Long, List<SubHomeworkProcessResult>> userProcessResultMap = processResultMap.values().stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getUserId));
        Map<String, List<SubHomeworkProcessResult>> questionProcessResultMap = processResultMap.values().stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getQuestionId));

        //当前题包哪些学生命中了课程
        Map<Long, SelfStudyHomework> studentSelfHomeworkMap = selfStudyHomeworkMap.values().stream()
                .filter(s -> Lists.transform(s.findNewHomeworkApps(configType), NewHomeworkApp::getQuestionBoxId).contains(questionBoxId))
                .collect(Collectors.toMap(SelfStudyHomework::getStudentId, Function.identity()));

        //哪些(几个)学生完成了订正
        Map<Long, SelfStudyHomeworkResult> studentSelfHomeworkResultMap = studyHomeworkResultMap.values().stream().collect(Collectors.toMap(SelfStudyHomeworkResult::getUserId, Function.identity()));
        Map<Long, SelfStudyHomework> finishStudentSelfHomeworkMap = Maps.filterKeys(studentSelfHomeworkMap, studentSelfHomeworkResultMap::containsKey);
        Map<Long, List<SubHomeworkProcessResult>> userGraspProcessResultMap = diagnoseReportService.getUserGraspSelfProcessResultMap(finishStudentSelfHomeworkMap.values(), configType);

        DiagnoseReportDetailResp.QuestionAnalysis questionAnalysis = new DiagnoseReportDetailResp.QuestionAnalysis();
        DiagnoseReportDetailResp.QuestionCourse questionCourse = new DiagnoseReportDetailResp.QuestionCourse();
        questionCourse.setCourseLearnCount(finishStudentSelfHomeworkMap.size());
        questionCourse.setCourseName(getVideoOrCourseName(configType, courseId));
        questionCourse.setSimQCorrectCount(getSimQGraspCount(finishStudentSelfHomeworkMap, studyHomeworkResultMap, configType, questionBoxId));
        questionCourse.setPackets(wrapPackets(newQuestionMap, questionProcessResultMap));
        questionAnalysis.getQuestionCourses().add(questionCourse);

        DiagnoseReportDetailResp reportDetailResp = new DiagnoseReportDetailResp();
        long allPreGraspCount = 0, allAfterGraspCount = 0;
        int totalCount = 0;
        //学生诊断分析
        for (User user : studentMap.values()) {
            NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(user.getId());
            if (newHomeworkResult == null) {
                reportDetailResp.getStudentAnalyses().add(new DiagnoseReportDetailResp.StudentAnalysis(user.getId(), user.fetchRealnameIfBlankId(), "未提交", "未提交"));
                continue;
            }
            int questionBoxQCount = Maps.filterKeys(newHomeworkResult.findProcessAnswersMap(ObjectiveConfigType.INTELLIGENT_TEACHING), questionBoxQids::contains).size();

            totalCount += questionBoxQCount;
            List<SubHomeworkProcessResult> subHomeworkProcessResults = userProcessResultMap.get(user.getId()).stream().filter(o -> questionBoxQids.contains(o.getQuestionId())).collect(Collectors.toList());
            long preGraspCount = subHomeworkProcessResults.stream().filter(BaseHomeworkProcessResult::getGrasp).count();
            allPreGraspCount += preGraspCount;
            int preAccuracy = accuracy(preGraspCount, questionBoxQCount);

            //课程前测做对+课程后测做对题数
            long afterGraspCount = preGraspCount + statQuestionBoxGraspPreQuestionCount(userGraspProcessResultMap.get(user.getId()), studentSelfHomeworkMap.get(user.getId()), questionBoxId);
            allAfterGraspCount += afterGraspCount;
            String accuracy = "未提交";
            if (preAccuracy == 100) {
                accuracy = "-";
            } else if (finishStudentSelfHomeworkMap.containsKey(user.getId())) {
                accuracy = SafeConverter.toString(accuracy(afterGraspCount, questionBoxQCount));
            }

            //正确率低于75%的学生
            if (preAccuracy < 75) {
                questionAnalysis.getStudentNames().add(user.fetchRealnameIfBlankId());
            }
            reportDetailResp.getStudentAnalyses().add(new DiagnoseReportDetailResp.StudentAnalysis(user.getId(), user.fetchRealnameIfBlankId(), SafeConverter.toString(preAccuracy), accuracy));
        }

        reportDetailResp.getStudentAnalyses().sort(new DiagnoseReportDetailResp.StudentAnalysis.StudentComparator());
        int accuracy = accuracy(allAfterGraspCount, totalCount);
        int preAccuracy = accuracy(allPreGraspCount, totalCount);
        reportDetailResp.setAccuracy(accuracy);
        reportDetailResp.setPreAccuracy(preAccuracy);
        reportDetailResp.setPromoteAccuracy(accuracy - preAccuracy);
        reportDetailResp.setQuestionAnalysis(questionAnalysis);
        reportDetailResp.setObjectiveConfigType(ObjectiveConfigType.INTELLIGENT_TEACHING.name());
        return reportDetailResp;
    }
}
