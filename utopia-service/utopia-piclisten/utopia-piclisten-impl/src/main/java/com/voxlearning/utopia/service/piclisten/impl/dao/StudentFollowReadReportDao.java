package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.vendor.api.entity.StudentFollowReadReport;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author jiangpeng
 * @since 2017-03-22 下午1:23
 **/
@Named
@CacheBean(type = StudentFollowReadReport.class)
public class StudentFollowReadReportDao extends AlpsStaticMongoDao<StudentFollowReadReport, Long>{

    @Override
    protected void calculateCacheDimensions(StudentFollowReadReport document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

}
