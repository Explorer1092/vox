package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.UnifiedExamApplyLoader;
import lombok.Getter;

/**
 * Created by dell on 2017/4/17.
 */
public class UnifiedExamApplyLoaderClient implements UnifiedExamApplyLoader {
    @Getter
    @ImportService(interfaceClass = UnifiedExamApplyLoader.class)
    private UnifiedExamApplyLoader remoteReference;
    public UnifiedExamApply findByWorkflowId(Long workflowId) {
        return remoteReference.findByWorkflowId(workflowId);
    }

    @Override
    public UnifiedExamApply load(Long id) {
        return remoteReference.load(id);
    }
}
