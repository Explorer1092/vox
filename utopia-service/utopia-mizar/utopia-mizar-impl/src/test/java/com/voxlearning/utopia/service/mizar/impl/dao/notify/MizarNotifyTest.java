package com.voxlearning.utopia.service.mizar.impl.dao.notify;

import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarUserNotify;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Yuechen.Wang on 2016/12/1.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class MizarNotifyTest {
    @Inject private MizarNotifyDao mizarNotifyDao;
    @Inject private MizarUserNotifyDao mizarUserNotifyDao;

    @Test
    public void testMizarNotifyDao() throws Exception {
        FlightRecorder.dot(String.format("==========    %s    =========", "测试id维度缓存"));
        MizarNotify mock = new MizarNotify();
        MizarNotify upsert = mizarNotifyDao.upsert(mock);
        assertNotNull(upsert);

        String cacheKey = upsert.newCacheKey(upsert.getId());
        CacheObject<MizarNotify> cache = mizarNotifyDao.getCache().get(cacheKey);
        assertNotNull(cache);

        FlightRecorder.dot(String.format("==========    %s    =========", "测试loadByCreator()方法"));
        for (int i = 0; i < 5; i++) {
            MizarNotify notify = new MizarNotify();
            notify.setCreator(String.valueOf(i % 2));
            mizarNotifyDao.insert(notify);
        }
        List<MizarNotify> notifies = mizarNotifyDao.loadByCreator("0");
        assertEquals(3, notifies.size());

        String key = mock.newCacheKey("C", "0");
        CacheObject<List<MizarNotify>> cacheObject = mizarNotifyDao.getCache().get(key);
        assertNotNull(cacheObject);
        assertEquals(3, cacheObject.getValue().size());
    }

    @Test
    public void testLoadByUser() throws Exception {
        List<MizarUserNotify> batch = new LinkedList<>();
        for (int i = 0; i < 10; ++i) {
            MizarUserNotify mock = new MizarUserNotify(String.valueOf(i % 3), String.valueOf(i));
            batch.add(mock);
        }
        mizarUserNotifyDao.inserts(batch);
        List<MizarUserNotify> notifies = mizarUserNotifyDao.loadByUser("2");
        assertEquals(3, notifies.size());
        String key = new MizarUserNotify().newCacheKey("U", "2");
        CacheObject<List<MizarUserNotify>> cacheObject = mizarUserNotifyDao.getCache().get(key);
        assertNotNull(cacheObject);
        assertEquals(3, cacheObject.getValue().size());
    }

    @Test
    public void testUpdateFlag() throws Exception {
        MizarUserNotify mock = new MizarUserNotify();
        assertNull(mock.getFlag());
        MizarUserNotify upsert = mizarUserNotifyDao.upsert(mock);
        mizarUserNotifyDao.load(upsert.getId());
        assertNotNull(upsert.getId());
        boolean flag = mizarUserNotifyDao.updateFlag(upsert.getId(), "flag");
        assertTrue(flag);

        MizarUserNotify load = mizarUserNotifyDao.load(upsert.getId());
        assertTrue(load.getFlag());

    }

}