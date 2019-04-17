package com.voxlearning.utopia.admin.controller.abtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.athena.SearchEngineServiceClient;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.config.api.entity.AbtestExperiment;
import com.voxlearning.utopia.service.config.api.entity.AbtestGroup;
import com.voxlearning.utopia.service.config.api.entity.AbtestPlan;
import com.voxlearning.utopia.service.config.consumer.AbtestExperimentLoaderClient;
import com.voxlearning.utopia.service.config.consumer.AbtestServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * @author qianlong.yang
 * @version 0.1
 * @since 2016/10/25
 */
@Controller
@RequestMapping("/abtest")
@Slf4j
public class AbtestAdminController extends AbstractAdminSystemController {

    @Inject private SearchEngineServiceClient searchEngineServiceClient; // 大数据标签接口...

    @Inject
    private AbtestServiceClient abtestServiceClient;

    @Inject
    AbtestExperimentLoaderClient abtestExperimentLoaderClient;

    // index page
    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index() {
        return "abtest/index";
    }

    // experiments page
    @RequestMapping(value = "/experiments.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String experiments() {
        return "abtest/experiments";
    }

    // load paged experiments
    @RequestMapping(value = "/upsertexperiment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertexperiment() {
        String experimentStr = getRequestString("experiment");
        Map<String, Object> experiment = JsonUtils.fromJson(experimentStr);
        // 默认的过期时间是30天
        String expireDatetimeStr = SafeConverter.toString(experiment.get("expireDatetime"));
        Date expireDatetime = DateUtils.stringToDate(expireDatetimeStr);
        if (expireDatetime == null) {
            expireDatetime = DateUtils.nextDay(new Date(), 30);
        }
        experiment.put("expireDatetime", expireDatetime);
        // 分流截止时间，默认无限1000天吧
        String shardEndTimeStr = SafeConverter.toString(experiment.get("shardEndTime"));
        Date shardEndTime;
        if (shardEndTimeStr == null) {
            shardEndTime = DateUtils.nextDay(new Date(), 1000);
        } else {
            shardEndTime = DateUtils.stringToDate(shardEndTimeStr);
            if (shardEndTime == null) {
                shardEndTime = DateUtils.nextDay(new Date(), 1000);
            }
        }
        experiment.put("shardEndTime", shardEndTime);

        // format tagsFilters，如果没有就是空的list,如果有
        List<List<Long>> tagsFilters;
        if (experiment.containsKey("tagsFilters")) {
            tagsFilters = (List<List<Long>>) experiment.get("tagsFilters");
        } else {
            tagsFilters = new ArrayList<>();
        }
        if (tagsFilters == null) {
            tagsFilters = new ArrayList<>();
        }
        experiment.put("tagsFilters", tagsFilters);
        AbtestExperiment abtestExperiment = AbtestExperiment.init(experiment);
        MapMessage mapMessage = abtestServiceClient.upsertExperiment(abtestExperiment);
        return mapMessage;
    }

    // load paged experiments
    @RequestMapping(value = "/loadcurrentpageexperiments.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadcurrentpageexperiments() {
        int page = getRequestInt("currentPage");
        page = page - 1;
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = new PageRequest(page, 10, Sort.Direction.DESC, "updateDatetime");
        boolean disabled = false;
        Page<AbtestExperiment> abtestExperiments = abtestExperimentLoaderClient.loadNextPageExperiments(pageable, disabled);
        return MapMessage.successMessage().add("experiments", abtestExperiments);
    }

    // 实验组管理
    @RequestMapping(value = "/groups.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String groups(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        List<AbtestPlan> abtestPlanList = abtestExperimentLoaderClient.loadPlansFromDao(id);
        model.addAttribute("plans", JsonUtils.toJson(abtestPlanList));
        return "abtest/groups";
    }

    // upsert 实验组
    @RequestMapping(value = "/upsertgroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertgroup() {
        String groupStr = getRequestString("group");
        Map<String, Object> group = JsonUtils.fromJson(groupStr);
        AbtestGroup abtestGroup = AbtestGroup.init(group);
        MapMessage mapMessage = abtestServiceClient.upsertGroup(abtestGroup);
        return mapMessage;
    }

    // load groups
    @RequestMapping(value = "/loadgroups.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadgroups() {
        String experimentId = getRequestString("experimentId");
        List<AbtestGroup> abtestGroupList = abtestExperimentLoaderClient.loadGroupsFromDao(experimentId);
        return MapMessage.successMessage().add("groups", abtestGroupList);
    }

    // 方案配置
    @RequestMapping(value = "/plans.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String addplan(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "abtest/plans";
    }

