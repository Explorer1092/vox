package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.TobbitMathHistory;

import java.util.List;
import java.util.concurrent.TimeUnit;


@ServiceVersion(version = "20190218")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface TobbitMathService extends IPingable {

    MapMessage identify(String openId, Long uid, byte[] bytes, String sys);

    MapMessage atBot(Long uid, List<String> latex);

    MapMessage load(String id);

    MapMessage clean(String openId,Long uid);

    MapMessage loadByUid(String openId, Long uid);

    MapMessage loadUserInfo(String openId,Long uid);

    MapMessage share(String openId, Long uid, String qid);

    boolean isNewUser(String openId);

    void appendAuthUser(String openId, String name, String avatar);

    boolean hasUser(Long uid);

    // 渠道邀请用户注册(蜂巢创建订单...)
    boolean spreadRegUser(String openId, Long uid,boolean outOfSystem);

    boolean markSpUser(String openId, String sp);
}
