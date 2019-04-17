package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.dao.mongo.AgentResearchersUpdateLogDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserResearchersDao;
import com.voxlearning.utopia.agent.persist.AgentResearchersPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchersUpdateLog;
import com.voxlearning.utopia.agent.persist.entity.AgentUserResearchers;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOrganization;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.organization.AgentOrganizationService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmVisitResearcherInfo;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordOuterResourceLoader;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教研员
 * Created by yaguang.wang on 2016/10/19.
 */
@Named
public class AgentResearchersService extends AbstractAgentService {
    public static final List<Integer> GRADES_HAS_HIGH = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    public static final List<String> GRADES_HAS_HIGH_STR = Arrays.asList("一", "二", "三", "四", "五", "六", "七", "八", "九", "高一", "高二", "高三");

    @Inject private RaikouSystem raikouSystem;

    @Inject private AgentResearchersPersistence agentResearchersPersistence;
    @Inject private WorkRecordService workRecordService;
    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentUserResearchersDao agentUserResearchersDao;

    @Inject private AgentResearchersUpdateLogDao agentResearchersUpdateLogDao;

    @Inject private AgentOrganizationService agentOrganizationService;

    @Inject private AgentOuterResourceService agentOuterResourceService;
    @Inject private WorkRecordOuterResourceLoader workRecordOuterResourceLoader;

    public MapMessage upsertResearchers(Long id, String name, Integer gender, String phone, Integer job, String gradeStr, Subject subject, String specificJob, String telephone, String organizationId, String department) {
        AuthCurrentUser currentUser = getCurrentUser();
        AgentResearchers researchers = new AgentResearchers();
        if (id != null && id != 0L) {
            researchers = loadResearchers(id);
            if (researchers == null) {
                return MapMessage.errorMessage("该资源已被删除");
            }
        }
        researchers.setGradeStr(gradeStr);
        researchers.setSubject(subject);
        researchers.setAgentUserId(currentUser.getUserId());
        researchers.setName(name);
        researchers.setGender(gender);
        researchers.setPhone(phone);
        researchers.setJob(job);
        researchers.setSpecificJob(specificJob);
        researchers.setSchoolPhase(this.getSchoolPhaseBygrade(gradeStr));
//        researchers.setSchoolPhaseStr(schoolPhaseStr);
        researchers.setDisabled(false);
        if (id != null && id != 0L) {
            AgentResearchers oldResearchers = agentResearchersPersistence.load(id);
            agentResearchersPersistence.upsert(researchers);
            saveUpdateLog(currentUser.getUserId(), currentUser.getRealName(), oldResearchers, researchers, id);
        } else {
            agentResearchersPersistence.insert(researchers);
        }

        AgentUserResearchers agentUserResearchers = new AgentUserResearchers();
        agentUserResearchers.setDisabled(false);
        agentUserResearchers.setResearchersId(researchers != null ? researchers.getId() : null);
        agentUserResearchers.setUserId(getCurrentUserId());
        agentUserResearchersDao.insert(agentUserResearchers);
        return MapMessage.successMessage();
    }

    public MapMessage isRepetitionPhone(Long userId, String phone, Long id) {
//        List<AgentResearchers> agentResearchers = loadResearchersByUserId(userId);
//        return CollectionUtils.isNotEmpty(agentResearchers.stream().filter(p -> !Objects.equals(p.getId(), id)).filter(p -> Objects.equals(p.getPhone(), phone)).collect(Collectors.toList()));
        List<AgentResearchers> agentResearchersList = agentResearchersPersistence.findAgentResearchersByPhone(phone).stream().filter(p -> p.getDisabled() == false && !Objects.equals(p.getId(), id)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(agentResearchersList)) {
            AgentResearchers agentResearchers = agentResearchersList.stream().findFirst().orElse(null);
            MapMessage mapMessage = MapMessage.errorMessage();
            Map<String, Object> map = new HashMap<>();
            map.put("id", agentResearchers.getId());
            map.put("name", agentResearchers.getName());
            map.put("region", getCityRegion(agentResearchers));
            map.put("level", agentResearchers.getLevel());
            map.put("subject", agentResearchers.getSubject().getId());
            mapMessage.put("data", map);
            return mapMessage;
        }
        return MapMessage.successMessage();
    }

    public List<AgentResearchers> loadResearchersByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return agentResearchersPersistence.findAgentResearchersByUserId(userId);
    }

    public AgentResearchers loadResearchers(Long id) {
        if (id == null) {
            return null;
        }
        return agentResearchersPersistence.load(id);
    }

    public Map<Long, AgentResearchers> loadResearchers(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return agentResearchersPersistence.loads(ids);
    }

    public List<AgentResearchers> getUserResearchersList(Long userId, Integer province, Integer city, String name) {
        List<AgentGroup> groups = baseOrgService.getUserGroups(userId);
        if (CollectionUtils.isEmpty(groups)) {
            return null;
        }
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        List<AgentResearchers> allResearchers = new ArrayList<>();
        if (roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.BusinessDeveloper || roleType == AgentRoleType.Country) {
            allResearchers = agentResearchersPersistence.findAgentResearchersPage(userId, Collections.singletonList(province), Collections.singleton(city), null, name, null);
        }
        return allResearchers;
    }

    //查询教研员列表  全国总监  大区经理
    public List<AgentResearchers> getUserResearchersListForVisit(Long userId, String name) {
        List<AgentGroup> groups = baseOrgService.getUserGroups(userId);
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        List<AgentResearchers> allResearchers = null;
        if (roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.BusinessDeveloper) {
            Collection<ExRegion> counties = baseOrgService.getCountyRegion(getCurrentUserId(), roleType);
            Set<Integer> provinceSet = new LinkedHashSet<>();
            Set<Integer> citySet = new LinkedHashSet<>();
            counties.forEach(p -> {
                provinceSet.add(p.getProvinceCode());
                citySet.add(p.getCityCode());
            });
            AgentGroup businessUnit = baseOrgService.getParentGroupByRole(groups.get(0).getId(), AgentGroupRoleType.BusinessUnit);
            if (businessUnit == null) {
                return Collections.emptyList();
            }
//            int schoolPhase = this.getSchoolPhaseByServiceType(businessUnit.getServiceType());
            allResearchers = agentResearchersPersistence.findAgentResearchersList(userId, provinceSet, citySet, null, name);
            if (roleType == AgentRoleType.BusinessDeveloper) {
                List<AgentUserResearchers> userResearchers = agentUserResearchersDao.findUserResearchersByAgentUserId(getCurrentUserId());
                List<Long> userResearcherIds = userResearchers.stream().map(AgentUserResearchers::getResearchersId).collect(Collectors.toList());
                allResearchers = allResearchers.stream().filter(p -> userResearcherIds.contains(p.getId())).collect(Collectors.toList());
            }
        } else if (roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager) {//全国总监单独走逻辑
            allResearchers = agentResearchersPersistence.findAgentResearchersByName(name);
        }
        return CollectionUtils.isNotEmpty(allResearchers) ? allResearchers : Collections.emptyList();
    }

    //查询教研员列表  标记是否私海
    public MapMessage loadResearchersInfoForPage(Long userId, Integer regionCode, String name, Pageable pageable, Integer sortType) {
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return MapMessage.errorMessage("未找到对应地区");
        }
        int province = exRegion.getProvinceCode();
        int city = exRegion.getCityCode();
        //走旧逻辑 地区编码在  AGENT_RESEARCHERS 表
        List<AgentResearchers> oldResearchers = getUserResearchersList(userId, province, city, name);
        List<AgentResearchers> researcherList = new ArrayList<>();
        if (pageable.getPageNumber() == 0) {
            Collection<AgentResearchers> deleteList = findDeleteOtherJobResearcher();
            if (CollectionUtils.isNotEmpty(deleteList)) {
                researcherList.addAll(deleteList);
            }
        }
        if (CollectionUtils.isNotEmpty(oldResearchers)) {
            researcherList.addAll(oldResearchers);
        }
        List<Map<String, Object>> oldList = assembleOldList(researcherList);

        List<AgentOrganization> organizations = agentOrganizationService.getListByRegionCode(regionCode);

        //上层资源
        List<AgentOrganization> outerOrgs = organizations.stream().filter(p -> p.getOrgType() != null && p.getOrgType() == 1).collect(Collectors.toList());
        List<Map<String, Object>> newOuterList = agentOuterResourceService.assembleList(outerOrgs, name);
