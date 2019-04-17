package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.service.MizarUserService;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import javax.inject.Inject;

/**
 * Created by wangshichao on 16/9/7.
 */

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserService {

    @Inject
    private MizarUserService mizarUserService;

    @Inject
    private MizarUserDao mizarUserDao;

    @Test
    public void  testEditePassWord(){

        MizarUser mizarUser = new MizarUser();
        mizarUser.setAccountName("13126");
        mizarUser.setId("A0001");
        mizarUser.setMobile("13126");
        mizarUser.setPassword("123");
        mizarUser.setPasswordSalt("123");
        mizarUserDao.insert(mizarUser);
        mizarUserService.editMizarUserPassWord("A0001","321");
        String codecPassWord = DigestUtils.sha1Hex("321"+"123");
        MizarUser mizarUser1 = mizarUserDao.findById("A0001");
        Assert.assertTrue(mizarUser1.getPassword().equals(codecPassWord));
    }

    @Test
    public void  testLogin(){

        String passWord = "123";
        String userName = "13126";
        String mobile = "13126678097";
        String  salt = "123";
        String codecPassWord = DigestUtils.sha1Hex(passWord+salt);
        MizarUser mizarUser = new MizarUser();
        mizarUser.setAccountName(userName);
        mizarUser.setId("A0001");
        mizarUser.setMobile(mobile);
        mizarUser.setPassword(codecPassWord);
        mizarUser.setPasswordSalt("123");
        mizarUserDao.insert(mizarUser);
        Assert.assertNotNull(mizarUserService.login(mobile,passWord));
    }
}
