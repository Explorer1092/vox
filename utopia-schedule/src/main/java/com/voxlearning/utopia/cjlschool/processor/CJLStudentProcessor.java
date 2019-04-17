package com.voxlearning.utopia.cjlschool.processor;

import com.unitever.cif.core.message.CIFDataEntity;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.cjlschool.support.CJLDataProcessor;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLStudent;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@Named("studentProcessor")
public class CJLStudentProcessor extends CJLDataProcessor<CJLStudent> {

    @Override
    public MapMessage sync(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }

        // FIXME 只同步陈经纶学校高中部数学老师信息
        Map<String, Long> schoolIdMap = getSchoolIdMapping();
        List<CJLStudent> studentList = data.stream()
                .filter(d -> schoolIdMap.containsKey(SafeConverter.toString(d.getFieldValue("schoolId"))))
                .map(this::convert)
                .filter(CJLStudent::isValid)
                .collect(Collectors.toList());

        logger.debug("Total {} CJLStudent data found. After filtered there are {} left.", data.size(), studentList.size());

        int slice = studentList.size() / 20 + 1;
        CollectionUtils.splitList(studentList, slice)
                .forEach(batch -> cjlSyncDataServiceClient.getSyncDataService().syncStudents(batch));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage modify(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }
        Map<String, Long> schoolIdMap = getSchoolIdMapping();
        List<CJLStudent> studentList = data.stream()
                .filter(d -> schoolIdMap.containsKey(SafeConverter.toString(d.getFieldValue("schoolId"))))
                .map(this::convert)
                .filter(CJLStudent::isValid)
                .collect(Collectors.toList());

        for (CJLStudent student : studentList) {
            cjlSyncDataServiceClient.getSyncDataService().modifyStudent(student);
        }

        return MapMessage.successMessage();
    }

}
