package com.voxlearning.utopia.service.newhomework.impl.strategy.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/7/24
 */
@Named
public class MathDiagnosticInterventionsReportImpl extends BaseDiagnoseReportStrategy {

    @Override
    public DiagnoseReportDetailResp getDiagnoseReportDetail(String courseId, String questionBoxId, List<String> questionBoxQids, Map<String, NewQuestion> newQuestionMap,
                                                            Map<Long, User> studentMap, Map<Long, NewHomeworkResult> newHomeworkResultMap, Map<String, SubHomeworkProcessResult> processResultMap,
                                                            Map<String, SelfStudyHomework> selfStudyHomeworkMap, Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap) {

        ObjectiveConfigType configType = ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS;
        Map<Long, List<SubHomeworkProcessResult>> userProcessResultMap = processResultMap.values().stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getUserId));
        Map<Long, SelfStudyHomework> studyHomeworkStudentMap = selfStudyHomeworkMap.values().stream()
                .filter(s->studyHomeworkResultMap.containsKey(s.getId()))
                .collect(Collectors.toMap(SelfStudyHomework::getStudentId, Function.identity()));

        //<学生ID, 巩固学习中同步诊断课程IDs>
        Map<Long, List<String>> studentSyncDiagnosisCourseIdMap = MapUtils.transform(studyHomeworkStudentMap, ssh -> ssh.findNewHomeworkApps(configType).stream()
                .filter(app -> app.getDiagnosisSource() != null && app.getDiagnosisSource().equals(NewHomeworkApp.DiagnosisSource.SyncDiagnosis))
                .map(NewHomeworkApp::getCourseId)
                .collect(Collectors.toList()));

        //<学生ID, 课程>
        Map<Long, Set<String>> userCourseMap = studyHomeworkResultMap.values().stream().collect(Collectors.toMap(SelfStudyHomeworkResult::getUserId, s -> {
            List<String> syncDiagnosisCourseIds = studentSyncDiagnosisCourseIdMap.getOrDefault(s.getUserId(), Collections.emptyList());
            return Maps.filterKeys(s.findAppAnswer(configType), syncDiagnosisCourseIds::contains).keySet();
            }));

        Map<Long, List<SubHomeworkProcessResult>> userGraspProcessResultMap = diagnoseReportService.getUserGraspSelfProcessResultMap(studyHomeworkStudentMap.values(), configType);
        //过滤掉非同步诊断的做题记录
        userGraspProcessResultMap = MapUtils.transform(userGraspProcessResultMap, prs ->
                prs.stream().filter(pr -> studentSyncDiagnosisCourseIdMap.getOrDefault(pr.getUserId(), Collections.emptyList()).contains(pr.getCourseId()))
                        .collect(Collectors.toList()));

        List<Map<String, String>> questionCourseIdMapList = Lists.newArrayList();
        studyHomeworkStudentMap.values().forEach(s -> questionCourseIdMapList.add(s.findAppErrorQuestionCourseMap(NewHomeworkApp.DiagnosisSource.SyncDiagnosis)));
        //<原作业题ID, 课程>
        Map<String, Set<String>> questionCourseIdMap = mapCombine(questionCourseIdMapList);

        Map<String, List<SubHomeworkProcessResult>> questionProcessResultMap = processResultMap.values().stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getQuestionId));
        Map<Long, Set<String>> userCorrectCourseMap = getUserGraspCourseMap(studyHomeworkResultMap, studentSyncDiagnosisCourseIdMap);

        //题目诊断分析
        Map<String, String> variantNameMap = getVariantNameMap(newQuestionMap.values());
        Map<String, String> courseNameMap = loadQuestionCourseNameMap(newQuestionMap.values());
        DiagnoseReportDetailResp.QuestionAnalysis questionAnalysis = new DiagnoseReportDetailResp.QuestionAnalysis();
        for (String questionId : questionBoxQids) {
            DiagnoseReportDetailResp.QuestionCourse questionCourse = new DiagnoseReportDetailResp.QuestionCourse();

            List<SubHomeworkProcessResult> processResults = questionProcessResultMap.get(questionId);
            long interventionGraspCount = processResults.stream().filter(p -> p.isIntervention() && p.getGrasp()).count();
            long questionPreGraspCount = processResults.stream().filter(p -> !p.isIntervention() && p.getGrasp()).count();
            int accuracy = accuracy(questionPreGraspCount, processResults.size());
            int errorCount = processResults.size() - (int) questionPreGraspCount;

            DiagnoseReportDetailResp.Question respQuestion = new DiagnoseReportDetailResp.Question(questionId, accuracy, errorCount);
            NewQuestion newQuestion = newQuestionMap.get(questionId);
            String variantId = CollectionUtils.isEmpty(newQuestion.getVariantIds()) ? null : newQuestion.getVariantIds().get(0);
            String errorFactorId = CollectionUtils.isEmpty(newQuestion.getErrorFactorIds()) ? null : newQuestion.getErrorFactorIds().get(0);
            questionCourse.setPackets(Collections.singletonList(new DiagnoseReportDetailResp.Packet(variantId, variantNameMap.get(variantId), Collections.singletonList(respQuestion))));
            questionCourse.setInterventionCount((int) interventionGraspCount);
            questionCourse.setCourseName(courseNameMap.get(variantId + errorFactorId));

            Set<String> courseIds = questionCourseIdMap.get(questionId);
            long courseLearnCount = userCourseMap.values().stream().filter(v -> CollectionUtils.isNotEmpty(courseIds) && !Collections.disjoint(courseIds, v)).count();
            long simQCorrectCount = userCorrectCourseMap.values().stream().filter(v -> CollectionUtils.isNotEmpty(courseIds) && !Collections.disjoint(courseIds, v)).count();
            questionCourse.setCourseLearnCount(SafeConverter.toInt(courseLearnCount));
            questionCourse.setSimQCorrectCount(simQCorrectCount);
            questionAnalysis.getQuestionCourses().add(questionCourse);

            //正确率（不含即时干预后做对）低于80%的题目对应的变式
            if (accuracy < 80) {
                questionAnalysis.getVariants().add(new DiagnoseReportDetailResp.Variant(variantId, variantNameMap.get(variantId), accuracy));
            }
        }

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
            long preGraspCount = subHomeworkProcessResults.stream().filter(p -> !p.isIntervention() && p.getGrasp()).count();
            long interventionGraspCount = subHomeworkProcessResults.stream().filter(BaseHomeworkProcessResult::getGrasp).count();
            allPreGraspCount += preGraspCount;
            int preAccuracy = accuracy(preGraspCount, questionBoxQCount);

            //课程前测做对+课程后测做对题数
            long afterGraspCount = interventionGraspCount + statQuestionBoxGraspPreQuestionCount(userGraspProcessResultMap.get(user.getId()), studyHomeworkStudentMap.get(user.getId()), questionBoxId);
            allAfterGraspCount += afterGraspCount;
            String accuracy = "未提交";
            if (preAccuracy == 100) {
                accuracy = "-";
            } else if (userCourseMap.containsKey(user.getId())) {
                accuracy = SafeConverter.toString(accuracy(afterGraspCount, questionBoxQCount));
            }

            //正确率（不含即时干预后做对）低于75%的学生
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

    /**
     * 获取学生自学作业同步诊断做对的课程ids
     *
     * @param studyHomeworkResultMap 自学作业中间结果map
     * @param studentSyncDiagnosisCourseIdMap <学生ID, 巩固学习中同步诊断课程IDs>
     * @return <学生ID, 做对的课程ids>
     */
    Map<Long, Set<String>> getUserGraspCourseMap(Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap, Map<Long, List<String>> studentSyncDiagnosisCourseIdMap) {
        Map<Long, Set<String>> userGraspCourseMap = Maps.newHashMap();
        studyHomeworkResultMap.values().forEach(s -> {
            List<String> syncDiagnosisCourseIds = studentSyncDiagnosisCourseIdMap.getOrDefault(s.getUserId(), Collections.emptyList());
            Set<String> courseSet = new HashSet<>();
            s.findAppAnswer(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS).values().stream().filter(BaseHomeworkResultAppAnswer::isGrasp).forEach(appAnswer -> {
                if (syncDiagnosisCourseIds.contains(appAnswer.getCourseId())) {
                    courseSet.add(appAnswer.getCourseId());
                }
            });
            userGraspCourseMap.put(s.getUserId(), courseSet);
        });
        return userGraspCourseMap;
    }
}
