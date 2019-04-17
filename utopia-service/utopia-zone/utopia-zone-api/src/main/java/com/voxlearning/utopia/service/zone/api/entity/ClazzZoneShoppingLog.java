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

package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.api.constant.Currency;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-5-12
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_CLAZZ_ZONE_SHOPPING_LOG")
public class ClazzZoneShoppingLog implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -4409045050433958361L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentField("USER_ID") private Long userId;
    @DocumentField("PRODUCT_ID") private Long productId;
    @DocumentField("CURRENCY") private Currency currency;
    @DocumentField("PRICE") private Integer price;
    @DocumentField("PERIOD_OF_VALIDITY") private Long periodOfValidity;

    @Override
    public void touchCreateTime(long timestamp) {
        if (createDatetime == null) {
            createDatetime = new Date(timestamp);
        }
    }
}
