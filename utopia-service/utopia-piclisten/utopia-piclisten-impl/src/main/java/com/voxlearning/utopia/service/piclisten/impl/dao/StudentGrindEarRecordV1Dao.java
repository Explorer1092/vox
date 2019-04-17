package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecordV1;

import javax.inject.Named;
import java.util.Collection;

/**
 * 第一期磨耳朵活动dao
 *
 * @author jiangpeng
 * @since 2016-11-29 下午5:48
 **/
@Named
public class StudentGrindEarRecordV1Dao extends AlpsStaticMongoDao<StudentGrindEarRecordV1, Long> {
    @Override
    protected void calculateCacheDimensions(StudentGrindEarRecordV1 document, Collection<String> dimensions) {

    }

}
