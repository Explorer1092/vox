/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.adventure.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.UserAdventure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserAdventureDao {

    @Inject private UserAdventureDao userAdventureDao;

    @Test
    @MockBinder(
            type = UserAdventure.class,
            jsons = "{'id':30009}",
            persistence = UserAdventureDao.class
    )
    public void testChangeBookStagesId() throws Exception {
        long userId = 30009;
        userAdventureDao.changeBookStagesId(userId, "A-1", 1L);
        userAdventureDao.changeBookStagesId(userId, "A-2", 2L);
        userAdventureDao.changeBookStagesId(userId, "B-1", 1L);
        UserAdventure inst = userAdventureDao.load(userId);
        assertEquals("B-1", inst.getBookStagesId());
        assertEquals(2, inst.getBookIds().size());
        assertTrue(inst.getBookIds().contains(1L));
        assertTrue(inst.getBookIds().contains(2L));
    }

    @Test
    @MockBinder(
            type = UserAdventure.class,
            jsons = "{'id':30009}",
            persistence = UserAdventureDao.class
    )
    public void testIncreaseBeanAndPkVitalityCount() throws Exception {
        long userId = 30009;
        userAdventureDao.increaseBeanAndPkVitalityCount(userId, 13, 17);
        UserAdventure inst = userAdventureDao.load(userId);
        assertEquals(13, inst.getTotalBeans().intValue());
        assertEquals(17, inst.getTotalPkVitality().intValue());
    }

    @Test
    @MockBinder(
            type = UserAdventure.class,
            jsons = "{'id':30009}",
            persistence = UserAdventureDao.class
    )
    public void testIncreaseCrown() throws Exception {
        long userId = 30009;
        userAdventureDao.increaseCrown(userId, 31);
        UserAdventure inst = userAdventureDao.load(userId);
        assertEquals(31, inst.getCurrentCrown().intValue());
    }
}
