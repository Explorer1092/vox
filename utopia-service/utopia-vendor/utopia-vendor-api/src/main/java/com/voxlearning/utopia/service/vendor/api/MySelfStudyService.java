package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRefinedLessons;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRemind;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyData;
import com.voxlearning.utopia.service.vendor.api.entity.StudyAppData;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.api.constant.MySelfStudyActionType;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 我的自学服务
 *
 * @author jiangpeng
 * @since 2016-10-20 上午11:48
 **/
@ServiceVersion(version = "20180703")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface MySelfStudyService {

    void handleEvent(MySelfStudyActionEvent event);


    @CacheMethod(
            writeCache = false,
            type = MySelfStudyData.class
    )
    List<MySelfStudyData> loadMySelfStudyDateBySId(@CacheParameter(value = "SID") Long studentId);

    @Async
    AlpsFuture<StudyAppData> loadStudyAppData(Long userId, SelfStudyType selfStudyType);

    @Async
    AlpsFuture<LiveCastIndexRemind.RemindContent> loadStudentLiveCastRemind(StudentDetail studentDetail);

    @Async
    AlpsFuture<LiveCastIndexRemind> loadStudentLiveCastRemindV2(StudentDetail studentDetail);


    @Async
    AlpsFuture<List<LiveCastIndexRefinedLessons.LessonInfo>> loadStudentLiveCastRefinedLessons(StudentDetail studentDetail);

    @NoResponseWait
    void sendMyselfstudyMessage(Message message);


    default void updateSelfStudyProgress(Long studentId, SelfStudyType selfStudyType, String progress) {
        MySelfStudyActionEvent event = new MySelfStudyActionEvent(studentId, selfStudyType, MySelfStudyActionType.UPDATE_PROGRESS);
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("studyProgress", progress == null ? "" : progress);
        map.put("lastUserDate", new Date().getTime());
        event.setAttributes(map);
        sendMyselfstudyMessage(event.toMessage());
    }

    default void updateIcon(Long userId, SelfStudyType selfStudyType, String iconUrl){
        if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN || userId == null || userId == 0)
            return;
        MySelfStudyActionEvent event = new MySelfStudyActionEvent(userId, selfStudyType, MySelfStudyActionType.UPDATE_ICON);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("iconUrl", iconUrl);
        event.setAttributes(map);
        sendMyselfstudyMessage(event.toMessage());
    }

    /**
     * 显示隐藏入口
     * 直播课和小课堂用
     * @param parentId
     * @param selfStudyType
     * @param show
     */
    default void updateShow(Long parentId, SelfStudyType selfStudyType, Boolean show){
        if (parentId == null || selfStudyType == null || show == null)
            return;
        MySelfStudyActionEvent event = new MySelfStudyActionEvent(parentId, selfStudyType, MySelfStudyActionType.UPDATE_SHOW);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("show", show);
        event.setAttributes(map);
        sendMyselfstudyMessage(event.toMessage());
    }

    default void updateUserNotify(Long userId, SelfStudyType selfStudyType, String msg, String uniqueId){
        if (userId == null || userId == 0L || selfStudyType == null || StringUtils.isBlank(msg)
                || StringUtils.isBlank(uniqueId))
            return;
        MySelfStudyActionEvent event = new MySelfStudyActionEvent(userId, selfStudyType, MySelfStudyActionType.UPDATE_USER_NOTIFY);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("notifyContent", msg);
        map.put("notifyUniqueId", uniqueId);
        event.setAttributes(map);
        sendMyselfstudyMessage(event.toMessage());
    }

    default void globalMsg(SelfStudyType selfStudyType, String msg) {
        if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
            return;
        MySelfStudyActionEvent event = new MySelfStudyActionEvent(-1L, selfStudyType, MySelfStudyActionType.UPDATE_GLOBAL_MSG);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("msg", msg);
        event.setAttributes(map);
        sendMyselfstudyMessage(event.toMessage());
    }
}
