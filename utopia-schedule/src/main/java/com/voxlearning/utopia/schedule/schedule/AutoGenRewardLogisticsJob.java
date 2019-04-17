/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.consumer.RewardManagementClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardCompleteOrder;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ActivityType;
import com.voxlearning.utopia.service.user.api.entities.BlackWhiteList;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Summer Yang on 2016/7/26.
 */
@Named
@ScheduledJobDefinition(
        jobName = "奖品中心每月自动生成学校收货人任务",
        jobDescription = "每个月1日05:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 0 5 1 * ?"
)
public class AutoGenRewardLogisticsJob extends ScheduledJobWithJournalSupport {

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;

    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private SchoolExtServiceClient extServiceClient;
    @Inject private UserServiceClient userSrvCli;
    @Inject private RewardManagementClient rewardMngCli;
    @Inject private RewardServiceClient rewardSrvCli;
    @Inject private BlackWhiteListManagerClient blackWhiteListManagerClient;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private UserLoginServiceClient userLoginServiceClient;

    @ImportService(interfaceClass = CRMRewardService.class) private CRMRewardService crmRewardService;

    private UtopiaSql utopiaSqlOrder;
    private UtopiaSql utopiaSqlReward;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
        utopiaSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

//        int month = MonthRange.current().getMonth();
//        if (month == 1 || month == 2 || month == 7 || month == 8) {
//            jobJournalLogger.log("寒暑假期间不发货，不执行。");
//            return;
//        }
        // 查询所有本月学生类型的快递单
        String currentMonth = DateUtils.dateToString(new Date(), "yyyyMM");
        List<RewardLogistics> logisticsList = crmRewardService.$findRewardLogisticsList(currentMonth, RewardLogistics.Type.STUDENT);
        if (CollectionUtils.isNotEmpty(logisticsList)) {
            for (RewardLogistics logistics : logisticsList) {
                try {
                    handleData(logistics);
                } catch (Exception ex) {
                    logger.error("find logistic teacher error, school is {}", logistics.getSchoolId());
                }
            }
        }

    }

    /**
     * 删除掉校园大使的逻辑
     * @param logistics
     */
    private void handleData(RewardLogistics logistics) {

        logistics = getInfo(logistics);
        if (logistics.getReceiverId() == null) {
            throw new RuntimeException("find logistic teacher error");
        } else {
            // 更新已经完善信息的快递单
            crmRewardService.$upsertRewardLogistics(logistics);
        }
    }

    private RewardLogistics getInfo(RewardLogistics logistics) {
        Function<Long,UserShippingAddress> getAddrFunc = tId -> {
            MapMessage resultMsg = userSrvCli.generateUserShippingAddress(tId);
            if(resultMsg.isSuccess()){
                return (UserShippingAddress) resultMsg.get("address");
            }else
                return null;
        };

        try {
            SchoolExtInfo schoolExtInfo = extServiceClient.getSchoolExtService().loadSchoolExtInfo(logistics.getSchoolId()).getUninterruptibly();
            RewardLogistics temp = Optional.ofNullable(schoolExtInfo)
                    .filter(e -> schoolExtInfo.getReceiveTeacher() != null)
                    .map(SchoolExtInfo::getReceiveTeacher)
                    .map(tid -> teacherLoaderClient.loadTeacherDetail(tid))
                    .filter(t -> logistics.getSchoolId().equals(t.getTeacherSchoolId()))
                    .map(e -> getAddrFunc.apply(e.getId()))
                    .map(e -> {
                        // 更新
                        logistics.setPostCode(e.getPostCode());
                        logistics.setReceiverId(e.getUserId());
                        logistics.setReceiverName(e.getReceiver());
                        logistics.setSensitivePhone(e.getReceiverPhone());
                        logistics.setDetailAddress(e.getDetailAddress());
                        logistics.setLogisticType(e.getLogisticTypeName());
                        return logistics;
                    }).orElse(null);
            if (temp != null) {
                return temp;
            }
        } catch (Exception e) {
            logger.error("load reward whiteBlackList error, school is {} ", logistics.getSchoolId(), e);
        }

        // 查询
        String sql = "SELECT CLAZZ_ID, COUNT(1) AS ORDER_COUNT " +
                " FROM VOX_REWARD_COMPLETE_ORDER " +
                " WHERE SCHOOL_ID=? " +
                " AND CREATE_DATETIME>=? " +
                " GROUP BY CLAZZ_ID " +
                " ORDER BY ORDER_COUNT DESC";

        Date startDate = MonthRange.current().getStartDate();
        List<Map<String, Object>> mapList = utopiaSqlReward.withSql(sql).useParamsArgs(logistics.getSchoolId(), startDate).queryAll();

        // 从上往下寻找老师
        for (Map<String, Object> map : mapList) {

            Long clazzId = SafeConverter.toLong(map.get("CLAZZ_ID"));
            // 过滤掉clazzId为异常数据的情况，以前碰到了值为零的坑
            // 如果很寸，碰到临时换班的情况，并且这学校又没有其它班级可以选了，就有问题
            // 这里改成，沿用之前发货单里面的地址信息，不再取老师判断
            if(clazzId == 0L){
                continue;
            }

            // 从该班级的最近订单记录下面抽一条，以确保选中的老师最近组里面还有活跃学生
            RewardCompleteOrder activeOrder = utopiaSqlReward.withSql(
                    "SELECT * FROM VOX_REWARD_COMPLETE_ORDER " +
                            " WHERE CLAZZ_ID = ? " +
                            " AND CREATE_DATETIME>=?" +
                            " ORDER BY CREATE_DATETIME desc limit 1")
                    .useParamsArgs(clazzId,startDate)
                    .queryAll(BeanPropertyRowMapper.newInstance(RewardCompleteOrder.class))
                    .stream()
                    .findAny()
                    .orElse(null);

            if(activeOrder != null){
                Teacher teacher = teacherLoaderClient.loadTeacher(activeOrder.getReceiverId());
                // 绑定手机
                UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacher.getId());
                if (authentication == null || !authentication.isMobileAuthenticated()) {
                    continue;
                }

                // 有详细地址
                UserShippingAddress address = getAddrFunc.apply(teacher.getId());
                if (address == null || StringUtils.isBlank(address.getDetailAddress())) {
                    continue;
                }

                // 前面为学生寻找老师时,故意没有卡死,这里尽量避开
                List<BlackWhiteList> bwList = blackWhiteListManagerClient.loadUserBlackWhiteLists(teacher.getId(), ActivityType.拒收学生奖品名单);
                if (CollectionUtils.isNotEmpty(bwList)) {
                    continue;
                }

                // 过滤假老师
                if (teacherLoaderClient.isFakeTeacher(teacher.getId())) {
                    continue;
                }

                // 最近三月有登陆
                Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacher.getId());
                if (lastLoginTime == null) {
                    continue;
                }
                Date now = new Date();
                Date start = DateUtils.addMonths(now, -3);
                DayRange dayRange = new DayRange(start.getTime(), now.getTime());
                if (!dayRange.contains(lastLoginTime)) {
                    continue;
                }

                // 更新
                logistics.setPostCode(address.getPostCode());
                logistics.setReceiverId(address.getUserId());
                logistics.setReceiverName(address.getReceiver());
                logistics.setSensitivePhone(address.getReceiverPhone());
                logistics.setDetailAddress(address.getDetailAddress());
                logistics.setLogisticType(address.getLogisticTypeName());
                return logistics;
            }
        }
        Long teacherId = logistics.getReceiverId();

        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacherId);
        UserShippingAddress address = getAddrFunc.apply(teacherId);
        Boolean fakeTeacher = teacherLoaderClient.isFakeTeacher(teacherId);
        // 之前的流程对拒收名单都比较放纵, 老师很愤怒, 这里必须严卡
        List<BlackWhiteList> blackList = blackWhiteListManagerClient.loadUserBlackWhiteLists(teacherId, ActivityType.拒收学生奖品名单);

        boolean noLogin = true;
        Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacherId);
        if (lastLoginTime != null) {
            Date now = new Date();
            Date start = DateUtils.addMonths(now, -3);
            DayRange dayRange = new DayRange(start.getTime(), now.getTime());
            if (dayRange.contains(lastLoginTime)) {
                noLogin = false;
            }
        }

        boolean noAuth = authentication == null || !authentication.isMobileAuthenticated();
        boolean noDetailAddress = address == null || StringUtils.isBlank(address.getDetailAddress());
        boolean inBlack = CollectionUtils.isNotEmpty(blackList);

        boolean notFountTeacher = noLogin || noAuth || noDetailAddress || inBlack || fakeTeacher;
        if (notFountTeacher) {
            logger.info("发货单最终没有找到收货老师,发货单ID" + logistics.getId());
            if (RuntimeMode.isProduction()) {
                emailServiceClient.createPlainEmail()
                        .to("junbao.zhang@17zuoye.com")
                        .subject("发货单最终没有找到收货老师")
                        .body("发货单ID：" + logistics.getId() + " 学校ID：" + logistics.getSchoolId())
                        .send();
            }
        }

        // 针对抽奖的订单，从前面那个批处理漏过来一些班里老师收货地址为空的单子。
        // 如果到这里面依然是联系方式有缺失的话，则删掉订单
        if (notFountTeacher || StringUtils.isEmpty(logistics.getDetailAddress()) || StringUtils.isEmpty(logistics.getSensitivePhone())) {
            // 漏到这里的，就是所有班里面都没有符合条件的全校代收货老师
            // 删掉Order和CompleteOrder记录
            Consumer<RewardOrder> removeOrderConsumer = o -> {
                rewardSrvCli.deleteRewardOrder(o);

                if (o.getCompleteId() != null)
                    crmRewardService.deleteCompleteOrder(o.getCompleteId());

                // 快递单也要置上状态
                logistics.setDisabled(true);
                crmRewardService.$upsertRewardLogistics(logistics);
            };

            rewardMngCli.loadRewardOrderByLogisticId(logistics.getId()).forEach(removeOrderConsumer);
        }

        return logistics;
    }
}
