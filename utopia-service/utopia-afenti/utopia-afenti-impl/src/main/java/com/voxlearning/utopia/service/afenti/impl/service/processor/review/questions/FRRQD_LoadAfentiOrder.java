package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewQuestionsContext;
import com.voxlearning.utopia.service.afenti.impl.dao.UserActivatedProductPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;


/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class FRRQD_LoadAfentiOrder extends SpringContainerSupport implements IAfentiTask<FetchReviewQuestionsContext> {

    @Inject
    private UserActivatedProductPersistence userActivatedProductPersistence;

    @Override
    public void execute(FetchReviewQuestionsContext context) {
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());
        if (type == null) {
            logger.error("FRRQD_LoadAfentiOrder orderProductType error");
            context.setErrorCode(AfentiErrorType.DEFAULT.getCode());
            context.errorResponse(AfentiErrorType.DEFAULT.getInfo());
            return;
        }

        UserActivatedProduct history = userActivatedProductPersistence
                .loadByUserIds(Collections.singleton(context.getStudent().getId()))
                .getOrDefault(context.getStudent().getId(), Collections.emptyList())
                .stream()
                .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == type)
                .findFirst()
                .orElse(null);
        boolean isPaid = history != null && history.getServiceEndTime().getTime() > System.currentTimeMillis();

        if (!isPaid) {
            context.setErrorCode(AfentiErrorType.NO_PAID_OR_OUT_OF_DATE.getCode());
            context.errorResponse(AfentiErrorType.NO_PAID_OR_OUT_OF_DATE.getInfo());
        }
    }
}
