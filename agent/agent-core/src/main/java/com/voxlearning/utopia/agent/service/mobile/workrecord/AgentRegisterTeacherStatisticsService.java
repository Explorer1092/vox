/**
 * Author:   xianlong.zhang
 * Date:     2018/11/23 20:10
 * Description: 新注册老师service
 * History:
 */
package com.voxlearning.utopia.agent.service.mobile.workrecord;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.mongo.AgentRegisterTeacherStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.AgentRegisterTeacherStatistics;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.daily.AgentDailyService;
import com.voxlearning.utopia.agent.service.mobile.TeacherSummaryService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskManageService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AgentRegisterTeacherStatisticsService extends AbstractAgentService {

    @Inject
    private AgentWorkRecordStatisticsService agentWorkRecordStatisticsService;

    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentRegisterTeacherStatisticsDao agentRegisterTeacherStatisticsDao;

    @Inject
    private WorkRecordService workRecordService;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;

    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private AgentGroupSupport agentGroupSupport;
    /**
     * 获取按组织机构统计的数据
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long, AgentRegisterTeacherStatistics> getGroupRegisterTeacherStatistics(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentRegisterTeacherStatisticsDao.getGroupRegisterTeacherStatistics(groupIds, day, dateType);
    }

    /**
     * 获取按人员机构统计的数据
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long, AgentRegisterTeacherStatistics> getUserRegisterTeacherStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentRegisterTeacherStatisticsDao.getUserRegisterTeacherStatistics(userIds, day, dateType);
    }

    /**
     * 按组织机构获取下边人员数据
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long, List<AgentRegisterTeacherStatistics>> getGroupUserRegisterTeacherStatistics(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentRegisterTeacherStatisticsDao.getGroupUserRegisterTeacherStatistics(groupIds, day, dateType,2);
    }

    public Map<String,Object> rangeOrganizationRole(Long groupId,String userRoleType,String groupRoleType){
        Map<String,Object> dataMap = new HashMap<>();

        List<Map<String, Object>> groupRoleTypeList = new ArrayList<>();
        List<Map<String, Object>> roleTypeList = new ArrayList<>();
        //默认
        AgentGroup group = getDefaultGroup(groupId);
        //组织
        Map<String, Object> organizationMap = generateOrganization(group,userRoleType, groupRoleType);
        groupRoleTypeList = (List<Map<String, Object>>)organizationMap.get("groupRoleTypeList");
        //角色
        roleTypeList = generateRole(group,userRoleType);

        dataMap.put("group",group);
        dataMap.put("groupRoleTypeList",groupRoleTypeList);
        dataMap.put("roleTypeList",roleTypeList);
        return dataMap;
    }

    public Map<String,Object> generateOrganization(AgentGroup group,String userRoleType ,String groupRoleType){
        AgentRoleType agentRoleType = AgentRoleType.nameOf(userRoleType);
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String,Object>> groupRoleTypeList = new ArrayList<>();
        AgentGroupRoleType currentGroupRoleType = group.fetchGroupRoleType();

        if(agentRoleType == AgentRoleType.CityManager) {
            groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"人员",true,groupRoleType));
        } else if (agentRoleType == AgentRoleType.AreaManager){
            groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"人员",true,groupRoleType));
        } else if( agentRoleType == AgentRoleType.Region){
            groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"人员",true,groupRoleType));
        }else if(agentRoleType == AgentRoleType.BusinessDeveloper || agentRoleType == null){
            //分区、
            if (currentGroupRoleType == AgentGroupRoleType.City){
                groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"默认",true,groupRoleType));
            }else if(currentGroupRoleType == AgentGroupRoleType.Area){ //区域
                groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"人员",false,groupRoleType));
                groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City.name(),"默认",true,groupRoleType));
            }if (currentGroupRoleType == AgentGroupRoleType.Region){//大区
                //小学大区
                groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"人员",false,groupRoleType));
                if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area.name(),"默认",true,groupRoleType));
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City.name(),AgentGroupRoleType.City.getRoleName(),false,groupRoleType));
                    //中学大区
                }else if (group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL)){
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City.name(),"默认",true,groupRoleType));
                }
                //市场
            }else if(currentGroupRoleType == AgentGroupRoleType.Marketing){
                if(group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL) ){
                    groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area.name(),AgentGroupRoleType.Area.getRoleName(),false,groupRoleType));
                }
                groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City.name(),AgentGroupRoleType.City.getRoleName(),false,groupRoleType));
                groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"人员",false,groupRoleType));
                groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region.name(),"默认",true,groupRoleType));
            }
        }if((agentRoleType == AgentRoleType.Country && currentGroupRoleType == AgentGroupRoleType.Country) || ((agentRoleType == AgentRoleType.BusinessDeveloper || agentRoleType == null) && currentGroupRoleType == AgentGroupRoleType.Country)) {
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City.name(),AgentGroupRoleType.City.getRoleName(),false,groupRoleType));
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area.name(),AgentGroupRoleType.Area.getRoleName(),false,groupRoleType));
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region.name(),AgentGroupRoleType.Region.getRoleName(),false,groupRoleType));
            groupRoleTypeList.add(generateGroupRoleType(AgentRoleType.BusinessDeveloper.name(),"人员",false,groupRoleType));
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Marketing.name(),"默认",true,groupRoleType));
        }


        dataMap.put("groupRoleTypeList",groupRoleTypeList);
        return dataMap;
    }

    public List<Map<String,Object>> generateRole(AgentGroup group,String userRoleType){
        AgentGroupRoleType currentGroupRoleType = group.fetchGroupRoleType();
        List<Map<String,Object>> roleTypeList = new ArrayList<>();
        //按分区看
        if (currentGroupRoleType == AgentGroupRoleType.City){
            roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",true,userRoleType));
        }
        //按区域看
        if (currentGroupRoleType == AgentGroupRoleType.Area){
            //小学部门
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                roleTypeList.add(generateRoleType(AgentRoleType.CityManager,AgentRoleType.CityManager.getRoleName(),false,userRoleType));
                roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",true,userRoleType));
            }
        }
        //按大区看
        if (currentGroupRoleType == AgentGroupRoleType.Region){
            roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",true,userRoleType));
            roleTypeList.add(generateRoleType(AgentRoleType.CityManager,AgentRoleType.CityManager.getRoleName(),false,userRoleType));
            //小学部门
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                roleTypeList.add(generateRoleType(AgentRoleType.AreaManager,AgentRoleType.AreaManager.getRoleName(),false,userRoleType));
            }
        }

        if(currentGroupRoleType == AgentGroupRoleType.Marketing){
            roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",true,userRoleType));
            roleTypeList.add(generateRoleType(AgentRoleType.CityManager,AgentRoleType.CityManager.getRoleName(),false,userRoleType));
            //小学部门
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                roleTypeList.add(generateRoleType(AgentRoleType.AreaManager,AgentRoleType.AreaManager.getRoleName(),false,userRoleType));
            }
            roleTypeList.add(generateRoleType(AgentRoleType.Region,AgentRoleType.Region.getRoleName(),false,userRoleType));
        }
        if(currentGroupRoleType == AgentGroupRoleType.Country){
            roleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",true,userRoleType));
            roleTypeList.add(generateRoleType(AgentRoleType.CityManager,AgentRoleType.CityManager.getRoleName(),false,userRoleType));
            roleTypeList.add(generateRoleType(AgentRoleType.AreaManager,AgentRoleType.AreaManager.getRoleName(),false,userRoleType));
            roleTypeList.add(generateRoleType(AgentRoleType.Region,AgentRoleType.Region.getRoleName(),false,userRoleType));
        }

        return roleTypeList;
    }

    public Map<String,Object> generateGroupRoleType(String groupRoleType,String roleName,Boolean ifShow,String selectGroupRoleType){
        Map<String,Object> groupRoleTypeMap = new HashMap<>();
        groupRoleTypeMap.put("groupRoleType",groupRoleType);
        groupRoleTypeMap.put("roleName",roleName);
        groupRoleTypeMap.put("show",ifShow);
//        if(StringUtils.isBlank(selectGroupRoleType)){
//            groupRoleTypeMap.put("show",ifShow);
//        }else{
//            groupRoleTypeMap.put("show",groupRoleType.equals(selectGroupRoleType));
//        }

        return groupRoleTypeMap;
    }

    public Map<String,Object> generateRoleType(AgentRoleType roleType,String roleName,Boolean ifShow,String userRoleType){
        Map<String,Object> roleTypeMap = new HashMap<>();
        roleTypeMap.put("userRoleType",roleType);
        roleTypeMap.put("roleName",roleName);
        roleTypeMap.put("show",ifShow);
        if(StringUtils.isBlank(userRoleType)){
            roleTypeMap.put("show",ifShow);
        }else {
            roleTypeMap.put("show",roleType == AgentRoleType.nameOf(userRoleType));
        }

        return roleTypeMap;
    }

    public MapMessage teamStatisticsData(Long groupId, String userRoleType,String groupRoleType,Integer dateType,Date date){
        MapMessage mapMessage = MapMessage.successMessage();
        AgentGroup group = getDefaultGroup(groupId);

        AgentRoleType agentRoleType = AgentRoleType.nameOf(userRoleType);
        if (agentRoleType == null){
            agentRoleType = AgentRoleType.BusinessDeveloper;
        }
        List<Long> groupIds = new ArrayList<>();
        if(agentRoleType == AgentRoleType.CityManager || agentRoleType == AgentRoleType.AreaManager || agentRoleType == AgentRoleType.Region || (agentRoleType == AgentRoleType.BusinessDeveloper && "BusinessDeveloper".equals(groupRoleType))){//按人员看的情况
            List<Long>  userIds =baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(),agentRoleType.getId());
            mapMessage.add("dataList",workRecordService.userWorkStatisticsList(userIds,date,dateType));
        }else if(agentRoleType == AgentRoleType.BusinessDeveloper){//按部门看
            AgentGroupRoleType agentGroupRoleType = AgentGroupRoleType.nameOf(groupRoleType);
            if(agentGroupRoleType != null ){
                //过滤出指定部门级别的子部门
                List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId).stream().filter(p -> Objects.equals(p.getRoleId(), agentGroupRoleType.getId())).collect(Collectors.toList());
                groupIds = subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            }else {
                List<AgentGroup> groupList = baseOrgService.getGroupListByParentId(group.getId());
                groupIds = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            }
            mapMessage.add("dataList",workRecordService.groupWorkStatisticsList(groupIds,date,dateType));
        }
        return mapMessage;
    }

    public AgentGroup getDefaultGroup(Long groupId){
        Long userId = getCurrentUserId();
        AgentGroup group = new AgentGroup();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        //默认
        if (groupId == 0L){
            //全国总监
            if (userRole == AgentRoleType.Country){
                //市场看市场部
                group = baseOrgService.findAllGroups().stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Country).findFirst().orElse(null);
            }else {
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
                group = baseOrgService.getGroupById(groupUser.getGroupId());
            }
        }else {
            group = baseOrgService.getGroupById(groupId);
        }
        return group;
    }

    public void generateRegisterTeacherData(String subject ,Long schoolId){
        //非字典表学校不统计
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if(agentDictSchool == null){
            return;
        }
        //根据学校id查询学校所属专员  统计人员数据时  市经理的数据都加一
        List<AgentUserSchool> agentUserSchools = baseOrgService.getUserSchoolBySchool(schoolId);
        Set<Long> userIds = agentUserSchools.stream().map(AgentUserSchool::getUserId).collect(Collectors.toSet());
        Long groupId = null;
        if(CollectionUtils.isEmpty(userIds)){
            List<Long> groupIds = agentGroupSupport.getGroupIdsBySchool(schoolId, Collections.singletonList(AgentGroupRoleType.City));
            if(CollectionUtils.isNotEmpty(groupIds)){
                groupId = groupIds.get(0);
            }
        }else {
            List<AgentGroup> cityGroup = baseOrgService.getUserGroups(userIds.iterator().next());
            groupId = cityGroup.get(0).getId();
        }
        Long cityManager = baseOrgService.getGroupManager(groupId);
        if(cityManager != null){
            userIds.add(cityManager);
        }
        Date date = new Date();
        if(CollectionUtils.isNotEmpty(userIds)){
            //人员数据
            generateUserRegisterTeacherData(userIds,subject,date,1);
            generateUserRegisterTeacherData(userIds,subject,date,2);
            generateUserRegisterTeacherData(userIds,subject,date,3);
            //部门数据
            if(groupId != null){
                generateGroupRegisterTeacherData(groupId,subject,date,1);
                generateGroupRegisterTeacherData(groupId,subject,date,2);
                generateGroupRegisterTeacherData(groupId,subject,date,3);
            }

        }

    }
    public void generateUserRegisterTeacherData(Set<Long> userIds,String subject,Date date,Integer dateType){
        Map<Long,AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser :: getId,Function.identity()));
        Map<Long,List<AgentGroup>> userGroups = baseOrgService.getUserGroups(userIds);
        Map<Long, AgentRegisterTeacherStatistics> userStatisticsMap = getUserRegisterTeacherStatistics(userIds,date,dateType);
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        userIds.forEach(p->{
            AgentGroup group = userGroups.get(p).get(0);
            AgentUser agentUser = userMap.get(p);
            AgentRoleType roleType = baseOrgService.getUserRole(p);
            AgentRegisterTeacherStatistics registerTeacherStatistics = userStatisticsMap.get(p);
            if(registerTeacherStatistics == null){
                registerTeacherStatistics = new AgentRegisterTeacherStatistics(day,dateType, 2,group.getId(),group.getGroupName(),group.fetchGroupRoleType().getId(),p,agentUser.getRealName(),roleType.getId());
            }
            Subject sub = Subject.safeParse(subject);
            if(sub == Subject.CHINESE){
                registerTeacherStatistics.setUserRegisterChnTeacherCount((registerTeacherStatistics.getUserRegisterChnTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterChnTeacherCount()) + 1);
            }else if(sub == Subject.MATH){
                registerTeacherStatistics.setUserRegisterMathTeacherCount((registerTeacherStatistics.getUserRegisterMathTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterMathTeacherCount()) + 1);
            }else if(sub == Subject.ENGLISH){
                registerTeacherStatistics.setUserRegisterEngTeacherCount((registerTeacherStatistics.getUserRegisterEngTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterEngTeacherCount()) + 1);
            }else {
                registerTeacherStatistics.setUserRegisterOtherTeacherCount((registerTeacherStatistics.getUserRegisterOtherTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterOtherTeacherCount()) + 1);
            }
            registerTeacherStatistics.setUserRegisterTeacherCount((registerTeacherStatistics.getUserRegisterTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterTeacherCount()) + 1);
            agentRegisterTeacherStatisticsDao.upsert(registerTeacherStatistics);
        });
    }
    public void generateGroupRegisterTeacherData(Long groupId,String subject,Date date,Integer dateType){
//        Map<Long,AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser :: getId,Function.identity()));
//        Map<Long,List<AgentGroup>> userGroups = baseOrgService.getUserGroups(userIds);
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
//        userIds.forEach(p->{
            AgentGroup group = baseOrgService.getGroupById(groupId);
            Set<AgentGroup> allGroup = baseOrgService.getAllParentGroup(group.getId());//当前人员的所有父级部门

            Map<Long,AgentGroup> groupMap = allGroup.stream().collect(Collectors.toMap(AgentGroup :: getId,Function.identity()));
            Set<Long> groupIds = allGroup.stream().map(AgentGroup::getId).collect(Collectors.toSet());
            groupIds.add(groupId);
            Map<Long, AgentRegisterTeacherStatistics> groupStatisticsMap = getGroupRegisterTeacherStatistics(groupIds,date,dateType);

            groupIds.forEach(g->{
                AgentGroup agentGroup = groupMap.get(g);
                AgentRegisterTeacherStatistics registerTeacherStatistics = groupStatisticsMap.get(g);
                if(registerTeacherStatistics == null){
                    registerTeacherStatistics = new AgentRegisterTeacherStatistics(day,dateType, 1,g,agentGroup.getGroupName(),agentGroup.fetchGroupRoleType().getId(),null,null,null);
                }

                List<Long> subGroupIds = baseOrgService.getSubGroupList(g).stream().map(AgentGroup::getId).collect(Collectors.toList());
                subGroupIds.add(g);
//                Map<Long, List<AgentRegisterTeacherStatistics>> groupStatisticsListMap = getGroupUserRegisterTeacherStatistics(subGroupIds,date,dateType);
//                List<AgentRegisterTeacherStatistics> userListTeacherStatistics = new ArrayList<>();
//                groupStatisticsListMap.values().forEach(gu ->{
//                    userListTeacherStatistics.addAll(gu);
//                });
//                List<Long> userIds = userListTeacherStatistics.stream().map(AgentRegisterTeacherStatistics :: getUserId).collect(Collectors.toList());
//
//                Map<Long, List<Integer>> userRoleMap =  baseOrgService.getGroupUserRoleMapByUserIds(userIds);
//                List<Long> userIdList = new ArrayList<>();
//                userRoleMap.forEach((k,v) ->{
//                    if(v.contains(AgentRoleType.BusinessDeveloper.getId())){
//                        userIdList.add(k);
//                    }
//                });
                int userNum = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(g,AgentRoleType.BusinessDeveloper.getId()).size();
                Subject sub = Subject.safeParse(subject);
                //设置部门数据总人数
                if(sub == Subject.CHINESE){
                    registerTeacherStatistics.setGroupRegisterChnTeacherCount((registerTeacherStatistics.getGroupRegisterChnTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterChnTeacherCount()) + 1);
                    registerTeacherStatistics.setPerPersonRegisterChnTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterChnTeacherCount(),userNum,1));
                }else if(sub == Subject.MATH){
                    registerTeacherStatistics.setGroupRegisterMathTeacherCount((registerTeacherStatistics.getGroupRegisterMathTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterMathTeacherCount()) + 1);
                    registerTeacherStatistics.setPerPersonRegisterMathTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterMathTeacherCount(),userNum,1));
                }else if(sub == Subject.ENGLISH){
                    registerTeacherStatistics.setGroupRegisterEngTeacherCount((registerTeacherStatistics.getGroupRegisterEngTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterEngTeacherCount()) + 1);
                    registerTeacherStatistics.setPerPersonRegisterEngTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterEngTeacherCount(),userNum,1));
                }else {
                    registerTeacherStatistics.setGroupRegisterOtherTeacherCount((registerTeacherStatistics.getGroupRegisterOtherTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterOtherTeacherCount()) + 1);
                    registerTeacherStatistics.setPerPersonRegisterOtherTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterOtherTeacherCount(),userNum,1));
                }
                registerTeacherStatistics.setGroupRegisterTeacherCount((registerTeacherStatistics.getGroupRegisterTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterTeacherCount()) + 1);
                registerTeacherStatistics.setPerPersonRegisterTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterTeacherCount(),userNum,1));
                agentRegisterTeacherStatisticsDao.upsert(registerTeacherStatistics);
            });
//        });
    }

    //定时任务  使用summary数据更人员新老师注册数据
    public void generateUserSummaryRegisterTeacherData(Set<Long> userIds, Date date, Integer dateType, List<CrmTeacherSummary> userDayRegisterList){
        if(CollectionUtils.isEmpty(userDayRegisterList) || CollectionUtils.isEmpty(userIds)){
            return;
        }
        List<CrmTeacherSummary> chnList = userDayRegisterList.stream().filter(p -> p.getSubject().equals(Subject.CHINESE.name())).collect(Collectors.toList());
        List<CrmTeacherSummary> mathList = userDayRegisterList.stream().filter(p -> p.getSubject().equals(Subject.MATH.name())).collect(Collectors.toList());
        List<CrmTeacherSummary> enList = userDayRegisterList.stream().filter(p -> p.getSubject().equals(Subject.ENGLISH.name())).collect(Collectors.toList());
        List<CrmTeacherSummary> otherList = userDayRegisterList.stream().filter(p -> !(p.getSubject().equals(Subject.CHINESE.name())) && !(p.getSubject().equals(Subject.MATH.name())) && !(p.getSubject().equals(Subject.ENGLISH.name()))  ).collect(Collectors.toList());

        Map<Long,AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser :: getId,Function.identity()));
        Map<Long,List<AgentGroup>> userGroups = baseOrgService.getUserGroups(userIds);
        Map<Long, AgentRegisterTeacherStatistics> userStatisticsMap = getUserRegisterTeacherStatistics(userIds,date,dateType);
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        userIds.forEach(p->{
            AgentGroup group = userGroups.get(p).get(0);
            AgentUser agentUser = userMap.get(p);
            AgentRoleType roleType = baseOrgService.getUserRole(p);
            AgentRegisterTeacherStatistics registerTeacherStatistics = userStatisticsMap.get(p);
            if(registerTeacherStatistics == null){
                registerTeacherStatistics = new AgentRegisterTeacherStatistics(day,dateType, 2,group.getId(),group.getGroupName(),group.fetchGroupRoleType().getId(),p,agentUser.getRealName(),roleType.getId());
            }
            registerTeacherStatistics.setUserRegisterChnTeacherCount((registerTeacherStatistics.getUserRegisterChnTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterChnTeacherCount()) + chnList.size());
            registerTeacherStatistics.setUserRegisterMathTeacherCount((registerTeacherStatistics.getUserRegisterMathTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterMathTeacherCount()) + mathList.size());
            registerTeacherStatistics.setUserRegisterEngTeacherCount((registerTeacherStatistics.getUserRegisterEngTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterEngTeacherCount()) + enList.size());
            registerTeacherStatistics.setUserRegisterOtherTeacherCount((registerTeacherStatistics.getUserRegisterOtherTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterOtherTeacherCount()) + otherList.size());
            registerTeacherStatistics.setUserRegisterTeacherCount((registerTeacherStatistics.getUserRegisterTeacherCount() == null ? 0 : registerTeacherStatistics.getUserRegisterTeacherCount()) + userDayRegisterList.size());
            agentRegisterTeacherStatisticsDao.upsert(registerTeacherStatistics);
        });
    }

    //每次处理一天的数据
    public void generateUserJobTeacherData(Long userId, Date date,Integer dateType,Integer dayNum){
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        Set<Long> userIds = new HashSet<>();
        List<Long> userSchoolIds = new ArrayList<>() ;
        if(userRole == AgentRoleType.BusinessDeveloper){
            userIds.add(userId);
            List<AgentUser> managers = baseOrgService.getUserManager(userId);
            if(CollectionUtils.isNotEmpty(managers)){
                userIds.add(managers.get(0).getId());
            }
            userSchoolIds = baseOrgService.getUserSchools(userId);
        }else if(userRole == AgentRoleType.CityManager){
            List<AgentGroup> cityGroup = baseOrgService.getUserGroups(userId);
            userSchoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(cityGroup.get(0).getId());
        }else{//指处理市经理和专员的数据 其他角色数据不处理
            return;
        }
        Map<Long, List<CrmTeacherSummary>>  teacherListMap = crmSummaryLoaderClient.loadSchoolTeachers(userSchoolIds);
        List<CrmTeacherSummary> userTeacherList = teacherListMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        for(int i = 0; i < dayNum ; i++){
            Date dateItem = DateUtils.calculateDateDay(date,i);
            Date startDate = agentWorkRecordStatisticsService.getStartDatePub(dateItem, 1);
            Long longStartDate = Long.valueOf(DateUtils.dateToString(startDate,"yyyyMMddHHmmss"));
            Date endDate = agentWorkRecordStatisticsService.getEndDatePub(dateItem, 1);
            Long longEndDate = Long.valueOf(DateUtils.dateToString(endDate,"yyyyMMddHHmmss"));
            List<CrmTeacherSummary> userDayList = userTeacherList.stream().filter( cs -> (cs.getRegisterTime() != null && cs.getRegisterTime() > longStartDate && cs.getRegisterTime() < longEndDate)).collect(Collectors.toList());
            generateUserSummaryRegisterTeacherData(userIds, dateItem,  dateType, userDayList);
        }


    }
    //每次处理一天部门的数据
    public void generateGroupJobTeacherData(Long groupId, Date date,Integer dateType,Integer dayNum){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        List<CrmTeacherSummary> groupTeacherList = new ArrayList<>();
        if(group != null && group.fetchGroupRoleType() == AgentGroupRoleType.City){
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByGroupId(group.getId());
            Set<Integer> countySet = new HashSet<>();
            counties.forEach(p -> {
                if(p.getCountyCode() > 0){
                    countySet.add(p.getCountyCode());
                }
            });
            List<String> schoolLevelList = getSchoolLevel(group.getServiceType());
            for(Integer code : countySet){
                List<CrmTeacherSummary> codeSummary = crmSummaryLoaderClient.findByCountyCodes(Collections.singleton(code)).stream().filter(gc -> schoolLevelList.contains(gc.getSchoolLevel())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(codeSummary)){
                    groupTeacherList.addAll(codeSummary);
                }
            }
        }else{//
            return;
        }

        for(int i = 0 ; i< dayNum ;i ++){
            Date dateItem  = DateUtils.calculateDateDay(date , i);
            Date startDate = agentWorkRecordStatisticsService.getStartDatePub(dateItem, 1);
            Long longStartDate = Long.valueOf(DateUtils.dateToString(startDate,"yyyyMMddHHmmss"));
            Date endDate = agentWorkRecordStatisticsService.getEndDatePub(dateItem, 1);
            Long longEndDate = Long.valueOf(DateUtils.dateToString(endDate,"yyyyMMddHHmmss"));
            List<CrmTeacherSummary> dayList =  groupTeacherList.stream().filter( cs -> (cs.getRegisterTime() != null && cs.getRegisterTime() > longStartDate && cs.getRegisterTime() < longEndDate)).collect(Collectors.toList());
            generateGroupSummaryRegisterTeacherData(groupId, dateItem,  dateType, dayList);
        }

    }
    public void generateGroupSummaryRegisterTeacherData(Long groupId,Date date,Integer dateType,List<CrmTeacherSummary> groupTeacherSummaryList){
        if(CollectionUtils.isEmpty(groupTeacherSummaryList)){
            return;
        }
        List<CrmTeacherSummary> chnList = groupTeacherSummaryList.stream().filter(p -> p.getSubject().equals(Subject.CHINESE.name())).collect(Collectors.toList());
        List<CrmTeacherSummary> mathList = groupTeacherSummaryList.stream().filter(p -> p.getSubject().equals(Subject.MATH.name())).collect(Collectors.toList());
        List<CrmTeacherSummary> enList = groupTeacherSummaryList.stream().filter(p -> p.getSubject().equals(Subject.ENGLISH.name())).collect(Collectors.toList());
        List<CrmTeacherSummary> otherList = groupTeacherSummaryList.stream().filter(p -> !(p.getSubject().equals(Subject.CHINESE.name())) && !(p.getSubject().equals(Subject.MATH.name())) && !(p.getSubject().equals(Subject.ENGLISH.name()))  ).collect(Collectors.toList());
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
//        AgentGroup group = baseOrgService.getGroupById(groupId);
        Set<AgentGroup> allGroup = baseOrgService.getAllParentGroup(groupId);//当前人员的所有父级部门
        Map<Long,AgentGroup> groupMap = allGroup.stream().collect(Collectors.toMap(AgentGroup :: getId,Function.identity()));
        Set<Long> groupIds = allGroup.stream().map(AgentGroup::getId).collect(Collectors.toSet());
        Map<Long, AgentRegisterTeacherStatistics> groupStatisticsMap = getGroupRegisterTeacherStatistics(groupIds,date,dateType);
        groupIds.forEach(g->{
            AgentGroup agentGroup = groupMap.get(g);
            AgentRegisterTeacherStatistics registerTeacherStatistics = groupStatisticsMap.get(g);
            if(registerTeacherStatistics == null){
                registerTeacherStatistics = new AgentRegisterTeacherStatistics(day,dateType, 1,g,agentGroup.getGroupName(),agentGroup.getRoleId() != null ? agentGroup.fetchGroupRoleType().getId() : null,null,null,null);
            }

//            List<Long> subGroupIds = baseOrgService.getSubGroupList(g).stream().map(AgentGroup::getId).collect(Collectors.toList());
//            Map<Long, List<AgentRegisterTeacherStatistics>> groupStatisticsListMap = getGroupUserRegisterTeacherStatistics(subGroupIds,date,dateType);
//            List<AgentRegisterTeacherStatistics> userList = new ArrayList<>();
//            groupStatisticsListMap.values().forEach(gu ->{
//                userList.addAll(gu);
//            });
            int userNum = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(g,AgentRoleType.BusinessDeveloper.getId()).size();

            registerTeacherStatistics.setGroupRegisterChnTeacherCount((registerTeacherStatistics.getGroupRegisterChnTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterChnTeacherCount()) + chnList.size());
            registerTeacherStatistics.setPerPersonRegisterChnTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterChnTeacherCount(),userNum,chnList.size(),1));

            registerTeacherStatistics.setGroupRegisterMathTeacherCount((registerTeacherStatistics.getGroupRegisterMathTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterMathTeacherCount()) + mathList.size());
            registerTeacherStatistics.setPerPersonRegisterMathTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterMathTeacherCount(),userNum,1));

            registerTeacherStatistics.setGroupRegisterEngTeacherCount((registerTeacherStatistics.getGroupRegisterEngTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterEngTeacherCount()) + enList.size());
            registerTeacherStatistics.setPerPersonRegisterEngTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterEngTeacherCount(),userNum,1));

            registerTeacherStatistics.setGroupRegisterOtherTeacherCount((registerTeacherStatistics.getGroupRegisterOtherTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterOtherTeacherCount()) + otherList.size());
            registerTeacherStatistics.setPerPersonRegisterOtherTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterOtherTeacherCount(),userNum,1));

            registerTeacherStatistics.setGroupRegisterTeacherCount((registerTeacherStatistics.getGroupRegisterTeacherCount() == null ? 0 : registerTeacherStatistics.getGroupRegisterTeacherCount()) + groupTeacherSummaryList.size());
            registerTeacherStatistics.setPerPersonRegisterTeacherCount(MathUtils.doubleDivide(registerTeacherStatistics.getGroupRegisterTeacherCount(),userNum,1));
            agentRegisterTeacherStatisticsDao.upsert(registerTeacherStatistics);
        });
//        });
    }
    private List<String> getSchoolLevel(String serviceType){
        if(StringUtils.isBlank(serviceType)){
            return Collections.emptyList();
        }
        List<String> schoolLevelList = new ArrayList<>();
        String[] arr = serviceType.split(",");
        for (String str : arr){
            AgentServiceType agentServiceType = AgentServiceType.nameOf(str);
            if(agentServiceType != null){
                schoolLevelList.add(agentServiceType.toSchoolLevel().toString());
            }
        }
        return schoolLevelList;
    }

//    // type :   1:统计数据  2：工作记录的T值计算
//    public void generateTeacherData(Date date){
//        List<AgentGroup> groupList = new ArrayList<>();
//        AgentGroup group = baseOrgService.getGroupByName("市场部");
//        groupList.add(group);
//        groupList.addAll(baseOrgService.getSubGroupList(group.getId()));
//
//        // 生成user的日，周，月数据
//        List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
//        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroups(groupIdList);
//        Set<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
//        //先跑人员数据再跑部门数据  部门数据需要人员数据的人数求平均数
//        userIds.forEach(uid ->{
//            generateUserJobTeacherData(uid, date,  1);
//            generateUserJobTeacherData(uid, date, 2);
//            generateUserJobTeacherData(uid, date, 3);
//        });
//        groupList.forEach(gid->{
//            generateGroupJobTeacherData(gid.getId(), date,  1);
//            generateGroupJobTeacherData(gid.getId(), date, 2);
//            generateGroupJobTeacherData(gid.getId(), date, 3);
//        });
//    }

    public List<CrmTeacherSummary> getUserOrGroupDayRegisterTeachcer(Long id,Integer groupOrUser,Date date){
        List<CrmTeacherSummary> crmTeacherSummaries = new ArrayList<>();
        if(groupOrUser == 1){
            AgentGroup group = baseOrgService.getGroupById(id);
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByGroupId(group.getId());
            Set<Integer> countySet = new HashSet<>();
            counties.forEach(p -> {
                if(p.getCountyCode() > 0){
                    countySet.add(p.getCountyCode());
                }
            });
            List<String> schoolLevelList = getSchoolLevel(group.getServiceType());
            for(Integer code : countySet){
                List<CrmTeacherSummary> codeSummary = crmSummaryLoaderClient.findByCountyCodes(Collections.singleton(code)).stream().filter(gc -> schoolLevelList.contains(gc.getSchoolLevel())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(codeSummary)){
                    crmTeacherSummaries.addAll(codeSummary);
                }
            }
        }else if(groupOrUser == 2){
            List<Long> userSchoolIds = baseOrgService.getUserSchools(id);
            Map<Long, List<CrmTeacherSummary>>  teacherListMap = crmSummaryLoaderClient.loadSchoolTeachers(userSchoolIds);
            crmTeacherSummaries = teacherListMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        }

        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, 1);
        Long longStartDate = Long.valueOf(DateUtils.dateToString(startDate,"yyyyMMddHHmmss"));
        Date endDate = agentWorkRecordStatisticsService.getEndDatePub(date, 1);
        Long longEndDate = Long.valueOf(DateUtils.dateToString(endDate,"yyyyMMddHHmmss"));
        return crmTeacherSummaries.stream().filter( cs -> (cs.getRegisterTime() != null && cs.getRegisterTime() > longStartDate && cs.getRegisterTime() < longEndDate)).collect(Collectors.toList());
    }
}
