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

package com.voxlearning.utopia.service.nekketsu.parkour.net.types.response;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.LoginRewardMonth;

import java.io.Serializable;


/**
 * ${@comment}
 */
public class ExchangeLoginRewardResponse extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * NOT_PAID:非付费用户,NO_PK_ROLE:没有PK角色,ALREADY_EXCHANGED:已经兑换了,ILLEGAL_PRIZE:非法奖品,EXCHANGE_FAIL:奖品已经扣了，但道具没加上，请联系客服
     */
    public String failReason = "";
    /**
     * 刷新后的当月奖励
     */
    public LoginRewardMonth rewardMonth;


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}