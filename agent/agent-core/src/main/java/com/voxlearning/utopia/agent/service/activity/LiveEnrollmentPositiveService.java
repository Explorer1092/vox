/**
 * Author:   xianlong.zhang
 * Date:     2018/12/28 15:13
 * Description: 正价课相关
 * History:
 */
package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.constants.QRCodeBusinessType;
import com.voxlearning.utopia.agent.dao.mongo.activity.LiveEnrollmentOrderDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.LiveEnrollmentOrderRefundDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.LiveEnrollmentUserPositiveStatisticsDao;
import com.voxlearning.utopia.agent.dao.mongo.qrcode.UserQrCodeDao;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentOrder;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentOrderRefund;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentUserPositiveStatistics;
import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollmentUserStatistics;
import com.voxlearning.utopia.agent.persist.entity.qrcode.UserQrCode;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class LiveEnrollmentPositiveService extends AbstractAgentService {
    @Inject
    private UserQrCodeDao userQrCodeDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private LiveEnrollmentUserPositiveStatisticsDao liveEnrollmentUserPositiveStatisticsDao;
    @Inject
    private LiveEnrollmentOrderRefundDao liveEnrollmentOrderRefundDao;
    @Inject
    private LiveEnrollmentOrderDao liveEnrollmentOrderDao;

    @Inject
    private LiveEnrollmentService liveEnrollmentService;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    /**
     * 更新用户当日正价课订单数
     * @param order
     * @param orderType 1 支付订单 2 退款订单
     */
    public void updateUserPositiveStatistics(LiveEnrollmentOrder order,Integer orderType,Boolean firstTime,Long refundPrice){
        if(order == null){
            return;
        }
        Integer day = SafeConverter.toInt(DateUtils.dateToString(order.getPayTime(), "yyyyMMdd"));
        UserQrCode userQrCode = userQrCodeDao.loadByRelatedId(QRCodeBusinessType.LIVE_ENROLLMENT, order.getDeliveryId());
        if(userQrCode != null && userQrCode.getUserId() != null){
            AgentUser user = baseOrgService.getUser(userQrCode.getUserId());
            if(user == null) {
                return;
            }
            Map<Long, LiveEnrollmentUserPositiveStatistics> userPositiveStatisticsMap = liveEnrollmentUserPositiveStatisticsDao.loadByUserIds(Collections.singleton(user.getId()), day);
            LiveEnrollmentUserPositiveStatistics userPositiveStatistics = MapUtils.isEmpty(userPositiveStatisticsMap)? null : userPositiveStatisticsMap.get(user.getId());
            if(userPositiveStatistics == null){
                userPositiveStatistics = new LiveEnrollmentUserPositiveStatistics();
                userPositiveStatistics.setUserId(user.getId());
                userPositiveStatistics.setUserName(user.getRealName());
                userPositiveStatistics.setDay(day);
            }
            if(StringUtils.isNotBlank(order.getCourseStage()) && (order.getCourseStage().equals("初中") || order.getCourseStage().equals("高中"))){ //中学数据
                if(orderType == 1){//付款
                    userPositiveStatistics.setMiddleOrderNum(SafeConverter.toDouble(userPositiveStatistics.getMiddleOrderNum()) + 1);
                    userPositiveStatistics.setMiddlePayPrice(SafeConverter.toLong(userPositiveStatistics.getMiddlePayPrice()) + SafeConverter.toLong(order.getPayPrice()));
                }else{//退款
                    if(firstTime){
                        userPositiveStatistics.setMiddleRefundNum(SafeConverter.toDouble(userPositiveStatistics.getMiddleRefundNum()) + 1);
                    }
                    userPositiveStatistics.setMiddleRefundPrice(SafeConverter.toLong(userPositiveStatistics.getMiddleRefundPrice()) + refundPrice);
                }
            }else{//小学数据
                if(orderType == 1){//付款
                    userPositiveStatistics.setOrderNum(SafeConverter.toDouble(userPositiveStatistics.getOrderNum()) + 1);
                    userPositiveStatistics.setPayPrice(SafeConverter.toLong(userPositiveStatistics.getPayPrice()) + SafeConverter.toLong(order.getPayPrice()));
                }else{//退款
                    if(firstTime){
                        userPositiveStatistics.setRefundNum(SafeConverter.toDouble(userPositiveStatistics.getRefundNum()) + 1);
                    }
                    userPositiveStatistics.setRefundPrice(SafeConverter.toLong(userPositiveStatistics.getRefundPrice()) + refundPrice);
                }
            }


            liveEnrollmentUserPositiveStatisticsDao.upsert(userPositiveStatistics);
        }
    }

    public void saveLiveEnrollmentRefundOrder(String orderId, Long refundPrice,String refundOrderId){
        List<LiveEnrollmentOrderRefund> orderRefunds = liveEnrollmentOrderRefundDao.loadByOrderId(orderId).stream().collect(Collectors.toList());
        List<LiveEnrollmentOrderRefund> refunds =orderRefunds.stream().filter(p->p.getRefundOrderId().equals(refundOrderId)).collect(Collectors.toList());
        if( CollectionUtils.isNotEmpty(refunds)){//退款消息已处理过
            return;
        }
        LiveEnrollmentOrderRefund orderRefund = new LiveEnrollmentOrderRefund();
        orderRefund.setOrderId(orderId);
        orderRefund.setRefundPrice(SafeConverter.toLong(refundPrice));
        orderRefund.setRefundOrderId(refundOrderId);
        liveEnrollmentOrderRefundDao.insert(orderRefund);

        List<LiveEnrollmentOrder> orders = liveEnrollmentOrderDao.loadByOrderId(orderId);
        boolean firstTime = false;
        if(CollectionUtils.isNotEmpty(orders)){//订单存在且首次退款 更新退款次数  及退款金额
            if(CollectionUtils.isEmpty(orderRefunds)){
                firstTime = true;
            }
            LiveEnrollmentOrder order = orders.get(0);
            if(order != null && order.getCourseType() == 2){
                boolean finalFirstTime = firstTime;
                AlpsThreadPool.getInstance().submit(() -> updateUserPositiveStatistics(order,2, finalFirstTime,refundPrice));
            }
        }
    }

    /**
     * 正价课趋势图
     * @param userId
     * @param startDate
     * @param endDate
     * @return
     */
    public MapMessage getUserPositiveOrderChart(Long userId, Date startDate, Date endDate,Integer stage){

        Set<Integer> days = liveEnrollmentService.getEveryDays(startDate, endDate);
        if(CollectionUtils.isEmpty(days)){
            return MapMessage.successMessage().add("dataList", new ArrayList<>()).add("total", 0d);
        }
        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if(CollectionUtils.isNotEmpty(managedGroupIds)){
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            List<Long> tempUserIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            userIds.addAll(tempUserIds);
        }



        List<Map<String, Object>> dataList = new ArrayList<>();
        AgentRoleType roleType = baseOrgService.getUserRole(userId);

        List<LiveEnrollmentUserPositiveStatistics> userDataList = liveEnrollmentUserPositiveStatisticsDao.loadByUsersAndDays(userIds, days);
        Map<Integer, List<LiveEnrollmentUserPositiveStatistics>> dayDataMap = userDataList.stream().collect(Collectors.groupingBy(LiveEnrollmentUserPositiveStatistics::getDay));
        if(stage == 1){
            double totalOrderNum = 0,totalRefundNum = 0;
            Long totalPayPrice = 0l,totalRefundPrice = 0l;
            for (Integer day : days) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("day", day);
                List<LiveEnrollmentUserPositiveStatistics> dayDataList = dayDataMap.get(day);
                double dayOrderNum = 0,dayRefundNum = 0;
                long dayPayPrice = 0,dayRefundPrice = 0;
                if (CollectionUtils.isNotEmpty(dayDataList)) {
                    for (LiveEnrollmentUserPositiveStatistics dayData : dayDataList) {
                        dayOrderNum = MathUtils.doubleAdd(dayOrderNum, SafeConverter.toDouble(dayData.getOrderNum()));
                        dayPayPrice = dayPayPrice +  SafeConverter.toLong(dayData.getPayPrice());
                        dayRefundNum = dayRefundNum + SafeConverter.toDouble(dayData.getRefundNum());
                        dayRefundPrice = dayRefundPrice + SafeConverter.toLong(dayData.getRefundPrice());
                    }
                }
                if(roleType == AgentRoleType.Country){
                    dayOrderNum = MathUtils.doubleDivide(dayOrderNum, 1, 0, BigDecimal.ROUND_CEILING);
                }
                itemMap.put("orderNum", dayOrderNum);

                dataList.add(itemMap);
                totalOrderNum = MathUtils.doubleAdd(totalOrderNum, dayOrderNum);
                totalPayPrice = totalPayPrice + dayPayPrice;
                totalRefundNum = MathUtils.doubleAdd(totalRefundNum, dayRefundNum);
                totalRefundPrice = totalRefundPrice + dayRefundPrice;
            }

            if(roleType == AgentRoleType.Country){
                totalOrderNum = MathUtils.doubleDivide(totalOrderNum, 1, 0, BigDecimal.ROUND_CEILING);
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("dataList", dataList);
            mapMessage.add("orderNum", totalOrderNum);
            mapMessage.add("payPrice", totalPayPrice);
            mapMessage.add("refundNum", totalRefundNum);
            mapMessage.add("refundPrice", totalRefundPrice);
            return mapMessage;
        }else {
            double totalMiddleOrderNum =0,totalMiddleRefundNum = 0;
            Long totalMiddlePayPrice = 0l,totalMiddleRefundPrice = 0l;
            for (Integer day : days) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("day", day);
                List<LiveEnrollmentUserPositiveStatistics> dayDataList = dayDataMap.get(day);
                double dayMiddleOrderNum = 0,dayMiddleRefundNum = 0;
                long dayMiddlePayPrice = 0,dayMiddleRefundPrice = 0 ;
                if (CollectionUtils.isNotEmpty(dayDataList)) {
                    for (LiveEnrollmentUserPositiveStatistics dayData : dayDataList) {
                        dayMiddleOrderNum = MathUtils.doubleAdd(dayMiddleOrderNum, SafeConverter.toDouble(dayData.getMiddleOrderNum()));
                        dayMiddlePayPrice = dayMiddlePayPrice +  SafeConverter.toLong(dayData.getMiddlePayPrice());
                        dayMiddleRefundNum = dayMiddleRefundNum + SafeConverter.toDouble(dayData.getMiddleRefundNum());
                        dayMiddleRefundPrice = dayMiddleRefundPrice + SafeConverter.toLong(dayData.getMiddleRefundPrice());
                    }
                }
                if(roleType == AgentRoleType.Country){
                    dayMiddleOrderNum = MathUtils.doubleDivide(dayMiddleOrderNum, 1, 0, BigDecimal.ROUND_CEILING);
                }
                itemMap.put("orderNum", dayMiddleOrderNum);

                dataList.add(itemMap);
                totalMiddleOrderNum = MathUtils.doubleAdd(totalMiddleOrderNum, dayMiddleOrderNum);
                totalMiddlePayPrice = totalMiddlePayPrice + dayMiddlePayPrice;
                totalMiddleRefundNum = MathUtils.doubleAdd(totalMiddleRefundNum, dayMiddleRefundNum);
                totalMiddleRefundPrice = totalMiddleRefundPrice + dayMiddleRefundPrice;
            }

            if(roleType == AgentRoleType.Country){
                totalMiddleOrderNum = MathUtils.doubleDivide(totalMiddleOrderNum, 1, 0, BigDecimal.ROUND_CEILING);
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("dataList", dataList);
            mapMessage.add("orderNum", totalMiddleOrderNum);
            mapMessage.add("payPrice", totalMiddlePayPrice);
            mapMessage.add("refundNum", totalMiddleRefundNum);
            mapMessage.add("refundPrice", totalMiddleRefundPrice);
            return mapMessage;
        }

    }

    /**
     *
     * @param id
     * @param idType
     * @param dimension 1: 默认， 2：专员， 3分区， 4区域， 5大区
     * @return
     */
    public List<Map<String,Object>> getPositiveOrderNumDataList(Long id, Integer idType, Integer dimension,Integer stage){
        List<Map<String, Object>> dataList = new ArrayList<>();
        if(idType.equals(AgentConstants.INDICATOR_TYPE_USER)){
            return dataList;
        }

        Set<Integer> days = liveEnrollmentService.getEveryDays(null, null);
        AgentGroup group = baseOrgService.getGroupById(id);
        if(dimension == 2 || (dimension == 1 && group.fetchGroupRoleType() == AgentGroupRoleType.City)){
            // 专员的情况下
            List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(),AgentRoleType.BusinessDeveloper.getId());
            dataList.addAll(generateUserPositiveOrderNumDataList(userIds, days));
        }else {
            List<AgentGroup> groups;
            if(dimension == 1 ){  // 默认情况下
                if(group.fetchGroupRoleType() != AgentGroupRoleType.City
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Area
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Region
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Marketing
                        ){
                    groups = baseOrgService.getSubGroupList(group.getId()).stream()
                            .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).collect(Collectors.toList());
                }else {
                    groups = baseOrgService.getGroupListByParentId(group.getId()).stream()
                            .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City
                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Area
                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Region
                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Marketing)
                            .collect(Collectors.toList());
                }
            }else {
                AgentGroupRoleType targetGroupRoleType;
                if(dimension == 3){
                    targetGroupRoleType = AgentGroupRoleType.City;
                }else if(dimension == 4){
                    targetGroupRoleType = AgentGroupRoleType.Area;
                }else if(dimension == 5){
                    targetGroupRoleType = AgentGroupRoleType.Region;
                }else {
                    targetGroupRoleType = null;
                }
                groups = baseOrgService.getSubGroupList(group.getId()).stream()
                        .filter(p -> Objects.equals(p.fetchGroupRoleType(), targetGroupRoleType))
                        .collect(Collectors.toList());
            }
            dataList.addAll(generateGroupPositiveOrderNumDataList(groups, days,stage));
        }
        return dataList;
    }


    private List<Map<String, Object>> generateUserPositiveOrderNumDataList(List<Long> userIds, Collection<Integer> days){
        List<Map<String, Object>> dataList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds) || CollectionUtils.isEmpty(days)){
            return dataList;
        }

        List<LiveEnrollmentUserPositiveStatistics> allUserDataList = liveEnrollmentUserPositiveStatisticsDao.loadByUsersAndDays(userIds, days);
        Map<Long, List<LiveEnrollmentUserPositiveStatistics>> userDataMap = allUserDataList.stream().collect(Collectors.groupingBy(LiveEnrollmentUserPositiveStatistics::getUserId));

        Set<Long> hasNoDataUserIds = userIds.stream().filter(p -> !userDataMap.containsKey(p)).collect(Collectors.toSet());
        Map<Long, AgentUser> userMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(hasNoDataUserIds)){
            userMap.putAll(agentUserLoaderClient.findByIds(hasNoDataUserIds));
        }

        userIds.forEach(p -> {

            double refundNum = 0d;     //退款单数
            double totalOrderNum = 0d; //总订单数
            long payPrice = 0l;        //支付金额
            long refundPrice = 0l;     //退款金额
            String name = "";

            List<LiveEnrollmentUserPositiveStatistics> userDataList = userDataMap.get(p);
            if(CollectionUtils.isNotEmpty(userDataList)){
                for(LiveEnrollmentUserPositiveStatistics data : userDataList){
                    totalOrderNum = MathUtils.doubleAdd(totalOrderNum, SafeConverter.toDouble(data.getOrderNum()));
                    refundNum = MathUtils.doubleAdd(refundNum,SafeConverter.toDouble(data.getRefundNum()));
                    payPrice = payPrice + SafeConverter.toLong(data.getPayPrice());
                    refundPrice = refundPrice + SafeConverter.toLong(data.getRefundPrice());
                    if(StringUtils.isBlank(name)){
                        name = data.getUserName();
                    }
                }
            }

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", p);
            itemMap.put("idType", AgentConstants.INDICATOR_TYPE_USER);
            if(StringUtils.isBlank(name)){
                AgentUser user = userMap.get(p);
                if(user != null){
                    name = user.getRealName();
                }
            }
            itemMap.put("name", name);

            // 总订单数
            itemMap.put("orderNum", totalOrderNum);
            itemMap.put("refundNum", refundNum);
            itemMap.put("payPrice", payPrice);
            itemMap.put("refundPrice", refundPrice);
            dataList.add(itemMap);
        });

        return dataList;
    }

    private List<Map<String, Object>> generateGroupPositiveOrderNumDataList(List<AgentGroup> groupList, Collection<Integer> days,Integer stage){
        List<Map<String, Object>> dataList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupList) || CollectionUtils.isEmpty(days)){
            return dataList;
        }
        List<Future<Map<String,Object>>> futureList = new ArrayList<>();
        groupList.forEach(p -> {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupPositiveOrderData( p, days,stage)));
        });
        for(Future<Map<String,Object>> future : futureList) {
            try {
                Map<String,Object> item = future.get();
                if(MapUtils.isNotEmpty(item)){
                    dataList.add(item);
                }
            } catch (Exception e) {
                logger.error("统计部门正价订单数据异常",e);
            }
        }
        return dataList;

    }

    public Map<String,Object> getGroupPositiveOrderData(AgentGroup group, Collection<Integer> days, Integer stage){
        List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
        List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
        List<LiveEnrollmentUserPositiveStatistics> userDataList = liveEnrollmentUserPositiveStatisticsDao.loadByUsersAndDays(userIds, days);
        if(stage == 1){//小学
            double refundNum = 0d;  //退款单数
            double totalOrderNum = 0d; //总订单数
            long payPrice = 0l; //支付金额
            long refundPrice = 0l;  //退款金额
            for(LiveEnrollmentUserPositiveStatistics userData : userDataList){
                totalOrderNum = MathUtils.doubleAdd(totalOrderNum, SafeConverter.toDouble(userData.getOrderNum()));
                refundNum = MathUtils.doubleAdd(refundNum,SafeConverter.toDouble(userData.getRefundNum()));
                payPrice = payPrice + SafeConverter.toLong(userData.getPayPrice());
                refundPrice = refundPrice + SafeConverter.toLong(userData.getRefundPrice());
            }

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", group.getId());
            itemMap.put("idType", AgentConstants.INDICATOR_TYPE_GROUP);
            itemMap.put("name", group.getGroupName());
            // 总订单数
            itemMap.put("orderNum", totalOrderNum);
            itemMap.put("refundNum", refundNum);
            itemMap.put("payPrice", payPrice);
            itemMap.put("refundPrice", refundPrice);
            return itemMap;
        }else{//中学
            double middleRefundNum = 0d;  //退款单数
            double middleTotalOrderNum = 0d; //总订单数
            long middlePayPrice = 0l; //支付金额
            long middleRefundPrice = 0l;  //退款金额
            for(LiveEnrollmentUserPositiveStatistics userData : userDataList){
                middleRefundNum = MathUtils.doubleAdd(middleRefundNum, SafeConverter.toDouble(userData.getMiddleOrderNum()));
                middleTotalOrderNum = MathUtils.doubleAdd(middleTotalOrderNum,SafeConverter.toDouble(userData.getMiddleRefundNum()));
                middlePayPrice = middlePayPrice + SafeConverter.toLong(userData.getMiddlePayPrice());
                middleRefundPrice = middleRefundPrice + SafeConverter.toLong(userData.getMiddleRefundPrice());
            }

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", group.getId());
            itemMap.put("idType", AgentConstants.INDICATOR_TYPE_GROUP);
            itemMap.put("name", group.getGroupName());
            // 总订单数
            itemMap.put("orderNum", middleRefundNum);
            itemMap.put("refundNum", middleTotalOrderNum);
            itemMap.put("payPrice", middlePayPrice);
            itemMap.put("refundPrice", middleRefundPrice);
            return itemMap;
        }

    }

    public Integer initLiveEnrollmentOrderData(Date startDate, Date endDate){

        List<LiveEnrollmentOrder>  list = liveEnrollmentOrderDao.loadByDate(startDate, endDate);
        if(CollectionUtils.isEmpty(list)){
            return 0;
        }
        int result = 0;
        Map<String, LiveEnrollmentOrder> orderMap = list.stream().collect(Collectors.toMap(LiveEnrollmentOrder::getOrderId, Function.identity(), (o1, o2) -> o1));

        List<String> orderIdList = list.stream().map(LiveEnrollmentOrder::getOrderId).collect(Collectors.toList());
        Map<Integer, List<String>> groupedMap = orderIdList.stream().collect(Collectors.groupingBy(p -> orderIdList.indexOf(p) / 300, Collectors.toList()));
        for(List<String> idList : groupedMap.values()){
            MapMessage mapMessage = LiveEnrollmentRemoteClient.loadOrderCourseInfo(idList);
            if(mapMessage.isSuccess()){
                Map<String, Object> dataMap = (Map<String, Object>)mapMessage.get("data");
                if(dataMap != null && dataMap.containsKey("orders")){
                    List<Map<String, Object>> orderList = (List<Map<String, Object>>)dataMap.get("orders");
                    if(CollectionUtils.isNotEmpty(orderList)){
                        for(Map<String, Object> orderData : orderList){
                            String orderId = (String)orderData.get("orderId");
                            if(StringUtils.isNotBlank(orderId) && orderMap.containsKey(orderId)){
                                LiveEnrollmentOrder order = orderMap.get(orderId);
                                if(order != null && StringUtils.isBlank(order.getCourseStage())){
                                    order.setCourseStage(SafeConverter.toString(orderData.get("courseStage")));
                                    order.setCourseSubject(SafeConverter.toString(orderData.get("courseSubject")));
                                    order.setCourseName(SafeConverter.toString(orderData.get("courseName")));
                                    order.setCourseGrades(StringUtils.toIntegerList(SafeConverter.toString(orderData.get("courseGrade"))));
                                    liveEnrollmentOrderDao.upsert(order);
                                    result ++;
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        }
        return result;
    }

    public void delPositiveData(String id){
        liveEnrollmentUserPositiveStatisticsDao.remove(id);
    }

}
