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

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiInvitationRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author peng.zhang.a
 * @since 16-7-24
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiInvitationRecordPersistence {

    @Inject AfentiInvitationRecordPersistence invitationRecordPersistence;
    Long userId = 1L;
    Long classmateUserId = 11L;
    Subject subject = Subject.MATH;

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiInvitationRecord.class)
    public void findByUserIdAndSubject() throws Exception {
        AfentiInvitationRecord afentiInvitationRecord = AfentiInvitationRecord.newInstence(userId, classmateUserId, subject);
        Long id = invitationRecordPersistence.persist(afentiInvitationRecord);
        Assert.assertNotNull(id);
        List<AfentiInvitationRecord> list = invitationRecordPersistence.findByUserIdAndSubject(userId, subject);
        Assert.assertNotNull(list);
        Assert.assertEquals(id, list.get(0).getId());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiInvitationRecord.class)
    public void findByInvitedUserIdAndSubject() throws Exception {
        AfentiInvitationRecord afentiInvitationRecord = AfentiInvitationRecord.newInstence(userId, classmateUserId, subject);
        Long id = invitationRecordPersistence.persist(afentiInvitationRecord);
        Assert.assertNotNull(id);
        List<AfentiInvitationRecord> list = invitationRecordPersistence.findByInvitedUserIdAndSubject(classmateUserId, subject);
        Assert.assertNotNull(list);
        Assert.assertEquals(id, list.get(0).getId());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiInvitationRecord.class)
    public void findByUserIdAndInviteUserId() throws Exception {
        AfentiInvitationRecord afentiInvitationRecord = AfentiInvitationRecord.newInstence(userId, classmateUserId, subject);
        Long id = invitationRecordPersistence.persist(afentiInvitationRecord);
        Assert.assertNotNull(id);
        List<AfentiInvitationRecord> list = invitationRecordPersistence.findByUserIdAndSubject(userId, subject)
                .stream()
                .filter(p -> classmateUserId.equals(p.getInvitedUserId()))
                .collect(Collectors.toList());

        Assert.assertNotNull(list);
        Assert.assertEquals(id, list.get(0).getId());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiInvitationRecord.class)
    public void findUnAcceptRecordsByInviteUserId() throws Exception {
        AfentiInvitationRecord afentiInvitationRecord = AfentiInvitationRecord.newInstence(userId, classmateUserId, subject);
        Long id = invitationRecordPersistence.persist(afentiInvitationRecord);
        Assert.assertNotNull(id);
        List<AfentiInvitationRecord> list = invitationRecordPersistence.findByUserIdAndSubject(userId, subject)
                .stream()
                .filter(p -> !p.getAccepted())
                .collect(Collectors.toList());
        Assert.assertNotNull(list);
        Assert.assertEquals(id, list.get(0).getId());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiInvitationRecord.class)
    public void updateAccepted() throws Exception {
        AfentiInvitationRecord afentiInvitationRecord = AfentiInvitationRecord.newInstence(userId, classmateUserId, subject);
        Long id = invitationRecordPersistence.persist(afentiInvitationRecord);
        Assert.assertNotNull(id);
        List<AfentiInvitationRecord> list = invitationRecordPersistence.findByUserIdAndSubject(userId, subject)
                .stream()
                .filter(p -> !p.getAccepted())
                .collect(Collectors.toList());

        Assert.assertNotNull(list);
        Assert.assertEquals(id, list.get(0).getId());
        int num = invitationRecordPersistence.updateAccepted(list);
        Assert.assertEquals(num, 1);
    }
}