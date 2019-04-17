package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.constants.MizarShopStatusType;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Yuechen.Wang on 2016/11/4.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestMizarShopDao {

    @Inject private MizarShopDao mizarShopDao;

    @Test
    public void loadShopByIds() throws Exception {
        for (int i = 0; i < 10; ++i) {
            MizarShop mock = new MizarShop();
            mock.setBrandId("B" + (i % 3));
            mock.setShopStatus(MizarShopStatusType.ONLINE.getName());
            mizarShopDao.insert(mock);
        }

        Map<String, List<MizarShop>> resultMap = mizarShopDao.loadShopByIds(Arrays.asList("B0", "B1", "B2", "B3"));

        assertNotNull(resultMap.get("B0"));
        assertEquals(4, resultMap.get("B0").size());
        assertNull(resultMap.get("B3"));

        String key = MizarShop.ck_brand("B0");

        CacheObject<List<String>> cacheObject = mizarShopDao.getCache().get(key);
        assertNotNull(cacheObject.getValue());
        assertEquals(4, cacheObject.getValue().size());

    }

}