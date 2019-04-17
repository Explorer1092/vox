package com.voxlearning.utopia.agent.service.mobile.resource;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.athena.LoadParentServiceClient;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.SchoolBasicInfo;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolParentIndicator;
import com.voxlearning.utopia.agent.bean.performance.school.*;
import com.voxlearning.utopia.agent.bean.resource.SingleSubjectAnsh;
import com.voxlearning.utopia.agent.bean.school.AgentHighPotentialSchoolInfo;
import com.voxlearning.utopia.agent.bean.school.AgentMauTopSchoolInfo;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.constants.*;
import com.voxlearning.utopia.agent.dao.mongo.taskmanage.AgentMainTaskDao;
import com.voxlearning.utopia.agent.dao.mongo.taskmanage.AgentSubTaskDao;
import com.voxlearning.utopia.agent.persist.entity.AgentCompetitiveProduct;
import com.voxlearning.utopia.agent.persist.entity.AgentTargetTag;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.persist.entity.schoollastworkrecord.AgentSchoolLastWorkRecord;
import com.voxlearning.utopia.agent.persist.entity.taskmanage.AgentMainTask;
import com.voxlearning.utopia.agent.persist.entity.taskmanage.AgentSubTask;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.competitiveproduct.AgentCompetitiveProductService;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.agent.service.mobile.AgentTargetTagService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.service.schoollastworkrecord.AgentSchoolLastWorkRecordService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskManageService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.agent.view.school.SchoolBasicData;
import com.voxlearning.utopia.agent.view.school.SchoolBasicExtData;
import com.voxlearning.utopia.agent.view.school.SchoolGradeBasicData;
import com.voxlearning.utopia.agent.view.school.SchoolPositionData;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.TeacherTaskType;
import com.voxlearning.utopia.entity.TeacherRoles;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.reward.client.TeacherTaskRewardHistoryServiceClient;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.constants.TeacherRolesType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.TeacherRolesServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


/**
 * SchoolResourceService
 *
 * @author song.wang
 * @date 2017/7/17
 */
@Named
public class SchoolResourceService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private AgentDictSchoolService agentDictSchoolService;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private AgentTargetTagService agentTargetTagService;
    @Inject
    private AmbassadorLoaderClient ambassadorLoaderClient;

    @Inject
    private WorkRecordService workRecordService;
    @Inject
    private AgentMemorandumService agentMemorandumService;
    @Inject
    private RewardLoaderClient rewardLoaderClient;

    @Inject
    private AgentResourceService agentResourceService;
    @Inject
    private SpecialTeacherLoaderClient specialTeacherLoaderClient;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private TeacherTaskRewardHistoryServiceClient teacherTaskRewardHistoryServiceClient;
    @Inject
    private SearchService searchService;
    @Inject
    private AgentRegionService agentRegionService;
    @Inject
    private TeacherRolesServiceClient teacherRolesServiceClient;
    @Inject
    private AgentCompetitiveProductService agentCompetitiveProductService;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private SchoolClueService schoolClueService;
    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;
    @Inject
    private AgentMainTaskDao agentMainTaskDao;
    @Inject
    private AgentSubTaskDao agentSubTaskDao;
    @Inject
    private LoadParentServiceClient loadParentServiceClient;
    @Inject
    private AgentSchoolLastWorkRecordService agentSchoolLastWorkRecordService;
    @Inject
    private AgentTaskManageService agentTaskManageService;
    @Inject
    private AgentTagService agentTagService;

    private static final Long OTHER_ID = 99999999L;

    public List<SchoolBasicInfo> searchSchool(Long userId, String schoolKey, Integer scene) {
        List<Long> searchSchoolIds = searchService.searchSchoolsForSceneWithNew(userId, schoolKey, scene);

        searchSchoolIds = sortOtherDictSchool(userId, searchSchoolIds).stream().collect(toList());
        Map<Long, School> schoolMap = raikouSystem.loadSchools(searchSchoolIds);
        List<School> schoolList = schoolMap.values().stream()
                .filter(p -> p.getSchoolAuthenticationState() == AuthenticationState.WAITING || p.getSchoolAuthenticationState() == AuthenticationState.SUCCESS)
                .collect(Collectors.toList());
        return generateSchoolBasicInfoBySchools(schoolList);
    }

    public List<SchoolBasicInfo> generateSchoolBasicInfoWithPage(Page<SchoolEsInfo> esInfoPage, Long userId) {
        //学校ID与距离对应关系
        Map<Long, String> schoolIdGenDistanceMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(esInfoPage.getContent())) {
            esInfoPage.getContent().stream().forEach(p -> schoolIdGenDistanceMap.put(SafeConverter.toLong(p.getId()), p.getGenDistance()));
        }
        List<Long> searchSchoolIds = sortOtherDictSchool(userId, schoolIdGenDistanceMap.keySet()).stream().collect(toList());
        Map<Long, School> schoolMap = raikouSystem.loadSchools(searchSchoolIds);
        List<School> schoolList = schoolMap.values().stream()
                .filter(p -> p.getSchoolAuthenticationState() == AuthenticationState.WAITING || p.getSchoolAuthenticationState() == AuthenticationState.SUCCESS)
                .collect(Collectors.toList());
        List<SchoolBasicInfo> schoolBasicInfoList = generateSchoolBasicInfoBySchools(schoolList);
        //拼装距离
        schoolBasicInfoList.forEach(item -> item.setGenDistance(schoolIdGenDistanceMap.get(item.getSchoolId())));
        return schoolBasicInfoList;
    }


    public List<School> searchSchoolBySchoolId(Collection<Long> schoolIds, Collection<Integer> regions, Set<Integer> userCities, AgentRoleType roleType) {
        if (null == schoolIds || null == regions || null == userCities || null == roleType) {
            return Collections.emptyList();
        }
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
        Collection<School> schoolList = schoolMap.values();
        List<School> resultSchools = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(schoolList)) {
            List<Integer> countyCodeList = agentRegionService.getCountyCodes(regions);
            schoolList.forEach(school -> {
                // 根据ID检索
                // 1. 专员搜索范围:  市经理所负责区域的所有学校
                // 2. 市经理搜索范围: 全市, 权限范围内地区 + 本市非字典表学校
                // 3. 大区经理搜索范围: 管辖大区下所有学校
                // 4. 全国总监搜索范围: 全国所有学校
                if (null != school && null != school.getSchoolAuthenticationState()
                        && (school.getSchoolAuthenticationState() == AuthenticationState.WAITING || school.getSchoolAuthenticationState() == AuthenticationState.SUCCESS)
                        && countyCodeList.contains(school.getRegionCode())) {
                    resultSchools.add(school);
                }
            });
        }
        return resultSchools;
    }

    private List<Long> sortOtherDictSchool(Long userId, Collection<Long> schoolIds) {
        List<Long> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(schoolIds)) {
            return resultList;
        }
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        List<AgentDictSchool> dictSchoolList = agentDictSchoolService.loadSchoolDictDataBySchool(schoolIds);
        Set<Long> dictSchoolIds = dictSchoolList.stream().map(AgentDictSchool::getSchoolId).filter(Objects::nonNull).collect(Collectors.toSet());
        // 专员或者市经理优先查询自己管辖的学校
        if (userRole == AgentRoleType.BusinessDeveloper || userRole == AgentRoleType.CityManager) {
            List<Long> immediateSchools = baseOrgService.loadBusinessSchoolByUserId(userId);
            resultList.addAll(schoolIds.stream().filter(immediateSchools::contains).collect(toList()));
        }
        resultList.addAll(schoolIds.stream().filter(p -> !resultList.contains(p) && dictSchoolIds.contains(p)).collect(toList()));
        resultList.addAll(schoolIds.stream().filter(p -> !resultList.contains(p)).collect(toList()));
        return resultList;
    }

    public SchoolBasicInfo generateSchoolBasicInfoById(Long schoolId) {
        List<SchoolBasicInfo> list = generateSchoolBasicInfoByIds(Collections.singleton(schoolId));
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public List<SchoolBasicInfo> generateSchoolBasicInfoByIds(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
        return generateSchoolBasicInfoBySchools(schoolMap.values());
    }

    public List<AgentHighPotentialSchoolInfo> highPotentialSchoolList(AuthCurrentUser currentUser, Long userId, Integer subjectCode, Double longitude, Double latitude, Integer topN, Integer model) {
        Long currentUserId = getCurrentUserId();
        Integer idType; //1：部门ID 2：人员ID
        List<Long> ids = new ArrayList<>();        //部门ID or 人员ID
        List<Integer> schoolLevelList = new ArrayList<>();
        // 专员
        if (currentUser.isBusinessDeveloper()) {
            ids.add(currentUserId);
            idType = 2;
        } else {
            List<AgentGroupUser> groupUser = baseOrgService.getGroupUserByUser(currentUserId);
            Long groupId = groupUser.stream().filter(Objects::nonNull).map(AgentGroupUser::getGroupId).findFirst().orElse(null);
            if (currentUser.isCityManager()) {
                if (userId == 0L) {
                    ids.addAll(baseOrgService.getGroupUsersByRole(groupId, AgentRoleType.BusinessDeveloper));
                } else {
                    ids.add(userId);
                }
                idType = 2;
            } else {
                if (userId == 0L) {
                    ids.add(groupId);
                    idType = 1;
                } else {
                    ids.add(userId);
                    idType = 2;
                }
            }
        }

        if (currentUser.isCountryManager()) {
            schoolLevelList.add(1);
            schoolLevelList.add(2);
            schoolLevelList.add(4);
        } else {
            List<AgentGroup> userGroups = new ArrayList<>();
            if (userId > 0) {
                userGroups = baseOrgService.getUserGroups(userId);
            } else {
                userGroups = baseOrgService.getUserGroups(currentUserId);
            }
            if (CollectionUtils.isNotEmpty(userGroups)) {
                AgentGroup group = userGroups.get(0);
                if (null != group) {
                    if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                        schoolLevelList.add(1);
                    } else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)) {
                        schoolLevelList.add(2);
                        schoolLevelList.add(4);
                    }
                }
            }
        }

        List<Long> schoolIds = new ArrayList<>();
        //获取学校潜力值信息
        List<AgentHighPotentialSchoolInfo> highPotentialSchoolInfoList = loadNewSchoolServiceClient.loadHighPotentialSchoolData(ids, idType, subjectCode, topN, model, schoolLevelList);
        schoolIds.addAll(highPotentialSchoolInfoList.stream().map(AgentHighPotentialSchoolInfo::getSchoolId).collect(toList()));
        //拼装学校基础信息
        Map<Long, AgentHighPotentialSchoolInfo> highPotentialSchoolInfoMap = generateHighPotentialSchoolInfoByIds(schoolIds).stream().collect(Collectors.toMap(AgentHighPotentialSchoolInfo::getSchoolId, Function.identity(), (o1, o2) -> o1));
        highPotentialSchoolInfoList.forEach(item -> {
            AgentHighPotentialSchoolInfo highPotentialSchoolInfo = highPotentialSchoolInfoMap.get(item.getSchoolId());
            try {
                Integer mauPotentialValue = item.getMauPotentialValue();
                BeanUtils.copyProperties(item, highPotentialSchoolInfo);
                item.setMauPotentialValue(mauPotentialValue);
            } catch (Exception e) {
                logger.error("bean copy error", e);
            }
        });

        //拼装学校距离信息
        Page<SchoolEsInfo> schoolEsInfoPage = searchService.searchSchoolWithSchoolIds(schoolIds, longitude, latitude, 0, 100);
        Map<String, SchoolEsInfo> schoolIdEsInfoMap = schoolEsInfoPage.getContent().stream().collect(Collectors.toMap(SchoolEsInfo::getId, Function.identity()));
        highPotentialSchoolInfoList.forEach(item -> {
            SchoolEsInfo schoolEsInfo = schoolIdEsInfoMap.get(ConversionUtils.toString(item.getSchoolId()));
            if (null != schoolEsInfo) {
                item.setGenDistance(schoolEsInfo.getGenDistance());
            }
        });
        return highPotentialSchoolInfoList;
    }

    public List<AgentHighPotentialSchoolInfo> generateHighPotentialSchoolInfoByIds(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
        return generateHighPotentialSchoolInfoBySchools(schoolMap.values());
    }

    private List<AgentHighPotentialSchoolInfo> generateHighPotentialSchoolInfoBySchools(Collection<School> schools) {
        schools = schools.stream().filter(p -> p.getSchoolAuthenticationState() != AuthenticationState.FAILURE).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schools)) {
            return Collections.emptyList();
        }
        Set<Long> schoolIds = schools.stream().map(School::getId).collect(Collectors.toSet());

        // 获取extInfo数据
        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIds).getUninterruptibly();

        Map<Long, List<AgentUserSchool>> userSchoolMap = baseOrgService.getUserSchoolBySchools(schoolIds);
        Set<Long> userIds = new HashSet<>();
        userSchoolMap.forEach((k, v) -> {
            userIds.addAll(v.stream().map(AgentUserSchool::getUserId).collect(Collectors.toSet()));
        });
        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);

        List<AgentHighPotentialSchoolInfo> resultList = new ArrayList<>();
        schools.forEach(school -> {
            AgentHighPotentialSchoolInfo schoolInfo = new AgentHighPotentialSchoolInfo();
            schoolInfo.setSchoolId(school.getId());
            schoolInfo.setSchoolName(school.getCname());
            schoolInfo.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
            schoolInfo.setAuthState(school.getSchoolAuthenticationState());

            SchoolExtInfo schoolExtInfo = extInfoMap.get(school.getId());
            if (null != schoolExtInfo) {
                schoolInfo.setAddress(schoolExtInfo.getAddress());
            }

            List<AgentUserSchool> userSchoolList = userSchoolMap.get(school.getId());
            if (CollectionUtils.isNotEmpty(userSchoolList)) {
                AgentUserSchool userSchool = userSchoolList.get(0);
                schoolInfo.setHasBd(true);
                schoolInfo.setBdId(userSchool.getUserId());
                AgentUser agentUser = userMap.get(userSchool.getUserId());
                if (null != agentUser) {
                    schoolInfo.setBdName(agentUser.getRealName());
                }
            }

            resultList.add(schoolInfo);

        });

        // 设置负责该学校的专员姓名
