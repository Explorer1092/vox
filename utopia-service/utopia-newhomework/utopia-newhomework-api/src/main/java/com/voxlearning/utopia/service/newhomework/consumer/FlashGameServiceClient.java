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

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.service.FlashGameService;

import java.util.List;
import java.util.Map;

/**
 * Flash游戏生成类
 *
 * @author xuesong.zhang
 * @since 2016-06-27
 */
public class FlashGameServiceClient implements FlashGameService {

    @ImportService(interfaceClass = FlashGameService.class)
    private FlashGameService flashGameService;

    @Override
    public Map<String, Object> loadVocabularyListenData(Long lessonId, PracticeType englishPractice) {
        return flashGameService.loadVocabularyListenData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadVocabularySpeakData(Long lessonId, PracticeType englishPractice) {
        return flashGameService.loadVocabularySpeakData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadGrammarData(Long lessonId, PracticeType englishPractice) {
        return flashGameService.loadGrammarData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadListenAndSpeakData(Long lessonId, PracticeType englishPractice) {
        return flashGameService.loadListenAndSpeakData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadVocabularyAndPictureData(Long lessonId, PracticeType englishPractice) {
        return flashGameService.loadVocabularyAndPictureData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadVocabularyAndParaphraseData(Long lessonId, PracticeType englishPractice) {
        return flashGameService.loadVocabularyAndParaphraseData(lessonId, englishPractice);
    }

    @Override
    public MapMessage loadData(Long userId, String cdnUrl, Long lessonId, PracticeType englishPractice, Ktwelve k12, Boolean isNew) {
        return flashGameService.loadData(userId, cdnUrl, lessonId, englishPractice, k12, isNew);
    }

    @Override
    public MapMessage loadDataFromSentenceList(Long userId, String cdnUrl, List<Sentence> sentences, PracticeType englishPractice, Ktwelve k12, Map<String, String> algovMap, Boolean isNew) {
        return flashGameService.loadDataFromSentenceList(userId, cdnUrl, sentences, englishPractice, k12, algovMap, isNew);
    }

    @Override
    public Map<String, Object> loadMentalArithmeticDataWithDscp(Long pointId, Integer amount, PracticeType mathPractice, String dataType, String baseDscp) {
        return flashGameService.loadMentalArithmeticDataWithDscp(pointId, amount, mathPractice, dataType, baseDscp);
    }

    @Override
    public Map<String, Object> loadMentalArithmeticData(Long pointId, Integer amount, PracticeType mathPractice, String dataType) {
        return flashGameService.loadMentalArithmeticData(pointId, amount, mathPractice, dataType);
    }

    @Override
    public List<Map<String, Object>> loadEnglishSentenceMappers(List<Sentence> sentences, Ktwelve ktwelve) {
        return flashGameService.loadEnglishSentenceMappers(sentences, ktwelve);
    }

    @Override
    public MapMessage loadNewData(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12,
                                  String homeworkId, Integer categoryId, Boolean isNew, NewHomeworkType newHomeworkType, String objectiveConfigType) {
        return flashGameService.loadNewData(userId, cdnUrl, newLessonId, englishPractice, k12, homeworkId, categoryId, isNew, newHomeworkType, objectiveConfigType);
    }

    @Override
    public MapMessage loadPreviewNewDate(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12, List<String> questionIds, Boolean isNew, String bookId) {
        return flashGameService.loadPreviewNewDate(userId, cdnUrl, newLessonId, englishPractice, k12, questionIds, isNew, bookId);
    }
}
