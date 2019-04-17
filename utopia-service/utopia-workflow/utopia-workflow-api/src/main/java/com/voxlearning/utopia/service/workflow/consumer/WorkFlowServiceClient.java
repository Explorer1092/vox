package com.voxlearning.utopia.service.workflow.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.workflow.api.WorkFlowLoader;
import com.voxlearning.utopia.service.workflow.api.WorkFlowService;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowContext;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
public class WorkFlowServiceClient implements WorkFlowService {

    @ImportService(interfaceClass = WorkFlowService.class)
    private WorkFlowService remoteReference;

    @ImportService(interfaceClass = WorkFlowLoader.class)
    private WorkFlowLoader workFlowLoaderReference;

    @Override
    public MapMessage agree(WorkFlowContext workFlowContext) {
        MapMessage checkResult = checkWorkFlowContext(workFlowContext);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("WorkFlowServiceClient")
                .keys(workFlowContext.getWorkFlowRecord().getId())
                .callback(() -> {
                    try {
                        return remoteReference.agree(workFlowContext);
                    }catch (CannotAcquireLockException e){
                        return MapMessage.errorMessage("请不要重复操作！").withDuplicatedException();
                    }
                })
                .build()
                .execute();
    }

    @Override
    public MapMessage reject(WorkFlowContext workFlowContext) {
        MapMessage checkResult = checkWorkFlowContext(workFlowContext);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("WorkFlowServiceClient")
                .keys(workFlowContext.getWorkFlowRecord().getId())
                .callback(() -> {
                    try {
                        return remoteReference.reject(workFlowContext);
                    }catch (CannotAcquireLockException e){
                        return MapMessage.errorMessage("请不要重复操作！").withDuplicatedException();
                    }
                })
                .build()
                .execute();
    }

    @Override
    public MapMessage raiseup(WorkFlowContext workFlowContext) {
        MapMessage checkResult = checkWorkFlowContext(workFlowContext);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("WorkFlowServiceClient")
                .keys(workFlowContext.getWorkFlowRecord().getId())
                .callback(() -> {
                    try {
                        return remoteReference.raiseup(workFlowContext);
                    }catch (CannotAcquireLockException e){
                        return MapMessage.errorMessage("请不要重复操作！").withDuplicatedException();
                    }
                })
                .build()
                .execute();
    }

    @Override
    public MapMessage revoke(WorkFlowContext workFlowContext) {
        MapMessage checkResult = checkWorkFlowContext(workFlowContext);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("WorkFlowServiceClient")
                .keys(workFlowContext.getWorkFlowRecord().getId())
                .callback(() -> {
                    try {
                        return remoteReference.revoke(workFlowContext);
                    }catch (CannotAcquireLockException e){
                        return MapMessage.errorMessage("请不要重复操作！").withDuplicatedException();
                    }
                })
                .build()
                .execute();
    }

    @Override
    public void insertWorkFlowHistory(WorkFlowProcessHistory workFlowProcessHistory) {
        if (workFlowProcessHistory == null || workFlowProcessHistory.getWorkFlowRecordId() == null) {
            return;
        }
        remoteReference.insertWorkFlowHistory(workFlowProcessHistory);
    }

    private MapMessage checkWorkFlowContext(WorkFlowContext workFlowContext) {
        if (workFlowContext == null || workFlowContext.getWorkFlowRecord() == null
                || StringUtils.isBlank(workFlowContext.getWorkFlowName())
                || StringUtils.isBlank(workFlowContext.getSourceApp())
                || StringUtils.isBlank(workFlowContext.getProcessorName())
                || StringUtils.isBlank(workFlowContext.getProcessorAccount())
                || StringUtils.isBlank(workFlowContext.getProcessNotes())) {
            return MapMessage.errorMessage("请正确设置WorkFlowContext");
        }
        return MapMessage.successMessage();
    }


    private WorkFlowContext generateWorkflowContext(WorkFlowRecord workFlowRecord, String userPlatform, String account, String accountName, String processNote, List<WorkFlowProcessUser> processUserList) {
        WorkFlowContext workFlowContext = new WorkFlowContext();
        workFlowContext.setWorkFlowRecord(workFlowRecord);
        workFlowContext.setWorkFlowName(workFlowRecord.getWorkFlowType().getWorkflowName());
        workFlowContext.setSourceApp(userPlatform);
        workFlowContext.setProcessNotes(processNote);
        workFlowContext.setProcessorAccount(account);
        workFlowContext.setProcessorName(accountName);
        if (CollectionUtils.isNotEmpty(processUserList)) {
            processUserList = processUserList.stream().filter(p -> StringUtils.isNotBlank(p.getUserPlatform()) && StringUtils.isNotBlank(p.getAccount())).collect(Collectors.toList());
            workFlowContext.setProcessUserList(processUserList);
        }
        return workFlowContext;
    }

    /**
     * 对工作流进行审核
     *
     * @param userPlatform    用户平台 admin, agent 等
     * @param account         处理者账号
     * @param accountName     处理者姓名
     * @param workflowId      工作流ID
     * @param processResult   处理结果
     * @param processNote     处理意见
     * @param processUserList 动态设定下次审核的人员列表
     * @return result
     */
    public MapMessage processWorkflow(String userPlatform, String account, String accountName, Long workflowId, WorkFlowProcessResult processResult, String processNote, List<WorkFlowProcessUser> processUserList) {
        if (workflowId == null) {
            return MapMessage.errorMessage("该条审核数据不存在");
        }
        WorkFlowRecord workFlowRecord = workFlowLoaderReference.loadWorkFlowRecord(workflowId);
        if (workFlowRecord == null) {
            return MapMessage.errorMessage("该条审核数据不存在");
        }
        if (WorkFlowProcessResult.agree == processResult) {
            return agree(generateWorkflowContext(workFlowRecord, userPlatform, account, accountName, processNote, processUserList));
        } else if (WorkFlowProcessResult.reject == processResult) {
            return reject(generateWorkflowContext(workFlowRecord, userPlatform, account, accountName, processNote, processUserList));
        } else if (WorkFlowProcessResult.revoke == processResult) {
            return revoke(generateWorkflowContext(workFlowRecord, userPlatform, account, accountName, processNote, processUserList));
        } else if (WorkFlowProcessResult.raiseup == processResult) {
            return raiseup(generateWorkflowContext(workFlowRecord, userPlatform, account, accountName, processNote, processUserList));
        }
        return MapMessage.errorMessage();
    }


}
