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

package com.voxlearning.utopia.service.nekketsu.parkour.net.types;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


/**
 * 游戏初始化，包括角色信息，物品信息
 */
public class ParkourRoleInfo extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * ${var.@comment}
     */
    public String roleId = "";
    /**
     * ${var.@comment}
     */
    public String roleName = "";
    /**
     * ${var.@comment}
     */
    public String img = "";
    /**
     * ${var.@comment}
     */
    public int exp;
    /**
     * ${var.@comment}
     */
    public int level;
    /**
     * ${var.@comment}
     */
    public int coinCount;
    /**
     * ${var.@comment}
     */
    public int openStage;
    /**
     * ${var.@comment}
     */
    public int passedStage;
    /**
     * ${var.@comment}
     */
    public int vitality;
    /**
     * 下次活力增长倒计时
     */
    public int vitalityRefillCountDown;
    /**
     * 用户未付费时获得的金币
     */
    public int cointToExchange;
    /**
     * 用户在未付费时获得的单词，用户绘制拼图时使用
     */
    public Collection<WordToExchange> wordToExchange = new ArrayList<>();


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}