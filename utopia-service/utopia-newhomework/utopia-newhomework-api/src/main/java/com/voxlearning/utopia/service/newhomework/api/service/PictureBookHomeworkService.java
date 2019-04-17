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

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180808")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface PictureBookHomeworkService extends IPingable {
    /**
     * 绘本摘要信息
     *
     * @param picBookIds 绘本id
     * @return List
     */
    List<PictureBookSummaryResult> getPictureBookSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId);

    List<PictureBookSummaryResult> getVacationPictureBookSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId);

    List<PictureBookPlusSummaryResult> getPictureBookPlusSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId);

    List<PictureBookPlusSummaryResult> getVacationPictureBookPlusSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId);

    /**
     * 获取绘本信息的结构转换
     *
     * @param picBookId 绘本id
     * @return MapMessage
     */
    MapMessage getPictureBookDraftByPicBookId(String picBookId);

    MapMessage getPictureBookPlusDraft(String picBookId);

    /**
     * 给livecast的
     */
    List<PictureBookSummaryResult> getLiveCastPictureBookSummaryInfo(LiveCastHomework liveCastHomework, Collection<String> picBookIds, Long studentId, String token);


    List<PictureBookPlusSummaryResult> getLiveCastPictureBookPlusSummaryInfo(LiveCastHomework liveCastHomework, Collection<String> picBookIds, Long studentId, String token);

    MapMessage getPictureBookPlusDubbingDraft(String dubbingId);

}
