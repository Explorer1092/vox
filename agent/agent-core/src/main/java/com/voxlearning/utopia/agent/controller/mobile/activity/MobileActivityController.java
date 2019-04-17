package com.voxlearning.utopia.agent.controller.mobile.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.agent.constants.ActivityDataIndicator;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityExtend;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.service.activity.AgentActivityService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.view.activity.*;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceRecordDataView;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mobile/activity")
public class MobileActivityController extends AbstractAgentController {

    @Inject
    private AgentActivityService agentActivityService;
    @Inject
    private AgentGroupSupport agentGroupSupport;
    @Inject
    private AgentGroupUserLoaderClient groupUserLoaderClient;

    @RequestMapping("list.vpage")
    @ResponseBody
    public MapMessage getActivityList(){
        Date startDate = DateUtils.addMonths(new Date(), -6);
        List<ActivityView> activityList = agentActivityService.getActivityList(getCurrentUserId(), startDate);
        if(CollectionUtils.isNotEmpty(activityList)){
            activityList.sort((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()));
        }
        return MapMessage.successMessage().add("dataList", activityList);
    }

    // 获取二维码连接
    @RequestMapping("introduction_url.vpage")
    @ResponseBody
    public MapMessage introductionUrl(){
        String activityId = getRequestString("activityId");
        ActivityExtend extend = agentActivityService.getActivityExtend(activityId);
        String introductionUrl = "";
        if(extend != null && StringUtils.isNotBlank(extend.getIntroductionUrl())){
            introductionUrl = extend.getIntroductionUrl();
        }
        return MapMessage.successMessage().add("introductionUrl", introductionUrl);
    }

    // 获取海报
    @RequestMapping("poster.vpage")
    @ResponseBody
    public MapMessage poster(){
        String activityId = getRequestString("activityId");
        ActivityExtend extend = agentActivityService.getActivityExtend(activityId);
        String introductionUrl = "";
        int qrCodeX = 0;
        int qrCodeY = 0;
        List<String> posterUrls = new ArrayList<>();
        if(extend != null ){
            if(StringUtils.isNotBlank(extend.getIntroductionUrl())){
                introductionUrl = extend.getIntroductionUrl();
            }
            if(extend.getQrCodeX() != null && extend.getQrCodeY() != null){
                qrCodeX = extend.getQrCodeX();
                qrCodeY = extend.getQrCodeY();
            }
            if(CollectionUtils.isNotEmpty(extend.getPosterUrls())){
                posterUrls = extend.getPosterUrls();
            }
        }
        return MapMessage.successMessage()
                .add("introductionUrl", introductionUrl)
                .add("qrCodeX", qrCodeX)
                .add("qrCodeY", qrCodeY)
                .add("posterUrls", posterUrls)
                ;
    }

    @RequestMapping("slogan.vpage")
    @ResponseBody
    public MapMessage slogan(){
        String activityId = getRequestString("activityId");
        String slogan = "";
        List<String> materialUrls = new ArrayList<>();
        ActivityExtend extend = agentActivityService.getActivityExtend(activityId);
        if(extend != null){
            if(StringUtils.isNotBlank(extend.getSlogan())){
                slogan = extend.getSlogan();
            }
            if(CollectionUtils.isNotEmpty(extend.getMaterialUrls())){
                materialUrls = extend.getMaterialUrls();
            }
        }
        return MapMessage.successMessage().add("slogan", slogan).add("materialUrls", materialUrls);
    }

