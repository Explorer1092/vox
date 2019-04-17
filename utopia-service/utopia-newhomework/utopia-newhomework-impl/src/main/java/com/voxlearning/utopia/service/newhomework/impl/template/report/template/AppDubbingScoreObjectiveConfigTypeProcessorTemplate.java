package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.DubbingWithScoreAppTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/21
 * \* Time: 上午11:25
 * \* Description: 带分数的趣味配音
 * \
 */
@Named
public class AppDubbingScoreObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING_WITH_SCORE;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        ObjectiveConfigType type = typePartContext.getType();
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        DubbingWithScoreAppTypePart dubbingAppTypePart = new DubbingWithScoreAppTypePart();
        dubbingAppTypePart.setType(type);
        dubbingAppTypePart.setTypeName(type.getValue());
        dubbingAppTypePart.setShowScore(false);
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap
                .values()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            dubbingAppTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, dubbingAppTypePart);
            return;
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<String> dubbingIds = newHomeworkPracticeContent
                .getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkApp::getDubbingId)
                .collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
        // 每个配音APP
        List<DubbingWithScoreAppTypePart.DubbingScorePart> dubbingParts = newHomeworkPracticeContent.getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(e -> {
                    String dubbingId = e.getDubbingId();
                    DubbingWithScoreAppTypePart.DubbingScorePart part = new DubbingWithScoreAppTypePart.DubbingScorePart();
                    part.setDubbingId(dubbingId);
                    if (MapUtils.isNotEmpty(dubbingMap) && dubbingMap.get(dubbingId) != null) {
                        Dubbing dubbing = dubbingMap.get(dubbingId);
                        part.setTabName(dubbing.getVideoName());
                    }
                    ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                    param.setDubbingId(dubbingId);
                    String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/dubbingdetail",
                            MapUtils.m(
                                    "homeworkId", newHomework.getId(),
                                    "type", type,
                                    "subject", newHomework.getSubject(),
                                    "param", JsonUtils.toJson(param)));
                    part.setUrl(url);
                    return part;
                }).collect(Collectors.toList());
        Map<String, DubbingWithScoreAppTypePart.DubbingScorePart> dubbingPartMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(dubbingParts)) {
            dubbingPartMap = dubbingParts
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(DubbingWithScoreAppTypePart.DubbingScorePart::getDubbingId, Function.identity()));
        }
        Long totalDuration = 0L;
        Double totalScore = 0d;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            if (MapUtils.isEmpty(newHomeworkResult.getPractices())) {
                continue;
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer == null) {
                continue;
            }
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
            totalScore += SafeConverter.toDouble(newHomeworkResultAnswer.processScore(type));
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            if (MapUtils.isEmpty(appAnswers)) {
                continue;
            }
            for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
                String dubbingId = entry.getKey();
                if (MapUtils.isEmpty(dubbingPartMap) || dubbingPartMap.get(dubbingId) == null) {
                    continue;
                }
                DubbingWithScoreAppTypePart.DubbingScorePart dubbingPart = dubbingPartMap.get(dubbingId);
                dubbingPart.setNum(1 + (dubbingPart.getNum() == null ? 0 : dubbingPart.getNum()));
                dubbingPart.setTotalDuration(SafeConverter.toLong(entry.getValue().processDuration()) + dubbingPart.getTotalDuration());
                dubbingPart.setTotalScore(SafeConverter.toDouble(entry.getValue().getScore()) + dubbingPart.getTotalScore());
            }
        }
        dubbingParts.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getNum() > 0)
                .forEach(e -> {
                    long avgDuration = new BigDecimal(e.getTotalDuration())
                            .divide(new BigDecimal(e.getNum() * 1000), 0, BigDecimal.ROUND_HALF_UP)
                            .longValue();
                    e.setAvgDuration(avgDuration);
                    int averScore = new BigDecimal(e.getTotalScore())
                            .divide(new BigDecimal(e.getNum()), 0, BigDecimal.ROUND_HALF_UP)
                            .intValue();
                    e.setAvgScore(averScore);
                    e.setTabValue(averScore + "分，" + NewHomeworkUtils.handlerTime((int) avgDuration));
                });
        long avgDuration = new BigDecimal(totalDuration)
                .divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP)
                .longValue();
        dubbingAppTypePart.setAverDuration(avgDuration);
        int avgScore = new BigDecimal(totalScore)
                .divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        dubbingAppTypePart.setAvgScore(avgScore);
        dubbingAppTypePart.setTabs(dubbingParts);
        dubbingAppTypePart.setSubContent("班平均分" + avgScore + " 平均用时" + avgDuration + "min");
        dubbingAppTypePart.setHasFinishUser(true);
        result.put(type, dubbingAppTypePart);
    }
}
