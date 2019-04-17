package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.CommonTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class AppCommonObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.COMMON;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {

        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        //*********** end 初始化数据准备 *********** //

        //*********** begin commonTypePart 是返回数据结构 *********** //
        //1>设置类型数据
        //2>判断是否有学生完成
        CommonTypePart commonTypePart = new CommonTypePart();
        commonTypePart.setType(type);
        commonTypePart.setTypeName(type.getValue());
        if (type.isSubjective()) {
            commonTypePart.setShowScore(false);
        }
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            commonTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, commonTypePart);
            commonTypePart.setShowUrl(false);
            return;
        }
        //*********** end commonTypePart 是返回数据结构 *********** //

        int totalScore = 0;
        long totalDuration = 0;
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
        }

        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();
        commonTypePart.setAverScore(averScore);
        commonTypePart.setHasFinishUser(true);
        commonTypePart.setAverDuration(averDuration);
        commonTypePart.setSubContent("班平均分" + averScore + " 平均用时" + averDuration + "min");
        commonTypePart.setShowUrl(true);
        if (type.isSubjective()) {
            commonTypePart.setShowUrl(false);
        }
        result.put(type, commonTypePart);
    }
}
