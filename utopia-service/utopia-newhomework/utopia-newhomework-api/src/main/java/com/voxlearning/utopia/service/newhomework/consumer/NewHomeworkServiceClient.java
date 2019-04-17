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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
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
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.*;


/**
 * @author tanguohong
 * @since 2016/1/6
 */
public class NewHomeworkServiceClient implements NewHomeworkService {

    @ImportService(interfaceClass = NewHomeworkService.class)
    private NewHomeworkService remoteReference;

    public MapMessage assignHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType, NewHomeworkType newHomeworkType, HomeworkTag homeworkTag) {
        return remoteReference.assignHomework(teacher, homeworkSource, homeworkSourceType, newHomeworkType, homeworkTag);
    }

    public MapMessage adjustHomework(Long teacherId, String id, Date end) {
        return remoteReference.adjustHomework(teacherId, id, end);
    }

    public MapMessage deleteHomework(Long teacherId, String id) {
        return remoteReference.deleteHomework(teacherId, id);
    }

    public MapMessage checkHomework(Teacher teacher, String homeworkId, HomeworkSourceType homeworkSourceType) {
        return remoteReference.checkHomework(teacher, homeworkId, homeworkSourceType);
    }

    public MapMessage batchCheckHomework(Teacher teacher, String homeworkIds, HomeworkSourceType homeworkSourceType) {
        return remoteReference.batchCheckHomework(teacher, homeworkIds, homeworkSourceType);
    }

    public MapMessage processorHomeworkResult(HomeworkResultContext homeworkResultContext) {
        return remoteReference.processorHomeworkResult(homeworkResultContext);
    }

    public void batchSaveHomeworkCorrect(String homeworkId, Long teacherId) {
        if (StringUtils.isBlank(homeworkId) || teacherId == null) {
            return;
        }
        remoteReference.batchSaveHomeworkCorrect(homeworkId, teacherId);
    }

    @Override
    public MapMessage batchSaveNewHomeworkComment(Teacher teacher, String homeworkId, Set<Long> userIds, String comment, String audioComment) {
        return remoteReference.batchSaveNewHomeworkComment(teacher, homeworkId, userIds, comment, audioComment);
    }

    public Page<HomeworkHistoryMapper> loadStudentHomeworkHistory(Long clazzGroupId, Subject subject, Long userId, Pageable pageable) {
        if (clazzGroupId == null || subject == null || userId == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return remoteReference.loadStudentHomeworkHistory(clazzGroupId, subject, userId, pageable);
    }


    //pc学生历史报告接口
    public Page<HomeworkHistoryMapper> loadStudentHomeworkHistoryWithTimeLimit(Long clazzGroupId, Subject subject, Date startDate, Date endDate, Long userId, Pageable pageable) {
        if (clazzGroupId == null || subject == null || userId == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return remoteReference.loadStudentHomeworkHistoryWithTimeLimit(clazzGroupId, subject, startDate, endDate, userId, pageable);
    }

    public HomeworkHistoryDetail loadStudentHomeworkHistoryDetail(String homeworkId, Long userId) {
        if (StringUtils.isBlank(homeworkId) || userId == null) {
            return null;
        }
        return remoteReference.loadStudentHomeworkHistoryDetail(homeworkId, userId);
    }


    public MapMessage incFinishHomeworkCount(Long teacherId, Long clazzId, Long studentId) {
        return remoteReference.incFinishHomeworkCount(teacherId, clazzId, studentId);
    }

    public void updatePossibleCheatingHomeworkIntegral(String id) {
        remoteReference.updatePossibleCheatingHomeworkIntegral(id);
    }

    public void persistPossibleCheatingTeacher(PossibleCheatingTeacher pct) {
        remoteReference.persistPossibleCheatingTeacher(pct);
    }

    public void updateLastCheatDateAndStatus(String id, CheatingTeacherStatus status) {
        remoteReference.updateLastCheatDateAndStatus(id, status);
    }

    public boolean isCheatingTeacher(Long teacherId) {
        return remoteReference.isCheatingTeacher(teacherId);
    }

    public void insertPossibleCheatingHomework(PossibleCheatingHomework homework) {
        remoteReference.insertPossibleCheatingHomework(homework);
    }

    public void disabledPossibleCheatingTeacherById(String id) {
        remoteReference.disabledPossibleCheatingTeacherById(id);
    }

    public void insertPossibleCheatingTeacher(PossibleCheatingTeacher teacher) {
        remoteReference.insertPossibleCheatingTeacher(teacher);
    }

    public void updatePossibleCheatingTeacherStatus(String id, CheatingTeacherStatus status) {
        remoteReference.updatePossibleCheatingTeacherStatus(id, status);
    }

    public void washTeacher(String id) {
        remoteReference.washTeacher(id);
    }

    public MapMessage batchRewardStudentIntegral(Long teacherId, Map<String, Object> jsonMap) {
        return remoteReference.batchRewardStudentIntegral(teacherId, jsonMap);
    }

    @Override
    public MapMessage submitVoiceRecommend(String homeworkId, List<VoiceRecommend.RecommendVoice> recommendVoiceList, String recommendComment) {
        return remoteReference.submitVoiceRecommend(homeworkId, recommendVoiceList, recommendComment);
    }

    @Override
    public MapMessage submitReadReciteVoiceRecommend(String homeworkId, List<VoiceRecommend.ReadReciteVoice> recommendVoiceList, String recommendComment) {
        return remoteReference.submitReadReciteVoiceRecommend(homeworkId, recommendVoiceList, recommendComment);
    }

    @Override
    public MapMessage submitDubbingVoiceRecommend(String homeworkId, List<VoiceRecommend.DubbingWithScore> dubbingVoiceList, String recommendComment) {
        return remoteReference.submitDubbingVoiceRecommend(homeworkId,dubbingVoiceList,recommendComment);
    }

    @Override
    public MapMessage submitImageTextRecommend(String homeworkId, List<BaseVoiceRecommend.ImageText> imageTextList) {
        return remoteReference.submitImageTextRecommend(homeworkId, imageTextList);
    }

    @Override
    public MapMessage addVoiceRecommendRequestParent(String homeworkId, Long parentId, String parentName) {
        return remoteReference.addVoiceRecommendRequestParent(homeworkId, parentId, parentName);
    }

    @Override
    public MapMessage processSyllable(NewHomeworkSyllable newHomeworkSyllable, String day) {
        return remoteReference.processSyllable(newHomeworkSyllable, day);
    }

    @Override
    public MapMessage processScholarship(Long teacherId, Integer type, Integer totalRateKey) {
        return remoteReference.processScholarship(teacherId, type, totalRateKey);
    }

    @Override
    public MapMessage processScholarshipFirstClick(Long teacherId, String scholarKeyType) {
        return remoteReference.processScholarshipFirstClick(teacherId, scholarKeyType);
    }

    @Override
    public MapMessage getScholarshipKeyRecord(Long teacherId) {
        return remoteReference.getScholarshipKeyRecord(teacherId);
    }

    @Override
    public void insertNewHomeworkBooks(Collection<NewHomeworkBook> entities) {
        remoteReference.insertNewHomeworkBooks(entities);
    }

    @Override
    public MapMessage rewardHomeworkTaskIntegral(Teacher teacher, String recordId) {
        return remoteReference.rewardHomeworkTaskIntegral(teacher, recordId);
    }

    @Override
    public MapMessage addIntegral(Long teacherId, String homeworkId) {
        return remoteReference.addIntegral(teacherId, homeworkId);
    }

    @Override
    public Map<ObjectiveConfigType, Map<String, Object>> findAppsFromHomework(NewHomework newHomework, Collection<Long> groupIds, Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContentMap) {
        return remoteReference.findAppsFromHomework(newHomework,groupIds,practiceContentMap);
    }

    @Override
    public MapMessage copyHomework(Teacher teacher, String homeworkId, Collection<Long> groupIds, String startTime, String endTime,HomeworkSourceType homeworkSourceType) {
        return remoteReference.copyHomework(teacher, homeworkId, groupIds, startTime, endTime,homeworkSourceType);
    }

    @Override
    public MapMessage assignBasicReviewHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType) {
        return remoteReference.assignBasicReviewHomework(teacher, homeworkSource, homeworkSourceType);
    }

    @Override
    public MapMessage deleteBasicReviewHomework(Teacher teacher, String packageId) {
        return remoteReference.deleteBasicReviewHomework(teacher, packageId);
    }

    @Override
    public boolean updateNewHomeworkResultUrge(NewHomework.Location location, Long studentId, Long parentId, int beanNum) {
        return remoteReference.updateNewHomeworkResultUrge(location, studentId, parentId, beanNum);
    }

    public MapMessage uploaderDubbing(String id, String audioUrl, String videoUrl, String path) {
        return remoteReference.uploaderDubbing(id, audioUrl, videoUrl, path);
    }

    @Override
    public MapMessage uploadPictureBookPlusDubbing(String homeworkId, String pictureBookId, Long userId, List<PictureBookPlusDubbing.Content> contents, String screenMode) {
        return remoteReference.uploadPictureBookPlusDubbing(homeworkId, pictureBookId, userId, contents, screenMode);
    }

    @Override
    public MapMessage uploadLiveCastPictureBookPlusDubbing(String homeworkId, String pictureBookId, Long userId, List<PictureBookPlusDubbing.Content> contents, String screenMode) {
        return remoteReference.uploadLiveCastPictureBookPlusDubbing(homeworkId, pictureBookId, userId, contents, screenMode);
    }

    @Override
    public void saveAccessDeniedRecord(AccessDeniedRecord accessDeniedRecord) {
        remoteReference.saveAccessDeniedRecord(accessDeniedRecord);
    }

    @Override
    public List<LightInteractionCourseResp> fetchLightInteractionCourse(Collection<String> courseIds) {
        return remoteReference.fetchLightInteractionCourse(courseIds);
    }

    @Override
    public List<Map<String, Object>> fetchLightInteractionCourseV2(Collection<String> courseIds) {
        return remoteReference.fetchLightInteractionCourseV2(courseIds);
    }

    @Override
    public List<MicroVideoResp> fetchVideoCourse(Collection<String> videoIds) {
        return remoteReference.fetchVideoCourse(videoIds);
    }

    @Override
    public MapMessage collectDubbing(TeacherDetail teacherDetail, String dubbingId) {
        return remoteReference.collectDubbing(teacherDetail, dubbingId);
    }

    @Override
    public MapMessage loadNationalDayHomeworkAssignStatus(Teacher teacher) {
        return remoteReference.loadNationalDayHomeworkAssignStatus(teacher);
    }

    @Override
    public MapMessage autoAssignNationalDayHomework(Teacher teacher) {
        return remoteReference.autoAssignNationalDayHomework(teacher);
    }

    @Override
    public MapMessage deleteNationalDayHomework(Teacher teacher) {
        return remoteReference.deleteNationalDayHomework(teacher);
    }

    @Override
    public MapMessage loadNationalDayClazzList(Teacher teacher) {
        return remoteReference.loadNationalDayClazzList(teacher);
    }

    @Override
    public MapMessage loadNationalDaySummaryReport(Teacher teacher, String packageId) {
        return remoteReference.loadNationalDaySummaryReport(teacher, packageId);
    }

    @Override
    public MapMessage updateHomeworkRemindCorrection(String id) {
        return remoteReference.updateHomeworkRemindCorrection(id);
    }

    @Override
    public MapMessage getOriginImageUrlByProcessId(String processId) {
        return remoteReference.getOriginImageUrlByProcessId(processId);
    }

    @Override
    public MapMessage updateReportShareParts(String homeworkId, String shareParts) {
        return remoteReference.updateReportShareParts(homeworkId, shareParts);
    }

    @Override
    public MapMessage ocrMentalArithmeticCorrect(Long userId, String homeworkId, String url, String boxJson){
        return remoteReference.ocrMentalArithmeticCorrect(userId, homeworkId, url, boxJson);
    }

    @Override
    public MapMessage uploaderResourceLibrary(UploaderResourceLibrary url){
        return remoteReference.uploaderResourceLibrary(url);
    }

    @Override
    public MapMessage imageTextRhymeView(String stoneDataId, WordTeachModuleType wordTeachModuleType) {
        return remoteReference.imageTextRhymeView(stoneDataId, wordTeachModuleType);
    }

    @Override
    public MapMessage remindAssignOralCommunicationHomework(Long studentId) {
        return remoteReference.remindAssignOralCommunicationHomework(studentId);
    }

    @Override
    public List<Long> loadRemindAssignTeacherIds() {
        return remoteReference.loadRemindAssignTeacherIds();
    }

    @Override
    public void sendRemindAssignMessage(List<Long> teacherIds) {
        remoteReference.sendRemindAssignMessage(teacherIds);
    }
}
