package com.voxlearning.utopia.service.vendor.impl.loader;

import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.vendor.api.TermReportLoader;
import com.voxlearning.utopia.service.vendor.api.entity.TermReport;
import com.voxlearning.utopia.service.vendor.impl.dao.TermReportDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author malong
 * @since 2017/6/19
 */
@Named
@ExposeService(interfaceClass = TermReportLoader.class)
public class TermReportLoaderImpl implements TermReportLoader{
    @Inject
    private TermReportDao termReportDao;

    @Override
    public TermReport getTermReport(@CacheParameter(value = "PID") Long parentId, @CacheParameter(value = "SID") Long studentId) {
        return termReportDao.getTermReport(parentId, studentId);
    }
}
