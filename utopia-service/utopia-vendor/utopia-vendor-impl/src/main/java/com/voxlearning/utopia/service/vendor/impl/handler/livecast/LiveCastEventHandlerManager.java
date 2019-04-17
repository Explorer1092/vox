package com.voxlearning.utopia.service.vendor.impl.handler.livecast;

import com.voxlearning.utopia.service.vendor.api.event.LiveCastEvent;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEventType;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;

import javax.inject.Named;
import java.util.EnumMap;

/**
 * @author jiangpeng
 * @since 2017-11-28 下午3:33
 **/
@Named
public class LiveCastEventHandlerManager extends ApplicationObjectSupport implements InitializingBean {


    private final EnumMap<LiveCastEventType, LiveCastEventHandler> handlers = new EnumMap<>(LiveCastEventType.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), LiveCastEventHandler.class, false, true)
                .values().forEach(e -> handlers.put(e.getLiveCastEventType(), e));
    }


    public void dealEvent(LiveCastEvent liveCastEvent){
        LiveCastEventType eventType = liveCastEvent.getEventType();
        LiveCastEventHandler liveCastEventHandler = handlers.get(eventType);
        if (liveCastEventHandler == null || liveCastEvent == LiveCastEventHandler.NOP)
            return;
        liveCastEventHandler.handle(liveCastEvent);
    }

}
