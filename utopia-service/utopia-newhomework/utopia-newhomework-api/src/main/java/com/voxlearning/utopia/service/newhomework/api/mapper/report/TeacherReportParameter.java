package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReportQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.QuestionWrongReason;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class TeacherReportParameter implements Serializable {
    private static final long serialVersionUID = 3109885884392696082L;

    private static final Set<ObjectiveConfigType> EXAM_TEMPLATE_TYPES = new HashSet<>();

    static {
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.EXAM);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.WORD_PRACTICE);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.READ_RECITE);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.LISTEN_PRACTICE);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.KEY_POINTS);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.BASIC_KNOWLEDGE);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.CHINESE_READING);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.FALLIBILITY_QUESTION);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.INTELLIGENCE_EXAM);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.KNOWLEDGE_REVIEW);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.ORAL_PRACTICE);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.INTERESTING_PICTURE);
        EXAM_TEMPLATE_TYPES.add(ObjectiveConfigType.RW_KNOWLEDGE_REVIEW);
    }


    private NewHomework newHomework;//作业

    private List<ObjectiveConfigType> homeworkTypes;//作业类型

    private Map<String, NewHomeworkResult> newHomeworkResultMap;

    private Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap;//学生每题答题信息

    private Integer newJoinCount = 0;//参与作业人数

    private Integer newFinishCount = 0;// 完成的人数

    private Integer newNeedCorrectCount = 0;//需要批改人数

    private Double newTotalScore = 0D;//总分

    private Long newTotalFinishTime = 0L;//总时长

    private Integer newCorrectedCount = 0;//批改的人数

    private Integer needCorrectNum = 0;//需要订正的人数

    private Integer finishedCorrectedNum = 0;//完成订正的人数


    private boolean showCorrect;//是否有订正


    private boolean isPcWay;


    private Map<ObjectiveConfigType, Integer> homeworkTypeUnCorrect = new HashMap<>();//类型没有批改的人数

    private Map<ObjectiveConfigType, Integer> homeworkTypeScore = new HashMap<>();// 类型分数

    private Map<ObjectiveConfigType, Integer> homeworkTypeDuration = new HashMap<>();//类型用时

    private Map<ObjectiveConfigType, Integer> homeworkTypeFinishCount = new HashMap<>();//类型完成数目


    private Map<String, Map<String, Object>> knowledgePointInfo = new LinkedHashMap<>();//知识点

    private Map<String, List<String>> knowledgePointIdToQIds = new LinkedHashMap<>();

    private List<Map<String, Object>> knowledgePointData = new LinkedList<>();


    private Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap = new LinkedHashMap<>();

    private Map<String, HomeworkSelfStudyRef> refMap = new LinkedHashMap<>();

    private Map<ObjectiveConfigType, Map<String, Map<String, Object>>> typeQuestionsInfo = new LinkedHashMap<>();


    //。。对应for循环的
    private Map<String, Object> studentReport;

    private SelfStudyHomeworkReport selfStudyHomeworkReport;

    private HomeworkSelfStudyRef homeworkSelfStudyRef;

    private NewHomeworkResult newHomeworkResult;

    private User user;

    private Map<ObjectiveConfigType, List<Map<String, Object>>> baseAppType = new LinkedHashMap<>();

    private ObjectiveConfigType type;

    //知识点统计数据处理
    public void finishHandleKnowledgePointResult(Map<String, NewKnowledgePoint> newKnowledgePointMap) {

        Set<String> removeIds = new HashSet<>();
        for (String kId : getKnowledgePointIdToQIds().keySet()) {
            NewKnowledgePoint newKnowledgePoint = newKnowledgePointMap.get(kId);
            if (newKnowledgePoint == null) continue;
            Map<String, Object> d = getKnowledgePointInfo().get(kId);
            int rightNum = SafeConverter.toInt(d.get("rightNum"));
            int errorNum = SafeConverter.toInt(d.get("errorNum"));
            if ((rightNum + errorNum) != 0) {
                int percentage = 100 * rightNum / (rightNum + errorNum);
                knowledgePointData.add(
                        MapUtils.m(
                                "name", newKnowledgePoint.getName(),
                                "knowledgePointId", kId,
                                "questionNum", getKnowledgePointIdToQIds().get(kId).size(),
                                "rightNum", d.get("rightNum"),
                                "errorNum", d.get("errorNum"),
                                "percentage", percentage
                        ));
            } else {
                removeIds.add(kId);
            }

        }
        if (CollectionUtils.isNotEmpty(removeIds)) {
            removeIds.forEach(o -> getKnowledgePointIdToQIds().remove(o));
        }
    }


    public void finishTypeQuestionsInfoResult() {
        for (ObjectiveConfigType o : typeQuestionsInfo.keySet()) {
            Map<String, Map<String, Object>> kl = typeQuestionsInfo.get(o);
            Set<String> removeIds = new HashSet<>();
            for (String quesId : kl.keySet()) {
                Map<String, Object> da = kl.get(quesId);
                Integer sumNum = SafeConverter.toInt(da.get("sumNum"));
                if (o == ObjectiveConfigType.ORAL_PRACTICE) {
                    double score = SafeConverter.toDouble(da.get("score"));
                    if (sumNum != 0) {
                        int averageScore = new BigDecimal(score)
                                .divide(new BigDecimal(sumNum), 0, BigDecimal.ROUND_UP)
                                .intValue();
                        da.put("proportion", averageScore);//保留使用参数proportion,为了其他类型保持一致
                    } else {
                        removeIds.add(quesId);
                    }
                } else {
                    Integer correctNum = SafeConverter.toInt(da.get("correctNum"));
                    if (sumNum != 0) {
                        int proportion = new BigDecimal(100 * correctNum)
                                .divide(new BigDecimal(sumNum), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue();
                        da.put("proportion", proportion);
                    } else {
                        removeIds.add(quesId);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(removeIds)) {
                removeIds.forEach(kl::remove);
            }
        }
    }


    public List<Map<String, Object>> handleSelfStudyHomeworkReport() {

        Map<QuestionWrongReason, Integer> questionWrongReasonIntegerMap = new LinkedHashMap<>();
        int totalNum = 0;
        for (QuestionWrongReason questionWrongReason : new QuestionWrongReason[]{QuestionWrongReason.Misread,
                QuestionWrongReason.Mistake,
                QuestionWrongReason.Missing,
                QuestionWrongReason.Other}) {
            questionWrongReasonIntegerMap.put(questionWrongReason, 0);
        }

        if (MapUtils.isNotEmpty(selfStudyHomeworkReportMap)) {
            for (SelfStudyHomeworkReport selfStudyHomeworkReport : selfStudyHomeworkReportMap.values()) {
                if (selfStudyHomeworkReport != null && MapUtils.isNotEmpty(selfStudyHomeworkReport.getPractices())) {

                    for (LinkedHashMap<String, SelfStudyHomeworkReportQuestion> stringSelfStudyHomeworkReportQuestionLinkedHashMap : selfStudyHomeworkReport.getPractices().values()) {
                        if (MapUtils.isNotEmpty(stringSelfStudyHomeworkReportQuestionLinkedHashMap)) {
                            for (SelfStudyHomeworkReportQuestion selfStudyHomeworkReportQuestion : stringSelfStudyHomeworkReportQuestionLinkedHashMap.values()) {
                                totalNum++;
                                questionWrongReasonIntegerMap.put(selfStudyHomeworkReportQuestion.getWrongReason(), SafeConverter.toInt(questionWrongReasonIntegerMap.get(selfStudyHomeworkReportQuestion.getWrongReason())) + 1);
                            }
                        }
                    }
                }
            }
        }

        int misreadNum = questionWrongReasonIntegerMap.get(QuestionWrongReason.Misread);
        int mistakeNum = questionWrongReasonIntegerMap.get(QuestionWrongReason.Mistake);
        int missingNum = questionWrongReasonIntegerMap.get(QuestionWrongReason.Missing);

        return statisticsError(totalNum, misreadNum,
                mistakeNum,
                missingNum
        );


    }


    //个人错题统计
    public void handleCorrect() {
        if (isShowCorrect()) {
            String s = getNewHomework().getId() + "_" + getNewHomeworkResult().getUserId();
            if (getRefMap().containsKey(s)) {
                HomeworkSelfStudyRef homeworkSelfStudyRef = getRefMap().get(s);
                setHomeworkSelfStudyRef(homeworkSelfStudyRef);
                if (StringUtils.isBlank(homeworkSelfStudyRef.getSelfStudyId())) {
                    getStudentReport().put("avgCorrectedScoreInfo", "未订正");
                } else {
                    SelfStudyHomeworkReport selfStudyHomeworkReport = getSelfStudyHomeworkReportMap().get(homeworkSelfStudyRef.getSelfStudyId());
                    if (selfStudyHomeworkReport != null) {
                        String correctInfo = "100%";
                        if (selfStudyHomeworkReport.getPractices() != null) {
                            setSelfStudyHomeworkReport(selfStudyHomeworkReport);
                            List<SelfStudyHomeworkReportQuestion> selfStudyHomeworkReportQuestions = new LinkedList<>();
                            for (LinkedHashMap<String, SelfStudyHomeworkReportQuestion> sh : selfStudyHomeworkReport.getPractices().values()) {
                                selfStudyHomeworkReportQuestions.addAll(sh.values());
                            }
                            if (selfStudyHomeworkReportQuestions.size() != 0) {
                                long num = selfStudyHomeworkReportQuestions.stream()
                                        .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                        .count();
                                correctInfo = new BigDecimal(num * 100).divide(new BigDecimal(selfStudyHomeworkReportQuestions.size()), BigDecimal.ROUND_HALF_UP, 0).intValue() + "%";
                            }
                        }
                        getStudentReport().put("avgCorrectedScoreInfo", correctInfo);
                    } else {
                        getStudentReport().put("avgCorrectedScoreInfo", "未订正");
                    }
                }
            } else {
                getStudentReport().put("avgCorrectedScoreInfo", "无需");
            }
        }
    }


    //完成类型人数
    public void handleHomeworkTypeFinishCount(ObjectiveConfigType type) {
        if (getHomeworkTypeFinishCount().get(type) == null) {
            getHomeworkTypeFinishCount().put(type, 1);
        } else {
            getHomeworkTypeFinishCount().put(type, getHomeworkTypeFinishCount().get(type) + 1);
        }
    }

    //完成时间类型
    public void handleHomeworkTypeDuration(ObjectiveConfigType type, Integer typeDuration) {
        if (getHomeworkTypeDuration().get(type) == null) {
            getHomeworkTypeDuration().put(type, typeDuration);
        } else {
            getHomeworkTypeDuration().put(type, getHomeworkTypeDuration().get(type) + typeDuration);
        }
    }


    //数学口算知识点汇聚
    public void initKnowledgePointIdToQIds() {

        List<NewHomeworkQuestion> mentalQuestion = newHomework.findNewHomeworkQuestions(ObjectiveConfigType.MENTAL);
        getKnowledgePointIdToQIds().putAll(mentalQuestion
                .stream()
                .filter(o -> StringUtils.isNotEmpty(o.getKnowledgePointId()))
                .collect(Collectors.groupingBy(
                        NewHomeworkQuestion::getKnowledgePointId,
                        Collectors.mapping(NewHomeworkQuestion::getQuestionId, Collectors.toList())
                )));
        getKnowledgePointInfo().putAll(getKnowledgePointIdToQIds()
                .keySet()
                .stream()
                .collect(Collectors.toMap(o -> o, o ->
                        MapUtils.m(
                                "rightNum", 0,
                                "errorNum", 0
                        ))));

    }


    //exam or quiz 每题的信息初始化
    public void initTypeQuestionsInfo() {
        Map<ObjectiveConfigType, Map<String, Map<String, Object>>> typeQuestionsInfo = getTypeQuestionsInfo();
        if (CollectionUtils.isNotEmpty(newHomework.getPractices())) {
            typeQuestionsInfo.putAll(newHomework.getPractices()
                    .stream()
                    .filter(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().isQuiz() || EXAM_TEMPLATE_TYPES.contains(newHomeworkPracticeContent.getType()))
                    .filter(newHomeworkPracticeContent -> CollectionUtils.isNotEmpty(newHomework.findNewHomeworkQuestions(newHomeworkPracticeContent.getType())))
                    .collect(Collectors.toMap(NewHomeworkPracticeContent::getType,
                            newHomeworkPracticeContent -> {
                                List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkQuestions(newHomeworkPracticeContent.getType());
                                return newHomeworkQuestions
                                        .stream()
                                        .collect(Collectors
                                                .toMap(NewHomeworkQuestion::getQuestionId, o ->
                                                        MapUtils.m(
                                                                "sumNum", 0,
                                                                "correctNum", 0,
                                                                "questionId", o.getQuestionId(),
                                                                "position", newHomeworkQuestions.indexOf(o) + 1
                                                        )));
                            })

                    ));
        }
    }

    //处理知识点

    public void handleKnowledgePointStatistics(NewHomeworkResultAnswer newHomeworkResultAnswer) {
        if (MapUtils.isNotEmpty(knowledgePointIdToQIds) &&
                MapUtils.isNotEmpty(newHomeworkResultAnswer.getAnswers())) {
            for (String knowledgePointId : knowledgePointIdToQIds.keySet()) {
                LinkedHashMap<String, String> answers = newHomeworkResultAnswer.getAnswers();
                List<String> qIds = knowledgePointIdToQIds.get(knowledgePointId);
                Map<String, Object> d = knowledgePointInfo.get(knowledgePointId);
                qIds.stream()
                        .filter(answers::containsKey)
                        .forEach(qId -> {
                            NewHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(answers.get(qId));
                            if (newHomeworkProcessResult != null && SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                                d.put("rightNum", SafeConverter.toInt(d.get("rightNum")) + 1);
                            } else {
                                d.put("errorNum", SafeConverter.toInt(d.get("errorNum")) + 1);
                            }
                        });
            }
        }
    }

    /**
     * 处理批改
     */
    public void handleNewCorrectedCount() {
        if (newHomeworkResult.isCorrected()) {
            setNewCorrectedCount(getNewCorrectedCount() + 1);
        }
    }


    /**
     * 处理批改
     */
    public void handleNewNeedCorrectCount() {
        if (MapUtils.isNotEmpty(newHomeworkResult.getPractices())) {
            List<String> processIds = newHomeworkResult.findAllHomeworkProcessIds(true);
            if (processIds
                    .stream()
                    .anyMatch(processId -> newHomeworkProcessResultMap.containsKey(processId)
                            && CollectionUtils.isNotEmpty(newHomeworkProcessResultMap.get(processId).getFiles()))) {
                newNeedCorrectCount = newNeedCorrectCount + 1;
            }
        }
    }


    public void handleTypeQuestionsInfoStatistics(ObjectiveConfigType type, NewHomeworkResultAnswer newHomeworkResultAnswer) {
        Map<ObjectiveConfigType, Map<String, Map<String, Object>>> typeQuestionsInfo = getTypeQuestionsInfo();
        LinkedHashMap<String, String> answers = newHomeworkResultAnswer.processAnswers();
        if (MapUtils.isEmpty(answers))
            return;
        Map<String, Map<String, Object>> questionsData = typeQuestionsInfo.get(type);// key : questionId ; value : data of map
        questionsData
                .keySet()
                .stream()
                .filter(answers::containsKey)
                .forEach(key -> {
                    NewHomeworkProcessResult re = newHomeworkProcessResultMap.get(answers.get(key));
                    if (re != null) {
                        Map<String, Object> questionData = questionsData.get(key);
                        questionData.put("sumNum", SafeConverter.toInt(questionData.get("sumNum")) + 1);
                        if (type == ObjectiveConfigType.ORAL_PRACTICE) {
                            questionData.put("score", SafeConverter.toDouble(questionData.get("score")) + SafeConverter.toDouble(re.getScore()));
                        } else {
                            if (Objects.equals(Boolean.TRUE, re.getGrasp())) {
                                questionData.put("correctNum", SafeConverter.toInt(questionData.get("correctNum")) + 1);
                            }
                        }
                    }
                });

    }


    public int handleNewTotalScore() {
        double basicData = getNewTotalScore();
        int finishCount = getNewFinishCount();
        return basicDataProcessing(basicData, finishCount);
    }


    public int handleNewTotalFinishTime() {
        double basicData = getNewTotalFinishTime();
        int finishCount = getNewFinishCount();
        return basicDataProcessing(basicData, finishCount);
    }


    private int basicDataProcessing(double basicData, int finishCount) {
        return finishCount > 0 ?
                new BigDecimal(basicData)
                        .divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;
    }


    public List<Map<String, Object>> processTypeReport() {
        List<Map<String, Object>> typeReports = new ArrayList<>();
        List<Map<String, Object>> _knowledgePointData;
        for (ObjectiveConfigType oct : homeworkTypes) {
            _knowledgePointData = (oct != ObjectiveConfigType.MENTAL ? null : knowledgePointData);
            int avgScore = 0;
            long avgDuration = 0;
            Integer finishCount = homeworkTypeFinishCount.getOrDefault(oct, 0);
            Integer unCorrectCount = homeworkTypeUnCorrect.getOrDefault(oct, 0);
            if (homeworkTypeScore.containsKey(oct) && finishCount > 0) {
                avgScore = new BigDecimal(homeworkTypeScore.get(oct))
                        .divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue();
            }
            if (homeworkTypeDuration.containsKey(oct) && finishCount > 0) {
                avgDuration = new BigDecimal(homeworkTypeDuration.get(oct))
                        .divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue();
            }
            if (typeQuestionsInfo.containsKey(oct)) {
                // 分为两类 一类是要求显示每题的信息，一类是不需要显示每题的信息

                List<Map<String, Object>> questionsInformation = typeQuestionsInfo.get(oct)
                        .values()
                        .stream()
                        .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o1.get("position")), SafeConverter.toInt(o2.get("position"))))
                        .collect(Collectors.toList());

                typeReports.add(
                        MapUtils.m("type", oct.name(),
                                "typeName", oct.getValue(),
                                "joinCount", newJoinCount,
                                "finishCorrectedCount", 0,
                                "totalNeedCorrectedNum", 0,
                                "knowledgePointData", _knowledgePointData,
                                "wrongReasonInformation", new LinkedList<>(),
                                "avgScore", avgScore,
                                "questionsInfo", questionsInformation,
                                "avgDuration", avgDuration,
                                "finishCount", finishCount,
                                "unCorrectCount", unCorrectCount));
            } else {
                List<Map<String, Object>> baseAppInfo = baseAppType.getOrDefault(oct, null);

                typeReports.add(
                        MapUtils.m("type", oct.name(),
                                "joinCount", newJoinCount,
                                "typeName", oct.getValue(),
                                "baseAppInformation", baseAppInfo,
                                "avgScore", avgScore,
                                "questionsInfo", null,
                                "knowledgePointData", _knowledgePointData,
                                "avgDuration", avgDuration,
                                "finishCount", finishCount,
                                "unCorrectCount", unCorrectCount));
            }

        }
        return typeReports;
    }


    //错题原因分析
    public static List<Map<String, Object>> statisticsError(int correctNum,
                                                      int misreadNum,
                                                      int mistakeNum,
                                                      int missingNum) {
        Integer misreadProportion = (correctNum == 0) ? 0 : misreadNum * 100 / correctNum;
        Integer mistakeNumProportion = (correctNum == 0) ? 0 : mistakeNum * 100 / correctNum;
        Integer missingNumProportion = (correctNum == 0) ? 0 : missingNum * 100 / correctNum;
        Map<QuestionWrongReason, Integer> map = new LinkedHashMap<>();
        map.put(QuestionWrongReason.Misread, misreadProportion);
        map.put(QuestionWrongReason.Mistake, mistakeNumProportion);
        map.put(QuestionWrongReason.Missing, missingNumProportion);
        map.put(QuestionWrongReason.Other, correctNum == 0 ? 0 : 100 - misreadProportion - mistakeNumProportion - missingNumProportion);
        return map.entrySet()
                .stream()
                .map(entry -> MapUtils.m("proportion", entry.getValue() + "%", "desc", entry.getKey().getDesc()))
                .collect(Collectors.toList());
    }


}
