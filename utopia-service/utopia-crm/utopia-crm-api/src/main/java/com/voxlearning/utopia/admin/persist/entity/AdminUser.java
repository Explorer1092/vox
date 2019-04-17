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
import com.voxlearning.alps.lang.convert.SafeConverter;
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
@DocumentTable(table = "ADMIN_USER")
public class AdminUser implements Serializable {
    private static final long serialVersionUID = -223977701924325836L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("ADMIN_USER_NAME") private String adminUserName;
    @DocumentField("PASSWORD") private String password;
    @DocumentField("PASSWORD_SALT") private String passwordSalt;
    @DocumentField("CREATE_DATETIME") private Timestamp createDatetime;
    @DocumentField("DEPARTMENT_NAME") private String departmentName;
    @DocumentField("REAL_NAME") private String realName;
    @DocumentField("COMMENT") private String comment;
    @DocumentField("AGENT_ID") private String agentId;
    @DocumentField("SUPER_ADMIN") private Boolean superAdmin;
    @DocumentField("DISABLED") private Boolean disabled;
    @DocumentField("REDMINE_APIKEY") private String redmineApikey;

    public static boolean checkSuperAdminPermission(AdminUser user) {
        return user != null
                && !SafeConverter.toBoolean(user.getDisabled())
                && SafeConverter.toBoolean(user.getSuperAdmin());
    }
}
