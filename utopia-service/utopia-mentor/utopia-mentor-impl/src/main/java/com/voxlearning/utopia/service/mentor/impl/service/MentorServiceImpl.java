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

package com.voxlearning.utopia.service.mentor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.api.constant.MentorLevel;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import com.voxlearning.utopia.service.business.api.entity.MentorRewardHistory;
import com.voxlearning.utopia.service.mentor.api.MentorService;
import com.voxlearning.utopia.service.mentor.impl.persistence.MentorHistoryPersistence;
import com.voxlearning.utopia.service.mentor.impl.persistence.MentorRewardHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named("com.voxlearning.utopia.service.mentor.impl.service.MentorServiceImpl")
@ExposeService(interfaceClass = MentorService.class)
public class MentorServiceImpl implements MentorService {

    @Inject private MentorHistoryPersistence mentorHistoryPersistence;
    @Inject private MentorRewardHistoryPersistence mentorRewardHistoryPersistence;

    @Override
    public AlpsFuture<MentorHistory> insertMentorHistory(MentorHistory history) {
        mentorHistoryPersistence.insert(history);
        return new ValueWrapperFuture<>(history);
    }

    @Override
    public AlpsFuture<Boolean> setMentorHistorySuccess(Long id) {
        int rows = mentorHistoryPersistence.updateSuccess(id);
        return new ValueWrapperFuture<>(rows > 0);
    }

    @Override
    public AlpsFuture<Boolean> changeMentorHistoryLevel(Long id, MentorLevel mentorLevel) {
        mentorHistoryPersistence.updateLevel(id, mentorLevel);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<List<MentorHistory>> findMentorHistoriesByMentorId(Long mentorId) {
        if (mentorId == null) {
            return new ValueWrapperFuture<>(Collections.emptyList());
        }
        List<MentorHistory> histories = mentorHistoryPersistence.findByMentorId(mentorId);
        return new ValueWrapperFuture<>(histories);
    }

    @Override
    public AlpsFuture<List<MentorHistory>> findMentorHistoriesByMenteeId(Long menteeId) {
        if (menteeId == null) {
            return new ValueWrapperFuture<>(Collections.emptyList());
        }
        List<MentorHistory> histories = mentorHistoryPersistence.findByMenteeId(menteeId);
        return new ValueWrapperFuture<>(histories);
    }

    @Override
    public AlpsFuture<List<MentorRewardHistory>> findMentorRewardHistoriesByMentorId(Long mentorId) {
        if (mentorId == null) {
            return new ValueWrapperFuture<>(Collections.emptyList());
        }
        List<MentorRewardHistory> histories = mentorRewardHistoryPersistence.findByMentorId(mentorId);
        return new ValueWrapperFuture<>(histories);
    }

    @Override
    public AlpsFuture<MentorRewardHistory> persistMentorRewardHistory(MentorRewardHistory history) {
        mentorRewardHistoryPersistence.insert(history);
        return new ValueWrapperFuture<>(history);
    }
}
