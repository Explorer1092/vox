package com.voxlearning.utopia.service.workflow.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.workflow.api.WorkFlowLoader;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowProcessHistoryPersistence;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowProcessPersistence;
import com.voxlearning.utopia.service.workflow.impl.dao.WorkFlowRecordPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@Named
@Service(interfaceClass = WorkFlowLoader.class)
@ExposeService(interfaceClass = WorkFlowLoader.class)
public class WorkFlowLoaderImpl extends SpringContainerSupport implements WorkFlowLoader {

    @Inject private WorkFlowRecordPersistence workFlowRecordPersistence;
    @Inject private WorkFlowProcessPersistence workFlowProcessPersistence;
    @Inject private WorkFlowProcessHistoryPersistence workFlowProcessHistoryPersistence;


    @Override
    public Map<Long, WorkFlowRecord> loadWorkFlowRecords(Collection<Long> workFlowRecordIds) {
        if (CollectionUtils.isEmpty(workFlowRecordIds)) {
            return Collections.emptyMap();
        }
        return workFlowRecordPersistence.loads(workFlowRecordIds);
    }

    @Override
    public List<WorkFlowRecord> loadWorkFlowRecordsByCreatorAccount(String sourceApp, String createAccount) {
        if (StringUtils.isBlank(createAccount) || StringUtils.isBlank(sourceApp)) {
            return Collections.emptyList();
        }
        return workFlowRecordPersistence.loadByCreatorAccount(sourceApp, createAccount);
    }

    @Override
    public List<WorkFlowProcess> loadWorkFlowProcessByTargetUser(String sourceApp, String targetUser) {
        if (StringUtils.isBlank(targetUser) || StringUtils.isBlank(sourceApp)) {
            return Collections.emptyList();
        }
        return workFlowProcessPersistence.loadByTargetUser(sourceApp, targetUser);
    }

    @Override
    public List<WorkFlowProcessHistory> loadWorkFlowProcessHistoriesByProcessorAccount(String sourceApp, String processorAccount) {
        if (StringUtils.isBlank(processorAccount) || StringUtils.isBlank(sourceApp)) {
            return Collections.emptyList();
        }
        return workFlowProcessHistoryPersistence.loadByProcessorAccount(sourceApp, processorAccount);
    }

    @Override
    public List<WorkFlowProcess> loadWorkFlowProcessByWorkFlowId(Long workFlowId) {
        return workFlowProcessPersistence.loadByWorkflowRecordId(workFlowId);
    }

    @Override
    public List<WorkFlowProcessHistory> loadWorkFlowProcessHistoryByWorkFlowId(Long workFlowId) {
        return workFlowProcessHistoryPersistence.loadByWorkFlowRecordId(workFlowId);
    }

    @Override
    public Page<WorkFlowTargetUserProcessData> fetchTodoWorkflowList(String sourceApp, String userAccount, WorkFlowType workFlowType, Date startDate, Date endDate, String applicant, int pageNo, int pageSize) {
        List<WorkFlowProcess> processList = workFlowProcessPersistence.loadByUserAndType(sourceApp, userAccount, workFlowType, startDate, endDate);
        if (CollectionUtils.isEmpty(processList)) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<Long> workflowIdList = processList.stream().map(WorkFlowProcess::getWorkflowRecordId).collect(Collectors.toList());

        Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowRecordPersistence.loads(workflowIdList);
        if (MapUtils.isEmpty(workFlowRecordMap)) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<WorkFlowRecord> list = workFlowRecordMap.values().stream()
                .filter(p -> StringUtils.isBlank(applicant) || StringUtils.countMatches(p.getCreatorName(), applicant) > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return new PageImpl<>(Collections.emptyList());
        }
        pageNo = Integer.max(pageNo, 0);
        list.sort(Comparator.comparing(WorkFlowRecord::getCreateDatetime));
        Pageable pageRequest = new PageRequest(pageNo, pageSize);

        Page<WorkFlowRecord> recordPage = PageableUtils.listToPage(list, pageRequest);
        // 这里实际上没必要全部加载

        List<WorkFlowTargetUserProcessData> targetResultList = recordPage.getContent()
                .stream().map(p -> {
                    WorkFlowTargetUserProcessData item = new WorkFlowTargetUserProcessData();
                    item.setWorkFlowRecord(p);
//                    List<WorkFlowProcessHistory> processHistoryList = workFlowProcessHistoryPersistence.loadByWorkFlowRecordId(p.getId());
//                    if (CollectionUtils.isNotEmpty(processHistoryList)) { // 用户可以有多次处理， 获取当前用户最近的处理意见
//                        List<WorkFlowProcessHistory> currentUserHistoryList = processHistoryList.stream()
//                                .filter(k -> Objects.equals(k.getSourceApp(), sourceApp) && Objects.equals(k.getProcessorAccount(), userAccount))
//                                .collect(Collectors.toList());
//                        if (CollectionUtils.isNotEmpty(currentUserHistoryList)) {
//                            currentUserHistoryList.sort(Comparator.comparing(WorkFlowProcessHistory::getCreateDatetime));
//                            item.setProcessHistory(currentUserHistoryList.get(0));
//                        }
//                    }
                    WorkFlowProcessHistory history = workFlowProcessHistoryPersistence.loadByWorkFlowRecordId(p.getId()).stream()
                            .filter(k -> Objects.equals(k.getSourceApp(), sourceApp) && Objects.equals(k.getProcessorAccount(), userAccount))
                            .sorted(Comparator.comparing(WorkFlowProcessHistory::getCreateDatetime))
                            .findFirst().orElse(null);
                    item.setProcessHistory(history);
                    return item;
                }).collect(Collectors.toList());
        return new PageImpl<>(targetResultList, pageRequest, list.size());
    }

