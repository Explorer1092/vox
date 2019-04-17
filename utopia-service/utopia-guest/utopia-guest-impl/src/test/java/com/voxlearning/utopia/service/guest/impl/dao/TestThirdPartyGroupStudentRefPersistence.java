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

package com.voxlearning.utopia.service.guest.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.utopia.service.guest.impl.support.GuestUnitTestSupport;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupStudentRef;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author xuesong.zhang
 * @since 2016/9/21
 */
public class TestThirdPartyGroupStudentRefPersistence extends GuestUnitTestSupport {

    @Inject private ThirdPartyGroupStudentRefPersistence thirdPartyGroupStudentRefPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupStudentRef.class)
    public void testFindByGroupId() throws Exception {
        Long groupId = 1L;
        thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, 1L));
        assertEquals(1, thirdPartyGroupStudentRefPersistence.findByGroupId(groupId).size());
        thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, 2L));
        assertEquals(2, thirdPartyGroupStudentRefPersistence.findByGroupId(groupId).size());
        thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, 3L));
        assertEquals(3, thirdPartyGroupStudentRefPersistence.findByGroupId(groupId).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupStudentRef.class)
    public void testFindByGroupIds() throws Exception {
        List<Long> groupIds = new ArrayList<>();
        groupIds.add(1L);
        groupIds.add(2L);
        groupIds.add(3L);

        for (Long groupId : groupIds) {
            thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, groupId * 10));
            thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, groupId * 10 + 1));
            thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, groupId * 10 + 2));
        }

        Map<Long, List<ThirdPartyGroupStudentRef>> refs = thirdPartyGroupStudentRefPersistence.findByGroupIds(groupIds);
        assertEquals(3, refs.size());
        for (Long groupId : groupIds) {
            assertEquals(3, refs.get(groupId).size());
        }

        for (Long groupId : groupIds) {
            thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, groupId * 10 + 3));
        }

        refs = thirdPartyGroupStudentRefPersistence.findByGroupIds(groupIds);
        assertEquals(3, refs.size());
        for (Long groupId : groupIds) {
            assertEquals(4, refs.get(groupId).size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupStudentRef.class)
    public void testFindByUserId() throws Exception {
        Long groupId = 1L;
        thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, 1L));
        assertEquals(1, thirdPartyGroupStudentRefPersistence.findByUserId(1L).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupStudentRef.class)
    public void testFindByUserIds() throws Exception {
        Long groupId = 1L;
        thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, 1L));
        thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, 2L));
        thirdPartyGroupStudentRefPersistence.insert(ThirdPartyGroupStudentRef.newInstance(groupId, 3L));

        assertEquals(3, thirdPartyGroupStudentRefPersistence.findByUserIds(Arrays.asList(1L, 2L, 3L)).size());
    }


}