    // 获取数据明细的URL
    @RequestMapping("record_url.vpage")
    @ResponseBody
    public MapMessage recordUrl(){
        String activityId = getRequestString("activityId");
        ActivityExtend extend = agentActivityService.getActivityExtend(activityId);
        String recordUrl = "";
        if(extend != null && StringUtils.isNotBlank(extend.getRecordUrl())){
            recordUrl = extend.getRecordUrl();
        }
        return MapMessage.successMessage().add("recordUrl", recordUrl);
    }

//    @RequestMapping("add.vpage")
//    @ResponseBody
//    public MapMessage addActivity(){
//        String name = getRequestString("name");
//        Date startDate = getRequestDate("startDate");
//        Date endDate = getRequestDate("endDate");
//        if(endDate != null){
//            endDate = DateUtils.addSeconds(endDate, -1);
//        }
//        boolean isShow = getRequestBool("isShow");
//        return agentActivityService.addActivity(name, startDate, endDate, isShow);
//    }
//
//    @RequestMapping("update_extend.vpage")
//    @ResponseBody
//    public MapMessage updateExtend(){
//        String activityId = getRequestString("activityId");
//        if(agentActivityService.getActivity(activityId) == null){
//            return MapMessage.errorMessage("不存在该活动！");
//        }
//
//        String linkUrl = getRequestString("linkUrl");
//        String iconUrlsStr = getRequestString("iconUrls");
//        List<String> iconUrls = new ArrayList<>();
//        if(StringUtils.isNotBlank(iconUrlsStr)){
//            String[] iconUrlArr = iconUrlsStr.split(",");
//            for(String iconUrl : iconUrlArr){
//                if(StringUtils.isNotBlank(iconUrl)){
//                    iconUrls.add(iconUrl);
//                }
//            }
//        }
//
//        String introductionUrl = getRequestString("introductionUrl");
//        List<String> posterUrls = new ArrayList<>();
//        String slogan = getRequestString("slogan");
//        Integer form = getRequestInt("form", 1);
//        Integer meetConditionDays = getRequestInt("meetConditionDays", 1);
//        Boolean multipleOrderFlag = getRequestBool("multipleOrderFlag");
//        Boolean hasGift = getRequestBool("hasGift");
//
//        return agentActivityService.updateExtend(activityId, iconUrls, linkUrl, introductionUrl, posterUrls, slogan, form, meetConditionDays, multipleOrderFlag, hasGift);
//    }
//
//    @RequestMapping("update_indicator.vpage")
//    @ResponseBody
//    public MapMessage updateIndicator(){
//        String activityId = getRequestString("activityId");
//        if(agentActivityService.getActivity(activityId) == null){
//            return MapMessage.errorMessage("不存在该活动！");
//        }
//
//        String indicator = getRequestString("indicator");
//        String alias = getRequestString("alias");
//        int sortNo = getRequestInt("sortNo", 1);
//
//        ActivityDataIndicator indicatorType = ActivityDataIndicator.nameOf(indicator);
//        return agentActivityService.updateIndicator(activityId, indicatorType, alias, sortNo);
//    }
//
//    @RequestMapping("delete_indicator.vpage")
//    @ResponseBody
//    public MapMessage deleteIndicator(){
//        String indicatorId = getRequestString("indicatorId");
//        return agentActivityService.deleteIndicator(indicatorId);
//    }

    @RequestMapping("add_control.vpage")
    @ResponseBody
    public MapMessage addControl(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Integer roleId = getRequestInt("roleId");
        AgentRoleType role = AgentRoleType.of(roleId);
        if(role == null){
            return MapMessage.errorMessage("不存在该角色");
        }

        return agentActivityService.addControl(activityId, role.getId());
    }

    @RequestMapping("delete_control.vpage")
    @ResponseBody
    public MapMessage deleteControl(){
        String controlId = getRequestString("controlId");
        return agentActivityService.deleteControl(controlId);
    }


//    @RequestMapping("update.vpage")
//    @ResponseBody
//    public MapMessage update(){
//        String activityId = getRequestString("activityId");
//        if(agentActivityService.getActivity(activityId) == null){
//            return MapMessage.errorMessage("不存在该活动！");
//        }
//        String name = getRequestString("name");
//        Date startDate = getRequestDate("startDate");
//        Date endDate = getRequestDate("endDate");
//        if(endDate != null){
//            endDate = DateUtils.addSeconds(endDate, -1);
//        }
//        Boolean isShow = getRequestBool("isShow");
//        return agentActivityService.updateActivity(activityId, name, startDate, endDate, isShow);
//    }

    @RequestMapping("data_overview.vpage")
    @ResponseBody
    public MapMessage dataOverview(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        MapMessage message = MapMessage.successMessage();

        ActivityDataView dataView = agentActivityService.getOverview(activityId, getCurrentUserId());
        message.add("dataView", dataView);

        return message;
    }

