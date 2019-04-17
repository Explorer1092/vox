package com.voxlearning.utopia.schedule.schedule;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.NamedDaemonThreadFactory;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.util.EternalLifeIterator;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.consumer.RewardManagementClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardCompleteOrder;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ActivityType;
import com.voxlearning.utopia.service.user.api.constants.RefStatus;
import com.voxlearning.utopia.service.user.api.entities.BlackWhiteList;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Named
@ScheduledJobDefinition(
        jobName = "奖品中心每月自动生成学生快递单",
        jobDescription = "每个月1日05:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 5 1 * ?"
)
public class AutoGenShardingRewardLogisticsJob extends ScheduledJobWithJournalSupport {

    private final String MODULE = "AutoGenShardingRewardLogisticsJob";
    private final String CURRENT_STAGE = RuntimeMode.getCurrentStage();

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    @Inject
    private UserServiceClient userSrvCli;
    @Inject
    private RewardServiceClient rewardSrvCli;
    @Inject
    private BlackWhiteListManagerClient blackWhiteListManagerClient;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;
    @Inject
    private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;

    private UtopiaSql utopiaSqlReward;
    @Inject
    private RewardManagementClient rewardManagementClient;

    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;

    @Inject
    private RaikouSDK raikouSDK;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    private static final Integer SHARDING_STUDENT_COUNT = 100;
    private static final Integer SHARDING_CLAZZ_COUNT = 20;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        List<RewardCompleteOrder> curMoonthCompleteOrder = findCurMoonthCompleteOrder();
        Map<Long, List<RewardCompleteOrder>> schoolOrderMap = curMoonthCompleteOrder.stream().collect(Collectors.groupingBy(RewardCompleteOrder::getSchoolId));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                10, 10, 10, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(10),
                NamedDaemonThreadFactory.getInstance("AutoGenShardingRewardLogisticsJob-Pool"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        CountDownLatch countDownLatch = new CountDownLatch(schoolOrderMap.entrySet().size());

        for (Map.Entry<Long, List<RewardCompleteOrder>> entry : schoolOrderMap.entrySet()) {
            executor.submit(() -> {
                Long schoolId = entry.getKey();
                List<RewardCompleteOrder> schoolOrder = entry.getValue();

                try {
                    oneSchoolLogic(schoolId, schoolOrder);
                } catch (Exception e) {
                    logger.error("学校ID:" + schoolId + " 分包发货异常", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executor.shutdown();
    }

    private void oneSchoolLogic(Long schoolId, List<RewardCompleteOrder> list) {
        String month = DateUtils.dateToString(new Date(), "yyyyMM");
        long clazzCount = list.stream().map(RewardCompleteOrder::getClazzId).distinct().count();     // 兑换班级数
        long studentCount = list.stream().map(RewardCompleteOrder::getBuyerId).distinct().count();   // 兑换学生数

        int packageCount = 1; // 分包数量
        if (studentCount > SHARDING_STUDENT_COUNT && clazzCount > SHARDING_CLAZZ_COUNT) {
            packageCount = SafeConverter.toInt(Math.ceil(clazzCount / 20d), 1);
        }

        LogCollector.info("backend-general", MapUtils.map(
                "env", CURRENT_STAGE,
                "module", MODULE,
                "step", 1,
                "schoolId", schoolId,
                "month", month,
                "orderSize", list.size(),
                "clazzSize", clazzCount,
                "studentSize", studentCount,
                "packageSize", packageCount
        ));

        Map<RewardTeacher, Set<Long>> teacherClazzSizeMap = new LinkedHashMap<>(); // key 老师 value 需要收货的 clazz ID

        Iterator<RewardCompleteOrder> iterator = list.iterator();
        while (iterator.hasNext()) {
            RewardCompleteOrder completeOrder = iterator.next();
            List<RewardTeacher> rewardTeachers = loadClazzTeacher(completeOrder.getBuyerId());

            if (CollectionUtils.isEmpty(rewardTeachers)) {
                deleteOrder(Collections.singletonList(completeOrder)); // 如果没老师,直接退单
                iterator.remove();
                continue;
            }

            // 认证、有详细地址、有手机号的老师
            Set<RewardTeacher> todo = rewardTeachers.stream().filter(RewardTeacher::getAuthenticationSuccess)
                    .filter(RewardTeacher::getDetailAddress)
                    .filter(RewardTeacher::getBindMobile).collect(toSet());

            LogCollector.info("backend-general", MapUtils.map(
                    "env", CURRENT_STAGE,
                    "module", MODULE,
                    "step", 2,
                    "schoolId", schoolId,
                    "orderId", completeOrder.getOrderId(),
                    "teacher", JSON.toJSONString(rewardTeachers),
                    "rewardTeacher", JSON.toJSONString(todo)
            ));

            // 统计老师班级数
            for (RewardTeacher rewardTeacher : todo) {
                teacherClazzSizeMap.compute(rewardTeacher, (key, value) -> {
                    if (value == null) value = new HashSet<>();
                    value.add(completeOrder.getClazzId());
                    return value;
                });
            }

            RewardTeacher rewardTeacher = rewardTeachers.get(0); // 这个老师是对当前学生最优的结果
            TeacherDetail teacherDetail = rewardTeacher.getTeacherDetail();
            UserAuthentication authentication = rewardTeacher.getUserAuthentication();

            // completeOrder 的学校、班级属性已经在 autoGenCompleteOrderJob 中设置完成(没有的会被立即退单)
            completeOrder.setSensitivePhone(authentication != null && authentication.isMobileAuthenticated() ? authentication.getSensitiveMobile() : "");
            completeOrder.setReceiverId(teacherDetail.getId());
            completeOrder.setReceiverName(teacherDetail.fetchRealname());

            UserShippingAddress teacherAddress = rewardTeacher.getUserShippingAddress();
            if (teacherAddress != null) { // 班级内老师可以没有收获地址
                setCompleteOrderAddress(completeOrder, teacherAddress);
            }
            rewardManagementClient.upsertRewardCompleteOrder(completeOrder);
        }

        // 如果学校指定了收货老师那就用指定的老师
        TeacherDetail schoolReceiveTeacher = getSchoolReceiveTeacher(schoolId);
        if (schoolReceiveTeacher != null) {
            logger.info("学校收货人白名单，schoolId:{} teacherId:{}", schoolId, schoolReceiveTeacher.getId());

            // 判断在不在黑名单
            List<BlackWhiteList> bwList = blackWhiteListManagerClient.loadUserBlackWhiteLists(schoolReceiveTeacher.getId(), ActivityType.拒收学生奖品名单);
            if (CollectionUtils.isEmpty(bwList)) {
                MapMessage mapMessage = userSrvCli.generateUserShippingAddress(schoolReceiveTeacher.getId());
                if (mapMessage.isSuccess()) {
                    UserShippingAddress tempAddress = (UserShippingAddress) mapMessage.get("address");
                    if (tempAddress != null) {
                        setLogistics(list, month, tempAddress);
                        return;
                    }
                }
            }
        }

        if (teacherClazzSizeMap.isEmpty()) {
            logger.info("奖品中心整校退单，schoolId:{}", schoolId);
            deleteOrder(list);
        } else {
            Map<RewardTeacher, Set<Long>> linkMap = sortByValue(teacherClazzSizeMap, new Comparator<Set<Long>>() {
                @Override
                public int compare(Set<Long> o1, Set<Long> o2) {
                    return Integer.compare(o2.size(), o1.size());
                }
            });

            // 把符合条件的收货老师按所带的 clazz 数量排序
            ArrayList<RewardTeacher> rewardTeachers = new ArrayList<>(linkMap.keySet());

            int teacherSize = rewardTeachers.size();
            int realPackageCount = Math.min(teacherSize, packageCount);

            LogCollector.info("backend-general", MapUtils.map(
                    "env", CURRENT_STAGE,
                    "module", MODULE,
                    "step", 3,
                    "schoolId", schoolId,
                    "teacher", JSON.toJSONString(linkMap),
                    "teacherSize", rewardTeachers.size(),
                    "packageSize", packageCount,
                    "realPackageCount", realPackageCount
            ));

            if (realPackageCount == 1) {
                setLogistics(list, month, rewardTeachers.get(0).getUserShippingAddress());
            } else {
                logger.info("需要分包的学校:{} 兑换量:{} 兑换人数:{} 兑换班级:{}", schoolId, list.size(), studentCount, clazzCount);
                List<RewardTeacher> subTeacher = rewardTeachers.subList(0, packageCount);

                // 补充老师所带的年级 && 生成快递单
                for (RewardTeacher rewardTeacher : subTeacher) {
                    Set<Integer> teacherClazzLevel = getTeacherClazzLevel(rewardTeacher.getTeacherId());
                    rewardTeacher.setClazzLevel(teacherClazzLevel);
                }

                LogCollector.info("backend-general", MapUtils.map(
                        "env", CURRENT_STAGE,
                        "module", MODULE,
                        "step", 4,
                        "schoolId", schoolId,
                        "packageTeacherDetail", JSON.toJSONString(subTeacher)
                ));

                sharding(list, teacherClazzSizeMap, subTeacher);
            }
        }
    }

    private void setCompleteOrderAddress(RewardCompleteOrder completeOrder, UserShippingAddress teacherAddress) {
        if (completeOrder == null || teacherAddress == null) {
            return;
        }
        completeOrder.setProvinceName(teacherAddress.getProvinceName());
        completeOrder.setProvinceCode(teacherAddress.getProvinceCode());
        completeOrder.setCityName(teacherAddress.getCityName());
        completeOrder.setCityCode(teacherAddress.getCityCode());
        completeOrder.setCountyName(teacherAddress.getCountyName());
        completeOrder.setCountyCode(teacherAddress.getCountyCode());
        completeOrder.setDetailAddress(StringHelper.filterEmojiForMysql(teacherAddress.getDetailAddress()));
        completeOrder.setLogisticType(teacherAddress.getLogisticTypeName());
        completeOrder.setPostCode(teacherAddress.getPostCode());
    }

    public RewardLogistics getTeacherLogistics(RewardTeacher teacher) {
        if (teacher.getLogistics() == null) {
            // 一个很神奇的地方,有详细地址,但是没有收货姓名、联系电话(页面上都已经是必填项,可能是早期数据)
            UserShippingAddress userShippingAddress = teacher.getUserShippingAddress();
            teacher.setLogistics(generateLogistics(userShippingAddress));
        }
        return teacher.getLogistics();
    }

    private void sharding(List<RewardCompleteOrder> list, Map<RewardTeacher, Set<Long>> teacherClazzSizeMap, List<RewardTeacher> subTeacher) {
        // 按照老师所带班级分
        for (RewardTeacher rewardTeacher : subTeacher) {
            Set<Long> clazzSet = teacherClazzSizeMap.get(rewardTeacher);
            UserShippingAddress schoolAddress = rewardTeacher.getUserShippingAddress();

            for (RewardCompleteOrder completeOrder : list) {
                if (logisticsIsEmpty(completeOrder.getLogisticsId())) {
                    if (clazzSet.contains(completeOrder.getClazzId())) {
                        setLogistics(schoolAddress, getTeacherLogistics(rewardTeacher), completeOrder);
                    }
                }
            }
        }

        // 按照老师所带年级分
        for (RewardTeacher rewardTeacher : subTeacher) {
            UserShippingAddress schoolAddress = rewardTeacher.getUserShippingAddress();
            Set<Integer> clazzLevel = rewardTeacher.getClazzLevel();

            for (RewardCompleteOrder completeOrder : list) {
                if (logisticsIsEmpty(completeOrder.getLogisticsId())) {
                    if (clazzLevel.contains(completeOrder.getClazzLevel())) {
                        setLogistics(schoolAddress, getTeacherLogistics(rewardTeacher), completeOrder);
                    }
                }
            }
        }

        // 剩余包裹都是哪些年级
        List<Integer> clazzLevel = list.stream()
                .filter(completeOrder -> logisticsIsEmpty(completeOrder.getLogisticsId()))
                .map(RewardCompleteOrder::getClazzLevel)
                .distinct()
                .collect(toList());

        EternalLifeIterator<RewardTeacher> iterator = new EternalLifeIterator<>(Collections.singletonList(subTeacher));

        for (Integer clazzLevelItem : clazzLevel) {
            RewardTeacher curTeacher = iterator.next();
            UserShippingAddress schoolAddress = curTeacher.getUserShippingAddress();

            for (RewardCompleteOrder completeOrder : list) {
                if (logisticsIsEmpty(completeOrder.getLogisticsId()) && Objects.equals(completeOrder.getClazzLevel(), clazzLevelItem)) {
                    setLogistics(schoolAddress, getTeacherLogistics(curTeacher), completeOrder);
                }
            }
        }
    }

    private Set<Integer> getTeacherClazzLevel(Long teacherId) {
        List<Long> teacherIds = teacherLoaderClient.loadSubTeacherIds(teacherId);
        HashSet<Long> teacherIdSet = new HashSet<>(teacherIds);
        teacherIdSet.add(teacherId);

        return teacherLoaderClient.loadTeachersClazzIds(teacherIdSet)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(cId -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(cId))
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.isTerminalClazz())
                .map(clazz -> clazz.getClazzLevel().getLevel())
                .collect(toSet());
    }

    private RewardLogistics generateLogistics(UserShippingAddress schoolAddress) {
        try {
            if (StringUtils.isBlank(schoolAddress.getReceiver())
                    || StringUtils.isBlank(schoolAddress.getReceiverPhone())) {
                MapMessage mapMessage = userSrvCli.generateUserShippingAddress(schoolAddress.getUserId());
                if (mapMessage.isSuccess()) {
                    schoolAddress = (UserShippingAddress) mapMessage.get("address");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        String month = DateUtils.dateToString(new Date(), "yyyyMM");
        RewardLogistics logistics = new RewardLogistics();
        logistics.setCityCode(schoolAddress.getId());
        logistics.setCityName(schoolAddress.getCityName());
        logistics.setProvinceCode(schoolAddress.getProvinceCode());
        logistics.setProvinceName(schoolAddress.getProvinceName());
        logistics.setCountyName(schoolAddress.getCountyName());
        logistics.setCountyCode(schoolAddress.getCountyCode());
        logistics.setType(RewardLogistics.Type.STUDENT);
        logistics.setSchoolName(schoolAddress.getSchoolName());
        logistics.setSchoolId(schoolAddress.getSchoolId());
        logistics.setMonth(month);
        logistics.setIsBack(false);

        logistics.setPostCode(schoolAddress.getPostCode());
        logistics.setReceiverId(schoolAddress.getUserId());
        logistics.setReceiverName(schoolAddress.getReceiver());
        logistics.setSensitivePhone(schoolAddress.getReceiverPhone());
        logistics.setDetailAddress(StringHelper.filterEmojiForMysql(schoolAddress.getDetailAddress()));
        logistics.setLogisticType(schoolAddress.getLogisticTypeName());
        logistics = crmRewardService.$upsertRewardLogistics(logistics);
        return logistics;
    }

    private void setLogistics(List<RewardCompleteOrder> list, String month, UserShippingAddress schoolAddress) {
        RewardLogistics logistics = generateLogistics(schoolAddress);

        for (RewardCompleteOrder completeOrder : list) {
            setLogistics(schoolAddress, logistics, completeOrder);
        }
    }

    private void setLogistics(UserShippingAddress schoolAddress, RewardLogistics logistics, RewardCompleteOrder completeOrder) {
        completeOrder.setLogisticType(schoolAddress.getLogisticTypeName());
        completeOrder.setLogisticsId(SafeConverter.toString(logistics.getId()));

        // 这一步是为了补充班级内没有找到收货老师的订单(虽然会分配校级老师或退单,但是导出时如果没有 cityCode 就不知道归属到哪个城市的excel)
        if (completeOrder.getCityCode() == null || Objects.equals(completeOrder.getCityCode(), 0L)) {
            setCompleteOrderAddress(completeOrder, schoolAddress);
            rewardManagementClient.upsertRewardCompleteOrder(completeOrder);
        }

        // 更新订单快递单ID
        rewardManagementClient.updateRewardOrderLogisticsId(completeOrder.getOrderId(), logistics.getId());
        rewardManagementClient.updateRewardCompleteOrderLogisticsId(completeOrder.getId(), logistics.getId());
    }

    private void deleteOrder(List<RewardCompleteOrder> completeOrders) {
        if (CollectionUtils.isEmpty(completeOrders)) {
            return;
        }
        for (RewardCompleteOrder completeOrder : completeOrders) {
            rewardSrvCli.deleteRewardOrder(completeOrder.getOrderId());     // 取消订单 包含退款加库存
            crmRewardService.deleteCompleteOrder(completeOrder.getId());    // 删除发货单
        }
    }

    /**
     * 查询学校设置的收货老师
     *
     * @param schoolId
     * @return
     */
    private TeacherDetail getSchoolReceiveTeacher(Long schoolId) {
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.getReceiveTeacher() != null) {
            Long receiveTeacherId = schoolExtInfo.getReceiveTeacher();

            // 检查老师是否还在当初的学校
            TeacherDetail receiveTeacher = teacherLoaderClient.loadTeacherDetail(receiveTeacherId);
            if (receiveTeacher != null && Objects.equals(receiveTeacher.getTeacherSchoolId(), schoolId)) {
                return receiveTeacher;
            } else {
                schoolExtInfo.setReceiveTeacher(null);
                schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(schoolExtInfo);
            }
        }
        return null;
    }


    /**
     * 挑选收货老师(这里会尽可能挑最优的, 但不保证满足学校收货老师的条件)
     */
    private List<RewardTeacher> loadClazzTeacher(Long studentId) {
        if (studentId == null) {
            return null;
        }

        Set<Long> teacherIds = getTeacherIdByStudent(studentId);
        if (CollectionUtils.isEmpty(teacherIds)) {
            return null;
        }

        // 考虑包班制,先把老师ID统一转成主老师ID
        Map<Long, Long> mainTeacherMap = teacherLoaderClient.loadMainTeacherIds(teacherIds);
        Set<Long> mainTeacherId = teacherIds.stream().map(teacherId -> {
            Long tempMainId = mainTeacherMap.get(teacherId);
            return tempMainId == null || tempMainId.equals(0L) ? teacherId : tempMainId;
        }).collect(toSet());

        Map<Long, TeacherDetail> teacherDetailMap = teacherLoaderClient.loadTeacherDetails(mainTeacherId);
        Map<Long, UserShippingAddress> addressMap = userLoaderClient.loadUserShippingAddresses(mainTeacherId);

        // 尽量挑选有认证的、尽量挑选有地址的、尽量挑选不在拒收名单的
        List<RewardTeacher> collect = mainTeacherId.stream().map(tempId -> {
            TeacherDetail teacherDetail = teacherDetailMap.get(tempId);

            RewardTeacher rewardTeacher = new RewardTeacher();
            rewardTeacher.setTeacherDetail(teacherDetail);
            rewardTeacher.setTeacherId(teacherDetail.getId());
            rewardTeacher.setAuthenticationSuccess(Objects.equals(teacherDetail.getAuthenticationState(), AuthenticationState.SUCCESS.getState()));

            UserShippingAddress address = addressMap.get(tempId);
            rewardTeacher.setUserShippingAddress(address);
            rewardTeacher.setAddress(address != null);
            rewardTeacher.setDetailAddress(address != null && StringUtils.isNotEmpty(address.getDetailAddress()));

            List<BlackWhiteList> bwList = blackWhiteListManagerClient.loadUserBlackWhiteLists(tempId, ActivityType.拒收学生奖品名单);
            rewardTeacher.setNotBlackList(CollectionUtils.isEmpty(bwList));

            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(tempId);
            rewardTeacher.setUserAuthentication(authentication);
            rewardTeacher.setBindMobile(authentication != null && authentication.isMobileAuthenticated());
            return rewardTeacher;
        })
                .filter(RewardTeacher::getNotBlackList)
                .sorted(
                        Comparator.comparing(RewardTeacher::getAuthenticationSuccess)
                                .thenComparing(RewardTeacher::getAddress)
                                .thenComparing(RewardTeacher::getDetailAddress)
                                .thenComparing(RewardTeacher::getBindMobile)
                                .thenComparing((o1, o2) -> o2.getTeacherId().compareTo(o1.getTeacherId()))
                                .reversed()
                ).collect(toList());

        LogCollector.info("backend-general", MapUtils.map(
                "env", CURRENT_STAGE,
                "module", MODULE,
                "step", 0,
                "studentId", studentId,
                "teacher", JSON.toJSONString(collect)
        ));

        return collect;
    }

    private Set<Long> getTeacherIdByStudent(Long studentId) {
        return getTeacherByStudent(studentId).stream().map(Teacher::getId).collect(toSet());
    }

    private Set<Teacher> getTeacherByStudent(Long studentId) {
        // 查询学生的所有组
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        Set<Long> groupIdSet = groupMappers.stream().map(GroupMapper::getId).collect(toSet());

        // 查询组下的有效老师
        Map<Long, List<Teacher>> teacher = teacherLoaderClient.loadGroupTeacher(groupIdSet, RefStatus.VALID);

        return teacher.values().stream().flatMap(Collection::stream).collect(toSet());
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "teacherId")
    static class RewardTeacher {
        Long teacherId;
        TeacherDetail teacherDetail;
        UserShippingAddress userShippingAddress;
        UserAuthentication userAuthentication;
        Boolean authenticationSuccess;  // 已认证
        Boolean address;                // 有地址
        Boolean detailAddress;          // 有详细地址
        Boolean notBlackList;           // 不在黑名单
        Boolean bindMobile;             // 绑定手机号
        Set<Integer> clazzLevel;        // 所带年级
        RewardLogistics logistics;      // 快递单
    }

    public List<RewardCompleteOrder> findCurMoonthCompleteOrder() {
        return utopiaSqlReward.withSql("SELECT * FROM VOX_REWARD_COMPLETE_ORDER WHERE BUYER_TYPE = 3 AND DISABLED=0 AND CREATE_DATETIME >= ?")
                .useParamsArgs(MonthRange.current().getStartDate())
                .queryAll(BeanPropertyRowMapper.newInstance(RewardCompleteOrder.class));
    }

    private boolean logisticsIsEmpty(String logistics) {
        return StringUtils.isBlank(logistics) || Objects.equals(logistics.trim(), "0");
    }

    @SuppressWarnings("ALL")
    private static <K, V> Map<K, V> sortByValue(Map<K, V> map, Comparator<V> comparable) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(comparable));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
