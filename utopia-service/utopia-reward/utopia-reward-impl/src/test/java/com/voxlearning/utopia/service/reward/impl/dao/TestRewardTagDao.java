/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.reward.constant.RewardTagLevel;
import com.voxlearning.utopia.service.reward.entity.RewardTag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardTag.class)
public class TestRewardTagDao {

    @Inject private RewardTagDao rewardTagDao;

    @Test
    public void testRewardTagDao() throws Exception {
        RewardTag tag = new RewardTag();
        tag.setStudentVisible(true);
        tag.setTeacherVisible(true);
        tag.setTagLevel(RewardTagLevel.ONE_LEVEL.name());
        tag.setTagName("一起作业专属");
        rewardTagDao.insert(tag);
        Long id = tag.getId();
        tag = rewardTagDao.load(id);
        assertEquals("一起作业专属", tag.getTagName());
    }
}
