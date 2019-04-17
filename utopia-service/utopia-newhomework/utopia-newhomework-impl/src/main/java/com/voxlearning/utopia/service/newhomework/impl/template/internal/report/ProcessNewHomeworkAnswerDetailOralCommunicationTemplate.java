package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationClazzLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.OralCommunicationAppPart;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.KeySentence;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.KeyWord;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/12/6
 * \* Time: 7:54 PM
 * \* Description: 口语交际
 * \
 */
@Named
public class ProcessNewHomeworkAnswerDetailOralCommunicationTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_COMMUNICATION;
    }

    /**
     * 处理个人答题详情
     *
     * @param reportRateContext
     */
    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        List<Map<String, Object>> studentAchievement = new LinkedList<>();
        int totalApps = 0;
        int finishedAppNum = 0;
        Long totalDuration = 0L;
        Double totalScore = 0d;
        NewHomeworkPracticeContent newHomeworkPracticeContent = reportRateContext
                .getNewHomework()
                .findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.ORAL_COMMUNICATION);
        if (newHomeworkPracticeContent == null
                || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())
                || reportRateContext.getNewHomeworkResult() == null
                || MapUtils.isEmpty(reportRateContext.getNewHomeworkResult().getPractices())
                || reportRateContext.getNewHomeworkResult().getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION) == null
                || MapUtils.isEmpty(reportRateContext.getNewHomeworkResult().getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION).getAppAnswers())
                ) {
            reportRateContext.getResultMap().put(ObjectiveConfigType.ORAL_COMMUNICATION, MapUtils.m(
                    "avgDuration", 0,
                    "avgScore", 0,
                    "totalApps", totalApps,
                    "finishedAppNum", finishedAppNum,
                    "finished", totalApps == finishedAppNum,
                    "studentAchievement", studentAchievement
            ));
            return;
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext
                .getNewHomeworkResult()
                .getPractices()
                .get(ObjectiveConfigType.ORAL_COMMUNICATION);
        finishedAppNum = newHomeworkResultAnswer.getAppAnswers().size();
        List<String> stoneIds = newHomeworkPracticeContent
                .getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkApp::getStoneDataId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stoneIds)) {
            reportRateContext.getResultMap().put(ObjectiveConfigType.ORAL_COMMUNICATION, MapUtils.m(
                    "avgDuration", 0,
                    "avgScore", 0,
                    "totalApps", totalApps,
                    "finishedAppNum", finishedAppNum,
                    "finished", totalApps == finishedAppNum,
                    "studentAchievement", studentAchievement
            ));
            return;
        }
        totalApps = stoneIds.size();
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            reportRateContext.getResultMap().put(ObjectiveConfigType.ORAL_COMMUNICATION, MapUtils.m(
                    "avgDuration", 0,
                    "avgScore", 0,
                    "totalApps", totalApps,
                    "finishedAppNum", finishedAppNum,
                    "finished", totalApps == finishedAppNum,
                    "studentAchievement", studentAchievement
            ));
            return;
        }
        Map<String, StoneBufferedData> stoneDataMap = stoneBufferedDataList.stream().collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));
        for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
            Map<String, Object> stoneResultMap = Maps.newLinkedHashMap();
            String stoneId = app.getStoneDataId();
            stoneResultMap.put("stoneId", stoneId);
            StoneBufferedData stoneBufferedData = stoneDataMap.get(stoneId);
            if (stoneBufferedData == null) {
                continue;
            }
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                    .getAppAnswers()
                    .get(stoneId);
            if (newHomeworkResultAppAnswer == null) {
                continue;
            }
            //计算时长
            if (newHomeworkResultAppAnswer.getDuration() != null) {
                int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                stoneResultMap.put("duration", duration);
                totalDuration += duration;
            } else {
                stoneResultMap.put("duration", 0);
            }
            //计算分数
            if (newHomeworkResultAppAnswer.getScore() != null) {
                int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                stoneResultMap.put("score", score);
                totalScore += newHomeworkResultAppAnswer.getScore();
            } else {
                stoneResultMap.put("score", 0);
            }
            if (stoneBufferedData.getOralPracticeConversion() != null) {
                stoneResultMap.put("topicName", StringUtils.isNotEmpty(stoneBufferedData.getOralPracticeConversion().getTopicTrans())
                        ? stoneBufferedData.getOralPracticeConversion().getTopicTrans()
                        : stoneBufferedData.getOralPracticeConversion().getTopicName());
                List<String> sentences = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(stoneBufferedData.getOralPracticeConversion().getKeyWords())) {
                    stoneBufferedData.getOralPracticeConversion().getKeyWords().stream().map(KeyWord::getText).forEach(sentences::add);
                }
                if (CollectionUtils.isNotEmpty(stoneBufferedData.getOralPracticeConversion().getKeySentences())) {
                    stoneBufferedData.getOralPracticeConversion().getKeySentences().stream().map(KeySentence::getText).forEach(sentences::add);
                }
                stoneResultMap.put("sentences", sentences);
            }
            if (stoneBufferedData.getInteractiveVideo() != null) {
                stoneResultMap.put("topicName", StringUtils.isNotEmpty(stoneBufferedData.getInteractiveVideo().getTopicTrans())
                        ? stoneBufferedData.getInteractiveVideo().getTopicTrans()
                        : stoneBufferedData.getInteractiveVideo().getTopicName());
                List<String> sentences = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(stoneBufferedData.getInteractiveVideo().getKeyWords())) {
                    stoneBufferedData.getInteractiveVideo().getKeyWords().stream().map(KeyWord::getText).forEach(sentences::add);
                }
                if (CollectionUtils.isNotEmpty(stoneBufferedData.getInteractiveVideo().getKeySentences())) {
                    stoneBufferedData.getInteractiveVideo().getKeySentences().stream().map(KeySentence::getText).forEach(sentences::add);
                }
                stoneResultMap.put("sentences", sentences);
            }
            if (stoneBufferedData.getInteractivePictureBook() != null) {
                stoneResultMap.put("topicName", StringUtils.isNotEmpty(stoneBufferedData.getInteractivePictureBook().getTopicTrans())
                        ? stoneBufferedData.getInteractivePictureBook().getTopicTrans()
                        : stoneBufferedData.getInteractivePictureBook().getTopicName());
                List<String> sentences = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(stoneBufferedData.getInteractivePictureBook().getKeyWords())) {
                    stoneBufferedData.getInteractivePictureBook().getKeyWords().stream().map(KeyWord::getText).forEach(sentences::add);
                }
                if (CollectionUtils.isNotEmpty(stoneBufferedData.getInteractivePictureBook().getKeySentences())) {
                    stoneBufferedData.getInteractivePictureBook().getKeySentences().stream().map(KeySentence::getText).forEach(sentences::add);
                }
                stoneResultMap.put("sentences", sentences);
            }
            studentAchievement.add(stoneResultMap);
        }
        int avgDuration = finishedAppNum > 0
                ? new BigDecimal(totalDuration)
                .divide(new BigDecimal(finishedAppNum), 0, BigDecimal.ROUND_HALF_UP)
                .intValue()
                : 0;
        int avgScore = finishedAppNum > 0
                ? new BigDecimal(totalScore)
                .divide(new BigDecimal(finishedAppNum), 0, BigDecimal.ROUND_HALF_UP)
                .intValue()
                : 0;
        reportRateContext.getResultMap()
                .put(ObjectiveConfigType.ORAL_COMMUNICATION, MapUtils.m(
                        "avgDuration", avgDuration,
                        "avgScore", avgScore,
                        "totalApps", totalApps,
                        "finishedAppNum", finishedAppNum,
                        "finished", totalApps == finishedAppNum,
                        "studentAchievement", studentAchievement
                ));

    }

    /**
     * 题目正确率详情
     *
     * @param context
     */
    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {
        Map<Long, User> userMap = context.getUserMap();
        ObjectiveConfigType type = context.getType();
        ObjectiveConfigTypeParameter parameter = context.getParameter();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        String stoneId = parameter.getStoneId();
        if (StringUtils.isEmpty(stoneId)) {
            MapMessage mapMessage = MapMessage.errorMessage("题包数据不错存在");
            context.setMapMessage(mapMessage);
            return;
        }
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(stoneId));
        if (CollectionUtils.isEmpty(stoneBufferedDataList) || stoneBufferedDataList.get(0) == null) {
            MapMessage mapMessage = MapMessage.errorMessage("题包数据不错存在");
            context.setMapMessage(mapMessage);
            return;
        }
        StoneBufferedData stoneBufferedData = stoneBufferedDataList.get(0);
        OralCommunicationAppPart appPartResult = new OralCommunicationAppPart();
        appPartResult.setStoneId(stoneId);
        if (stoneBufferedData.getOralPracticeConversion() != null) {
            appPartResult.setTopicName(
                    StringUtils.isNotEmpty(stoneBufferedData.getOralPracticeConversion().getTopicTrans())
                            ? stoneBufferedData.getOralPracticeConversion().getTopicTrans()
                            : stoneBufferedData.getOralPracticeConversion().getTopicName()
            );
        }
        if (stoneBufferedData.getInteractiveVideo() != null) {
            appPartResult.setTopicName(
                    StringUtils.isNotEmpty(stoneBufferedData.getInteractiveVideo().getTopicTrans())
                            ? stoneBufferedData.getInteractiveVideo().getTopicTrans()
                            : stoneBufferedData.getInteractiveVideo().getTopicName()
            );
        }
        if (stoneBufferedData.getInteractivePictureBook() != null) {
            appPartResult.setTopicName(
                    StringUtils.isNotEmpty(stoneBufferedData.getInteractivePictureBook().getTopicTrans())
                            ? stoneBufferedData.getInteractivePictureBook().getTopicTrans()
                            : stoneBufferedData.getInteractivePictureBook().getTopicName()
            );
        }
        List<OralCommunicationAppPart.OralCommunicationAppUser> userList = Lists.newArrayList();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            if (MapUtils.isEmpty(newHomeworkResult.getPractices())) {
                continue;
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer == null
                    || MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())
                    || newHomeworkResultAnswer.getAppAnswers().get(stoneId) == null) {
                continue;
            }
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                    .getAppAnswers()
                    .get(stoneId);
            Date appFinishedAt = newHomeworkResultAppAnswer.getFinishAt();
            OralCommunicationAppPart.OralCommunicationAppUser user = new OralCommunicationAppPart.OralCommunicationAppUser();
            Long userId = newHomeworkResult.getUserId();
            user.setUserId(userId);
            if (MapUtils.isNotEmpty(userMap) && userMap.get(userId) != null) {
                user.setUserName(userMap.get(userId).fetchRealname());
            }
            int time = 0;
            if (newHomeworkResultAppAnswer.processDuration() != null) {
                time = new BigDecimal(SafeConverter.toLong(newHomeworkResultAppAnswer.processDuration()))
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
            }
            String duration = NewHomeworkUtils.handlerEnTime(time);
            user.setDurationTime(time);
            user.setDuration(duration);
            user.setFinishedAt(appFinishedAt);
            int score = 0;
            if (newHomeworkResultAppAnswer.getScore() != null) {
                score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            }
            user.setScore(score);
            user.setScoreStr(score + "分");
            userList.add(user);
        }
        Comparator<OralCommunicationAppPart.OralCommunicationAppUser> comparator = (e1, e2) -> e2.getScore().compareTo(e1.getScore());
        comparator = comparator
                .thenComparing((e1, e2) -> e2.getDurationTime().compareTo(e1.getDurationTime()));
        userList = userList.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getDurationTime() != null)
                .filter(e -> e.getFinishedAt() != null)
                .sorted(comparator)
                .collect(Collectors.toList());
        appPartResult.setUsers(userList);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.set("oralCommunicationAppPart", appPartResult);
        context.setMapMessage(mapMessage);
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
            typeResult.put("avgScore", 0);
            typeResult.put("avgDuration", 0);
            result.put(type, typeResult);
            return;
        }
        int totalDuration = 0;
        Double totalScore = 0d;
        int finishCount = 0;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            if (MapUtils.isEmpty(newHomeworkResult.getPractices()) || newHomeworkResult.getPractices().get(type) == null) {
                continue;
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer duration;
            if (newHomeworkResultAnswer.processDuration() != null) {
                duration = newHomeworkResultAnswer.processDuration();
                totalDuration += SafeConverter.toInt(new BigDecimal(SafeConverter.toInt(duration)).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue());
            }
            if (newHomeworkResultAnswer.getScore() != null) {
                totalScore += newHomeworkResultAnswer.getScore();
            }
            finishCount++;
        }
        if (finishCount == 0) {
            typeResult.put("avgScore", 0);
            typeResult.put("avgDuration", 0);
            result.put(type, typeResult);
            return;
        }
        int avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_UP).intValue();
        int avgScore = new BigDecimal(totalScore)
                .divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        typeResult.put("finishCount", finishCount);
        typeResult.put("avgScore", avgScore);
        typeResult.put("avgDuration", avgDuration);
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        result.put(type, typeResult);
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        Map<String, Object> stoneReport = new LinkedHashMap<>();
        Map<String, List<Map<String, Object>>> studentsInfoMap = new LinkedHashMap<>();
        List<Map<String, Object>> stoneInfoList = new LinkedList<>();
        Map<String, NewHomeworkResult> newHomeworkResultFinishedMap = handlerNewHomeworkResultMap(
                reportRateContext.getNewHomeworkResultMap(),
                ObjectiveConfigType.ORAL_COMMUNICATION
        );
        if (MapUtils.isEmpty(newHomeworkResultFinishedMap)) {
            return;
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = reportRateContext
                .getNewHomework()
                .findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.ORAL_COMMUNICATION);
        if (newHomeworkPracticeContent == null || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
            stoneReport.put("stoneInfoList", stoneInfoList);
            stoneReport.put("studentInfo", studentsInfoMap);
            reportRateContext.getResult().put(ObjectiveConfigType.ORAL_COMMUNICATION.name(), stoneReport);
            return;
        }
        List<String> stoneIds = newHomeworkPracticeContent.getApps().stream().map(NewHomeworkApp::getStoneDataId).collect(Collectors.toList());
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            stoneReport.put("stoneInfoList", stoneInfoList);
            stoneReport.put("studentInfo", studentsInfoMap);
            reportRateContext.getResult().put(ObjectiveConfigType.ORAL_COMMUNICATION.name(), stoneReport);
            return;
        }
        Map<String, StoneBufferedData> stoneSourceDataMap = stoneBufferedDataList
                .stream()
                .collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));
        for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
            Map<String, Object> stoneInfo = new LinkedHashMap<>();
            String stoneId = newHomeworkApp.getStoneDataId();
            if (stoneSourceDataMap.get(stoneId) == null) {
                continue;
            }
            stoneInfo.put("stoneId", stoneId);
            if (stoneSourceDataMap.get(stoneId).getOralPracticeConversion() != null) {
                stoneInfo.put("topicName", StringUtils.isNotEmpty(stoneSourceDataMap.get(stoneId).getOralPracticeConversion().getTopicTrans())
                        ? stoneSourceDataMap.get(stoneId).getOralPracticeConversion().getTopicTrans()
                        : stoneSourceDataMap.get(stoneId).getOralPracticeConversion().getTopicName()
                );
                stoneInfo.put("thumbUrl", stoneSourceDataMap.get(stoneId).getOralPracticeConversion().getThumbUrl());
                OralCommunicationClazzLevel communicationClazzLevel = OralCommunicationClazzLevel.ofGrade(stoneSourceDataMap.get(stoneId).getOralPracticeConversion().getGrade());
                stoneInfo.put("classLevelName", communicationClazzLevel == null ||
                        StringUtils.isEmpty(communicationClazzLevel.getName()) ? ""
                        : communicationClazzLevel.getName());
                List<String> sentences = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(stoneSourceDataMap.get(stoneId).getOralPracticeConversion().getKeySentences())) {
                    stoneSourceDataMap.get(stoneId).getOralPracticeConversion().getKeySentences().stream().map(KeySentence::getText).forEach(sentences::add);
                }
                stoneInfo.put("sentences", sentences);
            }
            if (stoneSourceDataMap.get(stoneId).getInteractiveVideo() != null) {
                stoneInfo.put("topicName", StringUtils.isNotEmpty(stoneSourceDataMap.get(stoneId).getInteractiveVideo().getTopicTrans())
                        ? stoneSourceDataMap.get(stoneId).getInteractiveVideo().getTopicTrans()
                        : stoneSourceDataMap.get(stoneId).getInteractiveVideo().getTopicName());
                stoneInfo.put("thumbUrl", stoneSourceDataMap.get(stoneId).getInteractiveVideo().getThumbUrl());
                OralCommunicationClazzLevel communicationClazzLevel = OralCommunicationClazzLevel.ofGrade(stoneSourceDataMap.get(stoneId).getInteractiveVideo().getGrade());
                stoneInfo.put("classLevelName", communicationClazzLevel == null ||
                        StringUtils.isEmpty(communicationClazzLevel.getName()) ? ""
                        : communicationClazzLevel.getName());
                List<String> sentences = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(stoneSourceDataMap.get(stoneId).getInteractiveVideo().getKeySentences())) {
                    stoneSourceDataMap.get(stoneId).getInteractiveVideo().getKeySentences().stream().map(KeySentence::getText).forEach(sentences::add);
                }
                stoneInfo.put("sentences", sentences);
            }
            if (stoneSourceDataMap.get(stoneId).getInteractivePictureBook() != null) {
                stoneInfo.put("topicName", StringUtils.isNotEmpty(stoneSourceDataMap.get(stoneId).getInteractivePictureBook().getTopicTrans())
                        ? stoneSourceDataMap.get(stoneId).getInteractivePictureBook().getTopicTrans()
                        : stoneSourceDataMap.get(stoneId).getInteractivePictureBook().getTopicName());
                stoneInfo.put("thumbUrl", stoneSourceDataMap.get(stoneId).getInteractivePictureBook().getThumbUrl());
                OralCommunicationClazzLevel communicationClazzLevel = OralCommunicationClazzLevel.ofGrade(stoneSourceDataMap.get(stoneId).getInteractivePictureBook().getGrade());
                stoneInfo.put("classLevelName", communicationClazzLevel == null ||
                        StringUtils.isEmpty(communicationClazzLevel.getName()) ? ""
                        : communicationClazzLevel.getName());
                List<String> sentences = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(stoneSourceDataMap.get(stoneId).getInteractivePictureBook().getKeySentences())) {
                    stoneSourceDataMap.get(stoneId).getInteractivePictureBook().getKeySentences().stream().map(KeySentence::getText).forEach(sentences::add);
                }
                stoneInfo.put("sentences", sentences);
            }
            List<Map<String, Object>> studentInfo = new LinkedList<>();
            int finishedNum = 0;
            Long totalDuration = 0L;
            Double totalScore = 0d;
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultFinishedMap.values()) {
                if (MapUtils.isEmpty(newHomeworkResult.getPractices())
                        || newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION) == null
                        || MapUtils.isEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION).getAppAnswers())) {
                    continue;
                }
                Map<String, Object> studentAchievement = new LinkedHashMap<>();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResult
                        .getPractices()
                        .get(ObjectiveConfigType.ORAL_COMMUNICATION)
                        .getAppAnswers()
                        .get(stoneId);
                User user = reportRateContext.getUserMap().get(newHomeworkResult.getUserId());
                studentAchievement.put("userId", newHomeworkResult.getUserId());
                studentAchievement.put("userName", user != null ? user.fetchRealname() : "");
                //计算时长
                if (newHomeworkResultAppAnswer.getDuration() != null) {
                    int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                            .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                            .intValue();
                    studentAchievement.put("duration", duration);
                    totalDuration += duration;
                } else {
                    studentAchievement.put("duration", 0);
                }
                //计算分数
                if (newHomeworkResultAppAnswer.getScore() != null) {
                    int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    studentAchievement.put("score", score);
                    totalScore += newHomeworkResultAppAnswer.getScore();
                } else {
                    studentAchievement.put("score", 0);
                }
                finishedNum++;
                studentAchievement.put("finishAt", newHomeworkResultAppAnswer.getFinishAt());
                studentInfo.add(studentAchievement);
            }
            if (finishedNum == 0) {
                stoneInfo.put("avgDuration", 0);
                stoneInfo.put("avgScore", 0);
                stoneInfo.put("finishedNum", 0);
                stoneInfo.put("totalUserNum", MapUtils.isNotEmpty(reportRateContext.getUserMap()) ? reportRateContext.getUserMap().keySet().size() : 0);
                stoneInfoList.add(stoneInfo);
                studentsInfoMap.put(stoneId, studentInfo);
                continue;
            }
            Comparator<Map<String, Object>> comparator = (e1, e2) -> Long.compare(SafeConverter.toLong(e2.get("score")), SafeConverter.toLong(e1.get("score")));
            comparator = comparator
                    .thenComparing((e1, e2) -> Long.compare(SafeConverter.toLong(e2.get("duration")), SafeConverter.toLong(e1.get("duration"))));
            studentInfo = studentInfo.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.get("duration") != null)
                    .filter(e -> e.get("finishAt") != null)
                    .sorted(comparator)
                    .collect(Collectors.toList());
            Long avgDuration = new BigDecimal(totalDuration > 0 ? totalDuration / finishedNum : 0)
                    .setScale(0, BigDecimal.ROUND_UP)
                    .longValue();
            int avgScore = new BigDecimal(totalScore)
                    .divide(new BigDecimal(finishedNum), 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
            stoneInfo.put("avgDuration", avgDuration);
            stoneInfo.put("avgScore", avgScore);
            stoneInfo.put("finishedNum", finishedNum);
            stoneInfo.put("totalUserNum", MapUtils.isNotEmpty(reportRateContext.getUserMap()) ? reportRateContext.getUserMap().keySet().size() : 0);
            stoneInfo.put("studentInfo", studentInfo);
            studentsInfoMap.put(stoneId, studentInfo);
            stoneInfoList.add(stoneInfo);
        }
        stoneReport.put("dubbingInfo", stoneInfoList);
        stoneReport.put("studentInfo", studentsInfoMap);
        reportRateContext.getResult().put(ObjectiveConfigType.ORAL_COMMUNICATION.name(), stoneReport);
    }
}
