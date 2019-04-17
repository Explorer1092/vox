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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.content.api.constant.NewBookType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.mapper.PictureBookQuery;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author guoqiang.li
 * @since 2016/1/19
 */
@ServiceVersion(version = "20190314")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface NewHomeworkContentService extends IPingable {

    List<ExClazz> findTeacherClazzsCanBeAssignedHomework(Teacher teacher);

    MapMessage loadTeacherClazzList(Teacher teacher, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz);

    MapMessage loadTeachersClazzList(Collection<Long> teacherIds, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz);

    MapMessage loadNewTeacherClazzList(Teacher teacher, Set<NewHomeworkType> newHomeworkTypes, Set<HomeworkTag> HomeworkTags, Boolean filterEmptyClazz);

    MapMessage loadClazzBook(Teacher teacher, Map<Long, Long> clazzGroupMap, Boolean fromVacation);

    MapMessage load17XueBook(Teacher teacher, Collection<Long> groupIds, NewBookType newBookType);

    List<NewBookProfile> loadBooks(Teacher teacher, Integer clazzLevel, Integer term);

    MapMessage loadBookUnitList(String bookId);

    MapMessage getHomeworkType(TeacherDetail teacher, List<String> sectionIds, String unitId, String bookId, String sys, String appVersion, String cdnUrl);

    MapMessage getHomeworkContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds, String unitId, String bookId,
                                  ObjectiveConfigType objectiveConfigType, Integer currentPageNum);

    MapMessage getMentalQuestion(String knowledgePoint, Integer contentTypeId, List<String> chosenQuestionIds, Integer newQuestionCount);

    MapMessage loadOcrMentalWorkBookList(TeacherDetail teacherDetail, String bookId);

    MapMessage searchReading(PictureBookQuery pictureBookQuery, Pageable pageable, String bookId, String unitId, Teacher teacher);

    MapMessage previewContent(Teacher teacher, String bookId, Map<String, List> contentMap);

    MapMessage loadTermReviewContentTypeList(Subject subject, String bookId, List<Long> groupIds, Boolean fromPC, String cdnUrl, Teacher teacher, String sys, String appVersion);

    MapMessage loadTermReviewContent(Teacher teacher, List<Long> groupIds, String bookId, TermReviewContentType termReviewContentType);

    MapMessage previewBasicReviewContent(String bookId, List<String> contentTypes, String cdnUrl);

    MapMessage loadUnitProgress(Teacher teacher, Map<Long, Long> groupIdClazzIdMap, String unitId, String bookId);

    MapMessage loadIntelligenceQuestion(TeacherDetail teacher, Collection<Long> groupIds, List<String> sectionIds, String bookId, String unitId,
                                        String algoType, Integer difficulty, Integer questionCount, Collection<String> kpIds, Collection<Integer> contentTypeIds,
                                        String objectiveConfigId, String type);

    MapMessage loadObjectiveList(TeacherDetail teacher, List<String> sectionIds, String unitId, String bookId, HomeworkSourceType homeworkSourceType, String appVersion);

    MapMessage loadObjectiveContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds, String unitId, String bookId, ObjectiveConfigType objectiveConfigType,
                                    String objectiveConfigId, Integer currentPageNum, HomeworkSourceType homeworkSourceType, String sys, String appVersion);

    MapMessage loadExpandIndexData(TeacherDetail teacherDetail);

    MapMessage loadExpandObjectiveList(TeacherDetail teacherDetail, String bookId);

    MapMessage loadExpandObjectiveContent(TeacherDetail teacherDetail, String bookId, ObjectiveConfigType objectiveConfigType, String objectiveConfigId);

    MapMessage loadExpandVideoDetail(String videoId);

    List<Map<String, Object>> loadUniSoundWordScoreLevels(StudentDetail studentDetail);

    String loadImageQualityStr(StudentDetail studentDetail);

    List<Map<String, Object>> loadUniSoundSentenceScoreLevels(StudentDetail studentDetail);

    List<Map<String, Object>> loadUniSoundWordTeachSentenceScoreLevels(StudentDetail studentDetail);

    List<Map<String, Object>> loadVoxSentenceScoreLevels(StudentDetail studentDetail);

    List<Map<String, Object>> loadVoxSongScoreLevels(StudentDetail studentDetail);

    List<Map<String, Object>> loadVoxOralCommunicationSingleLevel(StudentDetail studentDetail);

    List<Map<String, Object>> loadVoxOralCommunicationTotalLevel(StudentDetail studentDetail);

    MapMessage loadVoiceEngineConfig(StudentDetail studentDetail, ObjectiveConfigType objectiveConfigType);

    MapMessage loadPracticeDetail(Teacher teacher, String homeworkId);

    MapMessage loadSameLevelClazzList(Teacher teacher, String homeworkId);

    MapMessage loadNaturalSpellingContent(TeacherDetail teacherDetail, String bookId, String unitId, String objectiveConfigId, Integer level);

    MapMessage loadDubbingAlbumList(Integer clazzLevel);

    MapMessage loadDubbingRecommendSearchWords();

    MapMessage searchDubbing(TeacherDetail teacherDetail, Integer clazzLevel, String searchWord, List<String> channelIds, List<String> albumIds, List<String> themeIds, String bookId, String unitId, Pageable pageable, ObjectiveConfigType objectiveConfigType);

    MapMessage loadDubbingDetail(TeacherDetail teacherDetail, String bookId, String unitId, String dubbingId, ObjectiveConfigType objectiveConfigType);

    MapMessage loadPictureBookPlusTopicList();

    MapMessage loadPictureBookPlusSeriesList();

    MapMessage loadPictureBookPlusRecommendSearchWords();

    MapMessage searchPictureBookPlus(TeacherDetail teacherDetail, String clazzLevel, List<String> topicIds, List<String> seriesIds,
                                     String searchWord, String bookId, String unitId, Pageable pageable, String sys, String appVersion);

    MapMessage loadPictureBookPlusHistory(TeacherDetail teacherDetail, String bookId, String unitId, Pageable pageable, String sys, String appVersion);

    MapMessage loadIndexRecommendContent(TeacherDetail teacher, String appVersion);

    MapMessage loadTeacherClazzListForRecommend(TeacherDetail teacher, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz);

    MapMessage loadNewIndexRecommendContent(TeacherDetail teacherDetail, String sys, String appVersion, String cdnUrl);

    MapMessage loadRecommendJumpParams(TeacherDetail teacherDetail, ObjectiveConfigType objectiveConfigType, String sys, String appVersion, String id);

    MapMessage loadBasicMathContent(String bookId, Integer stageId);

    MapMessage loadBasicChineseContent(Teacher teacher, String bookId, Integer stageId);

    MapMessage loadObjectiveWaterfallContent(TeacherDetail teacher, String objectiveId, Set<Long> groupIds, List<String> sectionIds, String bookId, String unitId, String sys, String appVersion);

    MapMessage loadBasicAppWaterfallContent(TeacherDetail teacher, String objectiveConfigId, String bookId, String unitId, String categoryGroup);

    MapMessage loadDubbingCollectionRecord(TeacherDetail teacherDetail, String bookId, String unitId, Pageable pageable, String sys, String appVersion);

    MapMessage loadOralCommunicationSearchWords();

    MapMessage searchOralCommunication(TeacherDetail teacherDetail, String clazzLevel, String type, String searchWord, String bookId, String uniId, Pageable pageable);

    MapMessage loadOralCommunicationDetail(TeacherDetail teacherDetail, String bookId, String unitId, String oralCommunicationId);

    MapMessage loadTeachingResourceDefaultBook(TeacherDetail teacherDetail);

    MapMessage changeTeachingResourceDefaultBook(TeacherDetail teacherDetail, String bookId);

    MapMessage loadTeachingResourceTypeList(TeacherDetail teacherDetail, String bookId, String unitId);

    MapMessage loadTeachingResourceContent(TeacherDetail teacherDetail, String bookId, String unitId, String sectionId, String type, Map params);

    MapMessage loadNaturalSpellingLevelsContent(String bookId);

    List<Integer> loadHomeworkReportShareChannel(TeacherDetail teacherDetail);

    MapMessage loadOcrMentalBookList(Teacher teacher);

    MapMessage addOcrMentalBook(Teacher teacher, String bookName);

    MapMessage deleteOcrMentalBook(Teacher teacher, String bookId);

    MapMessage loadOcrDictationUnitList(Teacher teacher, List<Long> groupIds);

    MapMessage loadOcrDictationContent(Teacher teacher, String bookId, String unitId);
}