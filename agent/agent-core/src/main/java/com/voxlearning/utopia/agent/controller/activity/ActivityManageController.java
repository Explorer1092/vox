package com.voxlearning.utopia.agent.controller.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.ActivityDataCategory;
import com.voxlearning.utopia.agent.constants.ActivityDataIndicator;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityExtend;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityIndicatorConfig;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.service.activity.AgentActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/activity/manage")
public class ActivityManageController extends AbstractAgentController {

    @Inject
    private AgentActivityService agentActivityService;

    /***
     * 活动列表
     * @return
     */
    @RequestMapping("activity_list.vpage")
    public String activityList(){
        return "activity/activity_list";
    }
    @RequestMapping("list.vpage")
    @ResponseBody
    public MapMessage list(){

        Date startDate = DateUtils.addMonths(new Date(), -6);
        List<AgentActivity> activityList = agentActivityService.getActivityList(startDate);
        return MapMessage.successMessage().add("dataList",activityList);
    }

    @RequestMapping("add_page.vpage")
    public String addPage(){
        return "activity/activity_add";
    }

    @RequestMapping("add.vpage")
    @ResponseBody
    public MapMessage addActivity(){
        String name = getRequestString("name");
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        if(endDate != null){
            endDate = DateUtils.addSeconds(endDate, -1);
        }
        String originalPrice = getRequestString("originalPrice");
        String presentPrice = getRequestString("presentPrice");

        return agentActivityService.addActivity(name, startDate, endDate, originalPrice, presentPrice);
    }

    @RequestMapping("update_page.vpage")
    public String updatePage(Model model){
        String activityId = getRequestString("activityId");                             // 活动ID

        AgentActivity agentActivity = agentActivityService.getActivity(activityId);

        if(agentActivity != null && agentActivity.getEndDate() != null){
            agentActivity.setEndDate(DateUtils.addSeconds(agentActivity.getEndDate(), 1));
        }
        model.addAttribute("activityId", activityId);
        model.addAttribute("activity", agentActivity);
        return "activity/activity_edit";
    }

    @RequestMapping("update.vpage")
    @ResponseBody
    public MapMessage update(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }
        String name = getRequestString("name");
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        if(endDate != null){
            endDate = DateUtils.addSeconds(endDate, -1);
        }
        String originalPrice = getRequestString("originalPrice");
        String presentPrice = getRequestString("presentPrice");
        return agentActivityService.updateActivity(activityId, name, startDate, endDate, originalPrice, presentPrice);
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete(){
        String activityId = getRequestString("activityId");
        return agentActivityService.deleteActivity(activityId);
    }


    /**
     * 配置扩展项
     * @return MapMessage
     */
    @RequestMapping("update_extend_page.vpage")
    public String extendPage(Model model){
        String activityId = getRequestString("activityId");                             // 活动ID
        ActivityExtend extend = agentActivityService.getActivityExtend(activityId);

        model.addAttribute("activityId", activityId);
        model.addAttribute("extend", extend);
        return "activity/activity_extend";
    }

