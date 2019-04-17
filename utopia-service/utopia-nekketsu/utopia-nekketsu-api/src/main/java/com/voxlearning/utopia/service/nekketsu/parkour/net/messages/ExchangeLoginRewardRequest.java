/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.net.messages;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.PrizeDetailLogin;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.ExchangeLoginRewardResponse;

import java.io.Serializable;


/**
 * 兑换登录奖励。将奖品写入背包
 */
public class ExchangeLoginRewardRequest extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 将PrizeDetailLogin所在的LoginRewardMonth中的id字段赋值到此
     */
    public String id;
    /**
     * 将GetLoginRewardListResponse.loginRewardList.PrizeDetailLogin结构原样传回
     */
    public PrizeDetailLogin prizeDetail;


    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static ExchangeLoginRewardRequest parseRequest(String input) {
        ExchangeLoginRewardRequest req = JsonStringDeserializer.getInstance().deserialize(input, ExchangeLoginRewardRequest.class);
        if (null == req) {
            throw new NullPointerException();
        }
        return req;
    }

    /**
     * 生成一个对应的返回类型对象
     *
     * @return 该类型消息对应的返回类型对象
     */
    public static ExchangeLoginRewardResponse newResponse() {
        ExchangeLoginRewardResponse response = new ExchangeLoginRewardResponse();
        return response;
    }
}