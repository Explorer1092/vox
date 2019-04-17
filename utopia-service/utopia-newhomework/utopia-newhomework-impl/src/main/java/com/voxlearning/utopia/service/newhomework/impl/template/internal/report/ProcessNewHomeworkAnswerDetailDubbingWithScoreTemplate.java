package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
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
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.DubbingWithScoreAppPart;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.DubbingCategory;
import com.voxlearning.utopia.service.question.api.entity.DubbingTheme;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/21
 * \* Time: 下午3:11
 * \* Description:带分数的趣味配音处理模板
 * \
 */
@Named
public class ProcessNewHomeworkAnswerDetailDubbingWithScoreTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING_WITH_SCORE;
    }

    @Override
    public String processStudentPartTypeScore(NewHomework newHomework, NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        Integer score = newHomeworkResultAnswer.processScore(type);
        return SafeConverter.toInt(score) + "分";
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
                .findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING_WITH_SCORE);
        if (newHomeworkPracticeContent == null
                || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())
                || reportRateContext.getNewHomeworkResult() == null
                || MapUtils.isEmpty(reportRateContext.getNewHomeworkResult().getPractices())
                || reportRateContext.getNewHomeworkResult().getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE) == null
                || MapUtils.isEmpty(reportRateContext.getNewHomeworkResult().getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE).getAppAnswers())
                ) {
            reportRateContext.getResultMap().put(ObjectiveConfigType.DUBBING_WITH_SCORE, MapUtils.m(
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
                .get(ObjectiveConfigType.DUBBING_WITH_SCORE);
        finishedAppNum = newHomeworkResultAnswer.getAppAnswers().size();
        Map<String, String> didToHyidMap = newHomeworkPracticeContent
                .getApps()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(NewHomeworkApp::getDubbingId,
                        (NewHomeworkApp o) ->
                                new DubbingSyntheticHistory.ID(reportRateContext.getNewHomework().getId(), reportRateContext.getNewHomeworkResult().getUserId(), o.getDubbingId()).toString()));
        if (MapUtils.isNotEmpty(didToHyidMap)) {
            totalApps = didToHyidMap.size();
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(didToHyidMap.values());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(didToHyidMap.keySet());
        if (MapUtils.isEmpty(dubbingMap)) {
            reportRateContext.getResultMap().put(ObjectiveConfigType.DUBBING_WITH_SCORE, MapUtils.m(
                    "avgDuration", 0,
                    "avgScore", 0,
                    "totalApps", totalApps,
                    "finishedAppNum", finishedAppNum,
                    "finished", totalApps == finishedAppNum,
                    "studentAchievement", studentAchievement
            ));
            return;
        }
        Set<String> categoryIds = dubbingMap
                .values()
                .stream()
                .filter(Objects::nonNull)
                .map(Dubbing::getCategoryId)
                .collect(Collectors.toSet());
        Map<String, DubbingCategory> dubbingCategoryMap = dubbingLoaderClient.loadDubbingCategoriesByIds(categoryIds);
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
        for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
            Map<String, Object> dubbingInfoMap = new LinkedHashMap<>();
            String dubbingId = app.getDubbingId();
            Dubbing dubbing = dubbingMap.get(dubbingId);
            if (dubbing == null) {
                continue;
            }
            String categoryId = dubbing.getCategoryId();
            if (MapUtils.isNotEmpty(dubbingCategoryMap)) {
                DubbingCategory dubbingCategory = dubbingCategoryMap.get(categoryId);
                dubbingInfoMap.putAll(NewHomeworkContentDecorator.decorateDubbing(dubbing, dubbingCategory, null, null, null, ObjectiveConfigType.DUBBING_WITH_SCORE, dubbingThemeMap));
            }
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                    .getAppAnswers()
                    .get(dubbingId);
            if (newHomeworkResultAppAnswer == null) {
                continue;
            }
            //计算时长
            if (newHomeworkResultAppAnswer.getDuration() != null) {
                int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                dubbingInfoMap.put("duration", duration);
                totalDuration += duration;
            } else {
                dubbingInfoMap.put("duration", 0);
            }
            //计算分数
            if (newHomeworkResultAppAnswer.getScore() != null) {
                int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                dubbingInfoMap.put("score", score);
                totalScore += newHomeworkResultAppAnswer.getScore();
            } else {
                dubbingInfoMap.put("score", 0);
            }
            if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getVideoUrl())) {
                dubbingInfoMap.put("studentVideoUrl", newHomeworkResultAppAnswer.getVideoUrl());
                boolean syntheticSuccess = true;
                if (didToHyidMap.containsKey(dubbingId) && dubbingSyntheticHistoryMap.containsKey(didToHyidMap.get(dubbingId))) {
                    DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(didToHyidMap.get(dubbingId));
                    syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(reportRateContext.getNewHomework().getCreateAt()));
                }
                dubbingInfoMap.put("syntheticSuccess", syntheticSuccess);
            }
            studentAchievement.add(dubbingInfoMap);
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
                .put(ObjectiveConfigType.DUBBING_WITH_SCORE, MapUtils.m(
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
        String dubbingId = parameter.getDubbingId();
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(Collections.singleton(dubbingId));
        if (MapUtils.isEmpty(dubbingMap) || dubbingMap.get(dubbingId) == null) {
            MapMessage mapMessage = MapMessage.errorMessage("配音不存在");
            context.setMapMessage(mapMessage);
            return;
        }
        Dubbing dubbing = dubbingMap.get(dubbingId);
        DubbingWithScoreAppPart dubbingAppPart = new DubbingWithScoreAppPart();
        dubbingAppPart.setDubbingId(dubbingId);
        dubbingAppPart.setDubbingName(dubbing == null ? "" : dubbing.getVideoName());
        dubbingAppPart.setVideoUrl(dubbing == null ? "" : dubbing.getVideoUrl());
        if (MapUtils.isEmpty(newHomeworkResultMap)) {
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.set("dubbingAppPart", dubbingAppPart);
            context.setMapMessage(mapMessage);
            return;
        }
        List<DubbingWithScoreAppPart.DubbingScoreAppUser> users = Lists.newArrayList();
        Map<Long, String> userIdTohyidMap = new LinkedHashMap<>();
        String hid = context.getNewHomework().getId();
        for (NewHomeworkResult newHomeworkResult : context.getNewHomeworkResultMap().values()) {
            userIdTohyidMap.put(newHomeworkResult.getUserId(), new DubbingSyntheticHistory.ID(hid, newHomeworkResult.getUserId(), dubbingId).toString());
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(userIdTohyidMap.values());
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            if (MapUtils.isEmpty(newHomeworkResult.getPractices())) {
                continue;
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer == null
                    || MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())
                    || newHomeworkResultAnswer.getAppAnswers().get(dubbingId) == null) {
                continue;
            }
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                    .getAppAnswers()
                    .get(dubbingId);
            Date appFinishedAt = newHomeworkResultAppAnswer.getFinishAt();
            DubbingWithScoreAppPart.DubbingScoreAppUser user = new DubbingWithScoreAppPart.DubbingScoreAppUser();
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
            int score = 0;
            if (newHomeworkResultAppAnswer.getScore() != null) {
                score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            }
            user.setScore(score);
            user.setScoreStr(score + "分");
            user.setStudentVideoUrl(newHomeworkResultAppAnswer.getVideoUrl());
            user.setFinishedAt(appFinishedAt);
            if (userIdTohyidMap.containsKey(userId) && dubbingSyntheticHistoryMap.containsKey(userIdTohyidMap.get(userId))) {
                DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(userIdTohyidMap.get(userId));
                user.setSyntheticSuccess(SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(context.getNewHomework().getCreateAt())));
            }
            users.add(user);
        }
        // 排序：优先按照成绩由高到低进行排序；
        //      若成绩相同，则按照用时由长到短排列；
        Comparator<DubbingWithScoreAppPart.DubbingScoreAppUser> comparator = (e1, e2) -> e2.getScore().compareTo(e1.getScore());
        comparator = comparator
                .thenComparing((e1, e2) -> e2.getDurationTime().compareTo(e1.getDurationTime()))
                .thenComparing(Comparator.comparing(DubbingWithScoreAppPart.DubbingScoreAppUser::getFinishedAt));
        users = users.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getDurationTime() != null)
                .filter(e -> e.getFinishedAt() != null)
                .sorted(comparator)
                .collect(Collectors.toList());
        dubbingAppPart.setUsers(users);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.set("dubbingAppPart", dubbingAppPart);
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
        Map<String, Object> dubbingReport = new LinkedHashMap<>();
        List<Map<String, Object>> dubbingInfoList = new LinkedList<>();
        Map<String, List<Map<String, Object>>> studentsInfoMap = new LinkedHashMap<>();
        Map<String, NewHomeworkResult> newHomeworkResultFinishedMap = handlerNewHomeworkResultMap(
                reportRateContext.getNewHomeworkResultMap(),
                ObjectiveConfigType.DUBBING_WITH_SCORE
        );
        if (MapUtils.isEmpty(newHomeworkResultFinishedMap)) {
            return;
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = reportRateContext
                .getNewHomework()
                .findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING_WITH_SCORE);
        if (newHomeworkPracticeContent == null || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
            dubbingReport.put("dubbingInfo", dubbingInfoList);
            dubbingReport.put("studentInfo", studentsInfoMap);
            reportRateContext.getResult().put(ObjectiveConfigType.DUBBING_WITH_SCORE.name(), dubbingReport);
            return;
        }
        List<String> dubbingIds = newHomeworkPracticeContent
                .getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkApp::getDubbingId)
                .collect(Collectors.toList());
        List<String> hyids = new LinkedList<>();
        for (String dubbingId : dubbingIds) {
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultFinishedMap.values()) {
                hyids.add(new DubbingSyntheticHistory.ID(reportRateContext.getNewHomework().getId(), newHomeworkResult.getUserId(), dubbingId).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(hyids);
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
        Set<String> categoryIds = dubbingMap
                .values()
                .stream()
                .filter(Objects::nonNull)
                .map(Dubbing::getCategoryId)
                .collect(Collectors.toSet());
        Map<String, DubbingCategory> dubbingCategoryMap = dubbingLoaderClient.loadDubbingCategoriesByIds(categoryIds);
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
        for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
            Map<String, Object> dubbingInfo = new LinkedHashMap<>();
            String dubbingId = newHomeworkApp.getDubbingId();
            if (StringUtils.isBlank(dubbingId)) {
                continue;
            }
            Dubbing dubbing = dubbingMap.get(dubbingId);
            String categoryId = dubbing == null ? "" : dubbing.getCategoryId();
            DubbingCategory dubbingCategory = dubbingCategoryMap.get(categoryId);
            if (dubbing == null) {
                continue;
            }
            dubbingInfo.putAll(NewHomeworkContentDecorator.decorateDubbing(dubbing, dubbingCategory, null, null, null, ObjectiveConfigType.DUBBING_WITH_SCORE, dubbingThemeMap));
            List<Map<String, Object>> studentInfo = new LinkedList<>();
            int finishedNum = 0;
            Long totalDuration = 0L;
            Double totalScore = 0d;
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultFinishedMap.values()) {
                if (MapUtils.isEmpty(newHomeworkResult.getPractices())
                        || newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE) == null
                        || MapUtils.isEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE).getAppAnswers())) {
                    continue;
                }
                Map<String, Object> studentAchievement = new LinkedHashMap<>();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResult
                        .getPractices()
                        .get(ObjectiveConfigType.DUBBING_WITH_SCORE)
                        .getAppAnswers()
                        .get(dubbingId);
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
                String hyid = new DubbingSyntheticHistory.ID(reportRateContext.getNewHomework().getId(), newHomeworkResult.getUserId(), dubbingId).toString();
                boolean syntheticSuccess = true;
                if (dubbingSyntheticHistoryMap.containsKey(hyid)) {
                    DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(hyid);
                    syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(reportRateContext.getNewHomework().getCreateAt()));
                }
                studentAchievement.put("syntheticSuccess", syntheticSuccess);
                studentAchievement.put("videoUrl", newHomeworkResultAppAnswer.getVideoUrl());
                studentInfo.add(studentAchievement);
            }
            if (finishedNum == 0) {
                dubbingInfo.put("avgDuration", 0);
                dubbingInfo.put("avgScore", 0);
                dubbingInfo.put("finishedNum", 0);
                dubbingInfo.put("totalUserNum", MapUtils.isNotEmpty(reportRateContext.getUserMap()) ? reportRateContext.getUserMap().keySet().size() : 0);
                dubbingInfoList.add(dubbingInfo);
                studentsInfoMap.put(dubbingId, studentInfo);
                continue;
            }
            // 排序：用时长的在前，用时相同的先完成的在前
            Comparator<Map<String, Object>> comparator = (e1, e2) -> Long.compare(SafeConverter.toLong(e2.get("score")), SafeConverter.toLong(e1.get("score")));
            comparator = comparator
                    .thenComparing((e1, e2) -> Long.compare(SafeConverter.toLong(e2.get("duration")), SafeConverter.toLong(e1.get("duration"))))
                    .thenComparing(Comparator.comparing(e -> SafeConverter.toDate(e.get("finishAt"))));
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
            dubbingInfo.put("avgDuration", avgDuration);
            dubbingInfo.put("avgScore", avgScore);
            dubbingInfo.put("finishedNum", finishedNum);
            dubbingInfo.put("totalUserNum", MapUtils.isNotEmpty(reportRateContext.getUserMap()) ? reportRateContext.getUserMap().keySet().size() : 0);
            dubbingInfo.put("studentInfo", studentInfo);
            studentsInfoMap.put(dubbingId, studentInfo);
            dubbingInfoList.add(dubbingInfo);
        }
        dubbingReport.put("dubbingInfo", dubbingInfoList);
        dubbingReport.put("studentInfo", studentsInfoMap);
        reportRateContext.getResult().put(ObjectiveConfigType.DUBBING_WITH_SCORE.name(), dubbingReport);
    }
}
