package com.voxlearning.utopia.agent.controller.mobile.activity;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.activity.AgentUserLiveOrderService;
import com.voxlearning.utopia.agent.service.activity.LiveEnrollmentPositiveService;
import com.voxlearning.utopia.agent.service.activity.LiveEnrollmentService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.view.activity.LiveEnrollmentOrderView;
import com.voxlearning.utopia.agent.view.activity.LiveEnrollmentSchoolView;
import com.voxlearning.utopia.agent.view.activity.LiveEnrollmentView;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 直播平台市场招生活动
 *
 * @author song.wang
 * @date 2018/12/17
 */

@Controller
@RequestMapping("/mobile/live/enrollment")
public class LiveEnrollmentController extends AbstractAgentController {

    @Inject
    private LiveEnrollmentService liveEnrollmentService;
    @Inject
    private AgentRequestSupport agentRequestSupport;
    @Inject
    private AgentUserLiveOrderService agentUserLiveOrderService;
    @Inject
    private LiveEnrollmentPositiveService liveEnrollmentPositiveService;
    // 参加的活动列表
    @RequestMapping(value = "gift_record_url.vpage")
    @ResponseBody
    public MapMessage getUrl(){

        final String secretKey;
        final String domain;
        if(RuntimeMode.lt(Mode.STAGING)){
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY_TEST;
            domain = "http://17xue-student.test.17zuoye.net";
        }else {
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY;
            if(RuntimeMode.current() == Mode.STAGING){
                domain = "http://17xue-student.staging.17zuoye.net";
            }else {
                domain = "http://www.17xueba.com";
            }
        }

        Long userId = getCurrentUserId();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("uid", String.valueOf(userId));
        String sig = AgentApiAuth.generateAppKeySig(paramMap, secretKey);
        return MapMessage.successMessage().add("gift_record_url", domain + "/phecda/activity/index.vpage?" + "uid=" + userId + "&sig=" + sig + "&app_key=" + AgentApiAuth.APP_KEY);
    }


    // 参加的活动列表
    @RequestMapping(value = "activity_list.vpage")
    @ResponseBody
    public MapMessage activityList(){

        Long userId = getCurrentUserId();
        List<LiveEnrollmentView> viewList = liveEnrollmentService.getActivityList(userId, null, null);
        List<Map<String,Object>> result = new ArrayList<>();
        Boolean isSignIn = false;
        if(CollectionUtils.isNotEmpty(viewList)){
            Map<String,List<LiveEnrollmentView>> dayListMap = viewList.stream().collect(Collectors.groupingBy(p -> DateUtils.dateToString(p.getWorkTime(),DateUtils.FORMAT_SQL_DATE)));
            dayListMap.forEach((k, v) -> {
                Map<String,Object> itemMap = new HashMap<>();
                itemMap.put("day",k);
                itemMap.put("items",v);
                result.add(itemMap);
            });
            DayRange dayRange = DayRange.current();
            isSignIn = viewList.stream().anyMatch(p -> Objects.equals(p.getUserId(), userId) && dayRange.contains(p.getWorkTime()));
        }
        result.sort((p1, p2) -> SafeConverter.toString(p2.get("day")).compareTo(SafeConverter.toString(p1.get("day"))));
        return MapMessage.successMessage().add("dataList",result).add("isSignIn", isSignIn);
    }

    @RequestMapping(value = "del_activity.vpage")
    @ResponseBody
    public MapMessage delSign(){
        String id = getRequestString("id");
        if(StringUtils.isBlank(id)){
            return MapMessage.errorMessage("请选择id");
        }
        return liveEnrollmentService.deleteActivity(id);
    }

    @RequestMapping(value = "join_activity.vpage")
    @ResponseBody
    public MapMessage joinActivity(){
        Long schoolId = getRequestLong("schoolId");
        String signInId = getRequestString("signInId");
        AuthCurrentUser user = getCurrentUser();
        return liveEnrollmentService.joinActivity(schoolId, signInId, user.getUserId(), user.getRealName());
    }


