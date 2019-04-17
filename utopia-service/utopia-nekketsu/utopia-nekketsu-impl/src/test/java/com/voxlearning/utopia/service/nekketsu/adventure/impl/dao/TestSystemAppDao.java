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
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestSystemAppDao {
    @Inject private SystemAppDao systemAppDao;

    @Test
    public void testSystemAppDao() throws Exception {
        List<SystemApp> list = systemAppDao.findAllSystemApps();
        assertEquals(0, list.size());

        SystemApp inst = new SystemApp();
        inst.setId(1L);
        inst.setValid(Boolean.TRUE);
        systemAppDao.insert(inst);

        list = systemAppDao.findAllSystemApps();
        assertEquals(1, list.size());
        assertTrue(list.get(0).isValidTrue());

        systemAppDao.changeSystemAppValid(1L);
        list = systemAppDao.findAllSystemApps();
        assertEquals(1, list.size());
        assertFalse(list.get(0).isValidTrue());

        systemAppDao.delete(1L);
        list = systemAppDao.findAllSystemApps();
        assertEquals(0, list.size());
    }
}
