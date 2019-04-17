package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer on 2018/3/27
 */
@ServiceVersion(version = "20190122")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface AiService extends IPingable {

    MapMessage processAIQuestionResult(AIUserQuestionContext context);

    MapMessage collectWarmUpResult(User user, String input, String lessonId, String unitId, String qId);

    /**
     * @See saveAIVideoResult(Long userId, String qid, String sessionId, String video)
     */
    @Deprecated
    MapMessage saveAIVideoResult(Long userId, String qid, String video);

    MapMessage handleUserVideo(User user, String qid, List<String> videos);

    /**
     * @param inviter 邀请人
     * @param invitee 被邀请人
     * @return
     */
    MapMessage saveInvitation(Long inviter, Long invitee);

    /**
     * 加载并记录情景对话
     *
     * @param user
     * @param usercode
     * @param input
     * @param lessonId
     * @return
     */
    MapMessage loadAndRecordDialogueTalk(User user, String usercode, String input, String lessonId);

    /**
     * 加载并记录任务对话
     *
     * @param user
     * @param usercode
     * @param input
     * @param roleName
     * @param lessonId
     * @return
     */
    MapMessage loadAndRecordTaskTalk(User user, String usercode, String input, String roleName, String lessonId);

    MapMessage updateUserVideoStatus(String id, AIUserVideo.ExamineStatus from, AIUserVideo.ExamineStatus to, String updater, AIUserVideo.Category category, String description);

    MapMessage updateUserVideoComment(String id, String comment, String commentAudio, List<AIUserVideo.Label> labels, String updater, AIUserVideo.Category category);

    // 记录用户今日总结打卡次数
    MapMessage recordUserShare(String unitId, User user);

    // 用户打卡记录补打卡
    MapMessage recordUserShareDoor(String unitId, String bookId, User user);

    //TODO 修数据用
    MapMessage fixClazzUserCount(Long clazzId, Integer number);

    //TODO 修数据用
    MapMessage fixUserVideo(String message);

    //TODO 修数据用
    MapMessage fixUserDrawingTask(List<Long> drawingTaskIds, Long userId);
}
