package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.admin.viewdata.FeedbackListView;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.bean.ProductFeedbackListCondition;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrgLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentProductFeedbackLoadClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentProductFeedbackServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmProductFeedbackRecordServiceClient;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CrmProductFeedbackService
 *
 * @author song.wang
 * @date 2017/2/23
 */
@Named
public class CrmProductFeedbackService extends AbstractAdminService {

    @Inject
    protected CrmProductFeedbackRecordServiceClient crmProductFeedbackServiceClient;
    @Inject
    AgentProductFeedbackLoadClient agentProductFeedbackLoadClient;
    @Inject
    AgentProductFeedbackServiceClient agentProductFeedbackServiceClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject
    private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject
    private AgentOrgLoaderClient agentOrgLoaderClient;
    private static final Integer ID_LENGTH = 6;

    public List<AgentProductFeedback> loadTeacherFeedbackList(Long teacherId) {
        return agentProductFeedbackLoadClient.findFeedbackByTeacherId(teacherId);
    }

    public MapMessage saveNewFeedback(AgentProductFeedback feedback) {
        if (feedback == null) {
            return MapMessage.errorMessage("输入的反馈信息有误");
        }
        feedback.setApplyType(ApplyType.AGENT_PRODUCT_FEEDBACK);
        feedback.setUserPlatform(SystemPlatformType.ADMIN);
        feedback.setStatus(ApplyStatus.PENDING);
        feedback.setFeedbackStatus(AgentProductFeedbackStatus.SO_PENDING);
        feedback.setOnlineFlag(false);
        feedback.setNoticeFlag(false);
        Long id = agentProductFeedbackServiceClient.saveAgentProductFeedback(feedback);
        if (id == null) {
            return MapMessage.errorMessage("保存反馈信息失败");
        }
        MapMessage msg = saveWorkFlowRecord(feedback);
        Long workflowId = fetchWorkflowId(msg);
        if (workflowId != null) {
            agentProductFeedbackServiceClient.updateWorkFlowId(id, workflowId);
        }
        return MapMessage.successMessage().add("id", id);
    }

