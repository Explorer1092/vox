package com.voxlearning.utopia.service.crm.impl.support.apppush;

import com.voxlearning.utopia.service.crm.impl.support.apppush.publisher.AppPushPublisher;

public interface AppPushProcessor {

    AppPushPublisher getPublisher(int pushType);

}