    @RequestMapping(value = "search_school.vpage")
    @ResponseBody
    public MapMessage searchSchoolList(HttpServletRequest request) {
        String searchKey = getRequestString("searchKey");

        Integer pageNo = getRequestInt("pageNo");       //第几页
        Integer pageSize = getRequestInt("pageSize");   //每页数量

        String longitude = getRequestString("longitude");   //经度
        String latitude = getRequestString("latitude");     //纬度

        Double targetLongitude = null;
        Double targetLatitude = null;
        if(StringUtils.isNotBlank(longitude) && StringUtils.isNotBlank(latitude)){
            //坐标转化
            String coordinateType;
            if (agentRequestSupport.isIOSRequest(request)) {
                coordinateType = "wgs84ll";//getRequestString("coordinateType");
            } else {
                coordinateType = "autonavi";
            }
            MapMessage address = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
            if (!address.isSuccess()) {
                return address;
            }

            Double tempLatitude = SafeConverter.toDouble(address.get("latitude"), 0d);
            Double tempLongitude = SafeConverter.toDouble(address.get("longitude"), 0d);
            if(tempLatitude != 0d && tempLongitude != 0d){
                targetLatitude = tempLatitude;
                targetLongitude = tempLongitude;
            }
        }

        Long userId = getCurrentUserId();
        List<LiveEnrollmentSchoolView> schoolList = liveEnrollmentService.searchSchool(userId, searchKey, targetLongitude, targetLatitude, pageNo, pageSize);
        return MapMessage.successMessage().add("schoolList", schoolList);
    }


