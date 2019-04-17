/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.TeacherAgentLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Maofeng Lu
 * @since 14-7-21 下午4:19
 * FIXME 如果现在已经有CRM-Service了，实际上这里的方法都可以想办法迁移一下
 */
@Controller
@RequestMapping("/crm")
@Slf4j
public class CrmController extends AbstractAgentController {

    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private BaseOrgService baseOrgService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherAgentLoaderClient teacherAgentLoaderClient;

    /**
     * 合并学校之前，判断学校是不是在字典表里
     * 1. 学校字典表，若学校ID在字典表里，则返回false
     * 2. 被关联的其他表
     * 2.1 AGENT_GROUP_SCHOOL
     * 3. 两个学校都在同一个代理名下的时候允许合并 (added 2016-05-09 By wyc)
     * ---- Update By Wyc 2016-08-08 ----
     * 1. 更新字典表
     * 2. 判断是否在同一代理用户名下逻辑修改
     * 3. 不再使用 AGENT_GROUP_SCHOOL 表
     */
    @RequestMapping(value = "validateschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateSchoolBeforeMerge() {
        Long sourceSchoolId = getRequestLong("sourceSchoolId");
        Long targetSchoolId = getRequestLong("targetSchoolId", -1L);
        try {
            if (sourceSchoolId == 0L) {
                return MapMessage.errorMessage("参数异常！");
            }
            List<AgentDictSchool> existDict = agentDictSchoolService.loadSchoolDictDataBySchool(Collections.singleton(sourceSchoolId));
            // 学校在字典表内无论如何不允许合并
            if (CollectionUtils.isNotEmpty(existDict)) {
                return MapMessage.errorMessage("该学校在学校字典表内！");
            }
            // 如果第二个参数不为 -1 表明是合并学校的操作，先查看这两个学校是不是在一个同一个代理的名下 Bug #23239
            // 如果是，则允许合并; 不是的话再校验源学校是不是字典表学校 (no more need)
            if (targetSchoolId != -1L && schoolInSameAgentGroup(sourceSchoolId, targetSchoolId)) {
                return MapMessage.successMessage("合并学校在同一代理名下");
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("check school dict failed, source:{}, target:{}, reason:{}", sourceSchoolId, targetSchoolId, ex.getMessage(), ex);
            return MapMessage.errorMessage("验证学校信息失败!");
        }
    }

    private boolean schoolInSameAgentGroup(Long sourceSchoolId, Long targetSchoolId) {
        List<AgentUser> sourceManagerList = baseOrgService.getSchoolManager(sourceSchoolId);
        List<AgentUser> targetManagerList = baseOrgService.getSchoolManager(targetSchoolId);
        // 有一个学校不属于任何用户的时候，返回false，走常规校验
        if (CollectionUtils.isEmpty(sourceManagerList) || CollectionUtils.isEmpty(targetManagerList)) {
            return false;
        }
        // 判断这个两个学校是不是同一个代理负责的
        boolean inSameAgent = sourceManagerList.get(0).getId().equals(targetManagerList.get(0).getId());
        if (inSameAgent) {
            inSameAgent = baseOrgService.getGroupUserRoleMapByUserId(sourceManagerList.get(0).getId())
                    .values().stream()
                    .anyMatch(r -> AgentRoleType.CityAgent.getId().equals(r));
        }
        return inSameAgent;
    }

    /**
     * 判断学校是否在字典表中
     * FIXME 已经有 AgentOrgLoader.isDictSchool 可以用，还是不要用HTTP调用了
     */
    @RequestMapping(value = "isdictschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage isDictSchool() {
        String schoolIdJson = getRequestString("schoolIds");
        if (StringUtils.isBlank(schoolIdJson)) {
            return MapMessage.errorMessage("参数异常");
        }
        List<String> strSchoolIds = JsonUtils.fromJsonToList(schoolIdJson, String.class);
        if (CollectionUtils.isEmpty(strSchoolIds)) {
            return MapMessage.errorMessage("参数异常");
        }
        List<Long> schoolIds = strSchoolIds.stream().map(SafeConverter::toLong).filter(p -> p != 0L).collect(Collectors.toList());
        try {
            List<AgentDictSchool> dictSchools = agentDictSchoolService.loadSchoolDictDataBySchool(schoolIds);
            List<String> dictSchoolList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(dictSchools)) {
                for (AgentDictSchool agentDictSchool : dictSchools) {
                    dictSchoolList.add(agentDictSchool.getSchoolId().toString());
                }
            }
            return MapMessage.successMessage().add("dictSchoolList", dictSchoolList);
        } catch (Exception ex) {
            log.error("check school dict failed, schoolId:{}, reason:{}", schoolIds, ex.getMessage(), ex);
            return MapMessage.errorMessage("验证学校信息失败!");
        }
    }

    // 根据老师ID获取负责市场专员的信息
    @RequestMapping(value = "hasbusinessdeveloper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAgentUser() {
        try {
            Long teacherId = getRequestLong("teacherId");
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacherDetail == null) {
                return MapMessage.errorMessage("Unknown Teacher");
            }
            List<AgentUser> userList = baseOrgService.getSchoolManager(teacherDetail.getTeacherSchoolId());
            AgentUser targetUser = null;
            if (CollectionUtils.isNotEmpty(userList)) {
                for (AgentUser user : userList) {
                    AgentRoleType userRole = baseOrgService.getUserRole(user.getId());
                    if (AgentRoleType.BusinessDeveloper == userRole) {
                        targetUser = user;
                        break;
                    }
                }
            }
            if (targetUser == null) {
                return MapMessage.errorMessage();
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("Failed to find teacher agent", ex);
            return MapMessage.errorMessage("Internal Error!");
        }
    }

    // 根据老师ID获取负责专员的信息
    @RequestMapping(value = "getteacheragent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTeacherAgent() {
        try {
            Long teacherId = getRequestLong("teacherId");
            String token = getRequestString("token");

//            if (Objects.equals(teacherId, 0L) || StringUtils.isBlank(token)
//                    || Objects.equals(token, DesUtils.encryptHexString("_17zu#ye", String.valueOf(teacherId)))) {
            if (Objects.equals(teacherId, 0L) || StringUtils.isBlank(token)) {
                return MapMessage.errorMessage("Illegal Access");
            }

            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacherDetail == null) {
                return MapMessage.errorMessage("Unknown Teacher");
            }

            return MapMessage.successMessage().add("userAgent", teacherAgentLoaderClient.getSchoolManager(teacherDetail.getTeacherSchoolId()));

        } catch (Exception ex) {
            log.error("Failed to find teacher agent", ex);
            return MapMessage.errorMessage("Internal Error!");
        }
    }

}
