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

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.zone.api.ClazzJournalLoader;
import com.voxlearning.utopia.service.zone.api.PersonalZoneLoader;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneComment;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;
import com.voxlearning.utopia.service.zone.api.mapper.JournalPagination;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzJournalPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneCommentPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.LikeDetailPersistence;
import com.voxlearning.utopia.service.zone.impl.support.AbstractClazzJournalLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("com.voxlearning.utopia.service.zone.impl.loader.ClazzJournalLoaderImpl")
@ExposeService(interfaceClass = ClazzJournalLoader.class)
public class ClazzJournalLoaderImpl extends AbstractClazzJournalLoader {

    @Inject private ClazzJournalPersistence clazzJournalPersistence;
    @Inject private ClazzZoneCommentPersistence clazzZoneCommentPersistence;
    @Inject private LikeDetailPersistence likeDetailPersistence;

    @Inject private PersonalZoneLoaderImpl personalZoneLoader;

    @Inject private UserLoaderClient userLoaderClient;

    @Override
    protected PersonalZoneLoader getPersonalZoneLoader() {
        return personalZoneLoader;
    }

    @Override
    public Map<Long, List<LikeDetail>> loadJournalLikeDetails(Collection<Long> journalIds) {
        journalIds = CollectionUtils.toLinkedHashSet(journalIds);
        if (journalIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return likeDetailPersistence.findByJournalIds(journalIds);
    }

    @Override
    public List<LikeDetail> loadUserJournalLikeDetails(Long journalOwnerId) {
        if (journalOwnerId == null) return Collections.emptyList();
        return likeDetailPersistence.findByJournalOwnerId(journalOwnerId);
    }

    @Override
    public Map<Long, List<ClazzZoneComment>> loadJournalComments(Collection<Long> journalIds) {
        journalIds = CollectionUtils.toLinkedHashSet(journalIds);
        if (journalIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return clazzZoneCommentPersistence.findByJournalIds(journalIds);
    }

    @Override
    public List<ClazzZoneComment> loadUserJournalComments(Long journalOwnerId) {
        if (journalOwnerId == null) return Collections.emptyList();
        return clazzZoneCommentPersistence.findByJournalOwnerId(journalOwnerId);
    }

    @Override
    public Map<Long, ClazzJournal> loadClazzJournals(Collection<Long> ids) {
        return clazzJournalPersistence.loads(ids);
    }

    @Override
    public Set<ClazzJournal.ComplexID> __queryByClazzId(Long clazzId) {
        if (clazzId == null) {
            return Collections.emptySet();
        }
        return clazzJournalPersistence.queryByClazzId(clazzId);
    }

    @Override
    public Set<ClazzJournal.ComplexID> __queryByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        return clazzJournalPersistence.queryByUserId(userId);
    }

    @Override
    public Set<ClazzJournal.ComplexID> __queryByJournalType(ClazzJournalType journalType) {
        if (journalType == null) {
            return Collections.emptySet();
        }
        return clazzJournalPersistence.queryByJournalType(journalType.getId());
    }

    @Override
    public JournalPagination getClazzJournals(Long userId, Long clazzId, int page, int size, ClazzJournalCategory category, Collection<Long> groupIds) {
        return $getClazzJournals(userId, clazzId, page, size, category, groupIds, userLoaderClient);
    }

    @Override
    public JournalPagination getClazzJournals(Long studentId, int page, int size) {
        return $getClazzJournals(studentId, page, size, userLoaderClient);
    }
}
