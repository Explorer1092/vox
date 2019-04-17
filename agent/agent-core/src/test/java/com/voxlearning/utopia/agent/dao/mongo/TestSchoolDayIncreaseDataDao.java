package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.SchoolDayIncreaseData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * Created by alex on 2016/7/26.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestSchoolDayIncreaseDataDao {
    @Inject private SchoolDayIncreaseDataDao schoolDayIncreaseDataDao;

    @Test
    public void testFindSchoolData() throws Exception {
        SchoolDayIncreaseData data = new SchoolDayIncreaseData();
        data.setSchoolId(1L);
        data.setDay(20160701);
        data.setCountyCode(110101);
        data.setAddStuAuthNum(10L);
        schoolDayIncreaseDataDao.insert(data);

        data = new SchoolDayIncreaseData();
        data.setSchoolId(1L);
        data.setDay(20160702);
        data.setCountyCode(110101);
        data.setAddStuAuthNum(11L);
        schoolDayIncreaseDataDao.insert(data);

        data = new SchoolDayIncreaseData();
        data.setSchoolId(1L);
        data.setDay(20160701);
        data.setCountyCode(110102);
        data.setAddStuAuthNum(12L);
        schoolDayIncreaseDataDao.insert(data);

        List<SchoolDayIncreaseData> dataList = schoolDayIncreaseDataDao.findSchoolData(1L, Arrays.asList(20160701, 20160702));
        assertEquals(2, dataList.size());

        dataList = schoolDayIncreaseDataDao.findRegionData(Arrays.asList(110101), 20160701);
        assertEquals(1, dataList.size());

        dataList = schoolDayIncreaseDataDao.findRegionData(Arrays.asList(110101, 110102), 20160701);
        assertEquals(2, dataList.size());
    }
}
