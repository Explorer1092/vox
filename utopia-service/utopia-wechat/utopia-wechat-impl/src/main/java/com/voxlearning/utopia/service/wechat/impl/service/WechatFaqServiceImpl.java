package com.voxlearning.utopia.service.wechat.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.wechat.api.WechatFaqService;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaq;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatFaqPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.wechat.impl.service.WechatFaqServiceImpl")
@ExposeService(interfaceClass = WechatFaqService.class)
public class WechatFaqServiceImpl extends SpringContainerSupport implements WechatFaqService {

    @Inject private WechatFaqPersistence wechatFaqPersistence;

    @Override
    public AlpsFuture<List<WechatFaq>> loadAllWechatFaqs() {
        return new ValueWrapperFuture<>(wechatFaqPersistence.query());
    }
}
