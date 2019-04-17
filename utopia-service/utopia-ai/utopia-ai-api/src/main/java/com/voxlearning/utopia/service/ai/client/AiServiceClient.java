package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.ai.api.AiService;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.List;

/**
 * Created by Summer on 2018/3/27
 */
public class AiServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);
    @Getter
    @ImportService(interfaceClass = AiService.class)
    AiService remoteReference;

    public MapMessage saveInvitation(Long inviter, Long invitee) {
        if (inviter == null || invitee == null) {
            return MapMessage.errorMessage("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("saveInvitation")
                    .keys(invitee)
                    .callback(() -> remoteReference.saveInvitation(inviter, invitee)
                     )
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在保存，请勿重复提交");
        } catch (Exception ex) {
            logger.error("saveInvitation error. inviter:{}, invitee:{}", inviter, invitee, ex);
            return MapMessage.errorMessage("保存异常");
        }
    }

    public MapMessage loadAndRecordDialogueTalk(User user, String userCode, String input, String lessonId) {
        if (user == null) {
           return MapMessage.errorMessage("请重新登录").add("result", "400").add("message", "请重新登录");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("loadAndRecordDialogueTalk")
                    .keys(userCode)
                    .callback(() -> remoteReference.loadAndRecordDialogueTalk(user, userCode, input, lessonId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请勿重复提交").add("result", "400").add("message", "请勿重复提交");
        } catch (Exception ex) {
            logger.error("saveInvitation error. usercode:{}, input:{}", userCode, input, ex);
            return MapMessage.errorMessage("加载异常").add("result", "400").add("message", "加载异常");
        }
    }

    public MapMessage loadAndRecordTaskTalk(User user, String usercode, String input, String roleName, String lessonId) {
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").add("result", "400").add("message", "请重新登录");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("loadAndRecordTaskTalk")
                    .keys(usercode)
                    .callback(() -> remoteReference.loadAndRecordTaskTalk(user, usercode, input, roleName, lessonId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请勿重复提交").add("result", "400").add("message", "请勿重复提交");
        } catch (Exception ex) {
            logger.error("saveInvitation error. usercode:{}, input:{}", usercode, input, ex);
            return MapMessage.errorMessage("加载异常").add("result", "400").add("message", "加载异常");
        }
    }

    public MapMessage examineUserVideo(String id, AIUserVideo.ExamineStatus from, AIUserVideo.ExamineStatus to, String updater, AIUserVideo.Category category, String description) {
        if (StringUtils.isAnyBlank(id, updater) || to == null) {
            return MapMessage.errorMessage("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("examineUserVideo")
                    .keys(updater)
                    .callback(() -> remoteReference.updateUserVideoStatus(id, from, to, updater, category, description)
                    )
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在保存，请勿重复提交");
        } catch (Exception ex) {
            logger.error("examineUserVideo error.",  ex);
            return MapMessage.errorMessage("保存异常");
        }
    }

    public MapMessage updateFailedUserVideoToPass(String id, String updater, AIUserVideo.Category category, String description) {
        if (StringUtils.isAnyBlank(id, updater)) {
            return MapMessage.errorMessage("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("examineUserVideo")
                    .keys(updater)
                    .callback(() -> remoteReference.updateUserVideoStatus(id, AIUserVideo.ExamineStatus.Failed, AIUserVideo.ExamineStatus.Passed, updater, category, ""))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在保存，请勿重复提交");
        } catch (Exception ex) {
            logger.error("examineUserVideo error.",  ex);
            return MapMessage.errorMessage("保存异常");
        }
    }


    public MapMessage updateUserVideoComment(String id, String updater, String comment, String commentAudio, List<AIUserVideo.Label> labelList, AIUserVideo.Category category) {
        if (StringUtils.isAnyBlank(id, updater)) {
            return MapMessage.errorMessage("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("examineUserVideo")
                    .keys(updater)
                    .callback(() -> remoteReference.updateUserVideoComment(id, comment, commentAudio, labelList, updater, category))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在保存，请勿重复提交");
        } catch (Exception ex) {
            logger.error("examineUserVideo error.",  ex);
            return MapMessage.errorMessage("保存异常");
        }
    }

}
