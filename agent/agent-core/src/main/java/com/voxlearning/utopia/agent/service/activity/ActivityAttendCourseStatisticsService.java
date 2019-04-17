package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.*;
import com.voxlearning.utopia.agent.persist.entity.activity.*;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.activity.ActivityAttendCourseStatisticsView;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ActivityAttendCourseStatisticsService {

    @Inject
    private ActivityAttendCourseStatisticsDao activityAttendCourseStatisticsDao;

    @Inject
    private ActivityOrderCourseDao orderCourseDao;
    @Inject
    private ActivityAttendCourseDao attendCourseDao;
    @Inject
    private ActivityOrderDao orderDao;
    @Inject
    private ActivityOrderStudentDao orderStudentDao;
    @Inject
    private AgentActivityDao activityDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private ActivityExtendDao activityExtendDao;
    @Inject
    private ActivityCardCourseDao cardCourseDao;
    @Inject
    private ActivityCardDao cardDao;

//    public void attendCourseStatistics(ActivityAttendCourse attendCourse){
//        if(attendCourse == null || attendCourse.getStudentId() == null || StringUtils.isBlank(attendCourse.getCourseId())){
//            return;
//        }
//
//        List<ActivityOrderCourse> orderCourseList = orderCourseDao.loadBySidAndCid(attendCourse.getStudentId(), attendCourse.getCourseId());
//        if(CollectionUtils.isEmpty(orderCourseList)){
//            return;
//        }
//
//        Integer day = SafeConverter.toInt(DateUtils.dateToString(attendCourse.getAttendTime(), "yyyyMMdd"));
//
//        DayRange dayRange = DayRange.newInstance(attendCourse.getAttendTime().getTime());
//        Date today = dayRange.getStartDate();
//
//        orderCourseList.sort((o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? -1 : 1);
//        ActivityOrderCourse orderCourse = orderCourseList.get(0);
//
//
//        String currentOrderId = orderCourse.getOrderId();
//        ActivityOrder order = orderDao.loadByOid(currentOrderId);
//        if(order == null){
//            return;
//        }
//
//        List<ActivityOrderStudent> studentList = orderStudentDao.loadByOid(currentOrderId);
//        if(CollectionUtils.isNotEmpty(studentList)){
//            List<ActivityOrderStudent> targetStudentList = studentList.stream().filter(s -> Objects.equals(s.getStudentId(), attendCourse.getStudentId())).collect(Collectors.toList());
//            if(CollectionUtils.isNotEmpty(targetStudentList)){
//                targetStudentList.forEach(orderStudent -> {
//                    if(orderStudent.getAttendClassLatestDay() == null || !Objects.equals(orderStudent.getAttendClassLatestDay(), day)){
//                        orderStudent.setAttendClassLatestDay(day);
//                        orderStudent.setAttendClassDayCount(SafeConverter.toInt(orderStudent.getAttendClassDayCount()) + 1);
//                        orderStudentDao.replace(orderStudent);
//                    }
//                });
//            }
//        }
//
//        ActivityExtend extend = activityExtendDao.loadByAid(order.getActivityId());
//        int meetConditionDays = extend == null ? 1 : SafeConverter.toInt(extend.getMeetConditionDays(), 1);
//
//        List<ActivityOrder> orderList = orderDao.loadByAidAndOrderUserId(order.getActivityId(), order.getOrderUserId());
//        Set<String> orderIds = orderList.stream().map(ActivityOrder::getOrderId).collect(Collectors.toSet());
//
//        Map<String, Integer> beforeTodayAttendDayCount = new HashMap<>();   // 今日之前上课天数
//        Map<String, Integer> todayAttendCount = new HashMap<>();      // 今天上课次数
//
//        List<ActivityAttendCourse> allAttendCourseList = new ArrayList<>();
//        for(String orderId : orderIds) {
//
//            List<ActivityOrderCourse> orderCourses = orderCourseDao.loadByOid(orderId);
//            Set<String> courseIds = orderCourses.stream().filter(p -> Objects.equals(p.getStudentId(), attendCourse.getStudentId())).map(ActivityOrderCourse::getCourseId).collect(Collectors.toSet());
//
//            if(CollectionUtils.isEmpty(courseIds)){
//                continue;
//            }
//
//
//            List<ActivityAttendCourse> attendCourseList = new ArrayList<>();
//            courseIds.forEach(c -> {
//                List<ActivityAttendCourse> tmpList = attendCourseDao.loadBySidAndCid(attendCourse.getStudentId(), c);
//                if (CollectionUtils.isNotEmpty(tmpList)) {
//                    attendCourseList.addAll(tmpList);
//                    allAttendCourseList.addAll(tmpList);
//                }
//            });
//
//            List<ActivityAttendCourse> beforeTodayList = attendCourseList.stream().filter(t -> t.getAttendTime().before(today)).collect(Collectors.toList());
//            Set<Integer> attendDays = beforeTodayList.stream().map(p -> SafeConverter.toInt(DateUtils.dateToString(p.getAttendTime(), "yyyyMMdd"))).collect(Collectors.toSet());
//            beforeTodayAttendDayCount.put(orderId, attendDays.size());
//            // 今天参加了多少次课， 学生首次参加才进行统计， 重复参加不进行计数
//            int todayCount = (int)attendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
//            todayAttendCount.put(orderId, todayCount);
//        }
//
//        ActivityAttendCourseStatistics statistics = activityAttendCourseStatisticsDao.loadByUidAndDay(order.getActivityId(), order.getUserId(), day);
//        if (statistics == null) {
//            statistics = new ActivityAttendCourseStatistics();
//            statistics.setActivityId(order.getActivityId());
//            statistics.setUserId(order.getUserId());
//            statistics.setUserName(order.getUserName());
//            statistics.setDay(day);
//        }
//
//        long todayCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
//        if(todayCount == 1){
//            statistics.setAttendStuCount(SafeConverter.toInt(statistics.getAttendStuCount()) + 1);
//            long beforeCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().before(today)).count();
//            if(beforeCount == 0 ){
//                statistics.setFirstAttendStuCount(SafeConverter.toInt(statistics.getFirstAttendStuCount()) + 1);
//            }
//        }
//
//        int count = 0;
//        boolean isCurrentOrder = false;
//        for(String k : beforeTodayAttendDayCount.keySet()){
//            if(SafeConverter.toInt(beforeTodayAttendDayCount.get(k)) == (meetConditionDays - 1)){
//                int t = SafeConverter.toInt(todayAttendCount.get(k));
//                if(t == 1){
//                    count ++;
//                    if(Objects.equals(currentOrderId, k)){
//                        isCurrentOrder = true;
//                    }
//                }
//            }
//        }
//
//        if(count == 1 && isCurrentOrder){
//            statistics.setMeetConditionStuCount(SafeConverter.toInt(statistics.getMeetConditionStuCount()) + 1);
//        }
//        activityAttendCourseStatisticsDao.upsert(statistics);
//    }

    public void attendCourseStatistics(ActivityAttendCourse attendCourse){
        if(attendCourse == null || StringUtils.isBlank(attendCourse.getActivityId()) || StringUtils.isBlank(attendCourse.getRelatedId()) ||attendCourse.getStudentId() == null || StringUtils.isBlank(attendCourse.getCourseId())){
            return;
        }

        ActivityOrder order = orderDao.loadByOid(attendCourse.getRelatedId());
        if(order != null && Objects.equals(order.getActivityId(), attendCourse.getActivityId())){
            // order
            attendCourseStatisticsOrder(order, attendCourse.getStudentId(), attendCourse.getAttendTime());
        }else {
            ActivityCard card = cardDao.loadByCn(attendCourse.getRelatedId());
            if(card != null && Objects.equals(card.getActivityId(), attendCourse.getActivityId())){
                // card
                attendCourseStatisticsCard(card, attendCourse.getStudentId(), attendCourse.getAttendTime());
            }
        }
    }

    private void attendCourseStatisticsOrder(ActivityOrder order, Long studentId, Date attendTime){

        if(order == null){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(attendTime, "yyyyMMdd"));

        DayRange dayRange = DayRange.newInstance(attendTime.getTime());
        Date today = dayRange.getStartDate();

        ActivityExtend extend = activityExtendDao.loadByAid(order.getActivityId());
        int meetConditionDays = extend == null ? 1 : SafeConverter.toInt(extend.getMeetConditionDays(), 1);


        List<ActivityOrderStudent> studentList = orderStudentDao.loadByOid(order.getOrderId());
        if(CollectionUtils.isNotEmpty(studentList)){
            List<ActivityOrderStudent> targetStudentList = studentList.stream().filter(s -> Objects.equals(s.getStudentId(), studentId)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(targetStudentList)){
                targetStudentList.forEach(orderStudent -> {
                    if(orderStudent.getAttendClassLatestDay() == null || !Objects.equals(orderStudent.getAttendClassLatestDay(), day)){
                        orderStudent.setAttendClassLatestDay(day);
                        orderStudent.setAttendClassDayCount(SafeConverter.toInt(orderStudent.getAttendClassDayCount()) + 1);
                        orderStudentDao.replace(orderStudent);
                    }
                });
            }
        }

        List<ActivityOrder> orderList = orderDao.loadByAidAndUid(order.getActivityId(), order.getUserId());
        Set<String> orderIds = orderList.stream().filter(p -> Objects.equals(p.getOrderUserId(), order.getOrderUserId())).map(ActivityOrder::getOrderId).collect(Collectors.toSet());

        Map<String, Integer> beforeTodayAttendDayCount = new HashMap<>();   // 今日之前上课天数
        Map<String, Integer> todayAttendCount = new HashMap<>();      // 今天上课次数

        List<ActivityAttendCourse> allAttendCourseList = new ArrayList<>();
        for(String orderId : orderIds) {

            List<ActivityOrderCourse> orderCourses = orderCourseDao.loadByOid(orderId);
            Set<String> courseIds = orderCourses.stream().filter(p -> Objects.equals(p.getStudentId(), studentId)).map(ActivityOrderCourse::getCourseId).collect(Collectors.toSet());

            if(CollectionUtils.isEmpty(courseIds)){
                continue;
            }


            List<ActivityAttendCourse> attendCourseList = new ArrayList<>();
            courseIds.forEach(c -> {
                List<ActivityAttendCourse> tmpList = attendCourseDao.loadBySidAndCid(order.getActivityId(), orderId, studentId, c);
                if (CollectionUtils.isNotEmpty(tmpList)) {
                    attendCourseList.addAll(tmpList);
                    allAttendCourseList.addAll(tmpList);
                }
            });

            List<ActivityAttendCourse> beforeTodayList = attendCourseList.stream().filter(t -> t.getAttendTime().before(today)).collect(Collectors.toList());
            Set<Integer> attendDays = beforeTodayList.stream().map(p -> SafeConverter.toInt(DateUtils.dateToString(p.getAttendTime(), "yyyyMMdd"))).collect(Collectors.toSet());
            beforeTodayAttendDayCount.put(orderId, attendDays.size());
            // 今天参加了多少次课， 学生首次参加才进行统计， 重复参加不进行计数
            int todayCount = (int)attendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
            todayAttendCount.put(orderId, todayCount);
        }

        ActivityAttendCourseStatistics statistics = activityAttendCourseStatisticsDao.loadByUidAndDay(order.getActivityId(), order.getUserId(), day);
        if (statistics == null) {
            statistics = new ActivityAttendCourseStatistics();
            statistics.setActivityId(order.getActivityId());
            statistics.setUserId(order.getUserId());
            statistics.setUserName(order.getUserName());
            statistics.setDay(day);
        }

        long todayCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
        if(todayCount == 1){
            statistics.setAttendStuCount(SafeConverter.toInt(statistics.getAttendStuCount()) + 1);
            long beforeCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().before(today)).count();
            if(beforeCount == 0 ){
                statistics.setFirstAttendStuCount(SafeConverter.toInt(statistics.getFirstAttendStuCount()) + 1);
            }
        }

        int count = 0;
        boolean isCurrentOrder = false;
        for(String k : beforeTodayAttendDayCount.keySet()){
            if(SafeConverter.toInt(beforeTodayAttendDayCount.get(k)) == (meetConditionDays - 1)){
                int t = SafeConverter.toInt(todayAttendCount.get(k));
                if(t == 1){
                    count ++;
                    if(Objects.equals(order.getOrderId(), k)){
                        isCurrentOrder = true;
                    }
                }
            }
        }

        if(count == 1 && isCurrentOrder){
            statistics.setMeetConditionStuCount(SafeConverter.toInt(statistics.getMeetConditionStuCount()) + 1);
        }
        activityAttendCourseStatisticsDao.upsert(statistics);

    }

    private void attendCourseStatisticsCard(ActivityCard card, Long studentId, Date attendTime){
        if(card == null){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(attendTime, "yyyyMMdd"));

        DayRange dayRange = DayRange.newInstance(attendTime.getTime());
        Date today = dayRange.getStartDate();

        ActivityExtend extend = activityExtendDao.loadByAid(card.getActivityId());
        int meetConditionDays = extend == null ? 1 : SafeConverter.toInt(extend.getMeetConditionDays(), 1);

        List<ActivityCard> cardList = cardDao.loadByAidAndUid(card.getActivityId(), card.getUserId());

        Set<String> cardNos = cardList.stream().filter(p -> Objects.equals(p.getCardUserId(), card.getCardUserId())).map(ActivityCard::getCardNo).collect(Collectors.toSet());

        Map<String, Integer> beforeTodayAttendDayCount = new HashMap<>();   // 今日之前上课天数
        Map<String, Integer> todayAttendCount = new HashMap<>();      // 今天上课次数

        List<ActivityAttendCourse> allAttendCourseList = new ArrayList<>();
        for(String tmpCardNo : cardNos) {

            List<ActivityCardCourse> cardCourses = cardCourseDao.loadByOid(tmpCardNo);
            Set<String> courseIds = cardCourses.stream().filter(p -> Objects.equals(p.getStudentId(), studentId)).map(ActivityCardCourse::getCourseId).collect(Collectors.toSet());

            if(CollectionUtils.isEmpty(courseIds)){
                continue;
            }


            List<ActivityAttendCourse> attendCourseList = new ArrayList<>();
            courseIds.forEach(c -> {
                List<ActivityAttendCourse> tmpList = attendCourseDao.loadBySidAndCid(card.getActivityId(), tmpCardNo, studentId, c);
                if (CollectionUtils.isNotEmpty(tmpList)) {
                    attendCourseList.addAll(tmpList);
                    allAttendCourseList.addAll(tmpList);
                }
            });

            List<ActivityAttendCourse> beforeTodayList = attendCourseList.stream().filter(t -> t.getAttendTime().before(today)).collect(Collectors.toList());
            Set<Integer> attendDays = beforeTodayList.stream().map(p -> SafeConverter.toInt(DateUtils.dateToString(p.getAttendTime(), "yyyyMMdd"))).collect(Collectors.toSet());
            beforeTodayAttendDayCount.put(tmpCardNo, attendDays.size());
            // 今天参加了多少次课， 学生首次参加才进行统计， 重复参加不进行计数
            int todayCount = (int)attendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
            todayAttendCount.put(tmpCardNo, todayCount);
        }

        ActivityAttendCourseStatistics statistics = activityAttendCourseStatisticsDao.loadByUidAndDay(card.getActivityId(), card.getUserId(), day);
        if (statistics == null) {
            statistics = new ActivityAttendCourseStatistics();
            statistics.setActivityId(card.getActivityId());
            statistics.setUserId(card.getUserId());
            statistics.setUserName(card.getUserName());
            statistics.setDay(day);
        }

        long todayCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
        if(todayCount == 1){
            statistics.setAttendStuCount(SafeConverter.toInt(statistics.getAttendStuCount()) + 1);
            long beforeCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().before(today)).count();
            if(beforeCount == 0 ){
                statistics.setFirstAttendStuCount(SafeConverter.toInt(statistics.getFirstAttendStuCount()) + 1);
            }
        }

        int count = 0;
        boolean isCurrentCard = false;
        for(String k : beforeTodayAttendDayCount.keySet()){
            if(SafeConverter.toInt(beforeTodayAttendDayCount.get(k)) == (meetConditionDays - 1)){
                int t = SafeConverter.toInt(todayAttendCount.get(k));
                if(t == 1){
                    count ++;
                    if(Objects.equals(card.getCardNo(), k)){
                        isCurrentCard = true;
                    }
                }
            }
        }

        if(count == 1 && isCurrentCard){
            statistics.setMeetConditionStuCount(SafeConverter.toInt(statistics.getMeetConditionStuCount()) + 1);
        }
        activityAttendCourseStatisticsDao.upsert(statistics);

    }


