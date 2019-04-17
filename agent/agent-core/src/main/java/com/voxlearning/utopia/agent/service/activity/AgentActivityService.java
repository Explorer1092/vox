package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.agent.constants.ActivityDataCategory;
import com.voxlearning.utopia.agent.constants.ActivityDataIndicator;
import com.voxlearning.utopia.agent.constants.AgentAuthorityType;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.*;
import com.voxlearning.utopia.agent.persist.entity.activity.*;
import com.voxlearning.utopia.agent.service.authority.AgentRecordAuthorityService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.view.activity.*;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AgentActivityService {

    @Inject
    private AgentActivityDao agentActivityDao;
    @Inject
    private ActivityExtendDao activityExtendDao;
    @Inject
    private ActivityIndicatorConfigDao indicatorConfigDao;
    @Inject
    private BaseOrgService baseOrgService;

    @Inject
    private ActivityCouponStatisticsService couponStatisticsService;
    @Inject
    private ActivityOrderStatisticsService orderStatisticsService;
    @Inject
    private ActivityAttendCourseStatisticsService attendCourseStatisticsService;
    @Inject
    private ActivityOrderUserStatisticsService orderUserStatisticsService;
    @Inject
    private ActivityCouponService couponService;
    @Inject
    private ActivityOrderStudentService orderStudentService;
    @Inject
    private ActivityOrderCourseService orderCourseService;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private ActivityOrderDao activityOrderDao;
    @Inject
    private ActivityOrderService orderService;
    @Inject
    private AgentGroupSupport agentGroupSupport;
    @Inject
    private ActivityGroupStatisticsService groupStatisticsService;
    @Inject
    private ActivityGroupUserStatisticsService groupUserStatisticsService;
    @Inject
    private ActivityGroupService groupService;
    @Inject
    private ActivityGroupUserService groupUserService;
    @Inject
    private ActivityControlDao activityControlDao;
    @Inject
    private ActivityCardStatisticsService cardStatisticsService;
    @Inject
    private ActivityOrderGiftDao activityOrderGiftDao;
    @Inject
    private AgentRecordAuthorityService recordAuthorityService;


    public List<ActivityView> getActivityList(Long userId, Date startDate){
        List<ActivityView> resultList = new ArrayList<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        if(userRole == null){
            return resultList;
        }
        List<AgentActivity> activityList = agentActivityDao.loadByStartDate(startDate).stream()
                .filter(p -> RuntimeMode.lt(Mode.PRODUCTION) || (RuntimeMode.isProduction() && SafeConverter.toBoolean(p.getIsShow())))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(activityList)){
            return resultList;
        }
        List<String> activityIds = activityList.stream().map(AgentActivity::getId).collect(Collectors.toList());
        Map<String, ActivityExtend> extendMap = activityExtendDao.loadByAids(activityIds);

        activityList.forEach(p -> {
            ActivityExtend extend = extendMap.get(p.getId());
            if(extend == null){
                return;
            }

            if(!recordAuthorityService.hasAuthority(p.getId(), AgentAuthorityType.ACTIVITY.getId(), userId)){
                return;
            }

            ActivityView view = new ActivityView();
            view.setId(p.getId());
            view.setName(p.getName());
            view.setStartDate(p.getStartDate());
            view.setEndDate(p.getEndDate());
            view.setOriginalPrice(p.getOriginalPrice());
            view.setPresentPrice(p.getPresentPrice());
            view.setLinkUrl(extend.getLinkUrl());
            view.setIconUrls(extend.getIconUrls());
            resultList.add(view);
        });

        return resultList;
    }

    public List<AgentActivity> getActivityList(Date startDate){
        return agentActivityDao.loadByStartDate(startDate);
    }

    public AgentActivity getActivity(String activityId){
        AgentActivity agentActivity = agentActivityDao.load(activityId);
        return agentActivity == null || SafeConverter.toBoolean(agentActivity.getDisabled()) ? null : agentActivity;
    }

    public ActivityExtend getActivityExtend(String activityId){
        return activityExtendDao.loadByAid(activityId);
    }

    public String getIntroductionUrl(String activityId){
        ActivityExtend extend = activityExtendDao.loadByAid(activityId);
        return extend == null ? "" : extend.getIntroductionUrl();
    }

    public String getSlogan(String activityId){
        ActivityExtend extend = activityExtendDao.loadByAid(activityId);
        return extend == null ? "" : extend.getSlogan();
    }

    public MapMessage addActivity(String name, Date startDate, Date endDate, String originalPrice, String presentPrice){
        if(StringUtils.isBlank(name) || startDate == null){
            return MapMessage.errorMessage("请填写完整的数据");
        }

        AgentActivity activity = new AgentActivity();
        activity.setName(name);
        activity.setStartDate(startDate);
        activity.setEndDate(endDate);
        activity.setOriginalPrice(originalPrice);
        activity.setPresentPrice(presentPrice);

        activity.setIsShow(false);

        activity.setDisabled(false);
        agentActivityDao.insert(activity);
        return MapMessage.successMessage();
    }

    public MapMessage updateActivity(String activityId, String name, Date startDate, Date endDate, String originalPrice, String presentPrice){
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null){
            return MapMessage.errorMessage();
        }
        if(StringUtils.isNotBlank(name)){
            activity.setName(name);
        }
        if(startDate != null){
            activity.setStartDate(startDate);
        }
        if(endDate != null){
            activity.setEndDate(endDate);
        }

        activity.setOriginalPrice(originalPrice);
        activity.setPresentPrice(presentPrice);
        agentActivityDao.replace(activity);
        return MapMessage.successMessage();
    }

    public MapMessage updateShowData(String activityId, boolean isShow){
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null){
            return MapMessage.errorMessage();
        }
        activity.setIsShow(isShow);
        agentActivityDao.replace(activity);
        return MapMessage.successMessage();
    }

    public MapMessage deleteActivity(String activityId){
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null){
            return MapMessage.errorMessage();
        }
        ActivityExtend extend = activityExtendDao.loadByAid(activityId);
        if(extend != null){
            extend.setDisabled(true);
            activityExtendDao.replace(extend);
        }

        List<ActivityIndicatorConfig> indicatorConfigList = indicatorConfigDao.loadByAid(activityId);
        if(CollectionUtils.isNotEmpty(indicatorConfigList)){
            indicatorConfigList.forEach(p -> {
                p.setDisabled(true);
                indicatorConfigDao.replace(p);
            });
        }

        // 删除权限
        recordAuthorityService.deleteRecordAuthority(activityId, AgentAuthorityType.ACTIVITY.getId());

        activity.setDisabled(true);
        agentActivityDao.replace(activity);

        return MapMessage.successMessage();
    }

    public MapMessage updateExtend(String activityId,
                                   List<String> iconUrls,
                                   String linkUrl,
                                   String introductionUrl,
                                   String recordUrl,
                                   Integer qrCodeX,
                                   Integer qrCodeY,
                                   List<String> posterUrls,
                                   String slogan,
                                   List<String> materialUrls,
                                   Integer form,
                                   Integer meetConditionDays,
                                   Boolean multipleOrderFlag,
                                   Boolean hasGift
                                   ){
        if(getActivity(activityId) == null){
            return MapMessage.errorMessage();
        }

        ActivityExtend extend = activityExtendDao.loadByAid(activityId);
        if(extend == null){
            extend = new ActivityExtend();
            extend.setActivityId(activityId);
        }

        if(CollectionUtils.isNotEmpty(iconUrls)){
            extend.setIconUrls(iconUrls);
        }
        if(StringUtils.isNotBlank(linkUrl)){
            extend.setLinkUrl(linkUrl);
        }

        if(StringUtils.isNotBlank(introductionUrl)){
            extend.setIntroductionUrl(introductionUrl);
        }
        if(StringUtils.isNotBlank(recordUrl)){
            extend.setRecordUrl(recordUrl);
        }

        extend.setQrCodeX(qrCodeX);
        extend.setQrCodeY(qrCodeY);

        extend.setPosterUrls(posterUrls);
        extend.setSlogan(slogan);
        extend.setMaterialUrls(materialUrls);
        extend.setForm(form);
        extend.setMeetConditionDays(SafeConverter.toInt(meetConditionDays, 1));
        extend.setMultipleOrderFlag(multipleOrderFlag);
        extend.setHasGift(hasGift);
        extend.setDisabled(false);
        activityExtendDao.upsert(extend);
        return MapMessage.successMessage();
    }

    public List<ActivityIndicatorConfig> getIndicatorList(String activityId){
        return indicatorConfigDao.loadByAid(activityId);
    }

    public MapMessage updateIndicator(String activityId, ActivityDataIndicator indicator, String alias, int sortNo){
        if(indicator == null || getActivity(activityId) == null){
            return MapMessage.errorMessage();
        }
        List<ActivityIndicatorConfig> indicatorList = indicatorConfigDao.loadByAid(activityId);

        ActivityIndicatorConfig config = indicatorList.stream().filter(p -> p.getIndicator() == indicator).findFirst().orElse(null);
        if(config == null){
            config = new ActivityIndicatorConfig();
            config.setActivityId(activityId);
            config.setIndicator(indicator);
        }

        config.setAlias(alias);
        config.setSortNo(sortNo);
        config.setDisabled(false);
        indicatorConfigDao.upsert(config);
        return MapMessage.successMessage();
    }

    public MapMessage deleteIndicator(String indicatorId){

        ActivityIndicatorConfig config = indicatorConfigDao.load(indicatorId);
        if(config != null){
            config.setDisabled(true);
            indicatorConfigDao.upsert(config);
        }
        return MapMessage.successMessage();
    }

    public MapMessage addControl(String activityId, Integer roleId){
        if(roleId == null || getActivity(activityId) == null){
            return MapMessage.errorMessage();
        }

        List<ActivityControl> activityControlList = activityControlDao.loadByAid(activityId);
        ActivityControl control = null;
        if(CollectionUtils.isNotEmpty(activityControlList)){
            control = activityControlList.stream().filter(p -> Objects.equals(p.getRoleId(), roleId)).findFirst().orElse(null);
        }
        if(control == null){
            control = new ActivityControl();
            control.setActivityId(activityId);
        }
        control.setRoleId(roleId);
        control.setDisabled(false);
        activityControlDao.upsert(control);
        return MapMessage.successMessage();
    }

    public MapMessage deleteControl(String controlId){
        ActivityControl control = activityControlDao.load(controlId);
        if(control == null || SafeConverter.toBoolean(control.getDisabled())){
            return MapMessage.successMessage();
        }
        control.setDisabled(true);
        activityControlDao.upsert(control);
        return MapMessage.successMessage();
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

    private List<Integer> getActivityStartToNowEveryDays(String activityId){
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return new ArrayList<>();
        }
        return getEveryDays(activity.getStartDate(), new Date());
    }

    private List<Integer> getEveryDays(Date startDate, Date endDate){
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

    private List<ActivityIndicatorConfig> getIndicatorConfigList(String activityId){
        return indicatorConfigDao.loadByAid(activityId);
    }

    private List<ActivityDataCategory> getIndicatorCategories(String activityId){

        List<ActivityDataCategory> resultList = new ArrayList<>();
        List<ActivityIndicatorConfig> indicatorList = indicatorConfigDao.loadByAid(activityId);
        if(CollectionUtils.isNotEmpty(indicatorList)){
            resultList.addAll(indicatorList.stream().map(ActivityIndicatorConfig::getIndicator).filter(Objects::nonNull).map(ActivityDataIndicator::getCategory).collect(Collectors.toSet()));
        }
        return resultList;
    }

    public ActivityDataView getOverview(String activityId, Long userId){


        ActivityCouponStatisticsView couponStatisticsView = null;
        ActivityOrderStatisticsView orderStatisticsView = null;
        ActivityAttendCourseStatisticsView attendCourseStatisticsView = null;
        ActivityOrderUserStatisticsView orderUserStatisticsView = null;
        ActivityGroupStatisticsView groupStatisticsView = null;
        ActivityGroupUserStatisticsView groupUserStatisticsView = null;
        ActivityCardStatisticsView cardStatisticsView = null;

        List<ActivityDataCategory> indicatorCategories = getIndicatorCategories(activityId);
        if(CollectionUtils.isNotEmpty(indicatorCategories)){

            List<Integer> days = getActivityEveryDays(activityId);
            List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
            if(CollectionUtils.isNotEmpty(groupIds)){
                Long groupId = groupIds.get(0);
                if(indicatorCategories.contains(ActivityDataCategory.COUPON)){
                    couponStatisticsView = couponStatisticsService.getGroupDataView(activityId, groupId, days);
                }

                if(indicatorCategories.contains(ActivityDataCategory.ORDER)){
                    orderStatisticsView = orderStatisticsService.getGroupDataView(activityId, groupId, days);
                }

                if(indicatorCategories.contains(ActivityDataCategory.ORDER_USER)){
                    orderUserStatisticsView = orderUserStatisticsService.getGroupDataView(activityId, groupId, days);
                }

                if(indicatorCategories.contains(ActivityDataCategory.COURSE)) {
                    attendCourseStatisticsView = attendCourseStatisticsService.getGroupDataView(activityId, groupId, getActivityStartToNowEveryDays(activityId));
                }

                if(indicatorCategories.contains(ActivityDataCategory.GROUP)){
                    groupStatisticsView = groupStatisticsService.getGroupDataView(activityId, groupId, days);
                }

                if(indicatorCategories.contains(ActivityDataCategory.GROUP_USER)){
                    groupUserStatisticsView = groupUserStatisticsService.getGroupDataView(activityId, groupId, days);
                }
                if(indicatorCategories.contains(ActivityDataCategory.CARD)){
                    cardStatisticsView = cardStatisticsService.getGroupDataView(activityId, groupId, days);
                }
            }else {
                if(indicatorCategories.contains(ActivityDataCategory.COUPON)) {
                    couponStatisticsView = couponStatisticsService.getUserDataView(activityId, userId, days);
                }
                if(indicatorCategories.contains(ActivityDataCategory.ORDER)) {
                    orderStatisticsView = orderStatisticsService.getUserDataView(activityId, userId, days);
                }
                if(indicatorCategories.contains(ActivityDataCategory.ORDER_USER)){
                    orderUserStatisticsView = orderUserStatisticsService.getUserDataView(activityId, userId, days);
                }
                if(indicatorCategories.contains(ActivityDataCategory.COURSE)) {
                    attendCourseStatisticsView = attendCourseStatisticsService.getUserDataView(activityId, userId, getActivityStartToNowEveryDays(activityId));
                }
                if(indicatorCategories.contains(ActivityDataCategory.GROUP)){
                    groupStatisticsView = groupStatisticsService.getUserDataView(activityId, userId, days);
                }

                if(indicatorCategories.contains(ActivityDataCategory.GROUP_USER)){
                    groupUserStatisticsView = groupUserStatisticsService.getUserDataView(activityId, userId, days);
                }
                if(indicatorCategories.contains(ActivityDataCategory.CARD)){
                    cardStatisticsView = cardStatisticsService.getUserDataView(activityId, userId, days);
                }
            }
        }

        return generateDataView(indicatorConfigDao.loadByAid(activityId), couponStatisticsView, orderStatisticsView, attendCourseStatisticsView, orderUserStatisticsView, groupStatisticsView, groupUserStatisticsView, cardStatisticsView);
    }

//    private ActivityCouponOrderCourseStatisticsView generateCouponOrderCourseView(ActivityCouponStatisticsView couponStatisticsView, ActivityOrderStatisticsView orderStatisticsView, ActivityAttendCourseStatisticsView attendCourseStatisticsView, ActivityOrderUserStatisticsView orderUserStatisticsView){
//
//        if(couponStatisticsView == null && orderStatisticsView == null && attendCourseStatisticsView == null && orderUserStatisticsView == null){
//            return null;
//        }
//
//        ActivityCouponOrderCourseStatisticsView result = new ActivityCouponOrderCourseStatisticsView();
//        if(couponStatisticsView != null){
//            if(result.getId() == null){
//                result.setId(couponStatisticsView.getId());
//                result.setIdType(couponStatisticsView.getIdType());
//                result.setName(couponStatisticsView.getName());
//            }
//            result.setDayCouponCount(couponStatisticsView.getDayCouponCount());
//            result.setTotalCouponCount(couponStatisticsView.getTotalCouponCount());
//        }
//
//        if(orderStatisticsView != null){
//            if(result.getId() == null){
//                result.setId(orderStatisticsView.getId());
//                result.setIdType(orderStatisticsView.getIdType());
//                result.setName(orderStatisticsView.getName());
//            }
//            result.setDayOrderCount(orderStatisticsView.getDayOrderCount());
//            result.setTotalOrderCount(orderStatisticsView.getTotalOrderCount());
//        }
//
//        if(attendCourseStatisticsView != null){
//            if(result.getId() == null){
//                result.setId(attendCourseStatisticsView.getId());
//                result.setIdType(attendCourseStatisticsView.getIdType());
//                result.setName(attendCourseStatisticsView.getName());
//            }
//
//            result.setDayFirstAttendStuCount(attendCourseStatisticsView.getDayFirstAttendStuCount());
//            result.setTotalAttendStuCount(attendCourseStatisticsView.getTotalAttendStuCount());
//
//            result.setDayMeetConditionStuCount(attendCourseStatisticsView.getDayMeetConditionStuCount());
//            result.setTotalMeetConditionStuCount(attendCourseStatisticsView.getTotalMeetConditionStuCount());
//        }
//
//        if(orderUserStatisticsView != null){
//            if(result.getId() == null){
//                result.setId(orderUserStatisticsView.getId());
//                result.setIdType(orderUserStatisticsView.getIdType());
//                result.setName(orderUserStatisticsView.getName());
//            }
//            result.setDayOrderUserCount(orderUserStatisticsView.getDayOrderUserCount());
//            result.setTotalOrderUserCount(orderUserStatisticsView.getTotalOrderUserCount());
//        }
//        return result;
//    }


    private ActivityDataView generateDataView(List<ActivityIndicatorConfig> indicatorList,
                                              ActivityCouponStatisticsView couponStatisticsView,
                                              ActivityOrderStatisticsView orderStatisticsView,
                                              ActivityAttendCourseStatisticsView attendCourseStatisticsView,
                                              ActivityOrderUserStatisticsView orderUserStatisticsView,
                                              ActivityGroupStatisticsView groupStatisticsView,
                                              ActivityGroupUserStatisticsView groupUserStatisticsView,
                                              ActivityCardStatisticsView cardStatisticsView){

        if(CollectionUtils.isEmpty(indicatorList)){
            return null;
        }
        if(couponStatisticsView == null && orderStatisticsView == null && attendCourseStatisticsView == null && orderUserStatisticsView == null && groupStatisticsView == null && groupUserStatisticsView == null){
            return null;
        }

        indicatorList.sort(Comparator.comparingInt(o -> SafeConverter.toInt(o.getSortNo())));

        ActivityDataView dataView = new ActivityDataView();

        for(ActivityIndicatorConfig indicatorConfig : indicatorList){
            ActivityDataIndicator indicatorType = indicatorConfig.getIndicator();
            if(indicatorType == null){
                continue;
            }

            ActivityDataView.ActivityIndicatorData indicatorData = new ActivityDataView.ActivityIndicatorData();
            indicatorData.setIndicatorName(indicatorConfig.getAlias());

            if(indicatorType.getCategory() == ActivityDataCategory.COUPON){
                if(couponStatisticsView != null){
                    if(dataView.getId() == null){
                        dataView.setId(couponStatisticsView.getId());
                        dataView.setIdType(couponStatisticsView.getIdType());
                        dataView.setName(couponStatisticsView.getName());
                    }
                    if(indicatorType == ActivityDataIndicator.COUPON_DAY){
                        indicatorData.setIndicatorValue(couponStatisticsView.getDayCouponCount());
                    }else if(indicatorType == ActivityDataIndicator.COUPON_SUM){
                        indicatorData.setIndicatorValue(couponStatisticsView.getTotalCouponCount());
                    }
                }
            }else if(indicatorType.getCategory() == ActivityDataCategory.ORDER){
                if(orderStatisticsView != null){
                    if(dataView.getId() == null){
                        dataView.setId(orderStatisticsView.getId());
                        dataView.setIdType(orderStatisticsView.getIdType());
                        dataView.setName(orderStatisticsView.getName());
                    }
                    if(indicatorType == ActivityDataIndicator.ORDER_DAY){
                        indicatorData.setIndicatorValue(orderStatisticsView.getDayOrderCount());
                    }else if(indicatorType == ActivityDataIndicator.ORDER_SUM){
                        indicatorData.setIndicatorValue(orderStatisticsView.getTotalOrderCount());
                    }
                }
            }else if(indicatorType.getCategory() == ActivityDataCategory.ORDER_USER){
                if(orderUserStatisticsView != null){
                    if(dataView.getId() == null){
                        dataView.setId(orderUserStatisticsView.getId());
                        dataView.setIdType(orderUserStatisticsView.getIdType());
                        dataView.setName(orderUserStatisticsView.getName());
                    }
                    if(indicatorType == ActivityDataIndicator.ORDER_USER_DAY){
                        indicatorData.setIndicatorValue(orderUserStatisticsView.getDayOrderUserCount());
                    }else if(indicatorType == ActivityDataIndicator.ORDER_USER_SUM){
                        indicatorData.setIndicatorValue(orderUserStatisticsView.getTotalOrderUserCount());
                    }
                }
            }else if(indicatorType.getCategory() == ActivityDataCategory.COURSE){
                if(attendCourseStatisticsView != null){
                    if(dataView.getId() == null){
                        dataView.setId(attendCourseStatisticsView.getId());
                        dataView.setIdType(attendCourseStatisticsView.getIdType());
                        dataView.setName(attendCourseStatisticsView.getName());
                    }

                    if(indicatorType == ActivityDataIndicator.COURSE_SUM){
                        indicatorData.setIndicatorValue(attendCourseStatisticsView.getTotalAttendStuCount());
                    }else if(indicatorType == ActivityDataIndicator.COURSE_MEET_SUM){
                        indicatorData.setIndicatorValue(attendCourseStatisticsView.getTotalMeetConditionStuCount());
                    }
                }
            }else if(indicatorType.getCategory() == ActivityDataCategory.GROUP){
                if(groupStatisticsView != null){
                    if(dataView.getId() == null){
                        dataView.setId(groupStatisticsView.getId());
                        dataView.setIdType(groupStatisticsView.getIdType());
                        dataView.setName(groupStatisticsView.getName());
                    }

                    if(indicatorType == ActivityDataIndicator.GROUP_SUM){
                        indicatorData.setIndicatorValue(groupStatisticsView.getTotalGroupCount());
                    }else if(indicatorType == ActivityDataIndicator.GROUP_COMPLETE_SUM){
                        indicatorData.setIndicatorValue(groupStatisticsView.getTotalCompleteGroupCount());
                    }
                }
            }else if(indicatorType.getCategory() == ActivityDataCategory.GROUP_USER){
                if(groupUserStatisticsView != null){
                    if(dataView.getId() == null){
                        dataView.setId(groupUserStatisticsView.getId());
                        dataView.setIdType(groupUserStatisticsView.getIdType());
                        dataView.setName(groupUserStatisticsView.getName());
                    }

                    if(indicatorType == ActivityDataIndicator.GROUP_USER_DAY){
                        indicatorData.setIndicatorValue(groupUserStatisticsView.getDayUserCount());
                    }else if(indicatorType == ActivityDataIndicator.GROUP_USER_SUM){
                        indicatorData.setIndicatorValue(groupUserStatisticsView.getTotalUserCount());
                    }else if(indicatorType == ActivityDataIndicator.GROUP_COMPLETE_USER_SUM){
                        indicatorData.setIndicatorValue(groupUserStatisticsView.getTotalCompleteUserCount());
                    }
                }
            }else if(indicatorType.getCategory() == ActivityDataCategory.CARD){
                if(cardStatisticsView != null){
                    if(dataView.getId() == null){
                        dataView.setId(cardStatisticsView.getId());
                        dataView.setIdType(cardStatisticsView.getIdType());
                        dataView.setName(cardStatisticsView.getName());
                    }

                    if(indicatorType == ActivityDataIndicator.CARD_DAY){
                        indicatorData.setIndicatorValue(cardStatisticsView.getDayCardCount());
                    }else if(indicatorType == ActivityDataIndicator.CARD_SUM){
                        indicatorData.setIndicatorValue(cardStatisticsView.getTotalCardCount());
                    }else if(indicatorType == ActivityDataIndicator.CARD_USED_SUM){
                        indicatorData.setIndicatorValue(cardStatisticsView.getTotalUsedCount());
                    }
                }
            }
            dataView.getDataList().add(indicatorData);
        }

        return dataView;
    }

    private List<ActivityDataView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityDataView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds) || CollectionUtils.isEmpty(days)){
            return resultList;
        }

        Map<Long, ActivityCouponStatisticsView> couponDataMap = new HashMap<>();
        Map<Long, ActivityOrderStatisticsView> orderDataMap = new HashMap<>();
        Map<Long, ActivityOrderUserStatisticsView> orderUserDataMap = new HashMap<>();
        Map<Long, ActivityAttendCourseStatisticsView> courseDataMap = new HashMap<>();
        Map<Long, ActivityGroupStatisticsView> groupDataMap = new HashMap<>();
        Map<Long, ActivityGroupUserStatisticsView> groupUserDataMap = new HashMap<>();
        Map<Long, ActivityCardStatisticsView> cardDataMap = new HashMap<>();



        List<ActivityDataCategory> indicatorCategories = getIndicatorCategories(activityId);
        if(CollectionUtils.isNotEmpty(indicatorCategories)){
            if(indicatorCategories.contains(ActivityDataCategory.COUPON)){
                List<ActivityCouponStatisticsView> couponDataList = couponStatisticsService.getGroupDataView(activityId, groupIds, days);
                if(CollectionUtils.isNotEmpty(couponDataList)){
                    couponDataMap.putAll(couponDataList.stream().collect(Collectors.toMap(ActivityCouponStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.ORDER)){
                List<ActivityOrderStatisticsView> orderDataList = orderStatisticsService.getGroupDataView(activityId, groupIds, days);
                if(CollectionUtils.isNotEmpty(orderDataList)){
                    orderDataMap.putAll(orderDataList.stream().collect(Collectors.toMap(ActivityOrderStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.ORDER_USER)){
                List<ActivityOrderUserStatisticsView> orderUserDataList = orderUserStatisticsService.getGroupDataView(activityId, groupIds, days);
                if(CollectionUtils.isNotEmpty(orderUserDataList)){
                    orderUserDataMap.putAll(orderUserDataList.stream().collect(Collectors.toMap(ActivityOrderUserStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.COURSE)) {
                List<ActivityAttendCourseStatisticsView> courseDataList = attendCourseStatisticsService.getGroupDataView(activityId, groupIds, getActivityStartToNowEveryDays(activityId));
                if(CollectionUtils.isNotEmpty(courseDataList)){
                    courseDataMap.putAll(courseDataList.stream().collect(Collectors.toMap(ActivityAttendCourseStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.GROUP)) {
                List<ActivityGroupStatisticsView> groupDataList = groupStatisticsService.getGroupDataView(activityId, groupIds, days);
                if(CollectionUtils.isNotEmpty(groupDataList)){
                    groupDataMap.putAll(groupDataList.stream().collect(Collectors.toMap(ActivityGroupStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.GROUP_USER)) {
                List<ActivityGroupUserStatisticsView> groupUserDataList = groupUserStatisticsService.getGroupDataView(activityId, groupIds, days);
                if(CollectionUtils.isNotEmpty(groupUserDataList)){
                    groupUserDataMap.putAll(groupUserDataList.stream().collect(Collectors.toMap(ActivityGroupUserStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.CARD)) {
                List<ActivityCardStatisticsView> cardDataList = cardStatisticsService.getGroupDataView(activityId, groupIds, days);
                if(CollectionUtils.isNotEmpty(cardDataList)){
                    cardDataMap.putAll(cardDataList.stream().collect(Collectors.toMap(ActivityCardStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }
        }

        List<ActivityIndicatorConfig> indicatorConfigList = indicatorConfigDao.loadByAid(activityId);
        groupIds.forEach(p -> {
            ActivityDataView view = generateDataView(indicatorConfigList, couponDataMap.get(p), orderDataMap.get(p), courseDataMap.get(p), orderUserDataMap.get(p), groupDataMap.get(p), groupUserDataMap.get(p), cardDataMap.get(p));
            if(view != null){
                resultList.add(view);
            }
        });
        return resultList;
    }

    private List<ActivityDataView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityDataView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds) || CollectionUtils.isEmpty(days)){
            return resultList;
        }

        Map<Long, ActivityCouponStatisticsView> couponDataMap = new HashMap<>();
        Map<Long, ActivityOrderStatisticsView> orderDataMap = new HashMap<>();
        Map<Long, ActivityOrderUserStatisticsView> orderUserDataMap = new HashMap<>();
        Map<Long, ActivityAttendCourseStatisticsView> courseDataMap = new HashMap<>();
        Map<Long, ActivityGroupStatisticsView> groupDataMap = new HashMap<>();
        Map<Long, ActivityGroupUserStatisticsView> groupUserDataMap = new HashMap<>();
        Map<Long, ActivityCardStatisticsView> cardDataMap = new HashMap<>();

        List<ActivityDataCategory> indicatorCategories = getIndicatorCategories(activityId);
        if(CollectionUtils.isNotEmpty(indicatorCategories)) {

            if(indicatorCategories.contains(ActivityDataCategory.COUPON)){
                List<ActivityCouponStatisticsView> couponDataList = couponStatisticsService.getUserDataView(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(couponDataList)){
                    couponDataMap.putAll(couponDataList.stream().collect(Collectors.toMap(ActivityCouponStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.ORDER)){
                List<ActivityOrderStatisticsView> orderDataList = orderStatisticsService.getUserDataView(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(orderDataList)){
                    orderDataMap.putAll(orderDataList.stream().collect(Collectors.toMap(ActivityOrderStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.ORDER_USER)){
                List<ActivityOrderUserStatisticsView> orderUserDataList = orderUserStatisticsService.getUserDataView(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(orderUserDataList)){
                    orderUserDataMap.putAll(orderUserDataList.stream().collect(Collectors.toMap(ActivityOrderUserStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.COURSE)) {
                List<ActivityAttendCourseStatisticsView> courseDataList = attendCourseStatisticsService.getUserDataView(activityId, userIds, getActivityStartToNowEveryDays(activityId));
                if(CollectionUtils.isNotEmpty(courseDataList)){
                    courseDataMap.putAll(courseDataList.stream().collect(Collectors.toMap(ActivityAttendCourseStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.GROUP)) {
                List<ActivityGroupStatisticsView> groupDataList = groupStatisticsService.getUserDataView(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(groupDataList)){
                    groupDataMap.putAll(groupDataList.stream().collect(Collectors.toMap(ActivityGroupStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.GROUP_USER)) {
                List<ActivityGroupUserStatisticsView> groupUserDataList = groupUserStatisticsService.getUserDataView(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(groupUserDataList)){
                    groupUserDataMap.putAll(groupUserDataList.stream().collect(Collectors.toMap(ActivityGroupUserStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }

            if(indicatorCategories.contains(ActivityDataCategory.CARD)) {
                List<ActivityCardStatisticsView> cardDataList = cardStatisticsService.getUserDataView(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(cardDataList)){
                    cardDataMap.putAll(cardDataList.stream().collect(Collectors.toMap(ActivityCardStatisticsView::getId, Function.identity(), (o1, o2) -> o1)));
                }
            }
        }

        List<ActivityIndicatorConfig> indicatorConfigList = indicatorConfigDao.loadByAid(activityId);
        userIds.forEach(p -> {
            ActivityDataView view = generateDataView(indicatorConfigList, couponDataMap.get(p), orderDataMap.get(p), courseDataMap.get(p), orderUserDataMap.get(p), groupDataMap.get(p), groupUserDataMap.get(p), cardDataMap.get(p));
            if(view != null){
                resultList.add(view);
            }
        });
        return resultList;
    }

    public Map<String, Integer> getChartInfo(String activityId, Long userId){
        List<ActivityDataCategory> indicatorCategories = getIndicatorCategories(activityId);

        if(indicatorCategories.contains(ActivityDataCategory.COUPON)){
            return getCouponChartInfo(activityId, userId);
        }else if(indicatorCategories.contains(ActivityDataCategory.ORDER)){
            return getOrderChartInfo(activityId, userId);
        }else if(indicatorCategories.contains(ActivityDataCategory.GROUP_USER)){
            return getGroupUserChartInfo(activityId, userId);
        }else if(indicatorCategories.contains(ActivityDataCategory.CARD)){
            return getCardChartInfo(activityId, userId);
        }
        return new HashMap<>();
    }

    private Map<String, Integer> getCouponChartInfo(String activityId, Long userId){
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return resultMap;
        }

        Collection<Long> userIds = getManagedUsers(userId);
        Map<Integer, Integer> dayCouponCountMap = new HashMap<>();
        List<ActivityCouponStatistics> dataList = couponStatisticsService.getCouponStatistics(activityId, userIds, days);
        if(CollectionUtils.isNotEmpty(dataList)) {
            dayCouponCountMap = dataList.stream().collect(Collectors.groupingBy(ActivityCouponStatistics::getDay, Collectors.summingInt(p -> SafeConverter.toInt(p.getCount()))));
        }

        for(Integer d : days){
            String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(d), "yyyyMMdd"), "MM/dd");
            resultMap.put(key, SafeConverter.toInt(dayCouponCountMap.get(d)));
        }
        return resultMap;
    }

    private Map<String, Integer> getOrderChartInfo(String activityId, Long userId){
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return resultMap;
        }

        Collection<Long> userIds = getManagedUsers(userId);
        Map<Integer, Integer> dayCouponCountMap = new HashMap<>();
        List<ActivityOrderStatistics> dataList = orderStatisticsService.getOrderStatistics(activityId, userIds, days);
        if(CollectionUtils.isNotEmpty(dataList)) {
            dayCouponCountMap = dataList.stream().collect(Collectors.groupingBy(ActivityOrderStatistics::getDay, Collectors.summingInt(p -> SafeConverter.toInt(p.getCount()))));
        }

        for(Integer d : days){
            String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(d), "yyyyMMdd"), "MM/dd");
            resultMap.put(key, SafeConverter.toInt(dayCouponCountMap.get(d)));
        }
        return resultMap;
    }

    private Map<String, Integer> getGroupUserChartInfo(String activityId, Long userId){
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return resultMap;
        }

        Collection<Long> userIds = getManagedUsers(userId);
        Map<Integer, Integer> dayDataMap = new HashMap<>();
        List<ActivityGroupUserStatistics> dataList = groupUserStatisticsService.getGroupUserStatistics(activityId, userIds, days);
        if(CollectionUtils.isNotEmpty(dataList)) {
            dayDataMap = dataList.stream().collect(Collectors.groupingBy(ActivityGroupUserStatistics::getDay, Collectors.summingInt(p -> SafeConverter.toInt(p.getUserCount()))));
        }

        for(Integer d : days){
            String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(d), "yyyyMMdd"), "MM/dd");
            resultMap.put(key, SafeConverter.toInt(dayDataMap.get(d)));
        }
        return resultMap;
    }

    private Map<String, Integer> getCardChartInfo(String activityId, Long userId){
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return resultMap;
        }

        Collection<Long> userIds = getManagedUsers(userId);
        Map<Integer, Integer> dayDataMap = new HashMap<>();
        List<ActivityCardStatistics> dataList = cardStatisticsService.getCardStatistics(activityId, userIds, days);
        if(CollectionUtils.isNotEmpty(dataList)) {
            dayDataMap = dataList.stream().collect(Collectors.groupingBy(ActivityCardStatistics::getDay, Collectors.summingInt(p -> SafeConverter.toInt(p.getCount()))));
        }

        for(Integer d : days){
            String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(d), "yyyyMMdd"), "MM/dd");
            resultMap.put(key, SafeConverter.toInt(dayDataMap.get(d)));
        }
        return resultMap;
    }

    public Collection<Long> getManagedUsers(Long userId){
        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if(CollectionUtils.isNotEmpty(managedGroupIds)){
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            userIds.addAll(groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
        }
        return userIds;
    }

    // dateType: 1 当日 2 累计
    // rankingType: 1 专员 2 分区
    public List<ActivityRankingView> getRankingList(String activityId, Integer dateType, Integer rankingType, Integer topN){
        List<ActivityDataCategory> indicatorCategories = getIndicatorCategories(activityId);

        if(indicatorCategories.contains(ActivityDataCategory.COUPON)){
            return getCouponRankingList(activityId, dateType, rankingType, topN);
        }else if(indicatorCategories.contains(ActivityDataCategory.ORDER)){
            return getOrderRankingList(activityId, dateType, rankingType, topN);
        }else if(indicatorCategories.contains(ActivityDataCategory.GROUP_USER)){
            return getGroupUserRankingList(activityId, dateType, rankingType, topN);
        }else if(indicatorCategories.contains(ActivityDataCategory.CARD)){
            return getCardRankingList(activityId, dateType, rankingType, topN);
        }
        return new ArrayList<>();
    }

    private List<ActivityRankingView> getCouponRankingList(String activityId, Integer dateType, Integer rankingType, Integer topN){
        List<ActivityRankingView> rankingDataList = new ArrayList<>();

        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return rankingDataList;
        }

        Integer day = days.stream().max(Comparator.comparing(Function.identity())).get();

        List<ActivityCouponStatisticsView> dataViewList = new ArrayList<>();
        if(rankingType == 1) { // 专员榜
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
            List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(dateType == 1){ // 日榜
                dataViewList.addAll(couponStatisticsService.getUserDataView(activityId, userIds, Collections.singleton(day)));
            }else if(dateType == 2){ // 累计
                dataViewList.addAll(couponStatisticsService.getUserDataView(activityId, userIds, days));
            }

        }else {  // 分区榜
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
            List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            if (dateType == 1) { // 日榜
                dataViewList.addAll(couponStatisticsService.getGroupDataView(activityId, groupIdList, Collections.singleton(day)));
            } else if (dateType == 2) { // 累计
                dataViewList.addAll(couponStatisticsService.getGroupDataView(activityId, groupIdList, days));
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
            ActivityCouponStatisticsView dataView = dataViewList.get(i);
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
                ActivityRankingView rankingView = new ActivityRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }else {
                if (preAmount != dataValue) {
                    break;
                }
                ActivityRankingView rankingView = new ActivityRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }
        }
        return rankingDataList;
    }

    private List<ActivityRankingView> getOrderRankingList(String activityId, Integer dateType, Integer rankingType, Integer topN){
        List<ActivityRankingView> rankingDataList = new ArrayList<>();

        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return rankingDataList;
        }

        Integer day = days.stream().max(Comparator.comparing(Function.identity())).get();

        List<ActivityOrderStatisticsView> dataViewList = new ArrayList<>();
        if(rankingType == 1) { // 专员榜
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
            List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(dateType == 1){ // 日榜
                dataViewList.addAll(orderStatisticsService.getUserDataView(activityId, userIds, Collections.singleton(day)));
            }else if(dateType == 2){ // 累计
                dataViewList.addAll(orderStatisticsService.getUserDataView(activityId, userIds, days));
            }

        }else {  // 分区榜
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
            List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            if (dateType == 1) { // 日榜
                dataViewList.addAll(orderStatisticsService.getGroupDataView(activityId, groupIdList, Collections.singleton(day)));
            } else if (dateType == 2) { // 累计
                dataViewList.addAll(orderStatisticsService.getGroupDataView(activityId, groupIdList, days));
            }
        }

        dataViewList = dataViewList.stream()
                .filter(p -> (dateType == 1 && p.getDayOrderCount() > 0) || (dateType == 2 && p.getTotalOrderCount() > 0))
                .sorted((o1, o2) -> {
                    if(dateType == 1){
                        return Integer.compare(SafeConverter.toInt(o2.getDayOrderCount()), SafeConverter.toInt(o1.getDayOrderCount()));
                    }else {
                        return Integer.compare(SafeConverter.toInt(o2.getTotalOrderCount()), SafeConverter.toInt(o1.getTotalOrderCount()));
                    }
                })
                .collect(Collectors.toList());

        int ranking = 0;
        int preAmount = 0;
        for(int i = 0; i< dataViewList.size(); i++){
            ActivityOrderStatisticsView dataView = dataViewList.get(i);
            Integer dataValue = 0;
            if(dateType == 1){
                dataValue = dataView.getDayOrderCount();
            }else if(dateType == 2){
                dataValue = dataView.getTotalOrderCount();
            }
            if(ranking < topN) {
                if (preAmount != dataValue) {
                    preAmount = dataValue;
                    ranking++;
                }
                ActivityRankingView rankingView = new ActivityRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }else {
                if (preAmount != dataValue) {
                    break;
                }
                ActivityRankingView rankingView = new ActivityRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }
        }
        return rankingDataList;
    }

    private List<ActivityRankingView> getGroupUserRankingList(String activityId, Integer dateType, Integer rankingType, Integer topN){
        List<ActivityRankingView> rankingDataList = new ArrayList<>();

        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return rankingDataList;
        }

        Integer day = days.stream().max(Comparator.comparing(Function.identity())).get();

        List<ActivityGroupUserStatisticsView> dataViewList = new ArrayList<>();
        if(rankingType == 1) { // 专员榜
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
            List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(dateType == 1){ // 日榜
                dataViewList.addAll(groupUserStatisticsService.getUserDataView(activityId, userIds, Collections.singleton(day)));
            }else if(dateType == 2){ // 累计
                dataViewList.addAll(groupUserStatisticsService.getUserDataView(activityId, userIds, days));
            }

        }else {  // 分区榜
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
            List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            if (dateType == 1) { // 日榜
                dataViewList.addAll(groupUserStatisticsService.getGroupDataView(activityId, groupIdList, Collections.singleton(day)));
            } else if (dateType == 2) { // 累计
                dataViewList.addAll(groupUserStatisticsService.getGroupDataView(activityId, groupIdList, days));
            }
        }

        dataViewList = dataViewList.stream()
                .filter(p -> (dateType == 1 && p.getDayUserCount() > 0) || (dateType == 2 && p.getTotalUserCount() > 0))
                .sorted((o1, o2) -> {
                    if(dateType == 1){
                        return Integer.compare(SafeConverter.toInt(o2.getDayUserCount()), SafeConverter.toInt(o1.getDayUserCount()));
                    }else {
                        return Integer.compare(SafeConverter.toInt(o2.getTotalUserCount()), SafeConverter.toInt(o1.getTotalUserCount()));
                    }
                })
                .collect(Collectors.toList());

        int ranking = 0;
        int preAmount = 0;
        for(int i = 0; i< dataViewList.size(); i++){
            ActivityGroupUserStatisticsView dataView = dataViewList.get(i);
            Integer dataValue = 0;
            if(dateType == 1){
                dataValue = dataView.getDayUserCount();
            }else if(dateType == 2){
                dataValue = dataView.getTotalUserCount();
            }
            if(ranking < topN) {
                if (preAmount != dataValue) {
                    preAmount = dataValue;
                    ranking++;
                }
                ActivityRankingView rankingView = new ActivityRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }else {
                if (preAmount != dataValue) {
                    break;
                }
                ActivityRankingView rankingView = new ActivityRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }
        }
        return rankingDataList;
    }

    private List<ActivityRankingView> getCardRankingList(String activityId, Integer dateType, Integer rankingType, Integer topN){
        List<ActivityRankingView> rankingDataList = new ArrayList<>();

        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return rankingDataList;
        }

        Integer day = days.stream().max(Comparator.comparing(Function.identity())).get();

        List<ActivityCardStatisticsView> dataViewList = new ArrayList<>();
        if(rankingType == 1) { // 专员榜
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
            List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(dateType == 1){ // 日榜
                dataViewList.addAll(cardStatisticsService.getUserDataView(activityId, userIds, Collections.singleton(day)));
            }else if(dateType == 2){ // 累计
                dataViewList.addAll(cardStatisticsService.getUserDataView(activityId, userIds, days));
            }

        }else {  // 分区榜
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
            List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            if (dateType == 1) { // 日榜
                dataViewList.addAll(cardStatisticsService.getGroupDataView(activityId, groupIdList, Collections.singleton(day)));
            } else if (dateType == 2) { // 累计
                dataViewList.addAll(cardStatisticsService.getGroupDataView(activityId, groupIdList, days));
            }
        }

        dataViewList = dataViewList.stream()
                .filter(p -> (dateType == 1 && p.getDayCardCount() > 0) || (dateType == 2 && p.getTotalCardCount() > 0))
                .sorted((o1, o2) -> {
                    if(dateType == 1){
                        return Integer.compare(SafeConverter.toInt(o2.getDayCardCount()), SafeConverter.toInt(o1.getDayCardCount()));
                    }else {
                        return Integer.compare(SafeConverter.toInt(o2.getTotalCardCount()), SafeConverter.toInt(o1.getTotalCardCount()));
                    }
                })
                .collect(Collectors.toList());

        int ranking = 0;
        int preAmount = 0;
        for(int i = 0; i< dataViewList.size(); i++){
            ActivityCardStatisticsView dataView = dataViewList.get(i);
            Integer dataValue = 0;
            if(dateType == 1){
                dataValue = dataView.getDayCardCount();
            }else if(dateType == 2){
                dataValue = dataView.getTotalCardCount();
            }
            if(ranking < topN) {
                if (preAmount != dataValue) {
                    preAmount = dataValue;
                    ranking++;
                }
                ActivityRankingView rankingView = new ActivityRankingView();
                rankingView.setId(dataView.getId());
                rankingView.setName(dataView.getName());
                rankingView.setRanking(ranking);
                rankingView.setDataValue(dataValue);
                rankingDataList.add(rankingView);
            }else {
                if (preAmount != dataValue) {
                    break;
                }
                ActivityRankingView rankingView = new ActivityRankingView();
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
    public List<ActivityDataView> getStatisticsDataList(String activityId, Long id, Integer idType, Integer dimension){
        List<ActivityDataView> dataList = new ArrayList<>();
        if(idType.equals(AgentConstants.INDICATOR_TYPE_USER)){
            return dataList;
        }

        List<Integer> days = getActivityEveryDays(activityId);

        AgentGroup group = baseOrgService.getGroupById(id);
        if(group == null){
            return dataList;
        }

        boolean isChannel = false;
        GroupWithParent groupWithParent = agentGroupSupport.generateGroupWithParent(id);
        while(groupWithParent != null){
            if(StringUtils.contains(groupWithParent.getGroupName(), "渠道")){
                isChannel = true;
                break;
            }
            groupWithParent = groupWithParent.getParent();
        }


        if(dimension == 2 || (dimension == 1 && group.fetchGroupRoleType() == AgentGroupRoleType.City)){
            AgentRoleType targetRole = isChannel ? AgentRoleType.CityAgentLimited : AgentRoleType.BusinessDeveloper;
            // 专员的情况下
            List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(), targetRole.getId());
            dataList.addAll(getUserDataView(activityId, userIds, days));
        }else {
            List<AgentGroup> groups;
            if(dimension == 1 ){  // 默认情况下
                if(group.fetchGroupRoleType() != AgentGroupRoleType.City
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Area
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Region
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Marketing
                ){
                    if(isChannel){
                        groups = baseOrgService.getGroupListByParentId(group.getId());
                    }else {
                        groups = baseOrgService.getSubGroupList(group.getId()).stream()
                                .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).collect(Collectors.toList());
                    }
                }else {
                    groups = baseOrgService.getGroupListByParentId(group.getId());
//                            .stream()
//                            .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City
//                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Area
//                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Region
//                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Marketing)
//                            .collect(Collectors.toList());
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
    public List<ActivityDataView> getOfficeDataList(String activityId, Long id, Integer idType, Integer dimension){
        List<ActivityDataView> dataList = new ArrayList<>();
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

    public List<Map<String, Object>> calAvgDataList(List<ActivityDataView> dataList){
        if(CollectionUtils.isEmpty(dataList)){
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataMapList = new ArrayList<>();
        dataList.forEach(p -> {
            int size = 1;
            if(Objects.equals(p.getIdType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentRoleType targetRole = AgentRoleType.BusinessDeveloper;
                GroupWithParent groupWithParent = agentGroupSupport.generateGroupWithParent(p.getId());
                while(groupWithParent != null){
                    if(StringUtils.contains(groupWithParent.getGroupName(), "渠道")){
                        targetRole = AgentRoleType.CityAgentLimited;
                        break;
                    }
                    groupWithParent = groupWithParent.getParent();
                }
                List<AgentGroupUser> groupUserList = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(p.getId(), targetRole.getId());
                size = groupUserList.size();
            }
            dataMapList.add(p.convertToAverageMap(size, 1));
        });
        return dataMapList;
    }


    public List<ActivityCouponOrderCourseView> getRecordList(String activityId, Long userId){
        List<ActivityDataCategory> indicatorCategories = getIndicatorCategories(activityId);

        if(indicatorCategories.contains(ActivityDataCategory.COUPON)){
            return getCouponRecordList(activityId, userId);
        }else if(indicatorCategories.contains(ActivityDataCategory.ORDER)){
            return getOrderRecordList(activityId, userId);
        }
        return new ArrayList<>();
    }


    public List<ActivityCouponOrderCourseView> getCouponRecordList(String activityId, Long userId){
        List<ActivityCouponOrderCourseView> resultList = new ArrayList<>();
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null){
            return resultList;
        }
        List<ActivityCoupon> couponList = couponService.getCouponList(activityId, userId);
        if(CollectionUtils.isEmpty(couponList)){
            return resultList;
        }

        List<String> orderIds = couponList.stream().filter(p -> StringUtils.isNotBlank(p.getOrderId())).map(ActivityCoupon::getOrderId).collect(Collectors.toList());

        Map<String, List<ActivityOrderStudent>> orderStudentMap = orderStudentService.getOrderStudentByOids(orderIds);


        Date targetDate = activity.getStartDate() == null? DateUtils.addMonths(new Date(), -1) : activity.getStartDate();


        couponList.forEach(p -> {
            ActivityCouponOrderCourseView view = new ActivityCouponOrderCourseView();
            view.setUserId(p.getCouponUserId());
            view.setUserName("");
            view.setBusinessTime(p.getCouponTime());
            view.setMobile(sensitiveUserDataServiceClient.loadUserMobileObscured(p.getCouponUserId()));

            view.setIsNewUser(p.getCouponUserRegTime() != null && p.getCouponUserRegTime().after(targetDate));
            if(StringUtils.isNotBlank(p.getOrderId())){
                view.setHasOrder(true);
                List<ActivityOrderStudent> studentDataList = orderStudentMap.get(p.getOrderId());
                if(CollectionUtils.isNotEmpty(studentDataList)){
                    ActivityOrderStudent student = studentDataList.get(0);
                    view.setStudentId(student.getStudentId());
                    view.setStudentName(student.getStudentName());
                    view.setSchoolId(student.getSchoolId());
                    view.setSchoolName(student.getSchoolName());
                    if(student.getAttendClassLatestDay() != null){
                        view.setAttendClass(true);
                    }
                }
            }
            resultList.add(view);
        });

        return resultList;
    }

    public List<ActivityCouponOrderCourseView> getOrderRecordList(String activityId, Long userId){
        List<ActivityCouponOrderCourseView> resultList = new ArrayList<>();
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null){
            return resultList;
        }
        List<ActivityOrder> orderList = orderService.getOrderList(activityId, userId);
        if(CollectionUtils.isEmpty(orderList)){
            return resultList;
        }

        ActivityExtend extend = activityExtendDao.loadByAid(activityId);
        boolean hasGift = extend != null && SafeConverter.toBoolean(extend.getHasGift());

        List<String> orderIds = orderList.stream().filter(p -> StringUtils.isNotBlank(p.getOrderId())).map(ActivityOrder::getOrderId).collect(Collectors.toList());

        Map<String, List<ActivityOrderStudent>> orderStudentMap = orderStudentService.getOrderStudentByOids(orderIds);

        Map<String, List<ActivityOrderCourse>> orderCourseMap = orderCourseService.getOrderCourseByOids(orderIds);

        Date targetDate = activity.getStartDate() == null? DateUtils.addMonths(new Date(), -1) : activity.getStartDate();

        orderList.forEach(p -> {
            ActivityCouponOrderCourseView view = new ActivityCouponOrderCourseView();
            view.setUserId(p.getOrderUserId());
            view.setUserName("");
            view.setBusinessTime(p.getOrderPayTime());
            view.setMobile(sensitiveUserDataServiceClient.loadUserMobileObscured(p.getOrderUserId()));

            view.setIsNewUser(p.getOrderUserRegTime() != null && p.getOrderUserRegTime().after(targetDate));

            view.setOrderId(p.getOrderId());
            view.setHasOrder(true);
            view.setHasGift(hasGift);
            if(hasGift){
                List<ActivityOrderGift> orderGiftList = activityOrderGiftDao.loadByOid(p.getOrderId());
                view.setGiftReceived(CollectionUtils.isNotEmpty(orderGiftList) && orderGiftList.stream().anyMatch(t -> SafeConverter.toInt(t.getCount()) > 0));
            }

            List<ActivityOrderStudent> studentDataList = orderStudentMap.get(p.getOrderId());
            if(CollectionUtils.isNotEmpty(studentDataList)){
                ActivityOrderStudent student = studentDataList.get(0);
                view.setStudentId(student.getStudentId());
                view.setStudentName(student.getStudentName());
                view.setSchoolId(student.getSchoolId());
                view.setSchoolName(student.getSchoolName());
                if(student.getAttendClassLatestDay() != null){
                    view.setAttendClass(true);
                }
            }

            List<ActivityOrderCourse> courseList = orderCourseMap.get(p.getOrderId());
            if(CollectionUtils.isNotEmpty(courseList)){
                ActivityOrderCourse course = courseList.get(0);
                view.setCourseName(course.getCourseName());
            }


            resultList.add(view);
        });

        return resultList;
    }


    // count >= 0   count == 0 : 取消赠送
    public MapMessage receiveGift(String orderId, String giftId, int count){
        if(count < 0){
            return MapMessage.errorMessage("礼品数量有误！");
        }
        ActivityOrder order = activityOrderDao.loadByOid(orderId);
        if(order == null){
            return MapMessage.errorMessage("订单不存在");
        }
        List<ActivityOrderGift> orderGiftList = activityOrderGiftDao.loadByOid(orderId);
        ActivityOrderGift orderGift = null;
        if(CollectionUtils.isNotEmpty(orderGiftList)){
            orderGift = orderGiftList.stream().filter(p -> Objects.equals(SafeConverter.toString(p.getGiftId(), ""), giftId)).findFirst().orElse(null);
        }

        if(orderGift != null && count > 0 && SafeConverter.toInt(orderGift.getCount()) == count){
            return MapMessage.errorMessage("礼品已领取！");
        }

        if(orderGift == null){
            orderGift = new ActivityOrderGift();
            orderGift.setOrderId(orderId);
            orderGift.setGiftId(giftId);
        }
        orderGift.setCount(count);
        activityOrderGiftDao.upsert(orderGift);
        return MapMessage.successMessage();
    }

//    public Map<String, Object> getNewUserData(String activityId, Long userId){
//
//        Map<String, Object> dataMap = new HashMap<>();
//        AgentActivity activity = agentActivityDao.load(activityId);
//        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
//            dataMap.put("newUserCount", 0);
//            dataMap.put("newUserRate", 0d);
//            return dataMap;
//        }
//
//        Set<Long> userIds = new HashSet<>();
//        userIds.add(userId);
//        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
//        if(CollectionUtils.isNotEmpty(managedGroupIds)){
//            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
//            userIds.addAll(groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
//        }
//
//        Date endDate = new Date();
//        if(activity.getEndDate() != null && activity.getEndDate().before(endDate)){
//            endDate = activity.getEndDate();
//        }
//        Date startDate = null;
//        if(activity.getStartDate() != null){
//            startDate = activity.getStartDate();
//        }
//
//        Integer orderUserCount = 0;
//        Integer newUserCount = 0;
//
//        List<ActivityOrder> orderList = activityOrderDao.loadByActivityAndUserAndTime(activityId, userIds, startDate, endDate);
//
//        if(CollectionUtils.isNotEmpty(orderList)){
//            Date targetDate = startDate == null? DateUtils.addMonths(new Date(), -1) : startDate;
//            Set<Long> orderUserIds = orderList.stream().map(ActivityOrder::getOrderUserId).collect(Collectors.toSet());
//            orderUserCount = orderUserIds.size();
//
//            Set<Long> newOrderUserIds = orderList.stream().filter(p -> p.getOrderUserRegTime() != null && p.getOrderUserRegTime().after(targetDate)).map(ActivityOrder::getOrderUserId).collect(Collectors.toSet());
//            newUserCount = newOrderUserIds.size();
//        }
//
//        dataMap.put("newUserCount", newUserCount);
//        dataMap.put("newUserRate", MathUtils.doubleDivide(newUserCount, orderUserCount));
//        return dataMap;
//    }

    public Map<String, Object> getNewUserData(String activityId, Long userId){

        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return new HashMap<>();
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

        ActivityExtend extend = activityExtendDao.loadByAid(activityId);
        // 组团形式
        if(extend != null && extend.getForm() != null && extend.getForm() == 3){
            return getGroupNewUser(activityId, userIds, startDate, endDate, userId);
        }else {
            return getOrderNewUser(activityId, userIds, startDate, endDate);
        }
    }

    private Map<String, Object> getOrderNewUser(String activityId, Collection<Long> userIds, Date startDate, Date endDate){
        Map<String, Object> dataMap = new HashMap<>();
        if(CollectionUtils.isEmpty(userIds)){
            dataMap.put("userCount", 0);
            dataMap.put("newUserCount", 0);
            return dataMap;
        }

        int userCount = 0;
        int newUserCount = 0;

        List<ActivityOrder> orderList = activityOrderDao.loadByActivityAndUserAndTime(activityId, userIds, startDate, endDate);

        if(CollectionUtils.isNotEmpty(orderList)){
            Date targetDate = startDate == null? DateUtils.addMonths(new Date(), -1) : startDate;
            Set<Long> orderUserIds = orderList.stream().map(ActivityOrder::getOrderUserId).collect(Collectors.toSet());
            userCount = orderUserIds.size();

            Set<Long> newOrderUserIds = orderList.stream().filter(p -> p.getOrderUserRegTime() != null && p.getOrderUserRegTime().after(targetDate)).map(ActivityOrder::getOrderUserId).collect(Collectors.toSet());
            newUserCount = newOrderUserIds.size();
        }
        dataMap.put("userCount", userCount);
        dataMap.put("newUserCount", newUserCount);
        return dataMap;
    }

    private Map<String, Object> getGroupNewUser(String activityId, Collection<Long> userIds, Date startDate, Date endDate, Long userId){
        Map<String, Object> dataMap = new HashMap<>();
        if(CollectionUtils.isEmpty(userIds)){
            dataMap.put("userCount", 0);
            dataMap.put("newUserCount", 0);
            dataMap.put("dictUserCount", 0);
            return dataMap;
        }



        int userCount = 0;
        int newUserCount = 0;
        int dictUserCount = 0;

        List<ActivityGroup> groupList = groupService.loadByAidAndUidsAndTime(activityId, userIds, startDate, endDate);

        if(CollectionUtils.isNotEmpty(groupList)){
            List<String> groupIds = groupList.stream().map(ActivityGroup::getGroupId).collect(Collectors.toList());
            List<ActivityGroupUser> groupUserList = groupUserService.loadByGidsAndTime(groupIds, startDate, endDate);

            Set<Long> joinUserIds = groupUserList.stream().map(ActivityGroupUser::getJoinUserId).collect(Collectors.toSet());
            userCount = joinUserIds.size();

            Date targetDate = startDate == null? DateUtils.addMonths(new Date(), -1) : startDate;
            Set<Long> joinNewUserIds = groupUserList.stream().filter(p -> p.getJoinUserRegTime() != null && p.getJoinUserRegTime().after(targetDate)).map(ActivityGroupUser::getJoinUserId).collect(Collectors.toSet());
            newUserCount = joinNewUserIds.size();

            List<Long> managedSchools = baseOrgService.getManagedSchoolList(userId);
            dictUserCount = (int)groupUserList.stream().filter(p -> p.getSchoolId() != null && managedSchools.contains(p.getSchoolId())).count();
        }

        dataMap.put("userCount", userCount);
        dataMap.put("newUserCount", newUserCount);
        dataMap.put("dictUserCount", dictUserCount);
        return dataMap;
    }

    public MapMessage refreshAttendCourseStatistics(String activityId, Collection<Long> userIds){
        if(CollectionUtils.isEmpty(userIds)){
            return MapMessage.successMessage();
        }
        userIds.forEach(p -> attendCourseStatisticsService.attendCourseStatisticsByUid(activityId, p, getActivityStartToNowEveryDays(activityId)));
        return MapMessage.successMessage();
    }

    public List<ActivityCouponStatisticsView> getCouponStatisticsDataList(String activityId, Collection<Long> userIds){
        List<ActivityCouponStatisticsView> dataViewList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return dataViewList;
        }
        List<Integer> days = getActivityEveryDays(activityId);
        if(CollectionUtils.isEmpty(days)){
            return dataViewList;
        }
        dataViewList.addAll(couponStatisticsService.getUserDataView(activityId, userIds, days));
        return dataViewList;
    }


    public List<ActivitySchoolRecordView> getDictSchoolStatisticsData(String activityId, Long userId){
        List<ActivitySchoolRecordView> dataList = new ArrayList<>();
        List<ActivityGroupUser> groupUserList = getActivityGroupUserList(activityId, userId);
        if(CollectionUtils.isEmpty(groupUserList)){
            return dataList;
        }

        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
        List<ActivityGroupUser> targetList = groupUserList.stream().filter(p -> p.getSchoolId() != null && managedSchoolList.contains(p.getSchoolId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(targetList)){
            Map<Long, List<ActivityGroupUser>> schoolDataMap = targetList.stream().collect(Collectors.groupingBy(ActivityGroupUser::getSchoolId));
            schoolDataMap.forEach((k, v) -> {
                ActivitySchoolRecordView view = new ActivitySchoolRecordView();
                view.setSchoolId(k);
                view.setSchoolName(v.get(0).getSchoolName());
                view.setJoinUserCount(v.size());
                dataList.add(view);
            });
        }
        return dataList;
    }

    // 获取改用户名下的所有组团用户(不包括下属人员数据)
    private List<ActivityGroupUser> getActivityGroupUserList(String activityId, Long userId){
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return new ArrayList<>();
        }


        Date endDate = new Date();
        if(activity.getEndDate() != null && activity.getEndDate().before(endDate)){
            endDate = activity.getEndDate();
        }
        Date startDate = null;
        if(activity.getStartDate() != null){
            startDate = activity.getStartDate();
        }

        List<ActivityGroup> groupList = groupService.loadByAidAndUidsAndTime(activityId, Collections.singleton(userId), startDate, endDate);

        List<ActivityGroupUser> resultList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(groupList)) {
            List<String> groupIds = groupList.stream().map(ActivityGroup::getGroupId).collect(Collectors.toList());
            List<ActivityGroupUser> tmpList = groupUserService.loadByGidsAndTime(groupIds, startDate, endDate);
            if(CollectionUtils.isNotEmpty(tmpList)){
                resultList.addAll(tmpList);
            }
        }
        return resultList;
    }


    public List<ActivityOrder> getActivityOrderList(Date startDate,Long userId){
        //获取未过期的活动
        List<AgentActivity> activityList = agentActivityDao.loadByStartDate(startDate).stream()
                .filter(p -> p.getEndDate().after(new Date()))
                .collect(Collectors.toList());

        //活动扩展信息
        List<String> activityIds = activityList.stream().map(AgentActivity::getId).collect(Collectors.toList());
        Map<String, ActivityExtend> activityExtendMap = activityExtendDao.loadByAids(activityIds);
        if (MapUtils.isEmpty(activityExtendMap)){
            return Collections.emptyList();
        }
        //普通推广活动
        List<ActivityExtend> activityExtendList = new ArrayList<>(activityExtendMap.values()).stream().filter(p -> p.getForm() == 1).collect(Collectors.toList());
        Set<String> finalActivityIds = activityExtendList.stream().map(ActivityExtend::getActivityId).collect(Collectors.toSet());

        Map<String, List<ActivityOrder>> activityOrderMap = activityOrderDao.loadByAidsAndUid(finalActivityIds,userId);
        if (MapUtils.isEmpty(activityOrderMap)){
            return Collections.emptyList();
        }
        return activityOrderMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
