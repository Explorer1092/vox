package com.voxlearning.utopia.cjlschool.processor;

import com.unitever.cif.core.message.CIFDataEntity;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.cjlschool.support.CJLDataProcessor;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLClass;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@Named("classProcessor")
public class CJLClassProcessor extends CJLDataProcessor<CJLClass> {

    @Override
    public MapMessage sync(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }
        // FIXME 只同步陈经纶学校高中部班级信息
        Set<String> validSchool = getSchoolIdMapping().keySet();
        List<CJLClass> classList = data.stream()
                .filter(d -> validSchool.contains(SafeConverter.toString(d.getFieldValue("schoolId"))))
                .map(this::convert)
                .filter(CJLClass::isNotGraduated)
                .collect(Collectors.toList());

        logger.debug("Total {} CJLClass data found. After filtered there are {} left.", data.size(), classList.size());

        int slice = classList.size() / 20 + 1;
        CollectionUtils.splitList(classList, slice)
                .forEach(batch -> cjlSyncDataServiceClient.getSyncDataService().syncClass(batch));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage modify(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }
        Map<String, Long> schoolIdMap = getSchoolIdMapping();
        // FIXME 只同步陈经纶学校高中部数学老师信息
        List<CJLClass> classList = data.stream()
                .filter(d -> schoolIdMap.containsKey(SafeConverter.toString(d.getFieldValue("schoolId"))))
                .map(this::convert)
                .filter(CJLClass::isNotGraduated)
                .collect(Collectors.toList());

        for (CJLClass clazz : classList) {
            cjlSyncDataServiceClient.getSyncDataService().modifyClass(clazz);
        }
        return MapMessage.successMessage();
    }

}
