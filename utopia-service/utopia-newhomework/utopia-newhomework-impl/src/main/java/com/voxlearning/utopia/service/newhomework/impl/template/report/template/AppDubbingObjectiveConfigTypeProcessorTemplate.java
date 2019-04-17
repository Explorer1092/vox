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
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.DubbingAppTypePart;
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
 * @author zhangbin
 * @since 2017/11/3
 */

@Named
public class AppDubbingObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        ObjectiveConfigType type = typePartContext.getType();
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();

        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap
                .values()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<String> dubbingIds = newHomeworkPracticeContent
                .getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkApp::getDubbingId)
                .collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);

        DubbingAppTypePart dubbingAppTypePart = new DubbingAppTypePart();
        dubbingAppTypePart.setType(type);
        dubbingAppTypePart.setTypeName(type.getValue());
        dubbingAppTypePart.setShowScore(false);
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            dubbingAppTypePart.setSubContent("平均用时--");
            result.put(type, dubbingAppTypePart);
            return;
        }

        // 每个配音APP
        List<DubbingAppTypePart.DubbingPart> dubbingParts = newHomeworkPracticeContent.getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(e -> {
                    String dubbingId = e.getDubbingId();
                    DubbingAppTypePart.DubbingPart part = new DubbingAppTypePart.DubbingPart();
                    part.setDubbingId(dubbingId);
                    if (MapUtils.isNotEmpty(dubbingMap) && dubbingMap.get(dubbingId) != null) {
                        Dubbing dubbing = dubbingMap.get(dubbingId);
                        part.setTabName(dubbing.getVideoName());
                    }
                    ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                    param.setDubbingId(dubbingId);
                    // url
                    String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/dubbingdetail",
                            MapUtils.m(
                                    "homeworkId", newHomework.getId(),
                                    "type", type,
                                    "subject", newHomework.getSubject(),
                                    "param", JsonUtils.toJson(param)));
                    part.setUrl(url);
                    return part;
                }).collect(Collectors.toList());
        Map<String, DubbingAppTypePart.DubbingPart> dubbingPartMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(dubbingParts)) {
            dubbingPartMap = dubbingParts
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(DubbingAppTypePart.DubbingPart::getDubbingId, Function.identity()));
        }

        Long totalDuration = 0L;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            if (MapUtils.isNotEmpty(newHomeworkResult.getPractices())) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
                if (newHomeworkResultAnswer != null) {
                    totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
                    LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
                    if (MapUtils.isNotEmpty(appAnswers)) {
                        for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
                            String dubbingId = entry.getKey();
                            if (MapUtils.isNotEmpty(dubbingPartMap) && dubbingPartMap.get(dubbingId) != null) {
                                DubbingAppTypePart.DubbingPart dubbingPart = dubbingPartMap.get(dubbingId);
                                dubbingPart.setNum(1 + (dubbingPart.getNum() == null ? 0 : dubbingPart.getNum()));
                                Long oldTotalDuration = dubbingPart.getTotalDuration() == null ? 0 : dubbingPart.getTotalDuration();
                                dubbingPart.setTotalDuration(SafeConverter.toLong(entry.getValue().processDuration()) + oldTotalDuration);
                            }
                        }
                    }
                }
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
                    e.setTabValue(NewHomeworkUtils.handlerTime((int) avgDuration));
                });

        long avgDuration = new BigDecimal(totalDuration)
                .divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP)
                .longValue();
        dubbingAppTypePart.setTabs(dubbingParts);
        dubbingAppTypePart.setAverDuration(avgDuration);
        dubbingAppTypePart.setSubContent("平均用时" + avgDuration + "min");
        dubbingAppTypePart.setHasFinishUser(true);
        result.put(type, dubbingAppTypePart);
    }
}
