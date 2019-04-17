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
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;
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
@TruncateDatabaseTable(databaseEntities = RewardCategory.class)
public class TestRewardCategoryDao {

    @Inject private RewardCategoryDao rewardCategoryDao;

    @Test
    public void testRewardCategoryDao() throws Exception {
        RewardCategory category = new RewardCategory();
        category.setCategoryName("文具类");
        category.setProductType(RewardProductType.JPZX_SHIWU.name());
        category.setDisplay(true);
        category.setParentId(0L);
        category.setTeacherVisible(true);
        category.setStudentVisible(true);
        rewardCategoryDao.insert(category);
        Long id = category.getId();
        category = rewardCategoryDao.load(id);
        assertEquals("文具类", category.getCategoryName());
    }
}
