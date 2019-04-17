package com.voxlearning.utopia.service.business.impl.support.order.userOrder;

import com.voxlearning.utopia.service.business.impl.support.order.OrderFilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter.*;
import org.springframework.context.ApplicationContext;

/**
 * @author Summer
 * @since 2016/12/9
 */
public class UserOrderFilterChain extends OrderFilterChain {

    // 主要添加的顺序，是按照顺序往下执行的
    public UserOrderFilterChain(ApplicationContext applicationContext) {
        //抵扣奖学金
        this.filters.add(applicationContext.getBean(OrderGiveBalanceFilter.class));
        // 支付
        this.filters.add(applicationContext.getBean(MarkUserOrderPaidFilter.class));
        //礼劵系统购买赠送天数
        this.filters.add(applicationContext.getBean(UserWelfareRedmindFilter.class));
        // 优惠劵处理
        this.filters.add(applicationContext.getBean(OrderCouponFilter.class));
        // 流水
        this.filters.add(applicationContext.getBean(UserPaymentHistoryFilter.class));
        // 激活
        this.filters.add(applicationContext.getBean(UserOrderActiveFilter.class));
        // 广播支付成功消息
        this.filters.add(applicationContext.getBean(AppOrderRemindPubsubFilter.class));
        // 广播支付成功消息, 新的消息，原来的消息让各业务逐步迁移
        this.filters.add(applicationContext.getBean(OrderPaymentPubsubFilter.class));
        // 道具类产品激活（比如次卡）
        this.filters.add(applicationContext.getBean(ItemBaseProductActiveFilter.class));
        // 阿分题英语发送班级动态
        this.filters.add(applicationContext.getBean(UserOrderClazzZoneFilter_AfentiExam.class));
        // 付费专辑购买成功设置订阅状态
        this.filters.add(applicationContext.getBean(UserOrderAlbumSubscribeFilter.class));
        // 点读机教材相关处理
        this.filters.add(applicationContext.getBean(PicListenOrderFilter.class));
        // 付费产品提醒
        this.filters.add(applicationContext.getBean(AppOrderRemindFilter.class));
        // 通用支付提醒
        this.filters.add(applicationContext.getBean(SeattleOrderRemindFilter.class));
        // 作业币消费提醒
        this.filters.add(applicationContext.getBean(FinanceDebitRemindFilter.class));
        // 回调
        this.filters.add(applicationContext.getBean(UserOrderVendorNotifyFilter.class));
        //加家长活跃值
        this.filters.add(applicationContext.getBean(UserActivationFilter.class));
        //临时加上报名活动发短信逻辑
        this.filters.add(applicationContext.getBean(ThirdPartySendMessageFilter.class));


    }
}
