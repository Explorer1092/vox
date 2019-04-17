package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

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
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.DubbingAppPart;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
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

/**
 * @author zhangbin
 * @since 2017/11/1
 */

@Named
public class ProcessNewHomeworkAnswerDetailDubbingTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING;
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        Map<String, Object> dubbingReport = new LinkedHashMap<>();
        List<Map<String, Object>> dubbingInfoList = new LinkedList<>();
        Map<String, List<Map<String, Object>>> studentsInfoMap = new LinkedHashMap<>();
        Map<String, NewHomeworkResult> newHomeworkResultFinishedMap = handlerNewHomeworkResultMap(
                reportRateContext.getNewHomeworkResultMap(),
                ObjectiveConfigType.DUBBING
        );
        if (MapUtils.isEmpty(newHomeworkResultFinishedMap)) {
            return;
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = reportRateContext
                .getNewHomework()
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
                    for (NewHomeworkResult newHomeworkResult : newHomeworkResultFinishedMap.values()) {
                        if (MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                                && newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING) != null
                                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING).getAppAnswers())) {
                            Map<String, Object> studentAchievement = new LinkedHashMap<>();
                            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResult
                                    .getPractices()
                                    .get(ObjectiveConfigType.DUBBING)
                                    .getAppAnswers()
                                    .get(dubbingId);
                            User user = reportRateContext.getUserMap().get(newHomeworkResult.getUserId());
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
                    dubbingInfo.put("totalUserNum", MapUtils.isNotEmpty(reportRateContext.getUserMap()) ? reportRateContext.getUserMap().keySet().size() : 0);
                    dubbingInfo.put("studentInfo", studentInfo);
                    studentsInfoMap.put(dubbingId, studentInfo);
                }
                dubbingInfoList.add(dubbingInfo);
            }
        }
        dubbingReport.put("dubbingInfo", dubbingInfoList);
        dubbingReport.put("studentInfo", studentsInfoMap);

        if (MapUtils.isNotEmpty(dubbingReport)) {
            reportRateContext.getResult().put(ObjectiveConfigType.DUBBING.name(), dubbingReport);
        }
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
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
        Map<Long, String> userIdTohyidMap = new LinkedHashMap<>();
        String hid = context.getNewHomework().getId();
        for (NewHomeworkResult newHomeworkResult : context.getNewHomeworkResultMap().values()) {
            userIdTohyidMap.put(newHomeworkResult.getUserId(), new DubbingSyntheticHistory.ID(hid, newHomeworkResult.getUserId(), dubbingId).toString());
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(userIdTohyidMap.values());
        Dubbing dubbing = dubbingMap.get(dubbingId);

        DubbingAppPart dubbingAppPart = new DubbingAppPart();
        dubbingAppPart.setDubbingId(dubbingId);
        dubbingAppPart.setDubbingName(dubbing == null ? "" : dubbing.getVideoName());
        dubbingAppPart.setVideoUrl(dubbing == null ? "" : dubbing.getVideoUrl());
        List<DubbingAppPart.DubbingAppUser> users = new ArrayList<>();
        if (MapUtils.isNotEmpty(newHomeworkResultMap)) {
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                if (MapUtils.isNotEmpty(newHomeworkResult.getPractices())) {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
                    if (newHomeworkResultAnswer != null
                            && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())
                            && newHomeworkResultAnswer.getAppAnswers().get(dubbingId) != null) {
                        NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                                .getAppAnswers()
                                .get(dubbingId);
                        Date appFinishedAt = newHomeworkResultAppAnswer.getFinishAt();
                        DubbingAppPart.DubbingAppUser user = new DubbingAppPart.DubbingAppUser();
                        Long userId = newHomeworkResult.getUserId();
                        user.setUserId(userId);
                        if (MapUtils.isNotEmpty(userMap) && userMap.get(userId) != null) {
                            user.setUserName(userMap.get(userId).fetchRealname());
                        }
                        int time = new BigDecimal(SafeConverter.toLong(newHomeworkResultAppAnswer.processDuration()))
                                .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                                .intValue();
                        String duration = NewHomeworkUtils.handlerEnTime(time);
                        user.setDurationTime(time);
                        user.setDuration(duration);
                        user.setStudentVideoUrl(newHomeworkResultAppAnswer.getVideoUrl());
                        user.setFinishedAt(appFinishedAt);
                        if (userIdTohyidMap.containsKey(userId) && dubbingSyntheticHistoryMap.containsKey(userIdTohyidMap.get(userId))) {
                            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(userIdTohyidMap.get(userId));
                            user.setSyntheticSuccess(SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(context.getNewHomework().getCreateAt())));
                        }
                        users.add(user);
                    }
                }
            }
            // 排序：用时长的在前，用时相同的先完成的在前
            Comparator<DubbingAppPart.DubbingAppUser> comparator = (e1, e2) -> e2.getDurationTime().compareTo(e1.getDurationTime());
            comparator = comparator.thenComparing(Comparator.comparing(DubbingAppPart.DubbingAppUser::getFinishedAt));
            users = users.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getDurationTime() != null)
                    .filter(e -> e.getFinishedAt() != null)
                    .sorted(comparator)
                    .collect(Collectors.toList());
            dubbingAppPart.setUsers(users);
        }

        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        mapMessage.set("dubbingAppPart", dubbingAppPart);
    }

    @Override
    public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context) {
        context.setMapMessage(MapMessage.errorMessage());
    }

    @Override
    public String processStudentPartTypeScore(NewHomework newHomework, NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        //非主观题
        return "已完成";
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


        int finishCount = 0;
        int totalDuration = 0;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            finishCount++;
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer duration = newHomeworkResultAnswer.processDuration();
            totalDuration += SafeConverter.toInt(new BigDecimal(SafeConverter.toInt(duration)).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue());
        }
        int avgDuration = 0;
        if (finishCount != 0) {
            avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_UP).intValue();
        }
        typeResult.put("finishCount", finishCount);
        typeResult.put("avgDuration", avgDuration);
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        result.put(type, typeResult);
    }
}
