package com.voxlearning.utopia.service.vendor.impl.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.TermReportLoader;
import com.voxlearning.utopia.service.vendor.api.entity.TermReport;
import com.voxlearning.utopia.service.vendor.impl.dao.TermReportDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * @author malong
 * @since 2017/6/19
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestTermReportLoaderImpl {
    @ImportService(interfaceClass = TermReportLoader.class)
    private TermReportLoader termReportLoader;
    @Inject
    private TermReportDao termReportDao;
    @Test
    public void test() {
        TermReport termReport = new TermReport();
        termReport.setParentId(256777L);
        termReport.setStudentId(333875333L);
        termReport.setStudentTile("优秀宝贝");
        termReportDao.insert(termReport);
        TermReport report = termReportLoader.getTermReport(256777L, 333875333L);
        Assert.assertNotNull(report);
        Assert.assertEquals("优秀宝贝", termReport.getStudentTile());
    }
}
