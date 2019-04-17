package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.pc.KnowledgePointDetail;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailMentalTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.MENTAL;
    }

    @Override
    public String processStudentPartTypeScore(NewHomework newHomework, NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        //非主观题
        int score = SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
        //口算又是一个特殊的，需要显示时间
        int mentalDuration = SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
        int minutes = mentalDuration / 60;
        int second = mentalDuration % 60;
        String result;
        if (minutes == 0) {
            result = score + "分" + " (" + second + "\"" + ")";
        } else {
            result = score + "分" + " (" + minutes + "'" + second + "\"" + ")";
        }
        return result;
    }

    @Override
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            typeResult.put("finishCount", 0);
            result.put(type, typeResult);
            return;
        }
        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = newHomework.processSubHomeworkResultAnswerIdsByObjectConfigType(Collections.singleton(type));
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<NewHomeworkQuestion> newHomeworkQuestions = target.processNewHomeworkQuestion(false);
        Map<String, KnowledgePointDetail> knowledgePointDetailMap = new LinkedHashMap<>();
        List<KnowledgePointDetail> knowledgePointData = new LinkedList<>();
        Set<String> knowledgePointIds = new LinkedHashSet<>();
        Map<String, String> qidToKid = new LinkedHashMap<>();
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            String knowledgePointId = newHomeworkQuestion.getKnowledgePointId();
            if (StringUtils.isNotBlank(knowledgePointId)) {
                knowledgePointIds.add(knowledgePointId);
                qidToKid.put(newHomeworkQuestion.getQuestionId(), knowledgePointId);
            }
        }
        Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(knowledgePointIds);
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            String kid = newHomeworkQuestion.getKnowledgePointId();
            if (newKnowledgePointMap.containsKey(kid)) {
                if (knowledgePointDetailMap.containsKey(kid)) {
                    KnowledgePointDetail knowledgePointDetail = knowledgePointDetailMap.get(kid);
                    knowledgePointDetail.setQuestionNum(1 + knowledgePointDetail.getQuestionNum());
                } else {
                    NewKnowledgePoint newKnowledgePoint = newKnowledgePointMap.get(kid);
                    KnowledgePointDetail knowledgePointDetail = new KnowledgePointDetail();
                    knowledgePointDetail.setKnowledgePointId(kid);
                    knowledgePointDetail.setName(newKnowledgePoint.getName());
                    knowledgePointDetailMap.put(kid, knowledgePointDetail);
                    knowledgePointData.add(knowledgePointDetail);
                }
            }
        }
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        int finishCount = 0;
        int totalDuration = 0;
        int totalScore = 0;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            finishCount++;
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer duration = newHomeworkResultAnswer.processDuration();
            totalDuration += SafeConverter.toInt(new BigDecimal(SafeConverter.toInt(duration)).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue());
            totalScore += SafeConverter.toInt(SafeConverter.toInt(newHomeworkResultAnswer.processScore(type)));
            for (NewHomework.NewHomeworkQuestionObj obj : newHomeworkQuestionObjs) {
                subHomeworkResultAnswerIds.add(obj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        //平均分数和平均时长
        int avgScore = 0;
        int avgDuration = 0;
        if (finishCount != 0) {
            avgScore = new BigDecimal(totalScore).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
            avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_UP).intValue();
        }
        typeResult.put("finishCount", finishCount);
        typeResult.put("avgScore", avgScore);
        typeResult.put("avgDuration", avgDuration);


        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> processIds = new LinkedList<>();
        for (SubHomeworkResultAnswer subHomeworkResultAnswer : subHomeworkResultAnswerMap.values()) {
            processIds.add(subHomeworkResultAnswer.getProcessId());
        }
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
        for (NewHomeworkProcessResult newHomeworkProcessResult : newHomeworkProcessResultMap.values()) {
            if (qidToKid.containsKey(newHomeworkProcessResult.getQuestionId())) {
                String kid = qidToKid.get(newHomeworkProcessResult.getQuestionId());
                if (knowledgePointDetailMap.containsKey(kid)) {
                    KnowledgePointDetail knowledgePointDetail = knowledgePointDetailMap.get(kid);
                    knowledgePointDetail.setTotalNum(1 + knowledgePointDetail.getTotalNum());
                    if (SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                        knowledgePointDetail.setRightNum(1 + knowledgePointDetail.getRightNum());
                    }
                }
            }
        }
        //每个题的正确率
        knowledgePointData.stream()
                .filter(o -> o.getTotalNum() > 0)
                .filter(o -> o.getRightNum() > 0)
                .forEach(o -> {
                    int percentage = new BigDecimal(100 * o.getRightNum()).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setPercentage(percentage);
                });
        typeResult.put("knowledgePointData", knowledgePointData);
        result.put(type, typeResult);
    }
}
