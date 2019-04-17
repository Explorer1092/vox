package com.voxlearning.utopia.agent.controller.activity;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.EmailRule;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mapper.ActivityConfigMapper;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 趣味活动配置平台
 */
@Controller
@RequestMapping("/activity/config")
@NoArgsConstructor
@Slf4j
public class AgentActivityConfigController extends AbstractAgentController {

    private static final String PATTERN = "yyyy-MM-dd";
    private static final String PATTERN2 = "yyyy-MM-dd 23:59:59";
    private static final String DF = "yyyy/MM/dd";
    private static final String DF2 = "yyyy-MM";

    @Inject private RaikouSystem raikouSystem;
    @Inject private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject private SearchService searchService;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;

    @RequestMapping(value = "publish.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage publish() {
        try {
            String schoolIds = getRequestString("schoolIds");// 学校Ids "," 号分割
            String clazzLevels = getRequestString("clazzLevels");// 年级 "," 号分割
            String areaIds = getRequestString("areaIds");// 区域ids "," 号分割
            if (StringUtils.isBlank(clazzLevels)) {
                return MapMessage.errorMessage("请选择年级");
            }
            ActivityConfig config = new ActivityConfig();
            config.setClazzLevels(splitStringToInteger(clazzLevels, "年级不能为空"));
            boolean selectSchoolOrArea = false;
            if (StringUtils.isNoneBlank(schoolIds)) {
                List<Long> schools = splitStringToLong(schoolIds, "学校不能为空");
                if (schools.size() > 500) {
                    return MapMessage.errorMessage("学校数量大于500");
                }
                config.setSchoolIds(schools);
                selectSchoolOrArea = true;
            }
            if (StringUtils.isNoneBlank(areaIds)) {
                config.setAreaIds(splitStringToLong(areaIds, "区域不能为空"));
                selectSchoolOrArea = true;
            }
            if (!selectSchoolOrArea) {
                return MapMessage.errorMessage("请选择学校或者区域");
            }
            String type = getRequestString("type");// 游戏Id
            String title = getRequestString("title");// 标题
            String description = getRequestString("description");// 描述
            if (StringUtils.isAnyBlank(type, title, description)) {
                return MapMessage.errorMessage("游戏类型、标题、描述 不能为空");
            }
            config.setType(ActivityTypeEnum.valueOf(type));
            config.setTitle(title);
            config.setDescription(description);
            config.setSource(getRequestString("source")); // 來源
            Date startTime = DateUtils.stringToDate(getRequestString("startTime"), PATTERN); // 活动开始时间
            Date endTime = DateUtils.stringToDate(getRequestString("endTime"), PATTERN); // 活动结束时间
            if (startTime == null || endTime == null) {
                return MapMessage.errorMessage("时间格式错误:yyyy-MM-dd");
            }
            endTime = DateUtils.stringToDate(DateUtils.dateToString(endTime, PATTERN2));
            if (startTime.after(endTime)) {
                return MapMessage.errorMessage("开始时间不能大于结束时间");
            }
            if (DateUtils.dayDiff(startTime, endTime) > 30) {
                return MapMessage.errorMessage("时间间隔大于30天");
            }
            config.setStartTime(startTime);
            config.setEndTime(endTime);
            config.setRules(validateRule(getRequestString("rules"))); // 游戏规则 ActivityBaseRule
            config.setApplicant(getCurrentUserId()); // 申请人
            config.setApplicantRole(ActivityConfig.ROLE_AGENT);
            String email = getRequestString("email");
            if (StringUtils.isEmpty(email)) {
                return MapMessage.errorMessage("邮箱不可为空");
            }
            if (!EmailRule.isEmail(email)) {
                return MapMessage.errorMessage("邮箱格式错误");
            }
            config.setEmail(email);

            String proveImg = getRequestString("proveImg");
            if (StringUtils.isNotEmpty(proveImg)) {
                config.setProveImg(proveImg);
            } else {
                if (StringUtils.isNotEmpty(areaIds)) {
                    return MapMessage.errorMessage("区级活动必须上传官网证明");
                }
            }
            return activityConfigServiceClient.getActivityConfigService().publishActivity(config);
        } catch (IllegalArgumentException e) {
            logger.error("publish activity error ", e);
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("publish activity error ", e);
            return MapMessage.errorMessage("发布活动失败");
        }
    }

    /**
     * 正在进行中的活动
     *
     * @return
     */
    @RequestMapping(value = "startinglist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage activityStartingList() {
        Date now = new Date();
        List<ActivityConfig> activities = getActivityConfig();
        return MapMessage.successMessage().add("result", activityMappers(activities.stream().filter(a -> a.isStarting(now)).collect(Collectors.toList())));
    }

