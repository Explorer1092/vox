package com.voxlearning.utopia.service.crm.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.crm.api.bean.ApplyProcessResult;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.api.loader.ApplyManagementLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.*;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ApplyManagementLoaderImpl
 *
 * @author song.wang
 * @date 2017/1/4
 */
@Named
@Service(interfaceClass = ApplyManagementLoader.class)
@ExposeService(interfaceClass = ApplyManagementLoader.class)
public class ApplyManagementLoaderImpl extends SpringContainerSupport implements ApplyManagementLoader {

    @Inject
    private AgentModifyDictSchoolApplyPersistence agentModifyDictSchoolApplyPersistence;

    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;

    @Inject
    private AgentOrderPersistence agentOrderPersistence;
    @Inject
    private AgentOrderProductPersistence agentOrderProductPersistence;
    @Inject
    private AgentProductFeedbackPersistence agentProductFeedbackPersistence;
    @Inject
    private UnifiedExamApplyPersistence unifiedExamApplyPersistence;
    @Inject
    private DataReportApplyPersistence dataReportApplyPersistence;
    @Inject
    private AgentGroupUserPersistence agentGroupUserPersistence;


    @Override
    public Page<AbstractBaseApply> fetchUserApplyList(SystemPlatformType platformType, String userAccount, ApplyStatus status, Boolean includeRevokeData, int pageNo, int pageSize) {
        List<AbstractBaseApply> retList = fetchUserApplyListByTypeAndStatus(platformType, userAccount, null, status);

        if (CollectionUtils.isNotEmpty(retList)) {
            if (includeRevokeData != null && includeRevokeData) {
                retList.forEach(p -> p.setCanRevoke(judgeCanRevoke(p)));
            }
            Collections.sort(retList, ((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())));
        }
        Pageable pageRequest = new PageRequest(pageNo - 1, pageSize);
        return PageableUtils.listToPage(retList, pageRequest);
    }


