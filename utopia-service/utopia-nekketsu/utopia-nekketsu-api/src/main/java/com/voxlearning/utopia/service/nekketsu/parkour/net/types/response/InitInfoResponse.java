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
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.CoinShopItem;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.LevelSpeed;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.ParkourRoleInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


/**
 * 游戏初始化，包括角色信息，物品信息
 */
public class InitInfoResponse extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * 服务器时间
     */
    public String serverTime = "";
    /**
     * 学生所在区域是否开通了该款产品，如果为false，则不可开始游戏
     */
    public boolean payOpen;
    /**
     * 该学生是否已付费，如果为false，则只可以玩前7关
     */
    public boolean paidUser;
    /**  */
    public ParkourRoleInfo roleInfo;
    /**
     * 关卡总数。每14关一个地图。关卡总数40.
     */
    public int stageCount;
    /**
     * 等级对应平跑速度配置信息
     */
    public Collection<LevelSpeed> levelSpeedConf = new ArrayList<>();
    /**
     * 用于save类消息的加密
     */
    public String secureKey = "";
    /**
     * 金币商店商品列表
     */
    public Collection<CoinShopItem> shopItemList = new ArrayList<>();
    /**
     * true时，表明此次登录领取了当天登录奖励，需要弹出奖励列表
     */
    public boolean showPrizeList;
    /**
     * true时，需显示做错题按钮
     */
    public boolean showSpButton;


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}