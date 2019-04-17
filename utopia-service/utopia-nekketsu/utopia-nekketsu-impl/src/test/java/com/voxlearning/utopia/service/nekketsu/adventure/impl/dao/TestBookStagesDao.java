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
import com.voxlearning.utopia.service.nekketsu.adventure.entity.BookStages;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestBookStagesDao {

    @Inject private BookStagesDao bookStagesDao;

    @Test
    @MockBinder(
            type = BookStages.class,
            jsons = "{'id':'1_1'}",
            persistence = BookStagesDao.class
    )
    public void testAddStage() throws Exception {
        Stage stage = new Stage();
        stage.setOrder(1);
        bookStagesDao.addStage(1L, 1L, stage);
        BookStages bs = bookStagesDao.load("1_1");
        assertEquals(1, bs.getStages().size());
        assertTrue(bs.getStages().containsKey(1));
    }
}
