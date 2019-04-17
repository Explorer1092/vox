package com.voxlearning.utopia.agent.service.honeycomb;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombOrder;
import com.voxlearning.utopia.agent.persist.honeycomb.HoneycombOrderDao;
import com.voxlearning.utopia.core.utils.MQUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Named
public class HoneycombOrderService {

    @Inject
    private HoneycombOrderDao orderDao;

    public void handleMessageData(String orderId, String productId, Date payTime, Long honeycombId){
        if(StringUtils.isBlank(orderId) || StringUtils.isBlank(productId) || honeycombId == null || honeycombId < 1){
            return;
        }

        HoneycombOrder dbOrder = orderDao.loadByOid(orderId);
        if(dbOrder != null){
            return;
        }

        HoneycombOrder order = new HoneycombOrder();
        order.setActivityId(productId);
        order.setOrderId(orderId);
        order.setPayTime(payTime == null ? new Date() : payTime);
        order.setHoneycombId(honeycombId);
        orderDao.insert(order);

        // 订单统计
        MQUtils.send(AgentConstants.AGENT_INNER_TOPIC, MapUtils.m("type", "HoneycombOrder", "data", JsonUtils.toJson(order)));
    }

}
