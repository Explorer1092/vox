package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.DataReportApplyLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.DataReportApplyPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/7
 */
@Named
@Service(interfaceClass = DataReportApplyLoader.class)
@ExposeService(interfaceClass = DataReportApplyLoader.class)
public class DataReportApplyLoaderImpl extends SpringContainerSupport implements DataReportApplyLoader {

    @Inject
    private DataReportApplyPersistence persistence;

    @Override
    public List<DataReportApply> loadByAccount(SystemPlatformType userPlatform, String account) {
        return persistence.findByUser(userPlatform, account);
    }

    @Override
    public DataReportApply loadByWorkflowId(Long workflowId) {
        return persistence.findByWorkflowId(workflowId);
    }
}
