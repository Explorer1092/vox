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

package com.voxlearning.utopia.service.zone.api.mapper;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用于显示我收到的礼物
 *
 * @author RuiBao
 * @version 0.1
 * @serial
 * @since 13-9-4
 */
@Getter
@Setter
public class GiftMapper implements Serializable {
    private static final long serialVersionUID = 5119258001755952152L;

    private Long giftHistoryId;
    private Long senderId;
    private String senderName;
    private String receiverName;
    private String date;
    private String postscript;
    private String giftImgUrl;
    private String latestReply;
    private Boolean isThanks;

    /**
     * 这个数据结构被重用来获取用户最新收到的3件礼物，其中只有img和ps字段有用。
     * 键值img和ps是程序中遗留的，前段ftl使用这两个名字。
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("img", StringUtils.defaultString(giftImgUrl));
        map.put("ps", StringUtils.defaultString(postscript));
        map.put("senderName", senderName);
        map.put("senderId", senderId);
        map.put("isThanks", isThanks);
        map.put("historyId", giftHistoryId);
        return map;
    }
}
