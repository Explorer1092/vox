/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.LightInteractionCourseResp;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.MicroVideoResp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/6
 */
@ServiceVersion(version = "20190308")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface NewHomeworkService extends IPingable {

    MapMessage assignHomework(Teacher teacher, HomeworkSource context, HomeworkSourceType homeworkSourceType, NewHomeworkType newHomeworkType, HomeworkTag homeworkTag);

    MapMessage adjustHomework(Long teacherId, String id, Date end);

    MapMessage deleteHomework(Long teacherId, String id);

    MapMessage checkHomework(Teacher teacher, String homeworkId, HomeworkSourceType homeworkSourceType);

    MapMessage batchCheckHomework(Teacher teacher, String homeworkIds, HomeworkSourceType homeworkSourceType);

    MapMessage processorHomeworkResult(HomeworkResultContext homeworkResultContext);

    /**
     * 批量批改
     *
     * @param homeworkId 作业id
     * @param teacherId  老师id
     */
    void batchSaveHomeworkCorrect(String homeworkId, Long teacherId);

    /**
     * 批量评语
     * 注：此处实现调用了原HomeworkComment中的保存方法
     * 其实mysql方法中的数据目前在新体系中是用不到的。
     *
     * @param teacher    添加评语的老师
     * @param homeworkId 作业id
     * @param userIds    被评语的学生id
     * @param comment    评语信息
     * @return MapMessage
     */
    MapMessage batchSaveNewHomeworkComment(Teacher teacher, String homeworkId, Set<Long> userIds, String comment, String audioComment);

    //接口不在需要开始和结束时间参数
    Page<HomeworkHistoryMapper> loadStudentHomeworkHistory(Long clazzGroupId, Subject subject, Long userId, Pageable pageable);

    Page<HomeworkHistoryMapper> loadStudentHomeworkHistoryWithTimeLimit(Long clazzGroupId, Subject subject, Date startDate, Date endDate, Long userId, Pageable pageable);


    /**
     * 作业历史-某一个作业的作业历史信息
     * xuesong.zhang
     *
     * @param homeworkId 作业id
     * @param userId     用户id
     * @return HomeworkHistoryDetail
     */
    HomeworkHistoryDetail loadStudentHomeworkHistoryDetail(String homeworkId, Long userId);


    MapMessage incFinishHomeworkCount(Long teacherId, Long clazzId, Long studentId);

    void updatePossibleCheatingHomeworkIntegral(String id);

    void persistPossibleCheatingTeacher(PossibleCheatingTeacher pct);

    void updateLastCheatDateAndStatus(String id, CheatingTeacherStatus status);

    boolean isCheatingTeacher(Long teacherId);

    void insertPossibleCheatingHomework(PossibleCheatingHomework homework);

    void disabledPossibleCheatingTeacherById(String id);

    void insertPossibleCheatingTeacher(PossibleCheatingTeacher teacher);

    void updatePossibleCheatingTeacherStatus(String id, CheatingTeacherStatus status);

    void washTeacher(String id);

    MapMessage batchRewardStudentIntegral(Long teacherId, Map<String, Object> jsonMap);

    MapMessage submitVoiceRecommend(String homeworkId, List<VoiceRecommend.RecommendVoice> recommendVoiceList, String recommendComment);

    MapMessage submitReadReciteVoiceRecommend(String homeworkId, List<VoiceRecommend.ReadReciteVoice> recommendVoiceList, String recommendComment);

    MapMessage submitDubbingVoiceRecommend(String homeworkId, List<VoiceRecommend.DubbingWithScore> dubbingVoiceList, String recommendComment);

    MapMessage submitImageTextRecommend(String homeworkId, List<BaseVoiceRecommend.ImageText> imageTextList);

    MapMessage addVoiceRecommendRequestParent(String homeworkId, Long parentId, String parentName);

    void insertNewHomeworkBooks(Collection<NewHomeworkBook> entities);

    /**
     * 家长作业报告改版2.0（英语一期）
     * 口语句子染红
     */
    MapMessage processSyllable(NewHomeworkSyllable newHomeworkSyllable, String day);

    /**
     * 17奖学金抽奖,获取老师总的钥匙数量
     */
    MapMessage processScholarship(Long teacherId, Integer type, Integer totalRateKey);

    /**
     * 17奖学金抽奖,首次查看学情评估、布置作业获取更多积分 赠送一把钥匙
     */
    MapMessage processScholarshipFirstClick(Long teacherId, String scholarKeyType);

    /**
     * 17奖学金抽奖,老师钥匙记录
     */
    MapMessage getScholarshipKeyRecord(Long teacherId);

    /**
     * 领取作业任务奖励
     */
    MapMessage rewardHomeworkTaskIntegral(Teacher teacher, String recordId);

    /**
     * 优秀录音推荐，首次分享到微信/QQ 增加2个园丁豆
     */
    MapMessage addIntegral(Long teacherId, String homeworkId);

    /**
     * 查找作业内容--for copy homework
     * @param newHomework
     * @param groupIds
     * @param practiceContentMap
     * @return
     */
    Map<ObjectiveConfigType, Map<String, Object>> findAppsFromHomework(NewHomework newHomework, Collection<Long> groupIds, Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContentMap);
    /**
     * 复制作业
     */
    MapMessage copyHomework(Teacher teacher, String homeworkId, Collection<Long> groupIds, String startTime, String endTime,HomeworkSourceType homeworkSourceType);

    /**
     * 2018年春季，布置期末复习基础必过
     */
    MapMessage assignBasicReviewHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType);

    /**
     * 删除期末复习基础必过
     */
    MapMessage deleteBasicReviewHomework(Teacher teacher, String packageId);


    /**
     * 家长端催促作业
     */
    boolean updateNewHomeworkResultUrge(NewHomework.Location location, Long studentId, Long parentId, int beanNum);

    /**
     * 趣配音上传音频成功后保存数据并发送Queue消息，志军接收到消息后合并视频并上传阿里云后发回Queue，
     */
    MapMessage uploaderDubbing(String id, String audioUrl, String videoUrl, String path);

    /**
     * 上传新绘本配音
     */
    MapMessage uploadPictureBookPlusDubbing(String homeworkId, String pictureBookId, Long userId, List<PictureBookPlusDubbing.Content> contents, String screenMode);

    MapMessage uploadLiveCastPictureBookPlusDubbing(String homeworkId, String pictureBookId, Long userId, List<PictureBookPlusDubbing.Content> contents, String screenMode);

    void saveAccessDeniedRecord(AccessDeniedRecord accessDeniedRecord);

    /**
     * 根据轻交互课程ID查询轻交互课程
     * @param courseIds 轻交互课程IDs
     * @return
     */
    List<LightInteractionCourseResp> fetchLightInteractionCourse(Collection<String> courseIds);

    /**
     * 根据轻交互课程ID查询轻交互课程V2
     * @param courseIds 轻交互课程IDs
     * @return
     */
    List<Map<String, Object>> fetchLightInteractionCourseV2(Collection<String> courseIds);

    /**
     * 根据视频课程ID查询视频课程
     * @param videoIds 视频课程ID
     * @return
     */
    List<MicroVideoResp> fetchVideoCourse(Collection<String> videoIds);

    /**
     * 收藏趣味配音
     */
    MapMessage collectDubbing(TeacherDetail teacherDetail, String dubbingId);

    MapMessage loadNationalDayHomeworkAssignStatus(Teacher teacher);

    MapMessage autoAssignNationalDayHomework(Teacher teacher);

    MapMessage deleteNationalDayHomework(Teacher teacher);

    MapMessage loadNationalDayClazzList(Teacher teacher);

    MapMessage loadNationalDaySummaryReport(Teacher teacher, String packageId);

    /**
     * 推荐学生巩固
     * @param id
     * @return
     */
    MapMessage updateHomeworkRemindCorrection(String id);

    /**
     * 根据processId获取纸质口算被替换的原图片
     * @param processId
     * @return
     */
    MapMessage getOriginImageUrlByProcessId(String processId);

    MapMessage updateReportShareParts(String homeworkId, String shareParts);

    /**
     * 改判口算拍照
     * @param userId 学生id
     * @param homeworkId 作业id
     * @param url 对应的图片
     * @param boxJson 坐标数据
     * @return
     */
    MapMessage ocrMentalArithmeticCorrect(Long userId, String homeworkId, String url, String boxJson);

    /**
     * 保存上传资源数据
     * @param url
     * @return
     */
    MapMessage uploaderResourceLibrary(UploaderResourceLibrary url);

    MapMessage imageTextRhymeView(String stoneDataId, WordTeachModuleType wordTeachModuleType);

    MapMessage remindAssignOralCommunicationHomework(Long studentId);

    List<Long> loadRemindAssignTeacherIds();

    void sendRemindAssignMessage(List<Long> teacherIds);
}
