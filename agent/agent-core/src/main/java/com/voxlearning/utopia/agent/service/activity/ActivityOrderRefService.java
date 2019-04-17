package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderRefDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderRef;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;

@Named
public class ActivityOrderRefService {

    @Inject
    private ActivityOrderRefDao activityOrderRefDao;
    @Inject
    private AgentActivityDao agentActivityDao;
    @Inject
    private ActivityOrderService orderService;

    public void handleListenerData(String activityId, String orderId, Date orderPayTime, Long orderUserId, Integer layer, Long userId){
        if(StringUtils.isBlank(activityId) || StringUtils.isBlank(orderId) || userId == null){
            return;
        }
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return;
        }

        if(activityOrderRefDao.loadByOid(orderId) != null){
            return;
        }

        ActivityOrderRef orderRef = new ActivityOrderRef();
        orderRef.setActivityId(activityId);
        orderRef.setOrderId(orderId);
        orderRef.setOrderPayTime(orderPayTime == null? new Date() : orderPayTime);
        orderRef.setOrderUserId(orderUserId);
        orderRef.setLayer(layer);
        orderRef.setUserId(userId);

        activityOrderRefDao.insert(orderRef);

        AlpsThreadPool.getInstance().submit(() -> orderService.handleListenerData(activityId, orderId, orderRef.getOrderPayTime(), new BigDecimal(0), orderUserId, userId));
    }
}
