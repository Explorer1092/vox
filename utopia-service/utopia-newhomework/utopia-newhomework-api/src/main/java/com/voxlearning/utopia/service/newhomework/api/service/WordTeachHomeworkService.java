package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/12/3 下午2:08
 */
@ServiceVersion(version = "20181228")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface WordTeachHomeworkService extends IPingable {

    @Idempotent
    List<Map> getWordTeachSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);

    @Idempotent
    List<Map> getModuleSummaryInfo(String homeworkId, Long studentId, String stoneDataId, WordTeachModuleType wordTeachModuleType);
}
