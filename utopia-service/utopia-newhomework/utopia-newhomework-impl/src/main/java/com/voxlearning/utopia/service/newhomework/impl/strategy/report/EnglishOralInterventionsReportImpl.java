package com.voxlearning.utopia.service.newhomework.impl.strategy.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
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
import java.util.Collections;
import java.util.LinkedHashMap;
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
public class EnglishOralInterventionsReportImpl extends BaseDiagnoseReportStrategy {

    @Override
    public DiagnoseReportDetailResp getDiagnoseReportDetail(String courseId, String questionBoxId, List<String> questionBoxQids, Map<String, NewQuestion> newQuestionMap,
                                                            Map<Long, User> studentMap, Map<Long, NewHomeworkResult> newHomeworkResultMap, Map<String, SubHomeworkProcessResult> processResultMap,
                                                            Map<String, SelfStudyHomework> selfStudyHomeworkMap, Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap) {

        ObjectiveConfigType configType = ObjectiveConfigType.ORAL_INTERVENTIONS;
        Map<Long, SelfStudyHomework> studyHomeworkStudentMap = selfStudyHomeworkMap.values().stream().collect(Collectors.toMap(SelfStudyHomework::getStudentId, Function.identity()));

        //转换口语诊断做题结果grasp
        Map<String, List<SubHomeworkProcessResult>> transformQPRMap = transformQPRMap(processResultMap, selfStudyHomeworkMap, questionBoxId);

        //当前题包哪些学生命中了课程
        Map<Long, SelfStudyHomework> studentSelfHomeworkMap = selfStudyHomeworkMap.values().stream()
                .filter(s -> Lists.transform(s.findNewHomeworkApps(configType), NewHomeworkApp::getQuestionBoxId).contains(questionBoxId))
                .collect(Collectors.toMap(SelfStudyHomework::getStudentId, Function.identity()));

        //哪些(几个)学生完成了订正
        Map<Long, SelfStudyHomeworkResult> studentSelfHomeworkResultMap = studyHomeworkResultMap.values().stream().collect(Collectors.toMap(SelfStudyHomeworkResult::getUserId, Function.identity()));
        Map<Long, SelfStudyHomework> finishStudentSelfHomeworkMap = Maps.filterKeys(studentSelfHomeworkMap, studentSelfHomeworkResultMap::containsKey);

        DiagnoseReportDetailResp.QuestionAnalysis questionAnalysis = new DiagnoseReportDetailResp.QuestionAnalysis();
        DiagnoseReportDetailResp.QuestionCourse questionCourse = new DiagnoseReportDetailResp.QuestionCourse();
        questionCourse.setCourseLearnCount(finishStudentSelfHomeworkMap.size());
        questionCourse.setCourseName(getVideoOrCourseName(configType, courseId));
        questionCourse.setSimQCorrectCount(getSimQGraspCount(finishStudentSelfHomeworkMap, studyHomeworkResultMap, configType, questionBoxId));
        questionCourse.setPackets(wrapPackets(newQuestionMap, transformQPRMap));
        questionAnalysis.getQuestionCourses().add(questionCourse);

        DiagnoseReportDetailResp reportDetailResp = new DiagnoseReportDetailResp();
        long allPreGraspCount = 0, allAfterGraspCount = 0;
        //学生诊断分析
        for (User user : studentMap.values()) {
            if (newHomeworkResultMap.get(user.getId()) == null) {
                reportDetailResp.getStudentAnalyses().add(new DiagnoseReportDetailResp.StudentAnalysis(user.getId(), user.fetchRealnameIfBlankId(), "未提交", "未提交"));
                continue;
            }
            String preAccuracy = "未通过";
            String accuracy = "未提交";
            SelfStudyHomeworkResult selfStudyHomeworkResult = studentSelfHomeworkResultMap.get(user.getId());
            SelfStudyHomework selfStudyHw = studyHomeworkStudentMap.get(user.getId());
            if (selfStudyHw == null || CollectionUtils.isEmpty(selfStudyHw.findQuestionBoxAppErrorQuestionsMap(configType).get(questionBoxId))) {
                preAccuracy = "通过";
                accuracy = "-";
                allPreGraspCount++;
            } else {
                Map<String, String> questionBoxCourseMap = selfStudyHw.findNewHomeworkApps(configType).stream().collect(Collectors.toMap(NewHomeworkApp::getQuestionBoxId, NewHomeworkApp::getCourseId, (o1, o2) -> o1));
                if (getQuestionBoxAppAnswer(selfStudyHomeworkResult, configType).containsKey(questionBoxCourseMap.get(questionBoxId))) {
                    questionAnalysis.getStudentNames().add(user.fetchRealnameIfBlankId());
                    if (getQuestionBoxAppAnswer(selfStudyHomeworkResult, configType).get(questionBoxCourseMap.get(questionBoxId)).isGrasp()) {
                        accuracy = "通过";
                        allAfterGraspCount++;
                    } else {
                        accuracy = "未通过";
                    }
                }
            }
            reportDetailResp.getStudentAnalyses().add(new DiagnoseReportDetailResp.StudentAnalysis(user.getId(), user.fetchRealnameIfBlankId(), preAccuracy, accuracy));
        }

        reportDetailResp.getStudentAnalyses().sort(new DiagnoseReportDetailResp.StudentAnalysis.StudentComparator());
        int accuracy = accuracy(allAfterGraspCount + allPreGraspCount, newHomeworkResultMap.size());
        int preAccuracy = accuracy(allPreGraspCount, newHomeworkResultMap.size());
        reportDetailResp.setAccuracy(accuracy);
        reportDetailResp.setPreAccuracy(preAccuracy);
        reportDetailResp.setPromoteAccuracy(accuracy - preAccuracy);
        reportDetailResp.setQuestionAnalysis(questionAnalysis);
        reportDetailResp.setObjectiveConfigType(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING.name());
        return reportDetailResp;
    }

    private Map<String, BaseHomeworkResultAppAnswer> getQuestionBoxAppAnswer(SelfStudyHomeworkResult selfStudyHomeworkResult, ObjectiveConfigType configType) {
        if (selfStudyHomeworkResult == null) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswer = selfStudyHomeworkResult.findAppAnswer(configType);
        return appAnswer.values().stream().collect(Collectors.toMap(BaseHomeworkResultAppAnswer::getCourseId, Function.identity()));
    }
}
