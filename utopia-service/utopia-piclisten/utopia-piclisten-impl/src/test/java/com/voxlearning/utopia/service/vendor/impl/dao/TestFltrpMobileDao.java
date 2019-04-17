package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.piclisten.impl.dao.FltrpMobileDao;
import com.voxlearning.utopia.service.vendor.api.entity.FltrpMobile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * @author xinxin
 * @since 3/20/17.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestFltrpMobileDao {
    @Inject
    private FltrpMobileDao fltrpMobileDao;

    @Test
    @TruncateDatabaseTable(databaseEntities = FltrpMobile.class)
    public void testGetByUserId() {
        FltrpMobile fltrpMobile = new FltrpMobile();
        fltrpMobile.setUserId(255785L);
        fltrpMobile.setChecked(false);
        fltrpMobile.setMobile("13800138000");
        fltrpMobile.setReal(false);
        fltrpMobile.setCreateTime(new Date());
        fltrpMobile.setUpdateTime(new Date());

        fltrpMobileDao.insert(fltrpMobile);

        FltrpMobile fm = fltrpMobileDao.getByUserId(255785L);
        Assert.assertNotNull(fm);
        Assert.assertEquals(fltrpMobile.getMobile(), fm.getMobile());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = FltrpMobile.class)
    public void testGetByMobile() {
        FltrpMobile fltrpMobile = new FltrpMobile();
        fltrpMobile.setUserId(255785L);
        fltrpMobile.setChecked(false);
        fltrpMobile.setMobile("13800138000");
        fltrpMobile.setReal(false);
        fltrpMobile.setCreateTime(new Date());
        fltrpMobile.setUpdateTime(new Date());

        fltrpMobileDao.insert(fltrpMobile);

        List<FltrpMobile> fm = fltrpMobileDao.getByMobile("13800138000");
        Assert.assertTrue(fm.size() > 0);
        Assert.assertEquals(fltrpMobile.getUserId(), fm.get(0).getUserId());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = FltrpMobile.class)
    public void testSetMobileChecked() {
        FltrpMobile fltrpMobile = new FltrpMobile();
        fltrpMobile.setUserId(255785L);
        fltrpMobile.setChecked(false);
        fltrpMobile.setMobile("13800138000");
        fltrpMobile.setReal(false);
        fltrpMobile.setCreateTime(new Date());
        fltrpMobile.setUpdateTime(new Date());

        fltrpMobileDao.insert(fltrpMobile);

        fltrpMobileDao.setMobileChecked(255785L, "13800138000", true);

        FltrpMobile fm = fltrpMobileDao.getByUserId(255785L);
        Assert.assertNotNull(fm);
        Assert.assertTrue(fm.getChecked());
        Assert.assertTrue(fm.getReal());
    }
}
