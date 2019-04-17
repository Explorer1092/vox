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

package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20160304")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClazzJournalManager extends IPingable {
    /**
     * Delete the clazz journal from database with specified id.
     *
     * @param id the clazz journal id to be deleted
     * @return affected rows
     */
    @ServiceMethod(timeout = 5, unit = TimeUnit.SECONDS, retries = 0)
    int deleteClazzJournal(Long id);

    /**
     * Find like details which created in specified range.
     *
     * @param start    the range start time
     * @param end      the range end time
     * @param pageable the pageable
     * @return like details
     */
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    Page<LikeDetail> findLikeDetails(Date start, Date end, Pageable pageable);
}
