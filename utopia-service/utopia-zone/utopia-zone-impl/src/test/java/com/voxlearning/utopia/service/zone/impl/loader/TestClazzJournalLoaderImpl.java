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

package com.voxlearning.utopia.service.zone.impl.loader;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneComment;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzJournalPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneCommentPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.LikeDetailPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestClazzJournalLoaderImpl {

    @Inject private ClazzJournalLoaderImpl clazzJournalLoader;

    @Test
    @TruncateDatabaseTable(databaseEntities = LikeDetail.class)
    @MockBinder(
            type = LikeDetail.class,
            jsons = {
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1}",
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2}",
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3}",
            },
            persistence = LikeDetailPersistence.class
    )
    public void testLoadJournalLikeDetails() throws Exception {
        Set<Long> journalIds = MDP.groupingBy(LikeDetail.class, LikeDetail::getJournalId).keySet();
        Map<Long, List<LikeDetail>> map = clazzJournalLoader.loadJournalLikeDetails(journalIds);
        assertEquals(journalIds.size(), map.size());
        for (Long journalId : journalIds) {
            assertEquals(3, map.get(journalId).size());
            assertEquals(3, clazzJournalLoader.loadJournalLikeDetails(journalId).size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzZoneComment.class)
    @MockBinder(
            type = ClazzZoneComment.class,
            jsons = {
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1,'userName':'','imgComment':0}",
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2,'userName':'','imgComment':0}",
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3,'userName':'','imgComment':0}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1,'userName':'','imgComment':0}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2,'userName':'','imgComment':0}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3,'userName':'','imgComment':0}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1,'userName':'','imgComment':0}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2,'userName':'','imgComment':0}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3,'userName':'','imgComment':0}",
            },
            persistence = ClazzZoneCommentPersistence.class
    )
    public void testLoadJournalComments() throws Exception {
        Set<Long> journalIds = MDP.groupingBy(ClazzZoneComment.class, ClazzZoneComment::getJournalId).keySet();
        Map<Long, List<ClazzZoneComment>> map = clazzJournalLoader.loadJournalComments(journalIds);
        assertEquals(journalIds.size(), map.size());
        for (Long journalId : journalIds) {
            assertEquals(3, map.get(journalId).size());
            assertEquals(3, clazzJournalLoader.loadJournalComments(journalId).size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = {
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            },
            persistence = ClazzJournalPersistence.class
    )
    public void testLoadClazzJournals() throws Exception {
        Set<Long> ids = MDP.toMap(ClazzJournal.class, ClazzJournal::getId).keySet();
        Map<Long, ClazzJournal> map = clazzJournalLoader.loadClazzJournals(ids);
        assertEquals(ids.size(), map.size());
        for (Long id : ids) {
            assertNotNull(map.get(id));
            assertNotNull(clazzJournalLoader.loadClazzJournal(id));
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = {
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            },
            persistence = ClazzJournalPersistence.class
    )
    public void testQueryByClazzId() throws Exception {
        long clazzId = 1;
        assertEquals(3, clazzJournalLoader.__queryByClazzId(clazzId).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = {
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            },
            persistence = ClazzJournalPersistence.class
    )
    public void testQueryByUserId() throws Exception {
        long userId = 1;
        assertEquals(3, clazzJournalLoader.__queryByUserId(userId).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = {
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
                    "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            },
            persistence = ClazzJournalPersistence.class
    )
    public void testQueryByJournalType() throws Exception {
        ClazzJournalType journalType = ClazzJournalType.TEACHER_ASSIGN_HOMEWORK;
        assertEquals(3, clazzJournalLoader.__queryByJournalType(journalType).size());
    }
}
