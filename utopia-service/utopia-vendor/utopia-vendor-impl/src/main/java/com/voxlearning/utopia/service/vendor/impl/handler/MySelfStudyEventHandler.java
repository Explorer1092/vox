package com.voxlearning.utopia.service.vendor.impl.handler;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.api.constant.MySelfStudyActionType;

/**
 * The handler of {@link MySelfStudyActionEvent}
 *
 * @author jiangpeng
 * @since 2016-10-20 上午11:55
 **/
public interface MySelfStudyEventHandler {

    MySelfStudyActionType getMySelfStudyActionType();

    void handle(MySelfStudyActionEvent event);


    MySelfStudyEventHandler NOP = new MySelfStudyEventHandler() {
        @Override
        public MySelfStudyActionType getMySelfStudyActionType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(MySelfStudyActionEvent event) {
            throw new IllegalStateException("Illegal event type,"+ JsonUtils.toJson(event));
        }
    };
}
