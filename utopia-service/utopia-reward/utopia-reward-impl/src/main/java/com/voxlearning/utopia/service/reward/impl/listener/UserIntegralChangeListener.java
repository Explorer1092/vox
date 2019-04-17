package com.voxlearning.utopia.service.reward.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.reward.api.newversion.RewardCenterService;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

/**
 * 监听学豆变化增加能量柱
 */
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.integral.change.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.user.integral.change.topic")
})
public class UserIntegralChangeListener extends SpringContainerSupport implements MessageListener {

    private static final Set<Integer> set = new HashSet<>();

    static {
        set.add(IntegralType.REWARD_TOBY_INTEGRAL.getType());                   // 托比支出
        set.add(IntegralType.REWARD_GAME_CLAW_PAY_INTEGRAL.getType());          // 小游戏支出
        set.add(IntegralType.REWARD_PUBLIC_GOOD_INTEGRAL.getType());            // 公益捐赠支出
        set.add(IntegralType.PRIMARY_STUDENT_APP_LOTTERY.getType());            // 奖品中心抽大奖
        set.add(IntegralType.REWARD_XNGOOD_EXCHANGE_INTEGRAL.getType());        // 头饰支出
        set.add(IntegralType.REWARD_MINICOURSE_EXCHANGE_INTEGRAL.getType());        // 微课支出
        set.add(IntegralType.REWARD_TEACHINGRESOURCES_EXCHANGE_INTEGRAL.getType());        // 教学资源支出
        set.add(IntegralType.REWARD_FLOWPACKET_EXCHANGE_INTEGRAL.getType());        // 流量包支出
        set.add(IntegralType.REWARD_COUPON_EXCHANGE_INTEGRAL.getType());        // 优惠券支出

        // @see com.voxlearning.utopia.service.campaign.impl.lottery.wrapper.StudentLotteryWrapper.doLottery()
        set.add(IntegralType.学生抽奖_产品平台.getType());                         // PC奖品中心幸运抽大奖
        set.add(IntegralType.学生班级空间签到.getType());                          // PC班级空间每日签到
        set.add(IntegralType.学生班级空间购买付费气泡.getType());                   // PC班级空间购买气泡
        set.add(IntegralType.赠送礼物.getType());                                // PC班级空间赠送礼物
    }

    @Autowired
    @Qualifier("com.voxlearning.utopia.service.reward.impl.service.newversion.RewardCenterSeviceImpl")
    private RewardCenterService rewardCenterService;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            return;
        }
        try {
            String bodyAsString = message.getBodyAsString();
            logger.debug("UserIntegralChangeListener bodyAsString {}", bodyAsString);

            IntegralHistory integralHistory = JsonUtils.fromJson(bodyAsString, IntegralHistory.class);
            if (integralHistory == null || integralHistory.getUserId() == null) {
                return;
            }
            Student student = studentLoaderClient.loadStudent(integralHistory.getUserId());
            if (student == null) {
                return;
            }

            // 如果是收入则忽略
            Integer integral = integralHistory.getIntegral();
            if (integral >= 0) {
                return;
            }

            // 如果是不关心的消耗类型则忽略
            Integer integralType = integralHistory.getIntegralType();
            if (!set.contains(integralType)) {
                return;
            }
            // 公益翻倍
            if (integralType == IntegralType.REWARD_PUBLIC_GOOD_INTEGRAL.getType()) {
                integral = integral * 2;
            }

            Long userId = integralHistory.getUserId();
            rewardCenterService.updatePowerPillarNum(userId, -integral);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


}
