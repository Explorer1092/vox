package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastReportRateContext;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessLiveCastHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.DubbingCategory;
import com.voxlearning.utopia.service.question.api.entity.DubbingTheme;
import com.voxlearning.utopia.service.question.api.entity.PictureBookTopic;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Named
public class ProcessLiveCastHomeworkAnswerDetailDubbingTemplate extends ProcessLiveCastHomeworkAnswerDetailTemplate
{
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING;
    }

    @Override
    public void processNewHomeworkAnswerDetail(LiveCastReportRateContext liveCastReportRateContext) {
        Map<String, Object> dubbingReport = new LinkedHashMap<>();
        List<Map<String, Object>> dubbingInfoList = new LinkedList<>();
        Map<String, List<Map<String, Object>>> studentsInfoMap = new LinkedHashMap<>();
        Map<String, LiveCastHomeworkResult> newHomeworkResultFinishedMap = handlerNewHomeworkResultMap(
                liveCastReportRateContext.getLiveCastHomeworkResultMap(),
                ObjectiveConfigType.DUBBING
        );
        if (MapUtils.isEmpty(newHomeworkResultFinishedMap)) {
            return;
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastReportRateContext
                .getLiveCastHomework()
                .findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);

        if (newHomeworkPracticeContent != null && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())) {
            List<String> dubbingIds = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(NewHomeworkApp::getDubbingId)
                    .collect(Collectors.toList());
            List<String> hyids = new LinkedList<>();
            for (String dubbingId : dubbingIds) {
                for (LiveCastHomeworkResult newHomeworkResult : newHomeworkResultFinishedMap.values()) {
                    hyids.add(new DubbingSyntheticHistory.ID(liveCastReportRateContext.getLiveCastHomework().getId(), newHomeworkResult.getUserId(), dubbingId).toString());
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
                if (StringUtils.isNotBlank(dubbingId)) {
                    Dubbing dubbing = dubbingMap.get(dubbingId);
                    String categoryId = dubbing == null ? "" : dubbing.getCategoryId();
                    DubbingCategory dubbingCategory = dubbingCategoryMap.get(categoryId);
                    if (dubbing == null) {
                        continue;
                    }
                    dubbingInfo.putAll(NewHomeworkContentDecorator.decorateDubbing(dubbing, dubbingCategory, null, null, null, ObjectiveConfigType.DUBBING, dubbingThemeMap));
                    List<Map<String, Object>> studentInfo = new LinkedList<>();

                    int finishedNum = 0;
                    Long totalDuration = 0L;
                    for (LiveCastHomeworkResult newHomeworkResult : newHomeworkResultFinishedMap.values()) {
                        if (MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                                && newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING) != null
                                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING).getAppAnswers())) {
                            Map<String, Object> studentAchievement = new LinkedHashMap<>();
                            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResult
                                    .getPractices()
                                    .get(ObjectiveConfigType.DUBBING)
                                    .getAppAnswers()
                                    .get(dubbingId);
                            User user = liveCastReportRateContext.getUserMap().get(newHomeworkResult.getUserId());
                            studentAchievement.put("userId", newHomeworkResult.getUserId());
                            studentAchievement.put("userName", user != null ? user.fetchRealname() : "");

                            if (newHomeworkResultAppAnswer.getDuration() != null) {
                                int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                                        .intValue();
                                studentAchievement.put("duration", duration);
                                totalDuration += duration;
                            } else {
                                studentAchievement.put("duration", null);
                            }
                            finishedNum++;
                            studentAchievement.put("finishAt", newHomeworkResultAppAnswer.getFinishAt());
                            String hyid = new DubbingSyntheticHistory.ID(liveCastReportRateContext.getLiveCastHomework().getId(), newHomeworkResult.getUserId(), dubbingId).toString();
                            boolean syntheticSuccess = true;
                            if (dubbingSyntheticHistoryMap.containsKey(hyid)) {
                                DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(hyid);
                                syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(liveCastReportRateContext.getLiveCastHomework().getCreateAt()));
                            }
                            studentAchievement.put("syntheticSuccess", syntheticSuccess);
                            studentAchievement.put("videoUrl", newHomeworkResultAppAnswer.getVideoUrl());
                            studentInfo.add(studentAchievement);
                        }
                    }
                    // 排序：用时长的在前，用时相同的先完成的在前
                    Comparator<Map<String, Object>> comparator = (e1, e2) -> Long.compare(SafeConverter.toLong(e2.get("duration")), SafeConverter.toLong(e1.get("duration")));
                    comparator = comparator.thenComparing(Comparator.comparing(e -> SafeConverter.toDate(e.get("finishAt"))));
                    studentInfo = studentInfo.stream()
                            .filter(Objects::nonNull)
                            .filter(e -> e.get("duration") != null)
                            .filter(e -> e.get("finishAt") != null)
                            .sorted(comparator)
                            .collect(Collectors.toList());
                    Long avgDuration = new BigDecimal(totalDuration > 0 ? totalDuration / finishedNum : 0)
                            .setScale(0, BigDecimal.ROUND_UP)
                            .longValue();
                    dubbingInfo.put("avgDuration", avgDuration);
                    dubbingInfo.put("finishedNum", finishedNum);
                    dubbingInfo.put("totalUserNum", MapUtils.isNotEmpty(liveCastReportRateContext.getUserMap()) ? liveCastReportRateContext.getUserMap().size() : 0);
                    dubbingInfo.put("studentInfo", studentInfo);
                    studentsInfoMap.put(dubbingId, studentInfo);
                }
                dubbingInfoList.add(dubbingInfo);
            }
        }
        dubbingReport.put("dubbingInfo", dubbingInfoList);
        dubbingReport.put("studentInfo", studentsInfoMap);

        if (MapUtils.isNotEmpty(dubbingReport)) {
            liveCastReportRateContext.getResult().put(ObjectiveConfigType.DUBBING.name(), dubbingReport);
        }
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(LiveCastReportRateContext liveCastReportRateContext) {
        List<Map<String, Object>> studentAchievement = new LinkedList<>();
        int totalApps = 0;
        int finishedAppNum = 0;
        Long totalDuration = 0L;
        NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastReportRateContext
                .getLiveCastHomework()
                .findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);

        if (newHomeworkPracticeContent != null
                && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())
                && liveCastReportRateContext.getLiveCastHomeworkResult() != null
                && MapUtils.isNotEmpty(liveCastReportRateContext.getLiveCastHomeworkResult().getPractices())) {
            Map<String, String> didToHyidMap = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(NewHomeworkApp::getDubbingId, (NewHomeworkApp o) -> new DubbingSyntheticHistory.ID(liveCastReportRateContext.getLiveCastHomework().getId(), liveCastReportRateContext.getLiveCastHomeworkResult().getUserId(), o.getDubbingId()).toString()));

            if (MapUtils.isEmpty(didToHyidMap)) {
                totalApps = didToHyidMap.size();
            }
            Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(didToHyidMap.values());


            NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastReportRateContext
                    .getLiveCastHomeworkResult()
                    .getPractices()
                    .get(ObjectiveConfigType.DUBBING);
            if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                finishedAppNum = newHomeworkResultAnswer.getAppAnswers().size();
            }

            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(didToHyidMap.keySet());
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
                String categoryId = dubbing == null ? "" : dubbing.getCategoryId();
                DubbingCategory dubbingCategory = dubbingCategoryMap.get(categoryId);
                if (dubbing == null) {
                    continue;
                }
                dubbingInfoMap.putAll(NewHomeworkContentDecorator.decorateDubbing(dubbing, dubbingCategory, null, null, null, ObjectiveConfigType.DUBBING, dubbingThemeMap));

                if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                    NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                            .getAppAnswers()
                            .get(dubbingId);
                    if (newHomeworkResultAppAnswer != null) {
                        if (newHomeworkResultAppAnswer.getDuration() != null) {
                            int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                                    .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                                    .intValue();
                            dubbingInfoMap.put("duration", duration);
                            totalDuration += duration;
                        } else {
                            dubbingInfoMap.put("duration", null);
                        }
                        if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getVideoUrl())) {
                            dubbingInfoMap.put("studentVideoUrl", newHomeworkResultAppAnswer.getVideoUrl());
                            boolean syntheticSuccess = true;
                            if (didToHyidMap.containsKey(dubbingId) && dubbingSyntheticHistoryMap.containsKey(didToHyidMap.get(dubbingId))) {
                                DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(didToHyidMap.get(dubbingId));
                                syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(liveCastReportRateContext.getLiveCastHomework().getCreateAt()));
                            }
                            dubbingInfoMap.put("syntheticSuccess", syntheticSuccess);
                        }
                    }
                }
                studentAchievement.add(dubbingInfoMap);
            }
        }

        int avgDuration = finishedAppNum > 0
                ? new BigDecimal(totalDuration)
                .divide(new BigDecimal(finishedAppNum), 0, BigDecimal.ROUND_HALF_UP)
                .intValue()
                : 0;
        liveCastReportRateContext.getResultMap()
                .put(ObjectiveConfigType.DUBBING, MapUtils.m(
                        "avgDuration", avgDuration,
                        "totalApps", totalApps,
                        "finishedAppNum", finishedAppNum,
                        "finished", totalApps == finishedAppNum,
                        "studentAchievement", studentAchievement
                ));
    }
}