    /**
     * 未开始的活动
     *
     * @return
     */
    @RequestMapping(value = "unsetartlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage activityUnStartList() {
        Date now = new Date();
        List<ActivityConfig> activities = getActivityConfig();
        List<Map> result = activityMappers(activities.stream().filter(a -> a.isUnStart(now)).collect(Collectors.toList()));
        return MapMessage.successMessage().add("result", result);
    }

    /**
     * 已经结束的活动
     *
     * @return
     */
    @RequestMapping(value = "endlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage activityEndList() {
        Date now = new Date();
        List<ActivityConfig> activities = getActivityConfig();
        List<Map> result = activityMappers(activities.stream().filter(a -> a.isEnd(now)).collect(Collectors.toList()));
        return MapMessage.successMessage().add("result", result);
    }


    /**
     * 活动详情
     *
     * @return
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage activityDetail() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数校验失败");
        }
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
        if (activityConfig == null) {
            return MapMessage.errorMessage("活动配置不存在");
        }
        return MapMessage.successMessage().add("result", mapperActivity(activityConfig));
    }

    /**
     * 下载报告
     *
     * @return
     */
    @RequestMapping(value = "download.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage downloadActivityReport() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数校验失败");
        }

        return MapMessage.successMessage();
    }

    /**
     * 删除活动
     *
     * @return
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteActivity() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数校验失败");
        }
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
        if (activityConfig == null) {
            return MapMessage.errorMessage("活动配置不存在");
        }
        // 进行中的活动不允许删除
        if (activityConfig.isStarting(new Date())) {
            return MapMessage.errorMessage("进行中的活动不允许删除");
        }
        activityConfigServiceClient.getActivityConfigService().delete(id);
        return MapMessage.successMessage();
    }

    /**
     * 返回全国所有的区
     *
     * @return
     */
    @RequestMapping(value = "getAllRegion.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAllRegion() {
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.BusinessDeveloper || roleType == AgentRoleType.ChannelManager) {
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByUserId(getCurrentUserId(), roleType);
            if (CollectionUtils.isEmpty(counties)) {
                return MapMessage.errorMessage("该用户下无地区");
            }
            Map<String, List<ExRegion>> map = counties.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getCityName())));
            Map<Integer, List<ExRegion>> cityCountyMap = counties.stream().collect(Collectors.groupingBy(ExRegion::getCityCode));
            map.forEach((k, v) -> {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("key", k);
                List<Map> cityList = new ArrayList<>();
                Map<Integer, ExRegion> cityMap = v.stream().collect(Collectors.toMap(ExRegion::getCityCode, Function.identity(), (e1, e2) -> e1));
                cityMap.values().forEach(e -> {
                    List<Map> areaList = cityCountyMap.get(e.getCityCode()).stream().map(c -> MapUtils.map("areaName", c.getCountyName(), "areaCode", c.getCountyCode())).collect(Collectors.toList());
                    cityList.add(MapUtils.m("cityName", e.getCityName(), "cityCode", e.getCityCode(), "areaList", areaList));
                });
                resultMap.put("cityList", cityList);
                resultList.add(resultMap);
            });
        }
        return MapMessage.successMessage().add("data", resultList);
    }

    /**
     * 获取代理商来源
     *
     * @return
     */
    @RequestMapping(value = "getAgentSource.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAgentSource() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return MapMessage.errorMessage("请登录");
        }
        List<AgentGroupUser> agentGroup = agentGroupUserLoaderClient.findByUserId(currentUserId);
        if (agentGroup == null || agentGroup.isEmpty()) {
            return MapMessage.errorMessage("没有代理商");
        }

        List<AgentGroupUser> agentGroupUsers = agentGroupUserLoaderClient.findByGroupIds(agentGroup.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toList())).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<AgentGroupUser> groupUsers = agentGroupUsers.stream().filter(a -> Objects.equals(a.getUserRoleId(), AgentRoleType.CityAgent.getId())).collect(Collectors.toList());
        Map<Long, AgentUser> users = agentUserLoaderClient.findByIds(groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
        return MapMessage.successMessage().add("result", users.values().stream().map(u -> MapUtils.m("id", u.getId(), "name", u.getRealName())).collect(Collectors.toList()));
    }

    /**
     * 根据学校ids 返回 学校和区域
     *
     * @return
     */
    @RequestMapping(value = "getSchoolByIds.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolByIds() {
        String schoolIds = getRequestString("schoolIds");// 学校Ids "," 号分割
        if (StringUtils.isBlank(schoolIds)) {
            return MapMessage.errorMessage("学校id不能为空");
        }
        try {
            List<Long> schoolIdList = splitStringToLong(schoolIds, "学校不能为空");
            List<Map> schools = new ArrayList<>();
            Map<Long, School> school = raikouSystem.loadSchools(schoolIdList);
            Map<Integer, List<School>> schoolMap = school.values().stream().collect(Collectors.groupingBy(School::getRegionCode));
            Map<String, List<ExRegion>> regionMap = raikouSystem.getRegionBuffer().loadRegions(schoolMap.keySet()).values().stream().collect(Collectors.groupingBy(r -> Pinyin4jUtils.getFirstCapital(r.getCountyName())));
            regionMap.forEach((k, v) -> {
                List<Map> areas = new ArrayList<>();
                v.forEach(r -> {
                    List<Map<String, Object>> schoolList = schoolMap.get(r.getCountyCode()).stream().map(s -> MapUtils.m("schoolName", s.getShortName(), "id", s.getId())).collect(Collectors.toList());
                    areas.add(MapUtils.m("areaName", r.getCountyName(), "count", schoolList.size(), "schools", schoolList));
                });
                schools.add(MapUtils.m("key", k, "areas", areas));
            });
            return MapMessage.successMessage().add("schools", schools);

        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 通过区域ids 返回区域
     *
     * @return
     */
    @RequestMapping(value = "getAreasByIds.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAreasByIds() {
        String areaIds = getRequestString("areaIds");// 区域Ids "," 号分割
        if (StringUtils.isBlank(areaIds)) {
            return MapMessage.errorMessage("区域id不能为空");
        }
        try {
            List<Integer> areaList = splitStringToInteger(areaIds, "区域不能为空");
            List<Map> areas = new ArrayList<>();
            Map<String, List<ExRegion>> regionMap = raikouSystem.getRegionBuffer().loadRegions(areaList).values().stream().collect(Collectors.groupingBy(r -> Pinyin4jUtils.getFirstCapital(r.getCityName())));
            regionMap.forEach((k, v) -> {
                Map<String, List<ExRegion>> cityMap = v.stream().collect(Collectors.groupingBy(ExRegion::getCityName));
                List<Map> cityList = new ArrayList<>();
                cityMap.forEach((k1, v1) ->
                        cityList.add(MapUtils.m("cityName", k1, "areas", v1.stream().map(r -> MapUtils.m("code", r.getCountyCode(), "name", r.getCountyName())).collect(Collectors.toList())))
                );
                areas.add(MapUtils.m("key", k, "cities", cityList));
            });
            return MapMessage.successMessage().add("areas", areas);

        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 根据区域ID 返回学校
     *
     * @return
     */
    @RequestMapping(value = "getSchoolsByAreaCode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolsByCountyCode() {
        Integer regionCode = getRequestInt("areaCode");   //区域
        if (regionCode == 0) {
            return MapMessage.errorMessage("根据区域ID不能为空");
        }
        // 这里只查询小学的学校
        Page<SchoolEsInfo> schoolEsInfoPage = searchService.searchJuniorSchoolInTargetRegions(Collections.singleton(regionCode), getCurrentUserId(), null, null, 0, 1000);
        List<Map> schools = new ArrayList<>();
        Map<String, List<Map<String, Object>>> schoolMap = schoolEsInfoPage.getContent().stream().map(s -> MapUtils.m("schoolName", s.getShortName(), "id", s.getId())).collect(Collectors.groupingBy(m -> Pinyin4jUtils.getFirstCapital((String) m.get("schoolName"))));
        schoolMap.forEach((k, v) ->
                schools.add(MapUtils.m("key", k, "list", v))
        );
        return MapMessage.successMessage().add("school", schools);
    }

    private ActivityConfigMapper mapperActivity(ActivityConfig activity) {
        ActivityConfigMapper mapper = new ActivityConfigMapper();
        Date now = new Date();
        mapper.setId(activity.getId());
        mapper.setTitle(activity.getTitle());
        mapper.setType(activity.getType() != null ? activity.getType().name() : "");
        mapper.setDescription(activity.getDescription());
        mapper.setStartTime(DateUtils.dateToString(activity.getStartTime(), DF));
        mapper.setEndTime(DateUtils.dateToString(activity.getEndTime(), DF));
        mapper.setReport(activity.getReport());
        mapper.setRules(activity.getRules());
        mapper.setActivityStatus(returnActivityStatus(activity));
        mapper.setStartDays(DateUtils.dayDiff(activity.getStartTime(), now));
        mapper.setEndDays(DateUtils.dayDiff(activity.getEndTime(), now) + 1);
        if (activity.getSchoolIds() != null && activity.getSchoolIds().size() == 1) {
            School school = raikouSystem.loadSchool(activity.getSchoolIds().get(0));
            mapper.setSchoolName(school != null ? school.getCname() : "");
        }
        if (activity.getClazzLevels() != null) {
            mapper.setClazzLevels(activity.getClazzLevels().stream().map(ClazzLevel::getDescription).collect(Collectors.toList()));
        }
        mapper.setSchoolIds(activity.getSchoolIds());
        mapper.setAreaIds(activity.getAreaIds());
        mapper.setStatus(activity.getStatus());
        return mapper;
    }

    private Integer returnActivityStatus(ActivityConfig activity) {
        Date now = new Date();
        if (activity.isStarting(now)) {
            return 1;
        } else if (activity.isUnStart(now)) {
            return 2;
        } else {
            return 3;
        }

    }

    private List<Map> activityMappers(List<ActivityConfig> activities) {
        if (CollectionUtils.isEmpty(activities)) {
            return Collections.emptyList();
        }
        List<ActivityConfigMapper> mappers = new ArrayList<>();
        Date now = new Date();
        activities.forEach(activity -> {
            ActivityConfigMapper mapper = new ActivityConfigMapper();
            mapper.setId(activity.getId());
            mapper.setTitle(activity.getTitle());
            mapper.setReport(activity.getReport());
            mapper.setStartTime(DateUtils.dateToString(activity.getStartTime(), DF));
            mapper.setEndTime(DateUtils.dateToString(activity.getEndTime(), DF));
            mapper.setCreateTime(DateUtils.dateToString(activity.getCreateTime(), DF2));
            if (activity.getClazzLevels() != null) {
                mapper.setClazzLevels(activity.getClazzLevels().stream().map(ClazzLevel::getDescription).collect(Collectors.toList()));
            }
            mapper.setActivityStatus(returnActivityStatus(activity));
            mapper.setStartDays(DateUtils.dayDiff(activity.getStartTime(), now));
            mapper.setEndDays(DateUtils.dayDiff(activity.getEndTime(), now) + 1);
            mapper.setStatus(activity.getStatus());
            mapper.setType(activity.getType().name());
            mappers.add(mapper);
        });
        LinkedHashMap<String, List<ActivityConfigMapper>> timeMappers = mappers.stream().collect(Collectors.groupingBy(ActivityConfigMapper::getCreateTime, LinkedHashMap::new, Collectors.toList()));
        List<Map> timeMappersList = new ArrayList<>();
        timeMappers.forEach((k, v) -> {
            timeMappersList.add(MapUtils.m("time", k, "activityList", v));
        });
        return timeMappersList;
    }

    private List<Long> splitStringToLong(String string, String desc) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(desc);
        }
        try {
            return Arrays.stream(string.split(",")).map(s -> Long.valueOf(s.trim())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException(desc);
        }
    }

    private List<Integer> splitStringToInteger(String string, String desc) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(desc);
        }
        try {
            return Arrays.stream(string.split(",")).map(s -> Integer.valueOf(s.trim())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException(desc);
        }
    }

    /**
     * 校验活动规则
     *
     * @return
     */
    private ActivityBaseRule validateRule(String rules) {
        ActivityBaseRule activityBaseRule = JsonUtils.fromJson(rules, ActivityBaseRule.class);
        if (activityBaseRule == null) {
            throw new IllegalArgumentException("请选择正确的活动规则");
        }
        if (activityBaseRule.getPlayLimit() == null) {
            throw new IllegalArgumentException("次数限制不可为空");
        }
        return activityBaseRule;
    }

    private List<ActivityConfig> getActivityConfig() {
        Long userId = getCurrentUserId();
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        if (roleType == AgentRoleType.CityManager) {
            List<AgentGroup> userGroups = baseOrgService.getUserGroups(userId);
            if (CollectionUtils.isNotEmpty(userGroups)) {
                userIds.addAll(baseOrgService.getGroupUsersByRole(userGroups.get(0).getId(), AgentRoleType.BusinessDeveloper));
            }
        }
        List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadByApplicants(userIds, ActivityConfig.ROLE_AGENT);
        activityConfigs.sort((o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? -1 : 1);
        return activityConfigs;
    }
}
