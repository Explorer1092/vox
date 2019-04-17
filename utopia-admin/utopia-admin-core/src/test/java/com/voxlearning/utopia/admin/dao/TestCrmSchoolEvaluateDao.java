package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.admin.entity.CrmSchoolEvaluate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * 学校评级的测试
 * Created by yaguang.wang on 2017/1/7.
 */

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestCrmSchoolEvaluateDao {
    @Inject private CrmSchoolEvaluateDao crmSchoolEvaluateDao;

    @Test
    public void testFindBySchoolId() {
        CrmSchoolEvaluate crmSchoolEvaluate = new CrmSchoolEvaluate();
        crmSchoolEvaluate.setSchoolId(123L);
        crmSchoolEvaluate.setAccount("111");
        crmSchoolEvaluate.setAccountName("wyg");
        crmSchoolEvaluateDao.insert(crmSchoolEvaluate);

        CrmSchoolEvaluate crmSchoolEvaluate1 = new CrmSchoolEvaluate();
        crmSchoolEvaluate1.setSchoolId(1234L);
        crmSchoolEvaluate1.setAccount("111");
        crmSchoolEvaluate1.setAccountName("wyg");
        crmSchoolEvaluateDao.insert(crmSchoolEvaluate);

        assertEquals(1,crmSchoolEvaluateDao.findBySchoolId(123L).size());
    }
}
