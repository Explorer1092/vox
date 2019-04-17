package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.zone.impl.dao.RecordLikeInfoDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author shiwei.liao
 * @since 2018-3-9
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestRecordLikeInfoDao {

    @Inject
    private RecordLikeInfoDao recordLikeInfoDao;


    @Test
    public void test(){
        Long userId = 1111L;
        String id ="1111";

        recordLikeInfoDao.liked(UserLikeType.TERM_BEGIN_ACTIVITY,id,userId,userId.toString(),new Date());

        RecordLikeInfo recordLikeInfo = recordLikeInfoDao.loadRecordLikeInfo(UserLikeType.TERM_BEGIN_ACTIVITY, id);
        Assert.assertNotNull(recordLikeInfo);
        Assert.assertEquals(1,recordLikeInfo.getLikerNames().size());
        Assert.assertEquals(id,recordLikeInfo.getLikerNames().iterator().next());
    }
}
