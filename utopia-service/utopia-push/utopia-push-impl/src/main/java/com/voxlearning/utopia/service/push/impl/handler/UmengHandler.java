package com.voxlearning.utopia.service.push.impl.handler;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.push.impl.invoker.UmengInvoker;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.push.impl.builder.UmengAndroidInvokeMessageBuilder;
import com.voxlearning.utopia.service.push.impl.builder.UmengIOSInvokeMessageBuilder;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 10/11/2016
 */
@Named
public class UmengHandler extends SpringContainerSupport {
    @Inject
    private UmengInvoker umengInvoker;

    public void handle(PushContext context) {
        umengInvoker.invoke(context, UmengAndroidInvokeMessageBuilder.instance(context));
        umengInvoker.invoke(context, UmengIOSInvokeMessageBuilder.instance(context));
    }
}
