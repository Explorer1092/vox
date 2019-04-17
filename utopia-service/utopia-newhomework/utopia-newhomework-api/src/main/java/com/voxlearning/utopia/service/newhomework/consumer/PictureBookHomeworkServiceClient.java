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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.service.PictureBookHomeworkService;

import java.util.Collection;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2016-07-14
 */
public class PictureBookHomeworkServiceClient implements PictureBookHomeworkService {

    @ImportService(interfaceClass = PictureBookHomeworkService.class)
    private PictureBookHomeworkService remoteReference;

    @Override
    public List<PictureBookSummaryResult> getPictureBookSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        return remoteReference.getPictureBookSummaryInfo(homeworkId, picBookIds, studentId);
    }

    @Override
    public List<PictureBookSummaryResult> getVacationPictureBookSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        return remoteReference.getVacationPictureBookSummaryInfo(homeworkId, picBookIds, studentId);
    }

    @Override
    public List<PictureBookPlusSummaryResult> getPictureBookPlusSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        return remoteReference.getPictureBookPlusSummaryInfo(homeworkId, picBookIds, studentId);
    }

    @Override
    public List<PictureBookPlusSummaryResult> getVacationPictureBookPlusSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        return remoteReference.getVacationPictureBookPlusSummaryInfo(homeworkId, picBookIds, studentId);
    }

    @Override
    public MapMessage getPictureBookDraftByPicBookId(String picBookId) {
        return remoteReference.getPictureBookDraftByPicBookId(picBookId);
    }

    @Override
    public MapMessage getPictureBookPlusDraft(String picBookId) {
        return remoteReference.getPictureBookPlusDraft(picBookId);
    }

    @Override
    public List<PictureBookSummaryResult> getLiveCastPictureBookSummaryInfo(LiveCastHomework liveCastHomework, Collection<String> picBookIds, Long studentId, String token) {
        return remoteReference.getLiveCastPictureBookSummaryInfo(liveCastHomework, picBookIds, studentId, token);
    }

    @Override
    public List<PictureBookPlusSummaryResult> getLiveCastPictureBookPlusSummaryInfo(LiveCastHomework liveCastHomework, Collection<String> picBookIds, Long studentId, String token) {
        return remoteReference.getLiveCastPictureBookPlusSummaryInfo(liveCastHomework, picBookIds, studentId, token);
    }
    @Override
    public MapMessage getPictureBookPlusDubbingDraft(String dubbingId) {
        return remoteReference.getPictureBookPlusDubbingDraft(dubbingId);
    }
}
