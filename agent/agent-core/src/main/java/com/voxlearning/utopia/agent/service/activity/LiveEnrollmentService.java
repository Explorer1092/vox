package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.constants.QRCodeBusinessType;
import com.voxlearning.utopia.agent.dao.mongo.activity.*;
import com.voxlearning.utopia.agent.dao.mongo.qrcode.UserQrCodeDao;
import com.voxlearning.utopia.agent.persist.entity.activity.*;
import com.voxlearning.utopia.agent.persist.entity.qrcode.UserQrCode;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.qrcode.UserQrCodeService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.activity.LiveEnrollmentOrderView;
import com.voxlearning.utopia.agent.view.activity.LiveEnrollmentSchoolView;
import com.voxlearning.utopia.agent.view.activity.LiveEnrollmentView;
import com.voxlearning.utopia.agent.view.activity.LiveEnrollmentWithPartners;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInBusinessType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.signin.SignInRecordLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * LiveEnrollmentService
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Named
public class LiveEnrollmentService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private LiveEnrollmentDao liveEnrollmentDao;
    @Inject
    private SignInRecordLoaderClient signInRecordLoaderClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private SearchService searchService;
    @Inject
    private UserQrCodeService userQrCodeService;
    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private UserQrCodeDao userQrCodeDao;
    @Inject
    private LiveEnrollmentOrderDao liveEnrollmentOrderDao;
    @Inject
    private LiveEnrollmentUserStatisticsDao liveEnrollmentUserStatisticsDao;
    @Inject
    private LiveEnrollmentSchoolStatisticsDao liveEnrollmentSchoolStatisticsDao;
    @Inject
    private LiveEnrollmentRegionStatisticsDao liveEnrollmentRegionStatisticsDao;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private LiveEnrollmentOrderRefundDao liveEnrollmentOrderRefundDao;
    @Inject
    private LiveEnrollmentPositiveService liveEnrollmentPositiveService;
    @Inject
    AgentCacheSystem agentCacheSystem;

    public List<LiveEnrollmentView> getActivityList(Long userId, Date startDate, Date endDate) {

        List<LiveEnrollmentWithPartners> userEnrollments = getActivityWithPartners(userId, startDate, endDate);
        if (CollectionUtils.isEmpty(userEnrollments)) {
            return Collections.emptyList();
        }
        List<String> signIds = userEnrollments.stream().map(LiveEnrollment::getSignInRecordId).collect(Collectors.toList());
        Map<String, SignInRecord> signInRecordMap = signInRecordLoaderClient.loads(signIds);

        List<Long> schoolIds = userEnrollments.stream().map(LiveEnrollment::getSchoolId).collect(Collectors.toList());
        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIds).getUninterruptibly();

        List<LiveEnrollmentView> viewList = new ArrayList<>();
        userEnrollments.forEach(p -> {
            LiveEnrollmentView view = new LiveEnrollmentView();
            view.setId(p.getId());
            view.setSchoolId(p.getSchoolId());
            view.setSchoolName(p.getSchoolName());
            view.setUserId(p.getUserId());
            view.setWorkTime(p.getWorkTime());
            SignInRecord signInRecord = signInRecordMap.get(p.getSignInRecordId());
            if (signInRecord != null) {
                view.setAddress(signInRecord.getAddress());
                SchoolExtInfo schoolExtInfo = extInfoMap.get(p.getSchoolId());
                if (schoolExtInfo != null) {
                    MapMessage distanceMessage = AmapMapApi.GetDistance(schoolExtInfo.getLongitude(), schoolExtInfo.getLatitude(), schoolExtInfo.getCoordinateType(), signInRecord.getLongitude(), signInRecord.getLatitude(), signInRecord.getCoordinateType());
                    if (distanceMessage.isSuccess()) {
                        view.setDistance(SafeConverter.toLong(distanceMessage.get("res")));
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(p.getPartnerList())) {
                List<String> partners = p.getPartnerList().stream().map(LiveEnrollmentWithPartners.LiveEnrollmentPartner::getPartnerName).collect(Collectors.toList());
                view.setPartners(partners);
            }
            viewList.add(view);
        });

        return viewList;
    }

    public MapMessage deleteActivity(String id) {
        LiveEnrollment liveEnrollment = liveEnrollmentDao.load(id);
        if (liveEnrollment == null) {
            return MapMessage.successMessage();
        }
        if (!DateUtils.isSameDay(liveEnrollment.getWorkTime(), new Date())) {
            return MapMessage.errorMessage("只能删除当天的签到数据");
        }
        MapMessage message = new MapMessage();
        message.setSuccess(liveEnrollmentDao.remove(id));


        Integer day = SafeConverter.toInt(DateUtils.dateToString(liveEnrollment.getWorkTime(), "yyyyMMdd"));
        updateUserStatisticsByUser(liveEnrollment.getUserId(), day);

        // 用户退出活动后，计算其余参与该活动人员的订单量
        LiveEnrollmentWithPartners activity = getActivityWithPartnersBySchool(liveEnrollment.getSchoolId(), day);
        if (activity != null) {
            updateUserStatisticsByActivity(activity);
        }
        return message;
    }

    public MapMessage joinActivity(Long schoolId, String signInId, Long userId, String userName) {
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在！");
        }
        SignInRecord signInRecord = signInRecordLoaderClient.load(signInId);
        if (signInRecord == null || signInRecord.getBusinessType() != SignInBusinessType.LIVE_ENROLLMENT) {
            return MapMessage.errorMessage("没有获得您的位置信息！");
        }

        DayRange dayRange = DayRange.current();
        List<LiveEnrollment> liveEnrollmentList = liveEnrollmentDao.loadByUserId(userId, dayRange.getStartDate(), dayRange.getEndDate());
        if (CollectionUtils.isNotEmpty(liveEnrollmentList)) {
            if (liveEnrollmentList.stream().anyMatch(p -> Objects.equals(p.getSchoolId(), schoolId))) {
                return MapMessage.errorMessage("您已签到该学校！");
            }
            return MapMessage.errorMessage("每天只可签到一所学校");

        }
//        if(DayUtils.isWorkDay(SafeConverter.toInt(DateUtils.dateToString(dayRange.getStartDate(), "yyyyMMdd")))){
//            WeekRange weekRange = WeekRange.current();
//            List<LiveEnrollment> weekLiveEnrollmentList = liveEnrollmentDao.loadByUserId(userId, weekRange.getStartDate(), weekRange.getEndDate());
//            Set<Long> schoolIdSet = weekLiveEnrollmentList.stream().map(LiveEnrollment::getSchoolId).collect(Collectors.toSet());
//            if(schoolIdSet.size() >= 3 && !schoolIdSet.contains(schoolId)){
//                return MapMessage.errorMessage("每周工作日最多只能签到三所学校");
//            }
//        }

        LiveEnrollment item = new LiveEnrollment();
        item.setSchoolId(school.getId());
        item.setSchoolName(school.getCname());
        item.setSignInRecordId(signInId);
        item.setUserId(userId);
        item.setUserName(userName);
        item.setWorkTime(new Date());

        liveEnrollmentDao.insert(item);

        updateUserStatisticsByUser(userId, SafeConverter.toInt(DateUtils.dateToString(item.getWorkTime(), "yyyyMMdd")));
        return MapMessage.successMessage();
    }


    public List<LiveEnrollmentSchoolView> searchSchool(Long userId, String searchKey, Double longitude, Double latitude, Integer pageNo, Integer pageSize) {

        List<SchoolLevel> schoolLevelList = new ArrayList<>();
//        schoolLevelList.add(SchoolLevel.HIGH);
//        schoolLevelList.add(SchoolLevel.MIDDLE);
        schoolLevelList.add(SchoolLevel.JUNIOR);


        List<Long> groupIds = baseOrgService.getGroupIdListByUserId(userId);
        Set<Integer> regionCodes = new HashSet<>();
        groupIds.forEach(p -> {
            List<Integer> groupRegionCodes = baseOrgService.getGroupRegionCodeList(p);
            if (CollectionUtils.isNotEmpty(groupRegionCodes)) {
                regionCodes.addAll(groupRegionCodes);
            }
        });

        Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
        Set<Integer> targetRegions = new HashSet<>();
        regionMap.values().forEach(p -> {
            if (p.fetchRegionType() == RegionType.COUNTY) {
                targetRegions.add(p.getCityCode());
            } else {
                targetRegions.add(p.getId());
            }
        });

        Page<SchoolEsInfo> esInfoPage = searchService.querySchoolFromEsByRegionCodes(targetRegions, searchKey, schoolLevelList, longitude, latitude, pageNo, pageSize);
        List<SchoolEsInfo> esInfoList = esInfoPage.getContent();
        if (CollectionUtils.isEmpty(esInfoList)) {
            return Collections.emptyList();
        }

        DayRange dayRange = DayRange.current();
        List<LiveEnrollmentSchoolView> schoolList = new ArrayList<>();
        esInfoList.forEach(p -> {
            LiveEnrollmentSchoolView schoolView = new LiveEnrollmentSchoolView();
            Long schoolId = SafeConverter.toLong(p.getId());
            schoolView.setSchoolId(schoolId);
            schoolView.setSchoolName(p.getCname());
            schoolView.setCityName(p.getCityName());
            schoolView.setCountyName(p.getCountyName());
            schoolView.setDistance(p.getGenDistance());

            schoolView.setIsSignIn(false);
            List<LiveEnrollment> enrollmentList = liveEnrollmentDao.loadBySchoolId(schoolId, dayRange.getStartDate(), dayRange.getEndDate());
            if (CollectionUtils.isNotEmpty(enrollmentList) && enrollmentList.stream().anyMatch(k -> Objects.equals(k.getUserId(), userId))) {
                schoolView.setIsSignIn(true);
            }

            schoolList.add(schoolView);
        });
        return schoolList;
    }

    public List<LiveEnrollmentOrderView> getActivityOrderList(Long userId, Date startDate, Date endDate) {
        if (startDate == null) {
            startDate = DateUtils.addMonths(new Date(), -3);
        }
        if (endDate == null) {
            endDate = new Date();
        }

        List<Map<String, Object>> paramList = new ArrayList<>();

        Map<Long, String> userRelatedIdMap = new HashMap<>();
        String userRelatedId = userQrCodeService.getRelatedId(userId, QRCodeBusinessType.LIVE_ENROLLMENT);
        if (StringUtils.isNotBlank(userRelatedId)) {
            userRelatedIdMap.put(userId, userRelatedId);

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("deliveryId", userRelatedId);
            itemMap.put("startPayTime", startDate.getTime());
            itemMap.put("endPayTime", endDate.getTime());
            paramList.add(itemMap);
        }


        List<LiveEnrollmentWithPartners> userEnrollments = getActivityWithPartners(userId, startDate, endDate);
        userEnrollments.forEach(p -> {
            if (CollectionUtils.isNotEmpty(p.getPartnerList())) {
                DayRange dayRange = DayRange.newInstance(p.getWorkTime().getTime());
                for (LiveEnrollmentWithPartners.LiveEnrollmentPartner partner : p.getPartnerList()) {
                    String partnerRelatedId = userRelatedIdMap.get(partner.getPartnerId());
                    if (StringUtils.isBlank(partnerRelatedId)) {
                        partnerRelatedId = userQrCodeService.getRelatedId(partner.getPartnerId(), QRCodeBusinessType.LIVE_ENROLLMENT);
                        if (StringUtils.isBlank(partnerRelatedId)) {
                            continue;
                        }
                        userRelatedIdMap.put(partner.getPartnerId(), partnerRelatedId);
                    }

                    Map<String, Object> partnerMap = new HashMap<>();
                    partnerMap.put("deliveryId", partnerRelatedId);
                    partnerMap.put("startPayTime", dayRange.getStartTime());
                    partnerMap.put("endPayTime", dayRange.getEndTime());
                    paramList.add(partnerMap);
                }
            }
        });

        MapMessage message = LiveEnrollmentRemoteClient.loadOrderList(paramList);
        if (!message.isSuccess()) {
            return Collections.emptyList();
        }

        Map<String, Object> dataMap = (Map<String, Object>) message.get("data");
        if (MapUtils.isEmpty(dataMap) || !dataMap.containsKey("orders")) {
            return Collections.emptyList();
        }
        List<LiveEnrollmentOrderView> orderViewList = new ArrayList<>();

        List<Map<String, Object>> orderList = (List<Map<String, Object>>) dataMap.get("orders");
        orderList.forEach(p -> {
            String courseStage = SafeConverter.toString(p.get("courseStage"), "");
            String orderDeliveryId = SafeConverter.toString(p.get("deliveryId"), "");
            //不是自己的中学订单不展示
            if (StringUtils.isNotBlank(courseStage) && (courseStage.equals("初中") || courseStage.equals("高中")) && !userRelatedId.equals(orderDeliveryId)) {
                return;
            }
            LiveEnrollmentOrderView order = new LiveEnrollmentOrderView();
            order.setOrderId(SafeConverter.toString(p.get("orderId"), ""));
            order.setDeliveryId(SafeConverter.toString(p.get("deliveryId"), ""));
            order.setParentId(SafeConverter.toLong(p.get("platformPid")));
            order.setPhoneNo(SafeConverter.toString(p.get("phoneNo"), ""));
            order.setStudentId(SafeConverter.toLong(p.get("platformSid")));
            order.setStudentName(SafeConverter.toString(p.get("studentName"), ""));
            order.setOrderTime(new Date(SafeConverter.toLong(p.get("payTime"))));
            order.setOrderStatus(SafeConverter.toInt(p.get("orderStatus"), 1));
            order.setGiftReceivedStatus(SafeConverter.toInt(p.get("giftReceivedStatus"), 2));
            order.setReceiptAddress(SafeConverter.toString(p.get("receiptAddress"), ""));
            order.setPayPrice(SafeConverter.toLong(p.get("payPrice"), 0));
            order.setCourseType(SafeConverter.toInt(p.get("courseType"), 1));
//            String courseGrade = SafeConverter.toString(p.get("courseGrade"));
//            List<Integer> gradeList = StringUtils.toIntegerList(courseGrade);
//            order.setCourseGrades(gradeList);
//            order.setCourseSubject(SafeConverter.toString(p.get("courseSubject")));
//            order.setCourseName(SafeConverter.toString(p.get("courseName")));
            order.setCourseStage(courseStage);

            if (DateUtils.stringToDate("2018-12-26", DateUtils.FORMAT_SQL_DATE).before(order.getOrderTime()) && !isCalculateData(order.getOrderTime())) {
                order.setNonCalculateTime(true);
            } else {
                order.setNonCalculateTime(false);
            }

            orderViewList.add(order);
        });

        setOrderOtherData(orderViewList, userEnrollments);
        return orderViewList;

    }

    private void setOrderOtherData(List<LiveEnrollmentOrderView> orderList, List<LiveEnrollmentWithPartners> userEnrollments) {
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        Set<Long> parentSet = new HashSet<>();
        Set<Long> studentSet = new HashSet<>();
        orderList.forEach(p -> {
            if (p.getParentId() != null) {
                parentSet.add(p.getParentId());
            }
            if (p.getStudentId() != null) {
                studentSet.add(p.getStudentId());
            }
        });
        Map<Long, User> userMap = userLoaderClient.loadUsers(parentSet);

        Map<Long, Long> studentSchoolIdMap = new HashMap<>();
        studentSet.forEach(p -> {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(p);
            if (studentDetail != null && studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null) {
                studentSchoolIdMap.put(p, studentDetail.getClazz().getSchoolId());
            }
        });

        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader().loadSchools(studentSchoolIdMap.values()).getUninterruptibly();

        Map<Long, School> userSchoolMap = new HashMap<>();
        studentSchoolIdMap.forEach((k, v) -> {
            School school = schoolMap.get(v);
            if (school != null) {
                userSchoolMap.put(k, school);
            }
        });

        orderList.forEach(p -> {
            User parent = userMap.get(p.getParentId());
            if (parent != null && parent.getProfile() != null) {
                p.setParentName(parent.getProfile().getRealname());
            }

            School school = userSchoolMap.get(p.getStudentId());
            if (school != null) {
                p.setSchoolId(school.getId());
                p.setSchoolName(school.getCname());
            }
            //只有小学需要显示合作伙伴  中学订单不需要显示合作伙伴
            if ((StringUtils.isBlank(p.getCourseStage()) || p.getCourseStage().equals("小学")) && CollectionUtils.isNotEmpty(userEnrollments)) {
                DayRange dayRange = DayRange.newInstance(p.getOrderTime().getTime());
                LiveEnrollmentWithPartners liveEnrollment = userEnrollments.stream()
                        .filter(k -> dayRange.contains(k.getWorkTime()))
                        .findFirst().orElse(null);
                if (liveEnrollment != null && CollectionUtils.isNotEmpty(liveEnrollment.getPartnerList())) {
                    List<Map<String, Object>> partners = new ArrayList<>();
                    liveEnrollment.getPartnerList().forEach(e -> {
                        Map<String, Object> partner = new HashMap<>();
                        partner.put("partnerId", e.getPartnerId());
                        partner.put("partnerName", e.getPartnerName());
                        partners.add(partner);
                    });
                    p.setPartners(partners);
                }
            }
        });
    }


    private List<LiveEnrollmentWithPartners> getActivityWithPartners(Long userId, Date startDate, Date endDate) {
        List<LiveEnrollment> userEnrollments = liveEnrollmentDao.loadByUserId(userId, startDate, endDate);
        if (CollectionUtils.isEmpty(userEnrollments)) {
            return Collections.emptyList();
        }


        List<LiveEnrollmentWithPartners> resultList = new ArrayList<>();

        userEnrollments.forEach(p -> {
            LiveEnrollmentWithPartners item = convert(p);
            Long schoolId = p.getSchoolId();
            DayRange dayRange = DayRange.newInstance(p.getWorkTime().getTime());
            List<LiveEnrollment> schoolEnrollments = liveEnrollmentDao.loadBySchoolId(schoolId, dayRange.getStartDate(), dayRange.getEndDate());
            if (CollectionUtils.isNotEmpty(schoolEnrollments)
                    && schoolEnrollments.stream().anyMatch(k -> !Objects.equals(k.getUserId(), userId))) {
                schoolEnrollments.stream().filter(k -> !Objects.equals(k.getUserId(), userId))
                        .forEach(e -> {
                            LiveEnrollmentWithPartners.LiveEnrollmentPartner partner = new LiveEnrollmentWithPartners.LiveEnrollmentPartner();
                            partner.setId(e.getId());
                            partner.setPartnerId(e.getUserId());
                            partner.setPartnerName(e.getUserName());
                            partner.setWorkTime(e.getWorkTime());
                            item.getPartnerList().add(partner);
                        });
            }
            resultList.add(item);
        });

        return resultList;
    }

    private LiveEnrollmentWithPartners getActivityWithPartnersBySchool(Long schoolId, Integer day) {
        Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        DayRange dayRange = DayRange.newInstance(date.getTime());
        List<LiveEnrollment> enrollmentList = liveEnrollmentDao.loadBySchoolId(schoolId, dayRange.getStartDate(), dayRange.getEndDate());
        if (CollectionUtils.isEmpty(enrollmentList)) {
            return null;
        }

        LiveEnrollmentWithPartners result = convert(enrollmentList.get(0));
        for (int i = 1; i < enrollmentList.size(); i++) {
            LiveEnrollment item = enrollmentList.get(i);
            LiveEnrollmentWithPartners.LiveEnrollmentPartner partner = new LiveEnrollmentWithPartners.LiveEnrollmentPartner();
            partner.setId(item.getId());
            partner.setPartnerId(item.getUserId());
            partner.setPartnerName(item.getUserName());
            partner.setWorkTime(item.getWorkTime());
            result.getPartnerList().add(partner);
        }
        return result;
    }


    private LiveEnrollmentWithPartners convert(LiveEnrollment item) {
        if (item == null) {
            return null;
        }
        LiveEnrollmentWithPartners result = new LiveEnrollmentWithPartners();
        BeanUtils.copyProperties(item, result);
        return result;
    }


    public MapMessage judgeDeliveryId(Date startDate, Date endDate) {
        List<LiveEnrollmentOrder> list = liveEnrollmentOrderDao.loadByDate(getStartDateWithDefault(startDate), getEndDateWithDefault(endDate));
        Set<String> deliveryIds = list.stream().map(LiveEnrollmentOrder::getDeliveryId).collect(Collectors.toSet());
        Set<String> result = new HashSet<>();
        if (CollectionUtils.isNotEmpty(deliveryIds)) {
            deliveryIds.forEach(p -> {
                UserQrCode userQrCode = userQrCodeDao.loadByRelatedId(QRCodeBusinessType.LIVE_ENROLLMENT, p);
                if (userQrCode == null || userQrCode.getUserId() == null) {
                    result.add(p);
                }

            });
        }
        return MapMessage.successMessage().add("没有对应用户的投放ID", result);
    }


    public void saveLiveEnrollmentOrder(String deliveryId, String orderId, Date payTime, Long parentId, Long studentId, Long payPrice, Integer courseType, List<Integer> courseGrades, String courseSubject, String courseName, String courseStage) {
        List<LiveEnrollmentOrder> orderList = liveEnrollmentOrderDao.loadByOrderId(orderId);
        if (CollectionUtils.isNotEmpty(orderList)) {
            return;
        }

        LiveEnrollmentOrder order = new LiveEnrollmentOrder();
        order.setDeliveryId(deliveryId);
        order.setOrderId(orderId);
        order.setPayTime(payTime == null ? new Date() : payTime);
        order.setParentId(parentId);
        order.setStudentId(studentId);
        if (studentId != null && studentId > 0) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null && studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null) {
                Long schoolId = studentDetail.getClazz().getSchoolId();
                School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
                if (school != null) {
                    order.setSchoolId(schoolId);
                    order.setSchoolName(school.getCname());
                }
            }
        }
        order.setPayPrice(payPrice);
        order.setCourseType(courseType);
        order.setCourseGrades(courseGrades);
        order.setCourseSubject(courseSubject);
        order.setCourseName(courseName);
        order.setCourseStage(courseStage);
        liveEnrollmentOrderDao.insert(order);
        AlpsThreadPool.getInstance().submit(() -> afterSaveOrder(order));

    }

    private boolean isCalculateData(LiveEnrollmentOrder order) {
        if (order != null) {
            if (StringUtils.isNotBlank(order.getCourseStage())) {
                if (order.getCourseStage().equals("小学")) {
                    if (order.getCourseType() == 1) { // 低价课
                        return isCalculateData(order.getPayTime());
                    } else if (order.getCourseType() == 2) { // 正价课
                        return true;
                    }
                } else if (order.getCourseStage().equals("初中")) {
                    return true;
                } else if (order.getCourseStage().equals("高中")) {
                    return true;
                }
            } else {
                if (order.getCourseType() == 1) { // 低价课
                    return isCalculateData(order.getPayTime());
                } else if (order.getCourseType() == 2) { // 正价课
                    return true;
                }
            }
        }
        return false;
    }

    // 工作日指定时间点之前的订单不做统计
    private boolean isCalculateData(Date date) {
        Integer day = SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
        if (day == 20181224 || day == 20181225) {
            return true;
        }

        boolean isWorkDay = DayUtils.isWorkDay(day);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (!isWorkDay
                || (hour >= 15 && hour < 20)
                || (hour == 14 && minute >= 30)
        ) {
            return true;
        }
        return false;
    }

    private void afterSaveOrder(LiveEnrollmentOrder order) {
        if (order == null) {
            return;
        }

        if (isCalculateData(order)) {
            if (order.getCourseType() == 1) {
                updateUserStatistics(order);
                updateSchoolStatistics(order);
                updateRegionStatistics(order);
            } else {
                liveEnrollmentPositiveService.updateUserPositiveStatistics(order, 1, null, null);
            }
        }
    }

    // 更新用户当日订单数
    private void updateUserStatistics(LiveEnrollmentOrder order) {
        if (order == null || !isCalculateData(order)) {
            return;
        }
        Integer day = SafeConverter.toInt(DateUtils.dateToString(order.getPayTime(), "yyyyMMdd"));
        DayRange dayRange = DayRange.newInstance(order.getPayTime().getTime());
        UserQrCode userQrCode = userQrCodeDao.loadByRelatedId(QRCodeBusinessType.LIVE_ENROLLMENT, order.getDeliveryId());
        if (userQrCode != null && userQrCode.getUserId() != null) {
            AgentUser user = baseOrgService.getUser(userQrCode.getUserId());
            if (user == null) {
                return;
            }
            // 初高中订单， 无合作模式
            if (StringUtils.isNotBlank(order.getCourseStage()) && (order.getCourseStage().equals("初中") || order.getCourseStage().equals("高中"))) {
                Map<Long, LiveEnrollmentUserStatistics> userDataMap = liveEnrollmentUserStatisticsDao.loadByUserIds(Collections.singleton(user.getId()), day);
                LiveEnrollmentUserStatistics userData = MapUtils.isEmpty(userDataMap) ? null : userDataMap.get(user.getId());
                if (userData == null) {
                    userData = new LiveEnrollmentUserStatistics();
                    userData.setUserId(user.getId());
                    userData.setUserName(user.getRealName());
                    userData.setDay(day);
                }
                userData.setMiddleOrderNum(SafeConverter.toDouble(userData.getMiddleOrderNum()) + 1);
                liveEnrollmentUserStatisticsDao.upsert(userData);
            } else {    // 小学订单， 有合作模式
                List<LiveEnrollmentWithPartners> activityList = getActivityWithPartners(userQrCode.getUserId(), dayRange.getStartDate(), dayRange.getEndDate());
                if (CollectionUtils.isEmpty(activityList) || CollectionUtils.isEmpty(activityList.get(0).getPartnerList())) {
                    // 没有进校，或者没有合作伙伴的情况下，产生的订单归该用户单独所有
                    Map<Long, LiveEnrollmentUserStatistics> userDataMap = liveEnrollmentUserStatisticsDao.loadByUserIds(Collections.singleton(user.getId()), day);
                    LiveEnrollmentUserStatistics userData = MapUtils.isEmpty(userDataMap) ? null : userDataMap.get(user.getId());
                    if (userData == null) {
                        userData = new LiveEnrollmentUserStatistics();
                        userData.setUserId(user.getId());
                        userData.setUserName(user.getRealName());
                        userData.setDay(day);
                    }
                    userData.setOrderNum(SafeConverter.toDouble(userData.getOrderNum()) + 1);
                    liveEnrollmentUserStatisticsDao.upsert(userData);
                } else {
                    // 有合伙人的情况下， 重新计算每个人的订单数
                    LiveEnrollmentWithPartners activity = activityList.get(0);
                    updateUserStatisticsByActivity(activity);
                }
            }


        }
    }

    // 计算指定用户在指定日期的订单量
    public void updateUserStatisticsByUser(Long userId, Integer day) {
        AgentUser user = baseOrgService.getUser(userId);
        if (user == null) {
            return;
        }

        Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        DayRange dayRange = DayRange.newInstance(date.getTime());
        List<LiveEnrollmentWithPartners> activityList = getActivityWithPartners(userId, dayRange.getStartDate(), dayRange.getEndDate());
        if (CollectionUtils.isEmpty(activityList)) {
            // 没有进校，产生的订单归该用户单独所有
            double orderNum = 0D;
            double middleOrderNum = 0d;
            UserQrCode userQrCode = userQrCodeDao.loadByTypeAndUser(QRCodeBusinessType.LIVE_ENROLLMENT, userId);
            if (userQrCode != null && StringUtils.isNoneBlank(userQrCode.getRelatedId())) {
                Map<String, List<LiveEnrollmentOrder>> orderMap = liveEnrollmentOrderDao.loadByDeliveryIds(Collections.singleton(userQrCode.getRelatedId()), 1, dayRange.getStartDate(), dayRange.getEndDate());
                List<LiveEnrollmentOrder> orderList = orderMap.get(userQrCode.getRelatedId());
                if (CollectionUtils.isNotEmpty(orderList)) {
                    List<LiveEnrollmentOrder> targetOrderList = orderList.stream().filter(p -> isCalculateData(p)).collect(Collectors.toList());
                    List<LiveEnrollmentOrder> juniorOrderList = targetOrderList.stream().filter(p -> StringUtils.isBlank(p.getCourseStage()) || Objects.equals(p.getCourseStage(), "小学")).collect(Collectors.toList());
                    orderNum = orderNum + juniorOrderList.size();

                    List<LiveEnrollmentOrder> middleOrderList = targetOrderList.stream().filter(p -> StringUtils.isNotBlank(p.getCourseStage()) && (Objects.equals(p.getCourseStage(), "初中") || Objects.equals(p.getCourseStage(), "高中"))).collect(Collectors.toList());
                    middleOrderNum += middleOrderList.size();
                }
            }

            Map<Long, LiveEnrollmentUserStatistics> userDataMap = liveEnrollmentUserStatisticsDao.loadByUserIds(Collections.singleton(user.getId()), day);
            LiveEnrollmentUserStatistics userData = MapUtils.isEmpty(userDataMap) ? null : userDataMap.get(user.getId());
            if (userData == null) {
                userData = new LiveEnrollmentUserStatistics();
                userData.setUserId(user.getId());
                userData.setUserName(user.getRealName());
                userData.setDay(day);
            }
            userData.setOrderNum(orderNum);
            userData.setMiddleOrderNum(middleOrderNum);
            liveEnrollmentUserStatisticsDao.upsert(userData);
        } else {
            updateUserStatisticsByActivity(activityList.get(0));
        }
    }

    // 计算参与本次活动人员的订单量, 中学没有合作模式
    private void updateUserStatisticsByActivity(LiveEnrollmentWithPartners activity) {
        if (activity == null) {
            return;
        }
        Integer day = SafeConverter.toInt(DateUtils.dateToString(activity.getWorkTime(), "yyyyMMdd"));

        Map<Long, String> userDeliveryIdMap = new HashMap<>();
        UserQrCode userQrCode = userQrCodeDao.loadByTypeAndUser(QRCodeBusinessType.LIVE_ENROLLMENT, activity.getUserId());
        userDeliveryIdMap.put(activity.getUserId(), userQrCode == null ? "" : userQrCode.getRelatedId());

        if (CollectionUtils.isNotEmpty(activity.getPartnerList())) {
            activity.getPartnerList().forEach(p -> {
                UserQrCode partnerQrCode = userQrCodeDao.loadByTypeAndUser(QRCodeBusinessType.LIVE_ENROLLMENT, p.getPartnerId());
                userDeliveryIdMap.put(p.getPartnerId(), partnerQrCode == null ? "" : partnerQrCode.getRelatedId());
            });
        }

        DayRange dayRange = DayRange.newInstance(activity.getWorkTime().getTime());
        Map<String, List<LiveEnrollmentOrder>> deliveryOrderMap = liveEnrollmentOrderDao.loadByDeliveryIds(userDeliveryIdMap.values(), 1, dayRange.getStartDate(), dayRange.getEndDate());
        int totalOrderCount = 0;
        List<LiveEnrollmentOrder> targetOrderList = deliveryOrderMap.values().stream().flatMap(List::stream).filter(this::isCalculateData).collect(Collectors.toList());
        // 过滤出小学订单
        List<LiveEnrollmentOrder> juniorOrderList = targetOrderList.stream().filter(p -> StringUtils.isBlank(p.getCourseStage()) || Objects.equals(p.getCourseStage(), "小学")).collect(Collectors.toList());
        totalOrderCount += juniorOrderList.size();

        Double orderNum = MathUtils.doubleDivide(totalOrderCount, userDeliveryIdMap.size(), 2, BigDecimal.ROUND_FLOOR);

        Map<String, List<LiveEnrollmentOrder>> middleDeliveryOrderMap = targetOrderList.stream().filter(p -> StringUtils.isNotBlank(p.getCourseStage()) && (Objects.equals(p.getCourseStage(), "初中") || Objects.equals(p.getCourseStage(), "高中"))).collect(Collectors.groupingBy(LiveEnrollmentOrder::getDeliveryId));

        Map<Long, LiveEnrollmentUserStatistics> userDataMap = liveEnrollmentUserStatisticsDao.loadByUserIds(userDeliveryIdMap.keySet(), day);

        // 更新自己的订单数
        LiveEnrollmentUserStatistics userData = MapUtils.isEmpty(userDataMap) ? null : userDataMap.get(activity.getUserId());
        if (userData == null) {
            userData = new LiveEnrollmentUserStatistics();
            userData.setUserId(activity.getUserId());
            userData.setUserName(activity.getUserName());
            userData.setDay(day);
        }
        userData.setOrderNum(orderNum);

        String deliveryId = userDeliveryIdMap.get(activity.getUserId());
        List<LiveEnrollmentOrder> middleOrderList = middleDeliveryOrderMap.get(deliveryId);
        userData.setMiddleOrderNum(CollectionUtils.isEmpty(middleOrderList) ? 0 : SafeConverter.toDouble(middleOrderList.size()));

        liveEnrollmentUserStatisticsDao.upsert(userData);

        if (CollectionUtils.isNotEmpty(activity.getPartnerList())) {
            // 更新合伙人多订单数
            for (LiveEnrollmentWithPartners.LiveEnrollmentPartner partner : activity.getPartnerList()) {
                LiveEnrollmentUserStatistics partnerData = MapUtils.isEmpty(userDataMap) ? null : userDataMap.get(partner.getPartnerId());
                if (partnerData == null) {
                    partnerData = new LiveEnrollmentUserStatistics();
                    partnerData.setUserId(partner.getPartnerId());
                    partnerData.setUserName(partner.getPartnerName());
                    partnerData.setDay(day);
                }
                partnerData.setOrderNum(orderNum);

                String partnerDeliveryId = userDeliveryIdMap.get(partner.getPartnerId());
                List<LiveEnrollmentOrder> partnerMiddleOrderList = middleDeliveryOrderMap.get(partnerDeliveryId);
                partnerData.setMiddleOrderNum(CollectionUtils.isEmpty(partnerMiddleOrderList) ? 0 : SafeConverter.toDouble(partnerMiddleOrderList.size()));

                liveEnrollmentUserStatisticsDao.upsert(partnerData);
            }
        }
    }

    // 更新学校当日订单数
    private void updateSchoolStatistics(LiveEnrollmentOrder order) {
        if (order == null || order.getSchoolId() == null || !isCalculateData(order)) {
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(order.getPayTime(), "yyyyMMdd"));
        Map<Long, LiveEnrollmentSchoolStatistics> schoolDataMap = liveEnrollmentSchoolStatisticsDao.loadBySchoolIds(Collections.singleton(order.getSchoolId()), day);
        LiveEnrollmentSchoolStatistics schoolData = MapUtils.isEmpty(schoolDataMap) ? null : schoolDataMap.get(order.getSchoolId());
        if (schoolData == null) {
            schoolData = new LiveEnrollmentSchoolStatistics();
            schoolData.setSchoolId(order.getSchoolId());
            schoolData.setSchoolName(order.getSchoolName());
            schoolData.setDay(day);
        }
        if (StringUtils.isNotBlank(order.getCourseStage()) && (order.getCourseStage().equals("初中") || order.getCourseStage().equals("高中"))) {
            schoolData.setMiddleOrderNum(SafeConverter.toDouble(schoolData.getMiddleOrderNum()) + 1);
        } else {
            schoolData.setOrderNum(SafeConverter.toDouble(schoolData.getOrderNum()) + 1);
        }

        liveEnrollmentSchoolStatisticsDao.upsert(schoolData);
    }

    // 更新地区当日订单数
    private void updateRegionStatistics(LiveEnrollmentOrder order) {
        if (order == null || order.getSchoolId() == null || !isCalculateData(order)) {
            return;
        }

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(order.getSchoolId()).getUninterruptibly();
        if (school == null || school.getRegionCode() == null) {
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(order.getPayTime(), "yyyyMMdd"));
        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());

        LiveEnrollmentRegionStatistics regionData = liveEnrollmentRegionStatisticsDao.loadByCountyCode(school.getRegionCode(), day);

        if (regionData == null) {
            regionData = new LiveEnrollmentRegionStatistics();

            regionData.setCountyCode(school.getRegionCode());
            if (exRegion != null) {
                regionData.setCountyName(exRegion.getCountyName());
                regionData.setCityCode(exRegion.getCityCode());
                regionData.setCityName(exRegion.getCityName());
                regionData.setProvinceCode(exRegion.getProvinceCode());
                regionData.setProvinceName(exRegion.getProvinceName());
            }
            regionData.setDay(day);
        }
        if (StringUtils.isNotBlank(order.getCourseStage()) && (order.getCourseStage().equals("初中") || order.getCourseStage().equals("高中"))) {
            regionData.setMiddleOrderNum(SafeConverter.toDouble(regionData.getMiddleOrderNum()) + 1);
        } else {
            regionData.setOrderNum(SafeConverter.toDouble(regionData.getOrderNum()) + 1);
        }
        liveEnrollmentRegionStatisticsDao.upsert(regionData);
    }


    // 1：小学，  2：初高中
    public MapMessage getUserOrderChart(Long userId, Integer stage, Date startDate, Date endDate) {

        Set<Integer> days = getEveryDays(startDate, endDate);
        if (CollectionUtils.isEmpty(days)) {
            return MapMessage.successMessage().add("dataList", new ArrayList<>()).add("total", 0d);
        }
        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if (CollectionUtils.isNotEmpty(managedGroupIds)) {
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            List<Long> tempUserIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            userIds.addAll(tempUserIds);
        }

        double totalOrderNum = 0;
        List<Map<String, Object>> dataList = new ArrayList<>();
        AgentRoleType roleType = baseOrgService.getUserRole(userId);

        List<LiveEnrollmentUserStatistics> userDataList = liveEnrollmentUserStatisticsDao.loadByUsersAndDays(userIds, days);
        Map<Integer, List<LiveEnrollmentUserStatistics>> dayDataMap = userDataList.stream().collect(Collectors.groupingBy(LiveEnrollmentUserStatistics::getDay));
        for (Integer day : days) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("day", day);
            List<LiveEnrollmentUserStatistics> dayDataList = dayDataMap.get(day);
            double dayOrderNum = 0;
            if (CollectionUtils.isNotEmpty(dayDataList)) {
                for (LiveEnrollmentUserStatistics dayData : dayDataList) {
                    if (stage == 1) {
                        dayOrderNum = MathUtils.doubleAdd(dayOrderNum, SafeConverter.toDouble(dayData.getOrderNum()));
                    } else {
                        dayOrderNum = MathUtils.doubleAdd(dayOrderNum, SafeConverter.toDouble(dayData.getMiddleOrderNum()));
                    }

                }
            }
            if (roleType == AgentRoleType.Country) {
                dayOrderNum = MathUtils.doubleDivide(dayOrderNum, 1, 0, BigDecimal.ROUND_CEILING);
            }
            itemMap.put("orderNum", dayOrderNum);
            dataList.add(itemMap);

            totalOrderNum = MathUtils.doubleAdd(totalOrderNum, dayOrderNum);
        }

        if (roleType == AgentRoleType.Country) {
            totalOrderNum = MathUtils.doubleDivide(totalOrderNum, 1, 0, BigDecimal.ROUND_CEILING);
        }
        return MapMessage.successMessage().add("dataList", dataList).add("total", totalOrderNum);
    }

    public MapMessage getUserOrderChartByOrder(Long userId, Date startDate, Date endDate, Integer stage) {

        Set<Integer> days = getEveryDays(startDate, endDate);
        if (CollectionUtils.isEmpty(days)) {
            return MapMessage.successMessage().add("dataList", new ArrayList<>()).add("total", 0d);
        }
        AgentRoleType roleType = baseOrgService.getUserRole(userId);
        if (roleType != AgentRoleType.Country) {
            return MapMessage.successMessage().add("dataList", new ArrayList<>()).add("total", 0d);
        }

        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if (CollectionUtils.isNotEmpty(managedGroupIds)) {
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            List<Long> tempUserIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            userIds.addAll(tempUserIds);
        }

        double totalOrderNum = 0;
        List<Map<String, Object>> dataList = new ArrayList<>();

        Map<Long, UserQrCode> userQrCodeMap = userQrCodeDao.loadByUserIds(QRCodeBusinessType.LIVE_ENROLLMENT, userIds);
        Set<String> deliveryIds = new HashSet<>();
        userQrCodeMap.values().forEach(p -> {
            if (StringUtils.isNotBlank(p.getRelatedId())) {
                deliveryIds.add(p.getRelatedId());
            }
        });

//        Map<Integer, List<LiveEnrollmentOrder>> dayOrderMap = orderMap.values().stream().flatMap(List::stream)
//                .collect(Collectors.groupingBy(p -> SafeConverter.toInt(DateUtils.dateToString(p.getPayTime(), "yyyyMMdd"))));

        List<Future<Map<String, Object>>> futureList = new ArrayList<>();
        for (Integer day : days) {
//            List<LiveEnrollmentOrder> dayDataList = dayOrderMap.get(day);
            futureList.add(AlpsThreadPool.getInstance().submit(() -> assemblingDayOrderNum(day, stage, deliveryIds)));
        }
        for (Future<Map<String, Object>> future : futureList) {
            try {
                Map<String, Object> item = future.get();
                if (MapUtils.isNotEmpty(item)) {
                    double dayOrderNum = SafeConverter.toDouble(item.get("orderNum"));
                    totalOrderNum = MathUtils.doubleAdd(totalOrderNum, dayOrderNum);
                    dataList.add(item);
                }
            } catch (Exception e) {
                logger.error("查询主任务异常", e);
            }
        }
        return MapMessage.successMessage().add("dataList", dataList).add("total", totalOrderNum);
    }

    private Map<String, Object> assemblingDayOrderNum(Integer day, Integer stage, Set<String> deliveryIds) {
        Date date = DateUtils.stringToDate(day.toString(), "yyyyMMdd");
        double dayOrderNum = 0;
        Double cacheOrderNum = getCountryDayOrderCount(day, stage);
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("day", day);
        if (cacheOrderNum == null) {
            DayRange dayRange = DayRange.newInstance(date.getTime());
            Map<String, List<LiveEnrollmentOrder>> orderMap = liveEnrollmentOrderDao.loadByDeliveryIds(deliveryIds, 1, dayRange.getStartDate(), dayRange.getEndDate());
            List<LiveEnrollmentOrder> dayDataList = orderMap.values().stream().flatMap(List::stream).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(dayDataList)) {
                if (stage == 1) {
                    dayDataList = dayDataList.stream().filter(p -> StringUtils.isBlank(p.getCourseStage()) || Objects.equals(p.getCourseStage(), "小学")).collect(Collectors.toList());
                } else {
                    dayDataList = dayDataList.stream().filter(p -> StringUtils.isNotBlank(p.getCourseStage()) && (Objects.equals(p.getCourseStage(), "初中") || Objects.equals(p.getCourseStage(), "高中"))).collect(Collectors.toList());
                }
            }
            dayOrderNum = MathUtils.doubleAdd(dayOrderNum, dayDataList.size());
            //如果不是当天则放入缓存
            if (!DateUtils.isSameDay(date, new Date())) {
                updateCountryDayOrderCount(day, stage, dayOrderNum);
            }
        } else {
            dayOrderNum = cacheOrderNum;
        }
        itemMap.put("orderNum", dayOrderNum);
        return itemMap;
    }

    private String getCountryCountKey(Integer day, Integer stage) {
        return "LIVE_ORDER_COUNT_DAY_" + day + "_STAGE_" + stage;
    }

    public Double getCountryDayOrderCount(Integer day, Integer stage) {
        return agentCacheSystem.CBS.unflushable.load(getCountryCountKey(day, stage));
    }

    public void updateCountryDayOrderCount(Integer day, Integer stage, Double orderNum) {
        agentCacheSystem.CBS.unflushable.set(getCountryCountKey(day, stage), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), orderNum);
    }

    private Date getStartDateWithDefault(Date date) {
        if (date == null) {
            date = DateUtils.stringToDate("20181224", "yyyyMMdd");
        }
        return date;
    }

    private Date getEndDateWithDefault(Date date) {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    public Set<Integer> getEveryDays(Date startDate, Date endDate) {
        startDate = getStartDateWithDefault(startDate);
        endDate = getEndDateWithDefault(endDate);

        Set<Integer> days = new LinkedHashSet<>();
        Date tempDate = startDate;
        while (tempDate.before(endDate)) {
            days.add(SafeConverter.toInt(DateUtils.dateToString(tempDate, "yyyyMMdd")));
            tempDate = DateUtils.addDays(tempDate, 1);
        }
        Integer endDay = SafeConverter.toInt(DateUtils.dateToString(endDate, "yyyyMMdd"));
        days.add(endDay);
        return days;
    }

    // dimension 1: 默认， 2：专员， 3分区， 4区域， 5大区
    public List<Map<String, Object>> getOrderNumDataList(Long id, Integer idType, Integer stage, Integer dimension) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (idType.equals(AgentConstants.INDICATOR_TYPE_USER)) {
            return dataList;
        }

        Set<Integer> days = getEveryDays(null, null);
        AgentGroup group = baseOrgService.getGroupById(id);
        if (dimension == 2 || (dimension == 1 && group.fetchGroupRoleType() == AgentGroupRoleType.City)) {
            // 专员的情况下
            List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(), AgentRoleType.BusinessDeveloper.getId());
            dataList.addAll(generateUserOrderNumDataList(userIds, days, stage));
        } else {
            List<AgentGroup> groups;
            if (dimension == 1) {  // 默认情况下
                if (group.fetchGroupRoleType() != AgentGroupRoleType.City
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Area
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Region
                        && group.fetchGroupRoleType() != AgentGroupRoleType.Marketing
                ) {
                    groups = baseOrgService.getSubGroupList(group.getId()).stream()
                            .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).collect(Collectors.toList());
                } else {
                    groups = baseOrgService.getGroupListByParentId(group.getId()).stream()
                            .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City
                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Area
                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Region
                                    || p.fetchGroupRoleType() == AgentGroupRoleType.Marketing)
                            .collect(Collectors.toList());
                }
            } else {
                AgentGroupRoleType targetGroupRoleType;
                if (dimension == 3) {
                    targetGroupRoleType = AgentGroupRoleType.City;
                } else if (dimension == 4) {
                    targetGroupRoleType = AgentGroupRoleType.Area;
                } else if (dimension == 5) {
                    targetGroupRoleType = AgentGroupRoleType.Region;
                } else {
                    targetGroupRoleType = null;
                }
                groups = baseOrgService.getSubGroupList(group.getId()).stream()
                        .filter(p -> Objects.equals(p.fetchGroupRoleType(), targetGroupRoleType))
                        .collect(Collectors.toList());
            }
            dataList.addAll(generateGroupOrderNumDataList(groups, days, stage));
        }
        return dataList;
    }

    private List<Map<String, Object>> generateUserOrderNumDataList(List<Long> userIds, Collection<Integer> days, Integer stage) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(userIds) || CollectionUtils.isEmpty(days)) {
            return dataList;
        }

        List<LiveEnrollmentUserStatistics> allUserDataList = liveEnrollmentUserStatisticsDao.loadByUsersAndDays(userIds, days);
        Map<Long, List<LiveEnrollmentUserStatistics>> userDataMap = allUserDataList.stream().collect(Collectors.groupingBy(LiveEnrollmentUserStatistics::getUserId));

        Set<Long> hasNoDataUserIds = userIds.stream().filter(p -> !userDataMap.containsKey(p)).collect(Collectors.toSet());
        Map<Long, AgentUser> userMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(hasNoDataUserIds)) {
            userMap.putAll(agentUserLoaderClient.findByIds(hasNoDataUserIds));
        }

        Integer targetDay = Collections.max(days);
        userIds.forEach(p -> {

            double dayOrderNum = 0d;
            double totalOrderNum = 0d;
            String name = "";

            List<LiveEnrollmentUserStatistics> userDataList = userDataMap.get(p);
            if (CollectionUtils.isNotEmpty(userDataList)) {
                for (LiveEnrollmentUserStatistics data : userDataList) {
                    if (stage == 1) {
                        totalOrderNum = MathUtils.doubleAdd(totalOrderNum, SafeConverter.toDouble(data.getOrderNum()));
                        if (data.getDay().equals(targetDay)) {
                            dayOrderNum = MathUtils.doubleAdd(dayOrderNum, SafeConverter.toDouble(data.getOrderNum()));
                        }
                    } else {
                        totalOrderNum = MathUtils.doubleAdd(totalOrderNum, SafeConverter.toDouble(data.getMiddleOrderNum()));
                        if (data.getDay().equals(targetDay)) {
                            dayOrderNum = MathUtils.doubleAdd(dayOrderNum, SafeConverter.toDouble(data.getMiddleOrderNum()));
                        }
                    }

                    if (StringUtils.isBlank(name)) {
                        name = data.getUserName();
                    }
                }
            }

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", p);
            itemMap.put("idType", AgentConstants.INDICATOR_TYPE_USER);
            if (StringUtils.isBlank(name)) {
                AgentUser user = userMap.get(p);
                if (user != null) {
                    name = user.getRealName();
                }
            }
            itemMap.put("name", name);

            // 总订单数
            itemMap.put("totalOrderNum", totalOrderNum);
            // 指定日期数据
            itemMap.put("dayOrderNum", dayOrderNum);
            dataList.add(itemMap);
        });

        return dataList;
    }

    private List<Map<String, Object>> generateGroupOrderNumDataList(List<AgentGroup> groupList, Collection<Integer> days, Integer stage) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(groupList) || CollectionUtils.isEmpty(days)) {
            return dataList;
        }

        Integer targetDay = Collections.max(days);
        List<Future<Map<String, Object>>> futureList = new ArrayList<>();
        groupList.forEach(p -> {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupOrderNumData(p, days, targetDay, stage)));
        });
        for (Future<Map<String, Object>> future : futureList) {
            try {
                Map<String, Object> item = future.get();
                if (MapUtils.isNotEmpty(item)) {
                    dataList.add(item);
                }
            } catch (Exception e) {
                logger.error("统计部门订单数据异常", e);
            }
        }
        return dataList;

    }

    public Map<String, Object> getGroupOrderNumData(AgentGroup group, Collection<Integer> days, Integer targetDay, Integer stage) {
        List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
        List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
        List<LiveEnrollmentUserStatistics> userDataList = liveEnrollmentUserStatisticsDao.loadByUsersAndDays(userIds, days);
        List<LiveEnrollmentUserStatistics> targetDayHasData;
        if (stage == 1) {
            targetDayHasData = userDataList.stream()
                    .filter(u -> u.getDay().equals(targetDay) && SafeConverter.toDouble(u.getOrderNum()) > 0)
                    .collect(Collectors.toList());
        } else {
            targetDayHasData = userDataList.stream()
                    .filter(u -> u.getDay().equals(targetDay) && SafeConverter.toDouble(u.getMiddleOrderNum()) > 0)
                    .collect(Collectors.toList());
        }
        double dayOrderNum = 0d;
        double totalOrderNum = 0d;
        Set<Long> totalHasDataUser = new HashSet<>();
        for (LiveEnrollmentUserStatistics userData : userDataList) {
            if (stage == 1) {
                if (SafeConverter.toDouble(userData.getOrderNum()) > 0) {
                    totalHasDataUser.add(userData.getUserId());
                }
                totalOrderNum = MathUtils.doubleAdd(totalOrderNum, SafeConverter.toDouble(userData.getOrderNum()));
                if (userData.getDay().equals(targetDay)) {
                    dayOrderNum = MathUtils.doubleAdd(dayOrderNum, SafeConverter.toDouble(userData.getOrderNum()));
                }
            } else {
                if (SafeConverter.toDouble(userData.getMiddleOrderNum()) > 0) {
                    totalHasDataUser.add(userData.getUserId());
                }
                totalOrderNum = MathUtils.doubleAdd(totalOrderNum, SafeConverter.toDouble(userData.getMiddleOrderNum()));
                if (userData.getDay().equals(targetDay)) {
                    dayOrderNum = MathUtils.doubleAdd(dayOrderNum, SafeConverter.toDouble(userData.getMiddleOrderNum()));
                }
            }
        }

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("id", group.getId());
        itemMap.put("idType", AgentConstants.INDICATOR_TYPE_GROUP);
        itemMap.put("name", group.getGroupName());
        // 总订单数
        itemMap.put("totalOrderNum", totalOrderNum);
        //总人均
        itemMap.put("totalAverage", totalHasDataUser.size() > 0 ? MathUtils.doubleDivide(totalOrderNum, totalHasDataUser.size(), 2, 1) : 0);
        itemMap.put("dayOrderNum", dayOrderNum);
        //当日人均
        itemMap.put("dayAverage", targetDayHasData.size() > 0 ? MathUtils.doubleDivide(dayOrderNum, targetDayHasData.size(), 2, 1) : 0);
        return itemMap;
    }

    // dateType: 1 当日 2 累计
    // rankingType: 1 专员 2 分区 3 学校 4 城市
    public MapMessage getRankingList(Integer dateType, Integer rankingType, int topN, Long userId, Integer stage) {

        MapMessage mapMessage = MapMessage.successMessage();

        List<Map<String, Object>> resultDataList = new ArrayList<>();

        Integer day = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMMdd"));
        Set<Integer> everyDays = getEveryDays(null, null);
        if (rankingType == 1 || rankingType == 2) {
            List<Map<String, Object>> userOrGroupDataList = new ArrayList<>();
            if (rankingType == 1) { // 专员榜
                List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
                List<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());

                if (dateType == 1) { // 日榜
                    userOrGroupDataList.addAll(generateUserOrderNumDataList(userIds, Collections.singleton(day), stage));
                } else if (dateType == 2) { // 累计
                    userOrGroupDataList.addAll(generateUserOrderNumDataList(userIds, everyDays, stage));
                }
            } else {  // 分区榜
                List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.City);
                if (dateType == 1) { // 日榜
                    userOrGroupDataList.addAll(generateGroupOrderNumDataList(groupList, Collections.singleton(day), stage));
                } else if (dateType == 2) { // 累计
                    userOrGroupDataList.addAll(generateGroupOrderNumDataList(groupList, everyDays, stage));
                }
            }

            userOrGroupDataList = userOrGroupDataList.stream()
