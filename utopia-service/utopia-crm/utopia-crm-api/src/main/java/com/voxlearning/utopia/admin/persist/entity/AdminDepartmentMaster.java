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

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-9
 * Time: 下午2:49
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@DocumentConnection(configName = "admin")
@DocumentTable(table = "ADMIN_DEPARTMENT_MASTER")
public class AdminDepartmentMaster implements Serializable {

    private static final long serialVersionUID = 2262239055770862713L;

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @UtopiaSqlColumn @DocumentField("DEPARTMENT_NAME") private String departmentName;
    @UtopiaSqlColumn @DocumentField("USER_NAME") private String userName;
    @UtopiaSqlColumn @DocumentField("RIGHT_READ") private Boolean rightRead;
    @UtopiaSqlColumn @DocumentField("RIGHT_WRITE") private Boolean rightWrite;
    @UtopiaSqlColumn @DocumentField("RIGHT_DELETE") private Boolean rightDelete;
    @UtopiaSqlColumn @DocumentField("CREATE_DATETIME") private Timestamp createDatetime;
}
