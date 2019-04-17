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
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-14
 * Time: 下午9:36
 * To change this template use File | Settings | File Templates.
 */
@Data
@DocumentTable(table = "ADMIN_APP_SYSTEM_MASTER")
@DocumentConnection(configName = "admin")
public class AdminAppSystemMaster implements Serializable {
    private static final long serialVersionUID = 7852510722372985418L;

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;
    @UtopiaSqlColumn private String appName;
    @UtopiaSqlColumn private String userName;
    @UtopiaSqlColumn private Boolean rightRead;
    @UtopiaSqlColumn private Boolean rightWrite;
    @UtopiaSqlColumn private Boolean rightDelete;
    @UtopiaSqlColumn private Timestamp createDatetime;
}
