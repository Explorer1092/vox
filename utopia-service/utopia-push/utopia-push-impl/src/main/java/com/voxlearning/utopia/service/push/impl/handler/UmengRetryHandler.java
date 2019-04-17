package com.voxlearning.utopia.service.push.impl.handler;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.push.impl.invoker.UmengInvoker;
import com.voxlearning.utopia.service.push.impl.builder.UmengAndroidInvokeMessageBuilder;
import com.voxlearning.utopia.service.push.impl.builder.UmengIOSInvokeMessageBuilder;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.push.api.support.PushRetryContext;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 15/11/2016.
 */
@Named
public class UmengRetryHandler extends SpringContainerSupport {
    @Inject
    private UmengInvoker umengInvoker;

    public void handle(PushRetryContext context) {
        if (context.getPushType() == PushType.UMENG_ANDRIOD) {
            umengInvoker.invoke(context, UmengAndroidInvokeMessageBuilder.instance(context));
        } else {
            umengInvoker.invoke(context, UmengIOSInvokeMessageBuilder.instance(context));
        }
    }
}
