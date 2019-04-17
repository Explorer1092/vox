/**
 * Author:   xianlong.zhang
 * Date:     2018/9/14 21:05
 * Description: 新大考管理
 * History:
 */
package com.voxlearning.utopia.agent.service.exam;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.dao.mongo.exam.AgentExamContractExtendDao;
import com.voxlearning.utopia.agent.dao.mongo.exam.AgentExamDistributionDao;
import com.voxlearning.utopia.agent.dao.mongo.exam.AgentExamUserInfoDao;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.persist.entity.exam.*;
import com.voxlearning.utopia.agent.persist.exam.AgentExamContractPersistence;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class AgentExamNewService extends AbstractAgentService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private BaseOrgService baseOrgService;
//    @Inject
//    private AgentExamSchoolNewDao agentExamSchoolNewDao;
    @Inject
    private AgentExamDistributionDao agentExamDistributionDao;
    @Inject
    private AgentExamUserInfoDao agentExamUserInfoDao;
    @Inject
    private AgentExamContractPersistence agentExamContractPersistence;

    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private SearchService searchService;
    @Inject
    private AgentExamContractExtendDao agentExamContractExtendDao;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    public MapMessage getExamList(Integer type,Integer grade,String name,Integer pageSize,Integer pageNo){
        if(pageSize <= 0){
            pageSize = 50;
        }
        Pageable pageable = new PageRequest(pageNo,pageSize);
        Long userId = getCurrentUserId();
        List<AgentGroup> groups = baseOrgService.getUserGroups(userId);
        if(CollectionUtils.isEmpty(groups)){
            return MapMessage.successMessage().add("dataList",Collections.emptyList()).add("hasNext",false);
        }
        boolean hasNext = true;
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        List<Map<String,Object>> allResult = new ArrayList<>();
        Set<String> examIdSet = new LinkedHashSet<>();
        List<Long> schoolIds = new ArrayList<>();
        if(StringUtils.isNotBlank(name)){
            Page<SchoolEsInfo> esInfoPage = searchService.searchSchoolPageForScene(userId, name, 2, null, null, 0, 20);//默认查20个学校
            if(CollectionUtils.isNotEmpty(esInfoPage.getContent())){
                esInfoPage.getContent().stream().forEach(p -> schoolIds.add(SafeConverter.toLong(p.getId())));
            }
            if(CollectionUtils.isEmpty(schoolIds)){
                return MapMessage.successMessage().add("dataList",allResult).add("hasNext",false);
            }
        }
        if(roleType == AgentRoleType.Country){
            if(type == null || type == 1 || type == 2){//从全部数据查
                allResult = getExamByIds(Collections.emptyList(),Collections.emptyList(),schoolIds,grade, pageable.getPageSize(),pageable.getOffset());
            } if(type == 3){//查分配过的
                Page<AgentExamDistribution> pages = agentExamDistributionDao.findUserExamListPage(Collections.emptyList(),type,schoolIds,grade,pageable);
                if(CollectionUtils.isNotEmpty(pages.getContent())){
                    pages.getContent().forEach(p ->{
                       if( p.getDistributionState() == true && p.getEvaluateState() == false){
                           examIdSet.add(p.getExamId());
                       }
                    });
                    if(pages.getContent().size() < pageSize){
                        hasNext = false;
                    }
                    allResult = getExamByIds(examIdSet,Collections.emptyList(),schoolIds,grade, pageable.getPageSize(),0);
                }else {
                    hasNext = false;
                }
            }

        }else if( roleType == AgentRoleType.ChannelDirector || roleType == AgentRoleType.ChannelManager || roleType == AgentRoleType.ChannelOperator) {
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByUserId(userId, roleType);
            Set<Integer> provinceSet = new LinkedHashSet<>();
            Set<Integer> citySet = new LinkedHashSet<>();
            Set<Integer> countySet = new LinkedHashSet<>();
            counties.forEach(p -> {
                provinceSet.add(p.getProvinceCode());
                citySet.add(p.getCityCode());
                countySet.add(p.getCountyCode());
            });
            if(countySet.size()> 0){//没有区域啥都不让看
                if(type == null || type == 1 || type == 2){//从全部数据查
                    allResult = getExamByIds(null,countySet,schoolIds,grade, pageable.getPageSize(),pageable.getOffset());
                } if(type == 3){//查分配过的
                    Page<AgentExamDistribution> pages = agentExamDistributionDao.findUserExamListPage(countySet,type,schoolIds,grade,pageable);
                    if(CollectionUtils.isNotEmpty(pages.getContent())){
                        pages.getContent().forEach(p ->{
                            if( p.getDistributionState() == true && p.getEvaluateState() == false){
                                examIdSet.add(p.getExamId());
                            }
                        });
                        if(pages.getContent().size() < pageSize){
                            hasNext = false;
                        }
                        allResult = getExamByIds(examIdSet,Collections.emptyList(),schoolIds,grade, pageable.getPageSize(),0);
                    }else {
                        hasNext = false;
                    }
                }
            }
        }else if ( roleType == AgentRoleType.CityManager ){
            List<AgentGroup> userGroups = baseOrgService.getUserGroups(userId);
            List<String> examIds = new ArrayList<>();
            if(schoolIds.size() > 0){
                examIds = getCityOrDevExamIds(type,schoolIds);
            }
            if (CollectionUtils.isNotEmpty(userGroups)) {
                List<Long> userIds = baseOrgService.getGroupUsersByRole(userGroups.get(0).getId(), AgentRoleType.BusinessDeveloper);
                userIds.add(userId);
                Page<AgentExamUserInfo> pages = agentExamUserInfoDao.findExamListByUserIdsPage(userIds,examIds,grade,pageable);
                if(CollectionUtils.isNotEmpty(pages.getContent())){
                    pages.getContent().forEach(p ->{
                        examIdSet.add(p.getExamId());
                    });
                    allResult = getExamByIds(examIdSet,Collections.emptyList(),schoolIds,grade, pageable.getPageSize(),0);
                }
            }
        }else if ( roleType == AgentRoleType.BusinessDeveloper ){
            List<String> examIds = new ArrayList<>();
            if(schoolIds.size() > 0){
                examIds = getCityOrDevExamIds(type,schoolIds);
            }
            Page<AgentExamUserInfo> pages = agentExamUserInfoDao.findExamListByUserIdsPage(Collections.singleton(userId),examIds,grade,pageable);
            if(CollectionUtils.isNotEmpty(pages.getContent())){
                pages.getContent().forEach(p ->{
                    examIdSet.add(p.getExamId());
                });
                allResult = getExamByIds(examIdSet,Collections.emptyList(),schoolIds,grade, pageable.getPageSize(),0);
            }
        }
        Set<String> pageExamList = new HashSet<>();
        allResult.forEach(p ->{
            pageExamList.add(SafeConverter.toString(p.get("examId")));
        });
        Map<String,List<AgentExamDistribution>> distributionExams = agentExamDistributionDao.findByExamIds(pageExamList);
        allResult.forEach(e -> {
            List<AgentExamDistribution> agentExamDistributions = distributionExams.get(SafeConverter.toString(e.get("examId")));
            if(CollectionUtils.isNotEmpty(agentExamDistributions)){
                e.put("distributionState",agentExamDistributions.get(0).getDistributionState());
                e.put("evaluateState",agentExamDistributions.get(0).getEvaluateState());
            }else{
                e.put("distributionState",false);
                e.put("evaluateState",false);
            }
        });
        if( type != 3 && allResult.size() < pageSize){
            hasNext = false;
        }
        if(type == 2){
            allResult = allResult.stream().filter(p ->
                !SafeConverter.toBoolean(p.get("distributionState"))
            ).collect(Collectors.toList());
        }
        return MapMessage.successMessage().add("dataList",allResult).add("hasNext",hasNext);
    }
    private List<String> getCityOrDevExamIds(Integer type,Collection<Long> schoolIds){
        List<AgentExamDistribution>  list = agentExamDistributionDao.findUserExamList(Collections.emptyList(),type,schoolIds,0);
        return list.stream().map(AgentExamDistribution :: getExamId).collect(Collectors.toList());
    }
    public List<Map<String,Object>> getExamByIds(Collection<String> examIds, Collection<Integer> regionCodes, Collection<Long> schoolIds, Integer grade, Integer limit, Integer offset){
        List<Map<String,Object>> resultList = new ArrayList<>();
       StringBuffer sbf = new StringBuffer(getBaseExamUrl());
        sbf.append("/exam/tianji_exams/list");
        Map<Object, Object> examParam = new HashMap<>();
        if(CollectionUtils.isNotEmpty(regionCodes)){
            examParam.put("region_ids",regionCodes);
        }
        if(grade != null && grade > 0){
            examParam.put("grade",grade);
        }
        if(CollectionUtils.isNotEmpty(examIds)){
            examParam.put("exam_ids",examIds);
        }
        if(CollectionUtils.isNotEmpty(schoolIds)){
            examParam.put("school_ids",schoolIds);
        }
        examParam.put("limit",limit);
        examParam.put("offset",offset);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(sbf.toString()).json(JsonUtils.toJson(examParam)).execute();
            if (response.getStatusCode() == 200) {
                Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                if (MapUtils.isNotEmpty(resultMap)) {
                    Object body = resultMap.get("body");
                    if (body != null) {
                        Map<String, Object> dataMap = (Map<String, Object>) body;
                        dataMap.forEach((k,v) ->{
                            Map<String,Object> vMap = (Map<String, Object>) v;
                            Map<String,Object> map = new HashMap<>();
                            map.put("examId",k);
                            map.put("schoolId",vMap.get("school_id"));
                            map.put("name",vMap.get("name"));
                            map.put("type",vMap.get("type"));
                            map.put("grade",vMap.get("grade"));
                            Object objSchoolIds = vMap.get("school_ids");
                            map.put("schoolNum",1);
                            if(objSchoolIds != null){
                                List<Long> schoolIdList = (List<Long>)objSchoolIds;
                                if(schoolIdList.size() > 1){
                                    map.put("schoolNum",schoolIdList.size());
                                }
                            }
                            map.put("subjects",vMap.get("subject_names"));
                            map.put("createTime",vMap.get("create_time"));
                            resultList.add(map);
                        });
                    }
                }
            }
        }catch (Exception e){
            logger.error("http request error :  url= " + sbf.toString() , e);
        }
        return resultList;
    }

    public Map<String,Object> getExamInfo(String examId){
        StringBuffer sbf = new StringBuffer(getBaseExamUrl());
        sbf.append("/exam/tianji_exams/detail?exam_id=").append(examId);
        Map<String,Object> result = new HashMap<>();

        Map<String,Object> basicMap = new HashMap<>();
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(sbf.toString()).execute();
            if (response.getStatusCode() == 200) {
                Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                if (MapUtils.isNotEmpty(resultMap)) {
                    Object body = resultMap.get("body");
                    if (body != null) {
                        Set<Integer> regionCodes = new HashSet<>();
                        Map<Long,Integer> schoolRegoinCode = new HashMap<>();
                        Map<String, Object> dataMap = (Map<String, Object>) body;
                        basicMap.put("examId",dataMap.get("exam_id"));
                        basicMap.put("schoolId",dataMap.get("school_id"));
                        School school = schoolLoaderClient.getSchoolLoader().loadSchool(SafeConverter.toLong(dataMap.get("school_id"))).getUninterruptibly();
                        basicMap.put("regionCode",school == null ? 0 : school.getRegionCode());
                        basicMap.put("schoolName",school == null ? "" : school.getCname());
                        if(school != null){
                            regionCodes.add(school.getRegionCode());
                            schoolRegoinCode.put(school.getId(),school.getRegionCode());
                        }
                        Long creatorId = SafeConverter.toLong(dataMap.get("creator"));
                        Teacher teacher = teacherLoaderClient.loadTeacher(creatorId);
                        basicMap.put("creator",teacher == null ? "" : teacher.getProfile().getRealname());
                        basicMap.put("name",dataMap.get("name"));
                        basicMap.put("type",dataMap.get("type"));
                        basicMap.put("grade",dataMap.get("grade"));
                        Object schoolIds = dataMap.get("school_ids");
                        basicMap.put("schoolNum",1);
                        if(schoolIds != null){
                            List<Long> schoolIdList = (List<Long>)schoolIds;
                            basicMap.put("schoolIds",schoolIdList);
                            Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                                        .loadSchools(schoolIdList)
                                        .getUninterruptibly();
                            List<String> schoolNames = new ArrayList<>();

                            schoolMap.values().forEach(s->{
                                schoolNames.add(s.getCname());
                                regionCodes.add(s.getRegionCode());
                                schoolRegoinCode.put(s.getId(),s.getRegionCode());
                            });
                            if(schoolNames.size() == 0 ){
                                schoolNames.add(school == null ? "" : school.getCname());
                            }

                            basicMap.put("schoolNames",schoolNames);
                            basicMap.put("schoolNum",schoolIdList.size());
                        }
                        basicMap.put("regionCodes",regionCodes);
                        basicMap.put("schoolRegoinCode",schoolRegoinCode);
                        basicMap.put("subjects",dataMap.get("subject_names"));
                        basicMap.put("createTime",dataMap.get("create_time"));

                        result.put("examBasic",basicMap);
                        Object sacnObj = dataMap.get("scan_info");
                        if(sacnObj != null){
                            List<Map<String,Object>> scanList = (List<Map<String,Object>>)sacnObj;
                            scanList.forEach(p->{
                                School school1 = schoolLoaderClient.getSchoolLoader().loadSchool(SafeConverter.toLong(p.get("school_id"))).getUninterruptibly();
                                p.put("schoolName",school1 == null ? "":school1.getCname());
                            });
                            result.put("scan_info",scanList);
                        }else{
                            result.put("scan_info",Collections.emptyList());
                        }
                    }
                }else {
                    return Collections.emptyMap();
                }
            }else {
                return Collections.emptyMap();
            }
        }catch (Exception e){
            logger.error("http request error :  url= " + sbf.toString() , e);
            return Collections.emptyMap();
        }
        result.put("marketers",agentExamUserInfoDao.loadByExamId(examId));//市场服务人员列表
        List<AgentExamDistribution> agentExamDistributions = agentExamDistributionDao.findByExamId(examId);
        if(CollectionUtils.isNotEmpty(agentExamDistributions)){//有分配记录
            if(agentExamDistributions.get(0).getAgentId() != null){//代理商信息
                AgentUser agentUser = baseOrgService.getUser(agentExamDistributions.get(0).getAgentId());
                result.put("agentName",agentUser == null ? "" : agentUser.getRealName());
                result.put("agentId",agentUser == null ? -1 : agentUser.getId());
            }
        } else{//无分配记录时 1 创建记录 状态都先写成未评价 未分配  2 找出一个默认代理商
            //没分配时代理商没地方保存  但是产品又要保存 先保存分配记录
            if(CollectionUtils.isEmpty(agentExamDistributions)){
                agentExamDistributions = this.saveAgentExamDistribution(examId,result,false);
            }
            Object objIds = basicMap.get("schoolIds");
            List<Long> schoolIds = new ArrayList<>();
            if(objIds != null){
                schoolIds = (List<Long>)objIds;
            }
            Long schoolId = SafeConverter.toLong(basicMap.get("schoolId"));
            if(schoolIds.size() == 0 ){
                schoolIds.add(schoolId);
            }
            List<AgentExamContract> contracts = agentExamContractPersistence.loadBySchoolIds(schoolIds);
            Set<Long> contractIds = contracts.stream().map(AgentExamContract::getId).collect(Collectors.toSet());
            Map<Long, AgentExamContractExtend> contractExtendMap = agentExamContractExtendDao.loadByContractIds(contractIds);
            //拼装签约人信息
            List<AgentExamContractSplitSetting> contractSplitSettingList = new ArrayList<>();
            contractExtendMap.values().forEach(item -> {
                if (null != item){
                    contractSplitSettingList.addAll(item.getSplitSettingList());
                }
            });
            //签约人id
            Set<Long> contractorIds = contractSplitSettingList.stream().map(AgentExamContractSplitSetting::getContractorId).collect(Collectors.toSet());

            if(CollectionUtils.isNotEmpty(contractorIds)){
                Set<Long> agentSet = new HashSet<>();
                Map<Long, List<Integer>> userRoles = baseOrgService.getGroupUserRoleMapByUserIds(contractorIds);
                userRoles.forEach((k,v) ->{
                    v.forEach(r ->{
                        if(AgentRoleType.ProvinceAgent.getId() == r || AgentRoleType.CityAgent.getId() == r || AgentRoleType.CityAgentLimited.getId() == r){
                            agentSet.add(k);
                        }
                    });
                });
                if(agentSet.size() == 1){
                    AgentUser agentUser = baseOrgService.getUser(agentSet.iterator().next());
                    if(CollectionUtils.isNotEmpty(agentExamDistributions)){
                        if(agentExamDistributions.get(0).getAgentId() == null || agentExamDistributions.get(0).getAgentId() <= 0){
                            agentExamDistributions.forEach(p->{
                                p.setAgentName(agentUser.getRealName());
                                p.setAgentId(agentUser.getId());
                                agentExamDistributionDao.upsert(p);
                                result.put("agentName",agentUser.getRealName());
                                result.put("agentId",agentUser.getId());
                            });
                        }
                    }
                }

            }
        }
        if(CollectionUtils.isNotEmpty(agentExamDistributions)){
            result.put("scanType",agentExamDistributions.get(0) == null ? 1 : agentExamDistributions.get(0).getScanType());
        }else {
            result.put("scanType",1);
        }

        return result;
    }

    //分配考试到专员
    public MapMessage saveDistributionUser(String examId , Collection<Long> userIds){
        if(!permitEdit(examId)){
            return MapMessage.errorMessage("超出编辑时限不可编辑！");
        }
        List<AgentUser> users = baseOrgService.getUsers(userIds);

        Map<String,Object> mapInfo= getExamInfo(examId);
        if(MapUtils.isEmpty(mapInfo)){
            return MapMessage.errorMessage().add("info","考试不存在");
        }
        Map<String,Object> examBasic =(Map<String,Object>)mapInfo.get("examBasic");
        List<AgentExamUserInfo> list = agentExamUserInfoDao.loadByExamId(examId);
        //保存记录到分配记录表
        List<AgentExamDistribution> agentExamDistributions = agentExamDistributionDao.findByExamId(examId);
        if(CollectionUtils.isEmpty(users)){
            cleanDistributionUser(list,agentExamDistributions);
            return MapMessage.successMessage();
        }
        List<Long> oldUserIds = list.stream().map(s -> s.getUserId()).collect(Collectors.toList());
        List<Long> newUserIds = users.stream().map(u->u.getId()).collect(Collectors.toList());
        users.forEach(p->{
            if(!oldUserIds.contains(p.getId())){
                //新增的人员
                AgentExamUserInfo agentExamUserInfo = new AgentExamUserInfo();
                agentExamUserInfo.setUserId(p.getId());
                agentExamUserInfo.setExamId(examId);
                agentExamUserInfo.setRealName(p.getRealName());
                agentExamUserInfo.setDisabled(false);
                agentExamUserInfo.setEvaluateState(false);
                agentExamUserInfo.setGrade(SafeConverter.toInt(examBasic.get("grade")));
                agentExamUserInfo.setName(SafeConverter.toString(examBasic.get("name")));
                list.add(agentExamUserInfo);
            }
        });
        if(!CollectionUtils.isEmpty(list)){//第一次分配时
            list.forEach( u-> {
                if(!newUserIds.contains(u.getUserId())){
                    u.setDisabled(true);
                }
            });
        }
        list.forEach(p-> agentExamUserInfoDao.upsert(p));
        //如果全部为新服务人员 则把分配记录改成未评价状态
        boolean allNewFlag = false;
        for (AgentExamUserInfo info : list){
            if(info.getDisabled() == false && info.getEvaluateState() != null && info.getEvaluateState() == true){
                allNewFlag = true;
            }
        }

        if(CollectionUtils.isEmpty(agentExamDistributions)){
            saveAgentExamDistribution(examId,mapInfo,true);
        }else{
            boolean finalAllNewFlag = allNewFlag;
            agentExamDistributions.forEach(p-> {
                p.setEvaluateState(finalAllNewFlag);
                p.setDistributionState(true);
                agentExamDistributionDao.upsert(p);
            });
        }
        return MapMessage.successMessage();
    }

    private List<AgentExamDistribution> saveAgentExamDistribution(String examId,Map<String,Object> mapInfo,Boolean distributionState){
        List<AgentExamDistribution> list = new ArrayList<>();
        Map<String,Object> examBasic =(Map<String,Object>)mapInfo.get("examBasic");
        if(examBasic != null){
            Set<Integer> regionCodes = (Set<Integer>)examBasic.get("regionCodes");
            Map<Long,Integer> schoolRegion = (Map<Long,Integer>)examBasic.get("schoolRegoinCode");
            if(MapUtils.isNotEmpty(schoolRegion)){
                schoolRegion.forEach((k,v)->{
                    AgentExamDistribution agentExamDistribution = new AgentExamDistribution();
                    agentExamDistribution.setEvaluateState(false);
                    agentExamDistribution.setExamId(examId);
                    agentExamDistribution.setGrade(SafeConverter.toInt(examBasic.get("grade")));
                    agentExamDistribution.setName(SafeConverter.toString(examBasic.get("name")));
                    agentExamDistribution.setRegionCode(v);
                    agentExamDistribution.setSchoolId(k);
                    agentExamDistribution.setExamCreateDate(SafeConverter.toDate(SafeConverter.toLong(examBasic.get("createTime"))*1000));
                    agentExamDistribution.setDistributionState(distributionState);
                    agentExamDistribution.setScanType(1);
                    agentExamDistribution.setEvaluateState(false);
                    agentExamDistribution.setDisabled(false);
                    list.add(agentExamDistributionDao.upsert(agentExamDistribution));
                });
            }
        }
        return list;
    }

    //更新用户评价
    public MapMessage saveEvaluateStateUser(String examId,Long userId,String level,String desc){
        AgentExamUserInfo agentExamUserInfo = agentExamUserInfoDao.loadByExamIdAndUserId(examId,userId);
        if(agentExamUserInfo == null){
            return MapMessage.errorMessage("未查到市场人员 {} 本场考试的分配信息",userId);
        }
        agentExamUserInfo.setLevel(level);
        agentExamUserInfo.setEvaluateState(true);
        agentExamUserInfo.setDesc(desc);
        agentExamUserInfoDao.upsert(agentExamUserInfo);

        //更新考试信息
        List<AgentExamDistribution> agentExamDistributions = agentExamDistributionDao.findByExamId(examId);
        if(CollectionUtils.isNotEmpty(agentExamDistributions)){
            agentExamDistributions.forEach(p->{
                p.setEvaluateState(true);
                agentExamDistributionDao.upsert(p);
            });

        }
        return MapMessage.successMessage();
    }
    /**
     * 获取中学下的专员和市经理
     * @return
     */
    public List<Map<String,Object>> businessDeveloperList(){
        List<Map<String,Object>> dataList = new ArrayList<>();
        //获取用户部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();
        AgentGroup middleSchoolGroup = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) ||
                p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
        if(middleSchoolGroup == null){
            return Collections.emptyList();
        }
        List<AgentGroup> cityGroupList = baseOrgService.getSubGroupList(middleSchoolGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());

        Set<Long> groupIds = cityGroupList.stream().filter(Objects::nonNull).map(AgentGroup::getId).collect(Collectors.toSet());
        //获取指定部门（多个）中，指定角色的用户
        List<Long> userIds = baseOrgService.getUserByGroupIdsAndRoles(groupIds, Arrays.asList(AgentRoleType.BusinessDeveloper,AgentRoleType.CityManager));

        List<AgentUser> userList = baseOrgService.getUsers(userIds);
        //根据用户姓名首字母分组
        Map<String, List<AgentUser>> firstCapitalUserMap = userList.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getRealName())));

        //用户姓名首字母排序
        List<String> sortedFirstCapital = firstCapitalUserMap.keySet().stream().sorted(Comparator.comparing(item -> item == null ? "" : item, Collator.getInstance(Locale.CHINA))).collect(Collectors.toList());

        sortedFirstCapital.forEach(item -> {
            Map<String,Object> dataMap = new HashMap<>();
            List<Map<String,Object>> userMapList = new ArrayList<>();
            List<AgentUser> agentUserList = firstCapitalUserMap.get(item);
            if (CollectionUtils.isNotEmpty(agentUserList)){
                agentUserList.forEach(user -> {
                    Map<String,Object> userMap = new HashMap<>();
                    userMap.put("userId",user.getId());
                    userMap.put("realName",user.getRealName());
                    userMapList.add(userMap);
                });
            }
            dataMap.put("firstCapital",item);
            dataMap.put("userList",userMapList);
            dataList.add(dataMap);
        });
        return dataList;
    }
    /**
     * 获取所有代理商列表
     * @return
     */
    public List<Map<String,Object>> getAgentUserList(){
        Set<Long> agentIds = baseOrgService.getGroupUserByRole(AgentRoleType.CityAgent.getId())
                .stream()
                .map(AgentGroupUser::getUserId)
                .collect(Collectors.toSet());
        List<AgentUser> users = baseOrgService.getUsers(agentIds);
        List<Map<String,Object>> agentList = new ArrayList<>();
        users.forEach(p->{
            Map<String,Object> map = new HashMap<>();
            map.put("id",p.getId());
            map.put("realName",p.getRealName());
            agentList.add(map);
        });
        return agentList;
    }

    //标记代理
    public MapMessage updateExamAgentUser(String examId,Long agentId,Integer signType){
        if(signType != null && signType == 1){//标记代理
            List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(agentId);
            List<Integer> roleIds = groupUsers.stream().map(AgentGroupUser::getUserRoleId).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(groupUsers) || !(roleIds.contains(AgentRoleType.CityAgent.getId()) ||
                    roleIds.contains(AgentRoleType.ProvinceAgent.getId()) || roleIds.contains(AgentRoleType.CityAgent.getId()))){
                MapMessage.errorMessage().add("info","请选择正确代理");
            }
            //更新考试信息
            AgentUser agentUser = baseOrgService.getUser(agentId);
            List<AgentExamDistribution> agentExamDistributions = agentExamDistributionDao.findByExamId(examId);
            if(CollectionUtils.isNotEmpty(agentExamDistributions)){
                agentExamDistributions.forEach(p->{
                    p.setAgentId(agentUser.getId());
                    p.setAgentName(agentUser.getRealName());
                    agentExamDistributionDao.upsert(p);
                });

            }
        }else {
            List<AgentExamDistribution> agentExamDistributions = agentExamDistributionDao.findByExamId(examId);
            if(CollectionUtils.isNotEmpty(agentExamDistributions)){
                agentExamDistributions.forEach(p->{
                    p.setAgentId(-1l);
                    p.setAgentName("");
                    agentExamDistributionDao.upsert(p);
                });

            }
        }

        return MapMessage.successMessage();
    }


    //标记代理
    public MapMessage updateExamSacnType(String examId,Integer scanType){
        if(scanType > 3 || scanType < 1){
            return MapMessage.errorMessage().add("info","扫描类型不正确");
        }
        List<AgentExamDistribution> agentExamDistributions = agentExamDistributionDao.findByExamId(examId);
        if(CollectionUtils.isNotEmpty(agentExamDistributions)){
            agentExamDistributions.forEach(p->{
                p.setScanType(scanType);
                agentExamDistributionDao.upsert(p);
            });

        }
        return MapMessage.successMessage();
    }
    private String getBaseExamUrl(){
        String url = "http://10.6.3.241:3100";
        if(RuntimeMode.isDevelopment() || RuntimeMode.isTest()){
            url = "http://10.200.3.16:3102";
        }else if(RuntimeMode.isStaging()){
            url = "http://10.6.15.81:3100";
        }
        return url;
    }

    //是否允许编辑  全国  admin 不限制   渠道 成绩发布后7天内
    private boolean permitEdit(String examId){
        Long userId = getCurrentUserId();
        List<AgentGroup> groups = baseOrgService.getUserGroups(userId);
        if(CollectionUtils.isEmpty(groups)){
            return false;
        }
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        if(AgentRoleType.Admin == roleType || roleType ==AgentRoleType.Country){
            return true;
        }else if(roleType == AgentRoleType.ChannelDirector || roleType == AgentRoleType.ChannelManager || roleType == AgentRoleType.ChannelOperator){
            StringBuffer sbf = new StringBuffer(getBaseExamUrl());
            sbf.append("/exam/tianji_exams/detail?exam_id=").append(examId);
            Map<String,Object> result = new HashMap<>();
            List<Date> publishTimeList = new ArrayList<>();
            try {
                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(sbf.toString()).execute();
                if (response.getStatusCode() == 200) {
                    Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                    if (MapUtils.isNotEmpty(resultMap)) {
                        Object body = resultMap.get("body");
                        if (body != null) {
                            Map<String, Object> dataMap = (Map<String, Object>) body;
                            Object subjectNamesObj = dataMap.get("subjects");
                            if(subjectNamesObj != null){
                                List<String> subjects = (List<String>)subjectNamesObj;
                                subjects.forEach( s->{
                                    Map<String,Object> map = (Map<String,Object>)dataMap.get(s);
                                    if(map != null){
                                        boolean scorePublished = SafeConverter.toBoolean(map.get("score_published"));
                                        if(scorePublished){
                                            Date score_published_time = SafeConverter.toDate(map.get("score_published_time"));
                                            publishTimeList.add(score_published_time);
                                        }
                                    }
                                });
                            }

                        }
                    }
                }
            }catch (Exception e){
                logger.error("http request error :  url= " + sbf.toString() , e);
            }
            if(publishTimeList.size() > 0){
                Collections.sort(publishTimeList);
                return DateUtils.calculateDateDay(new Date(),-7).compareTo(publishTimeList.get(0)) >= 0;
            }else {//没有成绩发布时间不限制
                return true;
            }
        }else{
            return false;
        }
    }

    private void cleanDistributionUser(List<AgentExamUserInfo> list,List<AgentExamDistribution> agentExamDistributions){
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(u->{
                u.setDisabled(true);
                agentExamUserInfoDao.upsert(u);
            });
        }
        if(CollectionUtils.isNotEmpty(agentExamDistributions)){
            agentExamDistributions.forEach(e->{
                e.setDisabled(true);
                agentExamDistributionDao.upsert(e);
            });
        }
    }
}
