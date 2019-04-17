package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.api.constant.WarmHeartPlanConstant;
import com.voxlearning.utopia.service.campaign.impl.dao.WarmHeartPlanActivityDao;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsUtils;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named
@Slf4j
public class WarmHeartPlanListenerServiceUtils implements InitializingBean {

    @Inject
    private WarmHeartPlanActivityDao warmHeartPlanActivityDao;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    private ParentNewTermPlanServiceImpl parentNewTermPlanService;
    @Inject
    private TeacherNewTermPlanServiceImpl teacherNewTermPlanService;
    @Inject
    private CacheExistsUtils cacheExistsUtils;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private WarmHeartPlanServiceImpl warmHeartPlanService;
    @Inject
    private CampaignCacheSystem campaignCacheSystem;

    private UtopiaCache storageCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        storageCache = campaignCacheSystem.CBS.storage;
    }

    private static List<String> parentMsg1 = new ArrayList<>();
    private static List<String> parentMsg2 = new ArrayList<>();
    private static List<String> parentMsg3 = new ArrayList<>();
    private static List<String> parentMsg4 = new ArrayList<>();

    static {
        parentMsg1.add("【爱的鼓励】你就是孩子心里最温暖的存在，还可以添加更多亲子陪伴目标喔，继续参加！\uD83D\uDC49");
        parentMsg1.add("【好消息】再继续添加亲子陪伴目标，你就能超过98%的家长喽！");
        parentMsg1.add("【暖心亲子计划】爱是最好的陪伴，知道认真坚持起来是什么样吗？继续添加目标！\uD83D\uDC49");
        parentMsg1.add("坚持高质量陪娃，是一种怎样的体验？继续添加目标！\uD83D\uDC49");
        parentMsg1.add("家长门诊：98%父母最爱的陪娃方式，你都试过了吗?尝试更多目标\uD83D\uDC49");
        parentMsg1.add("这些高质量的陪娃亲子习惯，正在让你越变越棒。继续添加目标!\uD83D\uDC49");
        parentMsg1.add("【好消息】原来还可以继续添加目标! 坚持成为更棒的父母，赢取百元课程礼券！Go>>");
        parentMsg1.add("【好消息】又双叒叕可以添加更多亲子陪伴目标，快来参加！\uD83D\uDC49");

        parentMsg2.add("【爱的召唤】您的孩子正邀请您加入「暖心亲子计划」。坚持21天，让孩子的成长，因你的陪伴而美妙 ！点击查看>>");
        parentMsg2.add("【暖心亲子计划】您的孩子已邀请您加入陪伴计划。成长的路上，有父母的陪伴真好! 快去查看吧\uD83D\uDC49");
        parentMsg2.add("【暖心亲子计划】您的孩子正在召唤您喔！每天陪孩子做些小事，成为更棒的父母，赢取百元课程礼券！Go>>");
        parentMsg2.add("【爱的召唤】您的孩子邀请您加入「暖心亲子计划」，再忙也不要忘了陪陪孩子喔！立即查看\uD83D\uDC49");
        parentMsg2.add("【来自孩子的召唤】成长的路上，有父母的陪伴真好!爸爸妈妈快来陪陪我吧！Go>>");

        parentMsg3.add("【暖心亲子计划】坚持21天打卡，用爱伴孩子成长,不留遗憾！马上打卡\uD83D\uDC49>>");
        parentMsg3.add("【暖心亲子计划】陪孩子在一起，真的就足够了，坚持21天打卡，感受满满幸福！\uD83D\uDC49");

        parentMsg4.add("【暖心亲子计划】不知道怎么高质量陪伴孩子，照着这样做总是没错了！查看更多>>\uD83D\uDC49");
        parentMsg4.add("【暖心亲子计划】陪伴孩子也需要一些套路，百万家长是这样陪孩子的！>>\uD83D\uDC49");
    }

    private String genParentMsg1Key(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(
                WarmHeartPlanConstant.WARM_HEART_MSG_INDEX_PARENT_1,
                new String[]{"SID"},
                new Object[]{studentId}
        );
    }

    /**
     * 提醒家长可以重复添加
     */
    public String getParentMsg1Text(Long studentId) {
        String key = genParentMsg1Key(studentId);
        Long index = storageCache.incr(key, 1, 0, calcExpire());
        if (index >= parentMsg1.size()) {
            return "";
        }

        return parentMsg1.get(SafeConverter.toInt(index));
    }


    private String genParentMsg2Key(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(
                WarmHeartPlanConstant.WARM_HEART_MSG_INDEX_PARENT_2,
                new String[]{"SID"},
                new Object[]{studentId}
        );
    }


    public String getParentMsg2Text(Long studentId) {
        String key = genParentMsg2Key(studentId);
        Long index = storageCache.incr(key, 1, 0, calcExpire());
        if (index >= parentMsg2.size()) {
            return "";
        }

        return parentMsg2.get(SafeConverter.toInt(index));
    }


    private String genParentMsg3Key(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(
                WarmHeartPlanConstant.WARM_HEART_MSG_INDEX_PARENT_3,
                new String[]{"SID"},
                new Object[]{studentId}
        );
    }

    public String getParentMsg3Text(Long studentId) {
        String key = genParentMsg3Key(studentId);
        Long index = storageCache.incr(key, 1, 0, calcExpire());
        if (index >= parentMsg3.size()) {
            return "";
        }

        return parentMsg3.get(SafeConverter.toInt(index));
    }

    private String genParentMsg4Key(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(
                WarmHeartPlanConstant.WARM_HEART_MSG_INDEX_PARENT_4,
                new String[]{"PID"},
                new Object[]{studentId}
        );
    }

    public String getParentMsg4Text(Long parent) {
        String key = genParentMsg4Key(parent);
        Long index = storageCache.incr(key, 1, 0, calcExpire());
        if (index >= parentMsg4.size()) {
            return "";
        }

        return parentMsg4.get(SafeConverter.toInt(index));
    }

    private int calcExpire() {
        long expire = WarmHeartPlanConstant.WARM_HEART_PLAN_END_TIME.getTime() - new Date().getTime();
        return (int) (expire / 1000);
    }

}
