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

package com.voxlearning.utopia.service.nekketsu.parkour.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRangeable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Parkour play log data structure.
 *
 * @author Sadi Wan
 * @author Xiaohai Zhang
 * @serial
 * @since Aug 18, 2014
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-nekketsu-parkour-logs-{}", dynamic = true)
@DocumentCollection(collection = "vox_nekketsu_parkour_play_log")
@DocumentRangeable(range = DateRangeType.M)
public class ParkourPlayLog implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = 1709357699306415256L;

    @DocumentId private String id;
    private Long playerId;
    private Long opponentId;
    private Integer stageId;
    private Boolean win;
    private Integer timeCost;
    private Double correctRate;
    private List<KeyValuePair<String, Boolean>> answerCorrectList;
    /**
     * 游戏类型 "AI":通关,"RANDOM":随机挑战,"CLASSMATE":挑战好友
     */
    private String playType;
    private Integer timePerQuestion;
    private Date playDateTime;
    private Integer star;
    /**
     * 可记录是否获得了新的碎片 是否获得了新的星星
     */
    private Map<String, String> additionalInfo;

    @Override
    public void touchCreateTime(long timestamp) {
        if (playDateTime == null) {
            playDateTime = new Date(timestamp);
        }
    }

    @JsonIgnore
    public boolean isWinTrue() {
        return Boolean.TRUE.equals(win);
    }
}
