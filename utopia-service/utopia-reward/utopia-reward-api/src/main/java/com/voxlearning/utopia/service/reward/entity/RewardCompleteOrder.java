/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by XiaoPeng.Yang on 14-8-1.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_COMPLETE_ORDER")
public class RewardCompleteOrder implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = 6684185040584419728L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") Date createDatetime;
    @DocumentField private Long orderId;
    @DocumentField private Long productId;
    @DocumentField private String productName;
    @DocumentField private Long skuId;
    @DocumentField private String skuName;
    @DocumentField private Integer quantity;
    @DocumentField private Double price;
    @DocumentField private Double totalPrice;
    @DocumentField private Double discount;
    @DocumentField private String unit;
    @DocumentField private Long buyerId;
    @DocumentField private String buyerName;
    @DocumentField private Integer buyerType; // 用户类型
    @DocumentField private String status;
    @DocumentField private String saleGroup;
    @DocumentField private Boolean disabled;
    @DocumentField private Long receiverId;
    @DocumentField private String receiverName;
    @DocumentField private Long schoolId;                   // 学校ID
    @DocumentField private String schoolName;               // 学校名称
    @DocumentField private Long clazzId;                    // 班级ID
    @DocumentField private String clazzName;                // 班级名称
    @DocumentField private Integer clazzLevel;              // 年级
    @DocumentField("PHONE") private String sensitivePhone;  // 联系电话
    @DocumentField private String detailAddress;            // 详细地址
    @DocumentField private String postCode;                 // 邮政编码
    @DocumentField private Long provinceCode;               // 省编码
    @DocumentField private String provinceName;             // 省
    @DocumentField private Long cityCode;                   // 市编码
    @DocumentField private String cityName;                 // 市
    @DocumentField private Long countyCode;                 // 区县编码
    @DocumentField private String countyName;               // 区县
    @DocumentField private String logisticType;             // 快递类型
    @DocumentField private String logisticsId;              // 快递单ID FIXME: 数据库里明明用的BIGINT(20)，还能说点啥好

    @Override
    public void touchCreateTime(long timestamp) {
        if (getCreateDatetime() == null) {
            setCreateDatetime(new Date(timestamp));
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
    }
}