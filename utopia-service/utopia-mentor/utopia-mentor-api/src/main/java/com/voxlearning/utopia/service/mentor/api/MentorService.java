package com.voxlearning.utopia.service.mentor.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.api.constant.MentorLevel;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import com.voxlearning.utopia.service.business.api.entity.MentorRewardHistory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.01.19")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MentorService {

    @Async
    AlpsFuture<MentorHistory> insertMentorHistory(MentorHistory history);

    @Async
    AlpsFuture<Boolean> setMentorHistorySuccess(Long id);

    @Async
    AlpsFuture<Boolean> changeMentorHistoryLevel(Long id, MentorLevel mentorLevel);

    @Async
    AlpsFuture<List<MentorHistory>> findMentorHistoriesByMentorId(Long mentorId);

    @Async
    AlpsFuture<List<MentorHistory>> findMentorHistoriesByMenteeId(Long menteeId);

    @Async
    AlpsFuture<List<MentorRewardHistory>> findMentorRewardHistoriesByMentorId(Long mentorId);

    @Async
    AlpsFuture<MentorRewardHistory> persistMentorRewardHistory(MentorRewardHistory history);
}
