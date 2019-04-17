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

package com.voxlearning.utopia.agent.persist.entity;

/**
 * Created by Shuai.Huan on 2014/7/17.
 */

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_PRODUCT")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180717")
public class AgentProduct extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 8575247533488334763L;

    @UtopiaSqlColumn Integer productType;                 // 产品类型
    @UtopiaSqlColumn String productName;                 // 产品物料名称
    @UtopiaSqlColumn String productDesc;                 // 产品物料描述
    @UtopiaSqlColumn String productImg1;                 // 产品物料图片1
    @UtopiaSqlColumn String productImg2;                 // 产品物料图片2
    @UtopiaSqlColumn String productImg3;                 // 产品物料图片3
    @UtopiaSqlColumn String productImg4;                 // 产品物料图片4
    @UtopiaSqlColumn Float price;                       // 产品价格
    @UtopiaSqlColumn Float discountPrice;               // 产品打折价格
    @UtopiaSqlColumn Date validFrom;                   // 起始有效期
    @UtopiaSqlColumn Date validTo;                     // 截至有效期
    @UtopiaSqlColumn Boolean regionLimit;                 // 是否区域限定 0：否 1：是
    @UtopiaSqlColumn Long latestEditor;                // 最后编辑人
    @UtopiaSqlColumn Integer inventoryQuantity;                   // 库存量
    @UtopiaSqlColumn Integer status;               // 商品状态 1：下架  2：上架
    @UtopiaSqlColumn Boolean primarySchoolVisible;  // 小学可见
    @UtopiaSqlColumn Boolean juniorSchoolVisible;   // 中学可见
    @UtopiaSqlColumn Integer roleVisibleAuthority;         //角色可见权限

    public boolean isPointUsable() {
//        return (productType != null) && (productType.equals(AgentProductType.WALKER.getType())
//                || productType.equals(AgentProductType.AFENTI.getType())
//                || productType.equals(AgentProductType.PICARO.getType())
//        );
        return true;
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentProduct.class, id);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AgentProduct.class, "ALL");
    }
}