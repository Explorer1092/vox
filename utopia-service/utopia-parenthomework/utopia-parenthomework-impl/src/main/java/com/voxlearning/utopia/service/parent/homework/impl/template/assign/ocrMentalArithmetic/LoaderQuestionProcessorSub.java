package com.voxlearning.utopia.service.parent.homework.impl.template.assign.ocrMentalArithmetic;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
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
 * 纸质口算练习 获取题包
 * @author chongeng.qi
 * @data 20190115
 */
@Named("OcrMental.LoaderQuestionProcessorSub")
@SubType({
        ObjectiveConfigType.OCR_MENTAL_ARITHMETIC
})
public class LoaderQuestionProcessorSub implements HomeworkProcessor {

    @Inject
    private QuestionPackageTemplate questionPackageTemplate;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        Map<String, Object> data = hc.getData();
        // 默认单元
        String unitId = hc.getUnitId();
        // 课时
        List<String> sectionIds = param.getSectionIds();
        if (CollectionUtils.isEmpty(sectionIds)) {
            List<NewBookCatalog> section = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.SECTION)
                    .getOrDefault(unitId, Collections.emptyList());
            // 返回课时列表
            data.put("sections", section.stream().map(catalog -> MapUtils.m("id", catalog.getId(), "name", catalog.getName())).collect(Collectors.toList()));
            sectionIds = section.stream().map(NewBookCatalog::getId).collect(Collectors.toList());
        }
        Map<String, List<Map<String, Object>>> questionBoxes = new LinkedHashMap<>();
        HomeworkContext packageContext = new HomeworkContext();
        try {
            BeanUtils.copyProperties(packageContext, hc);
            packageContext.getHomeworkParam().setSectionIds(sectionIds);
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
        List<String> selectedSections = (List)data.get("selectedSections");
        boolean isAssign = CollectionUtils.isEmpty(selectedSections);
        packageContext.getQuestionPackages().stream().collect(Collectors.groupingBy(QuestionPackage::getSection)).forEach((section, boxes) -> {
            questionBoxes.put(section, boxes.stream().map(question -> {
                // 以题包id缓存维度缓存题包, 布置的时候根据id查询具体题包布置
                HomeWorkCache.set(60 * 60, question, CacheKey.BOX, param.getBizType(), question.getId());
                return MapUtils.m(
                        "id", question.getId(),
                        "isAssign", isAssign || selectedSections.contains(section),
                        "questionCount", ObjectUtils.get(() -> question.getDocIds().size(), 0));
            }).collect(Collectors.toList()));
        });
        data.put("questionBoxes", questionBoxes);
        data.put("maxCount", ObjectUtils.get(() -> HomeWorkCache.load(CacheKey.Q_LIMIT), 180));
        hc.setData(data);
    }
}
