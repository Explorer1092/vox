package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304;

import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 发放奖励
 *
 * @author Wenlong Meng
 * @since  Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.RewardProcessor")
public class RewardProcessor  implements IProcessor<ActivityContext> {
    //local variables
    @Inject private CouponServiceClient couponServiceClient;
    UtopiaCache utopiaCache = CacheSystem.CBS.getCache("flushable");
    //Logic

    /**
     * 执行
     *
     * @param c context see {@link ActivityContext}
     */
    @Override
    public void process(ActivityContext c) {
        LoggerUtils.debug("RewardProcessor.process", c);
        Long studentId = c.getStudentId();
        Long parentId = c.getParentId();
        int userPeriod = c.getUserActivity().getExtInfo().size();
        if(userPeriod > c.getActivity().currentPeriod().getIndex()){
            LoggerUtils.info("activity.ocrMAv20190304.error", c,"用户已完成");
            c.setMapMessage(MapMessage.errorMessage("用户已完成"));
            c.setTerminate(true);
            return;
        }
        String couponId = getCouponId(userPeriod);
        MapMessage mapMessage = couponServiceClient.sendCoupon(couponId, parentId);
        LoggerUtils.debug("activity.ocrMAv20190304.reward.coupon", mapMessage);
        if(!mapMessage.isSuccess()){
            LoggerUtils.info("activity.ocrMAv20190304.reward.error", mapMessage.isSuccess());
            c.setMapMessage(MapMessage.errorMessage("发放奖励失败"));
            c.setTerminate(true);
            return;
        }
        String key = HomeworkUtil.generatorDayID("parentHomework_activity_reward", studentId);
        LoggerUtils.debug("activity.ocrMAv20190304.rewards", key);
        List<Integer> m = utopiaCache.load(key);
        if(m == null){
            m = new ArrayList<>();
        }
        m.add(c.getUserActivity().getExtInfo().size());
        utopiaCache.set(key, 60*60*24*7, m);
    }

    /**
     * 获取优惠券id
     *
     * @param i
     * @return
     */
    private String getCouponId(int i){
        return RuntimeMode.isProduction() || RuntimeMode.isStaging() ? rewards.get(i):testRewards.get(i);
    }

    //优惠券id定义
    private static Map<Integer, String> testRewards = Maps.newHashMap();
    private static Map<Integer, String> rewards = Maps.newHashMap();
    static {
        //测试
        testRewards.put(1, "5c7682f7ac74595c0feed2cd");
        testRewards.put(2, "5c7682f7ac74595c0feed2cd");
        testRewards.put(3, "5c7749fc8edbc8585028569b");
        testRewards.put(4, "5c7749fc8edbc8585028569b");
        //线上环境
        rewards.put(1, "5c775fc758e5147e060ac0ea");
        rewards.put(2, "5c775fc758e5147e060ac0ea");
        rewards.put(3, "5c776013a29361229dbe3bc1");
        rewards.put(4, "5c776013a29361229dbe3bc1");
    }

}
