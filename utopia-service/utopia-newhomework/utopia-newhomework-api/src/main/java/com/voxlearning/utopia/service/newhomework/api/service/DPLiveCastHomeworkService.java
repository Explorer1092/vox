package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@ServiceVersion(version = "20170629")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface DPLiveCastHomeworkService extends IPingable {
    // 删除作业接口
    MapMessage deleteHomework(Long teacherId, String hid);

    //批量评语接口
    MapMessage noteComment(Long teacherId, String comment, Collection<Long> useIds, String hid);

    /**
     * correctInfoMap 的格式如下
     * {
     * 　　"qid1": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语",
     *        "percentage":""//百分比信息,
     *        "voice":"" //录音
     * 　　},
     * 　　"qid2": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语",
     *        "percentage":""//百分比信息,
     *        "voice":"" //录音
     * 　　},
     * 　　"qid3": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语",
     *        "percentage":""//百分比信息,
     *        "voice":"" //录音
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

}
