package com.voxlearning.utopia.service.workflow.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.workflow.api.WorkFlowService;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.*;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowProcessHistoryPersistence;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowProcessPersistence;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowRecordPersistence;
import com.voxlearning.utopia.service.workflow.impl.queue.WorkflowAdminQueueProducer;
import com.voxlearning.utopia.service.workflow.impl.queue.WorkflowAgentQueueProducer;
import com.voxlearning.utopia.service.workflow.impl.queue.WorkflowMizarQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@Named
@Service(interfaceClass = WorkFlowService.class)
@ExposeServices({
        @ExposeService(interfaceClass = WorkFlowService.class, version = @ServiceVersion(version = "20161107")),
        @ExposeService(interfaceClass = WorkFlowService.class, version = @ServiceVersion(version = "20170327"))
})
public class WorkFlowServiceImpl extends SpringContainerSupport implements WorkFlowService {

    @Inject private WorkFlowRecordPersistence workFlowRecordPersistence;
    @Inject private WorkFlowProcessPersistence workFlowProcessPersistence;
    @Inject private WorkFlowProcessHistoryPersistence workFlowProcessHistoryPersistence;
    @Inject private WorkflowAdminQueueProducer workflowAdminQueueProducer;
    @Inject private WorkflowAgentQueueProducer workflowAgentQueueProducer;
    @Inject private WorkflowMizarQueueProducer workflowMizarQueueProducer;

    private Map<String, Map<String, WorkFlowStatus>> workFlowConfigMap = WorkFlowConfigParser.getWorkFlowConfig(RuntimeMode.current());

    private final List<WorkFlowType> newAdminModeFlow = Arrays.asList(
            WorkFlowType.ADMIN_SEND_APP_PUSH, WorkFlowType.ADMIN_WECHAT_NOTICE
    );

    @Override
    public MapMessage agree(WorkFlowContext workFlowContext) {
        MapMessage message = checkWorkFlowContext(workFlowContext);
        if (!message.isSuccess()) {
            return message;
        }

        WorkFlowStatus workFlowStatus = workFlowConfigMap.get(workFlowContext.getWorkFlowName()).get(workFlowContext.getWorkFlowRecord().getStatus());
        WorkFlowEvent workFlowEvent = workFlowStatus.getEventMap().get("agree");
        if (workFlowEvent == null) {
            return MapMessage.errorMessage(workFlowContext.getWorkFlowName() + "工作流下的" + workFlowStatus.getName() + "状态下没有事件agree");
        }
//        //验证处理权限
        MapMessage judgeMessage = judgeAllowProcess(workFlowContext, workFlowStatus);
        if (!judgeMessage.isSuccess()) {
            return judgeMessage;
        }

        WorkFlowRecord workFlowRecord = workFlowContext.getWorkFlowRecord();
        WorkFlowRecord tempWorkFlowRecord = workFlowRecordPersistence.load(workFlowRecord.getId());
        if (tempWorkFlowRecord == null || !StringUtils.equals(workFlowRecord.getStatus(), tempWorkFlowRecord.getStatus())) {
            //避免并发操作时,两人同时操作后,1人操作释放锁后,第2人的操作进入到方法里
            return MapMessage.errorMessage("该操作已经被操作过了");
        }

        // remove workFlowProcess
        workFlowProcessPersistence.deleteByWorkflowRecordId(workFlowRecord.getId());

        // insert workFlowProcessHistory
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(),
                workFlowContext.getProcessorAccount(), workFlowContext.getProcessorName(), WorkFlowProcessResult.agree, workFlowContext.getProcessNotes(), workFlowRecord.getWorkFlowType());
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        WorkFlowStatus newWorkFlowStatus = workFlowConfigMap.get(workFlowContext.getWorkFlowName()).get(workFlowEvent.getStatus());
        // newWorkFlowStatus==null时,不添加;
        // newWorkFlowStatus!=null时:processors != null 或 processors == null
        flowToOthers(workFlowContext, newWorkFlowStatus);

        workFlowRecord.setStatus(workFlowEvent.getStatus());
        workFlowRecord.setLatestProcessorName(workFlowContext.getProcessorName());
        workFlowRecordPersistence.upsert(workFlowRecord);

        if (StringUtils.isNotBlank(workFlowEvent.getMqmsg())) {
            sendMqmsg(workFlowContext, workFlowContext.getSourceApp(), WorkFlowProcessResult.nameOf(workFlowEvent.getName()), workFlowRecord.getStatus(), workFlowEvent.getMqmsg(), newWorkFlowStatus != null && !SafeConverter.toBoolean(newWorkFlowStatus.getAftertreatment()));
        }