    @RequestMapping("order_list.vpage")
    @ResponseBody
    public MapMessage orderList(){
        Long userId = getRequestLong("userId");
        if(userId  == 0){
            userId = getCurrentUserId();
        }
        List<LiveEnrollmentOrderView> orderList = liveEnrollmentService.getActivityOrderList(userId, null, null);
        Map<Integer, List<LiveEnrollmentOrderView>> dayOrderListMap = orderList.stream().collect(Collectors.groupingBy(p -> SafeConverter.toInt(DateUtils.dateToString(p.getOrderTime(),"yyyyMMdd"))));
        Map<String, List<LiveEnrollmentOrderView>> dataMap = new LinkedHashMap<>();
        List<Integer> sortedDay = dayOrderListMap.keySet().stream().sorted((o1, o2) -> o2 - o1).collect(Collectors.toList());
        for(Integer day : sortedDay){
            List<LiveEnrollmentOrderView> list = dayOrderListMap.get(day).stream()
                    .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                    .collect(Collectors.toList());

            String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyy-MM-dd");
            dataMap.put(key, list);
        }
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    //用户趋势图
    @RequestMapping("user_order_trend_map.vpage")
    @ResponseBody
    public MapMessage userOrderTrendMap(){
        Integer stage = getRequestInt("stage", 1);  // 1：小学，  2：初高中
        return liveEnrollmentService.getUserOrderChart(getCurrentUserId(), stage, null, null);
    }

    //用户趋势图
    @RequestMapping("country_order_trend_map.vpage")
    @ResponseBody
    public MapMessage countryOrderTrendMap(){
        Integer stage = getRequestInt("stage", 1);  // 1：小学，  2：初高中
        return liveEnrollmentService.getUserOrderChartByOrder(getCurrentUserId(), null, null, stage);
    }

    //数据详情
    @RequestMapping("order_data_item.vpage")
    @ResponseBody
    public MapMessage orderDataItem(){
        String groupRoleType = requestString("groupRoleType","default");
        Long id = getRequestLong("id",0);
        Integer idType = getRequestInt("idType",1);
        Integer dimension = convertToDimension(groupRoleType);
        Integer stage = getRequestInt("stage", 1);  // 1：小学，  2：初高中
        
        if(id == 0){
            Long userId = getCurrentUserId();
            List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
            if(CollectionUtils.isEmpty(groupIds)){
                id = userId;
                idType = AgentConstants.INDICATOR_TYPE_USER;
            }else {
                id = groupIds.get(0);
                idType = AgentConstants.INDICATOR_TYPE_GROUP;
            }
        }

        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String,Object>> dataList = liveEnrollmentService.getOrderNumDataList(id, idType, stage, dimension);
        return mapMessage.add("dataList", dataList);
    }

    // 1: 默认， 2：专员， 3分区， 4区域， 5大区
    private int convertToDimension(String groupRoleType){
        if(Objects.equals(groupRoleType, "default")){
            return 1;
        }else if(Objects.equals(groupRoleType, "BusinessDeveloper")){
            return 2;
        }else if(Objects.equals(groupRoleType, "City")){
            return 3;
        }else if(Objects.equals(groupRoleType, "Area")){
            return 4;
        }else if(Objects.equals(groupRoleType, "Region")){
            return 5;
        }else {
            return 1;
        }
    }

    //详情筛选条件
    @RequestMapping("select_range.vpage")
    @ResponseBody
    public MapMessage selectRange(){
        Long userId = getCurrentUserId();
        Long id = getRequestLong("id",0);
        Integer idType = getRequestInt("idType",1);
        return MapMessage.successMessage().add("dataMap",agentUserLiveOrderService.selectRange(userId, id, idType));
    }

    //排行榜
    @RequestMapping("ranking_list.vpage")
    @ResponseBody
    public MapMessage rankingList(){
        Integer dateType = getRequestInt("dateType",1);  // 1 当日 2 累计
        Integer rankingType = getRequestInt("rankingType",1);  //1 专员 2 分区 3 学校 4 城市
        Integer stage = getRequestInt("stage", 1);  // 1：小学，  2：初高中
        return liveEnrollmentService.getRankingList(dateType,rankingType, 10, getCurrentUserId(), stage);
    }

    //测试消息数据统计
    @RequestMapping("judgeDeliveryId.vpage")
    @ResponseBody
    public MapMessage judgeDeliveryId(){

        return liveEnrollmentService.judgeDeliveryId(null, null);
    }
    //订单列表领取奖励
    @RequestMapping("receive_gifts.vpage")
    @ResponseBody
    public MapMessage receiveGifts(){
        String phone = requestString("phone");
        if(StringUtils.isBlank(phone)){
            return MapMessage.errorMessage("电话不能为空");
        }
        return liveEnrollmentService.receiveGifts(phone);
    }


    //测试消息数据统计
    @RequestMapping("updateUserOrderNum.vpage")
    @ResponseBody
    public MapMessage updateUserOrderNum(){
        Integer day = getRequestInt("day");
        Set<Long> userIds = requestLongSet("userIds");
        if(CollectionUtils.isEmpty(userIds) || day < 20181220){
            return MapMessage.successMessage().add("更新订单统计结果的用户数量", 0);
        }

        int i = 0;
        for(Long userId : userIds){
            liveEnrollmentService.updateUserStatisticsByUser(userId, day);
            i++;
        }
        return MapMessage.successMessage().add("更新订单统计结果的用户数量", i);
    }

    //用户正价课趋势图
    @RequestMapping("user_order_positive_trend_map.vpage")
    @ResponseBody
    public MapMessage userOrderPositiveTrendMap(){
        Integer stage = getRequestInt("stage",1); //1小学 2 中学
        return liveEnrollmentPositiveService.getUserPositiveOrderChart(getCurrentUserId(), null, null,stage);
    }

    //正价课数据详情
    @RequestMapping("order_positive_data_item.vpage")
    @ResponseBody
    public MapMessage orderPositiveDataItem(){
        String groupRoleType = requestString("groupRoleType","default");
        Long id = getRequestLong("id",0);
        Integer idType = getRequestInt("idType",1);
        Integer stage = getRequestInt("stage",1); //1小学 2 中学
        Integer dimension = convertToDimension(groupRoleType);

        if(id == 0){
            Long userId = getCurrentUserId();
            List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
            if(CollectionUtils.isEmpty(groupIds)){
                id = userId;
                idType = AgentConstants.INDICATOR_TYPE_USER;
            }else {
                id = groupIds.get(0);
                idType = AgentConstants.INDICATOR_TYPE_GROUP;
            }
        }

        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String,Object>> dataList = liveEnrollmentPositiveService.getPositiveOrderNumDataList(id, idType, dimension,stage);
        return mapMessage.add("dataList", dataList);
    }

    //测试数据
    @RequestMapping("test_consumer.vpage")
    @ResponseBody
    public MapMessage testConsumer(){
        String deliveryId = getRequestString("deliveryId");
        String orderId = getRequestString("orderId");
        Long platformPid = getRequestLong("platformPid");
        Long studentId =getRequestLong("platformSid");
        Date payTime = getRequestDate("payTime",DateUtils.FORMAT_SQL_DATETIME);
        Long payPrice = getRequestLong("payPrice");
        Integer courseType = getRequestInt("courseType");

//        liveEnrollmentService.saveLiveEnrollmentOrder(deliveryId, orderId, payTime, platformPid, studentId,payPrice,courseType);
        return MapMessage.successMessage();
    }

    //测试数据
    @RequestMapping("test_refund_message.vpage")
    @ResponseBody
    public MapMessage testRefundMessage(){

        String orderId = getRequestString("orderId");
        Long refundPrice = getRequestLong("refundPrice");
        String refundOrderId = getRequestString("refundOrderId");
        liveEnrollmentPositiveService.saveLiveEnrollmentRefundOrder(orderId, refundPrice,refundOrderId);
        return MapMessage.successMessage();
    }


    //初始化课程类型
    @RequestMapping("init_course_type.vpage")
    @ResponseBody
    public MapMessage initCourseType(){
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        Set<Integer> days = liveEnrollmentService.getEveryDays(startDate, endDate);
        int count = 0;
        for(Integer day : days){
            Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            DayRange dayRange = DayRange.newInstance(date.getTime());
            count += liveEnrollmentPositiveService.initLiveEnrollmentOrderData(dayRange.getStartDate(), dayRange.getEndDate());
        }

        return MapMessage.successMessage().add("更新订单数", count);
    }


    //刷用户数据
    @RequestMapping("init_user_trend_map.vpage")
    @ResponseBody
    public MapMessage initUserTrendMap(){
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        Set<Integer> days = liveEnrollmentService.getEveryDays(startDate, endDate);

        AgentGroup group = baseOrgService.getGroupByName("市场部");
        List<AgentGroupUser> groupUsers =baseOrgService.getAllGroupUsersByGroupId(group.getId());
//        baseOrgService.getManagedGroupIdListByUserId(getCurrentUserId())
        List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());

        for(Long userId : userIds){
            for(Integer day : days){
                liveEnrollmentService.updateUserStatisticsByUser(userId, day);
            }
        }
        return MapMessage.successMessage();
    }

    //初始化课程类型
    @RequestMapping("init_school_and_region_data.vpage")
    @ResponseBody
    public MapMessage initSchoolAndRegionData(){
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        Set<Integer> days = liveEnrollmentService.getEveryDays(startDate, endDate);
        liveEnrollmentService.initSchoolAndRegionData(days);
        return MapMessage.successMessage();
    }

    //刷用户数据
    @RequestMapping("del_user_positive_data.vpage")
    @ResponseBody
    public MapMessage delUserPositiveData(){
        String id = getRequestString("id");
        liveEnrollmentPositiveService.delPositiveData(id);
        return MapMessage.successMessage();
    }
}