//                    .filter(p -> SafeConverter.toDouble(p.get("totalOrderNum")) > 0d)
                    .sorted((o1, o2) -> Double.compare(SafeConverter.toDouble(o2.get("totalOrderNum")), SafeConverter.toDouble(o1.get("totalOrderNum"))))
                    .collect(Collectors.toList());
            AgentRoleType roleType = baseOrgService.getUserRole(userId);
            Map<String, Object> selfMap = new HashMap<>();
            if (roleType == AgentRoleType.BusinessDeveloper && rankingType == 1) {

                for (int i = 0; i < userOrGroupDataList.size(); i++) {
                    if (userId == SafeConverter.toLong(userOrGroupDataList.get(i).get("id"))) {
                        List<AgentGroup> groups = baseOrgService.getUserGroups(SafeConverter.toLong(userOrGroupDataList.get(i).get("id")));
                        userOrGroupDataList.get(i).put("groupName", groups.get(0).getGroupName());
                        selfMap.put("id", userOrGroupDataList.get(i).get("id"));
                        selfMap.put("name", userOrGroupDataList.get(i).get("name"));
                        selfMap.put("ranking", i + 1);
                        selfMap.put("orderNum", userOrGroupDataList.get(i).get("totalOrderNum"));
//                            selfMap =userOrGroupDataList.get(i);
                        break;
                    }
                }

            }
            userOrGroupDataList = userOrGroupDataList.stream()
                    .filter(p -> SafeConverter.toDouble(p.get("totalOrderNum")) > 0d).collect(Collectors.toList());
            int ranking = 0;
            double preOrderNum = 0d;
            for (int i = 0; i < userOrGroupDataList.size(); i++) {
                Map<String, Object> dataMap = userOrGroupDataList.get(i);
                double orderNum = SafeConverter.toDouble(dataMap.get("totalOrderNum"));
                if (ranking < topN) {
                    if (preOrderNum != orderNum) {
                        preOrderNum = orderNum;
                        ranking++;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", dataMap.get("id"));
                    map.put("name", dataMap.get("name"));
                    map.put("ranking", ranking);
                    map.put("orderNum", orderNum);
                    resultDataList.add(map);
                } else {
                    if (preOrderNum != orderNum) {
                        break;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", dataMap.get("id"));
                    map.put("name", dataMap.get("name"));
                    map.put("ranking", ranking);
                    map.put("orderNum", orderNum);
                    resultDataList.add(map);
                }
            }

            if (rankingType == 1) {
                for (int i = 0; i < resultDataList.size(); i++) {
                    List<AgentGroup> groups = baseOrgService.getUserGroups(SafeConverter.toLong(resultDataList.get(i).get("id")));
                    resultDataList.get(i).put("groupName", groups.get(0).getGroupName());
                    if (userId == SafeConverter.toLong(resultDataList.get(i).get("id"))) {
                        selfMap.put("ranking", resultDataList.get(i).get("ranking"));
//                        break;
                    }
                }

            }
            mapMessage.put("selfMap", selfMap);
        } else if (rankingType == 3) {  // 学校榜
            List<LiveEnrollmentSchoolStatistics> schoolDataList = new ArrayList<>();
            if (dateType == 1) {
                schoolDataList.addAll(liveEnrollmentSchoolStatisticsDao.loadByDays(Collections.singleton(day)));
            } else if (dateType == 2) { // 累计
                List<LiveEnrollmentSchoolStatistics> schoolDaysDataList = liveEnrollmentSchoolStatisticsDao.loadByDays(everyDays);
                Map<Long, List<LiveEnrollmentSchoolStatistics>> schoolDataMap = schoolDaysDataList.stream().collect(Collectors.groupingBy(LiveEnrollmentSchoolStatistics::getSchoolId));
                schoolDataMap.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v)) {
                        LiveEnrollmentSchoolStatistics first = v.get(0);
                        for (int i = 1; i < v.size(); i++) {
                            double orderNum = SafeConverter.toDouble(v.get(i).getOrderNum());
                            double middleOrderNum = SafeConverter.toDouble(v.get(i).getMiddleOrderNum());
                            first.setOrderNum(MathUtils.doubleAdd(SafeConverter.toDouble(first.getOrderNum()), orderNum));
                            first.setMiddleOrderNum(MathUtils.doubleAdd(SafeConverter.toDouble(first.getMiddleOrderNum()), middleOrderNum));
                        }
                        schoolDataList.add(first);
                    }
                });
            }
            List<LiveEnrollmentSchoolStatistics> targetSchoolList = schoolDataList.stream()
                    .filter(p -> {
                        if (stage == 1) {
                            return SafeConverter.toDouble(p.getOrderNum()) > 0;
                        } else {
                            return SafeConverter.toDouble(p.getMiddleOrderNum()) > 0;
                        }
                    })
                    .sorted((o1, o2) -> {
                        if (stage == 1) {
                            return Double.compare(SafeConverter.toDouble(o2.getOrderNum()), SafeConverter.toDouble(o1.getOrderNum()));
                        } else {
                            return Double.compare(SafeConverter.toDouble(o2.getMiddleOrderNum()), SafeConverter.toDouble(o1.getMiddleOrderNum()));
                        }
                    })
                    .collect(Collectors.toList());
            int ranking = 0;
            double preOrderNum = 0d;

            for (int i = 0; i < targetSchoolList.size(); i++) {
                LiveEnrollmentSchoolStatistics schoolData = targetSchoolList.get(i);
                if (ranking < topN) {
                    if (stage == 1) { // 小学
                        if (preOrderNum != SafeConverter.toDouble(schoolData.getOrderNum())) {
                            preOrderNum = SafeConverter.toDouble(schoolData.getOrderNum());
                            ranking++;
                        }
                    } else {
                        if (preOrderNum != SafeConverter.toDouble(schoolData.getMiddleOrderNum())) {
                            preOrderNum = SafeConverter.toDouble(schoolData.getMiddleOrderNum());
                            ranking++;
                        }
                    }


                    Map<String, Object> map = new HashMap<>();
                    map.put("id", schoolData.getSchoolId());
                    map.put("name", schoolData.getSchoolName());
                    map.put("ranking", ranking);
                    map.put("orderNum", stage == 1 ? schoolData.getOrderNum() : schoolData.getMiddleOrderNum());
                    resultDataList.add(map);
                } else {
                    if (preOrderNum != (stage == 1 ? schoolData.getOrderNum() : schoolData.getMiddleOrderNum())) {
                        break;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", schoolData.getSchoolId());
                    map.put("name", schoolData.getSchoolName());
                    map.put("ranking", ranking);
                    map.put("orderNum", stage == 1 ? schoolData.getOrderNum() : schoolData.getMiddleOrderNum());
                    resultDataList.add(map);
                }
            }

        } else if (rankingType == 4) {   // 4 城市
            List<LiveEnrollmentRegionStatistics> countyDataList = new ArrayList<>();
            if (dateType == 1) {
                countyDataList.addAll(liveEnrollmentRegionStatisticsDao.loadByDays(Collections.singleton(day)));
            } else if (dateType == 2) {
                countyDataList.addAll(liveEnrollmentRegionStatisticsDao.loadByDays(everyDays));
            }

            List<LiveEnrollmentRegionStatistics> cityDataList = new ArrayList<>();

            Map<Integer, List<LiveEnrollmentRegionStatistics>> cityDataMap = countyDataList.stream().filter(p -> (p.getCityCode() != null)).collect(Collectors.groupingBy(LiveEnrollmentRegionStatistics::getCityCode));
            cityDataMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    LiveEnrollmentRegionStatistics first = v.get(0);
                    for (int i = 1; i < v.size(); i++) {
                        double orderNum = SafeConverter.toDouble(v.get(i).getOrderNum());
                        double middleOrderNum = SafeConverter.toDouble(v.get(i).getMiddleOrderNum());
                        first.setOrderNum(MathUtils.doubleAdd(SafeConverter.toDouble(first.getOrderNum()), orderNum));
                        first.setMiddleOrderNum(MathUtils.doubleAdd(SafeConverter.toDouble(first.getMiddleOrderNum()), middleOrderNum));
                    }
                    cityDataList.add(first);
                }
            });

            List<LiveEnrollmentRegionStatistics> targetCityList = cityDataList.stream()
                    .filter(p -> {
                        if (stage == 1) {
                            return SafeConverter.toDouble(p.getOrderNum()) > 0;
                        } else {
                            return SafeConverter.toDouble(p.getMiddleOrderNum()) > 0;
                        }
                    })
                    .sorted((o1, o2) -> {
                        if (stage == 1) {
                            return Double.compare(SafeConverter.toDouble(o2.getOrderNum()), SafeConverter.toDouble(o1.getOrderNum()));
                        } else {
                            return Double.compare(SafeConverter.toDouble(o2.getMiddleOrderNum()), SafeConverter.toDouble(o1.getMiddleOrderNum()));
                        }
                    })
                    .collect(Collectors.toList());

            int ranking = 0;
            double preOrderNum = 0d;

            for (int i = 0; i < targetCityList.size(); i++) {
                LiveEnrollmentRegionStatistics cityData = targetCityList.get(i);
                if (ranking < topN) {
                    if (stage == 1) {
                        if (preOrderNum != cityData.getOrderNum()) {
                            preOrderNum = cityData.getOrderNum();
                            ranking++;
                        }
                    } else {
                        if (preOrderNum != cityData.getMiddleOrderNum()) {
                            preOrderNum = cityData.getMiddleOrderNum();
                            ranking++;
                        }
                    }


                    Map<String, Object> map = new HashMap<>();
                    map.put("id", cityData.getCityCode());
                    map.put("name", cityData.getCityName());
                    map.put("ranking", ranking);
                    map.put("orderNum", stage == 1 ? cityData.getOrderNum() : cityData.getMiddleOrderNum());
                    resultDataList.add(map);
                } else {
                    if (preOrderNum != (stage == 1 ? cityData.getOrderNum() : cityData.getMiddleOrderNum())) {
                        break;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", cityData.getCityCode());
                    map.put("name", cityData.getCityName());
                    map.put("ranking", ranking);
                    map.put("orderNum", stage == 1 ? cityData.getOrderNum() : cityData.getMiddleOrderNum());
                    resultDataList.add(map);
                }
            }
        }

        mapMessage.put("dataList", resultDataList);
