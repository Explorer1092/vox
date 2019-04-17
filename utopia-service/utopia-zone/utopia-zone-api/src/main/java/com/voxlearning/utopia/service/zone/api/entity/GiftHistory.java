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

package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.zone.api.constant.GiftHistoryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FIXME:这张表中的gold字段和silver字段可能为null，0表示赠送了免费礼物。
 * FIXME:注意用晓光写的对象更新的方式，不能把一个非null字段更新成null。
 * FIXME:postscript字段不能为空，用""表示没用发送附言。
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @serial
 * @since 13-9-2
 */
@Getter
@Setter
@DocumentTable(table = "VOX_GIFT_HISTORY")
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20150611")
public class GiftHistory extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 2233805548297930165L;

    @UtopiaSqlColumn private Long senderId;
    @UtopiaSqlColumn private Long receiverId;
    @UtopiaSqlColumn private Long giftId;
    @UtopiaSqlColumn private Integer gold;
    @UtopiaSqlColumn private Integer silver;
    @UtopiaSqlColumn private String postscript;
    @UtopiaSqlColumn private GiftHistoryType giftHistoryType;
    @UtopiaSqlColumn private String latestReply;
    @UtopiaSqlColumn private Boolean isThanks; // 是否答谢

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(GiftHistory.class, id);
    }

    public static String ck_senderId(Long senderId) {
        return CacheKeyGenerator.generateCacheKey(GiftHistory.class, "S", senderId);
    }

    public static String ck_receiverId(Long receiverId) {
        return CacheKeyGenerator.generateCacheKey(GiftHistory.class, "R", receiverId);
    }

    public GiftHistory withSenderId(Long senderId) {
        this.senderId = senderId;
        return this;
    }

    public GiftHistory withReceiverId(Long receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static GiftHistory mockInstance() {
        GiftHistory inst = new GiftHistory();
        inst.senderId = 0L;
        inst.receiverId = 0L;
        inst.giftId = 0L;
        inst.postscript = "";
        inst.giftHistoryType = GiftHistoryType.STUDENT_TO_STUDENT;
        return inst;
    }

    public static GiftHistory of(Long senderId,
                                 Long receiverId,
                                 Long giftId,
                                 Integer gold,
                                 Integer silver,
                                 String postscript,
                                 GiftHistoryType type) {
        GiftHistory history = new GiftHistory();
        history.setSenderId(senderId);
        history.setReceiverId(receiverId);
        history.setGiftId(giftId);
        history.setGold(gold);
        history.setSilver(silver);
        history.setPostscript(StringUtils.defaultString(postscript));
        history.setGiftHistoryType(type);
        return history;
    }

}

