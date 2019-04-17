package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarGoodsItem;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Yuechen.Wang on 2016/9/7.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestMizarShopGoodsDao {

    @Inject private MizarShopGoodsDao mizarShopGoodsDao;

    @Test
    public void loadByShopIds() throws Exception {
        for (int i = 0; i < 10; ++i) {
            MizarShopGoods mock = new MizarShopGoods();
            mock.setShopId("S" + (i % 3));
            mizarShopGoodsDao.insert(mock);
        }

        Map<String, List<MizarShopGoods>> resultMap = mizarShopGoodsDao.loadByShopIds(Arrays.asList("S0", "S1", "S2", "S3"));

        assertNotNull(resultMap.get("S0"));
        assertEquals(4, resultMap.get("S0").size());
        assertNull(resultMap.get("S3"));

        String key = MizarShopGoods.ck_shopId("S0");

        CacheObject<List<String>> cacheObject = mizarShopGoodsDao.getCache().get(key);
        assertNotNull(cacheObject.getValue());
        assertEquals(4, cacheObject.getValue().size());
    }

    @Test
    public void cloneTest() throws Exception {
        MizarShopGoods temp = new MizarShopGoods();
        for (int i = 0; i < 5; ++i) {
            MizarShopGoods mock = temp.clone();
            mock.setShopId("C" + (i % 2));
            mizarShopGoodsDao.upsert(mock);
        }
        List<MizarShopGoods> clone = mizarShopGoodsDao.loadByShopId("C0");
        assertEquals(3, clone.size());
    }

    @Test
    public void itemChangeTest() throws Exception {
        MizarShopGoods temp = new MizarShopGoods();
        List<MizarGoodsItem> list = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            MizarGoodsItem item = new MizarGoodsItem();
            item.setItemId("Item" + i);
            item.setRemains(10 * (i + 1));
            list.add(item);
        }
        temp.setItems(list);
        MizarShopGoods upsert = mizarShopGoodsDao.upsert(temp);


        MizarShopGoods test = mizarShopGoodsDao.load(upsert.getId());
        assertNotNull(test);

        test.getItems().stream()
                .filter(item -> "Item0".equals(item.getItemId()))
                .forEach(t -> t.setRemains(t.getRemains() - 1));
        mizarShopGoodsDao.upsert(test);

        test = mizarShopGoodsDao.load(upsert.getId());
        assertNotNull(test);
        MizarGoodsItem first = test.getItems().stream()
                .filter(item -> "Item0".equals(item.getItemId()))
                .findFirst()
                .orElse(null);
        assertNotNull(first);
        assertEquals(9, (long)first.getRemains());

    }

}