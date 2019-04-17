package com.voxlearning.utopia.cjlschool.processor;

import com.unitever.cif.core.message.CIFDataEntity;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.cjlschool.support.CJLDataProcessor;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacherCourse;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@Named("teacherCourseProcessor")
public class CJLTeacherCourseProcessor extends CJLDataProcessor<CJLTeacherCourse> {

    @Override
    public MapMessage sync(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }

        logger.debug("Total {} CJLTeacherCourse data found.", data.size());

        List<CJLTeacherCourse> courseList = data.stream()
                .map(this::convert)
                .filter(CJLTeacherCourse::isValid)
                .collect(Collectors.toList());

        logger.debug("Total {} CJLTeacherCourse data found. After filtered there are {} left.", data.size(), courseList.size());

        for (CJLTeacherCourse course : courseList) {
            try {
                cjlSyncDataServiceClient.getSyncDataService().syncTeacherCourse(course);
            } catch (Exception ex) {
                logger.error("Failed Execute Data: " + JsonUtils.toJson(course));
            }
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage modify(List<CIFDataEntity> data) {
        if (CollectionUtils.isEmpty(data)) {
            return MapMessage.successMessage();
        }

        logger.debug("Total {} CJLTeacherCourse data found.", data.size());

        List<CJLTeacherCourse> courseList = data.stream()
                .map(this::convert)
                .filter(CJLTeacherCourse::isValid)
                .collect(Collectors.toList());

        logger.debug("Total {} CJLTeacherCourse data found. After filtered there are {} left.", data.size(), courseList.size());

        for (CJLTeacherCourse course : courseList) {
            cjlSyncDataServiceClient.getSyncDataService().modifyTeacherCourse(course);
        }

        return MapMessage.successMessage();
    }

}
