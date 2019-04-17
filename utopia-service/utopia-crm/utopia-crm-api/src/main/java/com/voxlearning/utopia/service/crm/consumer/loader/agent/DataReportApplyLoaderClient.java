package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.DataReportApplyLoader;

import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/7
 */
public class DataReportApplyLoaderClient implements DataReportApplyLoader {
    @ImportService(interfaceClass = DataReportApplyLoader.class)
    private DataReportApplyLoader remoteReference;

    @Override
    public List<DataReportApply> loadByAccount(SystemPlatformType userPlatform, String account) {
        return remoteReference.loadByAccount(userPlatform, account);
    }

    @Override
    public DataReportApply loadByWorkflowId(Long workflowId) {
        return remoteReference.loadByWorkflowId(workflowId);
    }
}
