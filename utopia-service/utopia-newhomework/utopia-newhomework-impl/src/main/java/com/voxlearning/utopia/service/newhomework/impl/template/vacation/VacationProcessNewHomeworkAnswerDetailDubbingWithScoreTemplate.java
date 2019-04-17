package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.DubbingCategory;
import com.voxlearning.utopia.service.question.api.entity.DubbingTheme;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @Description: 新版趣味配音
 * @author: Mr_VanGogh
 * @date: 2018/11/22 下午2:14
 */
@Named
public class VacationProcessNewHomeworkAnswerDetailDubbingWithScoreTemplate extends VacationProcessNewHomeworkAnswerDetailTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING_WITH_SCORE;
    }

    /**
     * 处理个人答题详情
     * @param reportRateContext
     */
    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext) {
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
                    "avgScoreLevel", ScoreLevel.D.getLevel(),
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
                    "avgScoreLevel", ScoreLevel.D.getLevel(),
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
                dubbingInfoMap.put("scoreLevel", ScoreLevel.processLevel(score).getLevel());
                totalScore += newHomeworkResultAppAnswer.getScore();
            } else {
                dubbingInfoMap.put("score", 0);
                dubbingInfoMap.put("scoreLevel", ScoreLevel.D.getLevel());
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
                        "avgScoreLevel", ScoreLevel.processLevel(avgScore).getLevel(),
                        "totalApps", totalApps,
                        "finishedAppNum", finishedAppNum,
                        "finished", totalApps == finishedAppNum,
                        "studentAchievement", studentAchievement
                ));
    }
}
