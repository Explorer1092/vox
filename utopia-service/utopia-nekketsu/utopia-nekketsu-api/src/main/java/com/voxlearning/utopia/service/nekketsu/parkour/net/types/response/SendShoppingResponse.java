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

import java.io.Serializable;


/**
 * ${@comment}
 */
public class SendShoppingResponse extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * ILLEGAL_ITEM(不存在的商品),NOT_ENOUGH_COIN(金币不足),NOT_ENOUGH_PK_BAG_SPACE(PK背包没地方了)
     */
    public String failReason = "";
    /**
     * 新的总活力值
     */
    public int newVitality;
    /**
     * 增加的活力值
     */
    public int vitalityAdd;
    /**
     * 花费的金币数量
     */
    public int coinCost;
    /**
     * 消费后的金币总数
     */
    public int coinTotal;


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}