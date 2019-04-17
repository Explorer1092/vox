package com.voxlearning.utopia.service.workflow.consumer;


import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.workflow.api.WorkFlowDataService;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;

import java.util.List;

/**
 * @author fugui.chang
 * @since 2016/11/15
 */
public class WorkFlowDataServiceClient implements WorkFlowDataService {

    @ImportService(interfaceClass = WorkFlowDataService.class)
    private WorkFlowDataService remoteReference;


    @Override
    public MapMessage clearWorkFlowRecordProcess(Long wordRecordId) {
        if (wordRecordId == null) {
            return MapMessage.successMessage();
        }
        return remoteReference.clearWorkFlowRecordProcess(wordRecordId);
    }

    @Override
    public MapMessage addWorkFlowRecord(WorkFlowRecord workFlowRecord, WorkFlowProcessUser... processUsers) {
        if (workFlowRecord == null || StringUtils.isBlank(workFlowRecord.getCreatorAccount())
                || StringUtils.isBlank(workFlowRecord.getCreatorName())
                || StringUtils.isBlank(workFlowRecord.getLatestProcessorName())
                || StringUtils.isBlank(workFlowRecord.getSourceApp())
                || StringUtils.isBlank(workFlowRecord.getStatus())
                || StringUtils.isBlank(workFlowRecord.getTaskContent())
                || StringUtils.isBlank(workFlowRecord.getTaskName())) {
            return MapMessage.errorMessage("请正确设置workFlowRecord");
        }
        return remoteReference.addWorkFlowRecord(workFlowRecord, processUsers);
    }

    @Override
    public MapMessage updateRecordStatus(Long wordRecordId, String status, List<WorkFlowProcessUser> processUsers) {
        if(wordRecordId == null || StringUtils.isBlank(status)){
            return MapMessage.errorMessage("参数无效！");
        }
        return remoteReference.updateRecordStatus(wordRecordId, status, processUsers);
    }

}
