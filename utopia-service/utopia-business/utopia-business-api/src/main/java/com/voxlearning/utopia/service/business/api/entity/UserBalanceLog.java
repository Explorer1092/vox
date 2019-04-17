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

package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.OperationType;
import com.voxlearning.utopia.api.constant.ReferenceType;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsOrder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * <pre>
 *     +--------------------+---------------+------+-----+---------+----------------+
 *     | Field              | Type          | Null | Key | Default | Extra          |
 *     +--------------------+---------------+------+-----+---------+----------------+
 *     | ID                 | bigint(20)    | NO   | PRI | NULL    | auto_increment |
 *     | CREATE_DATETIME    | datetime      | NO   | MUL | NULL    |                |
 *     | USER_ID            | bigint(20)    | NO   | MUL | NULL    |                |
 *     | OPERATION_TYPE     | varchar(25)   | NO   |     | NULL    |                |
 *     | OPERATION_METHOD   | varchar(255)  | YES  |     | NULL    |                |
 *     | OPERATION_EXT_ID   | varchar(255)  | YES  | MUL | NULL    |                |
 *     | OPERATION_EXT_DATA | varchar(255)  | YES  |     | NULL    |                |
 *     | AMOUNT             | decimal(14,4) | NO   |     | NULL    |                |
 *     | REFERENCE_TYPE     | varchar(25)   | NO   |     | NULL    |                |
 *     | REFERENCE_STR      | varchar(255)  | YES  | MUL | NULL    |                |
 *     +--------------------+---------------+------+-----+---------+----------------+
 * </pre>
 */
@Getter
@Setter
@DocumentTable(table = "UCT_USER_BALANCE_LOG")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20160729")
public class UserBalanceLog implements Serializable, CacheDimensionDocument {
    private static final long serialVersionUID = -6873069154515502982L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;
    @DocumentCreateTimestamp
    private Date createDatetime;
    private Long userId;
    private OperationType operationType;
    private String operationMethod;
    private String operationExtId;
    private String operationExtData;
    private Double amount;
    private ReferenceType referenceType;
    private String referenceStr;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("userId", userId)
        };
    }

    public static UserBalanceLog newFromPaymentCallbackContext(Long userId, ReferenceType referenceType, String referenceStr, PaymentCallbackContext context) {
        UserBalanceLog log = new UserBalanceLog();
        log.userId = userId;
        log.createDatetime = new Timestamp(System.currentTimeMillis());
        log.operationMethod = context.getPayMethodGateway();
        log.operationExtId = context.getVerifiedPaymentData().getExternalTradeNumber();
        log.operationExtData = context.getVerifiedPaymentData().getExternalUserId();
//        log.amount = context.getVerifiedPaymentData().getPayAmount();
        log.referenceType = referenceType;
        log.referenceStr = referenceStr;
        log.operationType = OperationType.Credit;
        return log;
    }

    public static UserBalanceLog newForAppsOrderPayment(VendorAppsOrder appsOrder) {
        UserBalanceLog log = new UserBalanceLog();
        log.userId = appsOrder.getUserId();
        log.createDatetime = new Timestamp(System.currentTimeMillis());
        log.operationMethod = "pay";
        log.operationExtId = "";
        log.operationExtData = "";
        log.amount = appsOrder.getAmount();
        log.referenceType = ReferenceType.AppsOrder;
        log.referenceStr = appsOrder.getId();
        log.operationType = OperationType.Debit;
        return log;
    }
}
