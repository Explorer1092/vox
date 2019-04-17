package com.voxlearning.utopia.agent.service.activity.palace;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.palace.PalaceActivityRecordDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.palace.PalaceActivityUserStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.persist.entity.activity.palace.PalaceActivityRecord;
import com.voxlearning.utopia.agent.persist.entity.activity.palace.PalaceActivityUserStatistics;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.activity.ActivityCouponOrderCourseStatisticsView;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceDataView;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceRankingView;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceRecordDataView;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.parent.api.StudyTogetherHulkService;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class PalaceActivityService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private AgentActivityDao agentActivityDao;

    @Inject
    private PalaceActivityRecordDao palaceActivityRecordDao;
    @Inject
    private PalaceActivityUserStatisticsDao userStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;

    @ImportService(interfaceClass = StudyTogetherHulkService.class)
    private StudyTogetherHulkService studyTogetherHulkService;


    public void resolveCouponData(String activityId, String couponId, String couponName, Long couponUserId, Date couponTime, Long userId){
        if(StringUtils.isBlank(activityId) || StringUtils.isBlank(couponId) || couponUserId == null || userId == null){
            return;
        }

        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return;
        }

        User platformUser = userLoaderClient.loadUser(couponUserId);
        if(platformUser == null){
            return;
        }

        List<PalaceActivityRecord> dataList = palaceActivityRecordDao.loadByCoupon(couponId, couponUserId);
        if(CollectionUtils.isNotEmpty(dataList)){
            return;
        }
        if(couponTime == null){
            couponTime = new Date();
        }

        PalaceActivityRecord item = new PalaceActivityRecord();

        item.setActivityId(activityId);

        item.setCouponId(couponId);
        item.setCouponName(couponName);
        item.setCouponUserId(couponUserId);
        item.setCouponTime(couponTime);

        Date date = DateUtils.stringToDate("20190124", "yyyyMMdd");
        item.setIsNewUser(platformUser.getCreateTime().after(date));

        item.setBusinessTime(couponTime);
        item.setUserId(userId);
        AgentUser agentUser = baseOrgService.getUser(userId);
        if(agentUser != null){
            item.setUserName(agentUser.getRealName());
        }
        palaceActivityRecordDao.insert(item);
        AlpsThreadPool.getInstance().submit(() -> couponStatistics(item));
    }

    private PalaceActivityUserStatistics getUserStatisticsData(PalaceActivityRecord data){
        if(data == null){
            return null;
        }
        Integer day = SafeConverter.toInt(DateUtils.dateToString(data.getCouponTime(), "yyyyMMdd"));
        PalaceActivityUserStatistics statistics = userStatisticsDao.loadByUserAndDay(data.getActivityId(), data.getUserId(), day);
        if(statistics == null){
            statistics = new PalaceActivityUserStatistics();
            statistics.setActivityId(data.getActivityId());
            statistics.setUserId(data.getUserId());
            statistics.setUserName(data.getUserName());
            statistics.setDay(day);
        }
        return statistics;
    }

    private void couponStatistics(PalaceActivityRecord data){
        if(data == null){
            return;
        }
        PalaceActivityUserStatistics statistics = getUserStatisticsData(data);
        if(statistics != null){
            statistics.setCouponCount(SafeConverter.toInt(statistics.getCouponCount()) + 1);
            userStatisticsDao.upsert(statistics);
        }
    }

    public void resolveOrderData(String orderId, Date orderPayTime, BigDecimal orderPayAmount, Long orderUserId){
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if(userOrder == null || StringUtils.isBlank(userOrder.getCouponRefId())){
            return;
        }
        CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(userOrder.getCouponRefId());
        if(couponUserRef == null){
            return;
        }

        List<PalaceActivityRecord> dataList = palaceActivityRecordDao.loadByCoupon(couponUserRef.getCouponId(), couponUserRef.getUserId());
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }

        PalaceActivityRecord data = dataList.get(0);
        // 数据已更新
        if(StringUtils.isNotBlank(data.getOrderId())){
            return;
        }


        data.setOrderId(orderId);
        data.setOrderPayTime(orderPayTime);
        data.setOrderAmount(userOrder.getOrderPrice());
        data.setOrderPayAmount(orderPayAmount);
        data.setOrderUserId(orderUserId);


        Long studentId = studyTogetherHulkService.loadStudyTogetherOrderSid(orderId);           // 根据订单ID及家长ID获取学生ID
        if(studentId != null){
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if(studentDetail != null){
                data.setStudentId(studentDetail.getId());
                if(studentDetail.getProfile() != null){
                    data.setStudentName(studentDetail.getProfile().getRealname());
                }
                if(studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null){
                    Long schoolId = studentDetail.getClazz().getSchoolId();
                    School school =schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
                    if(school != null){
                        data.setSchoolId(schoolId);
                        data.setSchoolName(school.getCname());
                    }
                }
            }
        }
        palaceActivityRecordDao.replace(data);
        AlpsThreadPool.getInstance().submit(() -> orderStatistics(data));
    }

    private void orderStatistics(PalaceActivityRecord data){
        if(data == null){
            return;
        }
        PalaceActivityUserStatistics statistics = getUserStatisticsData(data);
        if(statistics != null){
            statistics.setOrderCount(SafeConverter.toInt(statistics.getOrderCount()) + 1);
            userStatisticsDao.upsert(statistics);
        }
    }

    public void resolveAttendClassData(Long studentId){
        List<PalaceActivityRecord> dataList = palaceActivityRecordDao.loadByStudentId(studentId);
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }

        Integer today = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMMdd"));

        int maxDayCount = 0;
        int maxCourseCount = 0;
        for(PalaceActivityRecord p : dataList){
            if(p.getAttendClassLatestDay() == null || !Objects.equals(p.getAttendClassLatestDay(), today)){
                p.setAttendClassLatestDay(today);
                p.setAttendClassDayCount(SafeConverter.toInt(p.getAttendClassDayCount()) + 1);
                if(p.getAttendClassDayCount() > maxDayCount){
                    maxDayCount = p.getAttendClassDayCount();
                }
            }
            p.setAttendClassCourseCount(SafeConverter.toInt(p.getAttendClassCourseCount()) + 1);
            if(p.getAttendClassCourseCount() > maxCourseCount){
                maxCourseCount = p.getAttendClassCourseCount();
            }
            palaceActivityRecordDao.replace(p);
        }

        boolean firstAttendClass = maxCourseCount == 1;
        boolean meetCondition = maxDayCount == 3;
        AlpsThreadPool.getInstance().submit(() -> attendClassStatistics(dataList.get(0), firstAttendClass, meetCondition));
    }

    private void attendClassStatistics(PalaceActivityRecord data, boolean firstAttendClass, boolean meetCondition){
        if(data == null || (!firstAttendClass && !meetCondition)){
            return;
        }
        PalaceActivityUserStatistics statistics = getUserStatisticsData(data);
        if(statistics != null){
            if(firstAttendClass){
                statistics.setAttendClassStuCount(SafeConverter.toInt(statistics.getAttendClassStuCount()) + 1);
            }
            if(meetCondition){
                statistics.setMeetConditionStuCount(SafeConverter.toInt(statistics.getMeetConditionStuCount()) + 1);
            }
            userStatisticsDao.upsert(statistics);
        }
    }


    public PalaceDataView getUserOverview(String activityId, Long userId){
        List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        PalaceDataView result = null;
        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isNotEmpty(groupIds)){
            Long groupId = groupIds.get(0);
            List<PalaceDataView> dataList = getGroupDataView(activityId, Collections.singleton(groupId), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                result = dataList.stream().filter(p -> Objects.equals(p.getId(), groupId)).findFirst().orElse(null);
            }
        }else {
            List<PalaceDataView> dataList = getUserDataView(activityId, Collections.singleton(userId), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                result =dataList.stream().filter(p -> Objects.equals(p.getId(), userId)).findFirst().orElse(null);
            }
        }
        return result;
    }


    public List<Integer> getEveryDays(Date startDate, Date endDate){
        if(endDate == null){
            endDate = new Date();
        }
        if(startDate == null){
            startDate = endDate;
        }

        Set<Integer> days = new LinkedHashSet<>();
        Date tempDate = startDate;
        while(!tempDate.after(endDate)){
            days.add(SafeConverter.toInt(DateUtils.dateToString(tempDate, "yyyyMMdd")));
            tempDate = DateUtils.addDays(tempDate, 1);
        }
        return new ArrayList<>(days);
    }

    private List<Integer> getActivityEveryDays(String activityId){
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return new ArrayList<>();
        }
        Date endDate = new Date();
        if(activity.getEndDate() != null && activity.getEndDate().before(endDate)){
            endDate = activity.getEndDate();
        }
        return getEveryDays(activity.getStartDate(), endDate);
    }

    public List<PalaceDataView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<PalaceDataView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        Integer tmpDay = null;
        if(CollectionUtils.isNotEmpty(days)){
            tmpDay = days.stream().max(Comparator.comparing(Function.identity())).get();
        }
        Integer targetDay = tmpDay;

        List<AgentGroup> groupList = baseOrgService.getGroupByIds(groupIds);
        groupList.forEach(p -> {
            List<PalaceActivityUserStatistics> totalDataList = new ArrayList<>();
            List<PalaceActivityUserStatistics> targetDayDataList = new ArrayList<>();

            if(CollectionUtils.isNotEmpty(days)){
                List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(p.getId());
                List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(userIds)){
                    List<PalaceActivityUserStatistics> dataList = userStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                    if(CollectionUtils.isNotEmpty(dataList)){
                        totalDataList.addAll(dataList);
                        List<PalaceActivityUserStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                        if(CollectionUtils.isNotEmpty(tempDataList)){
                            targetDayDataList.addAll(tempDataList);
                        }
                    }
                }
            }

            PalaceDataView dataView = new PalaceDataView();
            dataView.setId(p.getId());
            dataView.setIdType(AgentConstants.INDICATOR_TYPE_GROUP);
            dataView.setName(p.getGroupName());
            int todayCouponCount = 0;
            for(PalaceActivityUserStatistics userStatistics: targetDayDataList){
                todayCouponCount += SafeConverter.toInt(userStatistics.getCouponCount());
            }
            dataView.setDayCouponCount(todayCouponCount);

            int totalCouponCount = 0;
            int totalOrderCount = 0;
            int totalAttendClassStuCount = 0;
            int totalMeetConditionStuCount = 0;
            for(PalaceActivityUserStatistics data: totalDataList){
                totalCouponCount += SafeConverter.toInt(data.getCouponCount());
                totalOrderCount += SafeConverter.toInt(data.getOrderCount());
                totalAttendClassStuCount += SafeConverter.toInt(data.getAttendClassStuCount());
                totalMeetConditionStuCount += SafeConverter.toInt(data.getMeetConditionStuCount());
            }
            dataView.setTotalCouponCount(totalCouponCount);
            dataView.setTotalOrderCount(totalOrderCount);
            dataView.setTotalAttendClassStuCount(totalAttendClassStuCount);
            dataView.setTotalMeetConditionStuCount(totalMeetConditionStuCount);

            resultList.add(dataView);
        });

        return resultList;
    }

    public List<PalaceDataView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<PalaceDataView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }


        Integer tmpDay = null;
        if(CollectionUtils.isNotEmpty(days)){
            tmpDay = days.stream().max(Comparator.comparing(Function.identity())).get();
        }
        Integer targetDay = tmpDay;

        List<AgentUser> userList = baseOrgService.getUsers(userIds);


        List<PalaceActivityUserStatistics> dataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(days)){
            List<PalaceActivityUserStatistics> allDataList = userStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
            if(CollectionUtils.isNotEmpty(allDataList)){
                dataList.addAll(allDataList);
            }
        }

        for(AgentUser user : userList){
            List<PalaceActivityUserStatistics> totalDataList = dataList.stream().filter(p -> Objects.equals(p.getUserId(), user.getId())).collect(Collectors.toList());
            List<PalaceActivityUserStatistics> targetDayDataList = totalDataList.stream().filter(p -> Objects.equals(targetDay, p.getDay())).collect(Collectors.toList());

            PalaceDataView dataView = new PalaceDataView();
            dataView.setId(user.getId());
            dataView.setIdType(AgentConstants.INDICATOR_TYPE_USER);
            dataView.setName(user.getRealName());
            int todayCouponCount = 0;
            for(PalaceActivityUserStatistics userStatistics: targetDayDataList){
                todayCouponCount += SafeConverter.toInt(userStatistics.getCouponCount());
            }
            dataView.setDayCouponCount(todayCouponCount);

            int totalCouponCount = 0;
            int totalOrderCount = 0;
            int totalAttendClassStuCount = 0;
            int totalMeetConditionStuCount = 0;
            for(PalaceActivityUserStatistics data: totalDataList){
                totalCouponCount += SafeConverter.toInt(data.getCouponCount());
                totalOrderCount += SafeConverter.toInt(data.getOrderCount());
                totalAttendClassStuCount += SafeConverter.toInt(data.getAttendClassStuCount());
                totalMeetConditionStuCount += SafeConverter.toInt(data.getMeetConditionStuCount());
            }
            dataView.setTotalCouponCount(totalCouponCount);
            dataView.setTotalOrderCount(totalOrderCount);
            dataView.setTotalAttendClassStuCount(totalAttendClassStuCount);
            dataView.setTotalMeetConditionStuCount(totalMeetConditionStuCount);

            resultList.add(dataView);
        }
        return resultList;
    }


    public List<PalaceRecordDataView> getRecordList(String activityId, Long userId){
        List<PalaceRecordDataView> resultList = new ArrayList<>();
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return resultList;
        }

        Date endDate = activity.getEndDate() == null ? new Date() : activity.getEndDate();
        Date startDate = activity.getStartDate() == null? DateUtils.addMonths(endDate, -3) : activity.getStartDate();

        List<PalaceActivityRecord> recordList = palaceActivityRecordDao.loadByActivityAndUser(activityId, userId);
        if(CollectionUtils.isNotEmpty(recordList)){
            recordList = recordList.stream().filter(p -> p.getBusinessTime() != null && p.getBusinessTime().after(startDate) && p.getBusinessTime().before(endDate)).collect(Collectors.toList());
        }

        if(CollectionUtils.isEmpty(recordList)){
            return resultList;
        }

        recordList.forEach(p -> {
            PalaceRecordDataView dataView = new PalaceRecordDataView();
            dataView.setUserId(p.getCouponUserId());
            dataView.setUserName("");                       // TODO: 2019/1/20
            dataView.setCouponTime(p.getCouponTime());
            dataView.setIsNewUser(SafeConverter.toBoolean(p.getIsNewUser()));
            dataView.setMobile(sensitiveUserDataServiceClient.loadUserMobileObscured(p.getCouponUserId()));
            dataView.setStudentId(p.getStudentId());
            dataView.setStudentName(p.getStudentName());
            dataView.setSchoolId(p.getSchoolId());
            dataView.setSchoolName(p.getSchoolName());

            dataView.setHasOrder(StringUtils.isNotBlank(p.getOrderId()));
            dataView.setAttendClass(SafeConverter.toInt(p.getAttendClassDayCount()) > 0);

            dataView.setBusinessTime(p.getBusinessTime());

            resultList.add(dataView);
        });

        return resultList;
    }


    public Map<String, Integer> getChartInfo(String activityId, Long userId){
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return resultMap;
        }

        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if(CollectionUtils.isNotEmpty(managedGroupIds)){
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            userIds.addAll(groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
        }

        List<PalaceActivityUserStatistics> dataList = userStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
        Map<Integer, Integer> dayCouponCountMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(dataList)) {
            dayCouponCountMap = dataList.stream().collect(Collectors.groupingBy(PalaceActivityUserStatistics::getDay, Collectors.summingInt(p -> SafeConverter.toInt(p.getCouponCount()))));
        }
        for(Integer d : days){
            String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(d), "yyyyMMdd"), "MM/dd");
            resultMap.put(key, SafeConverter.toInt(dayCouponCountMap.get(d)));
        }
        return resultMap;
    }

    // dateType: 1 当日 2 累计
    // rankingType: 1 专员 2 分区
    public List<PalaceRankingView> getRankingList(String activityId, Integer dateType, Integer rankingType, Integer topN, Long userId){

        List<PalaceRankingView> rankingDataList = new ArrayList<>();

        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return rankingDataList;
        }
        Integer day = days.stream().max(Comparator.comparing(Function.identity())).get();

        List<PalaceDataView> dataViewList = new ArrayList<>();
        if(rankingType == 1) { // 专员榜
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
            List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(dateType == 1){ // 日榜
                dataViewList.addAll(getUserDataView(activityId, userIds, Collections.singleton(day)));
            }else if(dateType == 2){ // 累计
                dataViewList.addAll(getUserDataView(activityId, userIds, days));
            }
        }else {  // 分区榜
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
            List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            if(dateType == 1) { // 日榜
                dataViewList.addAll(getGroupDataView(activityId, groupIdList, Collections.singleton(day)));
            } else if(dateType == 2){ // 累计
                dataViewList.addAll(getGroupDataView(activityId, groupIdList, days));
            }
        }

        dataViewList = dataViewList.stream()
                .filter(p -> (dateType == 1 && p.getDayCouponCount() > 0) || (dateType == 2 && p.getTotalCouponCount() > 0))
                .sorted((o1, o2) -> {
                    if(dateType == 1){
                        return Integer.compare(SafeConverter.toInt(o2.getDayCouponCount()), SafeConverter.toInt(o1.getDayCouponCount()));
                    }else {
                        return Integer.compare(SafeConverter.toInt(o2.getTotalCouponCount()), SafeConverter.toInt(o1.getTotalCouponCount()));
                    }
                })
                .collect(Collectors.toList());

        int ranking = 0;
        int preAmount = 0;
        for(int i = 0; i< dataViewList.size(); i++){
            PalaceDataView dataView = dataViewList.get(i);
            Integer dataValue = 0;
            if(dateType == 1){
                dataValue = dataView.getDayCouponCount();
            }else if(dateType == 2){
                dataValue = dataView.getTotalCouponCount();
            }
            if(ranking < topN) {
                if (preAmount != dataValue) {
                    preAmount = dataValue;
                    ranking++;
                }
                PalaceRankingView rankingView = new PalaceRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }else {
                if (preAmount != dataValue) {
                    break;
                }
                PalaceRankingView rankingView = new PalaceRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }
        }
        return rankingDataList;
    }


    // dimension 1: 默认， 2：专员， 3分区， 4区域， 5大区
    public List<PalaceDataView> getStatisticsDataList(String activityId, Long id, Integer idType, Integer dimension){
        List<PalaceDataView> dataList = new ArrayList<>();
        if(idType.equals(AgentConstants.INDICATOR_TYPE_USER)){
            return dataList;
        }

        List<Integer> days = getActivityEveryDays(activityId);

        AgentGroup group = baseOrgService.getGroupById(id);
        if(group == null){
            return dataList;
        }
        if(dimension == 2 || (dimension == 1 && group.fetchGroupRoleType() == AgentGroupRoleType.City)){
            // 专员的情况下
            List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(),AgentRoleType.BusinessDeveloper.getId());
            dataList.addAll(getUserDataView(activityId, userIds, days));
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
            List<Long> groupIds = groups.stream().map(AgentGroup::getId).collect(Collectors.toList());
            dataList.addAll(getGroupDataView(activityId, groupIds, days));
        }
        return dataList;

    }

    // 公司非市场部数据统计
    // dimension 1: 默认， 2：专员
    public List<PalaceDataView> getOfficeDataList(String activityId, Long id, Integer idType, Integer dimension){
        List<PalaceDataView> dataList = new ArrayList<>();
        if(idType.equals(AgentConstants.INDICATOR_TYPE_USER)){
            return dataList;
        }

        List<Integer> days = getActivityEveryDays(activityId);

        AgentGroup group = baseOrgService.getGroupById(id);
        if(group == null){
            return dataList;
        }
        if(dimension == 2 || (dimension == 1 && (Objects.equals(group.getParentId(), 1598L) || Objects.equals(group.getParentId(), 329L)))){
            // 专员的情况下
            List<AgentGroupUser> groupUserList = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            dataList.addAll(getUserDataView(activityId, userIds, days));
        }else {
            List<AgentGroup> groups = baseOrgService.getSubGroupList(group.getId());
            List<Long> groupIds = groups.stream().map(AgentGroup::getId).collect(Collectors.toList());
            dataList.addAll(getGroupDataView(activityId, groupIds, days));
        }
        return dataList;
    }

    public void calAvgDataList(List<PalaceDataView> dataList){
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }

        dataList.forEach(p -> {
            if(Objects.equals(p.getIdType(), AgentConstants.INDICATOR_TYPE_USER)){
                p.setDayAvgCouponCount(SafeConverter.toDouble(p.getDayCouponCount()));
                p.setTotalAvgCouponCount(SafeConverter.toDouble(p.getTotalCouponCount()));
                p.setTotalAvgOrderCount(SafeConverter.toDouble(p.getTotalOrderCount()));
                p.setTotalAvgAttendClassStuCount(SafeConverter.toDouble(p.getTotalAttendClassStuCount()));
                p.setTotalAvgMeetConditionStuCount(SafeConverter.toDouble(p.getTotalMeetConditionStuCount()));
            }else if(Objects.equals(p.getIdType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                List<AgentGroupUser> groupUserList = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(p.getId(), AgentRoleType.BusinessDeveloper.getId());
                int size = groupUserList.size();
                if(size != 0){
                    p.setDayAvgCouponCount(MathUtils.doubleDivide(SafeConverter.toDouble(p.getDayCouponCount()), size, 1));
                    p.setTotalAvgCouponCount(MathUtils.doubleDivide(SafeConverter.toDouble(p.getTotalCouponCount()), size, 1));
                    p.setTotalAvgOrderCount(MathUtils.doubleDivide(SafeConverter.toDouble(p.getTotalOrderCount()), size, 1));
                    p.setTotalAvgAttendClassStuCount(MathUtils.doubleDivide(SafeConverter.toDouble(p.getTotalAttendClassStuCount()), size, 1));
                    p.setTotalAvgMeetConditionStuCount(MathUtils.doubleDivide(SafeConverter.toDouble(p.getTotalMeetConditionStuCount()), size, 1));
                }else {
                    p.setDayAvgCouponCount(0d);
                    p.setTotalAvgCouponCount(0d);
                    p.setTotalAvgOrderCount(0d);
                    p.setTotalAvgAttendClassStuCount(0d);
                    p.setTotalAvgMeetConditionStuCount(0d);
                }

            }
        });
    }


    public Map<String, Object> getNewUserData(String activityId, Long userId){

        Map<String, Object> dataMap = new HashMap<>();
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            dataMap.put("newUserCount", 0);
            dataMap.put("newUserRate", 0d);
            return dataMap;
        }

        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if(CollectionUtils.isNotEmpty(managedGroupIds)){
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            userIds.addAll(groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
        }

        Date endDate = new Date();
        if(activity.getEndDate() != null && activity.getEndDate().before(endDate)){
            endDate = activity.getEndDate();
        }
        Date startDate = null;
        if(activity.getStartDate() != null){
            startDate = activity.getStartDate();
        }

        Integer orderCount = 0;
        Integer newUserCount = 0;

        List<PalaceActivityRecord> recordList = palaceActivityRecordDao.loadByActivityAndUserAndTime(activityId, userIds, startDate, endDate);
        List<PalaceActivityRecord> orderList = recordList.stream().filter(p -> StringUtils.isNotBlank(p.getOrderId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(orderList)){
            orderCount = orderList.size();
            newUserCount = (int)(orderList.stream().filter(p -> SafeConverter.toBoolean(p.getIsNewUser())).count());
        }

        dataMap.put("newUserCount", newUserCount);
        dataMap.put("newUserRate", MathUtils.doubleDivide(newUserCount, orderCount));
        return dataMap;
    }

    public void updateUserStatisticData(String activityId, Collection<Long> userIds, Date startDate, Date endDate){
        if(StringUtils.isBlank(activityId) || CollectionUtils.isEmpty(userIds)){
            return;
        }
        if(endDate == null){
            endDate = new Date();
        }
        if(startDate == null){
            startDate = new Date();
        }
        List<PalaceActivityRecord> dataList = palaceActivityRecordDao.loadByActivityAndUserAndTime(activityId, userIds, startDate, endDate);
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        // 仅修改参与课程相关的数据
        Map<Long, List<PalaceActivityRecord>> userDataMap = dataList.stream().filter(p -> StringUtils.isNotBlank(p.getOrderId())).collect(Collectors.groupingBy(PalaceActivityRecord::getUserId));
        userDataMap.forEach((u, v) -> {
            Map<Integer, List<PalaceActivityRecord>> dayListMap = v.stream().collect(Collectors.groupingBy(p ->  SafeConverter.toInt(DateUtils.dateToString(p.getCouponTime(), "yyyyMMdd"))));
            dayListMap.forEach((d, list) -> {
                PalaceActivityUserStatistics statistics = userStatisticsDao.loadByUserAndDay(activityId, u, d);
                if(statistics != null){
                    int attendClassStuCount = (int)list.stream().filter(p -> SafeConverter.toInt(p.getAttendClassDayCount()) > 0).count();
                    statistics.setAttendClassStuCount(attendClassStuCount);

                    int meetConditionStuCount = (int)list.stream().filter(p -> SafeConverter.toInt(p.getAttendClassDayCount()) > 2).count();
                    statistics.setMeetConditionStuCount(meetConditionStuCount);

                    userStatisticsDao.replace(statistics);
                }
            });
        });
    }



    public List<PalaceDataView> getUserDataViewList(String activityId, Collection<Long> userIds){
        List<Integer> days = getActivityEveryDays(activityId);
        return getUserDataView(activityId, userIds, days);
    }






}
