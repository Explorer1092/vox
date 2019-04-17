package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2017/1/13
 */
@ServiceVersion(version = "20190214")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface NewHomeworkProcessResultService extends IPingable {

    default void insert(NewHomeworkProcessResult entity) {
        if (entity == null) {
            return;
        }
        inserts(entity.getHomeworkId(), Collections.singleton(entity));
    }

    void inserts(String homeworkId, Collection<NewHomeworkProcessResult> entities);

    Boolean updateCorrection(String id,
                             String hid,
                             String qid,
                             Long userId,
                             Boolean review,
                             CorrectType correctType,
                             Correction correction,
                             String teacherMark,
                             Boolean isBatch);

    void classifyImage(String homeworkId,Long userId,List<String> ocrMentalAnswerIds);

    void classifyOcrDictationImage(String homeworkId, Long userId, List<String> ocrDictationAnswerIds);

    void upsert(SubHomeworkProcessResult entity);
}
