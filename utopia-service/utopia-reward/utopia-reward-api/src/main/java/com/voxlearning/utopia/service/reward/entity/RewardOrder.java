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

package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@DocumentTable(table = "VOX_REWARD_ORDER")
@DocumentConnection(configName = "hs_reward")
@UtopiaCacheExpiration(7200)
@UtopiaCacheRevision("20180911")
public class RewardOrder implements Serializable, TimestampTouchable, TimestampAccessor, PrimaryKeyAccessor<Long> {
    private static final long serialVersionUID = 6684185040584419728L;

    @Getter @Setter @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC) Long id;
    @DocumentCreateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "CREATE_DATETIME") Date createDatetime;
    @DocumentUpdateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "UPDATE_DATETIME") Date updateDatetime;
    @Getter @Setter @UtopiaSqlColumn private Long productId;
    @Getter @Setter @UtopiaSqlColumn private String productName;
    @Getter @Setter @UtopiaSqlColumn private String productType;
    @Getter @Setter @UtopiaSqlColumn private String productCategory;
    @Getter @Setter @UtopiaSqlColumn private Long skuId;
    @Getter @Setter @UtopiaSqlColumn private String skuName;
    @Getter @Setter @UtopiaSqlColumn private Integer quantity;
    @Getter @Setter @UtopiaSqlColumn private Double price;
    @Getter @Setter @UtopiaSqlColumn private Double totalPrice;
    @Getter @Setter @UtopiaSqlColumn private Double discount;
    @Getter @Setter @UtopiaSqlColumn private String unit;
    @Getter @Setter @UtopiaSqlColumn private String code;
    @Getter @Setter @UtopiaSqlColumn private Long clazzId;
    @Getter @Setter @UtopiaSqlColumn private Long buyerId;
    @Getter @Setter @UtopiaSqlColumn private String buyerName;
    @Getter @Setter @UtopiaSqlColumn private Integer buyerType; // 用户类型
    @Getter @Setter @UtopiaSqlColumn private String status;
    @Getter @Setter @UtopiaSqlColumn private String saleGroup;
    @Getter @Setter @UtopiaSqlColumn private Date completedDatetime;
    @Getter @Setter @UtopiaSqlColumn private Long logisticsId;   // 快递单ID
    @Getter @Setter @UtopiaSqlColumn private Boolean disabled;
    @Getter @Setter @UtopiaSqlColumn private String reason;      // 状态变更原因
    @Getter @Setter @UtopiaSqlColumn private Long completeId;      // 对应的发货单ID
    @Getter @Setter @UtopiaSqlColumn private RewardOrder.Source source;      // 下单来源
    @Getter @Setter @UtopiaSqlColumn private String extAttributes;      // 额外属性，json格式
    @Getter @Setter @UtopiaSqlColumn private Integer spendType; //花费类型，默认0是学豆，1是碎片

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(RewardOrder.class, id);
    }

    public static String ck_buyerId(Long buyerId) {
        return CacheKeyGenerator.generateCacheKey(RewardOrder.class, "userId", buyerId);
    }

    public static String ck_logisticsId(Long logisticId) {
        return CacheKeyGenerator.generateCacheKey(RewardOrder.class, "logisticId", logisticId);
    }

    public static String ck_clazzId_categoryCode(Long clazzId, String categoryCode) {
        return CacheKeyGenerator.generateCacheKey(RewardOrder.class,
                new String[]{"clazzId", "categoryCode"},
                new Object[]{clazzId, categoryCode});
    }

    @Override
    public void touchCreateTime(long timestamp) {
        if (getCreateDatetime() == null) {
            setCreateDatetime(new Date(timestamp));
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        if (getUpdateDatetime() == null) {
            setUpdateDatetime(new Date(timestamp));
        }
    }

    @Override
    public long fetchCreateTimestamp() {
        return createDatetime == null ? 0 : createDatetime.getTime();
    }

    @Override
    public long fetchUpdateTimestamp() {
        return updateDatetime == null ? 0 : updateDatetime.getTime();
    }

    public enum Source {
        pc, app, gift, power_pillar, claw, moonlightbox;

        public static Source parse(String name) {
            try {
                return valueOf(name);
            } catch (Exception e) {
                return Source.pc;
            }
        }
    }
}
