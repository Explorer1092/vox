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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.Unisound8SentenceScoreLevel;
import com.voxlearning.utopia.api.constant.Unisound8WordScoreLevel;
import com.voxlearning.utopia.service.content.api.constant.NewBookType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkContentService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.mapper.PictureBookQuery;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.*;

/**
 * @author guoqiang.li
 * @since 2016/1/19
 */
public class NewHomeworkContentServiceClient implements NewHomeworkContentService {

    @ImportService(interfaceClass = NewHomeworkContentService.class)
    private NewHomeworkContentService remoteReference;

    public List<ExClazz> findTeacherClazzsCanBeAssignedHomework(Teacher teacher) {
        return remoteReference.findTeacherClazzsCanBeAssignedHomework(teacher);
    }

    @Override
    public MapMessage loadTeacherClazzList(Teacher teacher, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz) {
        return remoteReference.loadTeacherClazzList(teacher, newHomeworkTypes, filterEmptyClazz);
    }

    @Override
    public MapMessage loadNewTeacherClazzList(Teacher teacher, Set<NewHomeworkType> newHomeworkTypes, Set<HomeworkTag> HomeworkTags, Boolean filterEmptyClazz) {
        return remoteReference.loadNewTeacherClazzList(teacher, newHomeworkTypes, HomeworkTags, filterEmptyClazz);
    }

    @Override
    public MapMessage loadTeachersClazzList(Collection<Long> teacherIds, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz) {
        return remoteReference.loadTeachersClazzList(teacherIds, newHomeworkTypes, filterEmptyClazz);
    }

    public MapMessage loadClazzBook(Teacher teacher, Map<Long, Long> clazzGroupMap, Boolean fromVacation) {
        return remoteReference.loadClazzBook(teacher, clazzGroupMap, fromVacation);
    }

    public MapMessage load17XueBook(Teacher teacher, Collection<Long> groupIds, NewBookType newBookType) {
        return remoteReference.load17XueBook(teacher, groupIds, newBookType);
    }

    @Override
    public List<NewBookProfile> loadBooks(Teacher teacher, Integer clazzLevel, Integer term) {
        return remoteReference.loadBooks(teacher, clazzLevel, term);
    }

    @Override
    public MapMessage loadBookUnitList(String bookId) {
        return remoteReference.loadBookUnitList(bookId);
    }

    public MapMessage getHomeworkType(TeacherDetail teacher, List<String> sectionIds, String unitId, String bookId, String sys, String appVersion, String cdnUrl) {
        return remoteReference.getHomeworkType(teacher, sectionIds, unitId, bookId, sys, appVersion, cdnUrl);
    }

