package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
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
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.OralCommunicationAppTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;

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
 * \* Date: 2018/12/4
 * \* Time: 4:36 PM
 * \* Description:口语交际
 * \
 */
@Named
public class AppOralCommunicationObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_COMMUNICATION;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        ObjectiveConfigType type = typePartContext.getType();
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        OralCommunicationAppTypePart oralCommunicationAppTypePart = new OralCommunicationAppTypePart();
        oralCommunicationAppTypePart.setType(type);
        oralCommunicationAppTypePart.setTypeName(type.getValue());
        oralCommunicationAppTypePart.setShowScore(false);
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap
                .values()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            oralCommunicationAppTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, oralCommunicationAppTypePart);
            return;
        }
        List<String> stoneIds = newHomeworkPracticeContent
                .getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkApp::getStoneDataId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stoneIds)) {
            oralCommunicationAppTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, oralCommunicationAppTypePart);
            return;
        }
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            oralCommunicationAppTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, oralCommunicationAppTypePart);
            return;
        }
        Map<String, StoneBufferedData> stoneDataMap = stoneBufferedDataList.stream().collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));
        List<OralCommunicationAppTypePart.OralCommunicationScorePart> partList = newHomeworkPracticeContent.getApps()
                .stream()
                .filter(Objects::nonNull)
                .map(e -> {
                    String stoneDataId = e.getStoneDataId();
                    OralCommunicationAppTypePart.OralCommunicationScorePart part = new OralCommunicationAppTypePart.OralCommunicationScorePart();
                    part.setStoneDataId(stoneDataId);
                    if (MapUtils.isNotEmpty(stoneDataMap) && stoneDataMap.get(stoneDataId) != null) {
                        StoneBufferedData stoneDataItem = stoneDataMap.get(stoneDataId);
                        if (stoneDataItem.getOralPracticeConversion() != null) {
                            part.setTabName(
                                    StringUtils.isNotEmpty(stoneDataItem.getOralPracticeConversion().getTopicTrans())
                                            ? stoneDataItem.getOralPracticeConversion().getTopicTrans()
                                            : stoneDataItem.getOralPracticeConversion().getTopicName()
                            );
                        }
                        if (stoneDataItem.getInteractiveVideo() != null) {
                            part.setTabName(
                                    StringUtils.isNotEmpty(stoneDataItem.getInteractiveVideo().getTopicTrans())
                                            ? stoneDataItem.getInteractiveVideo().getTopicTrans()
                                            : stoneDataItem.getInteractiveVideo().getTopicName()
                            );
                        }
                        if (stoneDataItem.getInteractivePictureBook() != null) {
                            part.setTabName(
                                    StringUtils.isNotEmpty(stoneDataItem.getInteractivePictureBook().getTopicTrans())
                                            ? stoneDataItem.getInteractivePictureBook().getTopicTrans()
                                            : stoneDataItem.getInteractivePictureBook().getTopicName()
                            );
                        }
                    }
                    ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                    param.setStoneId(stoneDataId);
                    String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/oralcommunicationdetail",
                            MapUtils.m(
                                    "homeworkId", newHomework.getId(),
                                    "type", type,
                                    "subject", newHomework.getSubject(),
                                    "param", JsonUtils.toJson(param)));
                    part.setUrl(url);
                    return part;
                }).collect(Collectors.toList());
        Map<String, OralCommunicationAppTypePart.OralCommunicationScorePart> partMap;
        partMap = partList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(OralCommunicationAppTypePart.OralCommunicationScorePart::getStoneDataId, Function.identity()));
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
                String stoneId = entry.getKey();
                if (MapUtils.isEmpty(stoneDataMap) || stoneDataMap.get(stoneId) == null) {
                    continue;
                }
                OralCommunicationAppTypePart.OralCommunicationScorePart stonePart = partMap.get(stoneId);
                stonePart.setNum(1 + (stonePart.getNum() == null ? 0 : stonePart.getNum()));
                stonePart.setTotalDuration(SafeConverter.toLong(entry.getValue().processDuration()) + stonePart.getTotalDuration());
                stonePart.setTotalScore(SafeConverter.toDouble(entry.getValue().getScore()) + stonePart.getTotalScore());
            }
        }
        partList.stream()
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
        oralCommunicationAppTypePart.setAverDuration(avgDuration);
        int avgScore = new BigDecimal(totalScore)
                .divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        oralCommunicationAppTypePart.setAvgScore(avgScore);
        oralCommunicationAppTypePart.setTabs(partList);
        oralCommunicationAppTypePart.setSubContent("班平均分" + avgScore + " 平均用时" + avgDuration + "min");
        oralCommunicationAppTypePart.setHasFinishUser(true);
        result.put(type, oralCommunicationAppTypePart);
    }
}
