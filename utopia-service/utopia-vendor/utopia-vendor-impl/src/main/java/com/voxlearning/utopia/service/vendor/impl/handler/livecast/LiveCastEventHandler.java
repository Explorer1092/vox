package com.voxlearning.utopia.service.vendor.impl.handler.livecast;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEvent;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEventType;

/**
 * @author jiangpeng
 * @since 2017-11-28 下午3:45
 **/
public interface LiveCastEventHandler {

    LiveCastEventType getLiveCastEventType();

    void handle(LiveCastEvent event);


    LiveCastEventHandler NOP = new LiveCastEventHandler() {
        @Override
        public LiveCastEventType getLiveCastEventType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(LiveCastEvent event) {
            throw new IllegalStateException("Illegal event type,"+ JsonUtils.toJson(event));
        }
    };
}
