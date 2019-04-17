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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TtsListeningService extends IPingable {

    Page<TtsListeningPaper> getListeningPaperPageByUserId(Long userId, Pageable pageable, String title);

    Page<TtsListeningPaper> getListenPaperPageByUidAndBid(Long userId, Long bookId, Pageable pageable);

    default Page<TtsListeningPaper> getSharedListeningPaperPage(Pageable pageable, Long bookId, Integer classLevel) {
        return getSharedListeningPaperPage(pageable, bookId, classLevel, Ktwelve.PRIMARY_SCHOOL);
    }

    Page<TtsListeningPaper> getSharedListeningPaperPage(Pageable pageable, Long bookId, Integer classLevel, Ktwelve ktwelve);

    TtsListeningPaper getListeningPaperById(String id);

    String saveListeningPaper(TtsListeningPaper paper);

    boolean deleteListeningPaper(String id, Long userId);

    // o2o support, may need to be moved to a new service (like tts-offline service)
    // by changyuan.liu

    // 默认存储到GFS里的前缀
    String TTS_O2O_FILE_PREFIX = "o2o-tts-";

}