//        mapMessage.put("selfMap",new HashMap<>());
        return mapMessage;
    }


    public MapMessage receiveGifts(String phone) {
        return LiveEnrollmentRemoteClient.receiveGifts(phone, getCurrentUserId());
    }


    public void initSchoolAndRegionData(Collection<Integer> days) {
        for (Integer day : days) {
            Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            DayRange dayRange = DayRange.newInstance(date.getTime());
            List<LiveEnrollmentOrder> list = liveEnrollmentOrderDao.loadByDate(dayRange.getStartDate(), dayRange.getEndDate());
            if (CollectionUtils.isNotEmpty(list)) {
                List<LiveEnrollmentSchoolStatistics> schoolDataList = liveEnrollmentSchoolStatisticsDao.loadByDays(Collections.singleton(day));
                List<String> ids = schoolDataList.stream().map(LiveEnrollmentSchoolStatistics::getId).collect(Collectors.toList());
                liveEnrollmentSchoolStatisticsDao.removes(ids);

                List<LiveEnrollmentRegionStatistics> regionDataList = liveEnrollmentRegionStatisticsDao.loadByDays(Collections.singleton(day));
                List<String> regionIds = regionDataList.stream().map(LiveEnrollmentRegionStatistics::getId).collect(Collectors.toList());
                liveEnrollmentRegionStatisticsDao.removes(regionIds);

                list.forEach(p -> {
                    updateSchoolStatistics(p);
                    updateRegionStatistics(p);
                });
            }
        }
    }
}