//        Set<Long> userIds = resultList.stream().filter(AgentHighPotentialSchoolInfo::getHasBd).map(AgentHighPotentialSchoolInfo::getBdId).filter(p -> p != null).collect(Collectors.toSet());
//        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);
//        resultList.forEach(p -> {
//            if (p.getHasBd() && p.getBdId() != null && userMap.get(p.getBdId()) != null) {
//                p.setBdName(userMap.get(p.getBdId()).getRealName());
//            }
//        });

        //获取月活潜力值
        Integer day = performanceService.lastSuccessDataDay();
        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(schoolIds, day);
        resultList.forEach(item -> {
            SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(item.getSchoolId());
            if (null != schoolOnlineIndicator) {
                OnlineIndicator onlineIndicator = schoolOnlineIndicator.fetchMonthData();
                if (null != onlineIndicator) {
                    item.setFinEngHwEq1UnSettleStuCount(onlineIndicator.getFinEngHwEq1UnSettleStuCount() != null ? onlineIndicator.getFinEngHwEq1UnSettleStuCount() : 0);
                    item.setFinEngHwEq2UnSettleStuCount(onlineIndicator.getFinEngHwEq2UnSettleStuCount() != null ? onlineIndicator.getFinEngHwEq2UnSettleStuCount() : 0);

                    item.setFinMathHwEq1UnSettleStuCount(onlineIndicator.getFinMathHwEq1UnSettleStuCount() != null ? onlineIndicator.getFinMathHwEq1UnSettleStuCount() : 0);
                    item.setFinMathHwEq2UnSettleStuCount(onlineIndicator.getFinMathHwEq2UnSettleStuCount() != null ? onlineIndicator.getFinMathHwEq2UnSettleStuCount() : 0);

                    item.setFinChnHwEq1UnSettleStuCount(onlineIndicator.getFinChnHwEq1UnSettleStuCount() != null ? onlineIndicator.getFinChnHwEq1UnSettleStuCount() : 0);
                    item.setFinChnHwEq2UnSettleStuCount(onlineIndicator.getFinChnHwEq2UnSettleStuCount() != null ? onlineIndicator.getFinChnHwEq2UnSettleStuCount() : 0);
                }
            }
        });
        return resultList;
    }


    private List<SchoolBasicInfo> generateSchoolBasicInfoBySchools(Collection<School> schools) {
        schools = schools.stream().filter(p -> p.getSchoolAuthenticationState() != AuthenticationState.FAILURE).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schools)) {
            return Collections.emptyList();
        }
        Set<Long> schoolIds = schools.stream().map(School::getId).collect(Collectors.toSet());

        // 获取extInfo数据
        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIds).getUninterruptibly();

        // 获取字典表数据
        Map<Long, AgentDictSchool> dictSchoolMap = agentDictSchoolService.loadSchoolDictDataBySchool(schoolIds).stream().collect(Collectors.toMap(AgentDictSchool::getSchoolId, Function.identity(), (o1, o2) -> o1));

        Map<Long, List<AgentUserSchool>> userSchoolMap = baseOrgService.getUserSchoolBySchools(schoolIds);

        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(schools.stream().map(School::getRegionCode).filter(Objects::nonNull).collect(Collectors.toSet()));

        Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(schoolIds);

        Integer day = performanceService.lastSuccessDataDay();
        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(schoolIds, day);


        //获取学校最新提交审核信息
        Map<Long, CrmSchoolClue> schoolClueMap = schoolClueService.findSchoolIdIs(schoolIds);

        //获取学校与最近一次拜访时间对应关系
        Map<Long, AgentSchoolLastWorkRecord> schoolLastWorkRecordMap = agentSchoolLastWorkRecordService.getLastVisitTimeBySchoolIds(schoolIds);

        //获取学校标签
        List<String> schoolIdList = new ArrayList<>();
        schoolIds.forEach(item -> schoolIdList.add(SafeConverter.toString(item)));
        Map<String, List<com.voxlearning.utopia.agent.persist.entity.tag.AgentTag>> schoolTagMap = agentTagService.getTagListByTargetIdsAndType(schoolIdList, AgentTagTargetType.SCHOOL, true);

        List<SchoolBasicInfo> resultList = new ArrayList<>();
        schools.forEach(school -> {
            SchoolBasicInfo schoolInfo = new SchoolBasicInfo();
            schoolInfo.setSchoolId(school.getId());
            schoolInfo.setSchoolName(school.getCname());
            schoolInfo.setShortName(school.getShortName());
            ExRegion region = exRegionMap.get(school.getRegionCode());
            if (null != region) {
                schoolInfo.setProvinceName(region.getProvinceName());
                schoolInfo.setCityName(region.getCityName());
                schoolInfo.setCountyName(region.getCityName());
            }
            schoolInfo.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
            schoolInfo.setAuthState(school.getSchoolAuthenticationState());

            // 设置学校规模，缩略图，是否开通扫描仪
            SchoolExtInfo schoolExtInfo = extInfoMap.get(school.getId());
            if (schoolExtInfo != null) {
                schoolInfo.setSchoolScale(SafeConverter.toInt(schoolExtInfo.getSchoolSize()));
                Boolean hasThumbnail = StringUtils.isNotBlank(schoolExtInfo.getPhotoUrl()) && schoolExtInfo.getPhotoUrl().contains("oss-image");
                schoolInfo.setHasThumbnail(hasThumbnail);
                schoolInfo.setThumbnailUrl(schoolExtInfo.getPhotoUrl());
                schoolInfo.setScannerFlag(schoolExtInfo.isScanMachineFlag());
                schoolInfo.setAddress(schoolExtInfo.getAddress());  //地址

                if (null != schoolExtInfo.getCompetitiveProductFlag()) {
                    schoolInfo.setCompetitiveProductFlag(schoolExtInfo.getCompetitiveProductFlag());
                } else {
                    schoolInfo.setCompetitiveProductFlag(0);
                }
                schoolInfo.setExternOrBoarder(schoolExtInfo.getExternOrBoarder());
            }

            //获取学校最新提交审核信息
            CrmSchoolClue crmSchoolClue = schoolClueMap.get(school.getId());
            //待审核
            if (crmSchoolClue != null && crmSchoolClue.getStatus() != null && Objects.equals(crmSchoolClue.getStatus(), 1)) {
                schoolInfo.setAuditStatus(1);
            }

            // 设置是否开通扫描仪
            AgentDictSchool dictSchool = dictSchoolMap.get(school.getId());
            //schoolInfo.setSchoolPopularityType(AgentSchoolPopularityType.B);
            if (dictSchool != null) {
                schoolInfo.setIsDictSchool(true);
                if (dictSchool.getCreateDatetime().after(DateUtils.calculateDateDay(new Date(), -7))) {
                    schoolInfo.setIsNew(true);
                }
                schoolInfo.setSchoolPopularityType(dictSchool.getSchoolPopularity());


                SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(school.getId());
                if (null != schoolOnlineIndicator) {
                    OnlineIndicator onlineIndicator = schoolOnlineIndicator.fetchMonthData();
                    if (null != onlineIndicator) {
                        schoolInfo.setPermeabilityType(getSchoolSubjectPermeabilityType(onlineIndicator.getMaxPenetrateRateSglSubj()));
                    }
                }

//                schoolInfo.setPermeabilityType(dictSchool.getPermeabilityType());

                List<AgentUserSchool> userSchoolList = userSchoolMap.get(school.getId());
                if (CollectionUtils.isNotEmpty(userSchoolList)) {
                    AgentUserSchool userSchool = userSchoolList.get(0);
                    schoolInfo.setHasBd(true);
                    schoolInfo.setBdId(userSchool.getUserId());
                    if (userSchool.getCreateDatetime().after(DateUtils.calculateDateDay(new Date(), -7))) {
                        schoolInfo.setIsNew(true);
                    } else {
                        schoolInfo.setIsNew(false);
                    }
                }
            }

            //拼装学校最近拜访时间
            Long lastVisitTime = null;
            AgentSchoolLastWorkRecord schoolLastWorkRecord = schoolLastWorkRecordMap.get(school.getId());
            if (schoolLastWorkRecord != null && schoolLastWorkRecord.getLastVisitTime() != null) {
                lastVisitTime = schoolLastWorkRecord.getLastVisitTime().getTime();
            } else {
                CrmSchoolSummary crmSchoolSummary = crmSchoolSummaryMap.get(school.getId());
                if (crmSchoolSummary != null && crmSchoolSummary.getLatestVisitTime() != null) {
                    lastVisitTime = crmSchoolSummary.getLatestVisitTime();
                }
            }
            if (lastVisitTime != null) {
                Date lastVisitDate = new Date(lastVisitTime);
                schoolInfo.setLastVisitTimeLong(lastVisitTime);
                schoolInfo.setLastVisitTime(ConversionUtils.toString(DateUtils.dateToString(lastVisitDate, "yyyy/MM/dd")));
            }

            SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(school.getId());
            if (schoolOnlineIndicator != null) {
                //累计数据
                OnlineIndicator sumOnlineIndicator = schoolOnlineIndicator.fetchSumData();
                if (sumOnlineIndicator != null) {
                    schoolInfo.setRegStuCount(SafeConverter.toInt(sumOnlineIndicator.getRegStuCount()));
                }
                //当月数据
                OnlineIndicator monthOnlineIndicator = schoolOnlineIndicator.fetchMonthData();
                if (monthOnlineIndicator != null) {
                    schoolInfo.setSglSubjMauc(SafeConverter.toInt(monthOnlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(monthOnlineIndicator.getReturnSettleNumSglSubj()));
                    //中学业务的“本月注册”和“昨日注册”指标由字段取值由“regStuCount”变更为：“regStuCount ”   +    “ promoteRegStuCount ”
                    Integer schoolLevel = school.getLevel();
                    if (schoolLevel == 1) {
                        schoolInfo.setTmIncRegStuCount(SafeConverter.toInt(monthOnlineIndicator.getRegStuCount()));
                    } else if (schoolLevel == 2 || schoolLevel == 4) {
                        int regStuCount = SafeConverter.toInt(monthOnlineIndicator.getRegStuCount());
                        int promoteRegStuCount = SafeConverter.toInt(monthOnlineIndicator.getPromoteRegStuCount());
                        schoolInfo.setTmIncRegStuCount(regStuCount + promoteRegStuCount);
                    }
                    schoolInfo.setLoginStuCount(SafeConverter.toInt(monthOnlineIndicator.getLoginStuCount()));
                }
            }

            schoolInfo.setTagList(schoolTagMap.get(SafeConverter.toString(school.getId())));
            resultList.add(schoolInfo);
        });

        // 设置负责该学校的专员姓名
        Set<Long> userIds = resultList.stream().filter(SchoolBasicInfo::getHasBd).map(SchoolBasicInfo::getBdId).filter(p -> p != null).collect(Collectors.toSet());
        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);
        resultList.forEach(p -> {
            if (p.getHasBd() && p.getBdId() != null && userMap.get(p.getBdId()) != null) {
                p.setBdName(userMap.get(p.getBdId()).getRealName());
            }
        });

        return resultList;
    }


    // 获取学校的关键KP
    public Map<String, Object> fetchSchoolKpInfo(Long schoolId, SchoolLevel schoolLevel) {
        Map<String, Object> map = new HashMap<>();

        //查询当前学校下的所有的真老师
        Set<Long> teacherIdSet = teacherLoaderClient.loadSchoolTeacherIds(schoolId);
        Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(teacherIdSet);
        teacherIdSet = teacherIdSet.stream()
                .filter(t -> teacherSummaryMap.get(t) == null || teacherSummaryMap.get(t).getManualFakeTeacher()).collect(Collectors.toSet()); //过滤假老师
        if (CollectionUtils.isEmpty(teacherIdSet)) {
            return map;
        }

        // 加载校园大使
        Map<Long, Teacher> ambassadorMap = ambassadorLoaderClient.getAmbassadorLoader().loadSchoolAmbassadors(schoolId);
        if (MapUtils.isNotEmpty(ambassadorMap)) {
            Map<String, String> infoMap = ambassadorMap.values().stream()
                    .collect(toMap(p -> String.valueOf(p.getId()), t -> t.getProfile() == null ? "" : SafeConverter.toString(t.getProfile().getRealname())));
            map.put("ambassadorMap", infoMap);
        }

        Set<Long> allTeacherIdSet = new HashSet<>();

        Set<Long> sqbaTeacherIdSet = new HashSet<>();
        Set<Long> tsrTeacherIdSet = new HashSet<>();
        Set<Long> gradeManagerIdSet = new HashSet<>();
        Set<Long> classManagerIdSet = new HashSet<>();
        Set<Long> schoolMasterIdSet = new HashSet<>();
        if (schoolLevel == SchoolLevel.MIDDLE || schoolLevel == SchoolLevel.HIGH) { // 初中，高中设置校本题库管理员和快乐学学科组长
            List<TeacherRoles> teacherRoles = teacherRolesServiceClient.getTeacherRolesService().loadSchoolRoleTeachers(schoolId);

            // school quiz bank manager
            sqbaTeacherIdSet.addAll(teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SCHOOL_BANK_MANAGER.name()))
                    .map(TeacherRoles::getUserId)
                    .collect(Collectors.toSet()));

            if (CollectionUtils.isNotEmpty(sqbaTeacherIdSet)) {
                allTeacherIdSet.addAll(sqbaTeacherIdSet);
            }

            // subject leader
            tsrTeacherIdSet.addAll(teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SUBJECT_LEADER.name()))
                    .map(TeacherRoles::getUserId)
                    .collect(Collectors.toSet()));

            if (CollectionUtils.isNotEmpty(tsrTeacherIdSet)) {
                allTeacherIdSet.addAll(tsrTeacherIdSet);
            }

            // grade manager
            gradeManagerIdSet.addAll(teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.GRADE_MANAGER.name()))
                    .map(TeacherRoles::getUserId)
                    .collect(Collectors.toSet()));

            if (CollectionUtils.isNotEmpty(gradeManagerIdSet)) {
                allTeacherIdSet.addAll(gradeManagerIdSet);
            }

            // class manager
            classManagerIdSet.addAll(teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.CLASS_MANAGER.name()))
                    .map(TeacherRoles::getUserId)
                    .collect(Collectors.toSet()));

            if (CollectionUtils.isNotEmpty(classManagerIdSet)) {
                allTeacherIdSet.addAll(classManagerIdSet);
            }

            // school master
            schoolMasterIdSet.addAll(teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SCHOOL_MASTER.name()))
                    .map(TeacherRoles::getUserId)
                    .collect(Collectors.toSet()));

            if (CollectionUtils.isNotEmpty(schoolMasterIdSet)) {
                allTeacherIdSet.addAll(schoolMasterIdSet);
            }

            //教务老师
            List<User> affairTeachers = specialTeacherLoaderClient.findSchoolAffairTeachers(schoolId);
            List<User> schoolAffairTeachers = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(affairTeachers)) {
                affairTeachers.forEach(item -> {
                    String phone = sensitiveUserDataServiceClient.loadUserMobile(item.getId());
                    if (StringUtils.isNoneBlank(phone)) {
                        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(phone, UserType.TEACHER);
                        if (userAuthentication != null) {
                            User user = userLoaderClient.loadUser(userAuthentication.getId());
                            if (null != user && Objects.equals(UserType.TEACHER.getType(), user.getUserType())) {
                                schoolAffairTeachers.add(user);
                            }
                        } else {
                            schoolAffairTeachers.add(item);
                        }
                    }
                });
                map.put("schoolAffairTeachers", schoolAffairTeachers);
            }
        }

        // 获取老师标签
        Map<Long, AgentTargetTag> targetTagMap = agentTargetTagService.loadTeacherTargetTagMap(teacherIdSet);
        // 校长副校长
        List<Long> presidentTeacherIds = targetTagMap.values().stream().filter(p -> p.hasTag(AgentTag.PRESIDENT) || p.hasTag(AgentTag.VICE_PRESIDENT)).map(AgentTargetTag::getTargetId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(presidentTeacherIds)) {
            allTeacherIdSet.addAll(presidentTeacherIds);
        }
        // 教务主任,教学主任
        List<Long> directorTeacherIds = targetTagMap.values().stream().filter(p -> p.hasTag(AgentTag.EDUCATION_DIRECTOR) || p.hasTag(AgentTag.TEACHING_DIRECTOR)).map(AgentTargetTag::getTargetId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(directorTeacherIds)) {
            allTeacherIdSet.addAll(directorTeacherIds);
        }
        // 学科组长,教研组长,年级组长,备课组长
        List<Long> leaderTeacherIds = targetTagMap.values().stream().filter(p -> p.hasTag(AgentTag.SUBJECT_LEADER) || p.hasTag(AgentTag.RESEARCH_LEADER) || p.hasTag(AgentTag.GRADE_LEADER) || p.hasTag(AgentTag.LESSON_PREPARATION_LEADER)).map(AgentTargetTag::getTargetId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(leaderTeacherIds)) {
            allTeacherIdSet.addAll(leaderTeacherIds);
        }

        //按条件封装数据
        if (CollectionUtils.isNotEmpty(allTeacherIdSet)) {
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(allTeacherIdSet);
            if (CollectionUtils.isNotEmpty(sqbaTeacherIdSet)) {
                Set<Long> convertTeacherIdSet = sqbaTeacherIdSet;
                map.put("schoolQuizBankAdministratorTeachers", teacherMap.values().stream().filter(p -> convertTeacherIdSet.contains(p.getId())).collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(tsrTeacherIdSet)) {
                Set<Long> convertTeacherIdSet = tsrTeacherIdSet;
                map.put("schoolKlxSubjectLeader", teacherMap.values().stream().filter(p -> convertTeacherIdSet.contains(p.getId())).collect(Collectors.toList()));
            }
            // 设置校长，副校长
//            if (CollectionUtils.isNotEmpty(presidentTeacherIds)) {
//                map.put("presidentTeachers", teacherMap.values().stream().filter(p -> presidentTeacherIds.contains(p.getId())).collect(Collectors.toList()));
//            }
            // 设置教务主任,教学主任
            if (CollectionUtils.isNotEmpty(directorTeacherIds)) {
                map.put("directorTeachers", teacherMap.values().stream().filter(p -> directorTeacherIds.contains(p.getId())).collect(Collectors.toList()));
            }
            // 设置学科组长,教研组长,年级组长,备课组长
            if (CollectionUtils.isNotEmpty(leaderTeacherIds)) {
                map.put("leaderTeachers", teacherMap.values().stream().filter(p -> leaderTeacherIds.contains(p.getId())).collect(Collectors.toList()));
            }
            //年级主任
            if (CollectionUtils.isNotEmpty(gradeManagerIdSet)) {
                map.put("gradeManagerTeachers", teacherMap.values().stream().filter(p -> gradeManagerIdSet.contains(p.getId())).collect(Collectors.toList()));
            }
            //班主任
            if (CollectionUtils.isNotEmpty(classManagerIdSet)) {
                map.put("classManagerTeachers", teacherMap.values().stream().filter(p -> classManagerIdSet.contains(p.getId())).collect(Collectors.toList()));
            }
            // 设置校长，副校长
            if (CollectionUtils.isNotEmpty(schoolMasterIdSet)) {
                map.put("presidentTeachers", teacherMap.values().stream().filter(p -> schoolMasterIdSet.contains(p.getId())).collect(Collectors.toList()));
            }
        }

        return map;
    }


    public List<SingleSubjectAnsh> getSingleSubjectAnshBySchoolId(Long schoolId) {
        List<SingleSubjectAnsh> list = new ArrayList<>();
        CrmSchoolSummary schoolSummary = crmSummaryLoaderClient.loadSchoolSummary(schoolId);
        if (null != schoolSummary) {
            list.add(generateSingleSubjectAnsh("数学"
                    , schoolSummary.getFinMathAnshEq1StuCount()
                    , schoolSummary.getFinMathAnshGte2StuCount()
                    , schoolSummary.getFinMathAnshGte2IncStuCount()
                    , schoolSummary.getFinMathAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("英语"
                    , schoolSummary.getFinEngAnshEq1StuCount()
                    , schoolSummary.getFinEngAnshGte2StuCount()
                    , schoolSummary.getFinEngAnshGte2IncStuCount()
                    , schoolSummary.getFinEngAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("物理"
                    , schoolSummary.getFinPhyAnshEq1StuCount()
                    , schoolSummary.getFinPhyAnshGte2StuCount()
                    , schoolSummary.getFinPhyAnshGte2IncStuCount()
                    , schoolSummary.getFinPhyAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("化学"
                    , schoolSummary.getFinCheAnshEq1StuCount()
                    , schoolSummary.getFinCheAnshGte2StuCount()
                    , schoolSummary.getFinCheAnshGte2IncStuCount()
                    , schoolSummary.getFinCheAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("生物"
                    , schoolSummary.getFinBiolAnshEq1StuCount()
                    , schoolSummary.getFinBiolAnshGte2StuCount()
                    , schoolSummary.getFinBiolAnshGte2IncStuCount()
                    , schoolSummary.getFinBiolAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("语文"
                    , schoolSummary.getFinChnAnshEq1StuCount()
                    , schoolSummary.getFinChnAnshGte2StuCount()
                    , schoolSummary.getFinChnAnshGte2IncStuCount()
                    , schoolSummary.getFinChnAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("历史"
                    , schoolSummary.getFinHistAnshEq1StuCount()
                    , schoolSummary.getFinHistAnshGte2StuCount()
                    , schoolSummary.getFinHistAnshGte2IncStuCount()
                    , schoolSummary.getFinHistAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("地理"
                    , schoolSummary.getFinGeogAnshEq1StuCount()
                    , schoolSummary.getFinGeogAnshGte2StuCount()
                    , schoolSummary.getFinGeogAnshGte2IncStuCount()
                    , schoolSummary.getFinGeogAnshGte2BfStuCount()));
            list.add(generateSingleSubjectAnsh("政治"
                    , schoolSummary.getFinPolAnshEq1StuCount()
                    , schoolSummary.getFinPolAnshGte2StuCount()
                    , schoolSummary.getFinPolAnshGte2IncStuCount()
                    , schoolSummary.getFinPolAnshGte2BfStuCount()));

        }
        return list;
    }


    /**
     * 获取学校动态
     *
     * @param schoolId
     * @return
     */
    public MapMessage getSchoolDynamics(Long schoolId) {
        MapMessage mapMessage = MapMessage.successMessage();
        //进校记录
        WorkRecordData record = workRecordService.getFirstSchoolWorkRecord(schoolId);
        mapMessage.add("schoolRecord", toSchoolRecordInfo(record));
        //竞品信息
        AgentCompetitiveProduct competitiveProduct = agentCompetitiveProductService.loadBySchoolId(schoolId).stream().findFirst().orElse(null);
        mapMessage.put("competitiveProduct", toCompetitiveProduct(competitiveProduct));
        //备忘录
        AgentMemorandum textAgentMemorandum = agentMemorandumService.loadFirstMemorandumBySchoolId(schoolId, MemorandumType.TEXT);
        mapMessage.add("textAgentMemorandum", toSchoolMemorandum(textAgentMemorandum));
        //图片库
        AgentMemorandum pictureAgentMemorandum = agentMemorandumService.loadFirstMemorandumBySchoolId(schoolId, MemorandumType.PICTURE);
        mapMessage.add("pictureAgentMemorandum", toSchoolMemorandum(pictureAgentMemorandum));
        //学生奖品
        Map<String, Object> rewardLogistics = getRewardLogisticsList(schoolId).stream().findFirst().orElse(null);
        mapMessage.add("rewardLogistics", rewardLogistics);
        return mapMessage;
    }

    /**
     * 生成单科作答数据
     *
     * @param name
     * @param anshEq1StuCount
     * @param anshGte2StuCount
     * @param anshGte2IncStuCount
     * @param anshGte2BfStuCount
     * @return
     */
    private SingleSubjectAnsh generateSingleSubjectAnsh(String name, Integer anshEq1StuCount, Integer anshGte2StuCount,
                                                        Integer anshGte2IncStuCount, Integer anshGte2BfStuCount) {
        SingleSubjectAnsh temp = new SingleSubjectAnsh();
        temp.setSubjectName(name);
        temp.setAnshEq1StuCount(anshEq1StuCount);
        temp.setAnshGte2StuCount(anshGte2StuCount);
        temp.setAnshGte2IncStuCount(anshGte2IncStuCount);
        temp.setAnshGte2BfStuCount(anshGte2BfStuCount);
        return temp;
    }


    private Map<String, String> toSchoolMemorandum(AgentMemorandum p) {
        if (null == p) {
            return null;
        }
        Map<String, String> info = new HashMap<>();
//        info.put("id", p.getId());
//        if (MemorandumType.TEXT.equals(p.getType())) {
//            AgentUser agentUser = agentUserLoaderClient.load(p.getCreateUserId());
//            if (null != agentUser) {
//                info.put("userName", agentUser.getRealName());
//            }
//        }
        info.put("content", p.getContent());
        info.put("time", DateUtils.dateToString(p.getWriteTime(), "yyyy/MM/dd"));
//        info.put("isIntoSchool", p.getIntoSchoolRecordId());
        return info;
    }

    private Map<String, String> toSchoolRecordInfo(WorkRecordData workRecordData) {
        if (workRecordData == null) {
            return null;
        }
        Map<String, String> temp = new HashMap<>();
        temp.put("writeTime", DateUtils.dateToString(workRecordData.getWorkTime(), "yyyy-MM-dd"));
        temp.put("workerName", workRecordData.getUserName());
        List<WorkRecordVisitUserInfo> list = workRecordData.getVisitUserInfoList();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> nameList = list.stream().map(WorkRecordVisitUserInfo::getName).collect(toList());
            temp.put("visitTeacherList", StringUtils.join(nameList, "、"));
        }
        temp.put("workTitle", workRecordData.getWorkTitle());
        return temp;
    }

    private Map<String, Object> toCompetitiveProduct(AgentCompetitiveProduct competitiveProduct) {
        if (competitiveProduct == null) {
            return null;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", competitiveProduct.getName());
        StringBuilder intoTimeStrBuilder = new StringBuilder();
        String intoTimeStr = competitiveProduct.getIntoTime() == null ? "" : competitiveProduct.getIntoTime().toString();
        if (!"".equals(intoTimeStr)) {
            intoTimeStrBuilder.append(intoTimeStr.substring(0, 4));
            intoTimeStrBuilder.append("/");
            intoTimeStrBuilder.append(intoTimeStr.substring(4, 6));
        }
        dataMap.put("intoTime", intoTimeStrBuilder);
        dataMap.put("stuNum", competitiveProduct.getStudentNum());
        dataMap.put("usageScenario", StringUtils.join(competitiveProduct.getUsageScenario().stream().map(UsageScenarioType::getValue).collect(Collectors.toList()), "、"));
        dataMap.put("paymentMode", StringUtils.join(competitiveProduct.getPaymentMode().stream().map(PaymentModeType::getValue).collect(Collectors.toList()), "、"));
        dataMap.put("remark", competitiveProduct.getRemark());
        return dataMap;
    }

    /**
     * 根据学校ID查询学校奖品情况
     *
     * @param schoolId
     * @return
     */
    public List<Map<String, Object>> getRewardLogisticsList(Long schoolId) {
        List<RewardLogistics> rlList = rewardLoaderClient.loadSchoolRewardLogistics(schoolId, RewardLogistics.Type.STUDENT);
        if (CollectionUtils.isNotEmpty(rlList)) {//过滤数据为空的现象
            //过滤 四月份之后的 倒序排序 发货日期转换
            rlList = rlList.stream().filter(p -> {
                int month = (StringUtils.isBlank(p.getMonth())) ? 0 : (Integer.valueOf(p.getMonth()));
                boolean disabled = (p.getDisabled() == null) ? false : p.getDisabled();//需要注意下测试库可能存在为空的情况，做个兼容处理吧
                return month >= 201704 && !disabled;
            }).sorted((p1, p2) ->
                    Integer.compare(Integer.valueOf(StringUtils.defaultString(p2.getMonth(), "0")), Integer.valueOf(StringUtils.defaultString(p1.getMonth(), "0")))
            ).collect(toList());
            rlList.forEach(p -> {
                String monthStr = p.getMonth();
                if (StringUtils.isNotBlank(monthStr)) {
                    p.setMonth(monthStr.substring(0, 4) + "-" + monthStr.substring(4));
                }
            });
        }
        return fillRewardLogistics(rlList);
    }

    /**
     * 填充其他信息（奖励领取状态）
     *
     * @param rewardLogisticsList
     * @return
     */
    private List<Map<String, Object>> fillRewardLogistics(List<RewardLogistics> rewardLogisticsList) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(rewardLogisticsList)) {
            rewardLogisticsList.forEach(logistics -> {
                Map<String, Object> objectMap = BeanMapUtils.tansBean2Map(logistics);
                int month = (StringUtils.isBlank(logistics.getMonth())) ? 0 : (Integer.valueOf(logistics.getMonth().replace("-", "")));
                if (null != logistics.getReceiverId() && month > 201710) {
                    Date now = new Date();
                    Date expireDate = DateUtils.addDays(logistics.getCreateDatetime(), 30);
                    boolean expired = expireDate.before(now);
                    DateRange range = new DateRange(logistics.getCreateDatetime(), expireDate);
                    boolean received = teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                            .findTeacherTaskRewardHistories(logistics.getReceiverId(), TeacherTaskType.REWARD_COLLECTION.name())
                            .getUninterruptibly()
                            .stream()
                            .anyMatch(h -> range.contains(h.getCreateDatetime()));
                    //奖励领取状态，1：已领取，0：未领取，-1：已过期
                    if (received) {
                        objectMap.put("rewardStatus", 1);
                    } else if (expired) {
                        objectMap.put("rewardStatus", -1);
                    } else {
                        objectMap.put("rewardStatus", 0);
                    }
                }
                result.add(objectMap);
            });
        }
        return result;
    }

    /**
     * 获取学校online指标数据
     *
     * @param school
     * @return
     */
    public SchoolOnlineIndicatorData getSchoolOnlineIndicator(School school) {
        SchoolOnlineIndicatorData schoolOnlineIndicatorData = new SchoolOnlineIndicatorData();
        //“学校规模”从学校扩展信息取值
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(school.getId()).getUninterruptibly();
        if (null != schoolExtInfo) {
            schoolOnlineIndicatorData.setStuScale(SafeConverter.toInt(schoolExtInfo.getSchoolSize()));
        } else {
            schoolOnlineIndicatorData.setStuScale(0);
        }
        Integer day = performanceService.lastSuccessDataDay();
        //获取学校所有纬度的指标数据
        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(Collections.singleton(school.getId()), day);
        SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(school.getId());
        if (null != schoolOnlineIndicator) {
            //累计数据
            OnlineIndicator sumOnlineIndicator = schoolOnlineIndicator.fetchSumData();
            if (null != sumOnlineIndicator) {
                schoolOnlineIndicatorData.setRegStuCount(sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getRegStuCount() : 0);
                schoolOnlineIndicatorData.setAuStuCount(sumOnlineIndicator.getAuStuCount() != null ? sumOnlineIndicator.getAuStuCount() : 0);

            }
            //本月数据
            OnlineIndicator monthOnlineIndicator = schoolOnlineIndicator.fetchMonthData();
            if (null != monthOnlineIndicator) {
                //英语渗透类型
                schoolOnlineIndicatorData.setEngPermeabilityType(getSchoolSubjectPermeabilityType(monthOnlineIndicator.getMaxPenetrateRateEng() != null ? monthOnlineIndicator.getMaxPenetrateRateEng() : 0));
                //数学渗透类型
                schoolOnlineIndicatorData.setMathPermeabilityType(getSchoolSubjectPermeabilityType(monthOnlineIndicator.getMaxPenetrateRateMath() != null ? monthOnlineIndicator.getMaxPenetrateRateMath() : 0));
                //语文渗透类型
                schoolOnlineIndicatorData.setChnPermeabilityType(getSchoolSubjectPermeabilityType(monthOnlineIndicator.getMaxPenetrateRateChn() != null ? monthOnlineIndicator.getMaxPenetrateRateChn() : 0));
                //单科渗透类型
                schoolOnlineIndicatorData.setSglSubjPermeabilityType(getSchoolSubjectPermeabilityType(monthOnlineIndicator.getMaxPenetrateRateSglSubj() != null ? monthOnlineIndicator.getMaxPenetrateRateSglSubj() : 0));

                //中学业务的“本月注册”和“昨日注册”指标由字段取值由“regStuCount”变更为：“regStuCount ”   +    “ promoteRegStuCount ”
                Integer schoolLevel = school.getLevel();
                if (schoolLevel == 1) {
                    schoolOnlineIndicatorData.setCurrentMonthIncRegStuCount(monthOnlineIndicator.getRegStuCount() != null ? monthOnlineIndicator.getRegStuCount() : 0);
                } else if (schoolLevel == 2 || schoolLevel == 4) {
                    int regStuCount = monthOnlineIndicator.getRegStuCount() != null ? monthOnlineIndicator.getRegStuCount() : 0;
                    int promoteRegStuCount = monthOnlineIndicator.getPromoteRegStuCount() != null ? monthOnlineIndicator.getPromoteRegStuCount() : 0;
                    schoolOnlineIndicatorData.setCurrentMonthIncRegStuCount(regStuCount + promoteRegStuCount);
                }
                schoolOnlineIndicatorData.setCurrentMonthIncAuStuCount(monthOnlineIndicator.getAuStuCount() != null ? monthOnlineIndicator.getAuStuCount() : 0);
            }
            //获取学校各个科目留存率
            Map<String, Double> rtRate = getRtRate(school.getId());
            //获取学校online学科指标数据
            schoolOnlineIndicatorData = getSchoolOnlineSubjectIndicator(schoolOnlineIndicatorData, monthOnlineIndicator, rtRate);
        }
        return schoolOnlineIndicatorData;
    }

    /**
     * 获取学校online学科指标数据
     *
     * @param schoolOnlineIndicatorData
     * @param onlineIndicator
     * @param rtRate
     * @return
     */
    public SchoolOnlineIndicatorData getSchoolOnlineSubjectIndicator(SchoolOnlineIndicatorData schoolOnlineIndicatorData, OnlineIndicator onlineIndicator, Map<String, Double> rtRate) {
        if (null != onlineIndicator) {
            Map<String, SchoolOnlineIndicatorData.SubjectIndicator> subjectIndicatorMap = new LinkedHashMap<>();
            //英语学科指标
            SchoolOnlineIndicatorData.SubjectIndicator englishIndicator = new SchoolOnlineIndicatorData().new SubjectIndicator();
            englishIndicator.setFinHwGte3AuStuCount(SafeConverter.toInt(onlineIndicator.getIncSettlementEngStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumEng()));
            englishIndicator.setLastSixMonthsMaxMauc(SafeConverter.toInt(onlineIndicator.getBaseFinEngHwGte3AuStuCount()));//近7月最高月活
            double englishRtRate1 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                englishRtRate1 = rtRate.get("ENGLISH1");
            }
            englishIndicator.setRtRate1(englishRtRate1);
            double englishRtRate2 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                englishRtRate2 = rtRate.get("ENGLISH2");
            }
            englishIndicator.setRtRate2(englishRtRate2);

            englishIndicator.setFinHwGte3UnSettleStuCount(onlineIndicator.getFinEngHwGte3UnSettleStuCount() != null ? onlineIndicator.getFinEngHwGte3UnSettleStuCount() : 0);
            englishIndicator.setFinHwEq1UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinEngHwEq1UnSettleStuCount()));
            englishIndicator.setFinHwEq2UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinEngHwEq2UnSettleStuCount()));
            englishIndicator.setFinHwGte3SettleStuCount(onlineIndicator.getFinEngHwGte3SettleStuCount() != null ? onlineIndicator.getFinEngHwGte3SettleStuCount() : 0);
            englishIndicator.setFinHwEq1SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinEngHwEq1SettleStuCount()));
            englishIndicator.setFinHwEq2SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinEngHwEq2SettleStuCount()));
            englishIndicator.setSubjectName(Subject.ENGLISH.getValue());

            //数学学科指标
            SchoolOnlineIndicatorData.SubjectIndicator mathIndicator = new SchoolOnlineIndicatorData().new SubjectIndicator();
            mathIndicator.setFinHwGte3AuStuCount(SafeConverter.toInt(onlineIndicator.getIncSettlementMathStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumMath()));
            mathIndicator.setLastSixMonthsMaxMauc(SafeConverter.toInt(onlineIndicator.getBaseFinMathHwGte3AuStuCount()));//近7月最高月活
            double mathRtRate1 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                mathRtRate1 = rtRate.get("MATH1");
            }
            mathIndicator.setRtRate1(mathRtRate1);
            double mathRtRate2 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                mathRtRate2 = rtRate.get("MATH2");
            }
            mathIndicator.setRtRate2(mathRtRate2);

            mathIndicator.setFinHwGte3UnSettleStuCount(onlineIndicator.getFinMathHwGte3UnSettleStuCount() != null ? onlineIndicator.getFinMathHwGte3UnSettleStuCount() : 0);
            mathIndicator.setFinHwEq1UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinMathHwEq1UnSettleStuCount()));
            mathIndicator.setFinHwEq2UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinMathHwEq2UnSettleStuCount()));
            mathIndicator.setFinHwGte3SettleStuCount(onlineIndicator.getFinMathHwGte3SettleStuCount() != null ? onlineIndicator.getFinMathHwGte3SettleStuCount() : 0);
            mathIndicator.setFinHwEq1SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinMathHwEq1SettleStuCount()));
            mathIndicator.setFinHwEq2SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinMathHwEq2SettleStuCount()));
            mathIndicator.setSubjectName(Subject.MATH.getValue());

            //语文学科指标
            SchoolOnlineIndicatorData.SubjectIndicator chineseIndicator = new SchoolOnlineIndicatorData().new SubjectIndicator();
            chineseIndicator.setFinHwGte3AuStuCount(SafeConverter.toInt(onlineIndicator.getIncSettlementChnStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumChn()));
            chineseIndicator.setLastSixMonthsMaxMauc(SafeConverter.toInt(onlineIndicator.getBaseFinChnHwGte3AuStuCount()));//近7月最高月活
            double chineseRtRate1 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                chineseRtRate1 = rtRate.get("CHINESE1");
            }
            chineseIndicator.setRtRate1(chineseRtRate1);
            double chineseRtRate2 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                chineseRtRate2 = rtRate.get("CHINESE2");
            }
            chineseIndicator.setRtRate2(chineseRtRate2);
            chineseIndicator.setFinHwGte3UnSettleStuCount(onlineIndicator.getFinChnHwGte3UnSettleStuCount() != null ? onlineIndicator.getFinChnHwGte3UnSettleStuCount() : 0);
            chineseIndicator.setFinHwEq1UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinChnHwEq1UnSettleStuCount()));
            chineseIndicator.setFinHwEq2UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinChnHwEq2UnSettleStuCount()));
            chineseIndicator.setFinHwGte3SettleStuCount(onlineIndicator.getFinChnHwGte3SettleStuCount() != null ? onlineIndicator.getFinChnHwGte3SettleStuCount() : 0);
            chineseIndicator.setFinHwEq1SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinChnHwEq1SettleStuCount()));
            chineseIndicator.setFinHwEq2SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinChnHwEq2SettleStuCount()));
            chineseIndicator.setSubjectName(Subject.CHINESE.getValue());

            //单科指标
            SchoolOnlineIndicatorData.SubjectIndicator sglSubjIndicator = new SchoolOnlineIndicatorData().new SubjectIndicator();
            sglSubjIndicator.setFinHwGte3AuStuCount(SafeConverter.toInt(onlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumSglSubj()));
            sglSubjIndicator.setLastSixMonthsMaxMauc(SafeConverter.toInt(onlineIndicator.getBaseFinSglSubjHwGte3AuStuCount()));//近7月最高月活
            double sglSubjRtRate1 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                sglSubjRtRate1 = rtRate.get("SglSubj1");
            }
            sglSubjIndicator.setRtRate1(sglSubjRtRate1);
            double sglSubjRtRate2 = 0D;
            if (MapUtils.isNotEmpty(rtRate)) {
                sglSubjRtRate2 = rtRate.get("SglSubj2");
            }
            sglSubjIndicator.setRtRate2(sglSubjRtRate2);
            sglSubjIndicator.setFinHwGte3UnSettleStuCount(onlineIndicator.getFinSglSubjHwGte3UnSettleStuCount() != null ? onlineIndicator.getFinSglSubjHwGte3UnSettleStuCount() : 0);
            sglSubjIndicator.setFinHwEq1UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinSglSubjHwEq1UnSettleStuCount()));
            sglSubjIndicator.setFinHwEq2UnSettleStuCount(SafeConverter.toInt(onlineIndicator.getFinSglSubjHwEq2UnSettleStuCount()));
            sglSubjIndicator.setFinHwGte3SettleStuCount(onlineIndicator.getFinSglSubjHwGte3SettleStuCount() != null ? onlineIndicator.getFinSglSubjHwGte3SettleStuCount() : 0);
            sglSubjIndicator.setFinHwEq1SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinSglSubjHwEq1SettleStuCount()));
            sglSubjIndicator.setFinHwEq2SettleStuCount(SafeConverter.toInt(onlineIndicator.getFinSglSubjHwEq2SettleStuCount()));
            sglSubjIndicator.setSubjectName("单科");

            subjectIndicatorMap.put(Subject.ENGLISH.name(), englishIndicator);
            subjectIndicatorMap.put(Subject.MATH.name(), mathIndicator);
            subjectIndicatorMap.put(Subject.CHINESE.name(), chineseIndicator);
            subjectIndicatorMap.put("SglSubj", sglSubjIndicator);

            schoolOnlineIndicatorData.setSubjectIndicatorMap(subjectIndicatorMap);
        }
        return schoolOnlineIndicatorData;
    }

    /**
     * 获取学校各个科目留存率
     * 留存数：	当月分科目回流学生3套月活
     * 基数：8/9月份取5月分科目认证3套月活，10月及以后取学期维度“分科目认证3套月活”
     * 留存率：留存数/基数
     *
     * @param schoolId
     * @return
     */
    private Map<String, Double> getRtRate(Long schoolId) {
        //留存数：当月分科目回流学生3套月活
        Integer day = performanceService.lastSuccessDataDay();

        int finChnHwGte1SettleStuCount = 0;//语文1套留存数
        int finMathHwGte1SettleStuCount = 0;//数学1套留存数
        int finEngHwGte1SettleStuCount = 0;//英语1套留存数
        int finSglSubjHwGte1SettleStuCount = 0;//单科1套留存数

        int finChnHwGte1AuStuCount = 0;//语文1套留存基数
        int finMathHwGte1AuStuCount = 0;//数学1套留存基数
        int finEngHwGte1AuStuCount = 0;//英语1套留存基数
        int finSglSubjHwGte1AuStuCount = 0;//单科1套留存基数

        int returnSettleNumChn = 0;//语文3套留存数
        int returnSettleNumMath = 0;//数学3套留存数
        int returnSettleNumEng = 0;//英语3套留存数
        int returnSettleNumSglSubj = 0;//单科3套留存数

        int baseFinChnHwGte3AuStuCount = 0;//语文3套留存基数
        int baseFinMathHwGte3AuStuCount = 0;//数学3套留存基数
        int baseFinEngHwGte3AuStuCount = 0;//英语3套留存基数
        int baseFinSglSubjHwGte3AuStuCount = 0;//单科3套留存基数

        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(Collections.singleton(schoolId), day);
        SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(schoolId);
        if (schoolOnlineIndicator != null) {
            OnlineIndicator onlineIndicator = schoolOnlineIndicator.fetchMonthData();
            if (onlineIndicator != null) {
                finChnHwGte1SettleStuCount = SafeConverter.toInt(onlineIndicator.getFinChnHwGte1SettleStuCount());
                finMathHwGte1SettleStuCount = SafeConverter.toInt(onlineIndicator.getFinMathHwGte1SettleStuCount());
                finEngHwGte1SettleStuCount = SafeConverter.toInt(onlineIndicator.getFinEngHwGte1SettleStuCount());
                finSglSubjHwGte1SettleStuCount = SafeConverter.toInt(onlineIndicator.getFinSglSubjHwGte1SettleStuCount());

                finChnHwGte1AuStuCount = SafeConverter.toInt(onlineIndicator.getFinChnHwGte1AuStuCount());
                finMathHwGte1AuStuCount = SafeConverter.toInt(onlineIndicator.getFinMathHwGte1AuStuCount());
                finEngHwGte1AuStuCount = SafeConverter.toInt(onlineIndicator.getFinEngHwGte1AuStuCount());
                finSglSubjHwGte1AuStuCount = SafeConverter.toInt(onlineIndicator.getFinSglSubjHwGte1AuStuCount());


                returnSettleNumChn = SafeConverter.toInt(onlineIndicator.getReturnSettleNumChn());
                returnSettleNumMath = SafeConverter.toInt(onlineIndicator.getReturnSettleNumMath());
                returnSettleNumEng = SafeConverter.toInt(onlineIndicator.getReturnSettleNumEng());
                returnSettleNumSglSubj = SafeConverter.toInt(onlineIndicator.getReturnSettleNumSglSubj());

                baseFinChnHwGte3AuStuCount = SafeConverter.toInt(onlineIndicator.getBaseFinChnHwGte3AuStuCount());
                baseFinMathHwGte3AuStuCount = SafeConverter.toInt(onlineIndicator.getBaseFinMathHwGte3AuStuCount());
                baseFinEngHwGte3AuStuCount = SafeConverter.toInt(onlineIndicator.getBaseFinEngHwGte3AuStuCount());
                baseFinSglSubjHwGte3AuStuCount = SafeConverter.toInt(onlineIndicator.getBaseFinSglSubjHwGte3AuStuCount());
            }
        }

        Map<String, Double> dataMap = new HashMap<>();
        dataMap.put("CHINESE1", MathUtils.doubleDivide(finChnHwGte1SettleStuCount, finChnHwGte1AuStuCount));   //语文留存率
        dataMap.put("MATH1", MathUtils.doubleDivide(finMathHwGte1SettleStuCount, finMathHwGte1AuStuCount));    //数学留存率
        dataMap.put("ENGLISH1", MathUtils.doubleDivide(finEngHwGte1SettleStuCount, finEngHwGte1AuStuCount));   //英语留存率
        dataMap.put("SglSubj1", MathUtils.doubleDivide(finSglSubjHwGte1SettleStuCount, finSglSubjHwGte1AuStuCount));   //单科1套留存率

        dataMap.put("CHINESE2", MathUtils.doubleDivide(returnSettleNumChn, baseFinChnHwGte3AuStuCount));   //语文留存率
        dataMap.put("MATH2", MathUtils.doubleDivide(returnSettleNumMath, baseFinMathHwGte3AuStuCount));    //数学留存率
        dataMap.put("ENGLISH2", MathUtils.doubleDivide(returnSettleNumEng, baseFinEngHwGte3AuStuCount));   //英语留存率
        dataMap.put("SglSubj2", MathUtils.doubleDivide(returnSettleNumSglSubj, baseFinSglSubjHwGte3AuStuCount));   //单科2套留存率
        return dataMap;
    }

    /**
     * 将渗透率转化为渗透类型
     *
     * @param penetrateRate
     * @return
     */
    public AgentSchoolPermeabilityType getSchoolSubjectPermeabilityType(Double penetrateRate) {
        if (null == penetrateRate) {
            penetrateRate = 0D;
        }
        penetrateRate = MathUtils.doubleDivide(penetrateRate, 100);
        //低渗
        if (penetrateRate <= 0.2) {
            return AgentSchoolPermeabilityType.LOW;
        }
        //中渗
        if (penetrateRate > 0.2 && penetrateRate <= 0.6) {
            return AgentSchoolPermeabilityType.MIDDLE;
        }
        //高渗
        if (penetrateRate > 0.6 && penetrateRate <= 0.8) {
            return AgentSchoolPermeabilityType.HIGH;
        }
        if (penetrateRate > 0.8) {
            return AgentSchoolPermeabilityType.SUPER_HIGH;
        }
        return null;
    }


    private Map<Subject, School17PerformanceVO.SubjectPerformance> getSubjectPerformanceMap(AgentSchool17PerformanceData currentMonthData, AgentSchool17PerformanceData previousMonthData) {
        Map<Subject, School17PerformanceVO.SubjectPerformance> resultMap = new LinkedHashMap<>();
        resultMap.put(Subject.ENGLISH, getEnglishSubjectPerformance(currentMonthData, previousMonthData));
        resultMap.put(Subject.MATH, getMathSubjectPerformance(currentMonthData, previousMonthData));
        resultMap.put(Subject.CHINESE, getChineseSubjectPerformance(currentMonthData, previousMonthData));
        return resultMap;
    }

    private School17PerformanceVO.SubjectPerformance getEnglishSubjectPerformance(AgentSchool17PerformanceData currentData, AgentSchool17PerformanceData previousData) {
        School17PerformanceVO.SubjectPerformance subjectPerformance = new School17PerformanceVO().new SubjectPerformance();
        AgentSchool17PerformanceIndicator currentMonthData = currentData.getIndicatorData();
        int currentFinSubjectHwEq1StuCount = currentMonthData.getFinEngHwEq1StuCount();
        int currentFinSubjectHwEq2StuCount = currentMonthData.getFinEngHwEq2StuCount();
        int currentFinSubjectHwGte3StuCount = currentMonthData.getFinEngHwGte3StuCount();
        int currentFinSubjectHwGte3AuStuCount = currentMonthData.getFinEngHwGte3AuStuCount();
        int currentFinSubjectHwGte3IncAuStuCount = currentMonthData.getFinEngHwGte3IncAuStuCount();
        double currentMrtRate = MathUtils.doubleDivide(currentMonthData.getEngMrtStuCount(), currentMonthData.getLmFinEngHwGte3AuStuCount());

        int currentFinSubjectHwEq1AuStuCount = currentMonthData.getFinEngHwEq1AuStuCount();
        int currentFinSubjectHwEq2AuStuCount = currentMonthData.getFinEngHwEq2AuStuCount();

        AgentSchool17PerformanceIndicator previousMonthData = previousData.getIndicatorData();
        double previousMrtRate = MathUtils.doubleDivide(previousMonthData.getEngMrtStuCount(), previousMonthData.getLmFinEngHwGte3AuStuCount());

        int previousFinSubjectHwEq1AuStuCount = previousMonthData.getFinEngHwEq1AuStuCount();
        int previousFinSubjectHwEq2AuStuCount = previousMonthData.getFinEngHwEq2AuStuCount();
        int previousFinSubjectHwGte3AuStuCount = previousMonthData.getFinEngHwGte3AuStuCount();


        int finHwEq0Count = currentMonthData.getRegStuCount() - currentFinSubjectHwEq1StuCount - currentFinSubjectHwEq2StuCount - currentFinSubjectHwGte3StuCount;

        int finEngHwGte3AuStuCountDf = currentMonthData.getFinEngHwGte3AuStuCountDf();
        subjectPerformance.setSubjectName(Subject.ENGLISH.getValue());
        subjectPerformance.setFinHwEq1StuCount(currentFinSubjectHwEq1StuCount);
        subjectPerformance.setFinHwEq2StuCount(currentFinSubjectHwEq2StuCount);
        subjectPerformance.setFinHwEq0StuCount(finHwEq0Count);
        subjectPerformance.setFinHwGte3AuStuCount(currentFinSubjectHwGte3AuStuCount);
        subjectPerformance.setFinHwGte3IncAuStuCount(currentFinSubjectHwGte3IncAuStuCount);
        subjectPerformance.setPreviousFinHwGte3AuStuCount(previousFinSubjectHwGte3AuStuCount);
        subjectPerformance.setMrtRate(currentMrtRate);
        subjectPerformance.setPreviousMrtRate(previousMrtRate);
        subjectPerformance.setFinHwGte3AuStuCountDf(finEngHwGte3AuStuCountDf);
        int finSubjectHwGte1AuStuCount = currentFinSubjectHwEq1AuStuCount + currentFinSubjectHwEq2AuStuCount + currentFinSubjectHwGte3AuStuCount;
        subjectPerformance.setPermeability(MathUtils.doubleDivide(finSubjectHwGte1AuStuCount, currentMonthData.getStuScale()));
        int previousFinEngHwGte1AuStuCount = previousFinSubjectHwEq1AuStuCount + previousFinSubjectHwEq2AuStuCount + previousFinSubjectHwGte3AuStuCount;
        subjectPerformance.setPreviousPermeability(MathUtils.doubleDivide(previousFinEngHwGte1AuStuCount, previousMonthData.getStuScale()));
        return subjectPerformance;
    }


    private School17PerformanceVO.SubjectPerformance getMathSubjectPerformance(AgentSchool17PerformanceData currentData, AgentSchool17PerformanceData previousData) {
        School17PerformanceVO.SubjectPerformance subjectPerformance = new School17PerformanceVO().new SubjectPerformance();
        AgentSchool17PerformanceIndicator currentMonthData = currentData.getIndicatorData();
        int currentFinSubjectHwEq1StuCount = currentMonthData.getFinMathHwEq1StuCount();
        int currentFinSubjectHwEq2StuCount = currentMonthData.getFinMathHwEq2StuCount();
        int currentFinSubjectHwGte3StuCount = currentMonthData.getFinMathHwGte3StuCount();
        int currentFinSubjectHwGte3AuStuCount = currentMonthData.getFinMathHwGte3AuStuCount();
        int currentFinSubjectHwGte3IncAuStuCount = currentMonthData.getFinMathHwGte3IncAuStuCount();
        double currentMrtRate = MathUtils.doubleDivide(currentMonthData.getMathMrtStuCount(), currentMonthData.getLmFinMathHwGte3AuStuCount());
        int currentFinSubjectHwEq1AuStuCount = currentMonthData.getFinMathHwEq1AuStuCount();
        int currentFinSubjectHwEq2AuStuCount = currentMonthData.getFinMathHwEq2AuStuCount();

        AgentSchool17PerformanceIndicator previousMonthData = previousData.getIndicatorData();
        double previousMrtRate = MathUtils.doubleDivide(previousMonthData.getMathMrtStuCount(), previousMonthData.getLmFinMathHwGte3AuStuCount());

        int previousFinSubjectHwEq1AuStuCount = previousMonthData.getFinMathHwEq1AuStuCount();
        int previousFinSubjectHwEq2AuStuCount = previousMonthData.getFinMathHwEq2AuStuCount();
        int previousFinSubjectHwGte3AuStuCount = previousMonthData.getFinMathHwGte3AuStuCount();


        int finHwEq0Count = currentMonthData.getRegStuCount() - currentFinSubjectHwEq1StuCount - currentFinSubjectHwEq2StuCount - currentFinSubjectHwGte3StuCount;
        subjectPerformance.setSubjectName(Subject.MATH.getValue());
        subjectPerformance.setFinHwEq1StuCount(currentFinSubjectHwEq1StuCount);
        subjectPerformance.setFinHwEq2StuCount(currentFinSubjectHwEq2StuCount);
        subjectPerformance.setFinHwEq0StuCount(finHwEq0Count);
        subjectPerformance.setFinHwGte3AuStuCount(currentFinSubjectHwGte3AuStuCount);
        subjectPerformance.setFinHwGte3IncAuStuCount(currentFinSubjectHwGte3IncAuStuCount);
        subjectPerformance.setPreviousFinHwGte3AuStuCount(previousFinSubjectHwGte3AuStuCount);
        subjectPerformance.setMrtRate(currentMrtRate);
        subjectPerformance.setPreviousMrtRate(previousMrtRate);
        int finSubjectHwGte1AuStuCount = currentFinSubjectHwEq1AuStuCount + currentFinSubjectHwEq2AuStuCount + currentFinSubjectHwGte3AuStuCount;
        subjectPerformance.setPermeability(MathUtils.doubleDivide(finSubjectHwGte1AuStuCount, currentMonthData.getStuScale()));
        int previousFinEngHwGte1AuStuCount = previousFinSubjectHwEq1AuStuCount + previousFinSubjectHwEq2AuStuCount + previousFinSubjectHwGte3AuStuCount;
        subjectPerformance.setPreviousPermeability(MathUtils.doubleDivide(previousFinEngHwGte1AuStuCount, previousMonthData.getStuScale()));

        int finMathHwGte3IncAuStuCountDf = currentMonthData.getFinMathHwGte3IncAuStuCountDf();
        subjectPerformance.setFinHwGte3AuStuCountDf(finMathHwGte3IncAuStuCountDf);
        return subjectPerformance;
    }


    private School17PerformanceVO.SubjectPerformance getChineseSubjectPerformance(AgentSchool17PerformanceData currentData, AgentSchool17PerformanceData previousData) {
        School17PerformanceVO.SubjectPerformance subjectPerformance = new School17PerformanceVO().new SubjectPerformance();
        AgentSchool17PerformanceIndicator currentMonthData = currentData.getIndicatorData();
        int currentFinSubjectHwEq1StuCount = currentMonthData.getFinChnHwEq1StuCount();
        int currentFinSubjectHwEq2StuCount = currentMonthData.getFinChnHwEq2StuCount();
        int currentFinSubjectHwGte3StuCount = currentMonthData.getFinChnHwGte3StuCount();
        int currentFinSubjectHwGte3AuStuCount = currentMonthData.getFinChnHwGte3AuStuCount();
        int currentFinSubjectHwGte3IncAuStuCount = currentMonthData.getFinChnHwGte3IncAuStuCount();
        double currentMrtRate = MathUtils.doubleDivide(currentMonthData.getChnMrtStuCount(), currentMonthData.getLmFinChnHwGte3AuStuCount());

        int currentFinSubjectHwEq1AuStuCount = currentMonthData.getFinChnHwEq1AuStuCount();
        int currentFinSubjectHwEq2AuStuCount = currentMonthData.getFinChnHwEq2AuStuCount();

        AgentSchool17PerformanceIndicator previousMonthData = previousData.getIndicatorData();
        double previousMrtRate = MathUtils.doubleDivide(previousMonthData.getChnMrtStuCount(), previousMonthData.getLmFinChnHwGte3AuStuCount());
        ;

        int previousFinSubjectHwEq1AuStuCount = previousMonthData.getFinChnHwEq1AuStuCount();
        int previousFinSubjectHwEq2AuStuCount = previousMonthData.getFinChnHwEq2AuStuCount();
        int previousFinSubjectHwGte3AuStuCount = previousMonthData.getFinChnHwGte3AuStuCount();

        int finHwEq0Count = currentMonthData.getRegStuCount() - currentFinSubjectHwEq1StuCount - currentFinSubjectHwEq2StuCount - currentFinSubjectHwGte3StuCount;
        subjectPerformance.setSubjectName(Subject.CHINESE.getValue());
        subjectPerformance.setFinHwEq1StuCount(currentFinSubjectHwEq1StuCount);
        subjectPerformance.setFinHwEq2StuCount(currentFinSubjectHwEq2StuCount);
        subjectPerformance.setFinHwEq0StuCount(finHwEq0Count);
        subjectPerformance.setFinHwGte3AuStuCount(currentFinSubjectHwGte3AuStuCount);
        subjectPerformance.setFinHwGte3IncAuStuCount(currentFinSubjectHwGte3IncAuStuCount);
        subjectPerformance.setPreviousFinHwGte3AuStuCount(previousFinSubjectHwGte3AuStuCount);
        subjectPerformance.setMrtRate(currentMrtRate);
        subjectPerformance.setPreviousMrtRate(previousMrtRate);
        int finSubjectHwGte1AuStuCount = currentFinSubjectHwEq1AuStuCount + currentFinSubjectHwEq2AuStuCount + currentFinSubjectHwGte3AuStuCount;
        subjectPerformance.setPermeability(MathUtils.doubleDivide(finSubjectHwGte1AuStuCount, currentMonthData.getStuScale()));
        int previousFinEngHwGte1AuStuCount = previousFinSubjectHwEq1AuStuCount + previousFinSubjectHwEq2AuStuCount + previousFinSubjectHwGte3AuStuCount;
        subjectPerformance.setPreviousPermeability(MathUtils.doubleDivide(previousFinEngHwGte1AuStuCount, previousMonthData.getStuScale()));
        int finChnHwGte3IncAuStuCountDf = currentMonthData.getFinChnHwGte3IncAuStuCountDf();
        subjectPerformance.setFinHwGte3AuStuCountDf(finChnHwGte3IncAuStuCountDf);
        return subjectPerformance;
    }


    public Map<String, Object> getSchoolKlxPerformance(long schoolId) {
        Integer day = performanceService.lastSuccessDataDay();
        Map<String, Object> resultMap = new HashMap<>();

        //“学校规模”从学校扩展信息取值
        int stuScale = 0;
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (null != schoolExtInfo) {
            stuScale = SafeConverter.toInt(schoolExtInfo.getSchoolSize());
        }

        //学校offline指标
        OfflineIndicator offlineIndicatorMonth;
        OfflineIndicator offlineIndicatorSum;
        Map<Long, SchoolOfflineIndicator> indicatorMap = loadNewSchoolServiceClient.loadSchoolOfflineIndicator(Collections.singleton(schoolId), day);
        SchoolOfflineIndicator schoolOfflineIndicator = indicatorMap.get(schoolId);
        //本月
        if (schoolOfflineIndicator != null && schoolOfflineIndicator.fetchMonthData() != null) {
            offlineIndicatorMonth = schoolOfflineIndicator.fetchMonthData();
        } else {
            offlineIndicatorMonth = new OfflineIndicator();
        }

        //累计
        if (schoolOfflineIndicator != null && schoolOfflineIndicator.fetchSumData() != null) {
            offlineIndicatorSum = schoolOfflineIndicator.fetchSumData();
        } else {
            offlineIndicatorSum = new OfflineIndicator();
        }

        resultMap.put("stuScale", stuScale);
        resultMap.put("klxTnCount", SafeConverter.toInt(offlineIndicatorSum.getKlxTotalNum()));

        // 周测1套
        resultMap.put("tmGte1Num", SafeConverter.toInt(offlineIndicatorMonth.getSettlementNumSglSubj()) + SafeConverter.toInt(offlineIndicatorMonth.getUnsettlementNumSglSubj()));
        resultMap.put("tmSettlementGte1Num", SafeConverter.toInt(offlineIndicatorMonth.getSettlementNumSglSubj()));     //有线上作业
        resultMap.put("tmUnSettlementGte1Num", SafeConverter.toInt(offlineIndicatorMonth.getUnsettlementNumSglSubj())); //无线上作业
        // 周测2套
        resultMap.put("tmGte2Num", SafeConverter.toInt(offlineIndicatorMonth.getSettlementGte2NumSglSubj()) + SafeConverter.toInt(offlineIndicatorMonth.getUnsettlementGte2NumSglSubj()));
        resultMap.put("tmSettlementGte2Num", SafeConverter.toInt(offlineIndicatorMonth.getSettlementGte2NumSglSubj()));     //有线上作业
        resultMap.put("tmUnSettlementGte2Num", SafeConverter.toInt(offlineIndicatorMonth.getUnsettlementGte2NumSglSubj())); //无线上作业
        return resultMap;
    }


    public Map<String, Object> generateSchoolKlxChartInfo(Long schoolId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Integer> dayList = new ArrayList<>();
        Date date = performanceService.lastSuccessDataDate();
        dayList.add(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")));
        for (int i = 0; i < 5; i++) {
            dayList.add(0, SafeConverter.toInt(DateUtils.dateToString(DayUtils.getLastDayOfMonth(DayUtils.addMonth(date, -(i + 1))), "yyyyMMdd")));
        }
        List<String> xAxis = dayList.stream().map(p -> String.valueOf(p / 100)).collect(Collectors.toList());
        resultMap.put("xAxis", xAxis);

        String legendTitle = "周测（≥2套）";
        List<String> legend = new ArrayList<>();
        legend.add(legendTitle);
        resultMap.put("legend", legend);

        List<Integer> scanData = new ArrayList<>();
        for (Integer day : dayList) {
            //学校offline指标
            OfflineIndicator offlineIndicator;
            Map<Long, SchoolOfflineIndicator> indicatorMap = loadNewSchoolServiceClient.loadSchoolOfflineIndicator(Collections.singleton(schoolId), day);
            SchoolOfflineIndicator schoolOfflineIndicator = indicatorMap.get(schoolId);
            //本月
            if (schoolOfflineIndicator != null && schoolOfflineIndicator.fetchMonthData() != null) {
                offlineIndicator = schoolOfflineIndicator.fetchMonthData();
            } else {
                offlineIndicator = new OfflineIndicator();
            }
            //周测2套
            scanData.add(SafeConverter.toInt(offlineIndicator.getSettlementGte2NumSglSubj()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2NumSglSubj()));
        }
        resultMap.put(legendTitle, scanData);
        return resultMap;
    }

    public List<Map<String, Object>> getSchoolKlxSubjectWeekTest(Long schoolId) {
        Integer day = performanceService.lastSuccessDataDay();
        List<Map<String, Object>> dataList = new ArrayList<>();
        //学校offline指标
        OfflineIndicator offlineIndicator;
        Map<Long, SchoolOfflineIndicator> indicatorMap = loadNewSchoolServiceClient.loadSchoolOfflineIndicator(Collections.singleton(schoolId), day);
        SchoolOfflineIndicator schoolOfflineIndicator = indicatorMap.get(schoolId);
        //本月
        if (schoolOfflineIndicator != null && schoolOfflineIndicator.fetchMonthData() != null) {
            offlineIndicator = schoolOfflineIndicator.fetchMonthData();
        } else {
            offlineIndicator = new OfflineIndicator();
        }
        //语文
        dataList.add(generateSubjectWeekTest(Subject.CHINESE, offlineIndicator.getSettlementNumChi(), offlineIndicator.getUnsettlementNumChi(), offlineIndicator.getSettlementGte2NumChi(), offlineIndicator.getUnsettlementGte2NumChi()));
        //数学
        dataList.add(generateSubjectWeekTest(Subject.MATH, offlineIndicator.getSettlementNumMath(), offlineIndicator.getUnsettlementNumMath(), offlineIndicator.getSettlementGte2NumMath(), offlineIndicator.getUnsettlementGte2NumMath()));
        //英语
        dataList.add(generateSubjectWeekTest(Subject.ENGLISH, offlineIndicator.getSettlementNumEng(), offlineIndicator.getUnsettlementNumEng(), offlineIndicator.getSettlementGte2NumEng(), offlineIndicator.getUnsettlementGte2NumEng()));
        //政治
        dataList.add(generateSubjectWeekTest(Subject.POLITICS, offlineIndicator.getSettlementNumPol(), offlineIndicator.getUnsettlementNumPol(), offlineIndicator.getSettlementGte2NumPol(), offlineIndicator.getUnsettlementGte2NumPol()));
        //历史
        dataList.add(generateSubjectWeekTest(Subject.HISTORY, offlineIndicator.getSettlementNumHis(), offlineIndicator.getUnsettlementNumHis(), offlineIndicator.getSettlementGte2NumHis(), offlineIndicator.getUnsettlementGte2NumHis()));
        //地理
        dataList.add(generateSubjectWeekTest(Subject.GEOGRAPHY, offlineIndicator.getSettlementNumGeo(), offlineIndicator.getUnsettlementNumGeo(), offlineIndicator.getSettlementGte2NumGeo(), offlineIndicator.getUnsettlementGte2NumGeo()));
        //物理
        dataList.add(generateSubjectWeekTest(Subject.PHYSICS, offlineIndicator.getSettlementNumPhy(), offlineIndicator.getUnsettlementNumPhy(), offlineIndicator.getSettlementGte2NumPhy(), offlineIndicator.getUnsettlementGte2NumPhy()));
        //化学
        dataList.add(generateSubjectWeekTest(Subject.CHEMISTRY, offlineIndicator.getSettlementNumChe(), offlineIndicator.getUnsettlementNumChe(), offlineIndicator.getSettlementGte2NumChe(), offlineIndicator.getUnsettlementGte2NumChe()));
        //生物
        dataList.add(generateSubjectWeekTest(Subject.BIOLOGY, offlineIndicator.getSettlementNumBio(), offlineIndicator.getUnsettlementNumBio(), offlineIndicator.getSettlementGte2NumBio(), offlineIndicator.getUnsettlementGte2NumBio()));
        //科学
        dataList.add(generateSubjectWeekTest(Subject.SCIENCE, offlineIndicator.getSettlementNumSci(), offlineIndicator.getUnsettlementNumSci(), offlineIndicator.getSettlementGte2NumSci(), offlineIndicator.getUnsettlementGte2NumSci()));
        //历史与社会
        dataList.add(generateSubjectWeekTest(Subject.HISTORY_SOCIETY, offlineIndicator.getSettlementNumHisSoc(), offlineIndicator.getUnsettlementNumHisSoc(), offlineIndicator.getSettlementGte2NumHisSoc(), offlineIndicator.getUnsettlementGte2NumHisSoc()));
        //通用技术
        dataList.add(generateSubjectWeekTest(Subject.GENERIC_TECHNOLOGY, offlineIndicator.getSettlementNumGen(), offlineIndicator.getUnsettlementNumGen(), offlineIndicator.getSettlementGte2NumGen(), offlineIndicator.getUnsettlementGte2NumGen()));

        return dataList;
    }

    public Map<String, Object> generateSubjectWeekTest(Subject subject, Integer tmSettlementGte1Num, Integer tmUnSettlementGte1Num, Integer tmSettlementGte2Num, Integer tmUnSettlementGte2Num) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("subject", subject);
        dataMap.put("subjectName", subject.getValue());
        //周测1套
        dataMap.put("tmGte1Num", SafeConverter.toInt(tmSettlementGte1Num) + SafeConverter.toInt(tmUnSettlementGte1Num));
        dataMap.put("tmSettlementGte1Num", SafeConverter.toInt(tmSettlementGte1Num));     //有线上作业
        dataMap.put("tmUnSettlementGte1Num", SafeConverter.toInt(tmUnSettlementGte1Num)); //无线上作业
        // 周测2套
        dataMap.put("tmGte2Num", SafeConverter.toInt(tmSettlementGte2Num) + SafeConverter.toInt(tmUnSettlementGte2Num));
        dataMap.put("tmSettlementGte2Num", SafeConverter.toInt(tmSettlementGte2Num));     //有线上作业
        dataMap.put("tmUnSettlementGte2Num", SafeConverter.toInt(tmUnSettlementGte2Num)); //无线上作业
        return dataMap;
    }

    /**
     * 根据三种场景判断是否对该学校有操作权限，若无权限判断，获取学校负责人员
     *
     * @param userId
     * @param schoolId
     * @param scene    三种场景（1：责任区域  2：范围区域  3：公私海）
     * @return
     */
    public MapMessage schoolAuthorityMessage(Long userId, Long schoolId, Integer scene) {
        String schoolManager = "";
        //判断是否对该学校有操作权限
        boolean hasSchoolPermission = searchService.hasSchoolPermission(userId, schoolId, scene);
        if (!hasSchoolPermission) {
            //获取负责该学校的专员和代理
            schoolManager = StringUtils.join(baseOrgService.getSchoolManager(schoolId).stream().map(AgentUser::getRealName).collect(toList()), "、");
            return MapMessage.errorMessage().add("schoolManager", schoolManager);
        } else {
            return MapMessage.successMessage();
        }
    }


    public SchoolBasicData generateSchoolBasicData(Long schoolId) {
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return null;
        }
        SchoolBasicData schoolBasicData = new SchoolBasicData();
        schoolBasicData.setSchoolId(schoolId);
        schoolBasicData.setCmainName(school.getCmainName());
        schoolBasicData.setSchoolDistrict(school.getSchoolDistrict());
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        schoolBasicData.setSchoolLevel(schoolLevel.getLevel());
        schoolBasicData.setSchoolLevelDesc(schoolLevel.getDescription());

        if (school.getRegionCode() != null) {
            schoolBasicData.setRegionCode(school.getRegionCode());
            ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
            if (exRegion != null) {
                schoolBasicData.setRegionName(exRegion.getCountyName());
            }
        }
        return schoolBasicData;
    }

    public SchoolBasicExtData generateSchoolBasicExtData(Long schoolId) {
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return null;
        }
        SchoolBasicExtData extData = new SchoolBasicExtData();
        String eduSystemTypeStr = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
        EduSystemType eduSystemType = EduSystemType.of(eduSystemTypeStr);
        if (eduSystemType != null) {
            extData.setEduSystem(eduSystemType);
            extData.setEduSystemDesc(eduSystemType.getDescription());
        }

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolExtInfo != null) {
            extData.setEnglishStartGrade(schoolExtInfo.getEnglishStartGrade());
            extData.setSchoolSize(SafeConverter.toInt(schoolExtInfo.getSchoolSize()));
            extData.setExternOrBoarder(schoolExtInfo.getExternOrBoarder());//走读方式
        }

        //获取学校最新提交审核信息
        Map<Long, CrmSchoolClue> schoolClueMap = schoolClueService.findSchoolIdIs(Collections.singleton(schoolId));
        CrmSchoolClue crmSchoolClue = schoolClueMap.get(school.getId());
        //待审核
        if (crmSchoolClue != null && crmSchoolClue.getStatus() != null && Objects.equals(crmSchoolClue.getStatus(), 1)) {
            extData.setAuditStatus(1);
        }

        return extData;
    }


    private List<ClazzLevel> fetchClazzLevelList(EduSystemType eduSystemType) {
        List<ClazzLevel> result = new ArrayList<>();
        if (eduSystemType == null) {
            return result;
        }
        String[] grades = eduSystemType.getCandidateClazzLevel().split(",");
        for (int i = 0; i < grades.length; i++) {
            int grade = SafeConverter.toInt(grades[i]);
            ClazzLevel clazzLevel = ClazzLevel.parse(grade);
            if (null != clazzLevel) {
                result.add(clazzLevel);
            }
        }
        return result;
    }

    public boolean checkGradeBasicDataIsComplete(Long schoolId) {
        List<SchoolGradeBasicData> gradeDataList = generateGradeBasicDataList(schoolId);
        if (CollectionUtils.isEmpty(gradeDataList)) {
            return false;
        }
        // p.getClazzNum()，  p.getStudentNum() 允许等 0
        return !gradeDataList.stream().anyMatch(p -> p.getClazzNum() == null || p.getClazzNum() < 0 || p.getStudentNum() == null || p.getStudentNum() < 0);
    }

    public List<SchoolGradeBasicData> generateGradeBasicDataList(Long schoolId) {

        List<SchoolGradeBasicData> resultList = new ArrayList<>();

        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return resultList;
        }
        String eduSystemTypeStr = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
        EduSystemType eduSystemType = EduSystemType.of(eduSystemTypeStr);

        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();

        List<ClazzLevel> clazzLevelList = fetchClazzLevelList(eduSystemType);
        for (ClazzLevel clazzLevel : clazzLevelList) {
            SchoolGradeBasicData item = new SchoolGradeBasicData();
            item.setGrade(clazzLevel.getLevel());
            item.setGradeDesc(clazzLevel.getDescription());
            if (extInfo != null) {
                item.setClazzNum(extInfo.fetchGradeClazzNum(clazzLevel, eduSystemType));
                item.setStudentNum(extInfo.fetchGradeStudentNum(clazzLevel, eduSystemType));
            }
            resultList.add(item);
        }
        return resultList;
    }

    public SchoolPositionData generateSchoolPositionData(Long schoolId) {
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return null;
        }

        SchoolPositionData data = new SchoolPositionData();
        data.setSchoolId(schoolId);
        data.setSchoolName(school.getCname());
        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (extInfo != null) {
            data.setCoordinateType(extInfo.getCoordinateType());
            data.setLatitude(extInfo.getLatitude());
            data.setLongitude(extInfo.getLongitude());
            data.setAddress(extInfo.getAddress());
            data.setPhotoUrl(extInfo.getPhotoUrl());
        }
        return data;
    }


    public MapMessage updateSchoolExtData(SchoolBasicExtData basicExtData) {
        if (basicExtData == null || basicExtData.getSchoolId() == null) {
            return MapMessage.errorMessage("学制，英语起始年级更新失败");
        }
        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(basicExtData.getSchoolId()).getUninterruptibly();
        if (extInfo == null) {
            extInfo = new SchoolExtInfo();
            extInfo.setId(basicExtData.getSchoolId());
        }
        if (basicExtData.getEduSystem() != null) {
            extInfo.setEduSystem(basicExtData.getEduSystem().name());
        }
        if (basicExtData.getEnglishStartGrade() != null) {
            extInfo.setEnglishStartGrade(basicExtData.getEnglishStartGrade());
        }
        if (basicExtData.getExternOrBoarder() != null) {
            extInfo.setExternOrBoarder(basicExtData.getExternOrBoarder());
        }
        schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(extInfo)
                .awaitUninterruptibly();

        return MapMessage.successMessage();
    }

    public MapMessage updateSchoolGradeData(Long schoolId, List<SchoolGradeBasicData> gradeDataList) {
        if (CollectionUtils.isEmpty(gradeDataList) || schoolId == null) {
            return MapMessage.errorMessage("年级数据更新失败");
        }
        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (extInfo == null) {
            extInfo = new SchoolExtInfo();
            extInfo.setId(schoolId);
        }


        for (SchoolGradeBasicData p : gradeDataList) {
            ClazzLevel clazzLevel = ClazzLevel.parse(p.getGrade());
            if (clazzLevel == null) {
                continue;
            }
            // 允许 null 和 0
//            if(p.getClazzNum() != null && p.getClazzNum() > 0){
            extInfo.setGradeClazzNum(clazzLevel, p.getClazzNum());
//            }
//            if(p.getStudentNum() != null && p.getStudentNum() > 0){
            extInfo.setGradeStudentNum(clazzLevel, p.getStudentNum());
//            }
        }


        // 设置schoolSize;
        Integer schoolSize = 0;
        List<ClazzLevel> clazzLevelList = fetchClazzLevelList(getSchoolEduSystem(schoolId));
        for (ClazzLevel clazzLevel : clazzLevelList) {
            schoolSize += SafeConverter.toInt(extInfo.fetchGradeStudentNum(clazzLevel, null));
        }
        extInfo.setSchoolSize(schoolSize);

        schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(extInfo)
                .awaitUninterruptibly();

        return MapMessage.successMessage();
    }

    private EduSystemType getSchoolEduSystem(Long schoolId) {
        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (extInfo != null && extInfo.fetchEduSystem() != null) {
            return extInfo.fetchEduSystem();
        }
        School school = raikouSystem.loadSchool(schoolId);
        return EduSystemType.of(school.getDefaultEduSystemType());
    }


    /**
     * 拼装学制
     *
     * @param schoolEduSystemType
     * @return
     */
    public List<Map<String, Object>> appendEduSystemType(EduSystemType schoolEduSystemType) {
        List<Map<String, Object>> eduSystemTypes = new ArrayList<>();
        for (EduSystemType eduSystemType : EduSystemType.values()) {
            if (eduSystemType == EduSystemType.I4) {
                continue;
            }

            Map<String, Object> eduSystemTypeInfo = new HashMap<>();
            eduSystemTypeInfo.put("name", eduSystemType.getDescription());
            eduSystemTypeInfo.put("code", eduSystemType.name());
            if (Objects.equals(schoolEduSystemType, eduSystemType)) {
                eduSystemTypeInfo.put("selected", true);
            }
            eduSystemTypeInfo.put("group", eduSystemType.getKtwelve().name());
            eduSystemTypes.add(eduSystemTypeInfo);
        }
        return eduSystemTypes;
    }


    //获取学校近六月科目月活
    public List<Map<String, Object>> generateSchoolChartInfoMonth(Long schoolId) {
        List<Integer> dayList = new ArrayList<>();
        Date date = performanceService.lastSuccessDataDate();
        dayList.add(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")));
        for (int i = 0; i < 5; i++) {
            dayList.add(0, SafeConverter.toInt(DateUtils.dateToString(DayUtils.getLastDayOfMonth(DayUtils.addMonth(date, -(i + 1))), "yyyyMMdd")));
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Integer day : dayList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("month", day / 100);
            itemMap.put("subjectPerformanceMap", getSubjectPerformance(schoolId, day, 1).getSubjectIndicatorMap());      // 本月作业套数
            resultList.add(itemMap);
        }
        return resultList;
    }

    //获取学校近六天科目月活
    public List<Map<String, Object>> generateSchoolChartInfoDay(Long schoolId, Integer startDate, String direction) {
        List<Integer> dayList = new ArrayList<>();
        if (StringUtils.isNotBlank(direction) && startDate > 0) {//前端传的左滑右划
            Date date = DateUtils.stringToDate(String.valueOf(startDate), "yyyyMMdd");
            if (Objects.equals("right", direction)) {
                for (int i = 0; i < 6; i++) {
                    dayList.add(0, SafeConverter.toInt(DateUtils.dateToString(DayUtils.addDay(date, (i + 1)), "yyyyMMdd")));
                }
            } else {
                for (int i = 0; i < 6; i++) {
                    dayList.add(0, SafeConverter.toInt(DateUtils.dateToString(DayUtils.addDay(date, -(i + 1)), "yyyyMMdd")));
                }
            }
        } else {
            Date date = performanceService.lastSuccessDataDate();
            dayList.add(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")));
            for (int i = 0; i < 6; i++) {
                dayList.add(0, SafeConverter.toInt(DateUtils.dateToString(DayUtils.addDay(date, -(i + 1)), "yyyyMMdd")));
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Integer day : dayList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("day", day);
            itemMap.put("subjectPerformanceMap", getSubjectPerformance(schoolId, day, 2).getSubjectIndicatorMap());      // 本月作业套数
            resultList.add(itemMap);
        }
        return resultList;
    }

    /**
     * 查询学校指标数据
     *
     * @param schoolId
     * @param day
     * @param type
     * @return
     */
    public SchoolOnlineIndicatorData getSubjectPerformance(Long schoolId, Integer day, Integer type) {
        SchoolOnlineIndicatorData schoolOnlineIndicatorData = new SchoolOnlineIndicatorData();
        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(Collections.singleton(schoolId), day);
        SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(schoolId);
        if (null != schoolOnlineIndicator) {
            //本月数据
            OnlineIndicator onlineIndicator;
            if (type == 1) {
                onlineIndicator = schoolOnlineIndicator.fetchMonthData();
            } else {
                onlineIndicator = schoolOnlineIndicator.fetchDayData();
            }
            //获取学校online学科指标数据
            schoolOnlineIndicatorData = getSchoolOnlineSubjectIndicator(schoolOnlineIndicatorData, onlineIndicator, null);
        }
        return schoolOnlineIndicatorData;
    }

    /**
     * 月活TOP校
     *
     * @param currentUser
     * @param longitude
     * @param latitude
     * @param type
     * @param regionCode
     * @param subjectCode
     * @param topN
     * @param model
     * @return
     */
    public List<AgentMauTopSchoolInfo> mauTopSchoolList(AuthCurrentUser currentUser, Double longitude, Double latitude, Integer type, Integer regionCode, Integer subjectCode, Integer topN, Integer model) {
        List<AgentMauTopSchoolInfo> mauTopSchoolInfoList = new ArrayList<>();
        //拼装月活TOP校参数信息
        Map<Integer, List<Long>> typeIdsMap = new HashMap<>();
        List<Integer> schoolLevelList = new ArrayList<>();
        Set<Integer> countyCodes = new HashSet<>();
        generateMauTopSchoolParamInfo(currentUser, type, regionCode, countyCodes, typeIdsMap, schoolLevelList);

        if (MapUtils.isNotEmpty(typeIdsMap)) {
            Set<Integer> idTypes = typeIdsMap.keySet();
            if (CollectionUtils.isNotEmpty(idTypes)) {
                Integer idType = idTypes.stream().filter(Objects::nonNull).findFirst().orElse(null);
                List<Long> ids = typeIdsMap.get(idType);
                if (idType != null && CollectionUtils.isNotEmpty(ids)) {
                    //获取学校月活TOP信息
                    mauTopSchoolInfoList.addAll(loadNewSchoolServiceClient.loadMauTOPSchoolData(ids, idType, countyCodes, subjectCode, topN, model, schoolLevelList));
                }
            }
        }

        List<Long> schoolIds = mauTopSchoolInfoList.stream().map(AgentMauTopSchoolInfo::getSchoolId).collect(toList());
        //拼装学校基础信息
        List<AgentMauTopSchoolInfo> mauTopSchoolInfoPage = generateMauTopSchoolInfoBySchoolIdsAndSubject(schoolIds, subjectCode);
        if (CollectionUtils.isNotEmpty(mauTopSchoolInfoPage)) {
            Map<Long, AgentMauTopSchoolInfo> mauTopSchoolInfoMap = mauTopSchoolInfoPage.stream().collect(Collectors.toMap(AgentMauTopSchoolInfo::getSchoolId, Function.identity(), (o1, o2) -> o1));
            mauTopSchoolInfoList.forEach(item -> {
                AgentMauTopSchoolInfo mauTopSchoolInfo = mauTopSchoolInfoMap.get(item.getSchoolId());
                if (null != mauTopSchoolInfo) {
                    try {
                        Integer lastSixMonthMaxFinHwGte3StuCount = item.getLastSixMonthMaxFinHwGte3StuCount();
                        BeanUtils.copyProperties(item, mauTopSchoolInfo);
                        item.setLastSixMonthMaxFinHwGte3StuCount(lastSixMonthMaxFinHwGte3StuCount);
                    } catch (Exception e) {
                        logger.error("bean copy error", e);
                    }
                }
            });
        }
        //拼装学校距离信息
        Page<SchoolEsInfo> schoolEsInfoPage = searchService.searchSchoolWithSchoolIds(schoolIds, longitude, latitude, 0, topN);
        Map<String, SchoolEsInfo> schoolIdEsInfoMap = schoolEsInfoPage.getContent().stream().collect(Collectors.toMap(SchoolEsInfo::getId, Function.identity()));
        mauTopSchoolInfoList.forEach(item -> {
            SchoolEsInfo schoolEsInfo = schoolIdEsInfoMap.get(ConversionUtils.toString(item.getSchoolId()));
            if (null != schoolEsInfo) {
                item.setGenDistance(schoolEsInfo.getGenDistance());
            }
        });
        return mauTopSchoolInfoList;
    }

    /**
     * 拼装月活TOP校参数信息
     *
     * @param currentUser
     * @param type
     * @param regionCode
     * @param countyCodes
     * @param typeIdsMap
     * @param schoolLevelList
     */
    public void generateMauTopSchoolParamInfo(AuthCurrentUser currentUser, Integer type, Integer regionCode, Set<Integer> countyCodes, Map<Integer, List<Long>> typeIdsMap, List<Integer> schoolLevelList) {
        Long currentUserId = currentUser.getUserId();
        Integer idType;//ID类型 1: 部门 ID   2：人员ID
        List<Long> ids = new ArrayList<>();
        Integer schoolLevelFlag = 1;
        //全国总监
        if (currentUser.isCountryManager()) {
            idType = AgentConstants.INDICATOR_TYPE_GROUP;
            //默认
            if (type == 0) {
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(currentUserId).stream().filter(Objects::nonNull).findFirst().orElse(null);
                if (null != groupUser) {
                    ids.add(groupUser.getGroupId());
                }
                schoolLevelFlag = 124;
            } else {
                List<AgentGroup> allGroups = baseOrgService.findAllGroups();
                AgentGroup marketingGroup = new AgentGroup();
                if (type == 1) {
                    marketingGroup = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
                    if (null != marketingGroup) {
                        ids.add(marketingGroup.getId());
                    }
                    schoolLevelFlag = 1;
                    //中学市场
                } else if (type == 2) {
                    marketingGroup = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
                    if (null != marketingGroup) {
                        ids.add(marketingGroup.getId());
                    }
                    schoolLevelFlag = 24;
                }
            }
        } else {
            if (currentUser.isBusinessDeveloper()) {
                ids.add(currentUserId);
                idType = AgentConstants.INDICATOR_TYPE_USER;
            } else {
                idType = AgentConstants.INDICATOR_TYPE_GROUP;
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(currentUserId).stream().filter(Objects::nonNull).findFirst().orElse(null);
                if (null != groupUser) {
                    ids.add(groupUser.getGroupId());
                }
            }
            AgentGroup group = baseOrgService.getUserGroups(currentUserId).stream().filter(Objects::nonNull).findFirst().orElse(null);
            if (group != null) {
                if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)) {
                    schoolLevelFlag = 1;
                } else if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)) {
                    schoolLevelFlag = 24;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            schoolLevelList.addAll(agentSchoolLevelSupport.fetchTargetSchoolLevelIds(ids.get(0), idType, schoolLevelFlag));
        }
        typeIdsMap.put(idType, ids);

        //默认情况，没有选地址的情况下
        if (regionCode == 0) {
            AgentRoleType userRole = baseOrgService.getUserRole(currentUserId);
            Collection<ExRegion> groupCountyRegion = baseOrgService.getGroupCountyRegion(currentUserId, userRole, type);
            countyCodes.addAll(groupCountyRegion.stream().map(ExRegion::getId).collect(Collectors.toSet()));
        } else {
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (exRegion != null) {
                // 获取countyCode
                if (exRegion.fetchRegionType() == RegionType.PROVINCE) {
                    List<ExRegion> cityRegionList = exRegion.getChildren();
                    cityRegionList.forEach(city -> {
                        List<ExRegion> countyRegionList = city.getChildren();
                        countyRegionList.forEach(t -> countyCodes.add(t.getId()));
                    });
                } else if (exRegion.fetchRegionType() == RegionType.CITY) {
                    countyCodes.addAll(exRegion.getChildren().stream().map(ExRegion::getId).collect(Collectors.toSet()));
                } else if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                    countyCodes.add(exRegion.getId());
                }
            }
        }
    }

    /**
     * 拼装月活TOP校
     *
     * @param schoolIds
     * @param subjectCode
     * @return
     */
    public List<AgentMauTopSchoolInfo> generateMauTopSchoolInfoBySchoolIdsAndSubject(Collection<Long> schoolIds, Integer subjectCode) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);

        List<School> schoolList = schoolMap.values().stream().filter(p -> p.getSchoolAuthenticationState() != AuthenticationState.FAILURE).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schoolList)) {
            return Collections.emptyList();
        }
        schoolIds = schoolList.stream().map(School::getId).collect(Collectors.toSet());

        Map<Long, List<AgentUserSchool>> userSchoolMap = baseOrgService.getUserSchoolBySchools(schoolIds);

        Set<Long> userIds = new HashSet<>();
        userSchoolMap.forEach((k, v) -> {
            userIds.addAll(v.stream().map(AgentUserSchool::getUserId).collect(Collectors.toSet()));
        });
        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);

        // 获取extInfo数据
        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIds).getUninterruptibly();

        List<AgentMauTopSchoolInfo> resultList = new ArrayList<>();
        schoolList.forEach(school -> {
            AgentMauTopSchoolInfo schoolInfo = new AgentMauTopSchoolInfo();
            schoolInfo.setSchoolId(school.getId());
            schoolInfo.setSchoolName(school.getCname());
            schoolInfo.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
            SchoolExtInfo schoolExtInfo = extInfoMap.get(school.getId());
            if (null != schoolExtInfo) {
                schoolInfo.setAddress(schoolExtInfo.getAddress());
            }

            List<AgentUserSchool> userSchoolList = userSchoolMap.get(school.getId());
            if (CollectionUtils.isNotEmpty(userSchoolList)) {
                AgentUserSchool userSchool = userSchoolList.get(0);
                schoolInfo.setHasBd(true);
                schoolInfo.setBdId(userSchool.getUserId());
                AgentUser agentUser = userMap.get(userSchool.getUserId());
                if (null != agentUser) {
                    schoolInfo.setBdName(agentUser.getRealName());
                }
            }
            resultList.add(schoolInfo);

        });

        // 设置负责该学校的专员姓名
