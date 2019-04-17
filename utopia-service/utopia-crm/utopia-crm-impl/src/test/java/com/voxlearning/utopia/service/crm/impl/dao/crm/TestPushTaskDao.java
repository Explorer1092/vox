package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.crm.api.entities.crm.PushTask;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by wangshichao on 16/8/31.
 */

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestPushTaskDao {

    @Inject private PushTaskDao pushTaskDao;

    @Test
    public void testInsert() {


        PushTask pushTask = new PushTask();
        Date date = new Date();
        ObjectId objectId = new ObjectId();
        String id = SafeConverter.toString(date.getTime()) + "-" + objectId.toString();
        pushTask.setId(id);
        pushTaskDao.insert(pushTask);
    }
}
