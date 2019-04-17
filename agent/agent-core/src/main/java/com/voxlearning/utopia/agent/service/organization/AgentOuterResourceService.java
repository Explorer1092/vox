/**
 * Author:   xianlong.zhang
 * Date:     2018/12/13 20:26
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.service.organization;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.outerresource.AgentOuterResourceView;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.dao.mongo.AgentResearchersUpdateLogDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserResearchersDao;
import com.voxlearning.utopia.agent.dao.mongo.organization.AgentOuterResourceApplyDao;
import com.voxlearning.utopia.agent.dao.mongo.organization.AgentOuterResourceExtendDao;
import com.voxlearning.utopia.agent.persist.AgentOrganizationPersistence;
import com.voxlearning.utopia.agent.persist.AgentOuterResourcePersistence;
import com.voxlearning.utopia.agent.persist.AgentResearchersPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchersUpdateLog;
import com.voxlearning.utopia.agent.persist.entity.AgentUserResearchers;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOrganization;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResource;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResourceApply;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResourceExtend;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.AgentResearchersService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordOuterResourceLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AgentOuterResourceService extends AbstractAgentService {
    @Inject private AgentOuterResourcePersistence agentOuterResourcePersistence;
    @Inject private AgentOuterResourceExtendDao agentOuterResourceExtendDao;
    @Inject private AgentUserResearchersDao agentUserResearchersDao;
    @Inject private AgentResearchersService agentResearchersService;
    @Inject private AgentOrganizationPersistence agentOrganizationPersistence;
    @Inject private AgentResearchersPersistence agentResearchersPersistence;
    @Inject private AgentResearchersUpdateLogDao agentResearchersUpdateLogDao;
    @Inject private AgentOrganizationService agentOrganizationService;
    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentNotifyService agentNotifyService;
    @Inject private AgentOuterResourceApplyDao agentOuterResourceApplyDao;
    @Inject private WorkRecordOuterResourceLoader workRecordOuterResourceLoader;
    @Inject private AgentTagService agentTagService;
    /**
     * 根据机构查询下外部资源或学校资源
     * @param organizations
     * @param name  资源姓名
     * @return
     */
    public List<Map<String,Object>> assembleList(Collection<AgentOrganization> organizations,String name){
        if(CollectionUtils.isEmpty(organizations)){
            return Collections.emptyList();
        }
        List<AgentUserResearchers> userResearchers = agentUserResearchersDao.findUserResearchersByAgentUserId(getCurrentUserId());
        List<Long> userResearcherIds = userResearchers.stream().map(AgentUserResearchers :: getResearchersId).collect(Collectors.toList());
        //上层资源
        List<Long> outerOrgIds = organizations.stream().map(AgentOrganization::getId).collect(Collectors.toList());
//        List<Long> allOrgIds = organizations.stream().filter(p -> p.getOrgType() == 1).map(AgentOrganization::getId).collect(Collectors.toList());
        List<Long> schoolOrgIds = organizations.stream().filter(p -> p.getOrgType() != null && p.getOrgType() == 2).map(AgentOrganization::getId).collect(Collectors.toList());
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long,List<AgentOuterResourceExtend>> extendMap = agentOuterResourceExtendDao.findListByOrganizationIds(outerOrgIds);
        List<Long> outerResourceIds = extendMap.values().stream().flatMap(List :: stream).map(AgentOuterResourceExtend::getResourceId).collect(Collectors.toList());
        List<AgentOuterResource> resourceList = agentOuterResourcePersistence.findListByIdsAndName(outerResourceIds,name);
        Map<Long,AgentOrganization> orgMap = organizations.stream().collect(Collectors.toMap(AgentOrganization :: getId,Function.identity(), (o1, o2) -> o1));
        Map<Long,AgentOuterResourceExtend> resourceExtendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(outerResourceIds);
        resourceList.forEach(p -> {
            AgentOuterResourceExtend extend = resourceExtendMap.get(p.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("dataFlag", "new");
            data.put("name", p.getName());
            data.put("id", p.getId());
            data.put("gender", p.getGender());
            data.put("gradeStr", extend != null ? extend.getGradeStr() : "");
            //已加入私海 或者学校资源不加锁
            boolean isPrivate = userResearcherIds.contains(p.getId()) || (extend != null && schoolOrgIds.contains(extend.getOrganizationId()));
            if(isPrivate || agentResearchersService.isManager()){
                data.put("phone", p.getPhone());
                data.put("telephone", p.getTelephone());
                data.put("lockStatus", false);
            }else {
                data.put("telephone", "");
                data.put("phone", "");
                data.put("lockStatus", true);
            }

            data.put("organizationId",extend != null ? extend.getOrganizationId() : "");
            data.put("organizationName",extend != null ? orgMap.get(extend.getOrganizationId()) == null ? "" : orgMap.get(extend.getOrganizationId()).getName() : "");
            data.put("department",extend != null ? extend.getDepartment() : "");
            data.put("job", extend != null ? extend.getJob() : "");
            ResearchersJobType researchersJobType = extend == null ? null : ResearchersJobType.typeOf(extend.getJob());
            data.put("jobName", researchersJobType != null ? researchersJobType.getJobName() : "");
//                List<CrmWorkRecord> workRecords = loadResearchersRecordsByResearchersId(userId,p.getId());
            //TODO 写完新的拜访的话 这里要做旧数据兼容
            List<CrmWorkRecord> workRecords = agentResearchersService.findResearcherVisitRecord(p.getId());
            List<Map<String, Object>> newWorkRecordList = workRecordOuterResourceLoader.resourceVisitList(p.getId());
            if(CollectionUtils.isNotEmpty(newWorkRecordList)){
                data.put("visitTime", newWorkRecordList.get(0).get("workTime"));
            }else {
                data.put("visitTime", workRecords.size() > 0 ? workRecords.get(0).getCreateTime() : null);
            }

            data.put("subject", extend != null ? (extend.getSubject() == null ? Subject.UNKNOWN.getId() : extend.getSubject().getId()) : Subject.UNKNOWN.getId());
            data.put("subjectName", extend != null ? (extend.getSubject() == null ? Subject.UNKNOWN.getValue() : extend.getSubject().getValue()) : Subject.UNKNOWN.getValue());
            data.put("provinceName", extend != null ? orgMap.get(extend.getOrganizationId()) == null ? "" : orgMap.get(extend.getOrganizationId()).getProvinceName() : "");
            data.put("cityName", extend != null ? orgMap.get(extend.getOrganizationId()) == null ? "" : orgMap.get(extend.getOrganizationId()).getCityName() : "");
            data.put("countyName", extend != null ? orgMap.get(extend.getOrganizationId()) == null ? "" : orgMap.get(extend.getOrganizationId()).getCountyName() : "");
            data.put("regionRank", extend != null ? orgMap.get(extend.getOrganizationId()) == null ? "" : orgMap.get(extend.getOrganizationId()).getRegionRank().getId() : "");
            result.add(data);
        });
        return result;
    }

    public MapMessage upsertOuterResource(Long id, String name, Integer gender, String phone, Integer job, String gradeStr, Subject subject,
                                          String specificJob, String telephone, Long organizationId, String department,
                                          String weChatOrQq,String email,List<String> photoUrls) {

        AgentOuterResource outerResource ;
        AgentOuterResourceExtend outerResourceExtend ;
        AgentOuterResource oldResource = null;
        AgentOuterResourceExtend oldExtend = null;
        boolean dataFlag =isNewResource(id);
        if (id != null && id != 0L) {
            outerResource = agentOuterResourcePersistence.load(id);
            oldResource = agentOuterResourcePersistence.load(id);
            AgentResearchers agentResearchers = agentResearchersPersistence.load(id);
            if(!dataFlag){
                if(agentResearchers == null){
                    return MapMessage.errorMessage("该资源不存在");
                }else {
                    agentResearchers.setDisabled(true);
                    agentResearchersPersistence.upsert(agentResearchers);
                    if (outerResource == null) {
                        outerResource = new AgentOuterResource();
                    }
                }
                outerResource.setId(id);
            }

            Map<Long,AgentOuterResourceExtend> extendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(Collections.singleton(id));
            if(extendMap.get(id) != null){
                outerResourceExtend = extendMap.get(id);
                oldExtend = extendMap.get(id);
            }else{
                outerResourceExtend = new AgentOuterResourceExtend();
            }
        }else{
            outerResource = new AgentOuterResource();
            outerResourceExtend = new AgentOuterResourceExtend();
        }
        outerResource.setAgentUserId(getCurrentUserId());
        outerResource.setName(name);
        outerResource.setGender(gender);
        outerResource.setDisabled(false);
        outerResource.setPhone(phone);
        outerResource.setTelephone(telephone);

        outerResourceExtend.setGradeStr(gradeStr);
        outerResourceExtend.setSubject(subject);
        outerResourceExtend.setJob(job);
        outerResourceExtend.setRemarks(specificJob);
        outerResourceExtend.setDisabled(false);
        outerResourceExtend.setWeChatOrQq(weChatOrQq);
        outerResourceExtend.setEmail(email);
        outerResourceExtend.setPhotoUrls(photoUrls);

        outerResourceExtend.setOrganizationId(organizationId);
        outerResourceExtend.setDepartment(department);
        if(dataFlag){
            saveUpdateLog(getCurrentUser().getUserId(),getCurrentUser().getRealName(), oldResource, oldExtend, outerResource, outerResourceExtend);
        }


        agentOuterResourcePersistence.upsert(outerResource);
        outerResourceExtend.setResourceId(outerResource.getId());

        agentOuterResourceExtendDao.upsert(outerResourceExtend);

        List<AgentUserResearchers> userResearchers = agentUserResearchersDao.findByUserIdAndResearchersId(getCurrentUserId(),outerResource.getId());
        if(CollectionUtils.isEmpty(userResearchers)){
            AgentUserResearchers agentUserResearchers= new AgentUserResearchers();
            agentUserResearchers.setDisabled(false);
            agentUserResearchers.setResearchersId(outerResource.getId());
            agentUserResearchers.setUserId(getCurrentUserId());
            agentUserResearchersDao.insert(agentUserResearchers);
        }
        return MapMessage.successMessage();
    }

    public void saveUpdateLog(Long userId,String userName,AgentOuterResource oldResource, AgentOuterResourceExtend oldExtend,
                              AgentOuterResource newResource, AgentOuterResourceExtend newExtend){
        List<Map<String,Object>> updateItems = changedItems( oldResource,  oldExtend,  newResource, newExtend);
        if(updateItems.size() ==0){
            return;
        }
        AgentResearchersUpdateLog agentResearchersUpdateLog = new AgentResearchersUpdateLog();
        agentResearchersUpdateLog.setUserId(userId);
        agentResearchersUpdateLog.setUpdateName(userName);
        agentResearchersUpdateLog.setResearchersId(oldResource.getId());
        agentResearchersUpdateLog.setUpdateItems(updateItems);
        agentResearchersUpdateLogDao.insert(agentResearchersUpdateLog);
    }
    private List<Map<String,Object>> changedItems(AgentOuterResource oldResource, AgentOuterResourceExtend oldExtend, AgentOuterResource newResource, AgentOuterResourceExtend newExtend){
        List<Map<String,Object>> result = new ArrayList<>();
        if(!Objects.equals(oldResource.getPhone() ,newResource.getPhone())){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("phone",oldResource.getPhone(),newResource.getPhone(),map);
            result.add(map);
        }
        if(oldExtend.getJob() != newExtend.getJob()){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("job",oldExtend.getJob(),newExtend.getJob(),map);
            result.add(map);
        }
        if(oldExtend.getSubject().getId() != newExtend.getSubject().getId()){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("subject",oldExtend.getSubject().getValue(),newExtend.getSubject().getValue(),map);
            result.add(map);
        }
        if(!Objects.equals(oldExtend.getGradeStr(),newExtend.getGradeStr())){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("gradeStr",oldExtend.getGradeStr(),newExtend.getGradeStr(),map);
            result.add(map);
        }
        if(!Objects.equals(oldResource.getName(),newResource.getName())){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("name",oldResource.getName(),newResource.getName(),map);
            result.add(map);
        }

        if(oldResource.getGender() != newResource.getGender()){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("gender",oldResource.getGender(),newResource.getGender(),map);
            result.add(map);
        }
        if(!(StringUtils.isNotEmpty(oldExtend.getRemarks())) && StringUtils.isNotBlank(newExtend.getRemarks()) &&Objects.equals(newExtend.getRemarks(),newExtend.getRemarks())){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("specificJob",oldExtend.getRemarks(),newExtend.getRemarks(),map);
            result.add(map);
        }
        if(!Objects.equals(oldExtend.getDepartment(),newExtend.getDepartment())){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("department",oldExtend.getDepartment(),newExtend.getDepartment(),map);
            result.add(map);
        }
        if(!Objects.equals(oldExtend.getOrganizationId(),newExtend.getOrganizationId())){
            Map<String,Object> map = new HashMap<>();
            agentResearchersService.saveVal("organizationId",oldExtend.getOrganizationId(),newExtend.getOrganizationId(),map);
            result.add(map);
        }
        return result;
    }

    public MapMessage loadOuterResourceById(Long id){
        MapMessage mapMessage = MapMessage.successMessage();
        AgentOuterResource resource = agentOuterResourcePersistence.load(id);
        AgentOuterResourceExtend resourceExtend = null;
        Map<Long,AgentOuterResourceExtend> resourceExtendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(Collections.singleton(id));
        if(MapUtils.isNotEmpty(resourceExtendMap)){
            resourceExtend = resourceExtendMap.get(id);
        }
        if(resourceExtend == null){
            resourceExtend = new AgentOuterResourceExtend();
        }
        if (resource != null) {
            boolean isManager = agentResearchersService.isManager();
            boolean isPrivate = agentResearchersService.isPrivateResearchers(getCurrentUserId(),id);
            boolean isSchoolResource = isSchoolResource(id);
            if(!isPrivate && !isManager && !isSchoolResource){
                return MapMessage.errorMessage("无权限不可编辑");
            }
        }else {
            resource = new AgentOuterResource();
        }

        Map<String,Object> map = new HashMap<>();

        map.put("id", resource.getId());
        map.put("name", resource.getName());
        map.put("gender", resource.getGender());
        map.put("phone", resource.getPhone());
        map.put("telephone", resource.getTelephone());
        map.put("specificJob",resourceExtend.getRemarks());
        map.put("department",resourceExtend.getDepartment());
        map.put("organizationId",resourceExtend.getOrganizationId());
        AgentOrganization agentOrganization = agentOrganizationPersistence.load(resourceExtend.getOrganizationId());
        map.put("organizationName",agentOrganization == null ? "" : agentOrganization.getName() );

        map.put("job", resourceExtend.getJob());
        map.put("subject", resourceExtend.getSubject() == null ? null : resourceExtend.getSubject().getId());
        map.put("subjectName", resourceExtend.getSubject() == null ? null : resourceExtend.getSubject().getValue());
        map.put("gradeStr", resourceExtend.getGradeStr());
        map.put("weChatOrQq",resourceExtend == null ? "" : resourceExtend.getWeChatOrQq());
        map.put("email",resourceExtend == null ? "" : resourceExtend.getEmail());
        map.put("photoUrls",resourceExtend == null ? "" : resourceExtend.getPhotoUrls());
        map.put("schoolId",agentOrganization == null ? "" : agentOrganization.getSchoolId());
        ResearchersJobType researchersJobType = ResearchersJobType.typeOf(resourceExtend.getJob());
        map.put("grResource",ResearchersJobType.sgrResourceJobType(researchersJobType));
//        map.put("sameSchoolPhase",agentResearchersService.researcherIsSamePhase(researchers.getGradeStr()));
        return mapMessage.add("dataMap",map);
    }

    public MapMessage getOuterResourceInfo(Long id){
        MapMessage mapMessage = MapMessage.successMessage();
        AgentOuterResource resource = agentOuterResourcePersistence.load(id);
        AgentOuterResourceExtend resourceExtend = null;
        Map<Long,AgentOuterResourceExtend> resourceExtendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(Collections.singleton(id));
        if(MapUtils.isNotEmpty(resourceExtendMap)){
            resourceExtend = resourceExtendMap.get(id);
        }
        if (resource != null) {
            Map<String,Object> resultMap = new HashMap<>();
            boolean isPrivate = agentResearchersService.isPrivateResearchers(getCurrentUserId(),id);
            if((isPrivate && agentResearchersService.isBusinessDeveloper()) || agentResearchersService.isManager()){
                MapMessage.errorMessage("没有权限查看数据");
            }
            resultMap.put("id",resource.getId());
            resultMap.put("name",resource.getName());
            resultMap.put("gender",resource.getGender());

            resultMap.put("phone",resource.getPhone());
            resultMap.put("telephone",resource.getTelephone());
            resultMap.put("subject",resourceExtend == null ? "" : resourceExtend.getSubject().getId());
            resultMap.put("subjectName",resourceExtend == null ? "" : resourceExtend.getSubject().getValue());
            resultMap.put("gradeStr",resourceExtend == null ? "" : resourceExtend.getGradeStr());
            resultMap.put("job",resourceExtend == null ? "" : resourceExtend.getJob());
            ResearchersJobType researchersJobType = resourceExtend == null ? null : ResearchersJobType.typeOf(resourceExtend.getJob());
            resultMap.put("jobName", researchersJobType != null ? researchersJobType.getJobName() : "");
            resultMap.put("department",resourceExtend == null ? "" : resourceExtend.getDepartment());
            AgentOrganization agentOrganization = agentOrganizationPersistence.load(resourceExtend == null ? 0L : resourceExtend.getOrganizationId());
            if(agentOrganization != null){
                resultMap.put("organizationName",agentOrganization.getName());
                resultMap.put("organizationId",agentOrganization.getId());
                resultMap.put("schoolId",agentOrganization.getSchoolId());
                resultMap.put("orgType",agentOrganization.getOrgType());
            }
            resultMap.put("weChatOrQq",resourceExtend == null ? "" : resourceExtend.getWeChatOrQq());
            resultMap.put("email",resourceExtend == null ? "" : resourceExtend.getEmail());
            resultMap.put("photoUrls",resourceExtend == null ? "" : resourceExtend.getPhotoUrls());
            List<Map<String, Object>> visitList = agentResearchersService.loadResearchersRecordsInfo(getCurrentUserId(), id);
            resultMap.put("visitList", visitList);
            resultMap.put("remarks",resourceExtend == null ? "" : resourceExtend.getRemarks());
            mapMessage.put("data",resultMap);
        }else {
            return MapMessage.errorMessage("信息不存在！");
        }
        return mapMessage;
    }

    public Map<Long,AgentOuterResource> getOuterResourceByIds(Collection<Long> ids) {
        return agentOuterResourcePersistence.findListByIds(ids);
    }

    public List<AgentOuterResourceExtend> getOuterResourceExtendByOrganizationId(Long schoolId) {
        //资源扩展信息
        AgentOrganization agentOrganization = agentOrganizationPersistence.loadBySchoolIdAndOrgType(schoolId);
        if(agentOrganization == null){
            return Collections.emptyList();
        }
        List<AgentOuterResourceExtend> reslut = Optional
                .ofNullable(agentOuterResourceExtendDao.findListByOrganizationIds(Collections.singletonList(agentOrganization.getId())))
                .orElse(new HashMap<>()).get(agentOrganization.getId());
        return Optional.ofNullable(reslut).orElse(new ArrayList<>());
    }

    /**
     * 根据学校ID获取上层资源列表
     * @param schoolId
     * @return
     */
    public List<Map<String,Object>> getOuterResourceListBySchoolId(Long schoolId){
        List<Map<String,Object>> dataList = new ArrayList<>();
        //资源扩展信息
        AgentOrganization agentOrganization = agentOrganizationPersistence.loadBySchoolIdAndOrgType(schoolId);
        if(agentOrganization == null){
            return Collections.emptyList();
        }
        Map<Long, List<AgentOuterResourceExtend>> schoolIdOuterResourceExtendListMap = agentOuterResourceExtendDao.findListByOrganizationIds(Collections.singleton(agentOrganization.getId()));
        List<AgentOuterResourceExtend> outerResourceExtendList = schoolIdOuterResourceExtendListMap.get(agentOrganization.getId());
        if (CollectionUtils.isEmpty(outerResourceExtendList)){
            return Collections.emptyList();
        }
        Map<Long, AgentOuterResourceExtend> outerResourceExtendMap = outerResourceExtendList.stream().collect(Collectors.toMap(AgentOuterResourceExtend::getResourceId, Function.identity(), (o1, o2) -> o1));
        //上层资源信息
        List<AgentOuterResource> outerResourceList = agentOuterResourcePersistence.findListByIdsAndName(outerResourceExtendMap.keySet(), null);
        outerResourceList.forEach(item -> {
            Map<String,Object> itemMap = new HashMap<>();
            itemMap.put("id",item.getId());
            itemMap.put("name",item.getName());

            AgentOuterResourceExtend outerResourceExtend = outerResourceExtendMap.get(item.getId());
            if (outerResourceExtend != null){
                itemMap.put("subject",outerResourceExtend.getSubject());
                itemMap.put("job",outerResourceExtend.getJob());
                ResearchersJobType researchersJobType = ResearchersJobType.typeOf(outerResourceExtend.getJob());
                itemMap.put("jobName",researchersJobType != null ? researchersJobType.getJobName() : "");
            }
            dataList.add(itemMap);
        });
        return dataList;
    }

    //关联外部资源与机构  只有旧有的教研员数据才关联
    public MapMessage linkResourceAndOrganization(Long resourceId,Long orgId){
        AgentOuterResource outerResource = agentOuterResourcePersistence.load(resourceId);
        AgentResearchers agentResearchers = agentResearchersPersistence.load(resourceId);
        AgentOrganization organization = agentOrganizationPersistence.load(orgId);
        if(outerResource != null){
            return MapMessage.errorMessage("该条记录不需要关联！");
        }
        if(agentResearchers == null || agentResearchers.getDisabled() == true){
            return MapMessage.errorMessage("资源信息不存在！");
        }
        if(organization == null){
            return MapMessage.errorMessage("部门信息信息不存在！");
        }
        AgentOuterResourceExtend outerResourceExtend = new AgentOuterResourceExtend();
        outerResource = new AgentOuterResource();
        outerResource.setId(resourceId);
        outerResource.setAgentUserId(getCurrentUserId());
        outerResource.setName(agentResearchers.getName());
        outerResource.setGender(agentResearchers.getGender());
        outerResource.setDisabled(false);
        outerResource.setPhone(agentResearchers.getPhone());


        outerResourceExtend.setGradeStr(agentResearchers.getGradeStr());
        outerResourceExtend.setSubject(agentResearchers.getSubject());
        outerResourceExtend.setJob(agentResearchers.getJob());
        outerResourceExtend.setRemarks(agentResearchers.getSpecificJob());
        outerResourceExtend.setDisabled(false);
        outerResourceExtend.setResourceId(resourceId);

        outerResourceExtend.setOrganizationId(orgId);

        agentResearchers.setDisabled(true);
        agentOuterResourcePersistence.insert(outerResource);
        agentOuterResourceExtendDao.insert(outerResourceExtend);
        agentResearchersPersistence.upsert(agentResearchers);
        return MapMessage.successMessage();
    }

    public List<Map<String,Object>> getUserOuterAndSchoolResource(Collection<Integer> provinceCodes,Collection<Integer> cityCodes,Collection<Integer> countyCodes,String name){
        List<Map<String,Object>> result = new ArrayList<>();
        List<AgentOrganization> organizations = agentOrganizationService.getListByRegionCodes(provinceCodes,cityCodes,countyCodes);
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        if( roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager) {
            List<Map<String,Object>> newOuterList = assembleList(organizations,name);
            if(CollectionUtils.isNotEmpty(newOuterList)){
                result.addAll(newOuterList);
            }
        }else if(roleType == AgentRoleType.BusinessDeveloper){//专员
            //上层资源
            List<AgentOrganization> outerOrgs = organizations.stream().filter(p -> p.getOrgType() != null && p.getOrgType() == 1).collect(Collectors.toList());
            List<Map<String,Object>> newOuterList = assembleList(outerOrgs,name);
            if(CollectionUtils.isNotEmpty(newOuterList)){
                result.addAll(newOuterList);
            }
            List<Long> schoolIds = baseOrgService.getManagedSchoolList(getCurrentUserId());
            //查学校资源
            List<AgentOrganization> schoolOrgs = organizations.stream().filter(p -> p.getOrgType() != null && p.getOrgType() == 2 && schoolIds.contains(p.getSchoolId())).collect(Collectors.toList());
            List<Map<String,Object>> schoolResourceList = assembleList(schoolOrgs,name);
            if(CollectionUtils.isNotEmpty(schoolResourceList)){
                result.addAll(schoolResourceList);
            }
        }
        return result;
    }

    public MapMessage pageResource(List<Map<String,Object>> result, Pageable pageable){
        int fromIndex = pageable.getOffset();
        int toIndex = 0;
        boolean hasNext = true;
        if (fromIndex + pageable.getPageSize() >= result.size()){
            toIndex = result.size();
            hasNext = false;
        }else {
            toIndex = fromIndex +  pageable.getPageSize();
        }
        if (fromIndex > toIndex){
            return MapMessage.successMessage().add("dataList",Collections.emptyList()).add("hasNext",false);
        }
        result = result.subList(fromIndex,toIndex);
        return MapMessage.successMessage().add("dataList",result).add("hasNext",hasNext);
    }

    public Boolean isSchoolResource(Long id){
        AgentOuterResource outerResource = agentOuterResourcePersistence.load(id);
        if(outerResource == null){
            return false;
        }
        Map<Long,AgentOuterResourceExtend> extendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(Collections.singleton(id));
        AgentOuterResourceExtend resourceExtend = extendMap.get(id);
        if(resourceExtend == null){
            return false;
        }
        AgentOrganization organization = agentOrganizationPersistence.load(resourceExtend.getOrganizationId());
        if(organization == null || Objects.equals(1,organization.getOrgType())){//空或机构资源
            return false;
        }
        if(Objects.equals(2,organization.getOrgType())){
            return true;
        }
        return false;
    }

    public MapMessage upsertSchoolResource(Long id, String name, Integer gender, String phone, Integer job, String gradeStr, Subject subject,
                                          String specificJob, String telephone, Long schoolId, String department,
                                          String weChatOrQq,String email,List<String> photoUrls) {
        AgentOrganization organization = agentOrganizationPersistence.loadBySchoolIdAndOrgType(schoolId);
        if(organization == null){
            MapMessage mapMessage= agentOrganizationService.createSchoolOrganization(schoolId);
            if(mapMessage.isSuccess()){
                organization = (AgentOrganization) mapMessage.get("org");
            }else {
                return MapMessage.errorMessage("学校信息有误！");
            }
        }
        AgentOuterResource outerResource ;
        AgentOuterResourceExtend outerResourceExtend ;
        AgentOuterResource oldResource = null;
        AgentOuterResourceExtend oldExtend = null;
        if (id != null && id != 0L) {
            outerResource = agentOuterResourcePersistence.load(id);
            oldResource = agentOuterResourcePersistence.load(id);
            Map<Long,AgentOuterResourceExtend> extendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(Collections.singleton(id));
            if(extendMap.get(id) != null){
                outerResourceExtend = extendMap.get(id);
                oldExtend = extendMap.get(id);
            }else{
                outerResourceExtend = new AgentOuterResourceExtend();
            }
        }else{
            outerResource = new AgentOuterResource();
            outerResourceExtend = new AgentOuterResourceExtend();
        }
        outerResource.setAgentUserId(getCurrentUserId());
        outerResource.setName(name);
        outerResource.setGender(gender);
        outerResource.setDisabled(false);
        outerResource.setPhone(phone);
        outerResource.setTelephone(telephone);

        outerResourceExtend.setGradeStr(gradeStr);
        outerResourceExtend.setSubject(subject);
        outerResourceExtend.setJob(job);
        outerResourceExtend.setRemarks(specificJob);
        outerResourceExtend.setDisabled(false);
        outerResourceExtend.setWeChatOrQq(weChatOrQq);
        outerResourceExtend.setEmail(email);
        outerResourceExtend.setPhotoUrls(photoUrls);

        outerResourceExtend.setOrganizationId(organization.getId());
        outerResourceExtend.setDepartment(department);
        if(id != null && id != 0L){
            saveUpdateLog(getCurrentUser().getUserId(),getCurrentUser().getRealName(), oldResource, oldExtend, outerResource, outerResourceExtend);
        }


        agentOuterResourcePersistence.upsert(outerResource);
        outerResourceExtend.setResourceId(outerResource.getId());

        agentOuterResourceExtendDao.upsert(outerResourceExtend);


        return MapMessage.successMessage();
    }

    public MapMessage getUserManagerInfo(){
        AgentUser manager = baseOrgService.getUserRealManager(getCurrentUserId());
        if(manager == null){
            return MapMessage.errorMessage("未查到上级领导，无法申请权限");
        }
        Map<String,Object> map = new HashMap<>();
        AgentRoleType roleType = baseOrgService.getUserRole(manager.getId());
        map.put("userRole",roleType.getRoleName());
        map.put("managerName",manager.getRealName());
        map.put("managerId",manager.getId());
        return MapMessage.successMessage().add("dataMap",map);
    }

    public MapMessage createApply(Long resourceId,Long managerId){
        AuthCurrentUser currentUser = getCurrentUser();
        AgentUser manager = baseOrgService.getUser(managerId);
        if(manager == null){
            return MapMessage.errorMessage("上级领导不存在！");
        }
        AgentOuterResourceApply apply = new AgentOuterResourceApply();
        apply.setApplyUserId(currentUser.getUserId());
        apply.setApplyUserName(currentUser.getRealName());
        apply.setDisabled(false);
        apply.setManagerId(managerId);
//        apply.setManagerName(manager.getRealName());
        apply.setResult(0);
        apply.setResourceId(resourceId);
        agentOuterResourceApplyDao.insert(apply);
        AlpsThreadPool.getInstance().submit(() -> sendNotify(resourceId,currentUser.getRealName(),managerId,apply.getId()));
        return MapMessage.successMessage();
    }

    //审核资源
    public MapMessage auditResourceApply(Long resourceId,String applyId,Integer result,String opinions){
        AgentOuterResourceApply apply = agentOuterResourceApplyDao.load(applyId);
        if(apply == null || !Objects.equals(0,apply.getResult())){
            return MapMessage.errorMessage("申请不存在或不需要审核");
        }
        AgentUser manager = baseOrgService.getUser(apply.getManagerId());
        apply.setManagerName(manager.getRealName());
        apply.setResult(result);
        apply.setOpinions(opinions);
        apply.setAuditTime(new Date());

        List<AgentUserResearchers> userResearchers = agentUserResearchersDao.findByUserIdAndResearchersId(apply.getApplyUserId(),resourceId);
        if(CollectionUtils.isEmpty(userResearchers) && result == 1){
            AgentUserResearchers agentUserResearchers= new AgentUserResearchers();
            agentUserResearchers.setDisabled(false);
            agentUserResearchers.setResearchersId(resourceId);
            agentUserResearchers.setUserId(apply.getApplyUserId());
            agentUserResearchersDao.insert(agentUserResearchers);
        }


        agentOuterResourceApplyDao.upsert(apply);
        AlpsThreadPool.getInstance().submit(() -> sendAuditMessage(resourceId,apply.getApplyUserId(),apply.getOpinions(),apply.getManagerName(),apply.getResult()));
        return MapMessage.successMessage();
    }

    public void sendNotify(Long resourceId,String applyName,Long managerId,String applyId){

        Map<String,Object> resourceInfo = getResourceInfo(resourceId);
        String organizationName = SafeConverter.toString(resourceInfo.get("organizationName"),"");
        String jobName  = SafeConverter.toString(resourceInfo.get("jobName"),"");
        String resourceName = SafeConverter.toString(resourceInfo.get("resourceName"));
        sendApplyMessage(applyName,organizationName,jobName,resourceName,managerId,resourceId,applyId);
    }

    public void sendApplyMessage(String applyName,String organizationName ,String jobName,String resourceName,Long receiverId,Long resourceId,String applyId){
        String content = StringUtils.formatMessage("专员“{} 申请拜访及查看 {} {} {}”的详细信息。",applyName,organizationName,jobName,resourceName);
        agentNotifyService.sendNotify(AgentNotifyType.GR_RESOURCE_APPLY.getType(), "上层资源申请", content,
                Collections.singleton(receiverId), "resourceId="+resourceId + "&applyId=" + applyId);
    }

    public void sendAuditMessage(Long resourceId,Long receiverId,String opinions,String rejectName,Integer result){
        Map<String,Object> resourceInfo = getResourceInfo(resourceId);
        String resourceName = SafeConverter.toString(resourceInfo.get("resourceName"));
        if(result == 1){
            sendApproveMessage(resourceName,receiverId);
        }else {
            sendRejectMessage(resourceName, receiverId, opinions, rejectName);
        }
    }

    public void sendApproveMessage(String resourceName,Long receiverId){
        String content = StringUtils.formatMessage("您提交的“{}”拜访及查看申请已审批通过。",resourceName);
        agentNotifyService.sendNotify(AgentNotifyType.GR_RESOURCE_APPLY.getType(), "上层资源申请", content,
                Collections.singleton(receiverId), null);
    }

    private void sendRejectMessage(String resourceName,Long receiverId,String opinions,String rejectName){
        String content = StringUtils.formatMessage("您提交的“{}”拜访及查看申请已审驳回。\r\n" +
                "驳回原因：{}【驳回人：{}】。", resourceName, opinions, rejectName);
        List<Long> tagIds = agentTagService.getNotifyTagIdsByName("驳回");
        agentNotifyService.sendNotifyWithTags(AgentNotifyType.GR_RESOURCE_APPLY.getType(), "上层资源申请", content,
                Collections.singleton(receiverId), null, null, null, tagIds);
    }

    public MapMessage getApplyInfo(String applyId){
        AgentOuterResourceApply apply = agentOuterResourceApplyDao.load(applyId);
        if(apply == null){
            return MapMessage.errorMessage("申请不存在");
        }
        Map<String,Object> resourceMap = getResourceInfo(apply.getResourceId());
        resourceMap.put("applyUserName",apply.getApplyUserName());
        resourceMap.put("applyTime",apply.getCreateTime());
        if(apply.getResult() == 1 || apply.getResult() == 2){
            resourceMap.put("managerName",apply.getManagerName());
            resourceMap.put("auditTime",apply.getAuditTime());
        }else {
            resourceMap.put("managerName","");
            resourceMap.put("auditTime","");
        }
        resourceMap.put("result",apply.getResult());
        List<AgentUserResearchers> userResearchers = agentUserResearchersDao.findUserResearchersByResourceId(apply.getResourceId());
        if(CollectionUtils.isNotEmpty(userResearchers)){
            List<AgentUser> users = baseOrgService.getUsers(userResearchers.stream().map(AgentUserResearchers::getUserId).collect(Collectors.toSet()));
            List<String> userNames = users.stream().map(AgentUser::getRealName).collect(Collectors.toList());
            resourceMap.put("userNames",userNames);
        }

        return MapMessage.successMessage().add("dataMap",resourceMap);
    }

    public Map<String,Object> getResourceInfo(Long resourceId){
        Map<String,Object> map = new HashMap<>();
        AgentOuterResource outerResource = agentOuterResourcePersistence.load(resourceId);
        if(outerResource == null){
            AgentResearchers agentResearchers = agentResearchersPersistence.load(resourceId);
            map = generateResearcher(agentResearchers);
        }else{
            AgentOuterResourceExtend outerResourceExtend = agentOuterResourceExtendDao.loadByResourceId(resourceId);
            if(outerResourceExtend != null){
                AgentOrganization organization = agentOrganizationPersistence.load(outerResourceExtend.getOrganizationId());
                map = generateOuterResource(outerResource,outerResourceExtend,organization);
            }
        }
        return map;
    }

    private Map<String,Object> generateResearcher(AgentResearchers agentResearchers){
        Map<String,Object> map = new HashMap<>();
        ResearchersJobType jobType =  ResearchersJobType.typeOf(agentResearchers.getJob());
        map.put("id",agentResearchers.getId());
        map.put("resourceName",agentResearchers.getName());
        map.put("subject",agentResearchers.getSubject().getId());
        map.put("subjectName",agentResearchers.getSubject().getValue());
        map.put("remarks",agentResearchers.getSpecificJob());
        map.put("regionRank",getOldResourceLevel(agentResearchers.getLevel()));
        map.put("gradeStr",agentResearchers.getGradeStr());
        map.put("provinceCode",agentResearchers.getProvince());
        map.put("cityCode",agentResearchers.getCity());
        map.put("countyCode",agentResearchers.getCounty());
        map.put("schoolResource",false);
        map.put("job",jobType == null ? null : jobType.getJobId());
        map.put("jobName",jobType == null ? "" : jobType.getJobName());
        return map;
    }

    private Map<String,Object> generateOuterResource(AgentOuterResource outerResource,AgentOuterResourceExtend outerResourceExtend,AgentOrganization organization){
        Map<String,Object> map = new HashMap<>();
        ResearchersJobType jobType =  ResearchersJobType.typeOf(outerResourceExtend.getJob());
        map.put("id",outerResource.getId());
        map.put("resourceName",outerResource.getName());
        map.put("organizationName",organization == null ? "" : organization.getName());
        map.put("regionRank",organization == null ? "" : organization.getRegionRank().getRankName());
        map.put("remarks",outerResourceExtend.getRemarks());
        map.put("gradeStr",outerResourceExtend.getGradeStr());
        map.put("provinceCode",organization == null ? "" : organization.getProvinceCode());
        map.put("cityCode",organization == null ? "" : organization.getCityCode());
        map.put("countyCode",organization == null ? "" : organization.getCountyCode());
        if(organization != null && organization.getOrgType() == 2){
            map.put("schoolResource",true);
        }
        map.put("job",jobType == null ? null : jobType.getJobId());
        map.put("jobName",jobType == null ? "" : jobType.getJobName());
        return map;
    }


    public Map<Long,Map<String,Object>> getResourceInfoByIds(Collection<Long> resourceIds){
        Map<Long,Map<String,Object>> dataMap = new HashMap<>();
        List<Long> outerResourceIds = new ArrayList<>();
        List<Long> researcherIds = new ArrayList<>();
        Map<Long, AgentOuterResource> outerResourceMap = agentOuterResourcePersistence.loads(resourceIds);
        resourceIds.forEach(id -> {
            AgentOuterResource agentOuterResource = outerResourceMap.get(id);
            if (agentOuterResource != null){
                outerResourceIds.add(id);
            }else {
                researcherIds.add(id);
            }
        });
        //历史教研员
        Map<Long, AgentResearchers> researchersMap = agentResearchersPersistence.loads(researcherIds);
        researchersMap.forEach((k,v) -> {
            dataMap.put(k,generateResearcher(v));
        });
        //上层资源
        Map<Long, AgentOuterResourceExtend> outerResourceExtendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(outerResourceIds);
        if (MapUtils.isNotEmpty(outerResourceExtendMap)){
            Set<Long> organizationIds = new ArrayList<>(outerResourceExtendMap.values()).stream().map(AgentOuterResourceExtend::getOrganizationId).collect(Collectors.toSet());
            Map<Long, AgentOrganization> organizationMap = agentOrganizationPersistence.loads(organizationIds);
            outerResourceIds.forEach(p -> {
                AgentOuterResource outerResource = outerResourceMap.get(p);
                AgentOuterResourceExtend outerResourceExtend = outerResourceExtendMap.get(p);
                if (outerResource != null && outerResourceExtend != null){
                    AgentOrganization organization = organizationMap.get(outerResourceExtend.getOrganizationId());
                    if (organization != null){
                        dataMap.put(p,generateOuterResource(outerResource,outerResourceExtend,organization));
                    }
                }
            });
        }
        return dataMap;
    }

    private String getOldResourceLevel(Integer level){
        String levelStr;
        switch (level){
            case 1:
                levelStr = "省级";
                break;
            case 2:
                levelStr = "市级";
                break;
            case 3:
                levelStr = "区级";
                break;
            default:
                levelStr = "";
                break;
        }
        return levelStr;
    }

    public MapMessage authorityUserList(Long resourceId) {
        List<Map<String,Object>> userNames = new ArrayList<>();
        List<AgentGroupUser> agentGroupUserList = baseOrgService.getGroupUserByUser(getCurrentUserId());
        Map<String,Object> resourceMap = getResourceInfo(resourceId);
        boolean schoolResource = SafeConverter.toBoolean(resourceMap.get("schoolResource"),false);
//        AgentRoleType currentRole = baseOrgService.getUserRole(getCurrentUserId());
        if(schoolResource){
            return MapMessage.successMessage().add("dataMap",Collections.emptyMap());
        }
        if (CollectionUtils.isNotEmpty(agentGroupUserList)) {
            Long groupId = agentGroupUserList.get(0).getGroupId();
            //部门下所有用户
            List<AgentGroupUser> managedUserIdsUsers = baseOrgService.getAllGroupUsersByGroupId(groupId);
            //部门下所有用户ID
            Set<Long> managedUserIds = managedUserIdsUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            managedUserIds.remove(getCurrentUserId());

            List<AgentUser> agentUsers = baseOrgService.getUsers(managedUserIds);
            //处理市经理及以上人员权限数据
            agentUsers.forEach(p->{
                AgentRoleType roleType = baseOrgService.getUserRole(p.getId());
                //资源对应权限列表
                Map<String,Set<Integer>> regionMap = agentResearchersService.getRegionCodes(getCurrentUserId(),roleType);
                Set<Integer> provinceCodes = regionMap.get("province");
                Set<Integer> cityCodes = regionMap.get("city");
                Set<Integer> countyCodes = regionMap.get("county");
                Integer provinceCode = SafeConverter.toInt(resourceMap.get("provinceCode"));
                Integer cityCode = SafeConverter.toInt(resourceMap.get("cityCode"));
                Integer countyCode = SafeConverter.toInt(resourceMap.get("countyCode"));
                if(roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager || roleType == AgentRoleType.CityManager){
                    if((provinceCode == 0 && cityCode == 0 && countyCode == 0) ||  //全国或者 旧教研员数据类型其他没有负责范围  暂时按全国算
                            (provinceCodes.contains(provinceCode) ||  cityCodes.contains(cityCode) || countyCodes.contains(countyCode))) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("roleType",roleType);
                        map.put("userName",p.getRealName());
                        userNames.add(map);
                    }
                }
            });

            List<AgentUserResearchers> userResearchers = agentUserResearchersDao.findUserResearchersByResourceId(resourceId);
            if(CollectionUtils.isNotEmpty(userResearchers)){
                List<AgentUser> users = baseOrgService.getUsers(userResearchers.stream().map(AgentUserResearchers::getUserId).collect(Collectors.toSet()));
                users.forEach(u ->{
                    AgentRoleType roleType = baseOrgService.getUserRole(u.getId());
                    if(roleType == AgentRoleType.BusinessDeveloper){
                        Map<String,Object> map = new HashMap<>();
                        map.put("roleType",roleType);
                        map.put("userName",u.getRealName());
                        userNames.add(map);
                    }
                });
            }

        }
        Map<Object,List<Map<String,Object>>> roleTypeListMap = userNames.stream().collect(Collectors.groupingBy(p -> p.get("roleType"), Collectors.toList()));
        Map<Object,List<String>> resultMap = new HashMap<>();
        roleTypeListMap.forEach((k,v)->{
            List<String> roleUserList = new ArrayList<>();
            v.forEach(m ->{
                roleUserList.add(SafeConverter.toString(m.get("userName")));
            });
            resultMap.put(k,roleUserList);
        });
        return MapMessage.successMessage().add("dataMap",resultMap);
    }

    //编辑时用于判断资源是否是新资源
    public boolean isNewResource(Long resourceId){
        if(resourceId == null || resourceId == 0){
            return false;
        }
        AgentOuterResource resource = agentOuterResourcePersistence.load(resourceId);
        return resource != null;
    }

    /**
     * 上层资源数据转换
     * @param outerResourceList
     * @return
     */
    public List<AgentOuterResourceView> generateOuterResourceInfo(List<Map<String,Object>> outerResourceList){
        if (CollectionUtils.isEmpty(outerResourceList)){
            return Collections.emptyList();
        }
        List<AgentOuterResourceView>  outerResourceViewList = new ArrayList<>();
        outerResourceList.forEach(item -> {
            AgentOuterResourceView outerResourceView = new AgentOuterResourceView();
            outerResourceView.setId(SafeConverter.toLong(item.get("id")));
            outerResourceView.setName(SafeConverter.toString(item.get("name")));
            outerResourceView.setSubject(Subject.fromSubjectId(SafeConverter.toInt(item.get("subject"))));
            outerResourceView.setSubjectName(SafeConverter.toString(item.get("subjectName")));
            ResearchersJobType job = ResearchersJobType.typeOf((Integer) item.get("job"));
            if (job != null){
                outerResourceView.setJobName(job.getJobName());
            }
            outerResourceView.setProvinceName(SafeConverter.toString(item.get("provinceName")));
            outerResourceView.setCityName(SafeConverter.toString(item.get("cityName")));
            outerResourceView.setCountyName(SafeConverter.toString(item.get("countyName")));
            outerResourceViewList.add(outerResourceView);
        });
        return outerResourceViewList;
    }

    /**
     * 日报，资源拓维选择上层资源
     * @param userId
     * @param name
     * @return
     */
    public Map<String, Object> getOuterResourceInfoForDaily(Long userId,String name) {
        Map<String, Object> result = new HashMap<>();
        Integer pageNo = 0;
        Integer pageSize = 1;
        Pageable pageable = new PageRequest(pageNo,pageSize);
        //英语上层资源
        List<AgentOuterResourceView> englishOuterResourceViewList = new ArrayList<>();
        //数学教研员
        List<AgentOuterResourceView> mathOuterResourceViewList = new ArrayList<>();
        //语文教研员
        List<AgentOuterResourceView> chineseOuterResourceViewList = new ArrayList<>();
        //其他科目教研员
        List<AgentOuterResourceView> otherOuterResourceViewList = new ArrayList<>();
        //数据不分页
        List<Map<String, Object>> outerResourceMapList = agentResearchersService.searchResearcherList(userId, name, pageable);
        if (CollectionUtils.isNotEmpty(outerResourceMapList)){
            //过滤出已解锁的上层资源
            outerResourceMapList = outerResourceMapList.stream().filter(p -> !(Boolean) p.get("lockStatus")).collect(Collectors.toList());
            //数据转换
            List<AgentOuterResourceView> outerResourceViewList = generateOuterResourceInfo(outerResourceMapList);
            outerResourceViewList.forEach(item -> {
                if (null != item) {
                    if (item.getSubject() == Subject.ENGLISH) {
                        englishOuterResourceViewList.add(item);
                    } else if (item.getSubject() == Subject.MATH) {
                        mathOuterResourceViewList.add(item);
                    } else if (item.getSubject() == Subject.CHINESE) {
                        chineseOuterResourceViewList.add(item);
                    }else {
                        otherOuterResourceViewList.add(item);
                    }
                }
            });
        }
        List<Map<String,Object>> englishOuterResourceList = getFirstCapitalOuterResourceList(englishOuterResourceViewList);
        List<Map<String,Object>> mathOuterResourceList = getFirstCapitalOuterResourceList(mathOuterResourceViewList);
        List<Map<String,Object>> chineseOuterResourceList = getFirstCapitalOuterResourceList(chineseOuterResourceViewList);
        List<Map<String,Object>> otherOuterResourceList = getFirstCapitalOuterResourceList(otherOuterResourceViewList);

        result.put("englishOuterResourceList",englishOuterResourceList);
        result.put("mathOuterResourceList",mathOuterResourceList);
        result.put("chineseOuterResourceList",chineseOuterResourceList);
        result.put("otherOuterResourceList",otherOuterResourceList);

        return result;
    }

    private List<Map<String,Object>> getFirstCapitalOuterResourceList(List<AgentOuterResourceView> outerResourceViewList) {
        List<Map<String, Object>> firstCapitalResearcherList = new ArrayList<>();
        //根据上层资源姓名首字母分组
        Map<String, List<AgentOuterResourceView>> outerResourceMap = outerResourceViewList.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getName())));

        //将教研员姓名首字母排序
        List<String> firstCapitalList = outerResourceMap.keySet().stream().sorted(Comparator.comparing(item -> item == null ? "" : item, Collator.getInstance(Locale.CHINA))).collect(Collectors.toList());
        firstCapitalList.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("firstCapital", item);
            dataMap.put("outerResourceList", outerResourceMap.get(item));
            firstCapitalResearcherList.add(dataMap);
        });
        return firstCapitalResearcherList;
    }

}
