package com.voxlearning.utopia.service.ai.impl.service.processor.dailyclass;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.ai.context.AIUserDailyClassContext;
import com.voxlearning.utopia.service.ai.data.AIClassInfo;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Map;

@Named
public class ADC_ClassInfo extends AbstractAiSupport implements IAITask<AIUserDailyClassContext> {

    @Override
    public void execute(AIUserDailyClassContext context) {
        if (context.getUnit() == null) {
            context.errorResponse("the unit is null");
            return;
        }

        AIClassInfo classInfo = new AIClassInfo();
        classInfo.setId(context.getUnit().getId());
        classInfo.setCname(context.getUnit().getAlias());
        classInfo.setName(context.getUnit().getName());
        classInfo.setCurrentDay(true);
        classInfo.setRank(context.getRank());
        Map<String, Object> map = JsonUtils.fromJson(SafeConverter.toString(context.getUnit().getExtras().get("ai_teacher")));
        classInfo.setImg(MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("cardImgUrl"), "") : "");
        classInfo.setTitle(MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("pageTitle"), "") : "");
        classInfo.setCardTitle(MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("cardTitle"), "") : "");
        classInfo.setCardDescription(MapUtils.isNotEmpty(map) ? SafeConverter.toString(map.get("cardDescription"), "") : "");
        // 是否完成
        AIUserUnitResultHistory result = aiUserUnitResultHistoryDao.load(context.getUser().getId(), context.getUnit().getId());
        if (result != null && result.getFinished()) {
            classInfo.setFinished(true);
            classInfo.setStar(result.getStar());
        } else {
            classInfo.setFinished(false);
        }
        context.setClassInfo(classInfo);
    }
}
