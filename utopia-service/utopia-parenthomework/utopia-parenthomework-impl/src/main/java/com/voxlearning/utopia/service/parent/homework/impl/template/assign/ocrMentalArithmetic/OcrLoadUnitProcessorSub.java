package com.voxlearning.utopia.service.parent.homework.impl.template.assign.ocrMentalArithmetic;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 纸质口算获取默认单元
 * @author chongfeng.qi
 * @data 20190115
 */
@Named
@SubType(
        ObjectiveConfigType.OCR_MENTAL_ARITHMETIC
)
public class OcrLoadUnitProcessorSub implements HomeworkProcessor {
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        String unitId = ObjectUtils.get(() -> param.getUnitIds().get(0));
        Map<String, Object> data = hc.getData();
        if (StringUtils.isBlank(unitId)) {
            Map<String, Object> unitIdMap = HomeWorkCache.load(CacheKey.UNIT, param.getBizType(), param.getStudentId(), param.getSubject());
            if (unitIdMap != null) {
                unitId = SafeConverter.toString(unitIdMap.get("unitId"));
                data.put("selectedSections", unitIdMap.get("sectionIds"));
            }
        }
        // 获取单元列表
        List<NewBookCatalog> units = newContentLoaderClient.loadChildren(Collections.singleton(param.getBookId()), BookCatalogType.UNIT).
                getOrDefault(param.getBookId(), Collections.emptyList()).stream().
                sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
        // 放到上下文
        hc.setUnits(units);
        NewBookCatalog selectUnit = null;
        if (StringUtils.isNotBlank(unitId)) {
            String finalUnitId = unitId;
            selectUnit = units.stream().filter(u -> u.getId().equals(finalUnitId)).findFirst().orElse(null);
        }
        if (selectUnit == null) {
            selectUnit = ObjectUtils.get(() -> units.get(0));
        }
        hc.setUnitId(selectUnit.getId());
        data.put("selectedUnit", MapUtils.m("id", selectUnit.getId(), "name",  selectUnit.getName()));
    }
}