    public MapMessage getHomeworkContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds, String unitId, String bookId,
                                         ObjectiveConfigType objectiveConfigType, Integer currentPageNum) {
        return remoteReference.getHomeworkContent(teacher, groupIds, sectionIds, unitId, bookId, objectiveConfigType, currentPageNum);
    }

    public MapMessage getMentalQuestion(String knowledgePoint, Integer contentTypeId, List<String> chosenQuestionIds, Integer newQuestionCount) {
        return remoteReference.getMentalQuestion(knowledgePoint, contentTypeId, chosenQuestionIds, newQuestionCount);
    }

    @Override
    public MapMessage loadOcrMentalWorkBookList(TeacherDetail teacherDetail, String bookId) {
        return remoteReference.loadOcrMentalWorkBookList(teacherDetail, bookId);
    }

    public MapMessage searchReading(PictureBookQuery pictureBookQuery, Pageable pageable, String bookId, String unitId, Teacher teacher) {
        return remoteReference.searchReading(pictureBookQuery, pageable, bookId, unitId, teacher);
    }

    @Override
    public MapMessage loadDubbingDetail(TeacherDetail teacherDetail, String bookId, String unitId, String dubbingId, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.loadDubbingDetail(teacherDetail, bookId, unitId, dubbingId, objectiveConfigType);
    }

    public MapMessage previewContent(Teacher teacher, String bookId, Map<String, List> contentMap) {
        return remoteReference.previewContent(teacher, bookId, contentMap);
    }

    @Override
    public MapMessage loadTermReviewContentTypeList(Subject subject, String bookId, List<Long> groupIds, Boolean fromPC, String cdnUrl, Teacher teacher, String sys, String appVersion) {
        return remoteReference.loadTermReviewContentTypeList(subject, bookId, groupIds, fromPC, cdnUrl, teacher, sys, appVersion);
    }

    @Override
    public MapMessage loadTermReviewContent(Teacher teacher, List<Long> groupIds, String bookId, TermReviewContentType termReviewContentType) {
        return remoteReference.loadTermReviewContent(teacher, groupIds, bookId, termReviewContentType);
    }

    @Override
    public MapMessage previewBasicReviewContent(String bookId, List<String> contentTypes, String cdnUrl) {
        return remoteReference.previewBasicReviewContent(bookId, contentTypes, cdnUrl);
    }

    @Override
    public MapMessage loadUnitProgress(Teacher teacher, Map<Long, Long> groupIdClazzIdMap, String unitId, String bookId) {
        return remoteReference.loadUnitProgress(teacher, groupIdClazzIdMap, unitId, bookId);
    }

    @Override
    public MapMessage loadIntelligenceQuestion(TeacherDetail teacher, Collection<Long> groupIds, List<String> sectionIds, String bookId, String unitId,
                                               String algoType, Integer difficulty, Integer questionCount, Collection<String> kpIds, Collection<Integer> contentTypeIds,
                                               String objectiveConfigId, String type) {
        return remoteReference.loadIntelligenceQuestion(teacher, groupIds, sectionIds, bookId, unitId, algoType, difficulty, questionCount, kpIds, contentTypeIds, objectiveConfigId, type);
    }

    @Override
    public MapMessage loadObjectiveList(TeacherDetail teacher, List<String> sectionIds, String unitId, String bookId, HomeworkSourceType homeworkSourceType, String appVersion) {
        return remoteReference.loadObjectiveList(teacher, sectionIds, unitId, bookId, homeworkSourceType, appVersion);
    }

    @Override
    public MapMessage loadObjectiveContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds, String unitId, String bookId, ObjectiveConfigType objectiveConfigType,
                                           String objectiveConfigId, Integer currentPageNum, HomeworkSourceType homeworkSourceType, String sys, String appVersion) {
        return remoteReference.loadObjectiveContent(teacher, groupIds, sectionIds, unitId, bookId, objectiveConfigType, objectiveConfigId, currentPageNum, homeworkSourceType, sys, appVersion);
    }

    @Override
    public MapMessage loadExpandIndexData(TeacherDetail teacherDetail) {
        return remoteReference.loadExpandIndexData(teacherDetail);
    }

    @Override
    public MapMessage loadExpandObjectiveList(TeacherDetail teacherDetail, String bookId) {
        return remoteReference.loadExpandObjectiveList(teacherDetail, bookId);
    }

    @Override
    public MapMessage loadExpandObjectiveContent(TeacherDetail teacherDetail, String bookId, ObjectiveConfigType objectiveConfigType, String objectiveConfigId) {
        return remoteReference.loadExpandObjectiveContent(teacherDetail, bookId, objectiveConfigType, objectiveConfigId);
    }

    @Override
    public MapMessage loadExpandVideoDetail(String videoId) {
        return remoteReference.loadExpandVideoDetail(videoId);
    }

    @Override
    public List<Map<String, Object>> loadUniSoundWordScoreLevels(StudentDetail studentDetail) {
        List<Map<String, Object>> wordScoreLevels = null;
        try {
            wordScoreLevels = remoteReference.loadUniSoundWordScoreLevels(studentDetail);
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(wordScoreLevels)) {
            wordScoreLevels = Unisound8WordScoreLevel.levels;
        }
        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(wordScoreLevels)) {
            for (Map<String, Object> scoreLevel : wordScoreLevels) {
                scoreLevels.add(processScoreLevel(scoreLevel));
            }
        }
        return scoreLevels;
    }

    @Override
    public String loadImageQualityStr(StudentDetail studentDetail) {
        return remoteReference.loadImageQualityStr(studentDetail);
    }

    @Override
    public List<Map<String, Object>> loadUniSoundSentenceScoreLevels(StudentDetail studentDetail) {
        List<Map<String, Object>> sentenceScoreLevels = null;
        try {
            sentenceScoreLevels = remoteReference.loadUniSoundSentenceScoreLevels(studentDetail);
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(sentenceScoreLevels)) {
            sentenceScoreLevels = Unisound8SentenceScoreLevel.levels;
        }
        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sentenceScoreLevels)) {
            for (Map<String, Object> scoreLevel : sentenceScoreLevels) {
                scoreLevels.add(processScoreLevel(scoreLevel));
            }
        }
        return scoreLevels;
    }

    @Override
    public List<Map<String, Object>> loadUniSoundWordTeachSentenceScoreLevels(StudentDetail studentDetail) {
        List<Map<String, Object>> sentenceScoreLevels = null;
        try {
            sentenceScoreLevels = remoteReference.loadUniSoundWordTeachSentenceScoreLevels(studentDetail);
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(sentenceScoreLevels)) {
            sentenceScoreLevels = WordTeachUniSound7SentenceScoreLevel.levels;
        }
        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sentenceScoreLevels)) {
            for (Map<String, Object> scoreLevel : sentenceScoreLevels) {
                scoreLevels.add(processScoreLevel(scoreLevel));
            }
        }
        return scoreLevels;
    }

    @Override
    public List<Map<String, Object>> loadVoxSentenceScoreLevels(StudentDetail studentDetail) {
        List<Map<String, Object>> sentenceScoreLevels = null;
        try {
            sentenceScoreLevels = remoteReference.loadVoxSentenceScoreLevels(studentDetail);
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(sentenceScoreLevels)) {
            sentenceScoreLevels = Vox8SentenceScoreLevel.levels;
        }
        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sentenceScoreLevels)) {
            for (Map<String, Object> scoreLevel : sentenceScoreLevels) {
                scoreLevels.add(processScoreLevel(scoreLevel));
            }
        }
        return scoreLevels;
    }

    @Override
    public List<Map<String, Object>> loadVoxSongScoreLevels(StudentDetail studentDetail) {
        List<Map<String, Object>> songScoreLevels = null;
        try {
            songScoreLevels = remoteReference.loadVoxSongScoreLevels(studentDetail);
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(songScoreLevels)) {
            songScoreLevels = Vox8SongScoreLevel.levels;
        }
        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(songScoreLevels)) {
            for (Map<String, Object> scoreLevel : songScoreLevels) {
                scoreLevels.add(processScoreLevel(scoreLevel));
            }
        }
        return scoreLevels;
    }

    @Override
    public List<Map<String, Object>> loadVoxOralCommunicationSingleLevel(StudentDetail studentDetail) {
        List<Map<String, Object>> scoreLevels = null;
        try {
            scoreLevels = remoteReference.loadVoxOralCommunicationSingleLevel(studentDetail);
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(scoreLevels)) {
            scoreLevels = Vox8SongScoreLevel.levels;
        }
        List<Map<String, Object>> scoreLevelsResult = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(scoreLevels)) {
            for (Map<String, Object> scoreLevel : scoreLevels) {
                scoreLevelsResult.add(processOralCommunicationSingleLevel(scoreLevel));
            }
        }
        return scoreLevels;
    }

    @Override
    public List<Map<String, Object>> loadVoxOralCommunicationTotalLevel(StudentDetail studentDetail) {
        List<Map<String, Object>> appScoreLevels = null;
        try {
            appScoreLevels = remoteReference.loadVoxOralCommunicationTotalLevel(studentDetail);
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(appScoreLevels)) {
            appScoreLevels = Vox8SongScoreLevel.levels;
        }
        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(appScoreLevels)) {
            for (Map<String, Object> scoreLevel : appScoreLevels) {
                scoreLevels.add(processScoreLevel(scoreLevel));
            }
        }
        return scoreLevels;
    }

    private Map<String, Object> processScoreLevel(Map<String, Object> scoreLevel) {
        String level = SafeConverter.toString(scoreLevel.get("level"));
        int minScore = SafeConverter.toInt(scoreLevel.get("minScore"));
        int maxScore = SafeConverter.toInt(scoreLevel.get("maxScore"));
        AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(level);
        return MapUtils.m(
                "level", level,
                "minScore", minScore,
                "maxScore", maxScore,
                "score", (int) appOralScoreLevel.getScore()
        );
    }

    public static Map<String, Object> processOralCommunicationSingleLevel(Map<String, Object> scoreLevels) {
        String level = SafeConverter.toString(scoreLevels.get("level"));
        Integer integrityMinScore = SafeConverter.toInt(scoreLevels.get("integrityMinScore"));
        Integer integrityMaxScore = SafeConverter.toInt(scoreLevels.get("integrityMaxScore"));
        Integer pronunciationMinScore = SafeConverter.toInt(scoreLevels.get("pronunciationMinScore"));
        Integer pronunciationMaxScore = SafeConverter.toInt(scoreLevels.get("pronunciationMaxScore"));
        Integer star = SafeConverter.toInt(scoreLevels.get("star"));
        Double keyStandardMinScore = SafeConverter.toDouble(scoreLevels.get("keyStandardMinScore"));
        AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(level);
        return MapUtils.m(
                "level", level,
                "star", star,
                "integrityMinScore", integrityMinScore,
                "integrityMaxScore", integrityMaxScore,
                "pronunciationMinScore", pronunciationMinScore,
                "pronunciationMaxScore", pronunciationMaxScore,
                "keyStandardMinScore", keyStandardMinScore,
                "score", SafeConverter.toInt(appOralScoreLevel.getScore()));
    }

    @Override
    public MapMessage loadVoiceEngineConfig(StudentDetail studentDetail, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.loadVoiceEngineConfig(studentDetail, objectiveConfigType);
    }

    @Override
    public MapMessage loadPracticeDetail(Teacher teacher, String homeworkId) {
        return remoteReference.loadPracticeDetail(teacher, homeworkId);
    }

    @Override
    public MapMessage loadSameLevelClazzList(Teacher teacher, String homeworkId) {
        return remoteReference.loadSameLevelClazzList(teacher, homeworkId);
    }

    @Override
    public MapMessage loadNaturalSpellingContent(TeacherDetail teacherDetail, String bookId, String unitId, String objectiveConfigId, Integer level) {
        return remoteReference.loadNaturalSpellingContent(teacherDetail, bookId, unitId, objectiveConfigId, level);
    }

    @Override
    public MapMessage loadDubbingAlbumList(Integer clazzLevel) {
        return remoteReference.loadDubbingAlbumList(clazzLevel);
    }

    @Override
    public MapMessage loadDubbingRecommendSearchWords() {
        return remoteReference.loadDubbingRecommendSearchWords();
    }

    @Override
    public MapMessage searchDubbing(TeacherDetail teacherDetail, Integer clazzLevel, String searchWord, List<String> channelIds, List<String> albumIds, List<String> themeIds, String bookId, String unitId, Pageable pageable, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.searchDubbing(teacherDetail, clazzLevel, searchWord, channelIds, albumIds, themeIds, bookId, unitId, pageable, objectiveConfigType);
    }

    @Override
    public MapMessage loadPictureBookPlusTopicList() {
        return remoteReference.loadPictureBookPlusTopicList();
    }

    @Override
    public MapMessage loadPictureBookPlusSeriesList() {
        return remoteReference.loadPictureBookPlusSeriesList();
    }

    @Override
    public MapMessage loadPictureBookPlusRecommendSearchWords() {
        return remoteReference.loadPictureBookPlusRecommendSearchWords();
    }

    @Override
    public MapMessage searchPictureBookPlus(TeacherDetail teacherDetail, String clazzLevel, List<String> topicIds, List<String> seriesIds, String searchWord, String bookId, String unitId, Pageable pageable, String sys, String appVersion) {
        return remoteReference.searchPictureBookPlus(teacherDetail, clazzLevel, topicIds, seriesIds, searchWord, bookId, unitId, pageable, sys, appVersion);
    }

    @Override
    public MapMessage loadPictureBookPlusHistory(TeacherDetail teacherDetail, String bookId, String unitId, Pageable pageable, String sys, String appVersion) {
        return remoteReference.loadPictureBookPlusHistory(teacherDetail, bookId, unitId, pageable, sys, appVersion);
    }

    @Override
    public MapMessage loadIndexRecommendContent(TeacherDetail teacher, String appVersion) {
        return remoteReference.loadIndexRecommendContent(teacher, appVersion);
    }

    @Override
    public MapMessage loadBasicMathContent(String bookId, Integer stageId) {
        return remoteReference.loadBasicMathContent(bookId, stageId);
    }

    @Override
    public MapMessage loadBasicChineseContent(Teacher teacher, String bookId, Integer stageId) {
        return remoteReference.loadBasicChineseContent(teacher, bookId, stageId);
    }

    @Override
    public MapMessage loadTeacherClazzListForRecommend(TeacherDetail teacher, Set<NewHomeworkType> newHomeworkTypes, Boolean filterEmptyClazz) {
        return remoteReference.loadTeacherClazzListForRecommend(teacher, newHomeworkTypes, filterEmptyClazz);
    }

    @Override
    public MapMessage loadNewIndexRecommendContent(TeacherDetail teacherDetail, String sys, String appVersion, String cdnUrl) {
        return remoteReference.loadNewIndexRecommendContent(teacherDetail, sys, appVersion, cdnUrl);
    }

    @Override
    public MapMessage loadRecommendJumpParams(TeacherDetail teacherDetail, ObjectiveConfigType objectiveConfigType, String sys, String appVersion, String id) {
        return remoteReference.loadRecommendJumpParams(teacherDetail, objectiveConfigType, sys, appVersion, id);
    }

    @Override
    public MapMessage loadObjectiveWaterfallContent(TeacherDetail teacher, String objectiveId, Set<Long> groupIds, List<String> sectionIds, String bookId, String unitId, String sys, String appVersion) {
        return remoteReference.loadObjectiveWaterfallContent(teacher, objectiveId, groupIds, sectionIds, bookId, unitId, sys, appVersion);
    }

    @Override
    public MapMessage loadBasicAppWaterfallContent(TeacherDetail teacher, String objectiveConfigId, String bookId, String unitId, String categoryGroup) {
        return remoteReference.loadBasicAppWaterfallContent(teacher, objectiveConfigId, bookId, unitId, categoryGroup);
    }

    @Override
    public MapMessage loadDubbingCollectionRecord(TeacherDetail teacherDetail, String bookId, String unitId, Pageable pageable, String sys, String appVersion) {
        return remoteReference.loadDubbingCollectionRecord(teacherDetail, bookId, unitId, pageable, sys, appVersion);
    }

    @Override
    public MapMessage loadOralCommunicationSearchWords() {
        return remoteReference.loadOralCommunicationSearchWords();
    }

    @Override
    public MapMessage searchOralCommunication(TeacherDetail teacherDetail, String clazzLevel, String type, String searchWord, String bookId, String uniId, Pageable pageable) {
        return remoteReference.searchOralCommunication(teacherDetail, clazzLevel, type, searchWord, bookId, uniId, pageable);
    }

    @Override
    public MapMessage loadOralCommunicationDetail(TeacherDetail teacherDetail, String bookId, String unitId, String oralCommunicationId) {
        return remoteReference.loadOralCommunicationDetail(teacherDetail, bookId, unitId, oralCommunicationId);
    }

    @Override
    public MapMessage loadTeachingResourceDefaultBook(TeacherDetail teacherDetail) {
        return remoteReference.loadTeachingResourceDefaultBook(teacherDetail);
    }

    @Override
    public MapMessage changeTeachingResourceDefaultBook(TeacherDetail teacherDetail, String bookId) {
        return remoteReference.changeTeachingResourceDefaultBook(teacherDetail, bookId);
    }

    @Override
    public MapMessage loadTeachingResourceTypeList(TeacherDetail teacherDetail, String bookId, String unitId) {
        return remoteReference.loadTeachingResourceTypeList(teacherDetail, bookId, unitId);
    }

    @Override
    public MapMessage loadTeachingResourceContent(TeacherDetail teacherDetail, String bookId, String unitId, String sectionId, String type, Map params) {
        return remoteReference.loadTeachingResourceContent(teacherDetail, bookId, unitId, sectionId, type, params);
    }

    @Override
    public MapMessage loadNaturalSpellingLevelsContent(String bookId) {
        return remoteReference.loadNaturalSpellingLevelsContent(bookId);
    }

    @Override
    public List<Integer> loadHomeworkReportShareChannel(TeacherDetail teacherDetail) {
        return remoteReference.loadHomeworkReportShareChannel(teacherDetail);
    }

    @Override
    public MapMessage loadOcrMentalBookList(Teacher teacher) {
        return remoteReference.loadOcrMentalBookList(teacher);
    }

    @Override
    public MapMessage addOcrMentalBook(Teacher teacher, String bookName) {
        return remoteReference.addOcrMentalBook(teacher, bookName);
    }

    @Override
    public MapMessage deleteOcrMentalBook(Teacher teacher, String bookId) {
        return remoteReference.deleteOcrMentalBook(teacher, bookId);
    }

    @Override
    public MapMessage loadOcrDictationUnitList(Teacher teacher, List<Long> groupIds) {
        return remoteReference.loadOcrDictationUnitList(teacher, groupIds);
    }

    @Override
    public MapMessage loadOcrDictationContent(Teacher teacher, String bookId, String unitId) {
        return remoteReference.loadOcrDictationContent(teacher, bookId, unitId);
    }
}
