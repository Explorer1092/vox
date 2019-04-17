package com.voxlearning.utopia.service.wechat.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaq;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.06.26")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WechatFaqService {

    @Async
    AlpsFuture<List<WechatFaq>> loadAllWechatFaqs();
}
