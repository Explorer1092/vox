/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.service.apply;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.TreeNode;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.utils.NodeStructureUtil;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.DataReportApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.DataReportApplyServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song.wang
 * @date 2017/6/7
 */
@Named
public class AgentDataReportApplyService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private DataReportApplyServiceClient dataReportApplyServiceClient;
    @Inject
    private DataReportApplyLoaderClient dataReportApplyLoaderClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentRegionService agentRegionService;
    @Inject
    private WorkFlowDataServiceClient workFlowDataServiceClient;

    public List<TreeNode> generateCategory(AuthCurrentUser user) {
        List<Long> groupIds = baseOrgService.getGroupIdListByUserId(user.getUserId());
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        Set<Integer> regionSet = baseOrgService.getGroupRegionsByGroupSet(groupIds).stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
        List<Integer> countyCodes = agentRegionService.getCountyCodes(regionSet);
        Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions(countyCodes);
        List<TreeNode> nodeList = new ArrayList<>();
        if (user.isBusinessDeveloper()) {
            regionMap.values().forEach(p -> {
                TreeNode node = new TreeNode();
                node.setId(String.valueOf(p.getId()));
                node.setPid("0");
                node.setName(p.getName());
                nodeList.add(node);
            });
        } else if (user.isRegionManager() || user.isCityManager()) {
            Set<Integer> cityCode = new HashSet<>();
            regionMap.values().forEach(p -> {
                TreeNode node = new TreeNode();
                node.setId(String.valueOf(p.getId()));
                node.setPid(String.valueOf(p.getCityCode()));
                node.setName(p.getName());
                nodeList.add(node);

                if (!cityCode.contains(p.getCityCode())) {
                    cityCode.add(p.getCityCode());
                    TreeNode cityNode = new TreeNode();
                    cityNode.setId(String.valueOf(p.getCityCode()));
                    cityNode.setPid("0");
                    cityNode.setName(p.getCityName());
                    nodeList.add(cityNode);
                    cityCode.add(p.getCityCode());
                }
            });
        }

        return NodeStructureUtil.generateNodeTree(nodeList, "0");

    }

    public MapMessage addDataReportApply(Integer subject, Integer reportLevel, Integer cityCode, Integer countyCode, Long schoolId,
                                         Integer englishStartGrade, Integer reportType, Integer reportTerm, Integer reportMonth, Long sampleSchoolId, String comment) {
        if (subject == 0) {
            return MapMessage.errorMessage("请选择学科");
        }
        if (reportLevel == 0) {
            return MapMessage.errorMessage("请选择级别");
        }
        if (reportLevel == 1 && cityCode == 0) {
            return MapMessage.errorMessage("市级级别市级区域必选");
        }
        if (reportLevel == 2 && countyCode == 0) {
            return MapMessage.errorMessage("区级级别区级区域必选");
        }
        if (reportLevel == 3 && schoolId == 0) {
            return MapMessage.errorMessage("校级级别学校必须添加");
        }
        if (reportLevel == 3 && englishStartGrade == 0) {
            return MapMessage.errorMessage("当选择学校级别时英语起始年级必选");
        }
        if (reportType == 0) {
            return MapMessage.errorMessage("时间维度必选");
        }
        if (reportType == 1 && reportTerm == 0) {
            return MapMessage.errorMessage("学期报告学期必选");
        }
        if (reportType == 2 && reportMonth == 0) {
            return MapMessage.errorMessage("月度报告月份必选");
        }
        if (reportLevel == 3 && sampleSchoolId != 0) {
            return MapMessage.errorMessage("当选择学校级别时样本校不能添加");
        }
        if (StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("申请原因不能为空");
        }
        if (comment.length() > 130) {
            comment = comment.substring(0, 130);
        }


        DataReportApply apply = new DataReportApply();
        apply.setSubject(subject);
        apply.setReportLevel(reportLevel);
        if (reportLevel == 1 || reportLevel == 2) {
            if (reportLevel == 1) { // 市级报告
                ExRegion city = raikouSystem.loadRegion(cityCode);
                if (city != null) {
                    apply.setCityCode(city.getCityCode());
                    apply.setCityName(city.getCityName());
                }
            } else { // 区级报告
                ExRegion county = raikouSystem.loadRegion(countyCode);
                if (county != null) {
                    apply.setCityCode(county.getCityCode());
                    apply.setCityName(county.getCityName());
                    apply.setCountyCode(county.getCountyCode());
                    apply.setCountyName(county.getCountyName());
                }
            }

            if (sampleSchoolId != 0) {
                School school = raikouSystem.loadSchool(sampleSchoolId);
                if (school != null) {
                    apply.setSampleSchoolId(school.getId());
                    apply.setSampleSchoolName(school.loadSchoolFullName());
                }
            }

        } else if (reportLevel == 3) { // 校级报告
            School school = raikouSystem.loadSchool(schoolId);
            if (school != null) {
                apply.setSchoolId(school.getId());
                apply.setSchoolName(school.loadSchoolFullName());
            }
            apply.setEngStartGrade(englishStartGrade);
        }

        apply.setReportType(reportType);
        if (reportType == 1) {
            apply.setReportTerm(reportTerm);
        } else {
            apply.setReportMonth(reportMonth);
        }

        apply.setComment(comment);
        apply.setApplyType(ApplyType.AGENT_DATA_REPORT_APPLY);
        apply.setUserPlatform(SystemPlatformType.AGENT);
        apply.setAccount(SafeConverter.toString(getCurrentUser().getUserId()));
        apply.setAccountName(getCurrentUser().getRealName());
        apply.setStatus(ApplyStatus.PENDING);
        Long id = dataReportApplyServiceClient.persist(apply);

        //创建工作流
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setStatus("init");
        workFlowRecord.setSourceApp("agent");
        workFlowRecord.setTaskName("大数据报告申请");
        workFlowRecord.setTaskContent(apply.generateSummary());
        AuthCurrentUser user = getCurrentUser();
        workFlowRecord.setLatestProcessorName(user.getRealName());
        workFlowRecord.setCreatorName(user.getRealName());
        workFlowRecord.setCreatorAccount(String.valueOf(user.getUserId()));
        workFlowRecord.setWorkFlowType(WorkFlowType.AGENT_DATA_REPORT_APPLY);

        AgentUser targetUser = null;
        if (user.isBusinessDeveloper()) {
            List<AgentUser> userManagerList = baseOrgService.getUserManager(user.getUserId());
            if (CollectionUtils.isNotEmpty(userManagerList)) {
                targetUser = userManagerList.get(0);
            }
        } else if (user.isCityManager() || user.isRegionManager()) {
            workFlowRecord.setStatus("lv1");
        }

        WorkFlowProcessUser processUser = null;
        if (targetUser != null) {
            processUser = new WorkFlowProcessUser();
            processUser.setUserPlatform("agent");
            processUser.setAccount(String.valueOf(targetUser.getId()));
            processUser.setAccountName(targetUser.getRealName());
        }

        MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord, processUser);
        Long workflowId = fetchWorkflowId(mapMessage);
        if (workflowId != null) {
            dataReportApplyServiceClient.updateWorkflowId(id, workflowId);
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

    public MapMessage addDocument(Long workflowId, String firstDocument, String secondDocument) {
        DataReportApply dataReportApply = dataReportApplyLoaderClient.loadByWorkflowId(workflowId);
        if (dataReportApply == null) {
            return MapMessage.errorMessage("统考申请不存在");
        }
        if (StringUtils.isBlank(firstDocument)) {
            return MapMessage.errorMessage("学期报告不能为空");
        }
        if (dataReportApply.getSampleSchoolId() == null && StringUtils.isNoneBlank(secondDocument)) {
            return MapMessage.errorMessage("样本校不存在不能上传样本校的报告");
        }
        dataReportApply.setFirstDocument(firstDocument);
        dataReportApply.setSecondDocument(secondDocument);

        dataReportApply = dataReportApplyServiceClient.update(dataReportApply);
        if (dataReportApply == null) {
            return MapMessage.errorMessage("统考申请添加文档");
        }
        return MapMessage.successMessage();
    }
}
