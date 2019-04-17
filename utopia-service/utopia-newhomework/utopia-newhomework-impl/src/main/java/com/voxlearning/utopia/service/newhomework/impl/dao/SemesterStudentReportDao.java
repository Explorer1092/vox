package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.SemesterStudentReport;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = SemesterStudentReport.class)
public class SemesterStudentReportDao extends AlpsStaticMongoDao<SemesterStudentReport, String> {

    @Override
    protected void calculateCacheDimensions(SemesterStudentReport document, Collection<String> dimensions) {
        dimensions.add(SemesterStudentReport.ck_id(document.getId()));
    }

}