    @RequestMapping("new_user_data.vpage")
    @ResponseBody
    public MapMessage getNewUserDate(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }
        MapMessage message = MapMessage.successMessage();
        Map<String, Object> newUserDataMap = agentActivityService.getNewUserData(activityId, getCurrentUserId());
        message.putAll(newUserDataMap);
        return message;
    }

    @RequestMapping("chart_info.vpage")
    @ResponseBody
    public MapMessage getChartInfo(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }
        Map<String, Integer> dataMap = agentActivityService.getChartInfo(activityId, getCurrentUserId());
        return MapMessage.successMessage().add("chartData", dataMap);
    }


    //排行榜
    @RequestMapping("ranking_list.vpage")
    @ResponseBody
    public MapMessage rankingList(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }
        Integer dateType = getRequestInt("dateType",1);  // 1 当日 2 累计
        Integer rankingType = getRequestInt("rankingType",1);  //1 专员 2 分区
        List<ActivityRankingView> dataList = agentActivityService.getRankingList(activityId,  dateType, rankingType, 10);
        if(rankingType == 1){  // 专员排行时，补充部门信息
            dataList.forEach(p -> {
                List<AgentGroup> groupList = baseOrgService.getUserGroups(p.getId());
                if(CollectionUtils.isNotEmpty(groupList)){
                    AgentGroup group = groupList.get(0);
                    p.setGroupId(group.getId());
                    p.setGroupName(group.getGroupName());
                }
            });
        }
        return MapMessage.successMessage().add("dataList", dataList);
    }

    @RequestMapping("statistics_data_list.vpage")
    @ResponseBody
    public MapMessage getDataList(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Long id = getRequestLong("id",0);
        Integer idType = getRequestInt("idType",1);
        Integer dimension = getRequestInt("dimension",1);

        int sumType = getRequestInt("sumType", 1);     // 1：合计  2： 人均

        boolean isMarketing = true;
        if(idType.equals(AgentConstants.INDICATOR_TYPE_GROUP)){
            GroupWithParent groupWithParent = agentGroupSupport.generateGroupWithParent(id);
            while(groupWithParent != null){
                if(StringUtils.contains(groupWithParent.getGroupName(), "一起教育科技")){
                    isMarketing = false;
                    break;
                }
                groupWithParent = groupWithParent.getParent();
            }
        }

        List<ActivityDataView> dataList = new ArrayList<>();
        if(isMarketing){
            dataList = agentActivityService.getStatisticsDataList(activityId, id, idType, dimension);
        }else {
            AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
            if(roleType == AgentRoleType.Country){
                dataList = agentActivityService.getOfficeDataList(activityId, id, idType, dimension);
            }else {
                List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(getCurrentUserId());
                if(CollectionUtils.isNotEmpty(groupUserList)){
                    Long targetGroupId = groupUserList.get(0).getGroupId();
                    dataList = agentActivityService.getOfficeDataList(activityId, targetGroupId, idType, dimension);
                }
            }
        }

        if(sumType == 1){
            return MapMessage.successMessage().add("dataList", dataList);
        }else if(sumType == 2){ // 人均的情况
            List<Map<String, Object>> avgDataList = agentActivityService.calAvgDataList(dataList);
            return MapMessage.successMessage().add("dataList", avgDataList);
        }
        return MapMessage.successMessage().add("dataList", dataList);
    }


    @RequestMapping("record_list.vpage")
    @ResponseBody
    public MapMessage recordList(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Long userId = getRequestLong("userId");
        if(userId < 1){
            userId = getCurrentUserId();
        }
        List<ActivityCouponOrderCourseView> recordList = agentActivityService.getRecordList(activityId, userId);
        Map<Integer, List<ActivityCouponOrderCourseView>> dayDataMap = recordList.stream().collect(Collectors.groupingBy(p -> SafeConverter.toInt(DateUtils.dateToString(p.getBusinessTime(), "yyyyMMdd"))));
        Map<String, List<ActivityCouponOrderCourseView>> dataMap = new LinkedHashMap<>();
        if(MapUtils.isNotEmpty(dayDataMap)) {
            List<Integer> dayList = dayDataMap.keySet().stream().sorted((o1, o2) -> (o2 - o1)).collect(Collectors.toList());
            for (Integer day : dayList) {
                String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyy-MM-dd");
                List<ActivityCouponOrderCourseView> dataList = dayDataMap.get(day);
                dataList.sort((o1, o2) -> o2.getBusinessTime().compareTo(o1.getBusinessTime()));
                dataMap.put(key, dataList);
            }
        }

        return MapMessage.successMessage().add("dataMap", dataMap);
    }


    @RequestMapping("record_dict_data.vpage")
    @ResponseBody
    public MapMessage recordDictData(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Long userId = getRequestLong("userId");
        if(userId < 1){
            userId = getCurrentUserId();
        }
        List<ActivitySchoolRecordView> recordList = agentActivityService.getDictSchoolStatisticsData(activityId, userId);

        return MapMessage.successMessage().add("dataList", recordList);
    }


    @RequestMapping("receive_gift.vpage")
    @ResponseBody
    public MapMessage receiveGift(){
        String orderId = getRequestString("orderId");
        String giftId = getRequestString("giftId");       // 暂时可为空
        int count = getRequestInt("count", 1);
        if(StringUtils.isBlank(orderId)){
            return MapMessage.errorMessage("订单号为空");
        }
        return agentActivityService.receiveGift(orderId, giftId, count);
    }


    @RequestMapping("refresh_attend_course_statistics.vpage")
    @ResponseBody
    public MapMessage refreshAttendCourseStatistics(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Set<Long> userIds = requestLongSet("userIds");
        if(CollectionUtils.isEmpty(userIds)){
            List<AgentUser> userList = baseOrgService.findAllAgentUsers();
            userIds = userList.stream().map(AgentUser::getId).collect(Collectors.toSet());
        }
        return agentActivityService.refreshAttendCourseStatistics(activityId, userIds);
    }


    @RequestMapping("get_target_user_coupon_data.vpage")
    @ResponseBody
    public MapMessage getTargetUserCouponData(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        List<AgentGroupUser> groupUserList = groupUserLoaderClient.findAll();
        Map<Long, AgentRoleType> userRoleMap = groupUserList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, AgentGroupUser::getUserRoleType, (o1, o2) -> o1));

        List<ActivityCouponStatisticsView> dataList = agentActivityService.getCouponStatisticsDataList(activityId, userRoleMap.keySet());

        List<Map<String, Object>> resultList = dataList.stream().map(p -> {
            Map<String, Object> map = BeanMapUtils.tansBean2Map(p);
            map.put("role", userRoleMap.get(p.getId()));
            return map;
        }).collect(Collectors.toList());

        return MapMessage.successMessage().add("dataList", resultList);
    }


}
