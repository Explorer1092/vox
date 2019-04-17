package com.voxlearning.utopia.agent.service.productfeedback;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.productfeedback.ProductFeedbackInfo;
import com.voxlearning.utopia.agent.bean.productfeedback.ProductFeedbackListInfo;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.crm.api.bean.ApplyProcessResult;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.consumer.loader.ApplyManagementLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentProductFeedbackLoadClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentProductFeedbackServiceClient;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by yaguang.wang
 * on 2017/2/21.
 */
@Named
public class AgentProductFeedbackService extends AbstractAgentService {
    @Inject private AgentProductFeedbackLoadClient agentProductFeedbackLoaderClient;
    @Inject private AgentProductFeedbackServiceClient agentProductFeedbackServiceClient;
    @Inject private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject private ApplyManagementLoaderClient applyManagementLoaderClient;


    private static final Integer PAGE_NO = 1;
    private static final Integer PAGE_SIZE = 9999;

    public AgentProductFeedback loadByWorkflowId(Long workflowId) {
        return agentProductFeedbackLoaderClient.findByWorkflowId(workflowId);
    }

    public ProductFeedbackListInfo createProductFeedbackInfo(Long userId) {
        ProductFeedbackListInfo result = new ProductFeedbackListInfo();
        Date mouthFirst = DayUtils.getFirstDayOfMonth(new Date());
        Page<ApplyWithProcessResultData> processResults = applyManagementLoaderClient.fetchUserApplyWithProcessResult(SystemPlatformType.AGENT, String.valueOf(userId), ApplyType.AGENT_PRODUCT_FEEDBACK, null,
                null, null, false, PAGE_NO, PAGE_SIZE);
        List<ProductFeedbackInfo> feedbackInfos = new ArrayList<>();
        for (ApplyWithProcessResultData processResult : processResults) {
            ProductFeedbackInfo info = createProductFeedbackInfo(processResult);
            if (info != null) {
                feedbackInfos.add(info);
            }
        }
        result.setProductFeedbackInfos(feedbackInfos);
        result.setTotalFeedbackCount(feedbackInfos.size());
        result.setTmFeedbackCount(feedbackInfos.stream().filter(p -> p.getCreateTime().after(mouthFirst)).count());
        return result;
    }

    public List<ApplyProcessResult> loadApplyProcessByApplyId(Long feedbackId) {
        if (feedbackId == null) {
            return Collections.emptyList();
        }
        ApplyWithProcessResultData applyWithProcessResultData = applyManagementLoaderClient.fetchApplyWithProcessResultByApplyId(ApplyType.AGENT_PRODUCT_FEEDBACK, feedbackId, false);
        if (applyWithProcessResultData == null) {
            return Collections.emptyList();
        }
        return applyWithProcessResultData.getProcessResultList();
    }

    public AgentProductFeedback loadAgentProductFeedbackById(Long id) {
        if (id == null) {
            return null;
        }
        return agentProductFeedbackLoaderClient.loadByFeedbackId(id);
    }

    public void createAgentProductFeedback(AgentProductFeedback feedback, Integer fbType, Boolean noticeFlag, String pic1Url, String pic2Url, String pic3Url,
                                           String content, String bookName, String bookGrade, String bookUnit, String bookCoveredArea, Integer bookCoveredStudentCount, boolean myself) {
        if (feedback == null) {
            return;
        }
        feedback.setFeedbackType(AgentProductFeedbackType.of(fbType));
        feedback.setNoticeFlag(noticeFlag);
        feedback.setPic1Url(pic1Url);
        feedback.setPic2Url(pic2Url);
        feedback.setPic3Url(pic3Url);
        feedback.setContent(content);
        if (!AgentProductFeedbackType.isClazz(1, AgentProductFeedbackType.of(fbType))) {
            feedback.setBookName(bookName);
            feedback.setBookGrade(bookGrade);
        }
        if (AgentProductFeedbackType.isClazz(2, AgentProductFeedbackType.of(fbType))) {
            feedback.setBookUnit(bookUnit);
        }
        if (AgentProductFeedbackType.isClazz(3, AgentProductFeedbackType.of(fbType))) {
            feedback.setBookCoveredArea(bookCoveredArea);
            feedback.setBookCoveredStudentCount(bookCoveredStudentCount);
        }
        if (myself) {
            feedback.setTeacherId(null);
            feedback.setTeacherName(null);
            feedback.setTeacherSubject(null);
        }
        feedback.setMySelf(myself);
    }