    // upsert 实验组
    @RequestMapping(value = "/upsertplan.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertplan() {
        String planStr = getRequestString("plan");
        Map<String, Object> plan = JsonUtils.fromJson(planStr);
        AbtestPlan abtestPlan = AbtestPlan.init(plan);
        MapMessage mapMessage = abtestServiceClient.upsertPlan(abtestPlan);
        return mapMessage;
    }

    // set default plan 设置默认方案
    @RequestMapping(value = "/setdefaultplan.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setdefaultplan() {
        String planId = getRequestString("planId");
        String experimentId = getRequestString("experimentId");
        MapMessage mapMessage = abtestServiceClient.setDefaultPlan(experimentId, planId);
        return mapMessage;
    }


    // load groups
    @RequestMapping(value = "/loadplans.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadplans() {
        String experimentId = getRequestString("experimentId");
        List<AbtestPlan> abtestGroupList = abtestExperimentLoaderClient.loadPlansFromDao(experimentId);
        return MapMessage.successMessage().add("plans", abtestGroupList);
    }

    // choose tag，选择标签
    @RequestMapping(value = "/choosetag.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String choosetag(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        AbtestExperiment abtestExperiment = abtestExperimentLoaderClient.loadAbtestExperimentById(id);
        List<List<Long>> tagsFilters = abtestExperiment.getTagsFilters();
        if (tagsFilters == null) {
            tagsFilters = Collections.emptyList();
        }
        model.addAttribute("tagsFilters", JsonUtils.toJson(tagsFilters));
        model.addAttribute("labelTree", JsonUtils.toJson(searchEngineServiceClient.getLabelTree()));
        return "abtest/choosetag";
    }

    // save tags 设置实验组标签
    @RequestMapping(value = "/savetags.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savetags() throws IOException {
        String id = getRequestString("id");
        String tagsFiltersStr = getRequestString("tagsFilters");
        ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
        JsonNode rootNode = mapper.readTree(tagsFiltersStr);
        CollectionType LongListType = mapper.getTypeFactory().constructCollectionType(List.class, Long.class);
        List<List<Long>> tagsFilters = new ArrayList<>();
        for (JsonNode childNode : rootNode) {
            mapper.readValue(childNode.traverse(), LongListType);
            tagsFilters.add(mapper.readValue(childNode.traverse(), LongListType));
        }
        AbtestExperiment abtestExperiment = abtestExperimentLoaderClient.loadAbtestExperimentById(id);
        if (abtestExperiment == null) {
            return MapMessage.errorMessage("实验不存在");
        } else {
            abtestExperiment.setTagsFilters(tagsFilters);
            MapMessage mapMessage = abtestServiceClient.upsertExperiment(abtestExperiment);
            return mapMessage;
        }
    }

    // set school，按照学校过滤
    @RequestMapping(value = "/setschoolfilter.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String setSchoolFilter(Model model) {
        String id = getRequestString("id");
        AbtestExperiment abtestExperiment = abtestExperimentLoaderClient.loadAbtestExperimentById(id);
        String choosedSchoolIds = abtestExperiment.getSchoolIds();
        if (choosedSchoolIds == null) {
            choosedSchoolIds = "";
        }
        model.addAttribute("id", id);
        model.addAttribute("choosedSchoolIds", choosedSchoolIds);
        return "abtest/setschoolfilter";
    }

    // save school ids，设置学校过滤条件
    @RequestMapping(value = "/saveschoolids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveschoolids() {
        String id = getRequestString("id");
        String schoolIds = getRequestString("schoolIds");
        AbtestExperiment abtestExperiment = abtestExperimentLoaderClient.loadAbtestExperimentById(id);
        if (abtestExperiment == null) {
            return MapMessage.errorMessage("实验不存在");
        } else {
            abtestExperiment.setSchoolIds(schoolIds);
        }
        MapMessage mapMessage = abtestServiceClient.upsertExperiment(abtestExperiment);
        return mapMessage;
    }

    // showusertagstree，展示用户标签树
    @RequestMapping(value = "/showusertagstree.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String showusertagstree(Model model) {
        model.addAttribute("labelTree", JsonUtils.toJson(searchEngineServiceClient.getLabelTree()));
        return "abtest/showusertagstree";
    }


    @RequestMapping(value = "getusertagset.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getusertagset() {
        long userId = getRequestLong("userId");
        Set<String> userLabelSet = searchEngineServiceClient.getUserLabelSet(userId);
        return MapMessage.successMessage().add("labelSet", userLabelSet);
    }
}
