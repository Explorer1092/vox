package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 获取阿分题订单信息
 *
 * @author Ruib
 * @since 2016/7/12
 */
@Named
public class L_LoadAfentiOrder extends SpringContainerSupport implements IAfentiTask<LoginContext> {
    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public void execute(LoginContext context) {
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());
        if (type == null) {
            logger.error("L_LoadAfentiOrder Subject {} not available", context.getSubject());
            context.errorResponse();
            return;
        }

        AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(type.name(), context.getStudent().getId());
        if (payMapper == null) {
            context.getResult().put("paidBefore", false); // 未付费
            context.getResult().put("authorized", false); // 当前是否有有效期范围内的订单
            context.getResult().put("daysToExpire", 0);   // 还有多少天过期
        } else {
            context.getResult().put("paidBefore", payMapper.hasPaid()); // 是否付过费
            context.getResult().put("authorized", payMapper.isActive()); // 当前是否有有效期范围内的订单
            // 向上取整 天数+1 我们的订单有效期都是截止到最后一天的23:59:59
            context.getResult().put("daysToExpire", payMapper.getDayToExpire() == null ? 0 : SafeConverter.toInt(payMapper.getDayToExpire()) + 1); // 还有多少天过期
        }

        // 判断用户是否开通阿分题视频增强服务
        OrderProductServiceType videoType = AfentiUtils.getAfentiVideoServiceType(context.getSubject());
        AppPayMapper videoPayMapper = userOrderLoaderClient.getUserAppPaidStatus(videoType.name(), context.getStudent().getId());
        if (videoPayMapper != null && videoPayMapper.getAppStatus() != null && videoPayMapper.getAppStatus() == 2) {
            context.getResult().put("hasOpenedVideoPlus", true); // 已付费并在有效期
        } else {
            context.getResult().put("hasOpenedVideoPlus", false);
        }
    }
}
