package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;
import com.voxlearning.utopia.service.crm.api.service.agent.UnifiedExamApplyService;

/**
 * Created by tao.zang on 2017/4/17.
 */
public class UnifiedExamApplyServiceClient implements UnifiedExamApplyService {

    @ImportService(interfaceClass = UnifiedExamApplyService.class)
    private UnifiedExamApplyService unifiedExamApplyService;
    @Override
    public UnifiedExamApply persist(UnifiedExamApply unifiedExamApply) {
        return unifiedExamApplyService.persist(unifiedExamApply);
    }

    @Override
    public UnifiedExamApply update(UnifiedExamApply unifiedExamApply) {
        return unifiedExamApplyService.update(unifiedExamApply);
    }

    @Override
    public MapMessage testPaperEnteryResult(String unifiedExamId, Integer entryStatus, String failureCause, String papers) {
        return unifiedExamApplyService.testPaperEnteryResult(unifiedExamId, entryStatus, failureCause, papers);
    }

    @Override
    public void updateStatus(Long id, ApplyStatus applyStatus) {
        unifiedExamApplyService.updateStatus(id,applyStatus);
    }
}
