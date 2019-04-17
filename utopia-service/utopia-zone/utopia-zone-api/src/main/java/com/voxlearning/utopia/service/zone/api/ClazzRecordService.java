package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordHwMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordLikeMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordSoundShareMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/2/27
 * Time: 11:47
 * 班级记录service
 */

@ServiceVersion(version = "20170502")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClazzRecordService extends IPingable {

    Map<NewHomework.Location, ClazzRecordHwMapper> loadUserHomeworkRecords(Long userId, Collection<NewHomework.Location> homeworkIds);

    /**
     * 获取当前学期的作业(已检查的)列表 (分group)
     */
    List<NewHomework.Location> loadHomeworkList(Long groupId);


    /**
     * 获取用户本季度完成的作业
     *
     * @param locations 作业信息
     * @param userId    学生用户id
     */
    Map<String, NewHomeworkResult> queryHomeworkRecords(Collection<NewHomework.Location> locations, Long userId);


    /**
     * 记录语音分享
     */
    void recordSoundShare(RecordSoundShareMapper mapper);

    /**
     * 获取班级语音分享
     */
    List<RecordSoundShareMapper> queryRecordSoundShare(Long clazzId, Long userId);

    /**
     * 直接从缓存里面加载， 只允许CRM使用
     */
    List<RecordSoundShareMapper> $directlyLoadFromCache(Long clazzId, Long userId);

    /**
     * 清除某个学生的某个录音分享缓存， 只允许CRM使用
     */
    void $clearNaughtyRecord(Long clazzId, Long userId, String uri);

    /**
     * 点赞记录
     */
    void like(RecordLikeMapper mapper);

    /**
     * 获取班级点赞记录
     */
    List<RecordLikeMapper> queryRecordLike(RecordLikeMapper mapper);

    /**
     * 是否已鼓励过
     */
    boolean recordLiked(RecordLikeMapper mapper);

    List<NewHomework.Location> loadHomeworkList(Collection<Long> groupIds);

    /**
     * 专注 top3
     *
     * @param homeworkId 作业id
     */
    List<ClazzRecordCardMapper> queryTop3FocusMapper(String homeworkId);

    /**
     * 学霸 top3
     *
     * @param homeworkId 作业id
     */
    List<ClazzRecordCardMapper> queryTop3StudyMasterMapper(String homeworkId);

    /**
     * 获取当此作业 平均分
     *
     * @param homeworkId 作业id
     */
    Double getAvgScore(String homeworkId);


    /**
     * 满分top3
     *
     * @param groupIds 分组ids
     * @param clazzId  班级id
     */
    List<ClazzRecordCardMapper> queryFullMarksTop3(List<Long> groupIds, Long clazzId);

    /**
     * 友谊top3
     *
     * @param clazzId    班级id
     * @param classmates 同学id
     */
    List<ClazzRecordCardMapper> queryFriendShipTop3(Long clazzId, List<Long> classmates);

    /**
     * 满分周最佳
     *
     * @param groupIds 分组ids
     * @param clazzId  班级id
     */
    ClazzRecordCardMapper queryWeekTopFullMarks(List<Long> groupIds, Long clazzId);

    /**
     * 友谊周最佳
     *
     * @param clazzId    班级id
     * @param classmates 同学ids
     */
    ClazzRecordCardMapper queryWeekTopFriendShip(Long clazzId, List<Long> classmates);

}
