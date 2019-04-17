package com.voxlearning.utopia.service.crm.impl.support.apppush;

import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import lombok.Getter;

/**
 * 简易工作流的上下文环境
 */
public class AppPushMsgContext {

    @Getter private AppPushWfMessage appPushMessage;

    public AppPushMsgContext(AppPushWfMessage message) {
        this.appPushMessage = message;
    }

}
