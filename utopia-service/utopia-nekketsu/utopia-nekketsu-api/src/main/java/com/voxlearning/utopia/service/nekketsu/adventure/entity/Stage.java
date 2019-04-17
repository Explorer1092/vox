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

package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 关卡
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/15 16:30
 */
@Getter
@Setter
public class Stage implements Serializable, Cloneable {
    private static final long serialVersionUID = 7324904423513829750L;

    private Integer order;//关卡顺序（1～60）
    private List<String> words; //关卡单词（7个）
    private Boolean decorativeCrown;//是否装饰皇冠
    private Integer obtainDiamond;//当前关卡获得钻石数
    private Integer totalDiamond;//关卡总钻石数
    private Map<Integer, StageApp> apps;//关卡内小应用（1～3年级4个，4～6年级5个）
    private Boolean receiveFreeBeans; //是否可以免费领取学习豆(限制免费试用前五关)
    private Date receiveBeansTime; //学习豆领取时间
    private Boolean isReceived; // 是否已经领取免费学习豆

    @Override
    public Stage clone() {
        try {
            return (Stage) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }

    // 将apps里面的数据放到stageAppList中，并将apps赋值为null
    @DocumentFieldIgnore
    public List<StageApp> getStageAppList() {
        if (getApps() == null) {
            return new LinkedList<>();
        }
        return new LinkedList<>(getApps().values());
    }

    public static Stage newInstance(Integer order, List<String> words, Map<Integer, StageApp> apps, Integer totalDiamond) {
        Stage stage = new Stage();
        stage.setOrder(order);
        stage.setWords(words);
        stage.setApps(apps);
        stage.setDecorativeCrown(false);
        stage.setObtainDiamond(0);
        stage.setTotalDiamond(totalDiamond);
        stage.setReceiveFreeBeans(false);
        stage.setReceiveBeansTime(null);
        stage.setIsReceived(false);
        return stage;
    }

}
