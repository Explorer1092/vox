package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.OcrDictationTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/25
 * \* Time: 4:28 PM
 * \* Description: 英语 - 纸质听写
 * \
 */
@Named
public class AppOcrDictationObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.OCR_DICTATION;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomework.getId());
        String unitInfo="";
        if (newHomeworkBook != null && MapUtils.isNotEmpty(newHomeworkBook.getPractices()) && CollectionUtils.isNotEmpty(newHomeworkBook.getPractices().get(type))) {
            List<NewHomeworkBookInfo> bookInfoList = newHomeworkBook.getPractices().get(type);
            List<String> unitList=bookInfoList.stream().map(NewHomeworkBookInfo::getUnitName).distinct().collect(Collectors.toList());
            unitInfo= StringUtils.join(unitList,",");
        }
        OcrDictationTypePart ocrDictationTypePart = new OcrDictationTypePart();
        ocrDictationTypePart.setType(type);
        ocrDictationTypePart.setTypeName(type.getValue());
        ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
        String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/dictation",
                MapUtils.m(
                        "homeworkId", newHomework.getId(),
                        "type", type,
                        "subject", newHomework.getSubject(),
                        "param", JsonUtils.toJson(param)));
        ocrDictationTypePart.setUrl(url);
        ocrDictationTypePart.setShowUrl(true);
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            ocrDictationTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, ocrDictationTypePart);
            return;
        }
        int totalScore = 0;
        Long totalDuration = 0L;
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
        }
        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long avgDuration = new BigDecimal(totalDuration)
                .divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP)
                .longValue();
        ocrDictationTypePart.setSubContent("班平均分" + averScore + " 平均用时" + avgDuration + "min");
        ocrDictationTypePart.setHasFinishUser(true);
        OcrDictationTypePart.OcrDictationType ocrDictationType = new OcrDictationTypePart.OcrDictationType();
        ocrDictationType.setTabName(unitInfo);
        ocrDictationTypePart.setTabs(Collections.singletonList(ocrDictationType));
        result.put(type, ocrDictationTypePart);
    }
}