//        //查学校资源   我的资源不需要显示学校资源
//        List<Long> schoolOrgIds = organizations.stream().filter(p -> p.getOrgType() == 2).map(AgentOrganization::getId).collect(Collectors.toList());
//        List<Map<String,Object>> schoolOuterList = agentOuterResourceService.assembleList(schoolOrgIds,name,false);
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(oldList)) {
            result.addAll(oldList);
        }
        if (CollectionUtils.isNotEmpty(newOuterList)) {
            result.addAll(newOuterList);
        }
//        if(CollectionUtils.isNotEmpty(schoolOuterList)){
//            result.addAll(schoolOuterList);
//        }
        if (Objects.equals(sortType, 1)) {
            result = result.stream().sorted(Comparator.comparing(p -> p.get("name") == null ? "" : SafeConverter.toString(p.get("name")), Collator.getInstance(Locale.CHINA))).collect(Collectors.toList());
        } else {
            result.sort(((o1, o2) -> {
                Long time1 = SafeConverter.toLong(o1.get("visitTime"), 0);
                Long time2 = SafeConverter.toLong(o2.get("visitTime"), 0);
                return time2.compareTo(time1);
            }));
        }
        int fromIndex = pageable.getOffset();
        int toIndex = 0;
        boolean hasNext = true;
        if (fromIndex + pageable.getPageSize() >= result.size()) {
            toIndex = result.size();
            hasNext = false;
        } else {
            toIndex = fromIndex + pageable.getPageSize();
        }
        if (fromIndex > toIndex) {
            return MapMessage.successMessage().add("researchersList", Collections.emptyList()).add("hasNext", false);
        }
        result = result.subList(fromIndex, toIndex);

        return MapMessage.successMessage().add("researchersList", result).add("hasNext", hasNext);
    }

    public List<Map<String, Object>> assembleOldList(List<AgentResearchers> researcherList) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<AgentUserResearchers> userResearchers = agentUserResearchersDao.findUserResearchersByAgentUserId(getCurrentUserId());
        List<Long> userResearcherIds = userResearchers.stream().map(AgentUserResearchers::getResearchersId).collect(Collectors.toList());
