package com.voxlearning.utopia.service.parent.homework.impl.template.assign.mentalArithmetic;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.QuestionPackageTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.apache.commons.beanutils.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取口算题包
 * @author chongfeng,qi
 * @data 20190111
 */
@Named("Mental.LoadQuestionPackageProcessorSub")
@SubType({
        ObjectiveConfigType.MENTAL_ARITHMETIC
})
public class LoadQuestionPackageProcessorSub implements HomeworkProcessor {
    @Inject
    private QuestionPackageTemplate questionPackageTemplate;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        // 单元ids
        List<String> unitIds = param.getUnitIds();
        if (CollectionUtils.isEmpty(unitIds)) {
            param.setUnitIds(hc.getUnits().stream().map(NewBookCatalog::unitId).collect(Collectors.toList()));
        }
        Map<String, List<Map<String, Object>>> questionBoxes = new LinkedHashMap<>();
        List<String> filterUnitIds = param.getUnitIds().stream().filter(uid -> {
            boolean isAssign = SafeConverter.toBoolean(HomeWorkCache.load(CacheKey.TODAYASSIGN, param.getBizType(), param.getStudentId(), uid, "BASE"));
            if (isAssign) {
                questionBoxes.put(uid, Collections.singletonList(MapUtils.m("isAssign", isAssign)));
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        Map<String, Object> data = hc.getData();
        data.put("questionBoxes", questionBoxes);
        hc.setData(data);
        // 如果已经全部布置，直接返回
        if (filterUnitIds.isEmpty()) {
            return;
        }
        HomeworkContext packageContext = new HomeworkContext();
        try {
            BeanUtils.copyProperties(packageContext, hc);
            packageContext.getHomeworkParam().setUnitIds(filterUnitIds);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
        questionPackageTemplate.questionPackage(packageContext);
        if (packageContext.getMapMessage() != null && !packageContext.getMapMessage().isSuccess()) {
            hc.setMapMessage(packageContext.getMapMessage());
            return;
        }
        if (packageContext.getQuestionPackages() == null) {
            hc.setMapMessage(MapMessage.errorMessage("获取口算题包失败"));
            return;
        }
        packageContext.getQuestionPackages().stream().collect(Collectors.groupingBy(QuestionPackage::getUnitId)).forEach((unitId, boxes) -> {
            questionBoxes.put(unitId, boxes.stream().map(question -> {
                // 以题包id缓存维度缓存题包, 布置的时候根据id查询具体题包布置
                HomeWorkCache.set(60 * 60, question, CacheKey.BOX, param.getBizType(), question.getId());
                return MapUtils.m(
                        "id", question.getId(),
                        "isAssign", false,
                        "duration", (question.getDuration() + 59) / 60,
                        "questionCount", ObjectUtils.get(() -> question.getDocIds().size(), 0));
            }).collect(Collectors.toList()));
        });
    }
}
