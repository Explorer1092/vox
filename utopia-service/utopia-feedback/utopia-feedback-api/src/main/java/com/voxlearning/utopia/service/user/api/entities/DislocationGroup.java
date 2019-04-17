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

package com.voxlearning.utopia.service.user.api.entities;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author fugui.chang
 * @since 2016-10-13 9:18
 */

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_DISLOCATION_GROUP")
public class DislocationGroup extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 5010557005857858685L;

    @DocumentField("GROUP_ID") private Long groupId; //可以根据groupId查询到当前所在的学校
    @DocumentField("REAL_SCHOOL_ID") private Long realSchoolId; //groupId实际所在学校
    @DocumentField("NOTES") private String notes; //操作备注
    @DocumentField("LATEST_OPERATOR") private String latestOperator; //操作人员姓名
}