    @Override
    public AbstractBaseApply fetchApplyDetail(ApplyType applyType, Long applyId) {
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            return agentModifyDictSchoolApplyPersistence.load(applyId);
        } else if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
            return agentOrderPersistence.load(applyId);
        } else if (ApplyType.AGENT_PRODUCT_FEEDBACK == applyType) {
            return agentProductFeedbackPersistence.load(applyId);
        } else if(ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType){
            return unifiedExamApplyPersistence.load(applyId);
        }else if(ApplyType.AGENT_DATA_REPORT_APPLY == applyType){
            return dataReportApplyPersistence.load(applyId);
        }
        return null;
    }

    @Override
    public Boolean judgeCanRevokeApply(ApplyType applyType, Long applyId) {
        AbstractBaseApply apply = null;
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            apply = agentModifyDictSchoolApplyPersistence.load(applyId);
        } else if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
            apply = agentOrderPersistence.load(applyId);
        } else if (ApplyType.AGENT_PRODUCT_FEEDBACK == applyType) {
            apply = agentProductFeedbackPersistence.load(applyId);
        } else if(ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType){
            apply = unifiedExamApplyPersistence.load(applyId);
        } else if(ApplyType.AGENT_DATA_REPORT_APPLY == applyType){
            apply = dataReportApplyPersistence.load(applyId);
        }
        return judgeCanRevoke(apply);
    }

    private Boolean judgeCanRevoke(AbstractBaseApply apply) {
        if (apply != null) {
            if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == apply.getApplyType()) {
                if (ApplyStatus.PENDING == apply.getStatus()) {
                    WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(apply.getWorkflowId());
                    if (workFlowRecord != null && (Objects.equals(workFlowRecord.getStatus(), "init") || Objects.equals(workFlowRecord.getStatus(), "lv1"))) {
                        return true;
                    }
                }
            } else if (ApplyType.AGENT_MATERIAL_APPLY == apply.getApplyType()) {
                // fixme 生成的发货单可以撤销
                return ((AgentOrder) apply).getInvoiceId() == null;
              /*  List<WorkFlowProcessHistory> workFlowProcessHistoryList = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(apply.getWorkflowId());
                if (CollectionUtils.isEmpty(workFlowProcessHistoryList)) {
                    return true;
                }*/
            } else if (ApplyType.AGENT_PRODUCT_FEEDBACK == apply.getApplyType()) {
                return false;
            } else if (ApplyType.AGENT_UNIFIED_EXAM_APPLY == apply.getApplyType()){
                if (ApplyStatus.PENDING == apply.getStatus()) {
                    WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(apply.getWorkflowId());
                    if (workFlowRecord != null && (Objects.equals(workFlowRecord.getStatus(), "lv1"))) {
                        return true;
                    }
                }
                return false;
            } else if(ApplyType.AGENT_DATA_REPORT_APPLY == apply.getApplyType()){
                if (ApplyStatus.PENDING == apply.getStatus()) {
                    WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(apply.getWorkflowId());
                    if (workFlowRecord != null) {
                        if (Objects.equals(workFlowRecord.getStatus(), "init")) {
                            return true;
                        }
                        List<AgentGroupUser> users = agentGroupUserPersistence.findByUserId(SafeConverter.toLong(workFlowRecord.getCreatorAccount()));
                        if (CollectionUtils.isNotEmpty(users.stream().
                                filter(p -> p.getUserRoleType() != null && (p.getUserRoleType() == AgentRoleType.CityManager || p.getUserRoleType() == AgentRoleType.CityManager))
                                .collect(Collectors.toList()))) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        return false;
    }

    private AgentOrder generateAgentOrderByOrderId(Long id) {
        AgentOrder retOrder = agentOrderPersistence.load(id);
        retOrder.setOrderProductList(agentOrderProductPersistence.findByOrderId(id));
        return retOrder;
    }

    private AgentOrder generateAgentOrderByWorkflowId(Long workflowId) {
        AgentOrder retOrder = agentOrderPersistence.findByWorkflowId(workflowId);
        if (retOrder != null) {
            retOrder.setOrderProductList(agentOrderProductPersistence.findByOrderId(retOrder.getId()));
        }
        return retOrder;
    }

    @Override
    public ApplyWithProcessResultData fetchApplyWithProcessResultByApplyId(ApplyType applyType, Long applyId, Boolean withCurrentProcess) {
        AbstractBaseApply apply = null;
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            apply = agentModifyDictSchoolApplyPersistence.load(applyId);
        } else if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
            apply = generateAgentOrderByOrderId(applyId);
        } else if (ApplyType.AGENT_PRODUCT_FEEDBACK == applyType) {
            apply = agentProductFeedbackPersistence.load(applyId);
        }else if(ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType){
            apply = unifiedExamApplyPersistence.load(applyId);
        } else if (ApplyType.AGENT_DATA_REPORT_APPLY == applyType) {
            apply = dataReportApplyPersistence.load(applyId);
        }
        if (apply == null) {
            return null;
        }
        return generateApplyWithProcessResultData(apply, withCurrentProcess);
    }

    @Override
    public ApplyWithProcessResultData fetchApplyWithProcessResultByWorkflowId(ApplyType applyType, Long workflowId, Boolean withCurrentProcess) {
        AbstractBaseApply apply = null;
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            apply = agentModifyDictSchoolApplyPersistence.findByWorkflowId(workflowId);
        } else if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
            apply = generateAgentOrderByWorkflowId(workflowId);
        } else if (ApplyType.AGENT_PRODUCT_FEEDBACK == applyType) {
            apply = agentProductFeedbackPersistence.findByWorkflowId(workflowId);
        } else if(ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType){//统考申请
            apply =  unifiedExamApplyPersistence.loadByWorkflowId(workflowId);
        } else if (ApplyType.AGENT_DATA_REPORT_APPLY == applyType) {
            apply = dataReportApplyPersistence.findByWorkflowId(workflowId);
        }
        if (apply == null) {
            return null;
        }
        return generateApplyWithProcessResultData(apply, withCurrentProcess);
    }

    private ApplyWithProcessResultData generateApplyWithProcessResultData(AbstractBaseApply apply, Boolean withCurrentProcess) {
        if (apply == null) {
            return null;
        }
        ApplyWithProcessResultData retData = new ApplyWithProcessResultData();
        retData.setApply(apply);
        List<ApplyProcessResult> processResultList = new ArrayList<>();
        // 设置当前处理人员数据
        if (withCurrentProcess != null && withCurrentProcess) {
            List<WorkFlowProcess> currentProcessList = workFlowLoaderClient.loadWorkFlowProcessByWorkFlowId(apply.getWorkflowId());
            if (CollectionUtils.isNotEmpty(currentProcessList)) {
                ApplyProcessResult processResult = new ApplyProcessResult();
                processResult.setUserPlatform(convertToPlatformType(currentProcessList.get(0).getSourceApp()));
                List<String> accountNameList = currentProcessList.stream().map(WorkFlowProcess::getTargetUserName).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                String accountName = "";
                if (CollectionUtils.isNotEmpty(accountNameList)) {
                    accountName = StringUtils.join(accountNameList, ",");
                }
                processResult.setAccountName(accountName);
                processResult.setResult("待审核");
                processResultList.add(processResult);
            }
        }

        // 设置历史处理数据
        List<WorkFlowProcessHistory> workFlowProcessHistoryList = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(apply.getWorkflowId());
        if (CollectionUtils.isNotEmpty(workFlowProcessHistoryList)) {
            ApplyProcessResult processResult;
            for (WorkFlowProcessHistory workFlowProcessHistory : workFlowProcessHistoryList) {
                processResult = new ApplyProcessResult();
                processResult.setProcessDate(workFlowProcessHistory.getCreateDatetime());
                processResult.setUserPlatform(convertToPlatformType(workFlowProcessHistory.getSourceApp()));
                processResult.setAccount(workFlowProcessHistory.getProcessorAccount());
                processResult.setAccountName(workFlowProcessHistory.getProcessorName());
                processResult.setResult(workFlowProcessHistory.getResult().getDesc());
                processResult.setProcessNotes(workFlowProcessHistory.getProcessNotes());
                processResultList.add(processResult);
            }
        }
        retData.setProcessResultList(processResultList);
        return retData;
    }

    private SystemPlatformType convertToPlatformType(String platform) {
        if (Objects.equals("agent", platform)) {
            return SystemPlatformType.AGENT;
        } else if (Objects.equals("admin", platform)) {
            return SystemPlatformType.ADMIN;
        }
        return SystemPlatformType.UNKNOWN;
    }


    @Override
    public Page<AbstractBaseApply> fetchApplyListByType(ApplyType applyType, Date startDate, Date endDate, int pageNo, int pageSize) {
        List<AbstractBaseApply> retList = new ArrayList<>();
        // 字典表申请
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            List<AgentModifyDictSchoolApply> modifyDictSchoolApplyList = agentModifyDictSchoolApplyPersistence.findByDate(startDate, endDate);
            if (CollectionUtils.isNotEmpty(modifyDictSchoolApplyList)) {
                retList.addAll(modifyDictSchoolApplyList);
            }
        }

        // 物料申请
        if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
            List<AgentOrder> orders = agentOrderPersistence.findByCreateTime(startDate, endDate); // 不包含草稿状态的订单
            if (CollectionUtils.isNotEmpty(orders)) {
                retList.addAll(orders);
            }
        }

        // 统考申请
        if (ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType) {
            List<UnifiedExamApply> orders = unifiedExamApplyPersistence.findByCreateTime(startDate, endDate);
            if (CollectionUtils.isNotEmpty(orders)) {
                retList.addAll(orders);
            }
        }

        // 大数据报告
        if(ApplyType.AGENT_DATA_REPORT_APPLY == applyType){
            List<DataReportApply> dataReportApplyList = dataReportApplyPersistence.findByDate(startDate, endDate);
            if (CollectionUtils.isNotEmpty(dataReportApplyList)) {
                retList.addAll(dataReportApplyList);
            }
        }

        if (CollectionUtils.isNotEmpty(retList)) {
            Collections.sort(retList, ((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())));
        }
        Pageable pageRequest = new PageRequest(pageNo - 1, pageSize);
        return PageableUtils.listToPage(retList, pageRequest);
    }

    private List<AbstractBaseApply> fetchUserApplyListByTypeAndStatus(SystemPlatformType platformType, String userAccount, ApplyType applyType, ApplyStatus status) {
        List<AbstractBaseApply> retList = new ArrayList<>();

        if (applyType == null) {
            // 字典表修改
            List<AgentModifyDictSchoolApply> modifyDictSchoolApplyList = agentModifyDictSchoolApplyPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(modifyDictSchoolApplyList)) {
                retList.addAll(modifyDictSchoolApplyList);
            }

            // 物料申请
            List<AgentOrder> orderList = agentOrderPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(orderList)) {
                if (ApplyStatus.PENDING == status) {
                    // 过滤掉草稿状态的订单
                    orderList = orderList.stream().filter(p -> !Objects.equals(AgentOrderStatus.DRAFT.getStatus(), p.getOrderStatus())).collect(Collectors.toList());
                }
                orderList.forEach(p -> p.setOrderProductList(agentOrderProductPersistence.findByOrderId(p.getId())));
                retList.addAll(orderList);
            }

            // 产品反馈
            List<AgentProductFeedback> productFeedbackList = agentProductFeedbackPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(productFeedbackList)) {
                retList.addAll(productFeedbackList);
            }

            // 统考申请
            List<UnifiedExamApply> unifiedExamApplyList = unifiedExamApplyPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(unifiedExamApplyList)) {
                retList.addAll(unifiedExamApplyList);
            }

            // 大数据报告申请
            List<DataReportApply> dataReportApplyList = dataReportApplyPersistence.findByUser(platformType, userAccount);
            if(CollectionUtils.isNotEmpty(dataReportApplyList)){
                retList.addAll(dataReportApplyList);
            }


        } else if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            // 字典表修改
            List<AgentModifyDictSchoolApply> modifyDictSchoolApplyList = agentModifyDictSchoolApplyPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(modifyDictSchoolApplyList)) {
                retList.addAll(modifyDictSchoolApplyList);
            }
        } else if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {
            // 物料申请
            List<AgentOrder> orderList = agentOrderPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(orderList)) {
                if (ApplyStatus.PENDING == status) {
                    // 过滤掉草稿状态的订单
                    orderList = orderList.stream().filter(p -> !Objects.equals(AgentOrderStatus.DRAFT.getStatus(), p.getOrderStatus())).collect(Collectors.toList());
                }
                orderList.forEach(p -> p.setOrderProductList(agentOrderProductPersistence.findByOrderId(p.getId())));
                retList.addAll(orderList);
            }
        } else if (ApplyType.AGENT_PRODUCT_FEEDBACK == applyType) {
            // 产品反馈
            List<AgentProductFeedback> productFeedbackList = agentProductFeedbackPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(productFeedbackList)) {
                retList.addAll(productFeedbackList);
            }
        }else if(ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType){
            // 统考申请
            List<UnifiedExamApply> unifiedExamApplyList = unifiedExamApplyPersistence.findByUser(platformType, userAccount);
            if (CollectionUtils.isNotEmpty(unifiedExamApplyList)) {
                retList.addAll(unifiedExamApplyList);
            }
        }else if(ApplyType.AGENT_DATA_REPORT_APPLY == applyType){
            // 大数据报告申请
            List<DataReportApply> dataReportApplyList = dataReportApplyPersistence.findByUser(platformType, userAccount);
            if(CollectionUtils.isNotEmpty(dataReportApplyList)){
                retList.addAll(dataReportApplyList);
            }
        }

        if (status != null) {
            retList = retList.stream().filter(p -> p.getStatus() == status).collect(Collectors.toList());
        }
        return retList;
    }

    @Override
    public Page<ApplyWithProcessResultData> fetchUserApplyWithProcessResult(SystemPlatformType platformType, String userAccount, ApplyType applyType, ApplyStatus status, Date startDate, Date endDate, Boolean withCurrentProcess, int pageNo, int pageSize) {
        List<AbstractBaseApply> applyList = fetchUserApplyListByTypeAndStatus(platformType, userAccount, applyType, status);

        if (startDate != null) {
            applyList = applyList.stream().filter(p -> p.getCreateDatetime().after(startDate)).collect(Collectors.toList());
        }
        if (endDate != null) {
            applyList = applyList.stream().filter(p -> p.getCreateDatetime().before(endDate)).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(applyList)) {
            Collections.sort(applyList, ((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())));
        }

        List<ApplyWithProcessResultData> retList = applyList.stream().map(p -> generateApplyWithProcessResultData(p, withCurrentProcess == null ? false : withCurrentProcess)).collect(Collectors.toList());
        Pageable pageRequest = new PageRequest(pageNo - 1, pageSize);
        return PageableUtils.listToPage(retList, pageRequest);
    }

    @Override
    public List<AgentModifyDictSchoolApply> fetchDictSchoolApplyListByUpdateDate(Date startDate, Date endDate) {
        return agentModifyDictSchoolApplyPersistence.findByUpdateDate(startDate, endDate);
    }
}
