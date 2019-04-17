package com.voxlearning.utopia.service.ai.impl.service.processor.order;

import com.voxlearning.utopia.service.ai.cache.manager.ChipsProductUserCountCacheManager;
import com.voxlearning.utopia.service.ai.cache.manager.UserProductBuyCacheManager;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsOrderPostContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserOrderExtDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class COPH_LoadOrderInfo extends AbstractAiSupport implements IAITask<ChipsOrderPostContext> {
    @Inject
    private ChipsUserOrderExtDao chipsUserOrderExtDao;

    @Inject
    private UserProductBuyCacheManager userProductBuyCacheManager;

    @Inject
    private ChipsProductUserCountCacheManager chipsProductUserCountCacheManager;

    @Override
    public void execute(ChipsOrderPostContext context) {
        //发送购买提醒邮件
        chipsMessageService.notifyUserBuyInfoEmail(context.getParam());

        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(context.getOrderId());
        if (userOrder == null) {
            context.terminateTask();
            logger.error("order is not exists, oid {}", context.getOrderId());
            return;
        }

        context.setUserOrder(userOrder);
        context.setOrderExt(chipsUserOrderExtDao.load(context.getOrderId()));

        processOfficialBuy(userOrder.getProductId(), context.getUserId());
    }

    private void processOfficialBuy(String orderProductId, Long userId) {
        chipsProductUserCountCacheManager.increase(orderProductId);
        userProductBuyCacheManager.addRecord(userId, orderProductId);
    }
}
