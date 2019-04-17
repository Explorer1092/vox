package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Yuechen.Wang on 2016/8/22.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestMizarBrandDao {

    @Inject MizarBrandDao mizarBrandDao;

    @Test
    public void loadByPage() throws Exception {
        String[] index = new String[]{"A", "B", "C"};
        List<String> strings = Arrays.asList("X", "Y", "Z");
        for (int i = 0; i < 10; ++i) {
            MizarBrand mock = new MizarBrand();
            mock.setBrandName(index[i % 3] + i);
            mock.setBrandLogo(index[i % 3] + i);
            mock.setBrandPhoto(strings);
            mizarBrandDao.insert(mock);
        }
        Pageable page = new PageRequest(0, 5);
        Page<MizarBrand> brandPage = mizarBrandDao.loadByPage(page, null);
        assertEquals(5, brandPage.getContent().size());

        brandPage = mizarBrandDao.loadByPage(page, "A");
        assertEquals(4, brandPage.getContent().size());

        MizarBrand brand = brandPage.getContent().get(2);
        assertTrue(StringUtils.contains(brand.getBrandName(), "A"));
        assertEquals(strings, brand.getBrandPhoto());
    }

}