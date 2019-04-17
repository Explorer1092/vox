package com.voxlearning.utopia.service.workflow.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.workflow.api.WorkFlowDataService;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowStatus;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowProcessPersistence;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowRecordPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/15
 */
@Named
@Service(interfaceClass = WorkFlowDataService.class)
@ExposeServices({
        @ExposeService(interfaceClass = WorkFlowDataService.class, version = @ServiceVersion(version = "20170426")),
        @ExposeService(interfaceClass = WorkFlowDataService.class, version = @ServiceVersion(version = "20170111"))
})
public class WorkFlowDataServiceImpl extends SpringContainerSupport implements WorkFlowDataService {

    @Inject
    private WorkFlowProcessPersistence workFlowProcessPersistence;

    @Inject
    private WorkFlowRecordPersistence workFlowRecordPersistence;

    private Map<String, Map<String, WorkFlowStatus>> workFlowConfigMap = WorkFlowConfigParser.getWorkFlowConfig(RuntimeMode.current());

    @Override
    public MapMessage clearWorkFlowRecordProcess(Long wordRecordId) {
        if (wordRecordId == null) {
            return MapMessage.successMessage();
        }
        workFlowRecordPersistence.disableByWorkflowRecordId(wordRecordId);
        workFlowProcessPersistence.deleteByWorkflowRecordId(wordRecordId);

        return MapMessage.successMessage();
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
        if (workFlowRecord.getId() == null) {
            workFlowRecordPersistence.insert(workFlowRecord);
        }
        workFlowRecord = workFlowRecordPersistence.load(workFlowRecord.getId());

        // 工作流创建后，将工作流指定给相应人员进行处理
        // 指定给相应人员处理
        saveProcessRecord(workFlowRecord, ArrayUtils.isEmpty(processUsers) ? new ArrayList<>() : Arrays.asList(processUsers));

        return MapMessage.successMessage().add("workFlowRecord", workFlowRecord);
    }

    @Override
    public MapMessage updateRecordStatus(Long wordRecordId, String status, List<WorkFlowProcessUser> processUsers) {
        if (wordRecordId == null || StringUtils.isBlank(status)) {
            return MapMessage.errorMessage("参数无效！");
        }
        WorkFlowRecord workFlowRecord = workFlowRecordPersistence.load(wordRecordId);
        if (workFlowRecord == null) {
            return MapMessage.errorMessage("工作流不存在！");
        }

        // 更新工作流状态
        workFlowRecord.setStatus(status);
        workFlowRecordPersistence.replace(workFlowRecord);

        // 清除当前待审核人员列表
        workFlowProcessPersistence.deleteByWorkflowRecordId(workFlowRecord.getId());

        // 重新设置新状态下的审核人员
        saveProcessRecord(workFlowRecord, processUsers);
        return MapMessage.successMessage();
    }


    private void saveProcessRecord(WorkFlowRecord workFlowRecord, List<WorkFlowProcessUser> processUsers) {
        List<WorkFlowProcessUser> processUserList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(processUsers)) {
            List<WorkFlowProcessUser> targetProcessUserList = processUsers.stream().filter(p -> p != null
                    && StringUtils.isNotBlank(p.getUserPlatform())
                    && StringUtils.isNotBlank(p.getAccount())
                    && StringUtils.isNotBlank(p.getAccountName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(targetProcessUserList)) {
                processUserList.addAll(targetProcessUserList);
            }
        }

        // 从配置信息获取处理人员数据
        WorkFlowType workFlowType = workFlowRecord.getWorkFlowType();
        if (workFlowType != null && workFlowConfigMap.get(workFlowType.getWorkflowName()) != null) {
            WorkFlowStatus workFlowStatus = workFlowConfigMap.get(workFlowType.getWorkflowName()).get(workFlowRecord.getStatus());
            if (workFlowStatus != null && CollectionUtils.isNotEmpty(workFlowStatus.getProcessUserList())) {
                processUserList.addAll(workFlowStatus.getProcessUserList());
            }
        }

        if (CollectionUtils.isNotEmpty(processUserList)) {
            List<WorkFlowProcess> processList = processUserList.stream().map(p -> {
                WorkFlowProcess workFlowProcess = new WorkFlowProcess();
                workFlowProcess.setWorkflowRecordId(workFlowRecord.getId());
                workFlowProcess.setSourceApp(p.getUserPlatform());
                workFlowProcess.setTargetUser(p.getAccount());
                workFlowProcess.setTargetUserName(p.getAccountName());
                workFlowProcess.setWorkFlowType(workFlowType);
                return workFlowProcess;
            }).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(processList)) {
                workFlowProcessPersistence.inserts(processList);
            }
        }
    }

}
