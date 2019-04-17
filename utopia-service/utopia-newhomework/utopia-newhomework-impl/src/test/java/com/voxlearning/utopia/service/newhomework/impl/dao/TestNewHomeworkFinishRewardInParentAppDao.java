package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author shiwe.liao
 * @since 2016-8-26
 */
@DropMongoDatabase
public class TestNewHomeworkFinishRewardInParentAppDao extends NewHomeworkUnitTestSupport {

    @Inject
    private NewHomeworkFinishRewardInParentAppDao newHomeworkFinishRewardInParentAppDao;

    @Test
    public void test(){
        List<NewHomeworkFinishRewardInParentApp> rewardInParentAppList = new ArrayList<>();
        for(Long userId = 0L;userId<10L;userId ++ ){
            Date expire = DateUtils.calculateDateDay(new Date(),30);
            newHomeworkFinishRewardInParentAppDao.addRewardInteger(userId,"11111",userId,1,expire);
            newHomeworkFinishRewardInParentAppDao.addRewardInteger(userId,"11112",userId,2,expire);
            newHomeworkFinishRewardInParentAppDao.addRewardInteger(userId,"11113",userId,3,expire);
            newHomeworkFinishRewardInParentAppDao.addRewardInteger(userId,"11114",userId,4,expire);
            newHomeworkFinishRewardInParentAppDao.addRewardInteger(userId,"11115",userId,5,expire);
            newHomeworkFinishRewardInParentAppDao.addRewardInteger(userId,"11111",userId,1,expire);
        }
        newHomeworkFinishRewardInParentAppDao.inserts(rewardInParentAppList);
        NewHomeworkFinishRewardInParentApp rewardInParentApp0 = newHomeworkFinishRewardInParentAppDao.load(0L);
        Assert.assertEquals(5,rewardInParentApp0.getNotReceivedRewardMap().keySet().size());
        NewHomeworkFinishRewardInParentApp rewardInParentApp1 = newHomeworkFinishRewardInParentAppDao.load(1L);
        Assert.assertEquals(5,rewardInParentApp1.getNotReceivedRewardMap().keySet().size());
        newHomeworkFinishRewardInParentAppDao.updateBeforeReceivedInteger(0L,"11111");
        rewardInParentApp0 = newHomeworkFinishRewardInParentAppDao.load(0L);
        Assert.assertEquals(4,rewardInParentApp0.getNotReceivedRewardMap().keySet().size());
        Assert.assertTrue(!rewardInParentApp0.getNotReceivedRewardMap().containsKey("11111"));
        Assert.assertEquals(1,rewardInParentApp0.getHadReceivedRewardMap().keySet().size());
        Assert.assertTrue(rewardInParentApp0.getHadReceivedRewardMap().containsKey("11111"));
    }
}
