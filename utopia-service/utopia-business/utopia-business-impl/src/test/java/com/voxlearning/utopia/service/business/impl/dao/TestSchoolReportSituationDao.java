package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.entity.SchoolReportSituation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author fugui.chang
 * @since 2016/9/28
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestSchoolReportSituationDao {
    @Inject
    private SchoolReportSituationDao schoolReportSituationDao;

    @Test
    @MockBinder(
            type = SchoolReportSituation.class,
            jsons = {
                    "{'schoolId':10001 , 'yearmonth':201603 , 'auth_use_tea_num_total':20 , 'month_sasc':3}",
                    "{'schoolId':10001 , 'yearmonth':201603 , 'auth_use_tea_num_total':10 , 'month_sasc':4}",
                    "{'schoolId':10002 , 'yearmonth':201604 , 'auth_use_tea_num_total':20 , 'month_sasc':3}"
            },
            persistence = SchoolReportSituationDao.class
    )
    public void testLoadSchoolReportSituationBySchoolIdAndDt(){
        List<SchoolReportSituation> schoolReportSituation = schoolReportSituationDao.loadSchoolReportSituationBySchoolIdAndDt(10001L,201603L);
        assertEquals(2,schoolReportSituation.size());
        schoolReportSituation = schoolReportSituationDao.loadSchoolReportSituationBySchoolIdAndDt(10002L,201604L);
        assertEquals(1,schoolReportSituation.size());
    }

}
