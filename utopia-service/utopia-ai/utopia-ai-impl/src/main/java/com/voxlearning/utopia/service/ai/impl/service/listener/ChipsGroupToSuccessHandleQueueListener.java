package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.entity.ChipsGroupShopping;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsGroupShoppingPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserOrderExtDao;
import com.voxlearning.utopia.service.ai.internal.ChipsMessageService;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.group.to.success.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.group.to.success.queue")
        },
        maxPermits = 4
)
public class ChipsGroupToSuccessHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static String ORDER_TEMP = "temp_";

    @Inject
    private ChipsGroupShoppingPersistence chipsGroupShoppingPersistence;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private ChipsMessageService chipsMessageService;

    @Inject
    private ChipsUserOrderExtDao chipsUserOrderExtDao;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("course begin notify handle share queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsGroupToSuccessHandleQueueListener error. message:{}", body);
                return;
            }
            String code = SafeConverter.toString(param.get("C"));

            int number = SafeConverter.toInt(param.get("N"));
            boolean res = true;
            if (number == 0) {
                res = chipsGroupShoppingPersistence.updateNumber(code, 1, 2);
            }
            if (res) {
                ChipsGroupShopping chipsGroupShopping = chipsGroupShoppingPersistence.loadByCode(code);
                if (chipsGroupShopping.getOrderId().contains(ORDER_TEMP)) {
                    return;
                }
                UserOrder userOrder = new UserOrder();
                userOrder.setUserId(chipsGroupShopping.getSponsor());
                userOrder.setId(chipsGroupShopping.getOrderId());
                userOrder = userOrderLoaderClient.loadUserOrder(userOrder.genUserOrderId());

                chipsUserService.processOrder(chipsUserOrderExtDao.load(userOrder.genUserOrderId()), userOrder);

                //提醒
                chipsMessageService.notifyUserGroupShoppingSuccess(userOrder);
            }
        }
    }
}
