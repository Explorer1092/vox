package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.api.service.agent.DataReportApplyService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.DataReportApplyPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/7
 */
@Named
@Service(interfaceClass = DataReportApplyService.class)
@ExposeService(interfaceClass = DataReportApplyService.class)
public class DataReportApplyServiceImpl extends SpringContainerSupport implements DataReportApplyService {

    @Inject
    private DataReportApplyPersistence persistence;

    @Override
    public Long persist(DataReportApply apply) {
        persistence.insert(apply);
        return apply.getId();
    }

    @Override
    public DataReportApply update(DataReportApply apply) {
        return  persistence.replace(apply);
    }

    @Override
    public Boolean updateWorkflowId(Long id, Long workflowId) {
        DataReportApply item = persistence.load(id);
        if (item == null) {
            return false;
        }
        item.setWorkflowId(workflowId);
        return persistence.replace(item) != null;
    }
}
