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

package com.voxlearning.utopia.agent.controller.mobile.my;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.EmailRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.agent.constants.Gender;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchersUpdateLog;
import com.voxlearning.utopia.agent.service.mobile.AgentResearchersService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.VisitedResearchersIntention;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 教研员
 * Created by yagaung.wang on 2016/10/19.
 */
@Controller
@RequestMapping(value = "/mobile/researchers")
public class AgentResearchersController extends AbstractAgentController {

    @Inject private AgentResearchersService agentResearchersService;
    @Inject private AgentRequestSupport agentRequestSupport;
    @Inject private AgentOuterResourceService agentOuterResourceService;

    // 教研员的新建和编辑
    @RequestMapping(value = "load_researchers.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadResearchers() {
        Long id = requestLong("id");
        if(id == null){
            return MapMessage.errorMessage("id不能为空");
        }

        if(agentOuterResourceService.isNewResource(id)){
            return agentOuterResourceService.loadOuterResourceById(id);
        }
        return agentResearchersService.loadResearchersById(id);
    }

    // 教研员详情
    @RequestMapping(value = "load_researchers_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadResearchersInfo() {
        Long id = requestLong("id");
        if(id == null){
            return MapMessage.errorMessage("id不能为空");
        }
        if(agentOuterResourceService.isNewResource(id)){
            return agentOuterResourceService.getOuterResourceInfo(id);
        }
        return agentResearchersService.getResearchersInfo(id);
    }



    // 教研员资源
    @RequestMapping(value = "load_researchers_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadResearchersList(){
        Integer regionCode = getRequestInt("regionCode");
        String name = getRequestString("name");
        Integer pageSize = getRequestInt("pageSize");
        Integer pageNo = getRequestInt("pageNo",0);
        Integer sortType = getRequestInt("sortType",1);
        if(pageSize <= 0){
            pageSize = 50;
        }
        Pageable pageable = new PageRequest(pageNo,pageSize);
        if(regionCode == null || regionCode < 1){
            MapMessage.errorMessage("地区编码不能为空");
        }
        return agentResearchersService.loadResearchersInfoForPage(getCurrentUserId(),regionCode,name,pageable,sortType);
    }

    // 资源首页搜索
    @RequestMapping(value = "search_researchers.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchResearchers(){
        String name = getRequestString("name");
        Integer pageSize = getRequestInt("pageSize");
        Integer pageNo = getRequestInt("pageNo");
        if(pageSize <= 0){
            pageSize = 50;
        }
        Pageable pageable = new PageRequest(pageNo,pageSize);
        List<Map<String,Object>> result = agentResearchersService.searchResearcherList(getCurrentUserId(),name,pageable);
        return agentOuterResourceService.pageResource(result,pageable);
    }

    // 新建或更新教研员信息
    @RequestMapping(value = "upsert_researchers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertResearchers() {

        Long id = requestLong("id");
        String name = getRequestString("name");
        Integer gender = getRequestInt("gender");
        Long organizationId = getRequestLong("organizationId");
        Integer job = getRequestInt("job");
        String department = getRequestString("department"); //部门

        String gradeStr = getRequestString("gradeList");
        Subject subject = Subject.fromSubjectId(getRequestInt("subject"));
        String specificJob = getRequestString("specificJob"); //备注

        if(organizationId == 0L){
            return MapMessage.errorMessage("工作单位不能为空");
        }
        String phone = getRequestString("phone");
        String telephone = getRequestString("telephone");
        String weChatOrQq = getRequestString("weChatOrQq");
        String email = getRequestString("email");
        String photoUrl = getRequestString("photoUrls"); //名片/照片
        List<String> photoUrls = new ArrayList<>();
        if(StringUtils.isNotBlank(photoUrl)){
            photoUrls = Arrays.asList(photoUrl.split(","));
        }
        if(StringUtils.isEmpty(phone) && StringUtils.isEmpty(telephone)){
            return MapMessage.errorMessage("手机、座机不能同时为空");
        }

        if(StringUtils.isNotBlank(telephone) && !Pattern.matches("0\\d{2,3}-?\\d{7,8}", telephone)){
            return MapMessage.errorMessage("座机号格式不正确");
        }
        if (StringUtils.isBlank(name)) {
            return MapMessage.errorMessage("请姓名");
        }
        if (Gender.typeOf(gender) == null) {
            return MapMessage.errorMessage("请选择性别");
        }
        if (StringUtils.isNotBlank(phone) && !MobileRule.isMobile(phone)) {
            return MapMessage.errorMessage("请填写正确的手机号");
        }
        if (StringUtils.isNotBlank(email) && !EmailRule.isEmail(email)) {
            return MapMessage.errorMessage("请填写正确的邮箱地址");
        }
        if (!agentResearchersService.isRepetitionPhone(getCurrentUserId(), phone, id).isSuccess()) {
            return agentResearchersService.isRepetitionPhone(getCurrentUserId(), phone, id).add("info","已有同一手机号码的教研员");
        }
        return agentOuterResourceService.upsertOuterResource(id, name, gender, phone, job, gradeStr, subject,specificJob,telephone,organizationId,department,weChatOrQq,email,photoUrls);
    }

