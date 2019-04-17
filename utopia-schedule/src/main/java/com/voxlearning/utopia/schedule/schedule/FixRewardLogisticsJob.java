
package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.consumer.RewardManagementClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardCompleteOrder;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;


@Named
@ScheduledJobDefinition(
        jobName = "奖品中心修复订单收货人为空",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 5 1 * ?",
        ENABLED = false
)
public class FixRewardLogisticsJob extends ScheduledJobWithJournalSupport {

    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private UserServiceClient userSrvCli;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private RewardServiceClient rewardServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @Inject private SchoolExtServiceClient extServiceClient;
    @Inject private RewardManagementClient rewardMngCli;

    @Inject private RaikouSDK raikouSDK;

    private UtopiaSql utopiaSqlOrder;
    private UtopiaSql orderSql;
    private UtopiaSql orderSqlReward;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("main");
        orderSql = utopiaSqlFactory.getUtopiaSql("order");
        orderSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String sql = " SELECT DISTINCT LOGISTICS_ID " +
                " FROM VOX_REWARD_ORDER " +
                " WHERE CREATE_DATETIME >= '2018-05-01 00:00:00' " +
                " AND status = 'PREPARE' " +
                " AND disabled = 0";

        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    List<Long> logisticsIdList = orderSqlReward.withSql(sql).queryColumnValues(Long.class);
                    int count = 0;
                    int empty = 0;
                    int graduateCount = 0;
                    int tchCount = 0;
                    int refundCount = 0;

                    for (Long logisticsId : logisticsIdList) {
                        RewardLogistics logistics = crmRewardService.$loadRewardLogistics(logisticsId);
                        if (logistics == null) {
                            logger.info("RewardM5 empty logistics id is {}", logisticsId);
                            continue;
                        }

                        if (BooleanUtils.isTrue(logistics.getDisabled())) {
                            List<RewardOrder> orders = rewardMngCli.loadRewardOrderByLogisticId(logistics.getId());
                            boolean refund = false;

                            RewardOrder anyOrder = orders.stream().findFirst().orElse(null);
                            if (anyOrder != null) {
                                Long userId = anyOrder.getBuyerId();
                                User user = userLoaderClient.loadUser(userId);
                                if (user.isStudent()) {
                                    StudentDetail stuDetail = studentLoaderClient.loadStudentDetail(userId);
                                    Clazz clazz = stuDetail.getClazz();
                                    if (clazz == null)
                                        continue;

                                    // 非毕业班要退掉，王珊说的
                                    if (clazz.isTerminalClazz()) {
                                        graduateCount++;
                                    } else {
                                        refundCount++;
                                        refund = true;
                                    }
                                } else if (user.isTeacher()) {
                                    tchCount++;
                                }
                            }

                            count++;


                            if (refund) {
                                // 漏到这里的，就是所有班里面都没有符合条件的全校代收货老师
                                // 删掉Order和CompleteOrder记录
                                Consumer<RewardOrder> removeOrderConsumer = o -> {
                                    rewardServiceClient.deleteRewardOrder(o);

                                    if (o.getCompleteId() != null)
                                        crmRewardService.deleteCompleteOrder(o.getCompleteId());
                                };

                                orders.forEach(removeOrderConsumer);
                            }
                        }
                    }


                    // 批量处理学豆扣除
                    AtomicInteger deductCount = new AtomicInteger();
                    teacherLoaderClient.loadTeacherClazzIds(12218076L).forEach(cId -> {
                        Clazz clazz = raikouSDK.getClazzClient()
                                .getClazzLoaderClient()
                                .loadClazz(cId);
                        if (clazz.isTerminalClazz() && clazz.getClazzLevel().getLevel() != 5) {
                            return;
                        }

                        studentLoaderClient.loadClazzStudentIds(cId).forEach(sId -> {
                            deductCount.addAndGet(1);
                            IntegralHistory integralHistory = new IntegralHistory(sId, IntegralType.奖品相关, -850);
                            integralHistory.setComment("兑换奖品");
                            MapMessage msg = userIntegralService.changeIntegral(integralHistory);
                        });
                    });

                    logger.info("RewardM5 Miss num sum:{},emptyAddress:{},graduateCount:{},tchCount:{},deductCount:{},refundCount:{}",
                            count, empty, graduateCount, tchCount, deductCount, refundCount);

                    return null;
                }).execute();
    }

    private RewardLogistics getInfo(RewardLogistics logistics) {
        Function<Long, UserShippingAddress> getAddrFunc = tId -> Optional.ofNullable(userSrvCli.generateUserShippingAddress(tId))
                .filter(MapMessage::isSuccess)
                .map(r -> (UserShippingAddress) r.get("address"))
                .orElse(null);

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
        List<Map<String, Object>> mapList = orderSqlReward.withSql(sql).useParamsArgs(logistics.getSchoolId(), startDate).queryAll();

        // 从上往下寻找老师
        for (Map<String, Object> map : mapList) {

            Long clazzId = SafeConverter.toLong(map.get("CLAZZ_ID"));
            // 过滤掉clazzId为异常数据的情况，以前碰到了值为零的坑
            // 如果很寸，碰到临时换班的情况，并且这学校又没有其它班级可以选了，就有问题
            // 这里改成，沿用之前发货单里面的地址信息，不再取老师判断
            if (clazzId == 0L) {
                continue;
            }

            // 从该班级的最近订单记录下面抽一条，以确保选中的老师最近组里面还有活跃学生
            RewardCompleteOrder activeOrder = orderSqlReward.withSql(
                    "SELECT * FROM VOX_REWARD_COMPLETE_ORDER " +
                            " WHERE CLAZZ_ID = ? " +
                            " AND CREATE_DATETIME>=?" +
                            " ORDER BY CREATE_DATETIME desc limit 1")
                    .useParamsArgs(clazzId, startDate)
                    .queryAll(BeanPropertyRowMapper.newInstance(RewardCompleteOrder.class))
                    .stream()
                    .findAny()
                    .orElse(null);

            if (activeOrder != null) {
                Teacher teacher = teacherLoaderClient.loadTeacher(activeOrder.getReceiverId());
                UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacher.getId());
                if (authentication == null || !authentication.isMobileAuthenticated()) {
                    continue;
                }

                UserShippingAddress address = getAddrFunc.apply(teacher.getId());
                if (address == null || StringUtils.isBlank(address.getDetailAddress())) {
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

        return logistics;
    }
}
