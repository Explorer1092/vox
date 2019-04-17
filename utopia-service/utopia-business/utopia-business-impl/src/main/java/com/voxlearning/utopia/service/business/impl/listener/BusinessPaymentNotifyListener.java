package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.service.business.impl.service.BusinessUserOrderServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xin.xin
 * @since 2018/6/14
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.business.payment.notify.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.business.payment.notify.queue"
                )
        },
        maxPermits = 64
)
public class BusinessPaymentNotifyListener extends SpringContainerSupport implements MessageListener {
    @Inject
    private BusinessUserOrderServiceImpl businessUserOrderService;

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (RuntimeModeLoader.getInstance().current().le(Mode.STAGING)) {
            logger.info("payment notify listener:{}", JsonUtils.toJson(body));
        }

        if (!(body instanceof PaymentCallbackContext)) {
            logger.error("Unsupported message type:{}", JsonUtils.toJson(body));
            return;
        }

        PaymentCallbackContext context = (PaymentCallbackContext) body;

        try {
            AtomicLockManager.getInstance().wrapAtomic(businessUserOrderService)
                    .keys(context.getTradeNumber(), context.getExternalTradeNumber())
                    .proxy()
                    .processUserOrderPayment(context);
        } catch (CannotAcquireLockException ignore) {

        } catch (Exception ex) {
            logger.error("userOrder:" + context.getVerifiedPaymentData() + ":" + context.getCallbackAction() + " proccess failed," + JsonUtils.toJson(context));
        }
    }
}
