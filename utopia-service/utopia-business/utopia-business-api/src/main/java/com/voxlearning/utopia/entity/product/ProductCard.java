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

package com.voxlearning.utopia.entity.product;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ProductCardStatus;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 产品卡信息结构
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-08-21
 */
@DocumentTable(table = "VOX_PRODUCT_CARD")
@UtopiaCacheExpiration(3600)
public class ProductCard extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -8399877679395738985L;

    @Getter @Setter @UtopiaSqlColumn private String cardType;              // 卡类型, 对应OrderProductServiceType
    @Getter @Setter @UtopiaSqlColumn private Double cardAmount;            // 卡片金额
    @Getter @Setter @UtopiaSqlColumn private Integer validPeriod;          // 有效期
    @Getter @Setter @UtopiaSqlColumn private Long cardSeq;                 // 序号
    @Getter @Setter @UtopiaSqlColumn private Long cardKey;                 // 密码,12位随机数字
    @Getter @Setter @UtopiaSqlColumn private Integer cardStatus;           // 状态,1:在库,2:开卡完毕,可激活,3:已激活,8:锁定,9:退卡
    @Getter @Setter @UtopiaSqlColumn private Date makeDatetime;            // 制卡时间
    @Getter @Setter @UtopiaSqlColumn private Long agentUserId;             // 代理用户ID
    @Getter @Setter @UtopiaSqlColumn private String openRegion;            // 可使用区域CODE
    @Getter @Setter @UtopiaSqlColumn private Date openDateTime;            // 开卡时间
    @Getter @Setter @UtopiaSqlColumn private Long userId;                  // 激活用户ID
    @Getter @Setter @UtopiaSqlColumn private Date activateDatetime;        // 激活时间
    @Getter @Setter @UtopiaSqlColumn private String lockReason;            // 锁定原因
    @Getter @Setter @UtopiaSqlColumn private Date lockDatetime;            // 锁定时间
    @Getter @Setter @UtopiaSqlColumn private String returnReason;          // 退卡原因
    @Getter @Setter @UtopiaSqlColumn private Date returnDatetime;          // 退卡时间
    @Getter @Setter @UtopiaSqlColumn private String extInfo;               // 附加信息

    public static ProductCard newInstance(OrderProductServiceType productType, Double cardAmount, Integer validPeriod, Long cardSeq, Long cardKey) {
        ProductCard instance = new ProductCard();
        instance.setCardType(productType.name());
        instance.setCardAmount(cardAmount);
        instance.setValidPeriod(validPeriod);
        instance.setCardSeq(cardSeq);
        instance.setCardKey(cardKey);
        instance.setCardStatus(ProductCardStatus.NEW.getCode());
        instance.setMakeDatetime(new Date());
        return instance;
    }
}
