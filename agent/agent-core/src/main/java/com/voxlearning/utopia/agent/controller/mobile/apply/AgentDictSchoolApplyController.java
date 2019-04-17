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

package com.voxlearning.utopia.agent.controller.mobile.apply;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.apply.DictSchoolEditParams;
import com.voxlearning.utopia.agent.bean.apply.DictSchoolNavigationVo;
import com.voxlearning.utopia.agent.bean.apply.DictSchoolVo;
import com.voxlearning.utopia.agent.bean.apply.ResponsibleListVo;
import com.voxlearning.utopia.agent.constants.AgentDateConfigType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentDateConfig;
import com.voxlearning.utopia.agent.service.apply.AgentDictSchoolApplyService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDateConfigService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.view.school.SchoolBasicExtData;
import com.voxlearning.utopia.agent.view.school.SchoolGradeBasicData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentModifyDictSchoolApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


/**
 *
 */
@Controller
@RequestMapping("/mobile/dict_school_apply")
@Slf4j
public class AgentDictSchoolApplyController extends AbstractAgentController {


    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentDictSchoolApplyService agentDictSchoolApplyService;
    @Inject private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject private SchoolResourceService schoolResourceService;
    @Inject private AgentDateConfigService agentDateConfigService;
    @Inject private AgentModifyDictSchoolApplyLoaderClient AgentModifyDictSchoolApplyLoaderClient;
    @Inject private AgentGroupSupport agentGroupSupport;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;


    @RequestMapping(value = "dict_school.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDictSchool() {
        Long schoolId = requestLong("schoolId");

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("输入的学校不存在！");
        }

        //公私海场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限",mapMessage.get("schoolManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }

        List<SchoolGradeBasicData> gradeDataList = schoolResourceService.generateGradeBasicDataList(schoolId);
        SchoolBasicExtData schoolBasicExtData = schoolResourceService.generateSchoolBasicExtData(schoolId);
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolBySchool(schoolId);
        AgentUserSchool userSchool = Optional.ofNullable(userSchools).orElse(new ArrayList<>()).stream().findAny().orElse(new AgentUserSchool());

        DictSchoolVo dictSchoolVo = DictSchoolVo.builder()
                .englishStartGrade(schoolBasicExtData.getEnglishStartGrade())
                .gradeDataList(DictSchoolEditParams.GradeData.Builder.build(gradeDataList))
                .responsibleId(userSchool.getUserId())
                .responsibleName(Objects.isNull(userSchool) ? "":Optional.ofNullable(baseOrgService.getUser(userSchool.getUserId())).orElse(new AgentUser()).getRealName())
                .schoolPopularity(Objects.isNull(agentDictSchool)||Objects.isNull(agentDictSchool.getSchoolPopularity()) ? null:agentDictSchool.getSchoolPopularity().getLevel())
                .build();

