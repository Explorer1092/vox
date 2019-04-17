package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.FamousSchoolSynHomeworkService;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@SubType({
        ObjectiveConfigType.EXAM,
        ObjectiveConfigType.MENTAL_ARITHMETIC
})
public class LoadUnitProcessorSub implements HomeworkProcessor {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @ImportService(interfaceClass = FamousSchoolSynHomeworkService.class)
    private FamousSchoolSynHomeworkService famousSchoolSynHomeworkService;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Long studentId = homeworkParam.getStudentId();
        String subject = homeworkParam.getSubject();
        String unitId = CollectionUtils.isNotEmpty(homeworkParam.getUnitIds()) ? homeworkParam.getUnitIds().get(0) : null;
        // 如果没有传unitId 或者传多个unitId 直接从缓存里面拿
        if (StringUtils.isBlank(unitId)) {
            unitId = HomeWorkCache.load(CacheKey.UNIT, studentId, subject);
        }
        StudentInfo studentInfo = hc.getStudentInfo();
        if (StringUtils.isBlank(unitId)) {
            Integer regionCode = studentInfo.getRegionCode();
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (exRegion == null) {
                hc.setMapMessage(MapMessage.errorMessage("区域参数错误"));
                return;
            }
            Long schoolId = studentInfo.getSchoolId();
            unitId = famousSchoolSynHomeworkService.queryFamousSchoolSynHomeworkBookProgress(
                    SafeConverter.toInt(hc.getGroupId()),
                    subject,
                    SafeConverter.toInt(schoolId),
                    regionCode,
                    exRegion.getCityCode(),
                    homeworkParam.getBookId());
        }
        // 获取单元列表
        List<NewBookCatalog> units = newContentLoaderClient.loadChildren(Collections.singleton(homeworkParam.getBookId()), BookCatalogType.UNIT).
                getOrDefault(homeworkParam.getBookId(), Collections.emptyList()).stream().
                sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
        // 放到上下文
        hc.setUnits(units);
        NewBookCatalog selectUnit = null;
        if (StringUtils.isNotBlank(unitId)) {
            String finalUnitId = unitId;
            selectUnit = units.stream().filter(u -> u.getId().equals(finalUnitId)).findFirst().orElse(null);
        }
        if (selectUnit == null) {
            selectUnit = ObjectUtils.get(() -> units.get(0));//进度返回单元与单元列表不一致
        }
        unitId = selectUnit.getId();
        hc.setUnitId(unitId);
        Map<String, Object> data = hc.getData();
        data.put("selectedUnit", MapUtils.m("id", selectUnit.getId(), "name", HomeworkUtil.unitName(selectUnit)));
    }
}
