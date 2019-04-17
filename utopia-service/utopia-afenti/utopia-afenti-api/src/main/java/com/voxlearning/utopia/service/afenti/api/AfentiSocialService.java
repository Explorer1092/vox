package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 社交相关的服务，邀请，排行榜以及成就等
 *
 * @author peng.zhang.a
 * @since 16-7-19
 */
@ServiceVersion(version = "20160721")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AfentiSocialService extends IPingable {

    MapMessage inviteNewUser(StudentDetail student, Long classmateId, Subject subject);

    MapMessage clickLiked(StudentDetail studentDetail, Long likedUserId, Subject subject, AfentiRankType afentiRankType);

    MapMessage loadUserInvitationMsg(StudentDetail student, Subject subject);

    MapMessage loadUserAchievements(StudentDetail student, Subject subject);

    MapMessage loadLearningRank(User user, Subject subject);

    MapMessage fetchPopupMessage(StudentDetail studentDetail, Subject subject);

    MapMessage fetchMaxLevelClassmates(StudentDetail student, Subject subject, AchievementType achievementType, Integer level);

    MapMessage receiveAchievement(StudentDetail student, Subject subject, AchievementType achievementType, Integer level);

    MapMessage refreshAchievement(Long refershUserId, Subject subject);
}
