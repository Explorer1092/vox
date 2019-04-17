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

package com.voxlearning.utopia.service.zone.mdb;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentDDL;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.service.zone.api.entity.UserMood;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@DocumentConnection(configName = "utopia")
@DocumentTable(table = "MDB_USER_MOOD")
@DocumentDDL(path = "ddl/mdb/MDB_USER_MOOD.ddl")
public class MDBUserMood implements Serializable {
    private static final long serialVersionUID = -4980270746524328311L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Long id;
    private Date createDatetime;
    private String title;
    private String description;
    private String imgUrl;

    public UserMood transform() {
        UserMood t = new UserMood();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setTitle(title);
        t.setDescription(description);
        t.setImgUrl(imgUrl);
        return t;
    }
}
