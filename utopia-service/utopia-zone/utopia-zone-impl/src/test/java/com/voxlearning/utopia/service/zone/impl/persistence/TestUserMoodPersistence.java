/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.IgnoreFlushCache;
import com.voxlearning.alps.dao.mysql.support.TruncateCommonVersionTable;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.entity.UserMood;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@IgnoreFlushCache
@TruncateCommonVersionTable
@TruncateDatabaseTable(databaseEntities = UserMood.class)
public class TestUserMoodPersistence {

    @Inject private UserMoodPersistence userMoodPersistence;

    @Test
    public void testUserMoodDao() throws Exception {
        UserMood document = new UserMood();
        document.setTitle("得瑟");
        document.setDescription("人不得瑟枉少年，得瑟也是一种资本！");
        document.setImgUrl("foo.gif");
        userMoodPersistence.insert(document);
        Long id = document.getId();
        document = userMoodPersistence.load(id);
        assertNotNull(document);
    }
}
