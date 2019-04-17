package com.voxlearning.utopia.service.mizar.impl.dao.change;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Yuechen.Wang on 2016/10/11.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestMizarEntityChangeRecordDaoTest {

    @Inject private MizarEntityChangeRecordDao mizarEntityChangeRecordDao;

    @Test
    public void loadByApplicant() throws Exception {
        for (int i = 0; i < 10; ++i) {
            MizarEntityChangeRecord mock = new MizarEntityChangeRecord();
            mock.setApplicantId("A" + (i % 3));
            mizarEntityChangeRecordDao.insert(mock);
        }

        List<MizarEntityChangeRecord> recordList = mizarEntityChangeRecordDao.loadByApplicant("A2");

        assertEquals(3, recordList.size());
    }

}