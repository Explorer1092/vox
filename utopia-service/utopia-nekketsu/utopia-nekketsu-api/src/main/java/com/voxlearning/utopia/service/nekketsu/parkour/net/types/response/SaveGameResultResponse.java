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
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.ParkourPuzzle;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.WordToExchange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


/**
 * 游戏结束，存储结果，返回奖励
 */
public class SaveGameResultResponse extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * 获得的拼图
     */
    public ParkourPuzzle achievedPuzzle;
    /**
     * 本关星星数。如果比原有数据大，代表获取了新的星星
     */
    public int newStarCount;
    /**
     * 如果第一次获得了三星成绩则此处大于0，表示奖励金币数
     */
    public int coinBonus;
    /**
     * 金币总数。刷新用
     */
    public int coinTotal;
    /**
     * 如果本次游戏赢取拼图导致某个单词拼图集齐，则奖励学豆，此字段为奖励学豆数量
     */
    public int integralBonus;
    /**
     * ${var.@comment}
     */
    public int newLevel;
    /**
     * ${var.@comment}
     */
    public int newExp;
    /**
     * ${var.@comment}
     */
    public int expBonus;
    /**
     * ${var.@comment}
     */
    public int newOpenStage;
    /**
     * 用户在未付费时获得的单词，用户绘制拼图时使用
     */
    public Collection<WordToExchange> wordToExchange = new ArrayList<>();
    /**
     * server端新生成的key，用于下次save消息时加密使用
     */
    public String newSecureKey = "";


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}