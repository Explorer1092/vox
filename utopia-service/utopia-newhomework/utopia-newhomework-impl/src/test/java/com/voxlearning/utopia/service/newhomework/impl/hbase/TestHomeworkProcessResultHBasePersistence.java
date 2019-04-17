package com.voxlearning.utopia.service.newhomework.impl.hbase;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkProcessResultHBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author xuesong.zhang
 * @since 2017/8/14
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestHomeworkProcessResultHBasePersistence {

    @Inject private HomeworkProcessResultHBasePersistence homeworkProcessResultHBasePersistence;

    @Test
    public void testInsert() throws Exception {
        String id = "111-111";
        String hid = "h_1";
        Long userId = 3009L;

        HomeworkProcessResultHBase hprh = new HomeworkProcessResultHBase();
        hprh.setId(id);
        hprh.setHomeworkId(hid);
        hprh.setUserId(userId);

        homeworkProcessResultHBasePersistence.insert(hprh);
        hprh = homeworkProcessResultHBasePersistence.load(id);
        assertEquals(hprh.getHomeworkId(), hid);
    }

    @Test
    public void testLoad() throws Exception {
        String rowKey = "58994361777487989d6d5a5f-1484712078545";
        HomeworkProcessResultHBase processResultHBase = homeworkProcessResultHBasePersistence.load(rowKey);
        System.out.println(processResultHBase);
    }
}
