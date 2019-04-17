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

package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.service.FlashGameService;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.FlashGameLoaderHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@Service(interfaceClass = FlashGameService.class)
@ExposeService(interfaceClass = FlashGameService.class)
public class FlashGameServiceImpl implements FlashGameService {

    @Inject private FlashGameLoaderHelper flashGameLoaderHelper;

    @Override
    public Map<String, Object> loadVocabularyListenData(Long lessonId, PracticeType englishPractice) {
        return flashGameLoaderHelper.loadVocabularyListenData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadVocabularySpeakData(Long lessonId, PracticeType englishPractice) {
        return flashGameLoaderHelper.loadVocabularySpeakData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadGrammarData(Long lessonId, PracticeType englishPractice) {
        return flashGameLoaderHelper.loadGrammarData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadListenAndSpeakData(Long lessonId, PracticeType englishPractice) {
        return flashGameLoaderHelper.loadListenAndSpeakData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadVocabularyAndPictureData(Long lessonId, PracticeType englishPractice) {
        return flashGameLoaderHelper.loadVocabularyAndPictureData(lessonId, englishPractice);
    }

    @Override
    public Map<String, Object> loadVocabularyAndParaphraseData(Long lessonId, PracticeType englishPractice) {
        return flashGameLoaderHelper.loadVocabularyAndParaphraseData(lessonId, englishPractice);
    }

    @Override
    public MapMessage loadData(Long userId, String cdnUrl, Long lessonId, PracticeType englishPractice, Ktwelve k12, Boolean isNew) {
        return flashGameLoaderHelper.loadData(userId, cdnUrl, lessonId, englishPractice, k12, isNew);
    }

    @Override
    public MapMessage loadDataFromSentenceList(Long userId, String cdnUrl, List<Sentence> sentences, PracticeType englishPractice, Ktwelve k12, Map<String, String> algovMap, Boolean isNew) {
        return flashGameLoaderHelper.loadDataFromSentenceList(userId, cdnUrl, sentences, englishPractice, k12, algovMap, isNew);
    }

    @Override
    public Map<String, Object> loadMentalArithmeticDataWithDscp(Long pointId, Integer amount, PracticeType mathPractice, String dataType, String baseDscp) {
        return flashGameLoaderHelper.loadMentalArithmeticDataWithDscp(pointId, amount, mathPractice, dataType, baseDscp);
    }

    @Override
    public Map<String, Object> loadMentalArithmeticData(Long pointId, Integer amount, PracticeType mathPractice, String dataType) {
        return flashGameLoaderHelper.loadMentalArithmeticData(pointId, amount, mathPractice, dataType);
    }

    @Override
    public List<Map<String, Object>> loadEnglishSentenceMappers(List<Sentence> sentences, Ktwelve ktwelve) {
        return flashGameLoaderHelper.loadEnglishSentenceMappers(sentences, ktwelve);
    }

    @Override
    public MapMessage loadNewData(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12,
                                  String homeworkId, Integer categoryId, Boolean isNew, NewHomeworkType newHomeworkType, String objectiveConfigType) {
        return flashGameLoaderHelper.loadNewData(userId, cdnUrl, newLessonId, englishPractice, k12, homeworkId, categoryId, isNew, newHomeworkType, objectiveConfigType);
    }

    @Override
    public MapMessage loadPreviewNewDate(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12, List<String> questionIds, Boolean isNew, String bookId) {
        return flashGameLoaderHelper.loadPreviewNewDate(userId, cdnUrl, newLessonId, englishPractice, k12, questionIds, isNew, bookId);
    }
}
