package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 * @author jiangpeng
 * @since 2017-05-22 下午5:49
 **/
@ServiceVersion(version = "2017.05.22")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPMySelfStudyService {


    /**
     * 预约了直播客
     * 用upstairsEntryShow
     * @param parentId
     */
    @Deprecated
    Boolean bookLiveCast(Long parentId);


    Boolean liveCastText(Long parentId, String text);


    /**
     * 楼上的入口显示与否
     * 直播客,小课堂
     */
    MapMessage upstairsEntryShow(Long parentId, String entryKey, Boolean show);


    MapMessage upstairsEntryGlobalMsg(String entryKey, String msg);


    /**
     * 给 php 提供的，更新增值应用的消息通知
     * @param entryKey
     * @param userId
     * @param notifyContent
     * @param notifyUniqueId
     * @return
     */
    MapMessage updateStudyAppUserNotify(String entryKey, Long userId, String notifyContent, String notifyUniqueId);


    /**
     * 给直播提供的，根据学生 id推首页竞品消息

     * @return
     */
    MapMessage pushJztIndexRemind(String data);

    MapMessage pushJztIndexRefinedLessons(String data);

}
