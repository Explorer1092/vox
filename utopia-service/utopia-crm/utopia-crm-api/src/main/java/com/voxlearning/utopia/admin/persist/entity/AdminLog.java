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
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@DocumentConnection(configName = "admin")
@DocumentTable(table = "ADMIN_LOG")
public class AdminLog implements Serializable {
    private static final long serialVersionUID = 6636483407346165433L;

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @UtopiaSqlColumn @DocumentField("ADMIN_USER_NAME") private String adminUserName;
    @UtopiaSqlColumn @DocumentField("CREATE_DATETIME") private Timestamp createDatetime;
    @UtopiaSqlColumn @DocumentField("OPERATION") private String operation;
    @UtopiaSqlColumn @DocumentField("WEB_ACTION_URL") private String webActionUrl;
    @UtopiaSqlColumn @DocumentField("TARGET_ID") private Long targetId;
    @UtopiaSqlColumn @DocumentField("TARGET_STR") private String targetStr;
    @UtopiaSqlColumn @DocumentField("TARGET_DATA") private String targetData;
    @UtopiaSqlColumn @DocumentField("COMMENT") private String comment;
}
