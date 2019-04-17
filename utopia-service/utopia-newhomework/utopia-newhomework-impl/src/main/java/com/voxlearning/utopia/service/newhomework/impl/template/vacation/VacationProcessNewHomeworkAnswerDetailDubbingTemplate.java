package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
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
import com.voxlearning.utopia.service.question.api.entity.PictureBookTopic;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Named
public class VacationProcessNewHomeworkAnswerDetailDubbingTemplate extends VacationProcessNewHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING;
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext) {
        List<Map<String, Object>> studentAchievement = new LinkedList<>();
        int totalApps = 0;
        int finishedAppNum = 0;
        Long totalDuration = 0L;
        NewHomeworkPracticeContent newHomeworkPracticeContent = reportRateContext
                .getNewHomework()
                .findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);

        if (newHomeworkPracticeContent != null
                && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())
                && reportRateContext.getNewHomeworkResult() != null
                && MapUtils.isNotEmpty(reportRateContext.getNewHomeworkResult().getPractices())) {
            Map<String, String> didToHyidMap = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(NewHomeworkApp::getDubbingId, (NewHomeworkApp o) -> new DubbingSyntheticHistory.ID(reportRateContext.getNewHomework().getId(), reportRateContext.getNewHomeworkResult().getUserId(), o.getDubbingId()).toString()));

            if (MapUtils.isEmpty(didToHyidMap)) {
                totalApps = didToHyidMap.size();
            }
            Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(didToHyidMap.values());
            NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext
                    .getNewHomeworkResult()
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
                                syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(reportRateContext.getNewHomework().getCreateAt()));
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
        reportRateContext.getResultMap()
                .put(ObjectiveConfigType.DUBBING, MapUtils.m(
                        "avgDuration", avgDuration,
                        "totalApps", totalApps,
                        "finishedAppNum", finishedAppNum,
                        "finished", totalApps == finishedAppNum,
                        "studentAchievement", studentAchievement
                ));
    }
}