    // 教研员拜访
    @RequestMapping(value = "visited_researchers.vpage", method = RequestMethod.GET)
    public String visitedResearchers(Model model) {
        CrmWorkRecord workRecord = getWorkRecordSession();
        //model.addAttribute("researchersList", agentResearchersService.loadResearchersInfoByUserId(getCurrentUserId()));
        model.addAttribute("researchersId", workRecord.getResearchersId());
        model.addAttribute("researcherName", workRecord.getResearchersName());
        model.addAttribute("intention", visitedIntention());
        model.addAttribute("visitedIntention", workRecord.getVisitedIntention());
        model.addAttribute("visitedPlace", workRecord.getVisitedPlace());
        model.addAttribute("visitedFlow", workRecord.getVisitedFlow());
        model.addAttribute("visitedConclusion", workRecord.getVisitedConclusion());
        model.addAttribute("date", new Date());
        return "rebuildViewDir/mobile/my/researcher_visit";
    }

    /**
     * 教研员拜访记录
     * @return
     */
    @RequestMapping(value = "researchers_visit_record_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage researchersVisitRecordList() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long researchersId = getRequestLong("researchersId");
        mapMessage.put("dataList", agentResearchersService.loadResearchersRecordsInfo(getCurrentUserId(), researchersId));
        return mapMessage;
    }

    @RequestMapping(value = "save_record_session.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecordSession() {
        CrmWorkRecord workRecord = getWorkRecordSession();
        Integer intention = getRequestInt("intention");
        String place = getRequestString("place");
        String flow = getRequestString("flow");
        String conclusion = getRequestString("conclusion");
        workRecord.setVisitedIntention(intention);
        workRecord.setVisitedPlace(place);
        workRecord.setVisitedFlow(flow);
        workRecord.setVisitedConclusion(conclusion);
        setWorkRecordSession(workRecord);
        return MapMessage.successMessage();
    }

    /**
     * 选择教研员列表
     * @return
     */
    @RequestMapping(value = "search_researchers_visit_record.vpage")
    @ResponseBody
    public MapMessage searchResearchersVisitRecord() {
        MapMessage mapMessage = MapMessage.successMessage();
        String name = requestString("name");
        mapMessage.put("dataMap",agentResearchersService.loadResearchersInfoByUserIdForVisit(getCurrentUserId(),name));
        return mapMessage;
    }

    @RequestMapping(value = "save_researchers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveResearchers() {
        CrmWorkRecord workRecord = getWorkRecordSession();
        Long researchersId = getRequestLong("researchersId");
        AgentResearchers agentResearchers = agentResearchersService.loadResearchers(researchersId);
        if (agentResearchers == null) {
            return MapMessage.errorMessage("所选的教务员不存在");
        }
        workRecord.setResearchersId(researchersId);
        workRecord.setResearchersName(agentResearchers.getName());
        setWorkRecordSession(workRecord);
        return MapMessage.successMessage();
    }

