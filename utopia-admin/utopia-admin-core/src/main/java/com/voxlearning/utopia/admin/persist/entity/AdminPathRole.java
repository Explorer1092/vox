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

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-16
 * Time: 下午3:59
 * To change this template use File | Settings | File Templates.
 */
@Data
@DocumentConnection(configName = "admin")
@DocumentTable(table = "ADMIN_PATH_ROLE")
public class AdminPathRole implements Serializable {
    private static final long serialVersionUID = -2796039582755288189L;

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;
    @UtopiaSqlColumn private Long pathId;
    @UtopiaSqlColumn private String roleName;
    @UtopiaSqlColumn private Boolean disabled;
}