//        List<String> organizationIds = researcherList.stream().filter(p-> StringUtils.isNotBlank(p.getOrganizationId())).map(AgentResearchers::getOrganizationId).collect(Collectors.toList());
//        Map<String,AgentOrganization> orgMap = agentOrganizationDao.loads(organizationIds);
        researcherList.forEach(p -> {
            Map<String, Object> data = new HashMap<>();
            data.put("dataFlag", "old");
            data.put("name", p.getName());
            data.put("id", p.getId());
            data.put("gender", p.getGender());
            data.put("gradeStr", p.getGradeStr());
            boolean isPrivate = userResearcherIds.contains(p.getId());
//            data.put("isPrivate",isPrivate);
            if (isPrivate || isManager()) {
                data.put("phone", p.getPhone());
                data.put("telephone", "");
                data.put("lockStatus", false);
            } else {
                data.put("telephone", "");
                data.put("phone", "");
                data.put("lockStatus", true);
            }
            data.put("job", p.getJob());
            ResearchersJobType researchersJobType = ResearchersJobType.typeOf(p.getJob());
            data.put("jobName", researchersJobType != null ? researchersJobType.getJobName() : "");
            data.put("grResource", ResearchersJobType.sgrResourceJobType(researchersJobType));
            //TODO 写完新的拜访的话 这里要做旧数据兼容
            List<CrmWorkRecord> workRecords = findResearcherVisitRecord(p.getId());
            List<Map<String, Object>> newWorkRecordList = workRecordOuterResourceLoader.resourceVisitList(p.getId());
            if (CollectionUtils.isNotEmpty(newWorkRecordList)) {
                data.put("visitTime", newWorkRecordList.get(0).get("workTime"));
            } else {
                data.put("visitTime", workRecords.size() > 0 ? workRecords.get(0).getCreateTime() : null);
            }
            data.put("visitTime", workRecords.size() > 0 ? workRecords.get(0).getCreateTime() : null);
            data.put("subject", p.getSubject() == null ? 0 : p.getSubject().getId());
            data.put("subjectName", p.getSubject() == null ? 0 : p.getSubject().getValue());

            ExRegion region = raikouSystem.loadRegion(p.getCounty());
            if (region == null) {
                region = raikouSystem.loadRegion(p.getCity());
                if (region == null) {
                    region = raikouSystem.loadRegion(p.getProvince());
                }
            }
            data.put("provinceName", region != null ? region.getProvinceName() : "");
            data.put("cityName", region != null ? region.getCityName() : "");
            data.put("countyName", region != null ? region.getCountyName() : "");
            data.put("department", "");
            result.add(data);
        });
        return result;
    }

    public Map<String, Object> loadResearchersInfoByUserIdForVisit(Long userId, String name) {
        Map<String, Object> result = new HashMap<>();
//        List<AgentResearchers> researchers = loadResearchersByUserId(userId);
        List<AgentResearchers> researchers = getUserResearchersListForVisit(userId, name);
        //英语教研员
        List<AgentResearchers> englishResearchers = new ArrayList<>();
        //数学教研员
        List<AgentResearchers> mathResearchers = new ArrayList<>();
        //语文教研员
        List<AgentResearchers> chineseResearchers = new ArrayList<>();
        //其他科目教研员
        List<AgentResearchers> otherResearchers = new ArrayList<>();
        researchers.forEach(item -> {
            if (null != item) {
                if (item.getSubject() == Subject.ENGLISH) {
                    englishResearchers.add(item);
                } else if (item.getSubject() == Subject.MATH) {
                    mathResearchers.add(item);
                } else if (item.getSubject() == Subject.CHINESE) {
                    chineseResearchers.add(item);
                } else {
                    otherResearchers.add(item);
                }
            }
        });

        List<Map<String, Object>> englishResearcherList = getFirstCapitalResearcherList(englishResearchers);
        List<Map<String, Object>> mathResearcherList = getFirstCapitalResearcherList(mathResearchers);
        List<Map<String, Object>> chineseResearcherList = getFirstCapitalResearcherList(chineseResearchers);
        List<Map<String, Object>> otherResearcherList = getFirstCapitalResearcherList(otherResearchers);

        result.put("englishResearcherList", englishResearcherList);
        result.put("mathResearcherList", mathResearcherList);
        result.put("chineseResearcherList", chineseResearcherList);
        result.put("otherResearcherList", otherResearcherList);

        return result;
    }

    /**
     * 获取教研员姓名首字母与教研员对应关系
     *
     * @param researchers
     * @return
     */
    private List<Map<String, Object>> getFirstCapitalResearcherList(List<AgentResearchers> researchers) {
        Map<String, List<Map<String, Object>>> firstCapitalResearcherMap = new HashMap<>();
        List<Map<String, Object>> firstCapitalResearcherList = new ArrayList<>();
        //根据教研员姓名首字母分组
        Map<String, List<AgentResearchers>> agentResearcherMap = researchers.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getName())));
        agentResearcherMap.forEach((k, v) -> {
            if (null != k && CollectionUtils.isNotEmpty(v)) {
                List<Map<String, Object>> researcherList = new ArrayList<>();
                v.forEach(item -> {
                    //拼装教研员数据
                    Map<String, Object> researcherMap = new HashMap<>();
                    researcherMap.put("id", item.getId());
                    researcherMap.put("name", item.getName());
                    researcherMap.put("level", item.getLevel());
                    researcherMap.put("job", item.getJob());
                    StringBuilder manageArea = new StringBuilder();
                    ExRegion countyRegion = raikouSystem.loadRegion(item.getCounty());
                    if (countyRegion == null) {
                        ExRegion cityRegion = raikouSystem.loadRegion(item.getCity());
                        if (cityRegion == null) {
                            ExRegion provinceRegion = raikouSystem.loadRegion(item.getProvince());
                            if (null != provinceRegion) {
                                manageArea.append((provinceRegion.getProvinceName()));
                            }
                        } else {
                            manageArea.append((cityRegion.getProvinceName() + " " + cityRegion.getCityName()));
                        }
                    } else {
                        manageArea.append((countyRegion.getProvinceName() + " " + countyRegion.getCityName() + " " + countyRegion.getCountyName()));
                    }
                    researcherMap.put("manageArea", manageArea.toString());
                    researcherList.add(researcherMap);
                });
                firstCapitalResearcherMap.put(k, researcherList);
            }
        });
        //将教研员姓名首字母排序
        List<String> firstCapitalList = firstCapitalResearcherMap.keySet().stream().sorted(Comparator.comparing(item -> item == null ? "" : item, Collator.getInstance(Locale.CHINA))).collect(Collectors.toList());
        firstCapitalList.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("firstCapital", item);
            dataMap.put("researchers", firstCapitalResearcherMap.get(item));
            firstCapitalResearcherList.add(dataMap);
        });
        return firstCapitalResearcherList;
    }

    public List<CrmWorkRecord> loadResearchersRecordsByResearchersId(Long userId, Long researchersId) {
        if (userId == 0L || null == researchersId || researchersId == 0L) {
            return Collections.emptyList();
        }
        List<CrmWorkRecord> workRecordList = new ArrayList<>();
        List<CrmWorkRecord> allRecords = workRecordService.getAllWorkerWorkRecords(userId, CrmWorkRecordType.TEACHING);
        //拼接拜访教研员新纪录（一次拜访多个教研员）
        allRecords.forEach(record -> {
            if (null != record) {
                //获取拜访记录中的教研员
                List<CrmVisitResearcherInfo> visitedResearcherList = record.getVisitedResearcherList();
                if (CollectionUtils.isNotEmpty(visitedResearcherList)) {
                    visitedResearcherList.forEach(researcher -> {
                        if (null != researcher && Objects.equals(researcher.getResearcherId(), researchersId)) {
                            record.setVisitedConclusion(researcher.getConclusion());
                            workRecordList.add(record);
                        }
                    });
                }
            }
        });
        //拼接拜访教研员历史纪录（一次拜访一个教研员）
        workRecordList.addAll(allRecords.stream().filter(p -> Objects.equals(p.getResearchersId(), researchersId)).collect(Collectors.toList()));
        return workRecordList;
    }

    public List<CrmWorkRecord> findResearcherVisitRecord(Long researchersId) {
        return workRecordService.findResearcherVisitRecord(researchersId);
    }


    public List<Map<String, Object>> loadResearchersRecordsInfo(Long userId, Long researchersId) {
        List<Map<String, Object>> newVisitList = workRecordOuterResourceLoader.resourceVisitList(researchersId);
//        List<CrmWorkRecord> records = loadResearchersRecordsByResearchersId(userId, researchersId);
        AgentResearchers agentResearchers = agentResearchersPersistence.load(researchersId);
        List<Long> researcherIds = new ArrayList<>();
        List<CrmWorkRecord> allRecords = new ArrayList<>();
        researcherIds.add(researchersId);
        if (agentResearchers != null && agentResearchers.getDisabled() != null && !agentResearchers.getDisabled()) {
            Map<String, List<AgentResearchers>> phoneMap = agentResearchersPersistence.findAgentResearchersByPhones(Collections.singleton(agentResearchers.getPhone()));
            if (MapUtils.isNotEmpty(phoneMap)) {
                phoneMap.get(agentResearchers.getPhone()).forEach(p -> {
                    if (p.getDisabled()) {
                        List<CrmWorkRecord> delRecords = findResearcherVisitRecord(p.getId());
                        if (CollectionUtils.isNotEmpty(delRecords)) {
                            allRecords.addAll(delRecords);
                            researcherIds.add(p.getId());
                        }
                    }
                });
            }
        }
        List<CrmWorkRecord> records = findResearcherVisitRecord(researchersId);
        if (CollectionUtils.isNotEmpty(records)) {
            allRecords.addAll(records);
        }
        List<CrmWorkRecord> finalRecords = allRecords.stream().sorted((o1, o2) -> o2.getWorkTime().compareTo(o1.getWorkTime())).collect(Collectors.toList());
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(newVisitList)) {
            result.addAll(newVisitList);
        }
        finalRecords.forEach(p -> {
            Map<String, Object> data = new HashMap<>();
            data.put("recordId", p.getId());
            data.put("intention", p.getVisitedIntention());
            data.put("flow", p.getVisitedFlow());
            data.put("place", null != p.getAddress() ? p.getAddress() : (null != p.getVisitedPlace() ? p.getVisitedPlace() : ""));
            String conclusion = "";
            for (CrmVisitResearcherInfo info : p.getVisitedResearcherList()) {
                if (researcherIds.contains(info.getResearcherId())) {
                    conclusion = info.getConclusion();
                }
            }
            data.put("conclusion", conclusion);
            data.put("workTime", DateUtils.dateToString(p.getWorkTime(), "yyyy/MM/dd HH:mm:ss"));
            List<Map<String, Object>> visitorList = workRecordService.getVisitorList(p.getId());
            StringBuffer sbf = new StringBuffer();
            sbf.append(p.getWorkerName());
            visitorList.forEach(v -> sbf.append(" ").append(v.get("partnerName")).append(" "));
            data.put("visitors", sbf.toString());
            List<String> imageList = new ArrayList<>();
            imageList.add(p.getVisitedImgUrl());
            data.put("imageList", imageList);
            //TODO 其他事项 暂时没有  做新资源拜访管理时加上
            data.put("otherMatters", "");
            result.add(data);
        });
        return result;
    }


    public MapMessage dropResearchers(Long id) {
        if (agentResearchersPersistence.remove(id)) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("删除教研员失败");
        }
    }

