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

package com.voxlearning.utopia.admin.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-14
 * Time: 下午9:06
 * To change this template use File | Settings | File Templates.
 */
@Data
@DocumentConnection(configName = "admin")
@DocumentTable(table = "ADMIN_APP_SYSTEM")
public class AdminAppSystem implements Serializable {
    private static final long serialVersionUID = 1826635943967134640L;

    @UtopiaSqlColumn(primaryKey = true) private String appName;
    @UtopiaSqlColumn private String appDescription;
    @UtopiaSqlColumn private String callBackUrl;
    @UtopiaSqlColumn private String appKey;
    @DocumentCreateTimestamp
    @UtopiaSqlColumn private Timestamp createDatetime;
}