        return MapMessage.successMessage();
    }

    //验证处理权限
    private MapMessage judgeAllowProcess(WorkFlowContext workFlowContext, WorkFlowStatus workFlowStatus) {
        WorkFlowType workFlowType = workFlowContext.getWorkFlowRecord().getWorkFlowType();
        // 验证处理权限
        // FIXME Admin里的 AppPush 使用以往的兼容模式
        if (workFlowType != null && !newAdminModeFlow.contains(workFlowType)) {
            // 获取配置信息中运行处理的人员列表
            List<WorkFlowProcessUser> processUserList = workFlowStatus.getProcessUserList();
            boolean allowProcess = false;
            if (CollectionUtils.isNotEmpty(processUserList)) {
                allowProcess = processUserList.stream().anyMatch(p -> Objects.equals(workFlowContext.getSourceApp(), p.getUserPlatform()) && Objects.equals(workFlowContext.getProcessorAccount(), p.getAccount())); // && Objects.equals(workFlowContext.getProcessorName(), p.getAccountName()));
            }

            if (!allowProcess) {
                // 配置信息没有配置的情况下， 判断当前工作流的审核节点是否在该用户名下
                List<WorkFlowProcess> processList = workFlowProcessPersistence.loadByWorkflowRecordId(workFlowContext.getWorkFlowRecord().getId());
                if (CollectionUtils.isNotEmpty(processList)) {
                    allowProcess = processList.stream().anyMatch(p -> Objects.equals(workFlowContext.getSourceApp(), p.getSourceApp()) && Objects.equals(workFlowContext.getProcessorAccount(), p.getTargetUser()));// && Objects.equals(workFlowContext.getProcessorName(), p.getTargetUserName()));
                }
            }
            if (!allowProcess) {
                return MapMessage.errorMessage("您无处理权限，请与管理员联系");
            }
        } else if (newAdminModeFlow.contains(workFlowType)) {
            // 名字需要去掉比如 "admin:"的前缀
            String[] processors = workFlowStatus.getProcessor();
            if (processors != null) {
                boolean processorFlag = false;
                for (String tempProcessName : processors) {
                    // 去掉前缀
                    if (tempProcessName.indexOf(':') > 0) {
                        tempProcessName = tempProcessName.split(":")[1];
                    }
                    if (Objects.equals(tempProcessName, workFlowContext.getProcessorName())) {
                        processorFlag = true;
                    }
                }
                if (!processorFlag) {
                    return MapMessage.errorMessage(workFlowContext.getProcessorName() + "无处理权限:" + workFlowContext.getWorkFlowName() + "工作流下的" + workFlowStatus.getName() + "状态下的事件");
                }
            }
        } else {  // 兼容现有功能
            String[] processors = workFlowStatus.getProcessor();
            if (processors != null) {
                boolean processorFlag = false;
                for (String tempProcessName : processors) {
                    if (Objects.equals(tempProcessName, workFlowContext.getProcessorName())) {
                        processorFlag = true;
                    }
                }
                if (!processorFlag) {
                    return MapMessage.errorMessage(workFlowContext.getProcessorName() + "无处理权限:" + workFlowContext.getWorkFlowName() + "工作流下的" + workFlowStatus.getName() + "状态下的事件");
                }
            }
        }
        return MapMessage.successMessage();
    }

    // insert new workFlowProcess
    private void flowToOthers(WorkFlowContext workFlowContext, WorkFlowStatus workFlowStatus) {
        if (workFlowStatus == null) { // 没有后续状态，工作流结束
            return;
        }
        List<WorkFlowProcess> workFlowProcessList = new ArrayList<>();
        WorkFlowRecord workFlowRecord = workFlowContext.getWorkFlowRecord();
        WorkFlowType workFlowType = workFlowRecord.getWorkFlowType();
        if (workFlowType != null && !newAdminModeFlow.contains(workFlowType)) {
            Set<WorkFlowProcessUser> processUserList = new TreeSet<>((o1, o2) -> {
                if (Objects.equals(o1.getUserPlatform(), o2.getUserPlatform()) && Objects.equals(o1.getAccount(), o2.getAccount())) {
                    return 0;
                }
                return 1;
            });
            if (CollectionUtils.isNotEmpty(workFlowStatus.getProcessUserList())) {
                processUserList.addAll(workFlowStatus.getProcessUserList());
            }
            if (CollectionUtils.isNotEmpty(workFlowContext.getProcessUserList())) {
                processUserList.addAll(workFlowContext.getProcessUserList());
            }

            Long workflowId = workFlowContext.getWorkFlowRecord().getId();
            processUserList.forEach(p -> {
                WorkFlowProcess workFlowProcess = new WorkFlowProcess();
                workFlowProcess.setWorkflowRecordId(workflowId);
                workFlowProcess.setSourceApp(p.getUserPlatform());
                workFlowProcess.setTargetUser(p.getAccount());
                workFlowProcess.setTargetUserName(p.getAccountName());
                workFlowProcess.setWorkFlowType(workFlowContext.getWorkFlowRecord().getWorkFlowType());
                workFlowProcessList.add(workFlowProcess);
            });
        } else if (newAdminModeFlow.contains(workFlowType)) {
            //第一步
            //把动态指定的人员也加入工作流
            Set<WorkFlowProcessUser> processUserList = new TreeSet<>((o1, o2) -> {
                if (Objects.equals(o1.getUserPlatform(), o2.getUserPlatform()) && Objects.equals(o1.getAccount(), o2.getAccount())) {
                    return 0;
                }
                return 1;
            });
            if (CollectionUtils.isNotEmpty(workFlowContext.getProcessUserList())) {
                processUserList.addAll(workFlowContext.getProcessUserList());
            }

            Long workflowId = workFlowContext.getWorkFlowRecord().getId();
            processUserList.forEach(p -> {
                WorkFlowProcess workFlowProcess = new WorkFlowProcess();
                workFlowProcess.setWorkflowRecordId(workflowId);
                workFlowProcess.setSourceApp(p.getUserPlatform());
                workFlowProcess.setTargetUser(p.getAccount());
                workFlowProcess.setTargetUserName(p.getAccountName());
                workFlowProcess.setWorkFlowType(workFlowContext.getWorkFlowRecord().getWorkFlowType());
                workFlowProcessList.add(workFlowProcess);
            });
            //第二步。xml里配置的人员加入工作流
            // 名字需要去掉比如 "admin:"的前缀
            String[] processors = workFlowStatus.getProcessor();
            if (processors != null) {
                for (String processor : processors) {
                    // 去掉前缀
                    if (processor.indexOf(':') > 0) {
                        processor = processor.split(":")[1];
                    }
                    workFlowProcessList.add(new WorkFlowProcess(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(), processor, "", workFlowRecord.getWorkFlowType()));
                }
            } else {
                workFlowProcessList.add(new WorkFlowProcess(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(), workFlowContext.getProcessorName(), "", workFlowRecord.getWorkFlowType()));
            }
        } else {  // 兼容现有功能
            String[] processors = workFlowStatus.getProcessor();
            if (processors != null) {
                for (String processor : processors) {
                    workFlowProcessList.add(new WorkFlowProcess(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(), processor, "", workFlowRecord.getWorkFlowType()));
                }
            } else {
                workFlowProcessList.add(new WorkFlowProcess(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(), workFlowContext.getProcessorName(), "", workFlowRecord.getWorkFlowType()));
            }
        }
        if (CollectionUtils.isNotEmpty(workFlowProcessList)) {
            workFlowProcessPersistence.inserts(workFlowProcessList);
        }
    }


    @Override
    public MapMessage reject(WorkFlowContext workFlowContext) {
        MapMessage message = checkWorkFlowContext(workFlowContext);
        if (!message.isSuccess()) {
            return message;
        }

        WorkFlowStatus workFlowStatus = workFlowConfigMap.get(workFlowContext.getWorkFlowName()).get(workFlowContext.getWorkFlowRecord().getStatus());
        WorkFlowEvent workFlowEvent = workFlowStatus.getEventMap().get("reject");
        if (workFlowEvent == null) {
            return MapMessage.errorMessage(workFlowContext.getWorkFlowName() + "工作流下的" + workFlowStatus.getName() + "状态下没有事件reject");
        }

        //验证处理权限
        MapMessage judgeMessage = judgeAllowProcess(workFlowContext, workFlowStatus);
        if (!judgeMessage.isSuccess()) {
            return judgeMessage;
        }

        WorkFlowRecord workFlowRecord = workFlowContext.getWorkFlowRecord();
        WorkFlowRecord tempWorkFlowRecord = workFlowRecordPersistence.load(workFlowRecord.getId());
        if (tempWorkFlowRecord == null || !StringUtils.equals(workFlowRecord.getStatus(), tempWorkFlowRecord.getStatus())) {
            //避免并发操作时,两人同时操作后,1人操作释放锁后,第2人的操作进入到方法里
            return MapMessage.errorMessage("该操作已经被操作过了");
        }

        // remove workFlowProcess
        workFlowProcessPersistence.deleteByWorkflowRecordId(workFlowRecord.getId());

        // insert workFlowProcessHistory
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(),
                workFlowContext.getProcessorAccount(), workFlowContext.getProcessorName(), WorkFlowProcessResult.reject, workFlowContext.getProcessNotes(), workFlowRecord.getWorkFlowType());
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        WorkFlowStatus newWorkFlowStatus = workFlowConfigMap.get(workFlowContext.getWorkFlowName()).get(workFlowEvent.getStatus());
        // newWorkFlowStatus==null时,不添加;
        // newWorkFlowStatus!=null时:processors != null 或 processors == null
        flowToOthers(workFlowContext, newWorkFlowStatus);

        workFlowRecord.setStatus(workFlowEvent.getStatus());
        workFlowRecord.setLatestProcessorName(workFlowContext.getProcessorName());
        workFlowRecordPersistence.upsert(workFlowRecord);

        if (StringUtils.isNotBlank(workFlowEvent.getMqmsg())) {
            sendMqmsg(workFlowContext, workFlowContext.getSourceApp(), WorkFlowProcessResult.nameOf(workFlowEvent.getName()), workFlowRecord.getStatus(), workFlowEvent.getMqmsg(), newWorkFlowStatus != null && !SafeConverter.toBoolean(newWorkFlowStatus.getAftertreatment()));
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage raiseup(WorkFlowContext workFlowContext) {
        MapMessage message = checkWorkFlowContext(workFlowContext);
        if (!message.isSuccess()) {
            return message;
        }

        WorkFlowStatus workFlowStatus = workFlowConfigMap.get(workFlowContext.getWorkFlowName()).get(workFlowContext.getWorkFlowRecord().getStatus());
        WorkFlowEvent workFlowEvent = workFlowStatus.getEventMap().get("raiseup");
        if (workFlowEvent == null) {
            return MapMessage.errorMessage(workFlowContext.getWorkFlowName() + "工作流下的" + workFlowStatus.getName() + "状态下没有事件raiseup");
        }
        //        //验证处理权限
        MapMessage judgeMessage = judgeAllowProcess(workFlowContext, workFlowStatus);
        if (!judgeMessage.isSuccess()) {
            return judgeMessage;
        }

        WorkFlowRecord workFlowRecord = workFlowContext.getWorkFlowRecord();
        WorkFlowRecord tempWorkFlowRecord = workFlowRecordPersistence.load(workFlowRecord.getId());
        if (tempWorkFlowRecord == null || !StringUtils.equals(workFlowRecord.getStatus(), tempWorkFlowRecord.getStatus())) {
            //避免并发操作时,两人同时操作后,1人操作释放锁后,第2人的操作进入到方法里
            return MapMessage.errorMessage("该操作已经被操作过了");
        }

        // remove workFlowProcess
        workFlowProcessPersistence.deleteByWorkflowRecordId(workFlowRecord.getId());

        // insert workFlowProcessHistory
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(),
                workFlowContext.getProcessorAccount(), workFlowContext.getProcessorName(), WorkFlowProcessResult.raiseup, workFlowContext.getProcessNotes(), workFlowRecord.getWorkFlowType());
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        // insert new workFlowProcess
        List<WorkFlowProcess> workFlowProcessList = new ArrayList<>();
        WorkFlowStatus newWorkFlowStatus = workFlowConfigMap.get(workFlowContext.getWorkFlowName()).get(workFlowEvent.getStatus());
        // newWorkFlowStatus==null时,不添加;
        // newWorkFlowStatus!=null时:processors != null 或 processors == null
        flowToOthers(workFlowContext, newWorkFlowStatus);

        workFlowRecord.setStatus(workFlowEvent.getStatus());
        workFlowRecord.setLatestProcessorName(workFlowContext.getProcessorName());
        workFlowRecordPersistence.upsert(workFlowRecord);

        if (!StringUtils.isBlank(workFlowEvent.getMqmsg())) {
            sendMqmsg(workFlowContext, workFlowContext.getSourceApp(), WorkFlowProcessResult.nameOf(workFlowEvent.getName()), workFlowRecord.getStatus(), workFlowEvent.getMqmsg(), newWorkFlowStatus != null && !SafeConverter.toBoolean(newWorkFlowStatus.getAftertreatment()));
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage revoke(WorkFlowContext workFlowContext) {
        MapMessage message = checkWorkFlowContext(workFlowContext);
        if (!message.isSuccess()) {
            return message;
        }

        WorkFlowRecord workFlowRecord = workFlowContext.getWorkFlowRecord();
        WorkFlowRecord tempWorkFlowRecord = workFlowRecordPersistence.load(workFlowRecord.getId());
        if (tempWorkFlowRecord == null || !StringUtils.equals(workFlowRecord.getStatus(), tempWorkFlowRecord.getStatus())) {
            //避免并发操作时,两人同时操作后,1人操作释放锁后,第2人的操作进入到方法里
            return MapMessage.errorMessage("该操作已经被操作过了");
        }

        // remove workFlowProcess
        workFlowProcessPersistence.deleteByWorkflowRecordId(workFlowRecord.getId());

        // insert workFlowProcessHistory
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory(workFlowContext.getWorkFlowRecord().getId(), workFlowContext.getSourceApp(),
                workFlowContext.getProcessorAccount(), workFlowContext.getProcessorName(), WorkFlowProcessResult.revoke, workFlowContext.getProcessNotes(), workFlowRecord.getWorkFlowType());
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        workFlowRecord.setStatus(WorkFlowProcessResult.revoke.name());
        workFlowRecord.setLatestProcessorName(workFlowContext.getProcessorName());
        workFlowRecordPersistence.upsert(workFlowRecord);

        // 发送消息
        sendMqmsg(workFlowContext, workFlowContext.getSourceApp(), WorkFlowProcessResult.revoke, workFlowRecord.getStatus(), WorkFlowProcessResult.revoke.name(), false);
        return MapMessage.successMessage();
    }

    @Override
    public void insertWorkFlowHistory(WorkFlowProcessHistory workFlowProcessHistory) {
        // insert workFlowProcessHistory
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);
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

        if (StringUtils.isBlank(workFlowContext.getWorkFlowRecord().getStatus()) || workFlowContext.getWorkFlowRecord().getId() == null) {
            return MapMessage.errorMessage("请正确设置WorkFlowContext中WorkFlowRecord:id=" + workFlowContext.getWorkFlowRecord().getId() + ",status=" + workFlowContext.getWorkFlowRecord().getStatus());
        }

        String workFlowName = workFlowContext.getWorkFlowName();
        if (!workFlowConfigMap.containsKey(workFlowName)) {
            return MapMessage.errorMessage("工作流" + workFlowName + "不存在");
        }

        WorkFlowStatus workFlowStatus = workFlowConfigMap.get(workFlowContext.getWorkFlowName()).get(workFlowContext.getWorkFlowRecord().getStatus());
        if (workFlowStatus == null) {
            return MapMessage.errorMessage("工作流" + workFlowName + "中状态" + workFlowContext.getWorkFlowRecord().getStatus() + "是结束状态或者不存在");
        }

        return MapMessage.successMessage();
    }

    //发送消息
    private MapMessage sendMqmsg(WorkFlowContext workFlowContext, String sourceApp, WorkFlowProcessResult processResult, String workflowStatus, String messageContent, boolean hasFollowStatus) {
        if (StringUtils.isBlank(messageContent)) {
            return MapMessage.errorMessage("sendMqmsg failed because  mqmsg is blank");
        }
        Map<String, String> map = new HashMap<>();
        map.put("configName", workFlowContext.getWorkFlowName());
        map.put("mqmsg", messageContent);
        map.put("recordId", workFlowContext.getWorkFlowRecord().getId().toString());
        map.put("status", workflowStatus);
        map.put("hasFollowStatus", String.valueOf(hasFollowStatus));  // 是否还有后续状态， 标志工作流是否结束了
        map.put("processResult", processResult != null ? processResult.name() : ""); // 本次处理结果  （通过， 拒绝 等）
        if (Objects.equals("agent", sourceApp)) {
            workflowAgentQueueProducer.getProducer().produce(Message.newMessage().writeObject(map));
        } else if (Objects.equals("admin", sourceApp)) {
            workflowAdminQueueProducer.getProducer().produce(Message.newMessage().writeObject(map));
        } else if (Objects.equals("mizar", sourceApp)) {
            workflowMizarQueueProducer.getProducer().produce(Message.newMessage().writeObject(map));
        } else {
            return MapMessage.errorMessage(workFlowContext.getWorkFlowName() + "工作流下workFlowRecordId=" + workFlowContext.getWorkFlowRecord().getId() + "时处理源是" + workFlowContext.getSourceApp());
        }
        return MapMessage.successMessage();
    }

}
