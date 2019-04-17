package com.voxlearning.utopia.service.newhomework.impl.strategy.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.DiagnoseReportDetailResp;
import com.voxlearning.utopia.service.newhomework.impl.service.DiagnoseReportImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.video.MicroVideoTask;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base {@link DiagnoseReportStrategy} class basically.
 *
 * @author majianxin
 * @version V1.0
 * @date 2018/7/26
 */
abstract class BaseDiagnoseReportStrategy extends SpringContainerSupport implements DiagnoseReportStrategy {

    @Inject private IntelDiagnosisClient intelDiagnosisClient;
    @Inject private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject DiagnoseReportImpl diagnoseReportService;

    /**
     * 获取视频课程或者轻交互课程名称
     *
     * @param configType 作业类型
     * @param courseId   课程ID
     * @return
     */
    String getVideoOrCourseName(ObjectiveConfigType configType, String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            return null;
        }
        if (configType.equals(ObjectiveConfigType.ORAL_INTERVENTIONS)) {
            Map<String, MicroVideoTask> microVideoTaskMap = intelDiagnosisClient.loadMicroVideoTaskByIdsIncludeDisabled(Collections.singletonList(courseId));
            return (MapUtils.isNotEmpty(microVideoTaskMap) && microVideoTaskMap.get(courseId) != null)
                    ? microVideoTaskMap.get(courseId).getName()
                    : "";
        }
        Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(Collections.singletonList(courseId));
        return (MapUtils.isNotEmpty(intelDiagnosisCourseMap) && intelDiagnosisCourseMap.get(courseId) != null)
                ? intelDiagnosisCourseMap.get(courseId).getName()
                : "";

    }

    /**
     * 统计学生一份作业掌握题包(后测题全做对)对应前测题数量
     * 注: 重点讲练测DIAGNOSTIC_INTERVENTIONS 一种作业形式
     *
     * @param questionBoxId 原作业题包ID
     * @return
     */
    long statQuestionBoxGraspPreQuestionCount(List<SubHomeworkProcessResult> selfHomeworkProcessResults, SelfStudyHomework selfStudyHomework, String questionBoxId) {
        int questionBoxGraspPreQuestionCount = 0;
        if (CollectionUtils.isNotEmpty(selfHomeworkProcessResults) && selfStudyHomework != null) {
            List<String> correctQuestionIds = Lists.transform(selfHomeworkProcessResults, SubHomeworkProcessResult::getQuestionId);
            //题包对应的课程ids
            Map<String, Set<String>> questionBoxCourseMap = getQuestionBoxCourseIdsMap(selfStudyHomework, ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS);
            Set<String> courseIds = questionBoxCourseMap.get(questionBoxId);
            if (CollectionUtils.isNotEmpty(courseIds)) {
                for (String courseId : courseIds) {
                    questionBoxGraspPreQuestionCount += diagnoseReportService.statCourseGraspPreQuestionCount(selfStudyHomework, correctQuestionIds, courseId);
                }
            }
        }
        return questionBoxGraspPreQuestionCount;
    }

    /**
     * 计算正确率(四舍五入取整)
     *
     * @param preCount
     * @param totalCount
     * @return
     */
    int accuracy(long preCount, int totalCount) {
        return BigDecimal.valueOf(preCount * 100).divide(new BigDecimal(totalCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    Map<String, String> getVariantNameMap(Collection<NewQuestion> newQuestions) {
        Set<String> variantIdSet = newQuestions.stream().filter(q -> CollectionUtils.isNotEmpty(q.getVariantIds())).map(q -> q.getVariantIds().get(0)).collect(Collectors.toSet());
        return diagnoseReportService.getVariantNameMap(variantIdSet);
    }

    /**
     * 根据题目变式和错因查询题目对应课程名称
     *
     * @param newQuestions
     * @return <variantId+errorFactorId, courseName>
     */
    Map<String, String> loadQuestionCourseNameMap(Collection<NewQuestion> newQuestions) {
        List<List<String>> variantIdErrorFactorIdPairs = Lists.newArrayList();
        newQuestions.stream().filter(q -> CollectionUtils.isNotEmpty(q.getVariantIds()) && CollectionUtils.isNotEmpty(q.getErrorFactorIds())).forEach(o ->
                variantIdErrorFactorIdPairs.add(Arrays.asList(o.getVariantIds().get(0), o.getErrorFactorIds().get(0)))
        );
        List<IntelDiagnosisCourse> intelDiagnosisCourses = intelDiagnosisClient.loadOnlineIntelCourseByVarIdsAndErrorIds(variantIdErrorFactorIdPairs);
        if (CollectionUtils.isNotEmpty(intelDiagnosisCourses)) {
            return intelDiagnosisCourses.stream().collect(Collectors.toMap(i -> i.getVariantId() + i.getErrorFactorId(), IntelDiagnosisCourse::getName));
        }
        return Collections.emptyMap();
    }

    /**
     * 合并map中相同key的value值
     */
    static Map<String, Set<String>> mapCombine(List<Map<String, String>> list) {
        Map<String, Set<String>> map = new HashMap<>();
        for (Map<String, String> m : list) {
            for (String key : m.keySet()) {
                if (!map.containsKey(key)) {
                    Set<String> newSet = new HashSet<>();
                    newSet.add(m.get(key));
                    map.put(key, newSet);
                } else {
                    map.get(key).add(m.get(key));
                }
            }
        }
        return map;
    }

    /**
     * 合并map中相同key的value值
     */
    static Map<String, Set<String>> mapSetCombine(List<Map<String, Set<String>>> list) {
        Map<String, Set<String>> map = new HashMap<>();
        for (Map<String, Set<String>> m : list) {
            for (String key : m.keySet()) {
                if (!map.containsKey(key)) {
                    Set<String> newSet = new HashSet<>(m.get(key));
                    map.put(key, newSet);
                } else {
                    map.get(key).addAll(m.get(key));
                }
            }
        }
        return map;
    }

    long getSimQGraspCount(Map<Long, SelfStudyHomework> finishStudentSelfHomeworkMap, Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap, ObjectiveConfigType configType, String questionBoxId) {
        int simQGraspCount = 0;
        List<Map<String, Set<String>>> questionBoxCourseMapList = Lists.newArrayList();
        for (SelfStudyHomework selfStudyHomework : finishStudentSelfHomeworkMap.values()) {
            questionBoxCourseMapList.add(getQuestionBoxCourseIdsMap(selfStudyHomework, configType));
        }
        Map<String, Set<String>> questionBoxCourseSetMap = mapSetCombine(questionBoxCourseMapList);
        Set<String> courseIds = questionBoxCourseSetMap.get(questionBoxId);
        if (CollectionUtils.isNotEmpty(courseIds)) {
            for (String courseId : courseIds) {
                simQGraspCount += getUserGraspQuestionBoxMap(studyHomeworkResultMap, configType).values().stream().filter(v -> v.contains(courseId)).count();
            }
        }
        return simQGraspCount;
    }

    /**
     * 获取学生自学作业指定作业形式做对的题包ids
     *
     * @param studyHomeworkResultMap 自学作业中间结果map
     * @param configType             作业形式
     * @return <学生ID, 该作业形式做对的课程ids>
     */
    Map<Long, Set<String>> getUserGraspQuestionBoxMap(Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap, ObjectiveConfigType configType) {
        Map<Long, Set<String>> userGraspCourseMap = Maps.newHashMap();
        studyHomeworkResultMap.values().forEach(s -> {
            Set<String> courseIdSet = new HashSet<>();
            s.findAppAnswer(configType).values().stream().filter(BaseHomeworkResultAppAnswer::isGrasp).forEach(appAnswer -> courseIdSet.add(appAnswer.getCourseId()));
            userGraspCourseMap.put(s.getUserId(), courseIdSet);
        });
        return userGraspCourseMap;
    }


    Map<String, Set<String>> getQuestionBoxCourseIdsMap(SelfStudyHomework selfStudyHw, ObjectiveConfigType configType) {
        return selfStudyHw.findNewHomeworkApps(configType)
                .stream()
                .filter(o -> o.getQuestionBoxId() != null)
                .collect(Collectors.groupingBy(NewHomeworkApp::getQuestionBoxId, Collectors.mapping(NewHomeworkApp::getCourseId, Collectors.toSet())));
    }

    /**
     * 封装题目分组
     * 英语->题型
     */
    List<DiagnoseReportDetailResp.Packet> wrapPackets(Map<String, NewQuestion> newQuestionMap, Map<String, List<SubHomeworkProcessResult>> questionProcessResultMap) {
        Map<Integer, String> contentTypeNameMap = MapUtils.transform(questionContentTypeLoaderClient.loadQuestionContentTypeAsMap(), NewContentType::getName);
        Map<Integer, List<NewQuestion>> contentTypeMap = newQuestionMap.values().stream().collect(Collectors.groupingBy(NewQuestion::getContentTypeId));
        List<DiagnoseReportDetailResp.Packet> packets = new LinkedList<>();
        for (Map.Entry<Integer, List<NewQuestion>> contentTypeEntry : contentTypeMap.entrySet()) {
            List<DiagnoseReportDetailResp.Question> questions = Lists.newLinkedList();
            for (NewQuestion typeQuestion : contentTypeEntry.getValue()) {
                String questionId = typeQuestion.getId();
                List<SubHomeworkProcessResult> processResults = questionProcessResultMap.get(questionId);
                long questionPreGraspCount = processResults.stream().filter(BaseHomeworkProcessResult::getGrasp).count();
                int accuracy = accuracy(questionPreGraspCount, processResults.size());
                int errorCount = processResults.size() - (int) questionPreGraspCount;
                questions.add(new DiagnoseReportDetailResp.Question(questionId, accuracy, errorCount));
            }
            Integer contentTypeId = contentTypeEntry.getKey();
            packets.add(new DiagnoseReportDetailResp.Packet(contentTypeId.toString(), contentTypeNameMap.get(contentTypeId), questions));
        }
        return packets;
    }

    /**
     * 转换指定课程口语诊断做题结果
     *
     * @param processResultMap
     * @param selfStudyHomeworkMap
     * @param courseId
     * @return
     */
    Map<String, List<SubHomeworkProcessResult>> transformQPRMap(Map<String, SubHomeworkProcessResult> processResultMap, Map<String, SelfStudyHomework> selfStudyHomeworkMap, String courseId) {
        Map<String, List<SubHomeworkProcessResult>> questionProcessResultMap = processResultMap.values().stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getQuestionId));

        //<questionId, Set<studentId>>
        Map<String, Set<Long>> questionStudentsMap = diagnoseReportService.getQuestionUnGraspStudentsMap(selfStudyHomeworkMap, courseId);
        return MapUtils.transform(questionProcessResultMap, l -> {
            l.forEach(s -> {
                Set<Long> unGraspStudents = questionStudentsMap.get(s.getQuestionId());
                s.setGrasp(unGraspStudents == null || !unGraspStudents.contains(s.getUserId()));
            });
            return l;
        });
    }

}
