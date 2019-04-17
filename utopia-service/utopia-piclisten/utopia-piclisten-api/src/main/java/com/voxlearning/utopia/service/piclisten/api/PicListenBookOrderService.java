package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;

import java.util.concurrent.TimeUnit;

/**
 * @author xinxin
 * @since 10/17/17.
 */
@ServiceRetries
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface PicListenBookOrderService {

    /**
     * 处理点读机订单取消时与第三方的同步
     */
    MapMessage notifyThirdPartyCancelOrder(String fixedOrderId);

    MapMessage notifyThirdPartyChangeOrderBook(String fixedOrderId,String newBookId);

    MapMessage synchronizePicListenOrder(UserOrder order);
}