    /**
     * 配置扩展项
     * @return MapMessage
     */
    @RequestMapping(value = "update_extend.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateExtend(){
        String activityId = getRequestString("activityId");                             // 活动ID
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        String linkUrl = getRequestString("linkUrl");                                  // 活动详情对应的前端连接

        List<String> iconUrls = requestStringList("iconUrls");                         // 活动图标

        String introductionUrl = getRequestString("introductionUrl");                  // 课程介绍及二维码对应的长连接
        String recordUrl = getRequestString("recordUrl");                  // 课程介绍及二维码对应的长连接
        Integer qrCodeX = getRequestInt("qrCodeX");                                  // 二维码位置 x轴
        Integer qrCodeY = getRequestInt("qrCodeY");                                  // 二维码位置 y轴
        List<String> posterUrls = requestStringList("posterUrls");                     // 活动海报
        String slogan = getRequestString("slogan");                                    // 推广文案
        List<String> materialUrls = requestStringList("materialUrls");                 // 推广素材

        Integer form = getRequestInt("form", 1);                                // 活动形式  1：普通推广    2：链式推广    3: 组团  4： 礼品卡
        Integer meetConditionDays = getRequestInt("meetConditionDays", 1);      // 需要参加的课程天数
        Boolean multipleOrderFlag = getRequestBool("multipleOrderFlag");             // 是否允许下多个订单
        Boolean hasGift = getRequestBool("hasGift");                                 // 是否会给下单用户赠送礼品

        return agentActivityService.updateExtend(activityId, iconUrls, linkUrl, introductionUrl, recordUrl, qrCodeX, qrCodeY, posterUrls, slogan, materialUrls, form, meetConditionDays, multipleOrderFlag, hasGift);
    }

    @RequestMapping("indicator_page.vpage")
    public String indicatorPage(Model model){
        String activityId = getRequestString("activityId");                             // 活动ID

        List<ActivityDataIndicator> indicatorList = Arrays.asList(ActivityDataIndicator.values());

        List<ActivityIndicatorConfig> indicatorConfigList = agentActivityService.getIndicatorList(activityId);
        Map<ActivityDataIndicator, ActivityIndicatorConfig> indicatorConfigMap = indicatorConfigList.stream().collect(Collectors.toMap(ActivityIndicatorConfig::getIndicator, Function.identity(), (o1, o2) -> o1));

        List<Map<String, Object>> dataList = new ArrayList<>();
        indicatorList.forEach(p -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("indicator", p.name());
            dataMap.put("indicatorName", p.getDesc());

            ActivityIndicatorConfig config = indicatorConfigMap.get(p);
            if(config != null){
                dataMap.put("alias", config.getAlias());
                dataMap.put("sortNo", config.getSortNo());
                dataMap.put("selected", true);
            }else {
                dataMap.put("alias", "");
                dataMap.put("sortNo", "");
                dataMap.put("selected", false);
            }
            dataList.add(dataMap);
        });

        model.addAttribute("activityId", activityId);
        model.addAttribute("indicatorList", dataList);

        return "activity/activity_indicators";
    }


    @RequestMapping(value = "save_indicators.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveIndicators(){
        String activityId = getRequestString("activityId");                             // 活动ID
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        List<ActivityIndicatorConfig> indicatorConfigList = agentActivityService.getIndicatorList(activityId);
        Map<ActivityDataIndicator, String> indicatorIdMap = indicatorConfigList.stream().collect(Collectors.toMap(ActivityIndicatorConfig::getIndicator, ActivityIndicatorConfig::getId, (o1, o2)-> o2));

        String jsonData = getRequestString("indicators");
        List<Map> indicatorList = JsonUtils.fromJsonToList(jsonData, Map.class);

        List<ActivityDataIndicator> selectedIndicators = new ArrayList<>();
        for(Map data : indicatorList){

            ActivityDataIndicator indicator = ActivityDataIndicator.nameOf(SafeConverter.toString(data.get("indicator")));
            if(indicator == null){
                continue;
            }

            String alias = SafeConverter.toString(data.get("alias"));
            int sortNo = SafeConverter.toInt(data.get("sortNo"));
            agentActivityService.updateIndicator(activityId, indicator, alias, sortNo);

            selectedIndicators.add(indicator);
        }

        indicatorIdMap.forEach((k, v) -> {
            if(!selectedIndicators.contains(k)){
                agentActivityService.deleteIndicator(v);
            }
        });

        return MapMessage.successMessage();
    }


    @RequestMapping(value = "on_off_line.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage onOffline(){
        String activityId = getRequestString("activityId");                             // 活动ID
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        boolean isShow = getRequestBool("isShow");
        return agentActivityService.updateShowData(activityId, isShow);
    }

}
