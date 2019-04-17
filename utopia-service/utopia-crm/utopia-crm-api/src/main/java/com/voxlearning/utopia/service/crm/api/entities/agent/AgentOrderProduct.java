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

package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * AGENT订单关联的商品信息表
 *
 * @author Alex
 * @serial
 * @since 2014-8-14
 */
@Getter
@Setter
@DocumentTable(table = "AGENT_ORDER_PRODUCT")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180525")
@DocumentConnection(configName = "agent")
public class AgentOrderProduct extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 2741325413223423233L;

    private Long orderId;                  // 订单ID
    private Integer productType;           // 产品类型
    private Long productId;                // 商品ID
    private Integer productQuantity;       // 商品数量
    private Integer rank;                  // 顺序
    private String productName; // 商品名称
    private Float price; // 商品价格

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("orderId", orderId)
        };
    }
}
