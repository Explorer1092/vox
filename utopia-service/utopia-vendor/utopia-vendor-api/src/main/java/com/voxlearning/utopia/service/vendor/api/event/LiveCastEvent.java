package com.voxlearning.utopia.service.vendor.api.event;

/**
 * @author jiangpeng
 * @since 2017-11-28 下午3:09
 **/
public interface LiveCastEvent<T> {

    T getEventPayLoad();
    LiveCastEventType getEventType();
}