        return mapMessage
                .add("englishStartGrade", dictSchoolVo.getEnglishStartGrade())
                .add("gradeDataList", dictSchoolVo.getGradeDataList())
                .add("responsibleId", dictSchoolVo.getResponsibleId())
                .add("responsibleName", dictSchoolVo.getResponsibleName())
                .add("schoolPopularity", dictSchoolVo.getSchoolPopularity())
                .add("schoolPopularityList", AgentSchoolPopularityType.viewSchoolPopularity());
    }

    @RequestMapping(value = "edit_dict_schoolGet.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editDictSchoolGet() {
        DictSchoolEditParams params = new DictSchoolEditParams();
        params.setSchoolId(419522L);
        params.setResponsibleId(getCurrentUser().getUserId());
        params.setComment("testget");
        params.setGradeDataList(new ArrayList<>());
        return editDictSchool(params);
    }

    @RequestMapping(value = "edit_dict_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editDictSchool(@RequestBody DictSchoolEditParams params) {
        AuthCurrentUser currentUser = getCurrentUser();

        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());

        if (StringUtils.isBlank(params.getComment())) {
            return MapMessage.errorMessage("未填写申请原因，请重新填写！");
        }

        AgentSchoolPopularityType schoolPopularity = AgentSchoolPopularityType.of(params.getSchoolPopularity());

        Long schoolId = Optional.ofNullable(params.getSchoolId()).orElse(0L);

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("输入的学校不存在！");
        }

        //公私海场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限",mapMessage.get("schoolManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }

        // 指定分配给的专员
        Long targetUserId;
        if (AgentRoleType.BusinessDeveloper == userRole) {
            targetUserId = currentUser.getUserId();
        } else {
            targetUserId = params.getResponsibleId();
        }

        //修改学校信息
        MapMessage message = agentDictSchoolApplyService.checkAndUpdateSchoolExtInfo(schoolId, params);
        if (!message.isSuccess()) {
            return message;
        }

        //先修改学校信息再校验
        MapMessage checkMessage = agentDictSchoolApplyService.checkMobileEditDictSchool(currentUser.getUserId(), school, schoolPopularity);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }

        return agentDictSchoolApplyService.saveModifyDicSchoolApply(AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType(), school, params.getComment(), schoolPopularity, targetUserId);
    }

    @RequestMapping(value = "remove_dict_schoolGet.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage removeDictSchoolGet() {
        return removeDictSchool();
    }

    @RequestMapping(value = "remove_dict_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeDictSchool() {
        AuthCurrentUser currentUser = getCurrentUser();

        Long schoolId = requestLong("schoolId");
        String comment = requestString("comment");

        if (StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("未填写申请原因，请重新填写！");
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("输入的学校不存在！");
        }

        //公私海场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限",mapMessage.get("schoolManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }

        MapMessage checkMessage = agentDictSchoolApplyService.checkMobileRemoveDictSchool(currentUser.getUserId(), school.getId());
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }

        return agentDictSchoolApplyService.saveModifyDicSchoolApply(AgentModifyDictSchoolApply.ModifyType.DELETE_SCHOOL.getType(), school, comment, null, null);
    }

    @RequestMapping(value = "edit_dict_school_popularity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editDictSchoolPopularity() {
        AuthCurrentUser currentUser = getCurrentUser();

        Long schoolId = requestLong("schoolId");
        String comment = requestString("comment");
        String schoolPopularityStr = requestString("schoolPopularity");

        if (StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("未填写申请原因，请重新填写！");
        }

        AgentSchoolPopularityType schoolPopularity = AgentSchoolPopularityType.of(schoolPopularityStr);

        if (Objects.isNull(schoolPopularity)) {
            return MapMessage.errorMessage("学校等级信息有误，请重新选择！");
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("输入的学校不存在！");
        }

        //公私海场景，判断该用户是否有权限操作，若无权限，返回学校负责人员
        MapMessage mapMessage = schoolResourceService.schoolAuthorityMessage(getCurrentUserId(), schoolId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("schoolManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该学校由{}负责，暂无操作权限",mapMessage.get("schoolManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }

        MapMessage checkMessage = agentDictSchoolApplyService.checkMobileEditDictSchoolPopularity(school, schoolPopularity);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }

        return agentDictSchoolApplyService.saveModifyDicSchoolApply(AgentModifyDictSchoolApply.ModifyType.UPDATE_POPULARITY.getType(), school, comment, schoolPopularity, null);
    }

    @RequestMapping(value = "edit_dict_school_responsibleGet.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editDictSchoolResponsibleGet() {
        return editDictSchoolResponsible();
    }

    @RequestMapping(value = "edit_dict_school_responsible.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editDictSchoolResponsible() {
        AuthCurrentUser currentUser = getCurrentUser();

        if (!agentDictSchoolApplyService.isManagent(currentUser.getRoleList())) {
            return MapMessage.errorMessage("您无权创建该类申请！");
        }

        Long schoolId = requestLong("schoolId");
        Long responsibleId = requestLong("responsibleId");

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("输入的学校不存在！");
        }

        MapMessage checkMessage = agentDictSchoolApplyService.checkMobileEditDictSchoolResponsible(currentUser.getUserId(), school, responsibleId);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }

        return agentDictSchoolApplyService.saveModifyDicSchoolApply(AgentModifyDictSchoolApply.ModifyType.UPDATE_RESPONSIBLE.getType(), school, null, null, responsibleId);
    }

    @RequestMapping(value = "responsible_list.vpage")
    @ResponseBody
    public MapMessage responsibleList() {
        AuthCurrentUser currentUser = getCurrentUser();
        Long schoolId = requestLong("schoolId");

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        List<Long> schoolGroupIds = agentGroupSupport.getGroupIdsByRegionCodeAndSchoolLevels(school.getRegionCode(), Collections.singleton(school.getLevel()));

        List<Long> groupIdList = baseOrgService.getManagedGroupIdListByUserId(currentUser.getUserId());
        List<Long> subGroupIdList = new ArrayList<>();
        subGroupIdList.addAll(groupIdList);
        groupIdList.forEach(groupId -> {
            List<AgentGroup> agentGroups = baseOrgService.getSubGroupList(groupId);
            List<Long> temp = Optional.ofNullable(agentGroups).orElse(new ArrayList<>())
                    .stream()
                    .map(AgentGroup::getId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(temp)) {
                subGroupIdList.addAll(temp);
            }
        });
        subGroupIdList.retainAll(schoolGroupIds);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return MapMessage.errorMessage("该学校不在您管理范围，您无权创建该类申请！");
        }
        List<Long> userIdList = baseOrgService.getUserByGroupIdsAndRole(subGroupIdList, AgentRoleType.BusinessDeveloper);
        List<AgentUser> userList = userIdList.stream().map(agentUserLoaderClient::load).filter(p -> p != null && p.isValidUser()).collect(Collectors.toList());

        List<ResponsibleListVo> bdUserList = Optional.ofNullable(userList).orElse(new ArrayList<>())
                .stream()
                .map(user -> {
                    ResponsibleListVo vo = ResponsibleListVo.builder().build();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                }).collect(Collectors.toList());

        return MapMessage.successMessage().add("bdUserList", bdUserList);
    }

    @RequestMapping(value = "dict_school_navigation.vpage")
    @ResponseBody
    public MapMessage dictSchoolNavigation() {
        AuthCurrentUser currentUser = getCurrentUser();
        Long schoolId = requestLong("schoolId");

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("输入的学校不存在！");
        }

        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        List<AgentModifyDictSchoolApply> applies = AgentModifyDictSchoolApplyLoaderClient.findBySchoolId(schoolId);
        AgentDateConfig cityManagerConfig = agentDateConfigService.findDateConfigByType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);

        DictSchoolNavigationVo vo = DictSchoolNavigationVo.builder()
                .dictSchoolFlag(Objects.nonNull(agentDictSchool))
                .juniorOrHighSchoolFlag(school.isJuniorSchool() || school.isSeniorSchool())
                .managentFlag(agentDictSchoolApplyService.isManagent(currentUser.getRoleList()))
                .cityManagerOrBusinessDeveloperFlag(isCityManagerOrBusinessDeveloper(currentUser.getRoleList()))
                .limitChangeResponsibleTimeFlag(this.isLimitChangeResponsibleTime())
                .changeResponsibleOpentime(cityManagerConfig.getStartDay() + "-" + cityManagerConfig.getEndDay())
                .inAuditFlag( Optional.ofNullable(applies).orElse(Collections.emptyList())
                        .stream()
                        .anyMatch(apply -> !apply.getResolved()))
                .build();

        return MapMessage.successMessage()
                .add("dictSchoolFlag", vo.getDictSchoolFlag())
                .add("juniorOrHighSchoolFlag", vo.getJuniorOrHighSchoolFlag())
                .add("limitChangeResponsibleTimeFlag", vo.getLimitChangeResponsibleTimeFlag())
                .add("changeResponsibleOpenTime", vo.getChangeResponsibleOpentime())
                .add("cityManagerOrBusinessDeveloperFlag", vo.getCityManagerOrBusinessDeveloperFlag())
                .add("managentFlag", vo.getManagentFlag())
                .add("inAuditFlag", vo.getInAuditFlag());
    }

    Boolean isLimitChangeResponsibleTime() {
        Integer nowDay = DayUtils.getDay(new Date());
        AgentDateConfig cityManagerConfig = agentDateConfigService.findDateConfigByType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);

        return nowDay < cityManagerConfig.getStartDay() || nowDay > cityManagerConfig.getEndDay();
    }

    Boolean isCityManagerOrBusinessDeveloper (List<Integer> roles) {
        return roles.contains(AgentRoleType.BusinessDeveloper.getId())
                || roles.contains(AgentRoleType.CityManager.getId());
    }
}
