package com.voxlearning.utopia.service.ai.impl.service.processor.order;

import com.voxlearning.utopia.service.ai.impl.context.ChipsOrderPostContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        COPH_LoadOrderInfo.class,
        COPH_ProcessGroupShop.class,
        COPH_ProcessBuy.class,
        COPH_NotifyUser.class,
        COPH_Invite.class
})
@Named
public class ChipsOrderPostHandleProcessor extends AbstractAIProcessor<ChipsOrderPostContext> {
}
