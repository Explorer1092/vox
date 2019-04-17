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

package com.voxlearning.utopia.admin.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.utopia.api.constant.RecordType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Zou Peng
 * Date: 13-6-9
 * Time: 下午7:01
 * To change this template use File | Settings | File Templates.
 */
@Data
@DocumentConnection(configName = "admin")
@DocumentTable(table = "ADMIN_CUSTOMER_SERVICE_RECORD")
public class CustomerServiceRecord implements Serializable {

    private static final long serialVersionUID = -3150072839828099900L;

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;
    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private String adminUserName;
    @UtopiaSqlColumn private RecordType recordType;
    @UtopiaSqlColumn private String questionDesc;
    @UtopiaSqlColumn private String operation;
    @UtopiaSqlColumn private Date createDatetime;
    @UtopiaSqlColumn private Date updateDatetime;
}
