package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.UnifiedExamApplyLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.UnifiedExamApplyPersistence;
import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by dell on 2017/4/17.
 */
@Named
@Service(interfaceClass = UnifiedExamApplyLoader.class)
@ExposeService(interfaceClass = UnifiedExamApplyLoader.class)
public class UnifiedExamApplyLoaderImpl extends SpringContainerSupport implements UnifiedExamApplyLoader {
    @Inject
    private UnifiedExamApplyPersistence unifiedExamApplyPersistence;

    @Override
    public UnifiedExamApply findByWorkflowId(Long workflowId) {
        if(workflowId == null){
            return null;
        }
        return unifiedExamApplyPersistence.loadByWorkflowId(workflowId);
    }

    @Override
    public UnifiedExamApply load(Long id) {
        return unifiedExamApplyPersistence.load(id);
    }
}
