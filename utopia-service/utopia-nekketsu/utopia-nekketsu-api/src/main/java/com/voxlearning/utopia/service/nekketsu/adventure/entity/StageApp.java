/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import com.voxlearning.utopia.service.nekketsu.adventure.constant.AdventureConstants;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.StageAppType;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 关卡内小应用
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/18 16:28
 */
@Data
public class StageApp implements Serializable, Cloneable {
    private static final long serialVersionUID = 7450824835714780753L;

    private Integer appId;
    private StageAppType type;//小应用类型，基础应用、口语应用
    private String fileName;//文件名
    private String name;//游戏名称
    private Integer diamond;//钻石
    private Integer order;  //顺序(1~5)
    private Boolean open;   //小游戏是否开启
    private Boolean obtainReward;//获得奖励
    private String size;//A：700×470， B：900×600

    @Override
    public StageApp clone() {
        try {
            return (StageApp) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }

    public static StageApp newInstance(Integer appId, Integer order, String fileName, String name, StageAppType type, String size) {
        StageApp stageApp = new StageApp();
        stageApp.setAppId(appId);
        stageApp.setType(type);
        stageApp.setFileName(fileName);
        stageApp.setName(name);
        stageApp.setDiamond(0);
        stageApp.setOrder(order);
        stageApp.setOpen(false);
        stageApp.setObtainReward(false);
        stageApp.setSize(size);
        return stageApp;
    }

    public boolean canGrantGift() {
        return (!this.getObtainReward() && Objects.equals(this.getDiamond(), AdventureConstants.STAGEAPP_MAX_DIAMOND));
    }

}
