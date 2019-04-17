/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.cache.support.CacheKeyBuilder;
import com.voxlearning.alps.cache.support.CacheKeyBuilderSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.GameTime;
import com.voxlearning.utopia.service.content.api.entity.Lesson;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.api.mapper.PracticeLesson;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;

/**
 * 用于从缓存中加载英语FLASH游戏的数据，以PracticeLesson为键值
 *
 * @author Xiaohai Zhang
 * @since Nov 30, 2014
 */
@CacheKeyBuilder(
        template = "EnglishFlashGameComboData:${source}:${revision}",
        revision = 1
)
@Named
public class EnglishFlashGameComboDataLoader extends CacheKeyBuilderSupport<PracticeLesson> {

    @Inject private EnglishContentLoaderClient englishContentLoaderClient;

    public EnglishFlashGameComboData load(PracticeLesson practiceLesson) {
        String key = buildCacheKey(Objects.requireNonNull(practiceLesson));
        UtopiaCache cache = CacheSystem.CBS.getCacheBuilder().getCache("flushable");
        CacheObject<EnglishFlashGameComboData> cacheObject = cache.get(key);
        if (cacheObject != null && cacheObject.getValue() != null) {
            return cacheObject.getValue();
        }
        // no EnglishFlashGameComboData hit in cache, continue to load directly
        Lesson lesson = englishContentLoaderClient.loadEnglishLesson(practiceLesson.getLessonId());
        List<Sentence> sentences = englishContentLoaderClient.loadEnglishLessonSentences(practiceLesson.getLessonId());
        if (CollectionUtils.isNotEmpty(sentences)) {
            sentences.forEach(Sentence::fixInvalidFields);
        }
        GameTime gameTime = englishContentLoaderClient.loadEnglishGameTime(practiceLesson);
        Book book = null;
        if (gameTime != null) {
            book = englishContentLoaderClient.loadEnglishBook(gameTime.getBookId());
        }

        EnglishFlashGameComboData data = new EnglishFlashGameComboData();
        data.setLesson(lesson);
        data.setSentences(sentences);
        data.setGameTime(gameTime);
        data.setBook(book);

        cache.add(key, 3600, data);
        return data;
    }
}
