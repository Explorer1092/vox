package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * Created by alex on 2016/7/29.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestSchoolSummaryService {

//    @Inject SchoolSummaryService schoolSummaryService;
//
//    @Test
//    public void testLoadSchoolVisitResultData() throws Exception {
//        Date date = new Date();
//        String runDate = DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE);
//        VisitSchoolResultData data = schoolSummaryService.loadSchoolVisitResultData(1000L,runDate);
//        assertNotNull(data);
//
//        Map<Long, VisitSchoolResultData> dataList = schoolSummaryService.loadSchoolVisitResultData(Arrays.asList(1000L, 1002L),runDate);
//        assertEquals(2, dataList.size());
//        FlightRecorder.dot("=================================");
//        dataList = schoolSummaryService.loadSchoolVisitResultData(Arrays.asList(1000L, 1002L),runDate);
//        assertEquals(2, dataList.size());
//    }
}
