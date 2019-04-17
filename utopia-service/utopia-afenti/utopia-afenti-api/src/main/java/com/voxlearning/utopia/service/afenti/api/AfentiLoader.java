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

package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.afenti.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.Video;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20160728")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AfentiLoader extends IPingable {

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = WrongQuestionLibrary.class,
            writeCache = false
    )
    List<WrongQuestionLibrary> loadWrongQuestionLibraryByUserIdAndSubject(@CacheParameter("userId") Long userId,
                                                                          @CacheParameter("subject") Subject subject);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = UserAfentiStats.class,
            writeCache = false
    )
    UserAfentiStats loadUserAfentiStats(@CacheParameter Long userId);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanPushExamHistory.class,
            writeCache = false
    )
    List<AfentiLearningPlanPushExamHistory> loadAfentiLearningPlanPushExamHistoryByUserId(@CacheParameter("UID") Long userId);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanPushExamHistory.class,
            writeCache = false
    )
    List<AfentiLearningPlanPushExamHistory> loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(
            @CacheParameter("UID") Long userId, @CacheParameter("NBID") String newBookId);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanUnitRankManager.class,
            writeCache = false
    )
    List<AfentiLearningPlanUnitRankManager> loadAfentiLearningPlanUnitRankManagerByNewBookId(@CacheParameter("NBID") String newBookId);

//    @Idempotent
//    @CacheMethod(
//            cacheSystem = CacheSystem.CBS,
//            cacheName = "flushable",
//            type = AfentiLearningPlanUnitRankManager.class,
//            writeCache = false,
//            key = "ALL"
//    )
//    List<String> loadBookIdsWithUnitRankManager();

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanUnitRankManager.class,
            writeCache = false,
            key = "ALL:P"
    )
    List<String> loadBookIdsWithUnitRankManagerForPreparation();

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanUserBookRef.class,
            writeCache = false
    )
    List<AfentiLearningPlanUserBookRef> loadAfentiLearningPlanUserBookRefByUserIdAndSubject(@CacheParameter("UID") Long userId,
                                                                                            @CacheParameter("S") Subject subject);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanUserFootprint.class,
            writeCache = false
    )
    AfentiLearningPlanUserFootprint loadAfentiLearningPlanUserFootprintByUserIdAndSubject(@CacheParameter("UID") Long userId,
                                                                                          @CacheParameter("S") Subject subject);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanUserFootprint.class,
            writeCache = false
    )
    Map<Long, AfentiLearningPlanUserFootprint> loadAfentiLearningPlanUserFootprintByUserIdsAndSubject(
            @CacheParameter(value = "UID", multiple = true) Collection<Long> userIds, @CacheParameter("S") Subject subject);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanUserRankStat.class,
            writeCache = false
    )
    List<AfentiLearningPlanUserRankStat> loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(@CacheParameter("UID") Long userId,
                                                                                                @CacheParameter("NBID") String newBookId);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiLearningPlanUserRankStat.class,
            writeCache = false
    )
    int loadUserTotalStar(@CacheParameter("UID") Long userId, @CacheParameter("S") Subject subject);

//    @Idempotent
//    @CacheMethod(
//            cacheSystem = CacheSystem.CBS,
//            cacheName = "flushable",
//            type = AfentiProductInfo.class,
//            writeCache = false,
//            key = "ALL_AFENTI_PRODUCT_INFOS"
//    )
//    List<AfentiProductInfo> loadAllAfentiProductsIncludeOffline();

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiQuizResult.class,
            writeCache = false
    )
    List<AfentiQuizResult> loadAfentiQuizResultByUserIdAndNewBookId(@CacheParameter("UID") Long userId,
                                                                    @CacheParameter("NBID") String newBookId);

    @Idempotent
    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = AfentiQuizStat.class,
            writeCache = false
    )
    List<AfentiQuizStat> loadAfentiQuizStatByUserId(@CacheParameter("UID") Long userId);

    List<Map<String, Object>> loadPurchaseInfos(StudentDetail student);

    Map<String,List<Video>> loadQuestionVideosByQuestionIds(Collection<String> ids);

    Map<String, Video> loadPreparationVideosByBookId(String bookId);

//    List<AfentiProductInfo> loadAllAfentiProductsByModifyPrice(StudentDetail studentDetail);
}
