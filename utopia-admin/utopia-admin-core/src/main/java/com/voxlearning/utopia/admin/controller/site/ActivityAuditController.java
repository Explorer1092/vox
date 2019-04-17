package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@Slf4j
@RequestMapping(value = "/site/activity")
public class ActivityAuditController extends AbstractAdminSystemController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;

    @RequestMapping("/audit/list.vpage")
    public String activityConfigs(Model model) {
        int status = getRequestInt("status", 1);
        int page = getRequestInt("page", 0);
        String type = getRequestString("type");
        int clazzLevel = getRequestInt("clazzLevel");
        String name = getRequestString("name");
        Page<ActivityConfig> configPage = activityConfigServiceClient.getActivityConfigService().query(status, type, clazzLevel, name, ActivityConfig.ROLE_AGENT, page, 10);
        model.addAttribute("activity", activityWrappers(configPage.getContent()));
        model.addAttribute("currentPage", configPage.getNumber());
        model.addAttribute("totalPage", configPage.getTotalPages());
        model.addAttribute("hasPrev", configPage.hasPrevious());
        model.addAttribute("hasNext", configPage.hasNext());

        // 搜索条件返回
        model.addAttribute("status", status);
        model.addAttribute("name", name);
        model.addAttribute("type", type);
        model.addAttribute("clazzLevel", clazzLevel);
        model.addAttribute("types", Arrays.stream(ActivityTypeEnum.values()).map(a -> MapUtils.m("type", a.name(), "name", a.getName())).collect(Collectors.toList()));
        return "/site/activity/auditlist";
    }

    @RequestMapping("/data/list.vpage")
    public String activityData(Model model) {
        // 往使用model.addAttribute往模板中插入具体的值，先解析链接参数start(开始时间)、end(结束时间)
        String startTime = getRequestString("start");
        Date startDate = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATE);
        String endTime = getRequestString("end");
        Date endDate = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATE);
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            model.addAttribute("startTime", startTime);
            model.addAttribute("endTime", endTime);
            List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadAllActivityConfigIncludeIsEnd();
            activityConfigs = activityConfigs.stream().filter(o -> o.getStartTime().after(startDate) && o.getStartTime().before(endDate)).collect(Collectors.toList());
            Map<ActivityTypeEnum, List<ActivityConfig>> typeToActivityConfigs = activityConfigs.stream().collect(Collectors.groupingBy(ActivityConfig::getType));
            //汇总参与总人数，按类型
            Iterator<ActivityTypeEnum> it = typeToActivityConfigs.keySet().iterator();
            Integer sumTotal = 0;
            while (it.hasNext()) {
                ActivityTypeEnum type = it.next();
                List<ActivityConfig> typeActivityConfigs = typeToActivityConfigs.get(type);
                Integer sumTypeTotal = 0;
                for (ActivityConfig temp : typeActivityConfigs) {
                    if (Objects.nonNull(temp.getReport())) {
                        if (Objects.nonNull(temp.getReport().getTotal())) {
                            sumTypeTotal += temp.getReport().getTotal();
                        }
                    }
                }
                sumTotal += sumTypeTotal;
                model.addAttribute(type.name(), sumTypeTotal);
            }
            model.addAttribute("activityTotal", sumTotal);
        }
        return "/site/activity/datalist";
    }

    @RequestMapping(value = "/audit/load.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage load() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
        if (activityConfig == null) {
            return MapMessage.errorMessage("游戏申请不存在");
        }
        return MapMessage.successMessage().add("activity", activityWrapper(activityConfig));
    }

    @RequestMapping(value = "/audit/agree.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage agree() {
        String id = getRequestString("id");
        String subjects = getRequestString("subjects");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        Set<Subject> subjectHashSet = new HashSet<>();
        if (StringUtils.isEmpty(subjects)) {
            subjectHashSet.add(Subject.UNKNOWN);
        } else {
            subjectHashSet.add(Subject.safeParse(subjects));
        }
        ActivityConfig config = activityConfigServiceClient.getActivityConfigService().load(id);
        if (config == null) {
            return MapMessage.errorMessage("活动不存在");
        }
        if (config.getStatus() != null && config.getStatus() != 1) {
            return MapMessage.errorMessage("该活动不允许通过");
        }
        if (!activityConfigServiceClient.getActivityConfigService().agree(id, getCurrentAdminUser().getAdminUserName(), subjectHashSet)) {
            return MapMessage.errorMessage("失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/audit/reject.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage reject() {
        String id = getRequestString("id");
        String rejectReason = getRequestString("rejectReason");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        ActivityConfig config = activityConfigServiceClient.getActivityConfigService().load(id);
        if (config == null) {
            return MapMessage.errorMessage("活动不存在");
        }
        if (config.getStatus() != null && config.getStatus() != 1) {
            return MapMessage.errorMessage("该活动不允许驳回");
        }
        if (!activityConfigServiceClient.getActivityConfigService().reject(id, getCurrentAdminUser().getAdminUserName(), rejectReason)) {
            return MapMessage.errorMessage("失败");
        }
        return MapMessage.successMessage();
    }

    private List<Map> activityWrappers(List<ActivityConfig> configs) {
        Map<Long, AgentUser> applicants = agentUserLoaderClient.findByIds(configs.stream().map(ActivityConfig::getApplicant).collect(Collectors.toSet()));
        List<Map> maps = new ArrayList<>();
        configs.forEach(c -> {
            Map<String, Object> map = new HashMap<>();
            // XXX（市场专员/经理名称）的XX（游戏名称）游戏申请
            AgentUser agentUser = applicants.get(c.getApplicant());
            map.put("title", String.format("%s的%s游戏申请", agentUser != null ? agentUser.getRealName() : "", c.getType().getName()));
            map.put("applicantTime", c.getCreateTime());
            map.put("name", c.getTitle());
            map.put("startTime", c.getStartTime());
            map.put("endTime", c.getEndTime());
            map.put("status", c.getStatus());
            map.put("statusDesc", returnStatus(c.getStatus()));
            map.put("applicant", agentUser != null ? agentUser.getRealName() : "");
            map.put("id", c.getId());
            map.put("clazzLevel", StringUtils.join(c.getClazzLevels().stream().map(level -> ClazzLevel.parse(level).getDescription()).collect(Collectors.toList()), "、"));
            if (Objects.nonNull(c.getReport())) {
                map.put("total", c.getReport().getTotal() != null ? c.getReport().getTotal() : 0);
            } else {
                map.put("total", 0);
            }
            maps.add(map);
        });
        return maps;
    }

    private Map activityWrapper(ActivityConfig activityConfig) {
        AgentUser agentUser = agentUserLoaderClient.load(activityConfig.getApplicant());

        return MapUtils.m(
                "id", activityConfig.getId(),
                "statusDesc", returnStatus(activityConfig.getStatus()),
                "status", activityConfig.getStatus(),
                "name", activityConfig.getTitle(),
                "applicant", agentUser != null ? agentUser.getRealName() : "",
                "applicantTime", DateUtils.dateToString(activityConfig.getCreateTime(), "yyyy-MM-dd"),
                "time", DateUtils.dateToString(activityConfig.getStartTime(), "yyyy-MM-dd") + "~" + DateUtils.dateToString(activityConfig.getEndTime(), "yyyy-MM-dd"),
                "clazzLevel", StringUtils.join(activityConfig.getClazzLevels().stream().map(level -> ClazzLevel.parse(level).getDescription()).collect(Collectors.toList()), "、"),
                "desc", activityConfig.getDescription(),
                "rules", returnRules(activityConfig.getRules()),
                "areaSchools", getSchools(activityConfig.getSchoolIds()),
                "areas", getAreas(activityConfig.getAreaIds()),
                "proveImg", activityConfig.getProveImg(),
                "subjects", activityConfig.fetchSubjectDesc()
        );
    }

    private String returnStatus(Integer status) {
        if (status == null) {
            return "";
        }
        return status == 1 ? "未审核" : (status == 2 ? "已通过" : "驳回");
    }

    private String returnRules(ActivityBaseRule rules) {
        if (rules == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (rules.getPattern() != null) {
            builder.append(rules.getPattern().getName()).append("、");
        }
        if (rules.getLimitAmount() != null) {
            builder.append(rules.getLimitAmount()).append("道题、");
        }
        if (rules.getLimitTime() != null) {
            builder.append(rules.getLimitTime()).append("分钟、");
        }
        if (rules.getLevel() != null) {
            builder.append(rules.getLevel().getName()).append("、");
        }
        if (rules.getExtent() != null) {
            builder.append(rules.getExtent().getName()).append("、");
        }
        if (rules.getPlayLimit() != null) {
            builder.append(rules.getPlayLimit()).append("次、");
        }
        String ruleStr = builder.toString();
        if (ruleStr.endsWith("、")) {
            ruleStr = ruleStr.substring(0, ruleStr.length() - 1);
        }
        return ruleStr;
    }

    private List<Map> getSchools(Collection<Long> schoolIds) {
        Map<Long, School> school = raikouSystem.loadSchools(schoolIds);
        Map<Integer, List<School>> schoolMap = school.values().stream().collect(Collectors.groupingBy(School::getRegionCode));
        Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions(schoolMap.keySet());
        List<Map> areaSchools = new ArrayList<>();
        regionMap.forEach((k, v) -> {
            List<School> schools = schoolMap.get(k);
            areaSchools.add(MapUtils.m("areaName", v.getCountyName(), "count", schools.size(), "schools", schools.stream().map(School::getCname).collect(Collectors.toList())));
        });
        return areaSchools;
    }

    private List getAreas(List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Map<Integer, ExRegion> areaMap = raikouSystem.getRegionBuffer().loadRegions(areaIds.stream().map(SafeConverter::toInt).collect(Collectors.toList()));
        Map<String, List<ExRegion>> areas = areaMap.values().stream().collect(Collectors.groupingBy(ExRegion::getCityName));
        List<Map> cityAreas = new ArrayList<>();
        areas.forEach((k, v) ->
                cityAreas.add((MapUtils.m("cityName", k, "areas", v.stream().map(ExRegion::getCountyName).collect(Collectors.toList()))))
        );
        return cityAreas;
    }
}
