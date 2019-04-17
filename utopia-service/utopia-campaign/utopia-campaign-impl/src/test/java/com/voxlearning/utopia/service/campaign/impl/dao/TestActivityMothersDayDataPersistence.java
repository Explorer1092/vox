package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.activity.ActivityMothersDayData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = ActivityMothersDayData.class)
public class TestActivityMothersDayDataPersistence {

    @Inject private ActivityMothersDayDataPersistence activityMothersDayDataPersistence;

    @Test
    public void testActivityMothersDayDataPersistence() throws Exception {
        long id = RandomUtils.nextLong();
        ActivityMothersDayData data = new ActivityMothersDayData();
        data.setStudentId(id);
        activityMothersDayDataPersistence.insert(data);
        data = activityMothersDayDataPersistence.load(id);
        assertNotNull(data);
    }
}
