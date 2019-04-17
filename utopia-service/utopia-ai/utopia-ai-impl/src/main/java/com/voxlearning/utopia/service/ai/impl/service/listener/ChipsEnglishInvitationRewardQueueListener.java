package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipEnglishInvitationPersistence;
import com.voxlearning.utopia.service.ai.support.ChipsInvitationHelper;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.english.invitation.reward.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.english.invitation.reward.queue")
        },
        maxPermits = 4
)
public class ChipsEnglishInvitationRewardQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipEnglishInvitationPersistence chipEnglishInvitationPersistence;

    @Inject
    private CouponServiceClient couponServiceClient;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("ChipsEnglishInvitationRewardQueueListener queue no message");
            return;
        }
        Object body = message.decodeBody();
        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (MapUtils.isEmpty(param)) {
                logger.error("ChipsEnglishInvitationRewardQueueListener error. message:{}", body);
                return;
            }
            Long userId = SafeConverter.toLong(param.get("U"));
            int num = SafeConverter.toInt(param.get("N"));
            String productId = SafeConverter.toString(param.get("P"));
            if (userId == null || userId <= 0L || StringUtils.isBlank(productId)) {
                logger.error("ChipsEnglishInvitationRewardQueueListener error. message:{}", body);
                return;
            }

            if (num <= 0) {
                return;
            }
            long res = chipEnglishInvitationPersistence.updateToSendByProduct(userId, productId);

            if (res > 0) {
                String couponId = ChipsInvitationHelper.getCouponId();
                for(int i = 0; i < res; i ++ ) {
                    try {
                        couponServiceClient.sendCoupon(couponId, userId);
                    } catch (Exception e) {
                        logger.error("send coupon error. couponId:{}, userId:{}", couponId, userId, e);
                    }
                }
            }

        }
    }
}
