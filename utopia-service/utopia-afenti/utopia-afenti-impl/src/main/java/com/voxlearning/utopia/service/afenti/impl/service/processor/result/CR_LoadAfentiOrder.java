package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.dao.UserActivatedProductPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author Ruib
 * @since 2016/8/8
 */
@Named
public class CR_LoadAfentiOrder extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @Inject private UserActivatedProductPersistence userActivatedProductPersistence;

    @Override
    public void execute(CastleResultContext context) {
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());
        if (type == null) {
            logger.error("CR_LoadAfentiOrder Subject {} not available", context.getSubject());
            context.errorResponse();
            return;
        }

        List<UserActivatedProduct> orderList = userActivatedProductPersistence
                .loadByUserIds(Collections.singleton(context.getStudent().getId()))
                .getOrDefault(context.getStudent().getId(), Collections.emptyList());

        UserActivatedProduct history = orderList.stream()
                .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == type)
                .findFirst()
                .orElse(null);
        context.setAuthorized(null != history && history.getServiceEndTime().getTime() > System.currentTimeMillis());

        //当前是否含有有效的阿分题产品（只要有任何一科目就算）
        history = orderList.stream()
                .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == AfentiMath || OrderProductServiceType.safeParse(t.getProductServiceType()) == AfentiExam
                        || OrderProductServiceType.safeParse(t.getProductServiceType()) == AfentiChinese)
                .filter(t -> t.getServiceEndTime() != null && t.getServiceEndTime().getTime() > System.currentTimeMillis())
                .findFirst()
                .orElse(null);
        context.setBoughtAfenti(history != null);

        //当前是否有阿分题视频购买列表
        List<UserActivatedProduct> afentiVideoList = orderList.stream()
                .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == AfentiUtils.getAfentiVideoServiceType(context.getSubject()))
                .filter(t -> new Date().before(t.getServiceEndTime()))
                .collect(Collectors.toList());
        context.setAfentiVideoActivatedProducts(afentiVideoList);

    }
}
