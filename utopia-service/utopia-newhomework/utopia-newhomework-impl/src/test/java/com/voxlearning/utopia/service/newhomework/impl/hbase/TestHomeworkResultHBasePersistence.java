package com.voxlearning.utopia.service.newhomework.impl.hbase;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultHBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/8/14
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestHomeworkResultHBasePersistence {

    @Inject private HomeworkResultHBasePersistence homeworkResultHBasePersistence;

    @Test
    public void testInsert() throws Exception {
        String id = "20160912-ENGLISH-201609_57d61c4b7774876ca65530b0-333894089";

        HomeworkResultHBase rh = new HomeworkResultHBase();
        rh.setId(id);
        rh.setHomeworkId("201609_57d61c4b7774876ca65530b0");
        rh.setSubject(Subject.ENGLISH);
        rh.setActionId("126605_1473649739536");
        rh.setClazzGroupId(11580L);
        rh.setUserId(333894089L);

        homeworkResultHBasePersistence.insert(rh);
        rh = homeworkResultHBasePersistence.load(id);
        Assert.assertNotNull(rh);
    }

    @Test
    public void testLoad() throws Exception {
        String rowKey = "20170830-ENGLISH-201708_59a669df8555ab0229273604_1-381542902";
        HomeworkResultHBase resultHBase = homeworkResultHBasePersistence.load(rowKey);
        System.out.println(resultHBase);

        Assert.assertEquals(resultHBase.getId(), rowKey);
    }

    @Test
    public void testLoads() throws Exception {
        String id1 = "20170119-MATH-201701_5880961ea3e7504b272c8574-333912345";
        String id2 = "20170119-MATH-201701_588085eeaf81a33178942b42-333912345";
        String id3 = "20170119-MATH-201701_588068f8a3e75029a6d6ffcc-333909868";
        List<String> rowKeys = Arrays.asList(id1, id2, id3);
        Map<String, HomeworkResultHBase> resultHBaseMap = homeworkResultHBasePersistence.loads(rowKeys);
        Assert.assertEquals(resultHBaseMap.size(), 3);

        HomeworkResultHBase hBase = resultHBaseMap.get(id2);
        Assert.assertEquals(hBase.getId(), id2);
    }

}