//    public void attendCourseStatistics(ActivityAttendCourse attendCourse){
//        if(attendCourse == null || attendCourse.getStudentId() == null || StringUtils.isBlank(attendCourse.getCourseId())){
//            return;
//        }
//
//        List<ActivityOrderCourse> orderCourseList = orderCourseDao.loadBySidAndCid(attendCourse.getStudentId(), attendCourse.getCourseId());
//
//        List<ActivityCardCourse> cardCourseList = cardCourseDao.loadBySidAndCid(attendCourse.getStudentId(), attendCourse.getCourseId());
//
//        ActivityOrderCourse orderCourse = null;
//        if(CollectionUtils.isNotEmpty(orderCourseList)){
//            orderCourseList.sort((o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? -1 : 1);
//            orderCourse = orderCourseList.get(0);
//        }
//
//        ActivityCardCourse cardCourse = null;
//        if(CollectionUtils.isNotEmpty(cardCourseList)){
//            cardCourseList.sort((o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? -1 : 1);
//            cardCourse = cardCourseList.get(0);
//        }
//
//        if(orderCourse == null && cardCourse == null){
//            return;
//        }else if(orderCourse != null && cardCourse == null){
//            // order
//            attendCourseStatisticsOrder(orderCourse.getOrderId(), attendCourse.getStudentId(), attendCourse.getAttendTime());
//        }else if(orderCourse == null){
//            // card
//            attendCourseStatisticsCard(cardCourse.getCardNo(), attendCourse.getStudentId(), attendCourse.getAttendTime());
//        }else {
//            if(orderCourse.getCreateTime().after(cardCourse.getCreateTime())){
//                // order
//                attendCourseStatisticsOrder(orderCourse.getOrderId(), attendCourse.getStudentId(), attendCourse.getAttendTime());
//            }else {
//                // card
//                attendCourseStatisticsCard(cardCourse.getCardNo(), attendCourse.getStudentId(), attendCourse.getAttendTime());
//            }
//        }
//    }
//
//    private void attendCourseStatisticsOrder(String currentOrderId, Long studentId, Date attendTime){
//        ActivityOrder order = orderDao.loadByOid(currentOrderId);
//        if(order == null){
//            return;
//        }
//
//        Integer day = SafeConverter.toInt(DateUtils.dateToString(attendTime, "yyyyMMdd"));
//
//        DayRange dayRange = DayRange.newInstance(attendTime.getTime());
//        Date today = dayRange.getStartDate();
//
//        ActivityExtend extend = activityExtendDao.loadByAid(order.getActivityId());
//        int meetConditionDays = extend == null ? 1 : SafeConverter.toInt(extend.getMeetConditionDays(), 1);
//
//
//        List<ActivityOrderStudent> studentList = orderStudentDao.loadByOid(currentOrderId);
//        if(CollectionUtils.isNotEmpty(studentList)){
//            List<ActivityOrderStudent> targetStudentList = studentList.stream().filter(s -> Objects.equals(s.getStudentId(), studentId)).collect(Collectors.toList());
//            if(CollectionUtils.isNotEmpty(targetStudentList)){
//                targetStudentList.forEach(orderStudent -> {
//                    if(orderStudent.getAttendClassLatestDay() == null || !Objects.equals(orderStudent.getAttendClassLatestDay(), day)){
//                        orderStudent.setAttendClassLatestDay(day);
//                        orderStudent.setAttendClassDayCount(SafeConverter.toInt(orderStudent.getAttendClassDayCount()) + 1);
//                        orderStudentDao.replace(orderStudent);
//                    }
//                });
//            }
//        }
//
//        List<ActivityOrder> orderList = orderDao.loadByAidAndOrderUserId(order.getActivityId(), order.getOrderUserId());
//        Set<String> orderIds = orderList.stream().map(ActivityOrder::getOrderId).collect(Collectors.toSet());
//
//        Map<String, Integer> beforeTodayAttendDayCount = new HashMap<>();   // 今日之前上课天数
//        Map<String, Integer> todayAttendCount = new HashMap<>();      // 今天上课次数
//
//        List<ActivityAttendCourse> allAttendCourseList = new ArrayList<>();
//        for(String orderId : orderIds) {
//
//            List<ActivityOrderCourse> orderCourses = orderCourseDao.loadByOid(orderId);
//            Set<String> courseIds = orderCourses.stream().filter(p -> Objects.equals(p.getStudentId(), studentId)).map(ActivityOrderCourse::getCourseId).collect(Collectors.toSet());
//
//            if(CollectionUtils.isEmpty(courseIds)){
//                continue;
//            }
//
//
//            List<ActivityAttendCourse> attendCourseList = new ArrayList<>();
//            courseIds.forEach(c -> {
//                List<ActivityAttendCourse> tmpList = attendCourseDao.loadBySidAndCid(studentId, c);
//                if (CollectionUtils.isNotEmpty(tmpList)) {
//                    attendCourseList.addAll(tmpList);
//                    allAttendCourseList.addAll(tmpList);
//                }
//            });
//
//            List<ActivityAttendCourse> beforeTodayList = attendCourseList.stream().filter(t -> t.getAttendTime().before(today)).collect(Collectors.toList());
//            Set<Integer> attendDays = beforeTodayList.stream().map(p -> SafeConverter.toInt(DateUtils.dateToString(p.getAttendTime(), "yyyyMMdd"))).collect(Collectors.toSet());
//            beforeTodayAttendDayCount.put(orderId, attendDays.size());
//            // 今天参加了多少次课， 学生首次参加才进行统计， 重复参加不进行计数
//            int todayCount = (int)attendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
//            todayAttendCount.put(orderId, todayCount);
//        }
//
//        ActivityAttendCourseStatistics statistics = activityAttendCourseStatisticsDao.loadByUidAndDay(order.getActivityId(), order.getUserId(), day);
//        if (statistics == null) {
//            statistics = new ActivityAttendCourseStatistics();
//            statistics.setActivityId(order.getActivityId());
//            statistics.setUserId(order.getUserId());
//            statistics.setUserName(order.getUserName());
//            statistics.setDay(day);
//        }
//
//        long todayCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
//        if(todayCount == 1){
//            statistics.setAttendStuCount(SafeConverter.toInt(statistics.getAttendStuCount()) + 1);
//            long beforeCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().before(today)).count();
//            if(beforeCount == 0 ){
//                statistics.setFirstAttendStuCount(SafeConverter.toInt(statistics.getFirstAttendStuCount()) + 1);
//            }
//        }
//
//        int count = 0;
//        boolean isCurrentOrder = false;
//        for(String k : beforeTodayAttendDayCount.keySet()){
//            if(SafeConverter.toInt(beforeTodayAttendDayCount.get(k)) == (meetConditionDays - 1)){
//                int t = SafeConverter.toInt(todayAttendCount.get(k));
//                if(t == 1){
//                    count ++;
//                    if(Objects.equals(currentOrderId, k)){
//                        isCurrentOrder = true;
//                    }
//                }
//            }
//        }
//
//        if(count == 1 && isCurrentOrder){
//            statistics.setMeetConditionStuCount(SafeConverter.toInt(statistics.getMeetConditionStuCount()) + 1);
//        }
//        activityAttendCourseStatisticsDao.upsert(statistics);
//
//    }
//
//
//    private void attendCourseStatisticsCard(String cardNo, Long studentId, Date attendTime){
//        ActivityCard card = cardDao.loadByCn(cardNo);
//        if(card == null){
//            return;
//        }
//
//        Integer day = SafeConverter.toInt(DateUtils.dateToString(attendTime, "yyyyMMdd"));
//
//        DayRange dayRange = DayRange.newInstance(attendTime.getTime());
//        Date today = dayRange.getStartDate();
//
//        ActivityExtend extend = activityExtendDao.loadByAid(card.getActivityId());
//        int meetConditionDays = extend == null ? 1 : SafeConverter.toInt(extend.getMeetConditionDays(), 1);
//
//        List<ActivityCard> cardList = cardDao.loadByAidAndCardUserId(card.getActivityId(), card.getCardUserId());
//
//        Set<String> cardNos = cardList.stream().map(ActivityCard::getCardNo).collect(Collectors.toSet());
//
//        Map<String, Integer> beforeTodayAttendDayCount = new HashMap<>();   // 今日之前上课天数
//        Map<String, Integer> todayAttendCount = new HashMap<>();      // 今天上课次数
//
//        List<ActivityAttendCourse> allAttendCourseList = new ArrayList<>();
//        for(String tmpCardNo : cardNos) {
//
//            List<ActivityCardCourse> cardCourses = cardCourseDao.loadByOid(tmpCardNo);
//            Set<String> courseIds = cardCourses.stream().filter(p -> Objects.equals(p.getStudentId(), studentId)).map(ActivityCardCourse::getCourseId).collect(Collectors.toSet());
//
//            if(CollectionUtils.isEmpty(courseIds)){
//                continue;
//            }
//
//
//            List<ActivityAttendCourse> attendCourseList = new ArrayList<>();
//            courseIds.forEach(c -> {
//                List<ActivityAttendCourse> tmpList = attendCourseDao.loadBySidAndCid(studentId, c);
//                if (CollectionUtils.isNotEmpty(tmpList)) {
//                    attendCourseList.addAll(tmpList);
//                    allAttendCourseList.addAll(tmpList);
//                }
//            });
//
//            List<ActivityAttendCourse> beforeTodayList = attendCourseList.stream().filter(t -> t.getAttendTime().before(today)).collect(Collectors.toList());
//            Set<Integer> attendDays = beforeTodayList.stream().map(p -> SafeConverter.toInt(DateUtils.dateToString(p.getAttendTime(), "yyyyMMdd"))).collect(Collectors.toSet());
//            beforeTodayAttendDayCount.put(tmpCardNo, attendDays.size());
//            // 今天参加了多少次课， 学生首次参加才进行统计， 重复参加不进行计数
//            int todayCount = (int)attendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
//            todayAttendCount.put(tmpCardNo, todayCount);
//        }
//
//        ActivityAttendCourseStatistics statistics = activityAttendCourseStatisticsDao.loadByUidAndDay(card.getActivityId(), card.getUserId(), day);
//        if (statistics == null) {
//            statistics = new ActivityAttendCourseStatistics();
//            statistics.setActivityId(card.getActivityId());
//            statistics.setUserId(card.getUserId());
//            statistics.setUserName(card.getUserName());
//            statistics.setDay(day);
//        }
//
//        long todayCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().after(today)).count();
//        if(todayCount == 1){
//            statistics.setAttendStuCount(SafeConverter.toInt(statistics.getAttendStuCount()) + 1);
//            long beforeCount = allAttendCourseList.stream().filter(t -> t.getAttendTime().before(today)).count();
//            if(beforeCount == 0 ){
//                statistics.setFirstAttendStuCount(SafeConverter.toInt(statistics.getFirstAttendStuCount()) + 1);
//            }
//        }
//
//        int count = 0;
//        boolean isCurrentCard = false;
//        for(String k : beforeTodayAttendDayCount.keySet()){
//            if(SafeConverter.toInt(beforeTodayAttendDayCount.get(k)) == (meetConditionDays - 1)){
//                int t = SafeConverter.toInt(todayAttendCount.get(k));
//                if(t == 1){
//                    count ++;
//                    if(Objects.equals(cardNo, k)){
//                        isCurrentCard = true;
//                    }
//                }
//            }
//        }
//
//        if(count == 1 && isCurrentCard){
//            statistics.setMeetConditionStuCount(SafeConverter.toInt(statistics.getMeetConditionStuCount()) + 1);
//        }
//        activityAttendCourseStatisticsDao.upsert(statistics);
//
//    }


    public void attendCourseStatisticsByUid(String activityId, Long userId, Collection<Integer> days){
        if(CollectionUtils.isEmpty(days)){
            return;
        }
//        Integer maxDay = days.stream().max(Comparator.comparing(Function.identity())).get();
//        Integer minDay = days.stream().min(Comparator.comparing(Function.identity())).get();
//
//        Date endDate = DateUtils.stringToDate(String.valueOf(maxDay), "yyyyMMdd");
//        Date startDate = DateUtils.stringToDate(String.valueOf(minDay), "yyyyMMdd");
//
//        List<ActivityOrder> orderList = orderDao.loadByActivityAndUserAndTime(activityId, Collections.singleton(userId), startDate, endDate);
//        if(CollectionUtils.isEmpty(orderList)){
//            return;
//        }
//
//        String userName = "";
//
//        List<ActivityAttendCourse> attendCourseList = new ArrayList<>();
//        for(ActivityOrder order : orderList){
//            if(StringUtils.isBlank(userName) && StringUtils.isNotBlank(order.getUserName())){
//                userName = order.getUserName();
//            }
//            List<ActivityOrderStudent> studentList = orderStudentDao.loadByOid(order.getOrderId());
//            List<ActivityOrderCourse> orderCourses = orderCourseDao.loadByOid(order.getOrderId());
//            if(CollectionUtils.isNotEmpty(studentList) && CollectionUtils.isNotEmpty(orderCourses)){
//                Set<String> courseIds = orderCourses.stream().map(ActivityOrderCourse::getCourseId).collect(Collectors.toSet());
//                Set<Long> studentIds = studentList.stream().map(ActivityOrderStudent::getStudentId).collect(Collectors.toSet());
//                studentIds.forEach(s -> {
//                    courseIds.forEach(c -> {
//                        List<ActivityAttendCourse> tmpList = attendCourseDao.loadBySidAndCid(s, c);
//                        if(CollectionUtils.isNotEmpty(tmpList)){
//                            attendCourseList.addAll(tmpList);
//                        }
//                    });
//                });
//            }
//        }
//
//        if(CollectionUtils.isEmpty(attendCourseList)){
//            return;
//        }
//
//        for(Integer day : days){
//
//            Date dayStart = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
//            Date dayEnd = DateUtils.addDays(dayStart, 1);
//
//            List<ActivityAttendCourse> dayAttendList = attendCourseList.stream().filter(p -> !p.getAttendTime().before(dayStart) && p.getAttendTime().before(dayEnd)).collect(Collectors.toList());
//            if(CollectionUtils.isEmpty(dayAttendList)){
//                continue;
//            }
//
//
//            ActivityAttendCourseStatistics statistics = activityAttendCourseStatisticsDao.loadByUidAndDay(activityId, userId, day);
//            if(statistics == null){
//                statistics = new ActivityAttendCourseStatistics();
//                statistics.setActivityId(activityId);
//                statistics.setUserId(userId);
//                statistics.setUserName(userName);
//                statistics.setDay(day);
//            }
//
//            Set<Long> studentIds = dayAttendList.stream().map(ActivityAttendCourse::getStudentId).collect(Collectors.toSet());
//            statistics.setAttendStuCount(studentIds.size());
//
//            List<ActivityAttendCourse> preAttendList = attendCourseList.stream()
//                    .filter(p -> studentIds.contains(p.getStudentId()) && p.getAttendTime().before(dayStart))
//                    .collect(Collectors.toList());
//
//            Set<Long> preStudentIds = preAttendList.stream().map(ActivityAttendCourse::getStudentId).collect(Collectors.toSet());
//            statistics.setFirstAttendStuCount(studentIds.size() - preStudentIds.size());
//
//            Map<Long, Set<Integer>> studentAttendDays =  preAttendList.stream().collect(Collectors.groupingBy(ActivityAttendCourse::getStudentId, Collectors.mapping(t -> SafeConverter.toInt(DateUtils.dateToString(t.getAttendTime(), "yyyyMMdd")), Collectors.toSet())));
//
//            int meetConditionStuCount = 0;
//            for(Set<Integer> attendDays : studentAttendDays.values()){
//                if(attendDays.size() == 2){
//                    meetConditionStuCount += 1;
//                }
//            }
//            statistics.setMeetConditionStuCount(meetConditionStuCount);
//
//            activityAttendCourseStatisticsDao.upsert(statistics);
//        }
    }


    public List<ActivityAttendCourseStatisticsView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityAttendCourseStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<ActivityAttendCourseStatisticsView>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupDataView(activityId, groupId, days)));
        }

        for(Future<ActivityAttendCourseStatisticsView> future : futureList) {
            try {
                ActivityAttendCourseStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityAttendCourseStatisticsView getGroupDataView(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<ActivityAttendCourseStatistics> totalDataList = new ArrayList<>();
        List<ActivityAttendCourseStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                List<ActivityAttendCourseStatistics> dataList = activityAttendCourseStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                    List<ActivityAttendCourseStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(tempDataList)){
                        targetDayDataList.addAll(tempDataList);
                    }
                }
            }
        }

        ActivityAttendCourseStatisticsView view = new ActivityAttendCourseStatisticsView();
        view.setId(group.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_GROUP);
        view.setName(group.getGroupName());
        int dayFirstAttendStuCount = 0;
        int dayMeetConditionStuCount = 0;
        for(ActivityAttendCourseStatistics statistics : targetDayDataList){
            dayFirstAttendStuCount += SafeConverter.toInt(statistics.getFirstAttendStuCount());
            dayMeetConditionStuCount += SafeConverter.toInt(statistics.getMeetConditionStuCount());
        }
        view.setDayFirstAttendStuCount(dayFirstAttendStuCount);
        view.setDayMeetConditionStuCount(dayMeetConditionStuCount);

        int totalAttendStuCount = 0;
        int totalMeetConditionStuCount = 0;
        for(ActivityAttendCourseStatistics statistics : totalDataList){
            totalAttendStuCount += SafeConverter.toInt(statistics.getFirstAttendStuCount());
            totalMeetConditionStuCount += SafeConverter.toInt(statistics.getMeetConditionStuCount());
        }
        view.setTotalAttendStuCount(totalAttendStuCount);
        view.setTotalMeetConditionStuCount(totalMeetConditionStuCount);
        return view;
    }



    public List<ActivityAttendCourseStatisticsView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityAttendCourseStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<ActivityAttendCourseStatisticsView>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserDataView(activityId, userId, days)));
        }
        for(Future<ActivityAttendCourseStatisticsView> future : futureList) {
            try {
                ActivityAttendCourseStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }


    public ActivityAttendCourseStatisticsView getUserDataView(String activityId, Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<ActivityAttendCourseStatistics> totalDataList = new ArrayList<>();
        List<ActivityAttendCourseStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){
            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();
            List<ActivityAttendCourseStatistics> dataList = activityAttendCourseStatisticsDao.loadByUsersAndDays(activityId, Collections.singleton(user.getId()), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                totalDataList.addAll(dataList);
                List<ActivityAttendCourseStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempDataList)){
                    targetDayDataList.addAll(tempDataList);
                }
            }
        }

        ActivityAttendCourseStatisticsView view = new ActivityAttendCourseStatisticsView();
        view.setId(user.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_USER);
        view.setName(user.getRealName());

        int dayFirstAttendStuCount = 0;
        int dayMeetConditionStuCount = 0;
        for(ActivityAttendCourseStatistics statistics : targetDayDataList){
            dayFirstAttendStuCount += SafeConverter.toInt(statistics.getFirstAttendStuCount());
            dayMeetConditionStuCount += SafeConverter.toInt(statistics.getMeetConditionStuCount());
        }
        view.setDayFirstAttendStuCount(dayFirstAttendStuCount);
        view.setDayMeetConditionStuCount(dayMeetConditionStuCount);

        int totalAttendStuCount = 0;
        int totalMeetConditionStuCount = 0;
        for(ActivityAttendCourseStatistics statistics : totalDataList){
            totalAttendStuCount += SafeConverter.toInt(statistics.getFirstAttendStuCount());
            totalMeetConditionStuCount += SafeConverter.toInt(statistics.getMeetConditionStuCount());
        }
        view.setTotalAttendStuCount(totalAttendStuCount);
        view.setTotalMeetConditionStuCount(totalMeetConditionStuCount);
        return view;
    }

}
