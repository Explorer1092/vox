/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.constant.OneLevelCategoryType;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.consumer.RewardManagementClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardCompleteOrder;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by XiaoPeng.Yang on 14-9-1.
 */
@Named
@ScheduledJobDefinition(
        jobName = "奖品中心每月自动生成发货单数据任务",
        jobDescription = "每个月1日01:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 1 1 * ?"
)
@ProgressTotalWork(100)
public class AutoGenCompleteOrderJob extends ScheduledJobWithJournalSupport {

    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private RewardManagementClient rewardManagementClient;
    @Inject private RewardServiceClient rewardServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSqlReward;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        //经和产品确认， 每年的1月 2月，7月 8月 不发货。
        int month = MonthRange.current().getMonth();
        if (month == 1 || month == 2 || month == 7 || month == 8) {
            jobJournalLogger.log("寒暑假期间不发货，不执行。");
            return;
        }
        Date startDate = MonthRange.current().previous().getStartDate();
        Date endDate = MonthRange.current().previous().getEndDate();
        if (month == 3) {
            startDate = MonthRange.current().previous().previous().previous().getStartDate();//去年12月1号开始的订单
        }
        if (month == 9) {
            startDate = MonthRange.current().previous().previous().previous().getStartDate();//6月1号开始的订单
        }
//        Date startDate = MonthRange.current().previous().getStartDate();
//        Date endDate = MonthRange.current().previous().getEndDate();
        Map<String, Object> param = new HashMap<>();
        param.put("status", RewardOrderStatus.SUBMIT.name());
        param.put("startDate", startDate);
        param.put("endDate", endDate);
        //List<RewardOrder> orderList = rewardManagementClient.loadExportRewardOrdersByParameters(param);
        List<RewardOrder> orderList = getRewardOrder(startDate, endDate, RewardOrderStatus.SUBMIT.name());
        jobJournalLogger.log("本月共有{}个待审核状态订单需要处理", orderList.size());
        if (orderList.isEmpty()) {
            return;
        }
        progressMonitor.worked(20);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, orderList.size());
        jobJournalLogger.log("开始处理订单");
        for (RewardOrder order : orderList) {
            try {
                handleOrder(order);
            } catch (Exception e) {
                // 取消订单， 返回用户园丁豆和学豆
                MapMessage message = rewardServiceClient.deleteRewardOrder(order);
                if (message.isSuccess()) {
                    jobJournalLogger.log("删除订单成功，异常：{}，订单ID：{}", e.getMessage(), order.getId());
                } else {
                    jobJournalLogger.log("删除订单失败，异常：{}，订单ID：{}", e.getMessage(), order.getId());
                }
            } finally {
                monitor.worked(1);
            }
        }

        handleToReduceIntegral(orderList);

        jobJournalLogger.log("奖品中心每月自动生成发货单数据任务执行完毕");
        progressMonitor.done();
    }

    private List<RewardOrder> getRewardOrder(Date startDate, Date endDate, String status) {
        return utopiaSqlReward.withSql("SELECT * FROM VOX_REWARD_ORDER WHERE DISABLED=0 AND STATUS = ? AND CREATE_DATETIME >= ? AND CREATE_DATETIME <= ?")
                .useParamsArgs(status, startDate, endDate)
                .queryAll(BeanPropertyRowMapper.newInstance(RewardOrder.class));
    }

    private void handleOrder(final RewardOrder order) {
        try {
            RewardCompleteOrder completeOrder = new RewardCompleteOrder();
            completeOrder.setOrderId(order.getId());
            UserShippingAddress address = null;
            TeacherDetail teacherDetail;
            User user = userLoaderClient.loadUser(order.getBuyerId());
            if (user == null) {
                throw new RuntimeException("User is null");
            }
            UserAuthentication authentication;
            switch (user.fetchUserType()) {
                case STUDENT:
                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());

                    Clazz clazz = studentDetail.getClazz();
                    if (clazz == null) {
                        throw new RuntimeException("clazz is null");
                    }

                    completeOrder.setReceiverId(0L);    // 数据库不可为空先设置一下
                    completeOrder.setReceiverName("");
                    completeOrder.setClazzId(clazz.getId());
                    completeOrder.setClazzName(clazz.formalizeClazzName());
                    completeOrder.setClazzLevel(clazz.getClazzLevel().getLevel());
                    completeOrder.setSchoolId(clazz.getSchoolId());
                    completeOrder.setSchoolName(studentDetail.getStudentSchoolName());
                    break;
                case TEACHER:
                    // 是否认证
                    if (user.getAuthenticationState() != AuthenticationState.SUCCESS.getState()) {
                        throw new RuntimeException("teacher not authenticationState");
                    }
                    authentication = userLoaderClient.loadUserAuthentication(user.getId());
                    if (authentication == null || !authentication.isMobileAuthenticated()) {
                        throw new RuntimeException("Teacher has no mobile");
                    }
                    completeOrder.setSensitivePhone(authentication.getSensitiveMobile());
                    address = userLoaderClient.loadUserShippingAddress(user.getId());
                    if (address == null || StringUtils.isBlank(address.getDetailAddress())) {
                        throw new RuntimeException("User shipping address is null");
                    }
                    completeOrder.setReceiverName(user.fetchRealname());
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                    completeOrder.setSchoolId(teacherDetail.getTeacherSchoolId());
                    completeOrder.setSchoolName(teacherDetail.getTeacherSchoolName());
                    break;
                case RESEARCH_STAFF:
                    address = userLoaderClient.loadUserShippingAddress(user.getId());
                    if (address == null || StringUtils.isBlank(address.getDetailAddress())) {
                        throw new RuntimeException("User shipping address is null");
                    }
                    completeOrder.setReceiverName(user.fetchRealname());
                    completeOrder.setSchoolId(address.getSchoolId());
                    completeOrder.setSchoolName(address.getSchoolName());
                    completeOrder.setSensitivePhone(address.getSensitivePhone());
                    break;
                default:
                    throw new RuntimeException("Illegal role");
            }

            if (address != null) {
                completeOrder.setReceiverId(address.getUserId());
                completeOrder.setProvinceName(address.getProvinceName());
                completeOrder.setProvinceCode(address.getProvinceCode());
                completeOrder.setCityName(address.getCityName());
                completeOrder.setCityCode(address.getCityCode());
                completeOrder.setCountyName(address.getCountyName());
                completeOrder.setCountyCode(address.getCountyCode());
                completeOrder.setDetailAddress(StringHelper.filterEmojiForMysql(address.getDetailAddress()));
                completeOrder.setLogisticType(address.getLogisticTypeName());
                completeOrder.setPostCode(address.getPostCode());
            }
            completeOrder.setBuyerId(order.getBuyerId());
            completeOrder.setBuyerName(order.getBuyerName());
            completeOrder.setBuyerType(order.getBuyerType());
            completeOrder.setDiscount(order.getDiscount());
            completeOrder.setPrice(order.getPrice());
            completeOrder.setTotalPrice(order.getTotalPrice());
            completeOrder.setProductId(order.getProductId());
            completeOrder.setProductName(order.getProductName());
            completeOrder.setSkuId(order.getSkuId());
            completeOrder.setSkuName(order.getSkuName());
            completeOrder.setQuantity(order.getQuantity());
            completeOrder.setSaleGroup(order.getSaleGroup());
            completeOrder.setStatus(RewardOrderStatus.PREPARE.name());
            completeOrder.setUnit(order.getUnit());
            Long completeOrderId = rewardManagementClient.persistRewardCompleteOrder(completeOrder);
            rewardManagementClient.updateRewardOrderById(order.getId(), RewardOrderStatus.PREPARE, completeOrderId);
            // 生成快递单
            String month = DateUtils.dateToString(new Date(), "yyyyMM");
            if (user.fetchUserType() == UserType.STUDENT) {
                // 一个学校一个
                /*Long schoolId = completeOrder.getSchoolId();
                // 查询快递单 看有没有生成 学生类型的
                RewardLogistics logistics = crmRewardService.$findRewardLogistics(schoolId, month, RewardLogistics.Type.STUDENT);
                if (logistics == null) {
                    // 生成一条记录
                    logistics = new RewardLogistics();
                    logistics.setCityCode(completeOrder.getCityCode());
                    logistics.setCityName(completeOrder.getCityName());
                    logistics.setProvinceCode(completeOrder.getProvinceCode());
                    logistics.setProvinceName(completeOrder.getProvinceName());
                    logistics.setCountyName(completeOrder.getCountyName());
                    logistics.setCountyCode(completeOrder.getCountyCode());
                    logistics.setType(RewardLogistics.Type.STUDENT);
                    logistics.setSchoolName(completeOrder.getSchoolName());
                    logistics.setSchoolId(completeOrder.getSchoolId());
                    logistics.setMonth(month);
                    logistics.setIsBack(false);
                    logistics = crmRewardService.$upsertRewardLogistics(logistics);
                }
                // 更新订单快递单ID
                rewardManagementClient.updateRewardOrderLogisticsId(order.getId(), logistics.getId());
                rewardManagementClient.updateRewardCompleteOrderLogisticsId(completeOrderId, logistics.getId());*/
            } else {
                // 一人一个单子
                RewardLogistics logistics = crmRewardService.$findRewardLogistics(user.getId(), RewardLogistics.Type.TEACHER, month);
                if (logistics == null) {
                    logistics = new RewardLogistics();
                    logistics.setCityCode(completeOrder.getCityCode());
                    logistics.setCityName(completeOrder.getCityName());
                    logistics.setProvinceCode(completeOrder.getProvinceCode());
                    logistics.setProvinceName(completeOrder.getProvinceName());
                    logistics.setCountyName(completeOrder.getCountyName());
                    logistics.setCountyCode(completeOrder.getCountyCode());
                    logistics.setDetailAddress(completeOrder.getDetailAddress());
                    logistics.setLogisticType(completeOrder.getLogisticType());
                    logistics.setType(RewardLogistics.Type.TEACHER);
                    logistics.setSensitivePhone(address.getReceiverPhone());
                    logistics.setSchoolName(completeOrder.getSchoolName());
                    logistics.setSchoolId(completeOrder.getSchoolId());
                    logistics.setReceiverName(address.getReceiver());
                    logistics.setReceiverId(completeOrder.getReceiverId());
                    logistics.setMonth(month);
                    logistics.setIsBack(false);
                    logistics.setPostCode(completeOrder.getPostCode());
                    logistics = crmRewardService.$upsertRewardLogistics(logistics);
                }
                // 更新订单快递单ID
                rewardManagementClient.updateRewardOrderLogisticsId(order.getId(), logistics.getId());
                rewardManagementClient.updateRewardCompleteOrderLogisticsId(completeOrderId, logistics.getId());
            }
        } catch (Exception ex) {
            throw new RuntimeException("handle Order Exception:" + ex.getMessage());
        }
    }

    private void handleToReduceIntegral(List<RewardOrder> orderList) {
        Map<Long, List<RewardOrder>> userRewardOrder = orderList.stream()
                .filter(e -> RewardProductType.JPZX_SHIWU.name().equals(e.getProductType())
                        || OneLevelCategoryType.JPZX_SHIWU.intType().toString().equals(e.getProductType()))
                .filter(r -> r.getBuyerType() != null && r.getBuyerType() == UserType.TEACHER.getType())
                .collect(Collectors.groupingBy(RewardOrder::getBuyerId));
        if (MapUtils.isNotEmpty(userRewardOrder)) {
            Map<Long, TeacherDetail> teacherDetailMap = new HashMap<>();
            List<Long> userIdList = new ArrayList<>(userRewardOrder.keySet());
            for (int i = 0; i < userIdList.size(); i += 200) {
                Map<Long, TeacherDetail> detailMap = teacherLoaderClient.loadTeacherDetails(userIdList.subList(i, Math.min(i + 200, userIdList.size())));
                if (MapUtils.isEmpty(detailMap)) {
                    continue;
                }
                teacherDetailMap.putAll(detailMap);
            }

            for(Map.Entry<Long, List<RewardOrder>> entry : userRewardOrder.entrySet()) {
                if (CollectionUtils.isEmpty(entry.getValue())){
                    continue;
                }
                int total = entry.getValue().stream().mapToInt(r -> r.getTotalPrice().intValue()).sum();
                if (total >= 500) {
                    continue;
                }

                TeacherDetail teacherDetail = teacherDetailMap.get(entry.getKey());
//                boolean reduce = teacherDetail != null && grayFunctionManagerClient.getTeacherGrayFunctionManager()
//                        .isWebGrayFunctionAvailable(teacherDetail,"Reward","ExchangeReduction", true);
//                if (!reduce) {
//                    continue;
//                }
                int num = Math.min(200 * 10, teacherDetail.getUserIntegral().getIntegral().getUsableIntegral());
                if (num <= 0) {
                    continue;
                }
                String content = "上月累积兑换实物未满500园丁豆,已扣除"+ (num / 10) +"园丁豆作为物流费用";
                if (teacherDetail.isJuniorTeacher()) {
                    content = "上月累积兑换实物未满5000学豆,已扣除"+ (num) +"学豆作为物流费用";
                }
                IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(entry.getKey(), IntegralType.REWARD_EXCHANGE_ACTUAL_ITEMS_REDUCTION_INTEGRAL)
                        .withIntegral(-num)
                        .withComment(content)
                        .build();
                try {
                    MapMessage result = userIntegralService.changeIntegral(history);
                    if (result.isSuccess()) {
                        AppMessage message = new AppMessage();
                        message.setUserId(entry.getKey());
                        message.setMessageType(TeacherMessageType.ACTIVIY.getType());
                        message.setTitle("通知");
                        message.setContent(content);
                        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

                        // 发pc端信息
                        teacherLoaderClient.sendTeacherMessage(entry.getKey(), content);
                        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PRIMARY_TEACHER, Arrays.asList(entry.getKey()), new HashMap<>());
                    } else {
                        logger.warn("changeIntegral failed. userId:{}, Integral:{}, changeIntegralResult:{}", entry.getKey(), num, result.getInfo());
                    }
                } catch (Exception ex) {
                    logger.error("handleToReduceIntegral error. history {}", history, ex);
                }
            }
        }
    }
}