//        Set<Long> userIds = resultList.stream().filter(AgentMauTopSchoolInfo::getHasBd).map(AgentMauTopSchoolInfo::getBdId).filter(p -> p != null).collect(Collectors.toSet());

//        resultList.forEach(p -> {
//            if (p.getHasBd() && p.getBdId() != null && userMap.get(p.getBdId()) != null) {
//                p.setBdName(userMap.get(p.getBdId()).getRealName());
//            }
//        });

        Integer day = performanceService.lastSuccessDataDay();
        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(schoolIds, day);
        resultList.forEach(item -> {
            SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(item.getSchoolId());
            if (null != schoolOnlineIndicator) {
                OnlineIndicator onlineIndicator = schoolOnlineIndicator.fetchMonthData();
                if (null != onlineIndicator) {
                    if (subjectCode == 2) {
                        item.setFinHwGte3StuCount(SafeConverter.toInt(onlineIndicator.getIncSettlementChnStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumChn()));    //语文本月月活
                    }
                    if (subjectCode == 3) {
                        item.setFinHwGte3StuCount(SafeConverter.toInt(onlineIndicator.getIncSettlementMathStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumMath()));  //数学本月月活
                    }
                    if (subjectCode == 4) {
                        item.setFinHwGte3StuCount(SafeConverter.toInt(onlineIndicator.getIncSettlementEngStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumEng()));    //英语本月月活
                    }
                }
            }
        });

        return resultList;
    }

    //获取学校扩展信息
    public SchoolExtInfo getSchoolExtInfo(Long schoolId) {
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        return schoolExtInfo;
    }

    /**
     * 获取当前登录人（市经理及以上）所在部门的专员信息
     *
     * @param currentUser
     * @return
     */
    public List<Map<String, Object>> businessDeveloperList(AuthCurrentUser currentUser) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //获取用户部门
        List<AgentGroup> userGroups = baseOrgService.getUserGroups(currentUser.getUserId());
        if (CollectionUtils.isNotEmpty(userGroups)) {
            AgentGroup agentGroup = userGroups.get(0);
            //获取部门及其子部门中，部门类型为“分区”的部门
            List<AgentGroup> cityGroupList = new ArrayList<>();
            if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
                cityGroupList.add(agentGroup);
            } else {
                List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
                cityGroupList.addAll(subGroupList);
            }

            Set<Long> groupIds = cityGroupList.stream().filter(Objects::nonNull).map(AgentGroup::getId).collect(Collectors.toSet());
            //获取指定部门（多个）中，指定角色的用户
            List<Long> userIds = baseOrgService.getUserByGroupIdsAndRole(groupIds, AgentRoleType.BusinessDeveloper);

            List<AgentUser> userList = baseOrgService.getUsers(userIds);
            //市经理，增加“未分配”
            if (currentUser.isCityManager()) {
                AgentUser agentUser = new AgentUser();
                agentUser.setId(OTHER_ID);
                agentUser.setRealName("未分配");
                userList.add(agentUser);
            }
            //根据用户姓名首字母分组
            Map<String, List<AgentUser>> firstCapitalUserMap = userList.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getRealName())));

            //用户姓名首字母排序
            List<String> sortedFirstCapital = firstCapitalUserMap.keySet().stream().sorted(Comparator.comparing(item -> item == null ? "" : item, Collator.getInstance(Locale.CHINA))).collect(Collectors.toList());

            sortedFirstCapital.forEach(item -> {
                Map<String, Object> dataMap = new HashMap<>();
                List<Map<String, Object>> userMapList = new ArrayList<>();
                List<AgentUser> agentUserList = firstCapitalUserMap.get(item);
                if (CollectionUtils.isNotEmpty(agentUserList)) {
                    agentUserList.forEach(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", user.getId());
                        userMap.put("realName", user.getRealName());
                        userMapList.add(userMap);
                    });
                }
                dataMap.put("firstCapital", item);
                dataMap.put("userList", userMapList);
                dataList.add(dataMap);
            });
        }
        return dataList;
    }

    public List<SchoolBasicInfo> generateMySchoolInfoByIds(Collection<Integer> regionCodes, Collection<Long> schoolIds, Integer sortType, Long userId, Double longitude, Double latitude) {
        //es索引库信息
        Page<SchoolEsInfo> schoolEsInfoPage = null;
        if (CollectionUtils.isNotEmpty(regionCodes)) {
            if (CollectionUtils.isNotEmpty(schoolIds)) {
                schoolEsInfoPage = searchService.searchSchoolInTargetRegionsAndSchoolIds(regionCodes, schoolIds, userId, longitude, latitude, 0, 100);
            } else {
                schoolEsInfoPage = searchService.searchSchoolInTargetRegions(regionCodes, userId, longitude, latitude, 0, 100);
            }
        } else {
            schoolEsInfoPage = searchService.searchSchoolInManagedSchools(schoolIds, userId, longitude, latitude, 0, 100);
        }

        if (schoolEsInfoPage == null || CollectionUtils.isEmpty(schoolEsInfoPage.getContent())) {
            return Collections.emptyList();
        }
        Map<String, SchoolEsInfo> schoolIdEsInfoMap = schoolEsInfoPage.getContent().stream().collect(Collectors.toMap(SchoolEsInfo::getId, Function.identity()));

        List<Long> schoolIdList = new ArrayList<>();
        schoolIdEsInfoMap.keySet().forEach(item -> {
            schoolIdList.add(SafeConverter.toLong(item));
        });

        List<SchoolBasicInfo> schoolBasicInfoAllList = new ArrayList<>();
        //学校基本信息
        List<SchoolBasicInfo> schoolBasicInfoList = generateSchoolBasicInfoByIds(schoolIdList);
        Map<Long, SchoolBasicInfo> schoolBasicInfoMap = schoolBasicInfoList.stream().collect(Collectors.toMap(SchoolBasicInfo::getSchoolId, Function.identity(), (o1, o2) -> o1));

        //学校扩展信息
        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIdList).getUninterruptibly();

        //获取学校标签
        List<String> schoolIdStrList = new ArrayList<>();
        schoolIds.forEach(item -> schoolIdStrList.add(SafeConverter.toString(item)));
        Map<String, List<com.voxlearning.utopia.agent.persist.entity.tag.AgentTag>> schoolTagMap = agentTagService.getTagListByTargetIdsAndType(schoolIdStrList, AgentTagTargetType.SCHOOL, true);

        //有距离信息
        List<SchoolBasicInfo> schoolBasicInfoHaveGenDistanceList = new ArrayList<>();
        //没有距离信息
        List<SchoolBasicInfo> schoolBasicInfoNoGenDistanceList = new ArrayList<>();
        for (Long schoolId : schoolIdList) {
            SchoolBasicInfo schoolBasicInfo = schoolBasicInfoMap.get(schoolId);
            if (null != schoolBasicInfo) {
                SchoolEsInfo schoolEsInfo = schoolIdEsInfoMap.get(ConversionUtils.toString(schoolId));
                if (null != schoolEsInfo) {
                    //拼装有无竞品标识
                    SchoolExtInfo schoolExtInfo = schoolExtInfoMap.get(schoolId);
                    if (null != schoolExtInfo && null != schoolExtInfo.getCompetitiveProductFlag()) {
                        schoolBasicInfo.setCompetitiveProductFlag(schoolExtInfo.getCompetitiveProductFlag());
                    } else {
                        schoolBasicInfo.setCompetitiveProductFlag(0);
                    }
                    //拼装地址
                    schoolBasicInfo.setAddress(schoolEsInfo.getAddress());
                    //拼装距离
                    schoolBasicInfo.setGenDistance(schoolEsInfo.getGenDistance());

                    if (StringUtils.isNotBlank(schoolEsInfo.getGenDistance())) {
                        //单位为“千米”
                        if (StringUtils.contains(schoolEsInfo.getGenDistance(), "km")) {
                            schoolBasicInfo.setGenDistanceDouble(MathUtils.doubleMultiply(SafeConverter.toDouble(StringUtils.replace(schoolEsInfo.getGenDistance(), "km", "")), 1000));
                            //单位为“米”
                        } else {
                            schoolBasicInfo.setGenDistanceDouble(SafeConverter.toDouble(StringUtils.replace(schoolEsInfo.getGenDistance(), "m", "")));
                        }
                        if (schoolBasicInfo.getGenDistanceDouble() > 0) {
                            schoolBasicInfoHaveGenDistanceList.add(schoolBasicInfo);
                        } else {
                            schoolBasicInfoNoGenDistanceList.add(schoolBasicInfo);
                        }
                    } else {
                        schoolBasicInfoNoGenDistanceList.add(schoolBasicInfo);
                    }
                }

                schoolBasicInfo.setTagList(schoolTagMap.get(SafeConverter.toString(schoolId)));
                schoolBasicInfoAllList.add(schoolBasicInfo);
            }
        }

        //过滤出字典表学校
        schoolBasicInfoAllList = schoolBasicInfoAllList.stream().filter(p -> p.getIsDictSchool() != null && p.getIsDictSchool()).collect(toList());

        //排序
        if (sortType > 0) {
            //按照拜访时间由新到旧
            if (sortType == 1) {
                schoolBasicInfoAllList = schoolBasicInfoAllList.stream().filter(p -> p != null && p.getLastVisitTimeLong() != null).sorted(Comparator.comparing(SchoolBasicInfo::getLastVisitTimeLong).reversed()).collect(toList());
                //按照拜访时间由旧到新
            } else if (sortType == 2) {
                schoolBasicInfoAllList = schoolBasicInfoAllList.stream().filter(p -> p != null && p.getLastVisitTimeLong() != null).sorted(Comparator.comparing(SchoolBasicInfo::getLastVisitTimeLong)).collect(toList());
                //按距离由近到远
            } else if (sortType == 3) {
                schoolBasicInfoHaveGenDistanceList = schoolBasicInfoHaveGenDistanceList.stream().filter(p -> p != null && p.getGenDistanceDouble() != null).sorted(Comparator.comparing(SchoolBasicInfo::getGenDistanceDouble)).collect(toList());
                schoolBasicInfoAllList = new ArrayList<>();
                schoolBasicInfoAllList.addAll(schoolBasicInfoHaveGenDistanceList);
                schoolBasicInfoAllList.addAll(schoolBasicInfoNoGenDistanceList);
                //按距离由远到近
            } else if (sortType == 4) {
                schoolBasicInfoHaveGenDistanceList = schoolBasicInfoHaveGenDistanceList.stream().filter(p -> p != null && p.getGenDistanceDouble() != null).sorted(Comparator.comparing(SchoolBasicInfo::getGenDistanceDouble).reversed()).collect(toList());
                schoolBasicInfoAllList = new ArrayList<>();
                schoolBasicInfoAllList.addAll(schoolBasicInfoHaveGenDistanceList);
                schoolBasicInfoAllList.addAll(schoolBasicInfoNoGenDistanceList);
            }
        }
        return schoolBasicInfoAllList;
    }


    /**
     * 月活Top校排名
     *
     * @param schoolId
     * @param currentUser
     * @param type
     * @param regionCode
     * @param topN
     * @param model
     * @return
     */
    public List<Map<String, Object>> mauTopSchoolRanking(Long schoolId, AuthCurrentUser currentUser, Integer type, Integer regionCode, Integer topN, Integer model) {
        List<Map<String, Object>> subjectRankingList = new ArrayList<>();
        //拼装月活TOP校参数信息
        Map<Integer, List<Long>> typeIdsMap = new HashMap<>();
        List<Integer> schoolLevelList = new ArrayList<>();
        Set<Integer> countyCodes = new HashSet<>();
        generateMauTopSchoolParamInfo(currentUser, type, regionCode, countyCodes, typeIdsMap, schoolLevelList);

        if (MapUtils.isNotEmpty(typeIdsMap)) {
            Set<Integer> idTypes = typeIdsMap.keySet();
            if (CollectionUtils.isNotEmpty(idTypes)) {
                Integer idType = idTypes.stream().filter(Objects::nonNull).findFirst().orElse(null);
                List<Long> ids = typeIdsMap.get(idType);
                if (idType != null && CollectionUtils.isNotEmpty(ids)) {
                    Map<String, Object> chiSubjectRankingMap = getMauTopSchoolSubjectRankingMap(schoolId, idType, ids, 2, countyCodes, schoolLevelList, topN, model);
                    if (MapUtils.isNotEmpty(chiSubjectRankingMap)) {
                        subjectRankingList.add(chiSubjectRankingMap);
                    }
                    Map<String, Object> mathSubjectRankingMap = getMauTopSchoolSubjectRankingMap(schoolId, idType, ids, 3, countyCodes, schoolLevelList, topN, model);
                    if (MapUtils.isNotEmpty(mathSubjectRankingMap)) {
                        subjectRankingList.add(mathSubjectRankingMap);
                    }
                    Map<String, Object> engSubjectRankingMap = getMauTopSchoolSubjectRankingMap(schoolId, idType, ids, 4, countyCodes, schoolLevelList, topN, model);
                    if (MapUtils.isNotEmpty(engSubjectRankingMap)) {
                        subjectRankingList.add(engSubjectRankingMap);
                    }
                }
            }
        }
        return subjectRankingList;
    }

    public Map<String, Object> getMauTopSchoolSubjectRankingMap(Long schoolId, Integer idType, List<Long> ids, Integer subjectCode, Set<Integer> countyCodes, List<Integer> schoolLevelList, Integer topN, Integer model) {
        Subject subject = Subject.CHINESE;
        if (subjectCode == 2) {
            subject = Subject.CHINESE;
        } else if (subjectCode == 3) {
            subject = Subject.MATH;
        } else if (subjectCode == 4) {
            subject = Subject.ENGLISH;
        }
        Map<String, Object> subjectRankingMap = new HashMap<>();
        //获取学校月活TOP信息
        List<AgentMauTopSchoolInfo> mauTopSchoolInfoList = loadNewSchoolServiceClient.loadMauTOPSchoolData(ids, idType, countyCodes, subjectCode, topN, model, schoolLevelList);
        if (CollectionUtils.isNotEmpty(mauTopSchoolInfoList)) {
            int ranking = 0;
            for (AgentMauTopSchoolInfo mauTopSchoolInfo : mauTopSchoolInfoList) {
                ranking++;
                if (Objects.equals(mauTopSchoolInfo.getSchoolId(), schoolId)) {
                    subjectRankingMap.put("subject", subject.name());
                    subjectRankingMap.put("ranking", ranking);
                    break;
                }
            }
        }
        return subjectRankingMap;
    }

    /**
     * 待办任务
     *
     * @param schoolId
     * @return
     */
    public Map<String, Object> todoTaskList(Long schoolId) {
        Map<String, Object> dataMap = new HashMap<>();
        /*
        补充信息：学校学制、英语起始年级、走读方式、年级班级人数某个字段填写不全时，出现此任务
         */
        Boolean supplementaryInfoShow = false;
        SchoolBasicExtData schoolBasicExtData = generateSchoolBasicExtData(schoolId);
        if (schoolBasicExtData != null) {
            if (schoolBasicExtData.getEduSystem() == null || schoolBasicExtData.getEnglishStartGrade() == null || schoolBasicExtData.getExternOrBoarder() == null) {
                supplementaryInfoShow = true;
            }
        }
        List<SchoolGradeBasicData> gradeDataList = generateGradeBasicDataList(schoolId);
        if (CollectionUtils.isNotEmpty(gradeDataList)) {
            for (SchoolGradeBasicData schoolGradeBasicData : gradeDataList) {
                if (schoolGradeBasicData.getClazzNum() == null || schoolGradeBasicData.getStudentNum() == null) {
                    supplementaryInfoShow = true;
                }
            }
        }
        dataMap.put("supplementaryInfoShow", supplementaryInfoShow);

        /*
        维护老师：“工作—维护老师”模块若有在任务期内，且未维护的老师任务时，出现此任务，并显示待维护的老师数量
         */
        int maintainTeacherNum = 0;
        AgentMainTask mainTask = agentTaskManageService.getMainTaskBySchoolId(schoolId);
        if (mainTask != null) {
            List<AgentSubTask> subTaskList = agentSubTaskDao.findTaskSubyMainTaskId(mainTask.getId());
            if (CollectionUtils.isNotEmpty(subTaskList)) {
                //当前学校未完成的子任务
                List<AgentSubTask> unfinishedSubTaskList = subTaskList.stream().filter(p -> !p.getIfFollowUp() && !p.getIfHomework() && Objects.equals(p.getSchoolId(), schoolId)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(unfinishedSubTaskList)) {
                    maintainTeacherNum = unfinishedSubTaskList.stream().map(AgentSubTask::getTeacherId).collect(Collectors.toSet()).size();
                }
            }
        }
        dataMap.put("maintainTeacherNum", maintainTeacherNum);

        /*
        处理换班：“工作—处理换班”模块若有未处理的换班任务时，出现此任务，并显示待处理的换班数量
         */
        dataMap.put("teacherAlterationNum", agentResourceService.countPendingClazzAlterationBySchool(Collections.singleton(schoolId), 10, 0));
        return dataMap;
    }

    /**
     * 学校数据中心-家长
     *
     * @param schoolId
     * @return
     */
    public SchoolParentIndicatorData getSchoolParentIndicator(Long schoolId) {
        SchoolParentIndicatorData schoolParentIndicatorData = new SchoolParentIndicatorData();
        schoolParentIndicatorData.setSchoolId(schoolId);
        Integer day = performanceService.lastSuccessDataDay();
        Map<Long, SchoolOnlineIndicator> schoolOnlineIndicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(Collections.singleton(schoolId), day);
        Map<Long, SchoolParentIndicator> schoolParentIndicatorMap = loadParentServiceClient.loadSchoolParentIndicator(Collections.singleton(schoolId), day);
        SchoolOnlineIndicator schoolOnlineIndicator = schoolOnlineIndicatorMap.get(schoolId);
        SchoolParentIndicator schoolParentIndicator = schoolParentIndicatorMap.get(schoolId);
        // 获取extInfo数据
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolOnlineIndicator != null && schoolParentIndicator != null) {
            OnlineIndicator sumOnlineIndicator = schoolOnlineIndicator.fetchSumData();
            ParentIndicator sumParentIndicator = schoolParentIndicator.fetchSumData();
            ParentIndicator monthParentIndicator = schoolParentIndicator.fetchMonthData();
            if (sumParentIndicator != null && monthParentIndicator != null) {
                int schoolSize = schoolExtInfo != null ? SafeConverter.toInt(schoolExtInfo.getSchoolSize()) : 0;
                schoolParentIndicatorData.setParentPermeateRate(MathUtils.doubleDivide(SafeConverter.toInt(sumParentIndicator.getBindParentStuNum()), schoolSize));
                schoolParentIndicatorData.setBindParentStuNum(SafeConverter.toInt(sumParentIndicator.getBindParentStuNum()));

                schoolParentIndicatorData.setTmBindParentStuNum(SafeConverter.toInt(monthParentIndicator.getBindParentStuNum()));
                schoolParentIndicatorData.setParentMauc(SafeConverter.toInt(monthParentIndicator.getTmLoginGte3BindStuParentNum()));
                schoolParentIndicatorData.setParentStuActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getParentStuActiveSettlementNum()));
                schoolParentIndicatorData.setNewParentActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getNewParentActiveSettlementNum()));

                schoolParentIndicatorData.setTmRegisterBindParentStuNum(SafeConverter.toInt(monthParentIndicator.getTmRegisterBindParentStuNum()));
                schoolParentIndicatorData.setTmRegisterUnBindParentStuNum(SafeConverter.toInt(monthParentIndicator.getTmRegisterUnbindParentStuNum()));

                //历史注册学生绑定家长学生 = 已绑定家长学生（累计） - 本月新注册学生且已经绑定家长的学生数
                schoolParentIndicatorData.setHistoryRegisterBindParentStuNum(SafeConverter.toInt(sumParentIndicator.getBindParentStuNum()) - SafeConverter.toInt(monthParentIndicator.getTmRegisterBindParentStuNum()));

                if (sumOnlineIndicator != null) {
                    //历史注册学生未绑定家长学生 = 注册学生（累计） - 已绑定家长学生（累计）- 本月新注册学生未绑定家长的学生数
                    schoolParentIndicatorData.setHistoryRegisterUnBindParentStuNum(SafeConverter.toInt(sumOnlineIndicator.getRegStuCount()) - SafeConverter.toInt(sumParentIndicator.getBindParentStuNum()) - SafeConverter.toInt(monthParentIndicator.getTmRegisterUnbindParentStuNum()));
                }

                //本月仅登录1次家长：已绑定学生本月登录1次及以上家长—已绑定学生本月登录2次及以上家长
                schoolParentIndicatorData.setTmLoginEq1BindStuParentNum(SafeConverter.toInt(monthParentIndicator.getTmLoginGte1BindStuParentNum()) - SafeConverter.toInt(monthParentIndicator.getTmLoginGte2BindStuParentNum()));
                //本月仅登录2次家长：已绑定学生本月登录2次及以上家长—已绑定学生本月登录3次及以上家长
                schoolParentIndicatorData.setTmLoginEq2BindStuParentNum(SafeConverter.toInt(monthParentIndicator.getTmLoginGte2BindStuParentNum()) - SafeConverter.toInt(monthParentIndicator.getTmLoginGte3BindStuParentNum()));
            }
        }
        return schoolParentIndicatorData;
    }
}
