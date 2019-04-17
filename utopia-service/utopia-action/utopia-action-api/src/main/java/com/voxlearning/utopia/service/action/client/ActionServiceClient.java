package com.voxlearning.utopia.service.action.client;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.ActionService;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.api.support.UserGrowthReward;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ActionServiceClient {

    @Getter
    @ImportService(interfaceClass = ActionService.class)
    private ActionService remoteReference;

    public void wakeupClassmate(Long userId) {
        if (userId == null) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.WakeupClassmate);
        event.setUserId(userId);
        remoteReference.sendEvent(event);
    }

    public void finishSelfLearning(Long userId) {
        if (userId == null) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.FinishSelfLearning);
        event.setUserId(userId);
        remoteReference.sendEvent(event);
    }

    public void obtainStar(Long userId, int starCount) {
        if (userId == null) return;
        if (starCount == 0) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.ObtainStar);
        event.setUserId(userId);
        event.getAttributes().put("starCount", starCount);
        remoteReference.sendEvent(event);
    }

    public void correctWrongIssue(Long userId, int issueCount) {
        if (userId == null) return;
        if (issueCount == 0) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.CorrectWrongIssue);
        event.setUserId(userId);
        event.getAttributes().put("issueCount", issueCount);
        remoteReference.sendEvent(event);
    }

    public void correctHomework(Long userId) {
        if (userId == null) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.CorrectHomework);
        event.setUserId(userId);
        remoteReference.sendEvent(event);
    }

    public void winPk(Long userId) {
        if (userId == null) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.WinPk);
        event.setUserId(userId);
        remoteReference.sendEvent(event);
    }

    public void finishHomework(Long userId, Subject subject, int homeworkScore) {
        if (userId == null) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.FinishHomework);
        event.setUserId(userId);
        event.getAttributes().put("homeworkScore", homeworkScore);
        event.getAttributes().put("homeworkSubject", subject.name());
        remoteReference.sendEvent(event);
    }

    public void finishMental(Long userId, int count) {
        if (userId == null || count == 0) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.FinishMental);
        event.setUserId(userId);
        event.getAttributes().put("count", count);
        remoteReference.sendEvent(event);
    }

    public void finishOral(Long userId, int count) {
        if (userId == null || count == 0) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.FinishOral);
        event.setUserId(userId);
        event.getAttributes().put("count", count);
        remoteReference.sendEvent(event);
    }

    public void finishReading(Long userId, int count) {
        if (userId == null || count == 0) return;
        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.FinishReading);
        event.setUserId(userId);
        event.getAttributes().put("count", count);
        remoteReference.sendEvent(event);
    }

    public void headlineComment(Long userId, Long relevantUserId, Map<String, Object> extInfo) {
        if (null == userId || null == relevantUserId || 0 == userId || 0 == relevantUserId) {
            return;
        }

        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.HeadlineComment);
        event.setUserId(userId);    //点赞人ID

        event.getAttributes().put("relevantUserId", relevantUserId); //被评论人ID

        if (MapUtils.isNotEmpty(extInfo)) {
            event.getAttributes().putAll(extInfo);
        }

        remoteReference.sendEvent(event);
    }


    public void receiveGrowthLevelReward(Long userId, Integer level) {
        if (null == userId || 0 == userId || null == level || !UserGrowthReward.canReceive(level)) {
            return;
        }

        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.ReceiveGrowthLevelReward);
        event.getAttributes().put("level", level);

        remoteReference.sendEvent(event);
    }

    public void lookHomeworkReport(Long userId, Subject subject) {
        if (null == userId || 0 == userId) {
            return;
        }

        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.LookHomeworkReport);
        event.getAttributes().put("homeworkSubject", subject.name());

        remoteReference.sendEvent(event);
    }

    public void submitZoumeiAnswer(Long userId) {
        if (userId == null)
            return;
        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.SubmitZoumeiAnswer);

        remoteReference.sendEvent(event);
    }

    public void submitAfentiEnglishAnswer(Long userId) {
        if (userId == null)
            return;
        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.SubmitAfentiEnglishAnswer);

        remoteReference.sendEvent(event);
    }

    public void submitAfentiMathAnswer(Long userId) {
        if (userId == null)
            return;
        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.SubmitAfentiMathAnswer);

        remoteReference.sendEvent(event);
    }

    public void submitAfentiChineseAnswer(Long userId) {
        if (userId == null)
            return;
        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.SubmitAfentiChineseAnswer);

        remoteReference.sendEvent(event);
    }

    public void clickSelfStudyEnglishPicListen(Long userId) {
        if (userId == null)
            return;
        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.ClickSelfStudyEnglishPicListen);

        remoteReference.sendEvent(event);
    }

    public void saveSelfStudyChineseTextRead(Long userId) {
        if (userId == null)
            return;
        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.SaveSelfStudyChineseTextRead);

        remoteReference.sendEvent(event);
    }

    public void startSelfStudyEnglishWalkman(Long userId) {
        if (userId == null)
            return;
        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.StartSelfStudyEnglishWalkman);

        remoteReference.sendEvent(event);
    }

    /**
     * 学生班级签到
     */
    public void studentAttendance(Long userId, Long clazzId, Long schoolId, Integer studentCountInClazz) {
        if (null == userId || 0 == userId || null == clazzId || 0 == clazzId || null == schoolId || 0 == schoolId || null == studentCountInClazz || 0 == studentCountInClazz) {
            return;
        }

        ActionEvent event = new ActionEvent();
        event.setUserId(userId);
        event.setType(ActionEventType.ClazzAttendance);
        event.setAttributes(new HashMap<>());
        event.getAttributes().put("clazzId", clazzId);
        event.getAttributes().put("schoolId", schoolId);
        event.getAttributes().put("totalCount", studentCountInClazz);

        remoteReference.sendEvent(event);
    }

    // 生日祝福点赞
    public void likeBirthday(Long clazzId, Long userId, Long likedUserId, Map<String, Object> extInfo) {
        sendLikeEvent(clazzId, userId, likedUserId, UserLikeType.BIRTHDAY_BLESS_HEADLINE, extInfo, SafeConverter.toString(likedUserId));
    }

    // 班级头条记录被点赞
    public void likeHeadline(Long clazzId, Long userId, Long likedUserId, Map<String, Object> extInfo, Long clazzJournalId) {
        if (anyEmpty(clazzJournalId)) {
            return;
        }
        sendLikeEvent(clazzId, userId, likedUserId, UserLikeType.CLAZZ_JOURNAL, extInfo, SafeConverter.toString(clazzJournalId));
    }

    // 排行榜点赞
    public void likeRank(Long clazzId, Long userId, Long likedUserId, UserLikeType type, String recordId, Map<String, Object> extInfo) {
        sendLikeEvent(clazzId, userId, likedUserId, type, extInfo, recordId);
    }

    private void sendLikeEvent(Long clazzId, Long userId, Long likedUserId, UserLikeType type, Map<String, Object> extInfo, String recordId) {
        if (anyEmpty(clazzId, userId, likedUserId) || null == type || StringUtils.isBlank(recordId)) {
            return;
        }

        ActionEvent event = new ActionEvent();
        event.setType(ActionEventType.Like);
        event.setUserId(userId);    //点赞人ID

        event.getAttributes().put("clazzId", clazzId);
        event.getAttributes().put("likedId", likedUserId); // 被赞人ID
        event.getAttributes().put("type", type);           // 点赞类型

        // 记录识别ID
        event.getAttributes().put("recordId", recordId);

        if (MapUtils.isNotEmpty(extInfo)) {
            event.getAttributes().putAll(extInfo);
        }

        remoteReference.sendEvent(event);
    }

    private boolean anyEmpty(Long... values) {
        if (values.length == 0) return false;
        for (Long value : values) {
            if (value == null || value == 0L) {
                return true;
            }
        }
        return false;
    }

}
