package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by yeuchen.wang on 2017/3/31.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAppPushWfMessageDao {
    @Inject private AppPushWfMessageDao appPushWfMessageDao;

    @Test
    public void testToBeSent() throws Exception {
        Date now = new Date();

        for (int i = 0; i < 10; ++i) {
            AppPushWfMessage mock = new AppPushWfMessage();
            mock.setStatus("processed");
            mock.setSendStatus("waiting");
            if (i % 3 == 0) {
                mock.setSendTime(DateUtils.addHours(now, -i));
            } else {
                mock.setSendTime(DateUtils.addHours(now, i));
            }
            appPushWfMessageDao.insert(mock);
        }

        List<AppPushWfMessage> toBeSent = appPushWfMessageDao.findToBeSent();
        assertEquals(4, toBeSent.size());
    }
}