package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementStatus;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiUserAchievementRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.*;

/**
 * @author peng.zhang.a
 * @since 16-7-26
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiUserAchievementRecordPersistence {

    @Inject AfentiUserAchievementRecordPersistence afentiUserAchievementRecordPersistence;

    Long userId = 1L;
    Subject subject = Subject.MATH;
    Long inviteUserId = 11L;
    Integer currentCumulativeNum = 1;
    Integer level = 1;

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiUserAchievementRecord.class)
    public void findByUserIdAndSubject() throws Exception {
        AfentiUserAchievementRecord afentiUserAchievementRecord = AfentiUserAchievementRecord.newInstance(
                userId, AchievementType.LOGIN, level, subject, AchievementStatus.NOT_ACHIEVED);
        Long id = afentiUserAchievementRecordPersistence.persist(afentiUserAchievementRecord);

        Assert.assertNotNull(id);

        List<AfentiUserAchievementRecord> list = afentiUserAchievementRecordPersistence.find(userId, subject);
        Assert.assertNotNull(list);
        Assert.assertEquals(list.get(0).getId(), id);

        //缓存加载
        list = afentiUserAchievementRecordPersistence.find(userId, subject);
        Assert.assertEquals(list.get(0).getId(), id);
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiUserAchievementRecord.class)
    public void findMaxLevelByUserIds() {
        Set<Long> userIds = new HashSet<>();
        long nowTime = System.currentTimeMillis();
        for (int i = 0; i <= 10; i++) {
            for (int j = 1; j <= 5; j++) {
                AfentiUserAchievementRecord afentiUserAchievementRecord = AfentiUserAchievementRecord.newInstance(
                        userId + i, AchievementType.LOGIN, j, subject, AchievementStatus.NOT_ACHIEVED);
                afentiUserAchievementRecord.setCreateDatetime(new Date(nowTime + j * 10000));
                afentiUserAchievementRecordPersistence.persist(afentiUserAchievementRecord);
                userIds.add(userId + i);
            }
        }
        Map<Long, Integer> map = afentiUserAchievementRecordPersistence.findMaxLevelByUserIds(userIds, subject, AchievementType.LOGIN);

        Assert.assertEquals(map.size(), 11);
        map.forEach((key, value) -> {
            Assert.assertEquals(value.intValue(), 5);
        });
        System.out.println(map);


    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiUserAchievementRecord.class)
    public void updateStatus() throws Exception {
        AfentiUserAchievementRecord afentiUserAchievementRecord = AfentiUserAchievementRecord.newInstance(
                userId, AchievementType.LOGIN, level, subject, AchievementStatus.NOT_ACHIEVED);
        Long id = afentiUserAchievementRecordPersistence.persist(afentiUserAchievementRecord);

        Assert.assertNotNull(id);

        boolean resultFlag = afentiUserAchievementRecordPersistence.updateStatus(id, AchievementStatus.RECEIVED);
        afentiUserAchievementRecord = afentiUserAchievementRecordPersistence.load(id);

        Assert.assertTrue(resultFlag);
        Assert.assertEquals(AchievementStatus.RECEIVED, afentiUserAchievementRecord.getStatus());
    }

}