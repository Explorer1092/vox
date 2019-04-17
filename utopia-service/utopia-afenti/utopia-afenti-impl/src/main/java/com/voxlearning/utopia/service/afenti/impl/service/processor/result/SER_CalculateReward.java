package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.dao.UserActivatedProductPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class SER_CalculateReward extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private UserActivatedProductPersistence userActivatedProductPersistence;

    @Override
    public void execute(ElfResultContext context) {
        if (context.getIds().size() < 5) {
            context.getResult().put("count", context.getIds().size());
            context.getResult().put("integral", 0);
            context.getResult().put("creditCount", 0);
            context.terminateTask();
        }

        // 判断是否付费
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());

        UserActivatedProduct history = userActivatedProductPersistence
                .loadByUserIds(Collections.singleton(context.getStudent().getId()))
                .getOrDefault(context.getStudent().getId(), Collections.emptyList())
                .stream()
                .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == type)
                .findFirst()
                .orElse(null);

        boolean authorized = null != history && history.getServiceEndTime().getTime() > System.currentTimeMillis();
        context.setIntegral(authorized ? 5 : 1);
        // 赠送自学积分
        context.setCreditCount(authorized ? 5 : 1);
    }
}
