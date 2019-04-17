package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.OcrMentalArithmeticTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class AppOcrMentalArithmeticProcessorTemplate extends AppReadReciteObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         * 4:newHomework
         * 5:newBookCatalogMap
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        NewHomework newHomework = typePartContext.getNewHomework();
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);

        OcrMentalArithmeticTypePart ocrMentalArithmeticTypePart = new OcrMentalArithmeticTypePart();
        ocrMentalArithmeticTypePart.setType(type);
        ocrMentalArithmeticTypePart.setTypeName(type.getValue());
        ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
        String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/ocrmentalarithmeticdetail",
                MapUtils.m(
                        "homeworkId", newHomework.getId(),
                        "type", type,
                        "subject", newHomework.getSubject(),
                        "param", JsonUtils.toJson(param)));
        ocrMentalArithmeticTypePart.setUrl(url);
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            result.put(type, ocrMentalArithmeticTypePart);
            return;
        }
        //*********** end OcrMentalArithmeticTypePart 是返回数据结构 *********** //
        int totalScore = 0;
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
        }
        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        OcrMentalArithmeticTypePart.TabObject tabObject = new OcrMentalArithmeticTypePart.TabObject();
        tabObject.setTabName(target.getHomeworkDetail());
        tabObject.setTabValue("班平均分" + averScore);
        tabObject.setSubValue("（按照可以识别的题型来算）");
        tabObject.setShowUrl(false);
        OcrMentalArithmeticTypePart.OcrMentalType ocrMentalType = new OcrMentalArithmeticTypePart.OcrMentalType();
        ocrMentalType.setTabName(target.getWorkBookName()+"  "+target.getHomeworkDetail());
        ocrMentalType.setTabs(Collections.singletonList(tabObject));
        ocrMentalArithmeticTypePart.setShowUrl(true);
        ocrMentalArithmeticTypePart.setSubContent("班平均分" + averScore);
        ocrMentalArithmeticTypePart.setHasFinishUser(true);
        ocrMentalArithmeticTypePart.setTabs(Collections.singletonList(ocrMentalType));
        result.put(type, ocrMentalArithmeticTypePart);
    }
}
