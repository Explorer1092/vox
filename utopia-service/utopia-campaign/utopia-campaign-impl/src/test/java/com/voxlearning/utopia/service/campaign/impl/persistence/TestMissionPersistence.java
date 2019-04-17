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

package com.voxlearning.utopia.service.campaign.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.MissionState;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.entity.mission.Mission;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = Mission.class)
public class TestMissionPersistence {

    @Inject private MissionPersistence missionPersistence;

    @Test
    public void testQueryLocations() throws Exception {
        Mission mission = new Mission();
        mission.setStudentId(1L);
        mission.setWishType(WishType.INTEGRAL);
        mission.setMissionState(MissionState.ONGOING);
        missionPersistence.insert(mission);
        Set<Mission.Location> set = missionPersistence.queryLocations(1L);
        assertEquals(1, set.size());
        set = missionPersistence.queryLocations(Collections.singleton(1L)).get(1L);
        assertEquals(1, set.size());
    }

    @Test
    public void testIncreaseFinishCount() throws Exception {
        Mission mission = new Mission();
        mission.setStudentId(1L);
        mission.setWishType(WishType.INTEGRAL);
        mission.setMissionState(MissionState.ONGOING);
        missionPersistence.insert(mission);
        Long id = mission.getId();
        mission = missionPersistence.load(id);
        assertEquals(0, mission.getFinishCount().intValue());
        assertTrue(missionPersistence.increaseFinishCount(id, 7));
        mission = missionPersistence.load(id);
        assertEquals(7, mission.getFinishCount().intValue());
    }

    @Test
    public void testUpdateComplete() throws Exception {
        Mission mission = new Mission();
        mission.setStudentId(1L);
        mission.setWishType(WishType.INTEGRAL);
        mission.setMissionState(MissionState.ONGOING);
        missionPersistence.insert(mission);
        Long id = mission.getId();
        mission = missionPersistence.load(id);
        assertEquals(MissionState.ONGOING, mission.getMissionState());
        assertNull(mission.getCompleteDatetime());
        assertTrue(missionPersistence.updateComplete(id));
        mission = missionPersistence.load(id);
        assertEquals(MissionState.COMPLETE, mission.getMissionState());
        assertNotNull(mission.getCompleteDatetime());
        assertFalse(missionPersistence.updateComplete(id));
    }

    @Test
    public void testUpdateImg() throws Exception {
        Mission mission = new Mission();
        mission.setStudentId(1L);
        mission.setWishType(WishType.INTEGRAL);
        mission.setMissionState(MissionState.ONGOING);
        mission.setImg("A");
        missionPersistence.insert(mission);
        Long id = mission.getId();
        mission = missionPersistence.load(id);
        assertEquals("A", mission.getImg());
        assertTrue(missionPersistence.updateImg(id, "B"));
        mission = missionPersistence.load(id);
        assertEquals("B", mission.getImg());
    }
}
