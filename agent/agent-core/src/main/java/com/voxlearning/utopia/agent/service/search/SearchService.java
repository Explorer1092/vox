package com.voxlearning.utopia.agent.service.search;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.mapper.SchoolEsQuery;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolEsInfoServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.TeacherSummaryEsInfo;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.TeacherSummaryQuery;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSummaryEsServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SearchService
 *
 * @author song.wang
 * @date 2018/5/24
 */
@Named
public class SearchService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentRegionService agentRegionService;
    @Inject
    private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private SchoolEsInfoServiceClient schoolEsInfoServiceClient;
    @Inject
    private TeacherSummaryEsServiceClient teacherSummaryEsServiceClient;

    public static final int SCENE_DICT = 1;   // 责任区域  直属部门负责行政区内该角色名下的字典表学校及学校中的老师
    public static final int SCENE_REGION = 2;  // 范围区域   直属部门负责行政区内的所有学校及学校中的老师
    public static final int SCENE_SEA = 3;   // 公私海   直属部门负责行政区内的所有学校及学校中的老师, 且非其他专员或其他部门的学校及学校中的老师


    private int days = 0;  // 先这么写， 最好是做一个配置项

    // 判断是否有该学校的权限
    public boolean hasSchoolPermission(Long userId, Long schoolId, Integer scene) {
        if (schoolId == null) {
            return false;
        }
        List<Long> schoolIds = searchSchoolsForSceneWithNew(userId, SafeConverter.toString(schoolId), scene);
        return CollectionUtils.isNotEmpty(schoolIds);
    }

    // 学校搜索， 包括新创建的学校
    public List<Long> searchSchoolsForSceneWithNew(Long userId, String schoolKey, Integer scene) {
        List<Long> schoolIds = new ArrayList<>();
        Page<SchoolEsInfo> esInfoPage = this.searchSchoolPageForScene(userId, schoolKey, scene, null, null, 0, 100);
        if (CollectionUtils.isNotEmpty(esInfoPage.getContent())) {
            esInfoPage.getContent().stream().forEach(p -> schoolIds.add(SafeConverter.toLong(p.getId())));
        }
        return schoolIds;
    }

    // 判断学校是否在用户所在部门的区域范围内
    private boolean isSchoolInUserGroupRegion(Long userId, Long schoolId) {
        boolean result = false;
        School school = raikouSystem.loadSchool(schoolId);
        if (school != null) {
            // 判断该学校是否属于该用户负责的阶段
            List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
            if (CollectionUtils.isEmpty(schoolLevelList)) {
                return false;
            } else {
                SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel(), null);
                if (schoolLevel != null && !schoolLevelList.contains(schoolLevel)) {
                    return false;
                }
            }
            Integer schoolRegionCode = school.getRegionCode();
            Set<Integer> regionCodes = getUserGroupRegionCodes(userId);
            if (CollectionUtils.isNotEmpty(regionCodes)) {
                if (regionCodes.contains(schoolRegionCode)) {
                    result = true;
                } else {
                    List<Integer> countyCodes = agentRegionService.getCountyCodes(regionCodes);
                    if (countyCodes.contains(schoolRegionCode)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    // 1.dict 责任区域，2.space 范围区域，3.sea 公私海
    // 返回结果为符合条件的SchoolIdList
    public Page<SchoolEsInfo> searchSchoolPageForScene(Long userId, String schoolKey, Integer scene, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        switch (scene) {
            case SearchService.SCENE_DICT:
                return searchSchoolInManagedSchools(userId, schoolKey, longitude, latitude, pageNo, pageSize);
            case SearchService.SCENE_REGION:
                return searchSchoolInManagedRegions(userId, schoolKey, longitude, latitude, pageNo, pageSize);
            case SearchService.SCENE_SEA:
                return searchSchoolExceptTargetSchools(userId, schoolKey, longitude, latitude, pageNo, pageSize);
            default:
                if (pageNo == null || pageNo < 0) {
                    pageNo = 0;
                }
                if (pageSize == null || pageSize < 20) {
                    pageSize = 20;
                }
                return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }
    }

    public List<SchoolLevel> getUserServiceSchoolLevelsWithCache(Long userId) {
        String cacheKey = "AGENT_USER_SERVICE_SCHOOL_LEVELS:UID=" + userId;
        List<SchoolLevel> cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
        if (cacheObject != null) {
            return cacheObject;
        }
        List<SchoolLevel> schoolLevelList = baseOrgService.getUserServiceSchoolLevels(userId);
        agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addHours(new Date(), 1).getTime() / 1000), schoolLevelList);
        return schoolLevelList;
    }

    // 获取用户所在部门区域范围内的学校
    // pageNo 从 0 开始
    private Page<SchoolEsInfo> searchSchoolInManagedSchools(Long userId, String schoolKey, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        return querySchoolFromEsInSchools(managedSchoolList, schoolKey, schoolLevelList, longitude, latitude, pageNo, pageSize);
    }

    // 获取用户所在部门区域范围内的学校
    // pageNo 从 0 开始
    public Page<SchoolEsInfo> searchSchoolInManagedSchools(Collection<Long> schoolIds, Long userId, Integer pageNo, Integer pageSize) {
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        return querySchoolFromEsInSchools(schoolIds, null, schoolLevelList, null, null, pageNo, pageSize);
    }

    public Page<SchoolEsInfo> searchSchoolInManagedSchools(Collection<Long> schoolIds, Long userId, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        return querySchoolFromEsInSchools(schoolIds, null, schoolLevelList, longitude, latitude, pageNo, pageSize);
    }

    public Page<SchoolEsInfo> searchSchoolWithSchoolIds(Collection<Long> schoolIds, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        return querySchoolFromEsWithSchoolIds(schoolIds, longitude, latitude, pageNo, pageSize);
    }

    private Page<SchoolEsInfo> querySchoolFromEsWithSchoolIds(Collection<Long> schoolIds, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {

        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }

        if (CollectionUtils.isEmpty(schoolIds)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        SchoolEsQuery esQuery = new SchoolEsQuery();
        esQuery.setIds(schoolIds);

        List<Integer> authState = new ArrayList<>();
        authState.add(AuthenticationState.WAITING.getState());
        authState.add(AuthenticationState.SUCCESS.getState());
        esQuery.setAuthenticationStates(authState);

        if (longitude != null && latitude != null) {
            String location = StringUtils.join(latitude, ",", longitude);
            esQuery.setCoordinates(location);
        }
        esQuery.setPage(pageNo);
        esQuery.setLimit(pageSize);
        return schoolEsInfoServiceClient.getSchoolEsService().loadSchoolEs(esQuery);
    }

    private Page<SchoolEsInfo> querySchoolFromEsInSchools(Collection<Long> schoolIds, String schoolKey, List<SchoolLevel> schoolLevelList, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {

        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }

        if (CollectionUtils.isEmpty(schoolIds) || CollectionUtils.isEmpty(schoolLevelList)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        SchoolEsQuery esQuery = new SchoolEsQuery();
        esQuery.setKeywords(schoolKey);
        esQuery.setIds(schoolIds);
        if (CollectionUtils.isNotEmpty(schoolLevelList)) {
            esQuery.setLevels(schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toList()));
        }

        List<Integer> authState = new ArrayList<>();
        authState.add(AuthenticationState.WAITING.getState());
        authState.add(AuthenticationState.SUCCESS.getState());
        esQuery.setAuthenticationStates(authState);

        if (longitude != null && latitude != null) {
            String location = StringUtils.join(latitude, ",", longitude);
            esQuery.setCoordinates(location);
        }
        esQuery.setPage(pageNo);
        esQuery.setLimit(pageSize);
        return schoolEsInfoServiceClient.getSchoolEsService().loadSchoolEs(esQuery);
    }


    private Page<SchoolEsInfo> searchSchoolInManagedRegions(Long userId, String schoolKey, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        Set<Integer> regionCodes = getUserGroupRegionCodes(userId);
        return querySchoolFromEsByRegionCodes(regionCodes, schoolKey, schoolLevelList, longitude, latitude, pageNo, pageSize);
    }

    public Page<SchoolEsInfo> searchSchoolInManagedRegions(Long userId, String schoolKey, List<SchoolLevel> schoolLevelList, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        Set<Integer> regionCodes = getUserGroupRegionCodes(userId);
        return querySchoolFromEsByRegionCodes(regionCodes, schoolKey, schoolLevelList, longitude, latitude, pageNo, pageSize);
    }


    private Set<Integer> getUserGroupRegionCodes(Long userId) {
        List<Long> groupIds = baseOrgService.getGroupIdListByUserId(userId);
        Set<Integer> regionCodes = new HashSet<>();
        groupIds.forEach(p -> {
            List<Integer> groupRegionCodes = baseOrgService.getGroupRegionCodeList(p);
            if (CollectionUtils.isNotEmpty(groupRegionCodes)) {
                regionCodes.addAll(groupRegionCodes);
            }
        });
        return regionCodes;
    }

    public Page<SchoolEsInfo> searchSchoolInTargetRegions(Collection<Integer> regionCodes, Long userId, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        return querySchoolFromEsByRegionCodes(regionCodes, null, schoolLevelList, longitude, latitude, pageNo, pageSize);
    }

    /**
     * 返回小学学校
     *
     * @param regionCodes
     * @param longitude
     * @param latitude
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<SchoolEsInfo> searchJuniorSchoolInTargetRegions(Collection<Integer> regionCodes, Long userId, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId).stream().filter(s -> s == SchoolLevel.JUNIOR).collect(Collectors.toList());
        return querySchoolFromEsByRegionCodes(regionCodes, null, schoolLevelList, longitude, latitude, pageNo, pageSize);
    }

    public Page<SchoolEsInfo> querySchoolFromEsByRegionCodes(Collection<Integer> regionCodes, String schoolKey, List<SchoolLevel> schoolLevelList, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 0) {
            pageSize = 20;
        }

        if (CollectionUtils.isEmpty(regionCodes) || CollectionUtils.isEmpty(schoolLevelList)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        Set<Integer> provinceCodes = new HashSet<>();
        Set<Integer> cityCodes = new HashSet<>();
        Set<Integer> countyCodes = new HashSet<>();
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
        if (MapUtils.isNotEmpty(exRegionMap)) {
            exRegionMap.values().forEach(p -> {
                if (p.fetchRegionType() == RegionType.PROVINCE) {
                    provinceCodes.add(p.getId());
                } else if (p.fetchRegionType() == RegionType.CITY) {
                    cityCodes.add(p.getId());
                } else if (p.fetchRegionType() == RegionType.COUNTY) {
                    countyCodes.add(p.getId());
                }
            });
        }

        SchoolEsQuery esQuery = new SchoolEsQuery();
        esQuery.setKeywords(schoolKey);

        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            esQuery.setProvinceCodes(provinceCodes);
        }
        if (CollectionUtils.isNotEmpty(cityCodes)) {
            esQuery.setCityCodes(cityCodes);
        }
        if (CollectionUtils.isNotEmpty(countyCodes)) {
            esQuery.setCountyCodes(countyCodes);
        }

        if (CollectionUtils.isNotEmpty(schoolLevelList)) {
            esQuery.setLevels(schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toList()));
        }

        List<Integer> authState = new ArrayList<>();
        authState.add(AuthenticationState.WAITING.getState());
        authState.add(AuthenticationState.SUCCESS.getState());
        esQuery.setAuthenticationStates(authState);

        if (longitude != null && latitude != null) {
            String location = StringUtils.join(latitude, ",", longitude);
            esQuery.setCoordinates(location);
        }
        esQuery.setPage(pageNo);
        esQuery.setLimit(pageSize);

        return schoolEsInfoServiceClient.getSchoolEsService().loadSchoolEs(esQuery);
    }


    public Page<SchoolEsInfo> searchSchoolInTargetRegionsAndSchoolIds(Collection<Integer> regionCodes, Collection<Long> schoolIds, Long userId, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        return querySchoolFromEsByRegionCodesAndSchoolIds(regionCodes, schoolIds, null, schoolLevelList, longitude, latitude, pageNo, pageSize);
    }

    private Page<SchoolEsInfo> querySchoolFromEsByRegionCodesAndSchoolIds(Collection<Integer> regionCodes, Collection<Long> schoolIds, String schoolKey, List<SchoolLevel> schoolLevelList, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 0) {
            pageSize = 20;
        }

        Set<Integer> provinceCodes = new HashSet<>();
        Set<Integer> cityCodes = new HashSet<>();
        Set<Integer> countyCodes = new HashSet<>();
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
        if (MapUtils.isNotEmpty(exRegionMap)) {
            exRegionMap.values().forEach(p -> {
                if (p.fetchRegionType() == RegionType.PROVINCE) {
                    provinceCodes.add(p.getId());
                } else if (p.fetchRegionType() == RegionType.CITY) {
                    cityCodes.add(p.getId());
                } else if (p.fetchRegionType() == RegionType.COUNTY) {
                    countyCodes.add(p.getId());
                }
            });
        }

        SchoolEsQuery esQuery = new SchoolEsQuery();
        esQuery.setKeywords(schoolKey);
        esQuery.setIds(schoolIds);

        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            esQuery.setProvinceCodes(provinceCodes);
        }
        if (CollectionUtils.isNotEmpty(cityCodes)) {
            esQuery.setCityCodes(cityCodes);
        }
        if (CollectionUtils.isNotEmpty(countyCodes)) {
            esQuery.setCountyCodes(countyCodes);
        }

        if (CollectionUtils.isNotEmpty(schoolLevelList)) {
            esQuery.setLevels(schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toList()));
        }

        List<Integer> authState = new ArrayList<>();
        authState.add(AuthenticationState.WAITING.getState());
        authState.add(AuthenticationState.SUCCESS.getState());
        esQuery.setAuthenticationStates(authState);

        if (longitude != null && latitude != null) {
            String location = StringUtils.join(latitude, ",", longitude);
            esQuery.setCoordinates(location);
        }
        esQuery.setPage(pageNo);
        esQuery.setLimit(pageSize);

        return schoolEsInfoServiceClient.getSchoolEsService().loadSchoolEs(esQuery);
    }


    // 近似准确,  部分负责的区域内学校 - 其他专员负责的学校
    private Page<SchoolEsInfo> searchSchoolExceptTargetSchools(Long userId, String schoolKey, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {

        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        Set<Integer> regionCodes = this.getUserGroupRegionCodes(userId);
        Page<SchoolEsInfo> page = querySchoolFromEsExceptTargetSchools(regionCodes, schoolKey, schoolLevelList, new ArrayList<>(), longitude, latitude, pageNo, pageSize);
        List<Long> schoolIds = page.getContent().stream().map(p -> SafeConverter.toLong(p.getId())).collect(Collectors.toList());
        List<Long> targetSchoolIds = filterSchoolIds(userId, schoolIds);
        List<SchoolEsInfo> newContentList = page.getContent().stream().filter(p -> targetSchoolIds.contains(SafeConverter.toLong(p.getId()))).collect(Collectors.toList());
        return new PageImpl<>(newContentList, new PageRequest(pageNo, pageSize), page.getTotalElements());
    }

    private Page<SchoolEsInfo> querySchoolFromEsExceptTargetSchools(Collection<Integer> regionCodes, String schoolKey, List<SchoolLevel> schoolLevelList, Collection<Long> exceptSchoolIds, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }

        if (CollectionUtils.isEmpty(regionCodes) || CollectionUtils.isEmpty(schoolLevelList)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        Set<Integer> provinceCodes = new HashSet<>();
        Set<Integer> cityCodes = new HashSet<>();
        Set<Integer> countyCodes = new HashSet<>();
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
        if (MapUtils.isNotEmpty(exRegionMap)) {
            exRegionMap.values().forEach(p -> {
                if (p.fetchRegionType() == RegionType.PROVINCE) {
                    provinceCodes.add(p.getId());
                } else if (p.fetchRegionType() == RegionType.CITY) {
                    cityCodes.add(p.getId());
                } else if (p.fetchRegionType() == RegionType.COUNTY) {
                    countyCodes.add(p.getId());
                }
            });
        }

        SchoolEsQuery esQuery = new SchoolEsQuery();
        esQuery.setKeywords(schoolKey);

        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            esQuery.setProvinceCodes(provinceCodes);
        }
        if (CollectionUtils.isNotEmpty(cityCodes)) {
            esQuery.setCityCodes(cityCodes);
        }
        if (CollectionUtils.isNotEmpty(countyCodes)) {
            esQuery.setCountyCodes(countyCodes);
        }

        if (CollectionUtils.isNotEmpty(schoolLevelList)) {
            esQuery.setLevels(schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toList()));
        }

        List<Integer> authState = new ArrayList<>();
        authState.add(AuthenticationState.WAITING.getState());
        authState.add(AuthenticationState.SUCCESS.getState());
        esQuery.setAuthenticationStates(authState);

        if (longitude != null && latitude != null) {
            String location = StringUtils.join(latitude, ",", longitude);
            esQuery.setCoordinates(location);
        }
        esQuery.setPage(pageNo);
        esQuery.setLimit(pageSize);

        return schoolEsInfoServiceClient.getSchoolEsService().loadSchoolEs(esQuery);
    }

    private List<Long> filterSchoolIds(Long userId, Collection<Long> schoolIds) {
        List<Long> resultIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(schoolIds)) {
            return resultIds;
        }

        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);

        Map<Long, List<Integer>> schoolRoleMap = new HashMap<>();

        Map<Long, List<AgentUserSchool>> userSchoolMap = baseOrgService.getUserSchoolBySchools(schoolIds);
        if (MapUtils.isNotEmpty(userSchoolMap)) {
            Set<Long> userIds = userSchoolMap.values().stream().flatMap(List::stream).map(AgentUserSchool::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, List<Integer>> userRoleList = baseOrgService.getGroupUserRoleMapByUserIds(userIds);

            for (Long schoolId : userSchoolMap.keySet()) {
                List<AgentUserSchool> userSchoolList = userSchoolMap.get(schoolId);
                List<Long> userIdList = userSchoolList.stream().map(AgentUserSchool::getUserId).collect(Collectors.toList());
                List<Integer> roleList = new ArrayList<>();
                for (Long uid : userIdList) {
                    List<Integer> tmpList = userRoleList.get(uid);
                    if (CollectionUtils.isNotEmpty(tmpList)) {
                        roleList.addAll(tmpList);
                    }
                }
                schoolRoleMap.put(schoolId, roleList);
            }
        }

        schoolIds.forEach(p -> {
            if (managedSchoolList.contains(p)) {
                resultIds.add(p);
            } else {
                List<Integer> roleList = schoolRoleMap.get(p);
                if (CollectionUtils.isNotEmpty(roleList)) {
                    if (!roleList.contains(roleType.getId())) {
                        resultIds.add(p);
                    }
                } else {
                    resultIds.add(p);
                }
            }
        });
        return resultIds;

    }


    public boolean hasTeacherPermission(Long userId, Long teacherId, Integer scene) {
        if (teacherId == null) {
            return false;
        }

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId).getUninterruptibly();
        if (school == null) {
            return false;
        }
        return hasSchoolPermission(userId, school.getId(), scene);
    }

    // 老师搜索， 包括新老师
    public List<Long> searchTeachersForSceneWithNew(Long userId, String teacherKey, Integer scene) {
        List<Long> teacherIds = new ArrayList<>();
        Page<TeacherSummaryEsInfo> esInfoPage = searchTeacherPageForScene(userId, teacherKey, scene, 0, 100);
        if (CollectionUtils.isNotEmpty(esInfoPage.getContent())) {
            esInfoPage.getContent().stream().forEach(p -> teacherIds.add(p.getTeacherId()));
        }
        return teacherIds;
    }


    /**
     * 搜索老师，分页
     *
     * @param userId
     * @param teacherKey
     * @param scene
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<Long> searchTeachersForSceneWithPage(Long userId, String teacherKey, Integer scene, Integer pageNo, Integer pageSize) {
        List<Long> teacherIds = new ArrayList<>();
        Page<TeacherSummaryEsInfo> esInfoPage = searchTeacherPageForScene(userId, teacherKey, scene, pageNo, pageSize);
        if (CollectionUtils.isNotEmpty(esInfoPage.getContent())) {
            esInfoPage.getContent().stream().forEach(p -> teacherIds.add(p.getTeacherId()));
        }
        return teacherIds;
    }

    // 获取新老师或者新手机号对应的老师
    // 获取指定日期后注册的新老师，或者在指定日期后更新的手机号
    private Set<Long> getTeacherByIdOrMobileForNew(String teacherKey) {
        Set<Long> teacherIds = new HashSet<>();
        if (StringUtils.isNotBlank(teacherKey) && (MobileRule.isMobile(teacherKey) || SafeConverter.toLong(teacherKey) != 0L)) {
            Date startDate = DayRange.newInstance(DateUtils.addDays(new Date(), -days).getTime()).getStartDate();
            if (MobileRule.isMobile(teacherKey)) {  // 手机号的情况, 老师的手机号可能会在当天变更， 所以根据手机号搜索时，UserAuthentication 是否在指定日期后有变更（可能不是手机号变更，但没关系）
                List<UserAuthentication> userAuthenticationList = userLoaderClient.loadMobileAuthentications(teacherKey);
                Set<Long> mobileTeacherIds = userAuthenticationList.stream().filter(p -> !p.isDisabledTrue() && p.getUserType() == UserType.TEACHER && p.getUpdateDatetime().after(startDate)).map(UserAuthentication::getId).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(mobileTeacherIds)) {
                    teacherIds.addAll(mobileTeacherIds);
                }
            } else { // ID的情况, 直接返回老师ID,实时判断权限（包括新老师）
                Long teacherId = SafeConverter.toLong(teacherKey);
//                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//                if(teacher != null && teacher.getCreateTime().after(startDate)){
                if (teacherId > 0) {
                    teacherIds.add(teacherId);
                }

            }
        }
        return teacherIds;
    }


    public Page<TeacherSummaryEsInfo> searchTeacherPageForScene(Long userId, String teacherKey, Integer scene, Integer pageNo, Integer pageSize) {
        switch (scene) {
            case SearchService.SCENE_DICT:
                return searchTeacherInManagedSchools(userId, teacherKey, pageNo, pageSize);
            case SearchService.SCENE_REGION:
                return searchTeacherInManagedRegions(userId, teacherKey, pageNo, pageSize);
            case SearchService.SCENE_SEA:
                return searchTeacherExceptTargetSchools(userId, teacherKey, pageNo, pageSize);
            default:
                if (pageNo == null || pageNo < 0) {
                    pageNo = 0;
                }
                if (pageSize == null || pageSize < 1) {
                    pageSize = 20;
                }
                return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }
    }


    private Page<TeacherSummaryEsInfo> searchTeacherInManagedSchools(Long userId, String teacherKey, Integer pageNo, Integer pageSize) {
        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        return queryTeacherFromEsInSchools(managedSchoolList, teacherKey, schoolLevelList, pageNo, pageSize);
    }

    private Page<TeacherSummaryEsInfo> queryTeacherFromEsInSchools(Collection<Long> schoolIds, String teacherKey, List<SchoolLevel> schoolLevelList, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }

        if (CollectionUtils.isEmpty(schoolIds) || CollectionUtils.isEmpty(schoolLevelList)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        TeacherSummaryQuery query = new TeacherSummaryQuery();
        query.setKeywords(teacherKey);
        query.setSchoolIds(schoolIds);
        query.setSchoolLevels(schoolLevelList.stream().map(SchoolLevel::name).collect(Collectors.toList()));
        query.setPage(pageNo);
        query.setLimit(pageSize);
        return teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
    }


    private Page<TeacherSummaryEsInfo> searchTeacherInManagedRegions(Long userId, String teacherKey, Integer pageNo, Integer pageSize) {
        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        Set<Integer> regionCodes = this.getUserGroupRegionCodes(userId);
        return queryTeacherFromEsByRegionCodes(regionCodes, teacherKey, schoolLevelList, pageNo, pageSize);
    }


    private Page<TeacherSummaryEsInfo> queryTeacherFromEsByRegionCodes(Collection<Integer> regionCodes, String teacherKey, List<SchoolLevel> schoolLevelList, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }

        if (CollectionUtils.isEmpty(regionCodes) || CollectionUtils.isEmpty(schoolLevelList)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        Set<Long> provinceCodes = new HashSet<>();
        Set<Long> cityCodes = new HashSet<>();
        Set<Long> countyCodes = new HashSet<>();
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
        if (MapUtils.isNotEmpty(exRegionMap)) {
            exRegionMap.values().forEach(p -> {
                if (p.fetchRegionType() == RegionType.PROVINCE) {
                    provinceCodes.add(Long.valueOf(p.getId()));
                } else if (p.fetchRegionType() == RegionType.CITY) {
                    cityCodes.add(Long.valueOf(p.getId()));
                } else if (p.fetchRegionType() == RegionType.COUNTY) {
                    countyCodes.add(Long.valueOf(p.getId()));
                }
            });
        }

        TeacherSummaryQuery query = new TeacherSummaryQuery();
        query.setKeywords(teacherKey);
        query.setSchoolLevels(schoolLevelList.stream().map(SchoolLevel::name).collect(Collectors.toList()));
        query.setProvinceCodes(provinceCodes);
        query.setCityCodes(cityCodes);
        query.setCountyCodes(countyCodes);
        query.setPage(pageNo);
        query.setLimit(pageSize);
        return teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
    }


    private Page<TeacherSummaryEsInfo> searchTeacherExceptTargetSchools(Long userId, String teacherKey, Integer pageNo, Integer pageSize) {

        List<SchoolLevel> schoolLevelList = getUserServiceSchoolLevelsWithCache(userId);
        Set<Integer> regionCodes = this.getUserGroupRegionCodes(userId);
        Page<TeacherSummaryEsInfo> page = queryTeacherFromEsExceptTargetSchools(regionCodes, teacherKey, schoolLevelList, new ArrayList<>(), pageNo, pageSize);
        List<Long> schoolIds = page.getContent().stream().map(p -> SafeConverter.toLong(p.getSchoolId())).collect(Collectors.toList());
        List<Long> targetSchoolIds = filterSchoolIds(userId, schoolIds);
        List<TeacherSummaryEsInfo> newContentList = page.getContent().stream().filter(p -> targetSchoolIds.contains(SafeConverter.toLong(p.getSchoolId()))).collect(Collectors.toList());
        return new PageImpl<>(newContentList, new PageRequest(page.getNumber(), page.getSize()), page.getTotalElements());
    }

    private Page<TeacherSummaryEsInfo> queryTeacherFromEsExceptTargetSchools(Collection<Integer> regionCodes, String teacherKey, List<SchoolLevel> schoolLevelList, Collection<Long> exceptSchoolIds, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }

        if (CollectionUtils.isEmpty(regionCodes) || CollectionUtils.isEmpty(schoolLevelList)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        Set<Long> provinceCodes = new HashSet<>();
        Set<Long> cityCodes = new HashSet<>();
        Set<Long> countyCodes = new HashSet<>();
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
        if (MapUtils.isNotEmpty(exRegionMap)) {
            exRegionMap.values().forEach(p -> {
                if (p.fetchRegionType() == RegionType.PROVINCE) {
                    provinceCodes.add(Long.valueOf(p.getId()));
                } else if (p.fetchRegionType() == RegionType.CITY) {
                    cityCodes.add(Long.valueOf(p.getId()));
                } else if (p.fetchRegionType() == RegionType.COUNTY) {
                    countyCodes.add(Long.valueOf(p.getId()));
                }
            });
        }

        TeacherSummaryQuery query = new TeacherSummaryQuery();
        query.setKeywords(teacherKey);
        query.setSchoolLevels(schoolLevelList.stream().map(SchoolLevel::name).collect(Collectors.toList()));
        query.setProvinceCodes(provinceCodes);
        query.setCityCodes(cityCodes);
        query.setCountyCodes(countyCodes);
        query.setPage(pageNo);
        query.setLimit(pageSize);
        return teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
    }


    public List<Long> searchTeachersInSchoolsWithNew(Collection<Long> schoolIds, String teacherKey) {
        List<Long> teacherIds = new ArrayList<>();
        Page<TeacherSummaryEsInfo> esInfoPage = searchTeachersInSchools(schoolIds, teacherKey, 0, 100);
        if (CollectionUtils.isNotEmpty(esInfoPage.getContent())) {
            esInfoPage.getContent().stream().forEach(p -> teacherIds.add(p.getTeacherId()));
        }
        return teacherIds;
    }


    public Page<TeacherSummaryEsInfo> searchTeachersInSchools(Collection<Long> schoolIds, String teacherKey, Integer pageNo, Integer pageSize) {
        return queryTeacherFromEsBySchoolIds(schoolIds, teacherKey, pageNo, pageSize);
    }


    private Page<TeacherSummaryEsInfo> queryTeacherFromEsBySchoolIds(Collection<Long> schoolIds, String teacherKey, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }

        TeacherSummaryQuery query = new TeacherSummaryQuery();
        query.setKeywords(teacherKey);
        query.setSchoolIds(schoolIds);
        query.setPage(pageNo);
        query.setLimit(pageSize);
        return teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
    }

    public Page<TeacherSummaryEsInfo> queryUnAuthTeacherFromEsInSchools(Collection<Long> schoolIds, String regStartTime, String authStatus, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }
        TeacherSummaryQuery query = new TeacherSummaryQuery();
        query.setSchoolIds(schoolIds);
        query.setRegStartTime(regStartTime);
        query.setAuthStatus(authStatus);
        query.setPage(pageNo);
        query.setLimit(pageSize);
        return teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
    }

    public Page<TeacherSummaryEsInfo> querySameDayRegisterTeacherFromEs(Collection<Long> countycodes, String regStartTime, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }
        if (CollectionUtils.isEmpty(countycodes)) {
            return new PageImpl<>(new ArrayList<>(), new PageRequest(pageNo, pageSize), 0);
        }
        TeacherSummaryQuery query = new TeacherSummaryQuery();
        query.setCountyCodes(countycodes);
        query.setRegStartTime(regStartTime);
        query.setPage(pageNo);
        query.setLimit(pageSize);
        return teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
    }

    public Page<TeacherSummaryEsInfo> queryNewRegTeacherFromEsInSchools(Collection<Long> schoolIds, String regStartTime, String regEndTime, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (pageSize == null || pageSize < 20) {
            pageSize = 20;
        }
        TeacherSummaryQuery query = new TeacherSummaryQuery();
        query.setSchoolIds(schoolIds);
        query.setRegStartTime(regStartTime);
        query.setRegEndTime(regEndTime);
        query.setPage(pageNo);
        query.setLimit(pageSize);
        return teacherSummaryEsServiceClient.getTeacherSummaryEsService().query(query);
    }
}