    private List<Map<String, Object>> visitedIntention() {
        List<Map<String, Object>> intention = new ArrayList<>();
        Arrays.stream(VisitedResearchersIntention.values()).forEach(p -> {
            Map<String, Object> data = new HashMap<>();
            data.put("intention", p.getIntention());
            data.put("describe", p.getDescribe());
            intention.add(data);
        });
        return intention;
    }



    private void setWorkRecordSession(CrmWorkRecord attrValue) {
        Long userId = getCurrentUserId();
        agentCacheSystem.addUserSessionAttribte(userId, "agent_researchers_record", attrValue);
    }

    private CrmWorkRecord getWorkRecordSession() {
        Long userId = getCurrentUserId();
        Object obj = agentCacheSystem.getUserSessionAttribte(userId, "agent_researchers_record");
        if (obj == null) return new CrmWorkRecord();
        if (obj instanceof CrmWorkRecord) return (CrmWorkRecord) obj;
        return new CrmWorkRecord();
    }



    /**
     * 初始化教研员数据
     * @return
     */
    @RequestMapping(value = "updateData2New.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage initResearchersData2New(){
        agentResearchersService.initResearchersData();
        return MapMessage.successMessage();
    }

    /**
     * 验证手机号  验证通过归为私海教研员
     * @return
     */
    @RequestMapping(value = "check_researchers_phone.vpage")
    @ResponseBody
    public MapMessage checkResearchersPhone(){
        String phone = getRequestString("phone");
        Long id = getRequestLong("id");
        if(StringUtils.isBlank(phone)){
            return MapMessage.errorMessage("请输入要验证的的手机号");
        }
        if(id <= 0){
            return MapMessage.errorMessage("选择教研员");
        }
        return agentResearchersService.checkResearchersPhone(phone,id,getCurrentUserId());
    }

    /**
     * 获取全部修改日志
     * @return
     */
    @RequestMapping(value = "get_all_update_logs.vpage")
    @ResponseBody
    public MapMessage getAllUpdateLogs(){
        MapMessage mapMessage = MapMessage.successMessage();
        Long researchersId = getRequestLong("researchersId");
        List<AgentResearchersUpdateLog> logs = agentResearchersService.findUpdateLogsResearchersId(researchersId);
        mapMessage.put("datas",logs);
        return mapMessage;
    }

    /**
     * 根据地区编码获取地区名  工具方法
     * @return
     */
    @RequestMapping(value = "get_region_name.vpage")
    @ResponseBody
    public MapMessage getRegionName(){
        MapMessage mapMessage = MapMessage.successMessage();
        Integer level = getRequestInt("level");
        Integer regionCode = getRequestInt("regionCode");
        String regionName = agentResearchersService.getCityRegion(level,regionCode);
        mapMessage.put("regionName",regionName);
        return mapMessage;
    }

    //初始化年级数据 （把年级数据刷成json）
    @RequestMapping(value = "init_grade_data.vpage")
    @ResponseBody
    public MapMessage initGradeData(){
        MapMessage mapMessage = MapMessage.successMessage();
        agentResearchersService.initGradeData();
        return mapMessage;
    }

    //把学校阶段转成数字
    @RequestMapping(value = "init_grade_schoolPhase.vpage")
    @ResponseBody
    public MapMessage initSchoolPhase(){
        MapMessage mapMessage = MapMessage.successMessage();
        agentResearchersService.initSchoolPhase();
        return mapMessage;
    }

    //初始化类型为其他的教研员到私海
    @RequestMapping(value = "init_Job_private.vpage")
    @ResponseBody
    public MapMessage initJobPrivte(){
        MapMessage mapMessage = MapMessage.successMessage();
        agentResearchersService.initDelete2Private();
        return mapMessage;
    }

    /**
     * 所在部门下负责的区域
     * @return
     */
    @RequestMapping(value = "get_user_city_region.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserCityRegion(){
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        if(roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.BusinessDeveloper){
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByUserId(getCurrentUserId(), roleType);
            if (CollectionUtils.isEmpty(counties)) {
                return MapMessage.errorMessage("该用户下无地区");
            }
            return MapMessage.successMessage().add("dataList",agentResearchersService.getUserCityRegion(counties));
        }

        return MapMessage.successMessage().add("dataList",Collections.emptyList());
    }
}