//    public MapMessage addResearchersRecords(AuthCurrentUser user, Integer intention, String longitude,String latitude, String signImgUrl,String visitedImgUrl, String flow,List<CrmVisitResearcherInfo> visitResearcherList,String coordinateType,Integer signType) {
//        if (user == null) {
//            return MapMessage.errorMessage("用户信息错误，请重新登录");
//        }
//        if (CollectionUtils.isEmpty(visitResearcherList)) {
//            return MapMessage.errorMessage("教研员信息错误");
//        }
//        if (intention == 0) {
//            return MapMessage.errorMessage("目的类型选择错误");
//        }
//        if (StringUtils.isBlank(flow)) {
//            return MapMessage.errorMessage("过程信息必填");
//        }
//        if (flow.length() > 100) {
//            return MapMessage.errorMessage("过程信息长度超过100个字符，请减少");
//        }
//
//        MapMessage addressMapMessage = AmapMapApi.getAddress(latitude, longitude, coordinateType);
//        if (!addressMapMessage.isSuccess()) {
//            return addressMapMessage;
//        }
//
//        CrmWorkRecord record = new CrmWorkRecord();
//        record.setWorkerId(user.getUserId());
//        record.setWorkerName(user.getRealName());
//        record.setWorkTime(new Date());
//        record.setVisitedIntention(intention);
//        record.setVisitedFlow(flow);
//        record.setWorkType(CrmWorkRecordType.TEACHING);
//
//        record.setSchoolPhotoUrl(signImgUrl);
//        record.setVisitedImgUrl(visitedImgUrl);
//        record.setAddress(ConversionUtils.toString(addressMapMessage.get("address")));
//        record.setCoordinateType("autonavi");
//        record.setLongitude(ConversionUtils.toString(addressMapMessage.get("longitude")));
//        record.setLatitude(ConversionUtils.toString(addressMapMessage.get("latitude")));
//        record.setSignType(signType);
//        record.setVisitedResearcherList(visitResearcherList);
//        record.setDisabled(false);
//        return workRecordService.saveCrmWorkRecord(record);
//    }


    /**
     * 刷数据方法
     * 数据初始化时，同一个教研员，若被多个人都创建了，在教研员信息的其他字段“姓名、性别、电话、职务、级别、管辖区域、年级、学科”出现不一致时，以重复次数多的为准，若重复次数相同，则随机显示一个
     */
    public void initResearchersData() {
        List<AgentResearchers> all = agentResearchersPersistence.query().stream().sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())).collect(Collectors.toList());
        //处理重复
        Map<String, List<AgentResearchers>> phoneListMap = all.stream().collect(Collectors.groupingBy(AgentResearchers::getPhone));
        //所有教研员数据按照电话分组
        phoneListMap.forEach((k, v) -> {
            if (v.size() > 1) {
                //处理姓名
                String maxName = "";
                Integer maxGender = 1;
                Subject subject = null;
                Integer maxjob = 0;
                Integer maxlevel = 0;
                Integer province = 0;
                String maxgrade = "";
                Integer city = 0;
                Integer county = 0;

                Map<String, List<AgentResearchers>> nameListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getName));
                if (nameListMap.keySet().size() > 1) {//同一手机号有多条数据且名字不相同
                    Map<String, Integer> nameNumMap = new HashMap<>();
                    nameListMap.forEach((name, namelist) -> nameNumMap.put(name, namelist.size()));

                    Map<String, Integer> finalNameNumMap = sortByValue(nameNumMap);

                    for (Map.Entry<String, Integer> entry : finalNameNumMap.entrySet()) {
                        if (entry != null) {
                            maxName = entry.getKey();
                            break;
                        }
                    }
                } else {
                    for (Map.Entry<String, List<AgentResearchers>> entry : nameListMap.entrySet()) {
                        if (entry != null) {
                            maxName = entry.getKey();
                            break;
                        }
                    }
                }


                //处理性别
                Map<Integer, List<AgentResearchers>> genderListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getGender));
                if (genderListMap.keySet().size() > 1) {//同一手机号有多条数据且性别不相同
                    Map<Integer, Integer> genderNumMap = new HashMap<>();
                    genderListMap.forEach((gender, genderList) -> genderNumMap.put(gender, genderList.size()));
                    Map<Integer, Integer> finalGenderNumMap = sortByValue(genderNumMap);
                    for (Map.Entry<Integer, Integer> entry : finalGenderNumMap.entrySet()) {
                        if (entry != null) {
                            maxGender = entry.getKey();
                            break;
                        }
                    }

                } else {
                    for (Map.Entry<Integer, List<AgentResearchers>> entry : genderListMap.entrySet()) {
                        if (entry != null) {
                            maxGender = entry.getKey();
                            break;
                        }
                    }
                }

                //处理职务
                Map<Integer, List<AgentResearchers>> jobListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getJob));
                if (jobListMap.keySet().size() > 1) {//同一手机号有多条数据且性别不相同
                    Map<Integer, Integer> jobNumMap = new HashMap<>();
                    jobListMap.forEach((job, jobList) -> jobNumMap.put(job, jobList.size()));
                    Map<Integer, Integer> finalJobNumMap = sortByValue(jobNumMap);
                    for (Map.Entry<Integer, Integer> entry : finalJobNumMap.entrySet()) {
                        if (entry != null) {
                            maxjob = entry.getKey();
                            break;
                        }
                    }

                } else {
                    for (Map.Entry<Integer, List<AgentResearchers>> entry : jobListMap.entrySet()) {
                        if (entry != null) {
                            maxjob = entry.getKey();
                            break;
                        }
                    }
                }
                //处理级别
                Map<Integer, List<AgentResearchers>> levelListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getLevel));
                if (levelListMap.keySet().size() > 1) {//同一手机号有多条数据且 级别 不相同
                    Map<Integer, Integer> levelNumMap = new LinkedHashMap<>();
                    levelListMap.forEach((level, levelList) -> levelNumMap.put(level, levelList.size()));
                    Map<Integer, Integer> finallevelNumMap = sortByValue(levelNumMap);
                    for (Map.Entry<Integer, Integer> entry : finallevelNumMap.entrySet()) {
                        if (entry != null) {
                            maxlevel = entry.getKey();
                            break;
                        }
                    }

                } else {
                    for (Map.Entry<Integer, List<AgentResearchers>> entry : levelListMap.entrySet()) {
                        if (entry != null) {
                            maxlevel = entry.getKey();
                            break;
                        }
                    }
                }

                //处理年级
                Map<String, List<AgentResearchers>> gradeListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getGrade));
                if (gradeListMap.keySet().size() > 1) {//同一手机号有多条数据且 级别 不相同
                    Map<String, Integer> gradeNumMap = new HashMap<>();
                    gradeListMap.forEach((grade, gradeList) -> gradeNumMap.put(grade, gradeList.size()));
                    Map<String, Integer> finalGradeNumMap = sortByValue(gradeNumMap);
                    for (Map.Entry<String, Integer> entry : finalGradeNumMap.entrySet()) {
                        if (entry != null) {
                            maxgrade = entry.getKey();
                            break;
                        }
                    }

                } else {
                    for (Map.Entry<String, List<AgentResearchers>> entry : gradeListMap.entrySet()) {
                        if (entry != null) {
                            maxgrade = entry.getKey();
                            break;
                        }
                    }
                }

                //处理学科
                Map<Subject, List<AgentResearchers>> subjectListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getSubject));
                if (subjectListMap.keySet().size() > 1) {//同一手机号有多条数据且 级别 不相同
                    Map<Subject, Integer> subjectNumMap = new HashMap<>();
                    subjectListMap.forEach((subKey, subList) -> subjectNumMap.put(subKey, subList.size()));
                    Map<Subject, Integer> finalsSubjectNumMap = sortByValue(subjectNumMap);
                    for (Map.Entry<Subject, Integer> entry : finalsSubjectNumMap.entrySet()) {
                        if (entry != null) {
                            subject = entry.getKey();
                            break;
                        }
                    }

                } else {
                    for (Map.Entry<Subject, List<AgentResearchers>> entry : subjectListMap.entrySet()) {
                        if (entry != null) {
                            subject = entry.getKey();
                            break;
                        }
                    }
                }

                //处理省
                Map<Integer, List<AgentResearchers>> provinceListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getProvince));
                if (provinceListMap.keySet().size() > 1) {//同一手机号有多条数据且 级别 不相同
                    Map<Integer, Integer> provinceNumMap = new HashMap<>();
                    provinceListMap.forEach((subKey, subList) -> provinceNumMap.put(subKey, subList.size()));
                    Map<Integer, Integer> finalProvinceNumMap = sortByValue(provinceNumMap);
                    for (Map.Entry<Integer, Integer> entry : finalProvinceNumMap.entrySet()) {
                        if (entry != null) {
                            province = entry.getKey();
                            break;
                        }
                    }
                } else {
                    for (Map.Entry<Integer, List<AgentResearchers>> entry : provinceListMap.entrySet()) {
                        if (entry != null) {
                            province = entry.getKey();
                            break;
                        }
                    }
                }

                //处理市
                Map<Integer, List<AgentResearchers>> cityListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getCity));
                if (cityListMap.keySet().size() > 1) {//同一手机号有多条数据且 市 不相同
                    Map<Integer, Integer> cityNumMap = new HashMap<>();
                    cityListMap.forEach((subKey, subList) -> cityNumMap.put(subKey, subList.size()));
                    Map<Integer, Integer> finalCityNumMap = sortByValue(cityNumMap);
                    for (Map.Entry<Integer, Integer> entry : finalCityNumMap.entrySet()) {
                        if (entry != null) {
                            city = entry.getKey();
                            break;
                        }
                    }

                } else {
                    for (Map.Entry<Integer, List<AgentResearchers>> entry : cityListMap.entrySet()) {
                        if (entry != null) {
                            city = entry.getKey();
                            break;
                        }
                    }
                }

                //处理区
                Map<Integer, List<AgentResearchers>> countyListMap = v.stream().collect(Collectors.groupingBy(AgentResearchers::getCounty));
                if (countyListMap.keySet().size() > 1) {//同一手机号有多条数据且 级别 不相同
                    Map<Integer, Integer> countyNumMap = new HashMap<>();
                    countyListMap.forEach((subKey, subList) -> countyNumMap.put(subKey, subList.size()));

                    Map<Integer, Integer> finalCountyNumMap = sortByValue(countyNumMap);

                    for (Map.Entry<Integer, Integer> entry : finalCountyNumMap.entrySet()) {
                        if (entry != null) {
                            county = entry.getKey();
                            break;
                        }
                    }

                } else {
                    for (Map.Entry<Integer, List<AgentResearchers>> entry : countyListMap.entrySet()) {
                        if (entry != null) {
                            county = entry.getKey();
                            break;
                        }
                    }
                }
                for (int i = 0; i < v.size(); i++) {
                    if (i == 0) {
                        String finalMaxName = maxName;
                        v.get(i).setName(finalMaxName);
                        Integer finalMaxGender = maxGender;
                        v.get(i).setGender(finalMaxGender);
                        Integer finalMaxjob = maxjob;
                        v.get(i).setJob(finalMaxjob);
                        Integer finalMaxlevel = maxlevel;
                        v.get(i).setLevel(finalMaxlevel);
                        String finalMaxgrade = maxgrade;
                        v.get(i).setGrade(finalMaxgrade);
                        Subject finalSubject = subject;
                        v.get(i).setSubject(finalSubject);
                        Integer finalProvince = province;
                        v.get(i).setProvince(finalProvince);
                        Integer finalCity = city;
                        v.get(i).setCity(finalCity);
                        Integer finalCounty = county;
                        v.get(i).setCounty(finalCounty);
                        v.get(i).setDisabled(false);
                    } else {
                        v.get(i).setDisabled(true);
                    }
                }
            } else {
                v.forEach(p -> p.setDisabled(false));
            }
            v.forEach(p -> agentResearchersPersistence.upsert(p));
        });
        System.out.println(JsonUtils.toJson(phoneListMap));

        //把教研员的map处理成

        //系统中所有专员
        Set<Long> allDevlopersId = baseOrgService.findAllGroupUserIds(AgentRoleType.BusinessDeveloper);
        //处理教研员与专员关系 （加入私海）
        Map<String, Long> phoneUserMap = new HashMap<>();
        List<AgentUserResearchers> userResearchers = new ArrayList<>();
        phoneListMap.forEach((k, v) -> {
            //获取出最终有效的那条教研员记录
            AgentResearchers agentResearchers = v.stream().filter(p -> p.getDisabled() == false).findFirst().orElse(null);

            v.forEach(r -> {
                if (allDevlopersId.contains(r.getAgentUserId())) {//是专员时
                    AgentUserResearchers agentUserResearchers = new AgentUserResearchers();
                    agentUserResearchers.setDisabled(false);
                    agentUserResearchers.setResearchersId(agentResearchers != null ? agentResearchers.getId() : null);
                    agentUserResearchers.setUserId(r.getAgentUserId());
                    userResearchers.add(agentUserResearchers);
                }
            });
            phoneUserMap.put(k, agentResearchers != null ? agentResearchers.getAgentUserId() : null);
        });
        if (userResearchers.size() > 0) {
         /*   for(int i = 0 ;i< userResearchers.size()/100;i++){
                List<AgentUserResearchers> subList = userResearchers.subList(i*2,(i+1)*2);
                System.out.println("第"+ i + "次循环");
                System.out.println(JsonUtils.toJson(subList));
                agentUserResearchersDao.$inserts(subList);
            }*/
            int listSize = userResearchers.size();
            int toIndex = 100;
            for (int i = 0; i < userResearchers.size(); i += toIndex) {
                if (i + toIndex > listSize) {        //作用为toIndex最后没有100条数据则剩余几条newList中就装几条
                    toIndex = listSize - i;
                }
                List<AgentUserResearchers> subList = userResearchers.subList(i, i + toIndex);
                System.out.println("第" + i + "次循环" + JsonUtils.toJson(subList));
                agentUserResearchersDao.$inserts(subList);
            }
        }


    }

    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                return (e2.getValue()).compareTo(e1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public boolean isJuniorResearchers(String grade) {
        try {
            if (StringUtils.isNotBlank(grade)) {
                Map<String, Object> map = JsonUtils.convertJsonObjectToMap(grade);
                if (map == null) {
                    return false;
                }
                List middleList = (List) map.get("middle");
                List highList = (List) map.get("high");
                List list = (List) map.get("junior");
                if (CollectionUtils.isEmpty(middleList) && CollectionUtils.isEmpty(highList) && CollectionUtils.isEmpty(list)) {
                    return true;//为空 即负责年级范围不对 先显示出来
                }
                return CollectionUtils.isNotEmpty(list);
            } else
                return true;//为空 即负责年级范围不对 先显示出来

        } catch (Exception e) {
            logger.error("isJuniorResearchers  判断学校是否小学异常 ");
            return false;
        }
    }

    public boolean isMiddleResearchers(String grade) {
        try {
            if (StringUtils.isNotBlank(grade)) {
                Map<String, Object> map = JsonUtils.convertJsonObjectToMap(grade);
                if (map == null) {
                    return false;
                }
                List middleList = (List) map.get("middle");
                List highList = (List) map.get("high");
                List list = (List) map.get("junior");
                if (CollectionUtils.isEmpty(middleList) && CollectionUtils.isEmpty(highList) && CollectionUtils.isEmpty(list)) {
                    return true;//为空 即负责年级范围不对 先显示出来
                }
                return CollectionUtils.isNotEmpty(middleList) || CollectionUtils.isNotEmpty(highList);
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error("isMiddleResearchers  判断学校是否中学异常 ");
            return false;
        }

    }

    public boolean isPrivateResearchers(Long userId, Long researchersId) {
        return CollectionUtils.isNotEmpty(agentUserResearchersDao.findByUserIdAndResearchersId(userId, researchersId));
    }

    public String getCityRegion(AgentResearchers researchers) {
        Integer level = SafeConverter.toInt(researchers.getLevel());
        if (level == 1) {
            ExRegion region = raikouSystem.loadRegion(SafeConverter.toInt(researchers.getProvince()));
            if (region != null) {
                return region.getProvinceName();
            }
        }
        if (level == 2) {
            ExRegion city = raikouSystem.loadRegion(SafeConverter.toInt(researchers.getCity()));
            if (city != null) {
                return String.format("%s %s", city.getProvinceName(), city.getCityName());
            }
        }
        if (level == 3) {
            ExRegion country = raikouSystem.loadRegion(SafeConverter.toInt(researchers.getCounty()));
            if (country != null) {
                return String.format("%s %s %s", country.getProvinceName(), country.getCityName(), country.getCountyName());
            }
        }
        return null;
    }

    public String getCityRegion(Integer level, Integer regionCOde) {
        if (level == 1) {
            ExRegion region = raikouSystem.loadRegion(regionCOde);
            if (region != null) {
                return region.getProvinceName();
            }
        }
        if (level == 2) {
            ExRegion city = raikouSystem.loadRegion(regionCOde);
            if (city != null) {
                return String.format("%s %s", city.getProvinceName(), city.getCityName());
            }
        }
        if (level == 3) {
            ExRegion country = raikouSystem.loadRegion(regionCOde);
            if (country != null) {
                return String.format("%s %s %s", country.getProvinceName(), country.getCityName(), country.getCountyName());
            }
        }
        return null;
    }

    public void saveUpdateLog(Long userId, String userName, AgentResearchers oldMsg, AgentResearchers newMsg, Long researchersId) {
        List<Map<String, Object>> updateItmes = changedItems(oldMsg, newMsg);
        if (updateItmes.size() == 0) {
            return;
        }
        AgentResearchersUpdateLog agentResearchersUpdateLog = new AgentResearchersUpdateLog();
        agentResearchersUpdateLog.setUserId(userId);
        agentResearchersUpdateLog.setUpdateName(userName);
        agentResearchersUpdateLog.setResearchersId(researchersId);
//        agentResearchersUpdateLog.setOldResearchers(oldMsg);
//        agentResearchersUpdateLog.setNewResearchers(newMsg);
        agentResearchersUpdateLog.setUpdateItems(updateItmes);
        agentResearchersUpdateLogDao.insert(agentResearchersUpdateLog);
    }

    private List<Map<String, Object>> changedItems(AgentResearchers oldMsg, AgentResearchers newMsg) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!Objects.equals(oldMsg.getPhone(), newMsg.getPhone())) {
            Map<String, Object> map = new HashMap<>();
            saveVal("phone", oldMsg.getPhone(), newMsg.getPhone(), map);
            result.add(map);
        }
        if (oldMsg.getJob() != newMsg.getJob()) {
            Map<String, Object> map = new HashMap<>();
            saveVal("job", oldMsg.getJob(), newMsg.getJob(), map);
            result.add(map);
        }
        if (oldMsg.getSubject().getId() != newMsg.getSubject().getId()) {
            Map<String, Object> map = new HashMap<>();
            saveVal("subject", oldMsg.getSubject().getValue(), newMsg.getSubject().getValue(), map);
            result.add(map);
        }
        if (!Objects.equals(oldMsg.getGradeStr(), newMsg.getGradeStr())) {
            Map<String, Object> map = new HashMap<>();
            saveVal("gradeStr", oldMsg.getGradeStr(), newMsg.getGradeStr(), map);
            result.add(map);
        }
        if (!Objects.equals(oldMsg.getName(), newMsg.getName())) {
            Map<String, Object> map = new HashMap<>();
            saveVal("name", oldMsg.getName(), newMsg.getName(), map);
            result.add(map);
        }
        if (!Objects.equals(oldMsg.getCounty(), newMsg.getCounty()) || !Objects.equals(oldMsg.getProvince(), newMsg.getProvince()) || !Objects.equals(oldMsg.getCity(), newMsg.getCity())) {
            Map<String, Object> map = new HashMap<>();
            saveVal("cityRegion", getCityRegion(oldMsg), getCityRegion(newMsg), map);
            result.add(map);
        }
        if (oldMsg.getGender() != newMsg.getGender()) {
            Map<String, Object> map = new HashMap<>();
            saveVal("gender", oldMsg.getGender(), newMsg.getGender(), map);
            result.add(map);
        }
        if (!(StringUtils.isNotEmpty(oldMsg.getSpecificJob())) && StringUtils.isNotBlank(newMsg.getSpecificJob()) && Objects.equals(oldMsg.getSpecificJob(), newMsg.getSpecificJob())) {
            Map<String, Object> map = new HashMap<>();
            saveVal("specificJob", oldMsg.getSpecificJob(), newMsg.getSpecificJob(), map);
            result.add(map);
        }
        return result;
    }

    public void saveVal(String key, Object oldVal, Object newVal, Map<String, Object> map) {
        map.put("key", key);
        map.put("oldVal", oldVal);
        map.put("newVal", newVal);
    }

    public List<AgentResearchersUpdateLog> findUpdateLogsResearchersId(Long researchersId) {
        return agentResearchersUpdateLogDao.findByUserIdAndResearchersId(researchersId);
    }

    public MapMessage checkResearchersPhone(String phone, Long id, Long userId) {
        AgentResearchers agentResearchers = agentResearchersPersistence.load(id);
        if (agentResearchers != null) {
            if (phone.equals(agentResearchers.getPhone())) {
                AgentUserResearchers agentUserResearchers = new AgentUserResearchers();
                agentUserResearchers.setDisabled(false);
                agentUserResearchers.setResearchersId(id);
                agentUserResearchers.setUserId(userId);
                agentUserResearchersDao.insert(agentUserResearchers);
                return MapMessage.successMessage().add("info", "验证通过，已归入私海");
            } else
                return MapMessage.errorMessage("手机号验证不通过，请重试");
        } else
            return MapMessage.errorMessage("根据教研员id未查到教研员信息");

    }

    public void initGradeData() {
        Long totalCount = agentResearchersPersistence.count();
        int pageCount;
        int pageSize = 20;
        if (totalCount % pageSize == 0) {
            pageCount = (totalCount.intValue()) / pageSize;
        } else {
            pageCount = totalCount.intValue() / pageSize + 1;
        }
        for (int i = 0; i < pageCount; i++) {
            Pageable pageable = new PageRequest(i, pageSize);
            List<AgentResearchers> list = agentResearchersPersistence.findByPage(pageable);
            list.forEach(p -> {
                p.setGradeStr(changeGradeForms(p.getGrade()));
                agentResearchersPersistence.upsert(p);
            });

            System.out.println("第" + i + "次循环 ，本次有" + list.size());
        }
    }

    private String changeGradeForms(String grade) {
        Map<String, Object> map = new HashMap<>();
        List<Integer> junior = new ArrayList<>();//小学
        List<Integer> middle = new ArrayList<>();//初中
        List<Integer> high = new ArrayList<>();//高中
        map.put("junior", junior);
        map.put("middle", middle);
        map.put("high", high);
        if (StringUtils.isBlank(grade)) {
            return JsonUtils.toJson(map);
        }
        if (grade.contains("junior") || grade.contains("middle") || grade.contains("high")) {
            return grade;
        }
        //{"junior":[2],"middle":[],"high":[]}
        String[] array = grade.split(",");
        if (array.length > 0) {
            //年级只选了6默认小学  后期根据id更新中学的几个教研员
            if (array.length == 1 && "6".equals(array[0])) {
                junior.add(Integer.valueOf(array[0]));
            } else {
                for (int i = 0; i < array.length; i++) {
                    if ("1".equals(array[i]) || "2".equals(array[i]) || "3".equals(array[i]) || "4".equals(array[i]) || "5".equals(array[i])) {
                        junior.add(Integer.valueOf(array[i]));
                    } else if ("6".equals(array[i])) {
                        if (i > 0) {//前边有年级  证明可以放小学
                            junior.add(Integer.valueOf(array[i]));
                        }
                        if (i < array.length - 1) {//前边有年级  证明可以放小学
                            middle.add(Integer.valueOf(array[i]));
                        }
                    } else if ("7".equals(array[i]) || "8".equals(array[i]) || "9".equals(array[i])) {
                        middle.add(Integer.valueOf(array[i]));
                    } else if ("10".equals(array[i]) || "11".equals(array[i]) || "12".equals(array[i])) {
                        high.add(Integer.valueOf(array[i]));
                    }
                }
            }

        }
        return JsonUtils.toJson(map);
    }

    public boolean isBusinessDeveloper() {
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        if (roleType == AgentRoleType.BusinessDeveloper)//专员时
            return true;
        else
            return false;
    }

    public boolean isManager() {
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        if (roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager)
            return true;
        else
            return false;
    }

    private Integer getSchoolPhaseBygrade(String gradeStr) {
//       1 小学 2 中学 3 小学中学都可见
        int schoolPhase = 0;
        Map<String, Object> map = JsonUtils.convertJsonObjectToMap(gradeStr);
        if (map == null) {
            return schoolPhase;
        }
        List middleList = (List) map.get("middle");
        List highList = (List) map.get("high");
        List list = (List) map.get("junior");
        if (CollectionUtils.isNotEmpty(list)) {
            schoolPhase = schoolPhase + 1;
        }
        if (CollectionUtils.isNotEmpty(middleList) || CollectionUtils.isNotEmpty(highList)) {
            schoolPhase = schoolPhase + 2;
        }
        return schoolPhase;
    }


    public void initSchoolPhase() {
        Long totalCount = agentResearchersPersistence.count();
        int pageCount;
        int pageSize = 20;
        if (totalCount % pageSize == 0) {
            pageCount = (totalCount.intValue()) / pageSize;
        } else {
            pageCount = totalCount.intValue() / pageSize + 1;
        }
        for (int i = 0; i < pageCount; i++) {
            Pageable pageable = new PageRequest(i, pageSize);
            List<AgentResearchers> list = agentResearchersPersistence.findByPage(pageable);
            list.forEach(p -> {
                p.setSchoolPhase(this.getSchoolPhaseBygrade(p.getGradeStr()));
                agentResearchersPersistence.upsert(p);
            });

            System.out.println("第" + i + "次循环 ，本次有" + list.size());
        }
    }


    //查找出被合并的类型为其他的教研员
    public Collection<AgentResearchers> findDeleteOtherJobResearcher() {
        List<String> deletePhoneList = getdeleteResearchers();
        Map<String, List<AgentResearchers>> researchers = agentResearchersPersistence.findAgentResearchersByPhones(deletePhoneList);
        List<AgentResearchers> result = new ArrayList<>();
        researchers.forEach((k, v) -> {
            List<AgentResearchers> item = v.stream().filter(p -> p.getDisabled() == false).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(item)) {
                result.addAll(item);
            }
        });
        return result;
    }

    public List<String> getdeleteResearchers() {
        List<String> deletePhoneList = agentResearchersPersistence.findAgentResearchersByUserId(getCurrentUserId()).stream().filter(p -> p.getDisabled() && p.getJob() == 2 && p.getCreateDatetime().before(DateUtils.stringToDate("2018-10-11", "yyyy-MM-dd"))).map(AgentResearchers::getPhone).collect(Collectors.toList());
        return deletePhoneList;
    }

    //合并时被删除的教研员对应手机号的教研员查询出给员创建人也添加到私有
    public void initDelete2Private() {
        List<AgentResearchers> agentResearchers = agentResearchersPersistence.findAgentResearchersByJob(2);
        agentResearchers.forEach(p -> {
            if (p.getDisabled() == false) {
                List<AgentUserResearchers> userResearchersList = agentUserResearchersDao.findByUserIdAndResearchersId(p.getAgentUserId(), p.getId());
                if (CollectionUtils.isEmpty(userResearchersList)) {
                    AgentUserResearchers agentUserResearchers = new AgentUserResearchers();
                    agentUserResearchers.setDisabled(false);
                    agentUserResearchers.setResearchersId(p.getId());
                    agentUserResearchers.setUserId(p.getAgentUserId());
                    agentUserResearchersDao.insert(agentUserResearchers);
                }
            } else {//删除状态
                Map<String, List<AgentResearchers>> mapList = agentResearchersPersistence.findAgentResearchersByPhones(Collections.singleton(p.getPhone()));
                if (MapUtils.isNotEmpty(mapList)) {
                    mapList.forEach((k, v) -> {
                        List<AgentUserResearchers> userResearchersList = agentUserResearchersDao.findByUserIdAndResearchersId(p.getAgentUserId(), v.get(0).getId());
                        if (CollectionUtils.isEmpty(userResearchersList)) {
                            AgentUserResearchers agentUserResearchers = new AgentUserResearchers();
                            agentUserResearchers.setDisabled(false);
                            agentUserResearchers.setResearchersId(v.get(0).getId());
                            agentUserResearchers.setUserId(p.getAgentUserId());
                            agentUserResearchersDao.insert(agentUserResearchers);
                        }
                    });


                }
            }
        });
    }

    public List<Map<String, Object>> getUserCityRegion(Collection<ExRegion> regions) {
        if (CollectionUtils.isEmpty(regions)) {
            return Collections.emptyList();
        }
//        Map<Integer,ExRegion> regionMap = regions.stream().collect(Collectors.toMap(ExRegion::getCityCode, Function.identity()));
        Map<String, List<ExRegion>> map = regions.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getCityName())));
        Map<String, List<Map<String, Object>>> mapMap = new HashMap<>();

        Set<Integer> codeSet = new HashSet<>();
        map.forEach((k, v) -> {
            List<Map<String, Object>> list = new ArrayList<>();
            v.forEach(c -> {
                if (codeSet.contains(c.getCityCode())) {
                    return;
                }
                Map<String, Object> subCityMap = new TreeMap<>();
                codeSet.add(c.getCityCode());
                subCityMap.put("cityCode", c.getCityCode());
                subCityMap.put("cityName", c.getCityName());
                list.add(subCityMap);
            });
            mapMap.put(k, list);
        });
        //排序
        Map<String, Object> sortedMap = new TreeMap<String, Object>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        // 降序排序
                        return obj1.compareTo(obj2);
                    }
                });
        sortedMap.putAll(mapMap);
        //
        List<Map<String, Object>> resultList = new ArrayList<>();
        sortedMap.forEach((k, v) -> {
            Map<String, Object> mapResult = new LinkedHashMap<>();
            mapResult.put("letter", k);
            mapResult.put("regions", v);
            resultList.add(mapResult);
        });
        return resultList;
    }

    public MapMessage getResearchersInfo(Long id) {
        MapMessage mapMessage = MapMessage.successMessage();
        AgentResearchers researchers = agentResearchersPersistence.load(id);
        if (researchers != null) {
            Map<String, Object> resultMap = new HashMap<>();
            boolean isPrivate = this.isPrivateResearchers(getCurrentUserId(), id);
            if ((isPrivate && this.isBusinessDeveloper()) || this.isManager()) {
                MapMessage.errorMessage("没有权限查看数据");
            }
            resultMap.put("id", researchers.getId());
            resultMap.put("name", researchers.getName());
            resultMap.put("gender", researchers.getGender());
            resultMap.put("subject", researchers.getSubject().getId());
            resultMap.put("subjectName", researchers.getSubject().getValue());

            resultMap.put("gradeStr", researchers.getGradeStr());
            resultMap.put("job", researchers.getJob());
            ResearchersJobType researchersJobType = ResearchersJobType.typeOf(researchers.getJob());
            resultMap.put("jobName", researchersJobType != null ? researchersJobType.getJobName() : "");
            resultMap.put("phone", researchers.getPhone());
            resultMap.put("telephone", "");
            resultMap.put("department", "");
            resultMap.put("organizationName", "");
            resultMap.put("organizationId", "");
            resultMap.put("remarks", researchers.getSpecificJob());

            List<Map<String, Object>> visitList = this.loadResearchersRecordsInfo(getCurrentUserId(), id);
            resultMap.put("visitList", visitList);
            mapMessage.put("data", resultMap);
        } else {
            return MapMessage.errorMessage("信息不存在！");
        }
        return mapMessage;
    }

    public MapMessage loadResearchersById(Long id) {
        MapMessage mapMessage = MapMessage.successMessage();
        AgentResearchers researchers = agentResearchersPersistence.load(id);
        if (researchers != null) {
            boolean isManager = this.isManager();
            boolean isPrivate = this.isPrivateResearchers(getCurrentUserId(), id);
            if (!isPrivate && !isManager) {
                return MapMessage.errorMessage("无权限不可编辑");
            }
        } else {
            researchers = new AgentResearchers();
        }

        Map<String, Object> map = new HashMap<>();

        map.put("id", researchers.getId());
        map.put("name", researchers.getName());
        map.put("gender", researchers.getGender());
        map.put("phone", researchers.getPhone());
        map.put("telephone", "");
        map.put("specificJob", researchers.getSpecificJob());
        map.put("job", researchers.getJob());
        map.put("subject", researchers.getSubject() == null ? null : researchers.getSubject().getId());
        map.put("subjectName", researchers.getSubject() == null ? null : researchers.getSubject().getValue());
        map.put("gradeStr", researchers.getGradeStr());
        ResearchersJobType researchersJobType = ResearchersJobType.typeOf(researchers.getJob());
        map.put("grResource", ResearchersJobType.sgrResourceJobType(researchersJobType));
        return mapMessage.add("dataMap", map);
    }

    //查询教研员列表  标记是否私海
    public List<Map<String, Object>> searchResearcherList(Long userId, String name, Pageable pageable) {
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        Map<String, Set<Integer>> regionMap = getRegionCodes(userId, roleType);
        Set<Integer> provinceFilter = regionMap.get("province");
        Set<Integer> cityFilter = regionMap.get("city");
        Set<Integer> countyFilter = regionMap.get("county");

        //未关联机构的旧教研员数据
        List<AgentResearchers> oldResearchers = searchUserResearchersList(userId, provinceFilter, cityFilter, countyFilter, name);
        List<AgentResearchers> researcherList = new ArrayList<>();
        if (pageable.getPageNumber() == 0) {
            Collection<AgentResearchers> deleteList = findDeleteOtherJobResearcher();
            if (CollectionUtils.isNotEmpty(deleteList)) {
                researcherList.addAll(deleteList);
            }
        }
        if (CollectionUtils.isNotEmpty(oldResearchers)) {
            researcherList.addAll(oldResearchers);
        }
        List<Map<String, Object>> oldList = assembleOldList(researcherList);

        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(oldList)) {
            result.addAll(oldList);
        }
        List<Map<String, Object>> resourceList = agentOuterResourceService.getUserOuterAndSchoolResource(provinceFilter, cityFilter, countyFilter, name);
        if (CollectionUtils.isNotEmpty(resourceList)) {
            result.addAll(resourceList);
        }

        return result;
    }

    public List<AgentResearchers> searchUserResearchersList(Long userId, Collection<Integer> provinceCodes, Collection<Integer> cityCodes, Collection<Integer> countyCodes, String name) {
        List<AgentGroup> groups = baseOrgService.getUserGroups(userId);
        if (CollectionUtils.isEmpty(groups)) {
            return null;
        }
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        List<AgentResearchers> allResearchers = new ArrayList<>();
        if (roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.BusinessDeveloper) {

            allResearchers = agentResearchersPersistence.findAgentResearchersPage(userId, provinceCodes, cityCodes, countyCodes, name, null);
        } else if (roleType == AgentRoleType.Country) {//全国总监单独走逻辑
            allResearchers = agentResearchersPersistence.findAgentResearchersPage(userId, null, null, null, name, null);
        }
        return allResearchers;
    }

    public Map<String, Set<Integer>> getRegionCodes(Long userId, AgentRoleType roleType) {
        Collection<ExRegion> counties = baseOrgService.getCountyRegion(userId, roleType);
        Set<Integer> provinceSet = new LinkedHashSet<>();
        Set<Integer> citySet = new LinkedHashSet<>();
        Set<Integer> countySet = new LinkedHashSet<>();
        counties.forEach(p -> {
            provinceSet.add(p.getProvinceCode());
            citySet.add(p.getCityCode());
            countySet.add(p.getCountyCode());
        });
        Set<Integer> provinceFilter = new LinkedHashSet<>();
        Set<Integer> cityFilter = new LinkedHashSet<>();
        Set<Integer> countyFilter = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(provinceSet)) {
            provinceFilter.addAll(provinceSet);
        }
        if (CollectionUtils.isNotEmpty(citySet)) {
            cityFilter.addAll(citySet);
        }
        if (CollectionUtils.isNotEmpty(countySet)) {
            countyFilter.addAll(countySet);
        }
        Map<String, Set<Integer>> resultMap = new HashMap<>();
        resultMap.put("province", provinceFilter);
        resultMap.put("city", cityFilter);
        resultMap.put("county", countyFilter);
        return resultMap;
    }
}
