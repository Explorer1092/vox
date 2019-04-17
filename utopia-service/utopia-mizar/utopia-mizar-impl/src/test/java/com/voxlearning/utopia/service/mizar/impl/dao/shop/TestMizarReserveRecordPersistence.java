package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarReserveRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Yuechen.Wang on 2016/8/22.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestMizarReserveRecordPersistence {

    @Inject MizarReserveRecordPersistence mizarReserveRecordPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = MizarReserveRecord.class)
    public void loadByParentId() throws Exception {
        for (int i = 0; i < 10; ++i) {
            MizarReserveRecord mock = MizarReserveRecord.mockInstance();
            mock.setParentId(1L + i % 3);
            mizarReserveRecordPersistence.upsert(mock);
        }

        assertEquals(4, mizarReserveRecordPersistence.loadByParentId(1L).size());

        String key = MizarReserveRecord.ck_parentId(1L);
        CacheObject<List<MizarReserveRecord>> cacheObject = mizarReserveRecordPersistence.getCache().get(key);
        assertNotNull(cacheObject.getValue());
        assertEquals(4, cacheObject.getValue().size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = MizarReserveRecord.class)
    public void loadByShopId() throws Exception {
        String[] index = new String[]{"A", "B", "C"};
        for (int i = 0; i < 10; ++i) {
            MizarReserveRecord mock = MizarReserveRecord.mockInstance();
            mock.setShopId(index[i % 3]);
            mizarReserveRecordPersistence.upsert(mock);
        }

        assertEquals(3, mizarReserveRecordPersistence.loadByShopId("B").size());

        String key = MizarReserveRecord.ck_shopId("B");
        CacheObject<List<MizarReserveRecord>> cacheObject = mizarReserveRecordPersistence.getCache().get(key);
        assertNotNull(cacheObject.getValue());
        assertEquals(3, cacheObject.getValue().size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = MizarReserveRecord.class)
    public void loadByMobileAndShopId() throws Exception {
        String[] index = new String[]{"A", "B", "C"};
        for (int i = 0; i < 10; ++i) {
            MizarReserveRecord mock = MizarReserveRecord.mockInstance();
            mock.setShopId(index[i % 3]);
            mock.setMobile("1" + i % 3);
            mizarReserveRecordPersistence.upsert(mock);
        }

        assertEquals(3, mizarReserveRecordPersistence.loadByMobileAndShopId("11", "C").size());

        String key = MizarReserveRecord.ck_mobileAndShopId("11", "C");
        CacheObject<List<MizarReserveRecord>> cacheObject = mizarReserveRecordPersistence.getCache().get(key);
        assertNotNull(cacheObject.getValue());
        assertEquals(3, cacheObject.getValue().size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = MizarReserveRecord.class)
    public void updateStatusTest() throws Exception {
        for (int i = 0; i < 10; ++i) {
            MizarReserveRecord mock = MizarReserveRecord.mockInstance();
            mock.setShopId("S" + (i % 3));
            mizarReserveRecordPersistence.upsert(mock);
        }
        mizarReserveRecordPersistence.loadByShopIds(Arrays.asList("S0", "S2"));

        String key = MizarReserveRecord.ck_shopId("S0");
        CacheObject<List<MizarReserveRecord>> cacheObject = mizarReserveRecordPersistence.getCache().get(key);
        assertNotNull(cacheObject.getValue());
        assertEquals(4, cacheObject.getValue().size());

        List<Long> recordIds = cacheObject.getValue().stream().map(MizarReserveRecord::getId).collect(Collectors.toList());
        mizarReserveRecordPersistence.updateStatus(recordIds, MizarReserveRecord.Status.Access);

        cacheObject = mizarReserveRecordPersistence.getCache().get(key);
        assertNull(cacheObject.getValue());

        List<MizarReserveRecord> loads = new ArrayList<>(mizarReserveRecordPersistence.loads(recordIds).values());
        for (MizarReserveRecord r : loads) {
            assertEquals(MizarReserveRecord.Status.Access, r.getStatus());
        }

    }
}