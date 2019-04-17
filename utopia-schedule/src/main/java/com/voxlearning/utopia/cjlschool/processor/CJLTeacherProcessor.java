package com.voxlearning.utopia.cjlschool.processor;

import com.unitever.cif.core.message.CIFDataEntity;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.cjlschool.support.CJLDataProcessor;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@Named("teacherProcessor")
public class CJLTeacherProcessor extends CJLDataProcessor<CJLTeacher> {

    @Override
    public MapMessage sync(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }

        logger.debug("Total {} CJLTeacher data found.", data.size());

        // FIXME 只同步陈经纶学校高中部数学老师信息
        Set<String> validSchool = getSchoolIdMapping().keySet();
        Map<String, List<CJLTeacher>> teacherData = data.stream()
                .filter(d -> validSchool.contains(SafeConverter.toString(d.getFieldValue("schoolId"))))
                .map(this::convert)
                .filter(CJLTeacher::isValid)
                .collect(Collectors.groupingBy(CJLTeacher::getSchoolId));

        // 以学校维度进行更新
        for (Map.Entry<String, List<CJLTeacher>> entry : teacherData.entrySet()) {
            logger.debug("Processing CJLTeacher Data, school is {}, which has {} teachers", entry.getKey(), entry.getValue().size());
            cjlSyncDataServiceClient.getSyncDataService().syncSchoolTeacher(entry.getKey(), entry.getValue());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage modify(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }
        Map<String, Long> schoolIdMap = getSchoolIdMapping();
        // FIXME 只同步陈经纶学校高中部数学老师信息
        List<CJLTeacher> teacherList = data.stream()
                .filter(d -> schoolIdMap.containsKey(SafeConverter.toString(d.getFieldValue("schoolId"))))
                .map(this::convert)
                .filter(CJLTeacher::isValid)
                .collect(Collectors.toList());

        for (CJLTeacher teacher : teacherList) {
            cjlSyncDataServiceClient.getSyncDataService().modifyTeacher(teacher);
        }

        return MapMessage.successMessage();
    }

}