    @Override
    public Page<WorkFlowTargetUserProcessData> fetchDoneWorkflowList(String sourceApp, String userAccount, WorkFlowType workFlowType, WorkFlowProcessResult processResult, Date startDate, Date endDate, int pageNo, int pageSize) {
        // 调整为直接读取Page然后再填充
        pageNo = Integer.max(0, pageNo);
        pageSize = Integer.min(pageSize, 50);  // 最多50条就够了吧
        Pageable pageRequest = new PageRequest(pageNo, pageSize);
//        List<WorkFlowProcessHistory> historyList = workFlowProcessHistoryPersistence.loadByUserAndType(sourceApp, userAccount, workFlowType, processResult, startDate, endDate);
        Page<WorkFlowProcessHistory> historyPage = workFlowProcessHistoryPersistence.loadPageByUserAndType(sourceApp, userAccount, workFlowType, processResult, startDate, endDate, pageRequest);
        List<WorkFlowProcessHistory> historyList = historyPage.getContent();
        if (CollectionUtils.isEmpty(historyList)) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 是已经排好序的
//        Map<Long, WorkFlowProcessHistory> targetHistoryMap = historyList.stream().collect(Collectors.toMap(WorkFlowProcessHistory::getWorkFlowRecordId, Function.identity(), (o1, o2) -> {
//            if (o1.getCreateDatetime().compareTo(o2.getCreateDatetime()) > 0) {
//                return o1;
//            }
//            return o2;
//        }));
        List<WorkFlowTargetUserProcessData> list = historyList.stream()
                .map(p -> {
                    WorkFlowTargetUserProcessData item = new WorkFlowTargetUserProcessData();
                    item.setProcessHistory(p);
                    WorkFlowRecord record = workFlowRecordPersistence.load(p.getWorkFlowRecordId());
                    if (record == null) {
                        return null;
                    }
//                    item.setWorkFlowRecord(workFlowRecordPersistence.load(p.getWorkFlowRecordId()));
                    item.setWorkFlowRecord(record);
                    return item;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        return new PageImpl<>(list, pageRequest, historyPage.getTotalElements());
    }

    @Override
    public WorkFlowRecord loadWorkFlowRecord(Long workFlowRecordId) {
        WorkFlowRecord workFlowRecord = workFlowRecordPersistence.load(workFlowRecordId);
        if (workFlowRecord == null || workFlowRecord.isDisabledTrue()) {
            return null;
        }
        return workFlowRecord;
    }


    @Override
    public Map<Long, List<WorkFlowProcessHistory>> loadWorkFlowProcessHistoriesByWorkFlowId(Collection<Long> workFlowRecordIds) {
        if (CollectionUtils.isEmpty(workFlowRecordIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<WorkFlowProcessHistory>> result = new HashMap<>();
        workFlowRecordIds.forEach(tempworkFlowRecordId -> result.put(tempworkFlowRecordId, workFlowProcessHistoryPersistence.loadByWorkFlowRecordId(tempworkFlowRecordId)));
        return result;
    }
}
