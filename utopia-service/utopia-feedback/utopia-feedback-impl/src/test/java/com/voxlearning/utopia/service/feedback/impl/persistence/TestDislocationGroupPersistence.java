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

package com.voxlearning.utopia.service.feedback.impl.persistence;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * @author fugui.chang
 * @since 2016-10-13 10:18
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = DislocationGroup.class)
public class TestDislocationGroupPersistence {

    @Inject private DislocationGroupPersistence dislocationGroupPersistence;

    @Test
    public void testLoadByGroupId() {
        DislocationGroup dislocationGroup = new DislocationGroup();
        dislocationGroup.setGroupId(20000L);
        dislocationGroup.setRealSchoolId(30000L);
        dislocationGroup.setNotes("Notes");
        dislocationGroup.setLatestOperator("LatestOperator");
        dislocationGroupPersistence.insert(dislocationGroup);
        dislocationGroup = dislocationGroupPersistence.loadByGroupId(20000L);
        Assert.assertNotNull(dislocationGroup);
        Assert.assertEquals(30000L, dislocationGroup.getRealSchoolId().longValue());
        Assert.assertEquals("Notes", dislocationGroup.getNotes());
        Assert.assertEquals("LatestOperator", dislocationGroup.getLatestOperator());
        Assert.assertEquals(false, dislocationGroup.getDisabled());
    }


    @Test
    public void testLoadByRealSchoolId() {
        DislocationGroup dislocationGroup = new DislocationGroup();
        dislocationGroup.setGroupId(20000L);
        dislocationGroup.setRealSchoolId(30000L);
        dislocationGroup.setNotes("Notes");
        dislocationGroup.setLatestOperator("LatestOperator");
        dislocationGroupPersistence.insert(dislocationGroup);

        dislocationGroup = new DislocationGroup();
        dislocationGroup.setGroupId(20001L);
        dislocationGroup.setRealSchoolId(30000L);
        dislocationGroup.setNotes("Notes");
        dislocationGroup.setLatestOperator("LatestOperator");
        dislocationGroupPersistence.insert(dislocationGroup);

        dislocationGroup = new DislocationGroup();
        dislocationGroup.setGroupId(20002L);
        dislocationGroup.setRealSchoolId(30000L);
        dislocationGroup.setNotes("Notes");
        dislocationGroup.setLatestOperator("LatestOperator");
        dislocationGroup.setDisabled(true);
        dislocationGroupPersistence.insert(dislocationGroup);


        List<DislocationGroup> dislocationGroups = dislocationGroupPersistence.findByRealSchoolId(30000L);
        Assert.assertEquals(2, dislocationGroups.size());
    }


    @Test
    public void testLoadByTime() {
        DislocationGroup dislocationGroup = new DislocationGroup();
        dislocationGroup.setGroupId(20000L);
        dislocationGroup.setRealSchoolId(30000L);
        dislocationGroup.setNotes("Notes");
        dislocationGroup.setLatestOperator("LatestOperator");
        Date date = new Date();
        dislocationGroup.setUpdateDatetime(date);
        dislocationGroupPersistence.insert(dislocationGroup);

        Date beginTime = DateUtils.addDays(date, -1);
        Date endTime = DateUtils.addDays(date, 1);
        List<DislocationGroup> dislocationGroups = dislocationGroupPersistence.findByTime(beginTime, endTime);
        Assert.assertEquals(1, dislocationGroups.size());

        beginTime = DateUtils.addDays(date, 1);
        endTime = DateUtils.addDays(date, 2);
        dislocationGroups = dislocationGroupPersistence.findByTime(beginTime, endTime);
        Assert.assertEquals(0, dislocationGroups.size());

        beginTime = DateUtils.addDays(date, -2);
        endTime = DateUtils.addDays(date, -1);
        dislocationGroups = dislocationGroupPersistence.findByTime(beginTime, endTime);
        Assert.assertEquals(0, dislocationGroups.size());

    }

    @Test
    public void testDisableByGroupId() throws Exception {
        DislocationGroup dislocationGroup = new DislocationGroup();
        dislocationGroup.setGroupId(20000L);
        dislocationGroup.setRealSchoolId(30000L);
        dislocationGroup.setNotes("Notes");
        dislocationGroup.setLatestOperator("LatestOperator");
        Date date = new Date();
        dislocationGroup.setUpdateDatetime(date);
        dislocationGroupPersistence.insert(dislocationGroup);
        dislocationGroup = dislocationGroupPersistence.loadByGroupId(20000L);
        Assert.assertNotNull(dislocationGroup);

        dislocationGroupPersistence.disableByGroupId(20000L, "disabled", "operator");
        dislocationGroup = dislocationGroupPersistence.loadByGroupId(20000L);
        Assert.assertNull(dislocationGroup);
    }


}
