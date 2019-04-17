package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;
import com.voxlearning.alps.lang.util.MapMessage;
import java.util.concurrent.TimeUnit;

/**
 * Created by dell on 2017/4/17.
 */
@ServiceVersion(version = "2017.04.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface UnifiedExamApplyService extends IPingable {

    UnifiedExamApply persist(UnifiedExamApply unifiedExamApply);
    UnifiedExamApply update(UnifiedExamApply unifiedExamApply);

    /**
     * 统考试卷导入情况 （内容库调用该接口，反馈统考试卷审核及发布情况）
     * @param unifiedExamId
     * @param status
     * @return
     */
    MapMessage testPaperEnteryResult(String unifiedExamId, Integer status,String failureCause, String papers);

    void updateStatus( Long Id,ApplyStatus applyStatus);
}
