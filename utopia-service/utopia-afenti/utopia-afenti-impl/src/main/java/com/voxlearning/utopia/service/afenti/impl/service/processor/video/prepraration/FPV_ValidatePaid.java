package com.voxlearning.utopia.service.afenti.impl.service.processor.video.prepraration;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.FetchPreparationVideoContext;
import com.voxlearning.utopia.service.afenti.impl.dao.UserActivatedProductPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.PUSH_VIDEO_NOT_ENOUGH_FOR_PREPARATION;

/**
 * @author songtao
 * @since 17/7/20
 */
@Named
public class FPV_ValidatePaid extends SpringContainerSupport implements IAfentiTask<FetchPreparationVideoContext> {
    @Inject private UserActivatedProductPersistence userActivatedProductPersistence;

    @Override
    public void execute(FetchPreparationVideoContext context) {
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());
        if (type == null) {
            logger.error("FPV_ValidatePaid orderProductType error");
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

        if (history == null || history.getServiceEndTime().getTime() <= System.currentTimeMillis()) {
            context.setErrorCode(PUSH_VIDEO_NOT_ENOUGH_FOR_PREPARATION.getCode());
            context.errorResponse(PUSH_VIDEO_NOT_ENOUGH_FOR_PREPARATION.getInfo());
        }
    }

}
