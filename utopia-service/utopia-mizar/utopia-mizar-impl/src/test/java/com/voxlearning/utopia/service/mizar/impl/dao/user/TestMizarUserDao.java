package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import javax.inject.Inject;
import java.util.List;

/**
 *
 * Created by alex on 2016/8/16.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestMizarUserDao {


    @Inject private MizarUserDao mizarUserDao;

    @Test
    public void testFindAll(){

        MizarUser user = new MizarUser();
        user.setAccountName("A0001");
        user.setPassword("P0001");
        mizarUserDao.insert(user);

        user = new MizarUser();
        user.setAccountName("A0002");
        user.setPassword("P0002");
        mizarUserDao.insert(user);

        List<MizarUser> userList = mizarUserDao.findAll();
        Assert.assertEquals(userList.size(), 2);
    }

    @Test
    public void testFindByAccount(){

        MizarUser user = new MizarUser();
        user.setAccountName("A0001");
        user.setPassword("P0001");
        mizarUserDao.insert(user);

        user = new MizarUser();
        user.setAccountName("A0002");
        user.setPassword("P0002");
        mizarUserDao.insert(user);

        user = mizarUserDao.findByAccount("A0001");
        Assert.assertNotNull(user);
    }

    @Test
    public void testCloseAccount(){

        MizarUser user = new MizarUser();
        user.setAccountName("A0001");
        user.setPassword("P0001");
        mizarUserDao.insert(user);

        user = new MizarUser();
        user.setAccountName("A0002");
        user.setPassword("P0002");
        mizarUserDao.insert(user);

        user = mizarUserDao.findByAccount("A0001");
        Assert.assertNotNull(user);
        mizarUserDao.closeAccount(user.getId());
        user = mizarUserDao.findByAccount("A0001");
        Assert.assertNull(user);
    }
}