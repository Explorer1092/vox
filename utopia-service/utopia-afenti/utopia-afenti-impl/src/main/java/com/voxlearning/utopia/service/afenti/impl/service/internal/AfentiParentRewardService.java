package com.voxlearning.utopia.service.afenti.impl.service.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.base.cache.managers.AfentiReviewParrentRewardCacheManager;
import com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardType;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by Summer on 2017/6/21.
 */
@Named
public class AfentiParentRewardService extends UtopiaAfentiSpringBean {
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject
    private AfentiQueueProducer afentiQueueProducer;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    private static long time_end_8 = 57600000L;
    private static long time_end_16 = 28800000L;

    private static Map<String, String> reviewParentRewardMap = new HashMap<>();
    static {
        reviewParentRewardMap.put(Subject.CHINESE.name(), "CHINESE_MISSION_COMPLETE");
        reviewParentRewardMap.put(Subject.MATH.name(), "MATH_MISSION_COMPLETE");
        reviewParentRewardMap.put(Subject.ENGLISH.name(), "ENGLISH_MISSION_COMPLETE");
    }

    public void sendParentRewardForPass(CastleResultContext context) {
        if (context.getStudent() == null || context.getSubject() == null) {
            logger.error("AfentiParentRewardService sendReward param error");
            return;
        }

        // 获取奖励类型
        ParentRewardType rewardType = null;
        String productType = "";
        switch (context.getSubject()) {
            case ENGLISH:
                rewardType = ParentRewardType.AFENTI_ENGLISH_PASS_DAY;
                productType = OrderProductServiceType.AfentiExam.name();
                break;
            case MATH:
                rewardType = ParentRewardType.AFENTI_MATH_PASS_DAY;
                productType = OrderProductServiceType.AfentiMath.name();
                break;
            case CHINESE:
                rewardType = ParentRewardType.AFENTI_CHINESE_PASS_DAY;
                productType = OrderProductServiceType.AfentiChinese.name();
                break;
            default:
                break;
        }
        if (rewardType == null) {
            logger.error("AfentiParentRewardService sendReward rewardType is null, Subject:{}", context.getSubject());
            return;
        }
        // 今天是否完成
        boolean rewarded = asyncAfentiCacheService.AfentiParentRewardCacheManager_existRecord(context.getStudent().getId(), rewardType).take();
        if (!rewarded) {
            Map<String, Object> ext = new HashMap<>();
            if (StringUtils.isNotEmpty(productType)) {
                ext.put("productType", productType);
            }
            // 发送奖励
            parentRewardService.generateParentReward(context.getStudent().getId(), rewardType.name(), ext);
            // 记录今天发送行为
            asyncAfentiCacheService.AfentiParentRewardCacheManager_addRecord(context.getStudent().getId(), rewardType);
        }
    }

    public int sendReveiewParentReward(StudentDetail student, String unitId, Subject subject) {
        int number = 0;
        AfentiReviewParrentRewardCacheManager manager = asyncAfentiCacheService.getAfentiReviewParrentRewardCacheManager();
        if (!manager.containRecord(student.getId(), unitId)) {
            // 发送奖励
            List<StudentParentRef> parentRefs = studentLoaderClient.loadStudentParentRefs(student.getId());
            if (CollectionUtils.isNotEmpty(parentRefs)) {
                parentRewardService.generateParentReward(student.getId(), reviewParentRewardMap.get(subject.name()), null);

                StudentParentRef parentRef = parentRefs.stream().findFirst().orElse(null);
                parentRewardService.sendParentReward(parentRef.getParentId(), student.getId(), Arrays.asList(reviewParentRewardMap.get(subject.name())));
            }

            Date now = new Date();
            Date end = DateUtils.getTodayEnd();
            number = 1;
            if (end.getTime() - now.getTime() >= time_end_16 && end.getTime() - now.getTime() <= time_end_8) {
                number = 2;
            }

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("T", AfentiQueueMessageType.FAMILY_JOIN);
            message.put("S", student.getId());
            message.put("N", number);
            afentiQueueProducer.getProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));

            manager.addRecord(student.getId(), unitId);
        }
        return number;
    }

}
