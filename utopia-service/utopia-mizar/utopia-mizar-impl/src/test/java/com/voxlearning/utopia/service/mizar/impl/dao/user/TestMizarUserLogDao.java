package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserLog;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangshichao on 16/9/7.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestMizarUserLogDao {

    @Inject
    private MizarUserLogDao mizarUserLogDao;


    @Test
    public void  testInsert(){

        MizarUserLog mizarUserLog = new MizarUserLog();
        mizarUserLog.setId("log001");
        mizarUserLog.setUserId("A001");
        Map<String,Object> map = new HashMap();
        MizarUser before = new MizarUser();
        before.setPassword("12");
        before.setAccountName("131");
        MizarUser after = new MizarUser();
        after.setPassword("13");
        after.setAccountName("131");
        map.put("before",before);
        map.put("after",after);
        mizarUserLog.setContent(map);
        mizarUserLogDao.insert(mizarUserLog);
        Assert.assertNotNull(mizarUserLogDao.$load("log001"));
    }
}
