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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.wechat.api.entities.WechatLittleChampion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WechatLittleChampion.class)
public class TestWechatLittleChampionPersistence {

    @Inject private WechatLittleChampionPersistence wechatLittleChampionPersistence;

    @Test
    public void testFindByTeacherId() throws Exception {
        long teacherId = 10000;
        for (int i = 0; i < 3; i++) {
            WechatLittleChampion champion = new WechatLittleChampion();
            champion.setHelpContent("");
            champion.setStory("");
            champion.setTeacherId(teacherId);
            wechatLittleChampionPersistence.insert(champion);
            assertEquals(i + 1, wechatLittleChampionPersistence.findByTeacherId(teacherId).size());
        }
    }

    @Test
    public void testFindByStudentId() throws Exception {
        long studentId = 30000;
        WechatLittleChampion champion = new WechatLittleChampion();
        champion.setHelpContent("");
        champion.setStory("");
        champion.setStudentId(studentId);
        wechatLittleChampionPersistence.insert(champion);
        assertNotNull(wechatLittleChampionPersistence.findByStudentId(studentId));
    }
}
