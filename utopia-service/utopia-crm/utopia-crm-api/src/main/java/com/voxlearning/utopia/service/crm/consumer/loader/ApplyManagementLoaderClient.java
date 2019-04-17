package com.voxlearning.utopia.service.crm.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.api.loader.ApplyManagementLoader;

import java.util.Date;
import java.util.List;

/**
 * ApplyManagementLoaderClient
 *
 * @author song.wang
 * @date 2017/1/4
 */
public class ApplyManagementLoaderClient implements ApplyManagementLoader {

    @ImportService(interfaceClass = ApplyManagementLoader.class)
    private ApplyManagementLoader remoteReference;

    @Override
    public Page<AbstractBaseApply> fetchUserApplyList(SystemPlatformType platformType, String userAccount, ApplyStatus status, Boolean includeRevokeData, int pageNo, int pageSize) {
        return remoteReference.fetchUserApplyList(platformType, userAccount, status, includeRevokeData, pageNo, pageSize);
    }

    @Override
    public AbstractBaseApply fetchApplyDetail(ApplyType applyType, Long applyId) {
        return remoteReference.fetchApplyDetail(applyType, applyId);
    }

    @Override
    public Boolean judgeCanRevokeApply(ApplyType applyType, Long applyId) {
        return remoteReference.judgeCanRevokeApply(applyType, applyId);
    }

    @Override
    public ApplyWithProcessResultData fetchApplyWithProcessResultByApplyId(ApplyType applyType, Long applyId, Boolean withCurrentProcess) {
        return remoteReference.fetchApplyWithProcessResultByApplyId(applyType, applyId, withCurrentProcess);
    }

    @Override
    public ApplyWithProcessResultData fetchApplyWithProcessResultByWorkflowId(ApplyType applyType, Long workflowId, Boolean withCurrentProcess) {
        return remoteReference.fetchApplyWithProcessResultByWorkflowId(applyType, workflowId, withCurrentProcess);
    }

    @Override
    public Page<AbstractBaseApply> fetchApplyListByType(ApplyType applyType, Date startDate, Date endDate, int pageNo, int pageSize) {
        return remoteReference.fetchApplyListByType(applyType, startDate, endDate, pageNo, pageSize);
    }

    @Override
    public Page<ApplyWithProcessResultData> fetchUserApplyWithProcessResult(SystemPlatformType platformType, String userAccount, ApplyType applyType, ApplyStatus status, Date startDate, Date endDate, Boolean withCurrentProcess, int pageNo, int pageSize) {
        return remoteReference.fetchUserApplyWithProcessResult(platformType, userAccount, applyType, status, startDate, endDate, withCurrentProcess, pageNo, pageSize);
    }

    @Override
    public List<AgentModifyDictSchoolApply> fetchDictSchoolApplyListByUpdateDate(Date startDate, Date endDate) {
        return remoteReference.fetchDictSchoolApplyListByUpdateDate(startDate, endDate);
    }
}
