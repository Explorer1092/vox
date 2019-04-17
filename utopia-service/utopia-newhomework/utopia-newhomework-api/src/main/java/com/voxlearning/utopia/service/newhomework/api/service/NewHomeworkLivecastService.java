package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.constants.ThirdPartyGroupType;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2016/9/12
 */
@ServiceVersion(version = "20161212")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface NewHomeworkLivecastService extends IPingable {

    /**
     * 布置作业新版
     *
     * @param teacherId           老师id
     * @param subject             学科
     * @param homeworkSource      作业map
     * @param newHomeworkType     作业类型  USTalk / OlympicMath
     * @param thirdPartyGroupType 第三方用户分组类型 USTALK_GROUP / OlympicMath_GROUP
     * @return MapMessage
     */
    MapMessage assignHomework4LiveCast(Long teacherId, Subject subject, HomeworkSource homeworkSource, String newHomeworkType, ThirdPartyGroupType thirdPartyGroupType);

    /**
     * 删除Livecast作业
     *
     * @param id 作业id
     * @return MapMessage
     */
    @Deprecated
    MapMessage deleteHomework(String id);

    /**
     * 删除作业
     *
     * @param id              作业id
     * @param newHomeworkType 作业类型
     * @return MapMessage
     */
    MapMessage deleteHomework(String id, String newHomeworkType);

    /**
     * correctInfoMap 的格式如下
     * {
     * 　　"qid1": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　},
     * 　　"qid2": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　},
     * 　　"qid3": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　}
     * }
     *
     * @param homeworkId     作业id
     * @param studentId      学生id
     * @param correctInfoMap 批改信息Map
     * @return MapMessage
     */
    MapMessage correctQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap);



    MapMessage newCorrectQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap, ObjectiveConfigType type);


    /**
     * 做题
     *
     * @param context 做题内容
     * @return MapMessage
     */
    MapMessage processorHomeworkResult(LiveCastHomeworkResultContext context);


    // 以下方法为了迁移数据用
    @NoResponseWait
    void insertsLiveCastHomeworkWithoutCache(Collection<LiveCastHomework> entities);

    void insertsLiveCastHomeworkBookWithoutCache(Collection<LiveCastHomeworkBook> entities);

    void insertsLiveCastHomeworkResultWithoutCache(Collection<LiveCastHomeworkResult> entities);

    void insertsLiveCastHomeworkProcessResultWithoutCache(Collection<LiveCastHomeworkProcessResult> entities);

}