    public MapMessage saveProductFeedback(AgentProductFeedback feedback) {
        MapMessage msg = checkProductFeedback(feedback);
        if (!msg.isSuccess()) {
            return msg;
        }
        feedback.setApplyType(ApplyType.AGENT_PRODUCT_FEEDBACK);
        feedback.setUserPlatform(SystemPlatformType.AGENT);
        feedback.setStatus(ApplyStatus.PENDING);
        feedback.setFeedbackStatus(AgentProductFeedbackStatus.SO_PENDING);
        Long id = agentProductFeedbackServiceClient.saveAgentProductFeedback(feedback);
        if (id == null) {
            return MapMessage.errorMessage("保存反馈信息失败");
        }
        msg = saveWorkFlowRecord(feedback);
        Long workflowId = fetchWorkflowId(msg);
        if (workflowId != null) {
            agentProductFeedbackServiceClient.updateWorkFlowId(id, workflowId);
        }
        return MapMessage.successMessage().add("id", id);
    }

    private Long fetchWorkflowId(MapMessage mapMessage) {
        if (!mapMessage.isSuccess()) {
            return null;
        }
        WorkFlowRecord workFlowRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");
        if (workFlowRecord == null) {
            return null;
        }
        return workFlowRecord.getId();
    }

    private MapMessage saveWorkFlowRecord(AgentProductFeedback feedback) {
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setStatus("init");
        workFlowRecord.setSourceApp("agent");
        workFlowRecord.setTaskName("产品反馈");
        workFlowRecord.setTaskContent(feedback.generateSummary());
        workFlowRecord.setLatestProcessorName(feedback.getAccountName());
        workFlowRecord.setCreatorName(feedback.getAccountName());
        workFlowRecord.setCreatorAccount(feedback.getAccount());
        workFlowRecord.setWorkFlowType(WorkFlowType.AGENT_PRODUCT_FEEDBACK);
        List<WorkFlowProcessUser> processUserList = new ArrayList<>();

        if (Objects.equals(AgentProductFeedbackSubject.JUNIOR_MATH, feedback.getTeacherSubject())) {
            WorkFlowProcessUser processUser4 = new WorkFlowProcessUser();
            processUser4.setUserPlatform("admin");
            processUser4.setAccount("jingxiao.duan");
            processUser4.setAccountName("段景孝");
            processUserList.add(processUser4);
        } else if (Objects.equals(AgentProductFeedbackSubject.JUNIOR_ENGLISH, feedback.getTeacherSubject())) {
            WorkFlowProcessUser processUser5 = new WorkFlowProcessUser();
            processUser5.setUserPlatform("admin");
            processUser5.setAccount("baochang.yang");
            processUser5.setAccountName("杨宝长");
            processUserList.add(processUser5);
        } else if (Objects.equals(AgentProductFeedbackSubject.MIDDLE_MATH, feedback.getTeacherSubject())
                || Objects.equals(AgentProductFeedbackSubject.HIGH_MATH, feedback.getTeacherSubject())
                || Objects.equals(AgentProductFeedbackSubject.MIDDLE_HIGH_OTHERS, feedback.getTeacherSubject())){
            WorkFlowProcessUser processUser6 = new WorkFlowProcessUser();
            processUser6.setUserPlatform("admin");
            processUser6.setAccount("shen.yang");
            processUser6.setAccountName("杨屾");
            processUserList.add(processUser6);
            WorkFlowProcessUser processUser7 = new WorkFlowProcessUser();
            processUser7.setUserPlatform("admin");
            processUser7.setAccount("ying.liu");
            processUser7.setAccountName("刘莹");
            processUserList.add(processUser7);
            WorkFlowProcessUser processUser8 = new WorkFlowProcessUser();
            processUser8.setUserPlatform("admin");
            processUser8.setAccount("na.li.b");
            processUser8.setAccountName("李娜");
            processUserList.add(processUser8);
        }
        return workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord, processUserList.toArray(new WorkFlowProcessUser[processUserList.size()]));
    }

    private MapMessage checkProductFeedback(AgentProductFeedback feedback) {
        if (feedback.getFeedbackType() == null) {
            return MapMessage.errorMessage("请选择反馈类别!");
        }
        if (feedback.getTeacherId() == null && !SafeConverter.toBoolean(feedback.getMySelf())) {
            return MapMessage.errorMessage("请选择反馈老师!");
        }
        if (feedback.getNoticeFlag() == null && !SafeConverter.toBoolean(feedback.getMySelf())) {
            return MapMessage.errorMessage("请选择是否发送感谢老师!");
        }
        if (StringUtils.isBlank(feedback.getContent())) {
            return MapMessage.errorMessage("请填写反馈内容!");
        }
        if (feedback.getContent().length() > 200) {
            return MapMessage.errorMessage("反馈内容请限制在200字以内!");
        }
        if (AgentProductFeedbackType.isClazz(2, feedback.getFeedbackType()) || AgentProductFeedbackType.isClazz(3, feedback.getFeedbackType())) {
            if (StringUtils.isBlank(feedback.getBookName())) {
                return MapMessage.errorMessage("请填写教材名称!");
            }
            if (feedback.getBookName().length() >= 20) {
                return MapMessage.errorMessage("教材名称过长!");
            }
            if (StringUtils.isBlank(feedback.getBookGrade())) {
                return MapMessage.errorMessage("请填写年级信息!");
            }
            if (feedback.getBookGrade().length() >= 20) {
                return MapMessage.errorMessage("年级信息过长!");
            }
        }
        if (AgentProductFeedbackType.isClazz(2, feedback.getFeedbackType())) {
            if (StringUtils.isBlank(feedback.getBookUnit())) {
                return MapMessage.errorMessage("清填写单元信息!");
            }
            if (feedback.getBookUnit().length() >= 20) {
                return MapMessage.errorMessage("年级信息过长!");
            }
        }
        if (AgentProductFeedbackType.isClazz(3, feedback.getFeedbackType())) {
            if (StringUtils.isBlank(feedback.getBookCoveredArea())) {
                return MapMessage.errorMessage("请填写覆盖地区!");
            }
            if (feedback.getBookCoveredArea().length() >= 20) {
                return MapMessage.errorMessage("覆盖地区名过长!");
            }
            if (SafeConverter.toInt(feedback.getBookCoveredStudentCount()) < 0) {
                return MapMessage.errorMessage("覆盖学生数必须大于0!");
            }
            if (SafeConverter.toInt(feedback.getBookCoveredStudentCount()) > 999999) {
                return MapMessage.errorMessage("覆盖学生数必须小于999999!");
            }
        }
        return MapMessage.successMessage();
    }

    // 产品反馈;
    private ProductFeedbackInfo createProductFeedbackInfo(ApplyWithProcessResultData processResult) {
        if (processResult == null) {
            return null;
        }
        //此处因为知道类型所以直接转换了
        AgentProductFeedback feedback = (AgentProductFeedback) processResult.getApply();
        ProductFeedbackInfo result = new ProductFeedbackInfo();
        result.setId(feedback.getId());
        result.setTeacherName(feedback.getTeacherName());
        result.setTeacherId(feedback.getTeacherId());
        result.setCreateTime(feedback.getCreateDatetime());
        result.setFeedbackType(feedback.getFeedbackType());
        result.setFbContent(feedback.getContent());
        result.setFeedbackStatus(feedback.getFeedbackStatus());
        result.setStatusDesc(feedback.getFeedbackStatus() == null ? "新提交" : feedback.getFeedbackStatus().getDesc());
        result.setOnlineFlag(feedback.getOnlineFlag());
        result.setOnlineDate(feedback.getOnlineEstimateDate());
        List<ApplyProcessResult> applyProcessResults = processResult.getProcessResultList();
        if (CollectionUtils.isNotEmpty(applyProcessResults)) {
            result.setProcessResultList(applyProcessResults);
//            for (ApplyProcessResult applyProcessResult : applyProcessResults) {
//                if (Objects.equals(applyProcessResult.getAccount(), feedback.getPmAccount())) {
//                    result.setPmOpinion(applyProcessResult.getProcessNotes());
//                    result.setPmName(applyProcessResult.getAccountName());
//                } else {
//                    result.setMarketOpinion(applyProcessResult.getProcessNotes());
//                    result.setMarketName(applyProcessResult.getAccountName());
//                }
//            }
        }
        // fixme 销运意见 需求意见 销运姓名
        return result;
    }

}
