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

package com.voxlearning.utopia.agent.service.log;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentConfigSchoolLogType;
import com.voxlearning.utopia.agent.persist.AgentSchoolConfigLogPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentSchoolConfigLog;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yaguang.wang
 * on 2017/3/28.
 */
@Named
public class AgentSchoolConfigLogService extends SpringContainerSupport {
    private final static Integer CITY_MANAGER_CONFIG_SCHOOL_DAY = -1;

    @Inject private AgentSchoolConfigLogPersistence agentSchoolConfigLogPersistence;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private BaseOrgService baseOrgService;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;

    public List<AgentSchoolConfigLog> findSchoolConfigLogByDay(Date startDate, Date endDate) {
        return agentSchoolConfigLogPersistence.findSchoolConfigLogByTime(startDate, endDate);
    }

    public boolean checkCityManagerAddSchool(Long schoolId) {
        AgentSchoolConfigLog log = filterCityManagerLog(schoolId);
        return log != null && (DateUtils.dayDiff(new Date(), log.getOperatingTime()) < CITY_MANAGER_CONFIG_SCHOOL_DAY);
    }

    public AgentSchoolConfigLog filterCityManagerLog(Long schoolId) {
        List<AgentSchoolConfigLog> logs = findSchoolConfigLogs(schoolId, AgentConfigSchoolLogType.ADD.getId());
        AgentSchoolConfigLog result = null;
        for (AgentSchoolConfigLog log : logs) {
            Long newResponsibleId = log.getNewResponsibleId();
            AgentRoleType role = baseOrgService.getUserRole(newResponsibleId);
            if (AgentRoleType.BusinessDeveloper == role) {
                result = log;
                break;
            }
        }
        return result;
    }

    public List<AgentSchoolConfigLog> findSchoolConfigLogs(Long schoolId, Integer operationType) {
        return agentSchoolConfigLogPersistence.findSchoolConfigLogs(schoolId, operationType);
    }

    public MapMessage initSchoolConfigLog(Date operatingTime, Long handlerId, AgentConfigSchoolLogType operationType, Long schoolId, Long sourceResponsibleId, Long newResponsibleId) {
        MapMessage msg = MapMessage.successMessage();
        AgentSchoolConfigLog log = new AgentSchoolConfigLog();
        msg.put("log", log);
        if (operatingTime == null) {
            return MapMessage.errorMessage("操作时间为空");
        }
        log.setOperatingTime(operatingTime);
        AgentUser handler = agentUserLoaderClient.load(handlerId);
        if (handler == null) {
            return MapMessage.errorMessage("操作人为空");
        }
        log.setHandlerId(handlerId);
        log.setHandlers(handler.getRealName());
        AgentGroup group = baseOrgService.getUserGroupsFirstOne(handlerId, null);
        if (group != null && !Objects.equals(group.getGroupName(), "市场部")) {
            AgentGroup region = baseOrgService.getGroupById(group.getParentId());
            if (region == null || Objects.equals(region.getGroupName(), "市场部")) {
                log.setRegionId(group.getId());
                log.setRegionName(group.getGroupName());
            } else {
                log.setDepartmentId(group.getId());
                log.setDepartmentName(group.getGroupName());
                log.setRegionId(region.getId());
                log.setRegionName(region.getGroupName());
            }
        }
        if (operationType == null) {
            return MapMessage.errorMessage("操作类型不能为空");
        }
        log.setOperationType(operationType.getId());
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校信息不存在");
        }
        log.setSchoolId(school.getId());
        log.setSchoolName(school.getCmainName());
        log.setLevel(school.getLevel());
        if (operationType != AgentConfigSchoolLogType.ADD) {
            AgentUser sourceResponsible = agentUserLoaderClient.load(sourceResponsibleId);
            if (sourceResponsible != null) {
                log.setSourceResponsibleId(sourceResponsible.getId());
                log.setSourceResponsible(sourceResponsible.getRealName());
            }
        }
        if (operationType != AgentConfigSchoolLogType.DELETE) {
            AgentUser newResponsible = agentUserLoaderClient.load(newResponsibleId);
            if (newResponsible == null) {
                return MapMessage.errorMessage("新负责人信息不存在");
            }
            log.setNewResponsible(newResponsible.getRealName());
            log.setNewResponsibleId(newResponsible.getId());
        }
        return msg;
    }

    public Long insertConfigSchoolLog(AgentSchoolConfigLog log) {
        agentSchoolConfigLogPersistence.insert(log);
        return log.getId();
    }


    public void addSchoolConfigLogs(Long userId, AgentConfigSchoolLogType operationType, Collection<AgentUserSchool> userSchools){
        List<AgentSchoolConfigLog> logList = generateSchoolConfigLogList(userId, operationType, userSchools);
        if(CollectionUtils.isNotEmpty(logList)){
            agentSchoolConfigLogPersistence.inserts(logList);
        }
    }


    public List<AgentSchoolConfigLog> generateSchoolConfigLogList(Long userId, AgentConfigSchoolLogType operationType, Collection<AgentUserSchool> userSchools){
        if(operationType == null || CollectionUtils.isEmpty(userSchools)){
            return Collections.emptyList();
        }

        userSchools = userSchools.stream().filter(p -> p != null && p.getUserId() != null && p.getSchoolId() != null).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(userSchools)){
            return Collections.emptyList();
        }

        AgentUser handler = agentUserLoaderClient.load(userId);
        if(handler == null){
            return Collections.emptyList();
        }
        AgentGroup group = baseOrgService.getUserGroupsFirstOne(userId, null);

        List<Long> schoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toList());
        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(schoolIds);


        List<Long> userIds = userSchools.stream().map(AgentUserSchool::getUserId).collect(Collectors.toList());
        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);



        List<AgentSchoolConfigLog> logList = new ArrayList<>();

        Date date = new Date();
        for(AgentUserSchool userSchool : userSchools){
            AgentSchoolConfigLog log = new AgentSchoolConfigLog();
            log.setOperatingTime(date);

            log.setHandlerId(userId);
            log.setHandlers(handler.getRealName());
            if(group != null){
                log.setDepartmentId(group.getId());
                log.setDepartmentName(group.getGroupName());
            }

            log.setOperationType(operationType.getId());


            log.setSchoolId(userSchool.getSchoolId());
            CrmSchoolSummary schoolSummary = schoolSummaryMap.get(userSchool.getSchoolId());
            if(schoolSummary != null){
                log.setSchoolName(schoolSummary.getSchoolName());
            }

            AgentUser user = userMap.get(userSchool.getUserId());
            if (operationType == AgentConfigSchoolLogType.ADD) {
                log.setNewResponsibleId(userSchool.getUserId());
                if(user != null){
                    log.setNewResponsible(user.getRealName());
                }
            }else if(operationType == AgentConfigSchoolLogType.DELETE){
                log.setSourceResponsibleId(userSchool.getUserId());
                if(user != null){
                    log.setSourceResponsible(user.getRealName());
                }
            }
            logList.add(log);
        }
        return logList;
    }
}
