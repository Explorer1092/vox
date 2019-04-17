package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.impl.dao.UserActivatedProductPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FRQD_LoadAfentiOrder extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {
    @Inject private UserActivatedProductPersistence userActivatedProductPersistence;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());
        if (type == null) {
            logger.error("FRQD_LoadAfentiOrder orderProductType error");
            context.setErrorCode(DEFAULT.getCode());
            context.errorResponse(DEFAULT.getInfo());
            return;
        }

        UserActivatedProduct history = userActivatedProductPersistence
                .loadByUserIds(Collections.singleton(context.getStudent().getId()))
                .getOrDefault(context.getStudent().getId(), Collections.emptyList())
                .stream()
                .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == type)
                .findFirst()
                .orElse(null);

        context.setPaid(null != history && history.getServiceEndTime().getTime() > System.currentTimeMillis());
    }
}