    private MapMessage saveWorkFlowRecord(AgentProductFeedback feedback) {
        // TODO 动态设定审批人员
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setStatus("init");
        workFlowRecord.setSourceApp("admin");
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
        }
        return workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord, processUserList.toArray(new WorkFlowProcessUser[processUserList.size()]));
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

    public MapMessage updateFeedbackInfo(ProductFeedbackListCondition condition, AgentProductFeedback feedback) {
        feedback.setTeacherSubject(condition.getSubject());
        feedback.setFeedbackType(condition.getType());
        feedback.setFirstCategory(condition.getFirstCategory());
        feedback.setSecondCategory(condition.getSecondCategory());
        feedback.setThirdCategory(condition.getThirdCategory());

        if (!(Objects.equals(feedback.getFeedbackStatus(), AgentProductFeedbackStatus.PM_APPROVED)) && !StringUtils.isBlank(condition.getOnlineEstimateDate()) && !Objects.equals(condition.getOnlineEstimateDate(), feedback.getOnlineEstimateDate())) {
            return MapMessage.errorMessage("只有状态为“PM已采纳”的反馈，才可以修改“预计上线日期”");
        }
        if (StringUtils.isNoneBlank(condition.getOnlineEstimateDate())) {
            feedback.setOnlineEstimateDate(condition.getOnlineEstimateDate());
        }
        agentProductFeedbackServiceClient.replaceAgentProductFeedback(feedback);
        return MapMessage.successMessage();
    }

    public List<FeedbackListView> loadFeedbackListByCondition(ProductFeedbackListCondition condition, String adminUserName) {
        MapMessage msg = loadFeedbackListByCondition(condition, adminUserName, null, null);
        if (msg.isSuccess()) {
            return (List<FeedbackListView>) msg.get("dataList");
        }
        return Collections.emptyList();
    }

    public MapMessage loadFeedbackListByCondition(ProductFeedbackListCondition condition, String adminUserName, Integer page, Integer pageSize) {
        if (condition == null) {
            return MapMessage.errorMessage("条件错误");
        }
        List<AgentProductFeedback> result = new ArrayList<>();
        // 如果输入了ID就不按照其他的条件查询了
        Long size = 1L;
        if (condition.getId() != 0L) {
            AgentProductFeedback productFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(condition.getId());
            if (productFeedback != null) {
                result.add(productFeedback);
            }
        } else {
            // 输入了老师的信息，要新找到老师的信息，在去查询
            List<Long> targetTeacherIdList = new ArrayList<>();
            if (StringUtils.isNotBlank(condition.getTeacher())) {
                Long teacherId = SafeConverter.toLong(condition.getTeacher());
                if (teacherId == 0L) {
                    condition.setTeacherName(condition.getTeacher());
                } else {
                    if (MobileRule.isMobile(condition.getTeacher())) {
                        List<UserAuthentication> userAuthenticationList = userLoaderClient.loadMobileAuthentications(condition.getTeacher());
                        Set<Long> userIdList = userAuthenticationList.stream().map(UserAuthentication::getId).collect(Collectors.toSet());
                        if (CollectionUtils.isNotEmpty(userIdList)) {
                            targetTeacherIdList.addAll(userIdList);
                        }
                        condition.setTeacher(null);
                    }
                    targetTeacherIdList.add(SafeConverter.toLong(condition.getTeacher()));
                }
            }
            condition.setTeacherIds(targetTeacherIdList);
            result = agentProductFeedbackLoadClient.findFeedbackByCondition(condition, page, pageSize);
            size = agentProductFeedbackLoadClient.findFeedbackByConditionCount(condition);
        }
        Set<Long> workFlowIds  = result.stream().map(AgentProductFeedback::getWorkflowId).collect(Collectors.toSet());
        Map<Long, List<WorkFlowProcessHistory>> histories = workFlowLoaderClient.loadWorkFlowProcessHistoriesByWorkFlowId(workFlowIds);
        List<FeedbackListView> feedbackListViews = result.stream().map(p -> createFeedbackView(p, adminUserName, histories)).collect(Collectors.toList());
        MapMessage msg = MapMessage.successMessage();
        msg.add("dataList", feedbackListViews);
        msg.add("size", size);
        return msg;
    }

    private FeedbackListView createFeedbackView(AgentProductFeedback feedback, String adminUserName, Map<Long, List<WorkFlowProcessHistory>> histories) {
        FeedbackListView result = new FeedbackListView();
        String am = sensitiveUserDataServiceClient.showUserMobile(feedback.getTeacherId(), "admin" + adminUserName + "产品反馈查看老师电话", SafeConverter.toString(feedback.getTeacherId()));
        if (am != null) {
            result.setTeacherTelephone(am);
        }
        Integer idLength = SafeConverter.toString(feedback.getId()).length();
        String id = SafeConverter.toString(feedback.getId());
        for (Integer i = idLength; i < ID_LENGTH; i++) {
            id = "0" + id;
        }
        result.setId(id);
        result.setFeedbackDate(DateUtils.dateToString(feedback.getCreateDatetime(), DateUtils.FORMAT_SQL_DATE));
        result.setFeedbackPeople(feedback.getAccountName());
        result.setFeedbackPeopleId(feedback.getAccount());
        result.setSubject(feedback.getTeacherSubject() == null ? "" : feedback.getTeacherSubject().getDesc());
        result.setType(feedback.getFeedbackType().getDesc());
        result.setContent(feedback.getContent());
        result.setTeacherId(feedback.getTeacherId());
        result.setTeacherName(feedback.getTeacherName());
        //result.setTeacherTelephone();
        result.setStatus(feedback.getFeedbackStatus() == null ? "" : feedback.getFeedbackStatus().getDesc());
       /* result.setThreeCategory(StringUtils.formatMessage("{}/{}/{}", feedback.getFirstCategory(),
                feedback.getSecondCategory(), feedback.getThirdCategory()));*/
        result.setPmData(feedback.getPmAccountName());
        result.setOnlineData(feedback.getOnlineEstimateDate());
        result.setContent(feedback.getContent());
        result.setOnline(feedback.getOnlineFlag());
        result.setCallback(feedback.getCallback());
        result.setPic1Url(feedback.getPic1Url());
        result.setPic2Url(feedback.getPic2Url());
        result.setPic3Url(feedback.getPic3Url());
        result.setPic4Url(feedback.getPic4Url());
        result.setPic5Url(feedback.getPic5Url());
        result.setWorkflowId(feedback.getWorkflowId());

        result.setUserPlatform(feedback.getUserPlatform().getDesc());
        result.setFirstCategory(feedback.getFirstCategory() != null ? String.valueOf(feedback.getFirstCategory()) : null);
        result.setSecondCategory(feedback.getSecondCategory() != null ? String.valueOf(feedback.getSecondCategory()) : null);
        result.setThirdCategory(feedback.getThirdCategory() != null ? String.valueOf(feedback.getThirdCategory()) : null);
        if(feedback.getUserPlatform() == SystemPlatformType.AGENT) {
            AgentGroup agentGroup = agentOrgLoaderClient.loadAgentGroupByUserId(SafeConverter.toLong(feedback.getAccount()));
            if (agentGroup != null) {
                result.setCityName(agentGroup.getGroupName());
                AgentGroup parentGroup = agentGroupLoaderClient.load(agentGroup.getParentId());
                if (parentGroup != null) {
                    result.setRegionName(parentGroup.getGroupName());
                }
            }
        }
        if (MapUtils.isNotEmpty(histories) && histories.containsKey(feedback.getWorkflowId())) {
            List<WorkFlowProcessHistory> processHistories = histories.get(feedback.getWorkflowId());
            if (CollectionUtils.isNotEmpty(processHistories)) {
                processHistories.sort(Comparator.comparing(AbstractDatabaseEntity::getCreateDatetime));
                for (WorkFlowProcessHistory process : processHistories) {
                    if (Objects.equals(feedback.getPmAccount(), process.getProcessorAccount())) {
                        result.setPmOpinion(process.getProcessNotes());
                    } else {
                        result.setSoOpinion(process.getProcessNotes());
                    }
                }
            }
        }
        result.setBookName(feedback.getBookName());
        result.setBookGrade(feedback.getBookGrade());
        result.setBookUnit(feedback.getBookUnit());
        result.setBookCoveredArea(feedback.getBookCoveredArea());
        result.setBookCoveredStudentCount(feedback.getBookCoveredStudentCount());

        if(feedback.getRelationCode() != null){
            List<AgentProductFeedback> relationFeedbackList = agentProductFeedbackLoadClient.findByRelationCode(feedback.getRelationCode());
            if(CollectionUtils.isNotEmpty(relationFeedbackList)){
                List<Long> relationIds = relationFeedbackList.stream().map(AgentProductFeedback::getId).filter(p -> !Objects.equals(p, feedback.getId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(relationIds)){
                    result.getRelationIds().addAll(relationIds);
                }
            }
        }
        return result;
    }
}
