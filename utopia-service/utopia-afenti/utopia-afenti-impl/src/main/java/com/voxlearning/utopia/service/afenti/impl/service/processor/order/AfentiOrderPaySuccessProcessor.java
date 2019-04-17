package com.voxlearning.utopia.service.afenti.impl.service.processor.order;

import com.voxlearning.utopia.service.afenti.api.context.OrderPaySuccessContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * 阿分题产品购买成功接收通知提醒
 *
 * @author peng.zhang.a
 * @since 16-7-26
 */
@Named
@AfentiTasks({
        AOPS_NotifyClassmatesPaySuccess.class,
        AOPS_NotifyInviteSuccess.class,
        AOPS_NotifyPurchaseInfo.class,
})
public class AfentiOrderPaySuccessProcessor extends AbstractAfentiProcessor<OrderPaySuccessContext> {

}
