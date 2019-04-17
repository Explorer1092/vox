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

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181128")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface FlashGameService extends IPingable {

    @Idempotent
    Map<String, Object> loadVocabularyListenData(Long lessonId, PracticeType englishPractice);

    @Idempotent
    Map<String, Object> loadVocabularySpeakData(Long lessonId, PracticeType englishPractice);

    @Idempotent
    Map<String, Object> loadGrammarData(Long lessonId, PracticeType englishPractice);

    @Idempotent
    Map<String, Object> loadListenAndSpeakData(Long lessonId, PracticeType englishPractice);

    @Idempotent
    Map<String, Object> loadVocabularyAndPictureData(Long lessonId, PracticeType englishPractice);

    @Idempotent
    Map<String, Object> loadVocabularyAndParaphraseData(Long lessonId, PracticeType englishPractice);

    @Idempotent
    MapMessage loadData(Long userId, String cdnUrl, Long lessonId, PracticeType englishPractice, Ktwelve k12, Boolean isNew);

    @Idempotent
    MapMessage loadDataFromSentenceList(Long userId, String cdnUrl,
                                        List<Sentence> sentences,
                                        PracticeType englishPractice,
                                        Ktwelve k12,
                                        Map<String, String> algovMap,
                                        Boolean isNew);

    @Idempotent
    Map<String, Object> loadMentalArithmeticDataWithDscp(Long pointId,
                                                         Integer amount,
                                                         PracticeType mathPractice,
                                                         String dataType,
                                                         String baseDscp);

    @Idempotent
    Map<String, Object> loadMentalArithmeticData(Long pointId, Integer amount, PracticeType mathPractice, String dataType);

    @Idempotent
    List<Map<String, Object>> loadEnglishSentenceMappers(List<Sentence> sentences, Ktwelve ktwelve);

    @Idempotent
    MapMessage loadNewData(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12,
                           String homeworkId, Integer categoryId, Boolean isNew, NewHomeworkType newHomeworkType, String objectiveConfigType);

    @Idempotent
    MapMessage loadPreviewNewDate(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12, List<String> questionIds, Boolean isNew, String bookId);
}
