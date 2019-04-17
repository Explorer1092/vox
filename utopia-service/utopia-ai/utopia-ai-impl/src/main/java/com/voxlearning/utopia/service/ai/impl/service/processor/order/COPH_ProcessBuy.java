package com.voxlearning.utopia.service.ai.impl.service.processor.order;

import com.voxlearning.utopia.service.ai.entity.ChipsUserOrderExt;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsOrderPostContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserOrderExtDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Named
public class COPH_ProcessBuy extends AbstractAiSupport implements IAITask<ChipsOrderPostContext> {

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private ChipsUserOrderExtDao chipsUserOrderExtDao;

    @Override
    public void execute(ChipsOrderPostContext context) {
        ChipsUserOrderExt chipsUserOrderExt = context.getOrderExt();
        chipsUserService.processOrder(chipsUserOrderExt, context.getUserOrder());

        if (chipsUserOrderExt != null) {
            chipsUserOrderExt.setStatus(ChipsUserOrderExt.OrderStatus.PAYED);
            chipsUserOrderExt.setUpdateDate(new Date());
            chipsUserOrderExtDao.upsert(chipsUserOrderExt);
        }
    }
}
