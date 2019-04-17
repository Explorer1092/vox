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


package com.voxlearning.utopia.service.vendor.mdb;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentDDL;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "utopia")
@DocumentTable(table = "MDB_VENDOR_RESG_CONTENT")
@DocumentDDL(path = "ddl/mdb/MDB_VENDOR_RESG_CONTENT.ddl")
public class MDBVendorResgContent implements Serializable {
    private static final long serialVersionUID = 1463482227007347565L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("ID") private Long id;
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentField("UPDATE_DATETIME") private Date updateDatetime;
    @DocumentField("RESG_ID") private Long resgId;
    @DocumentField("RES_NAME") private String resName;

    public VendorResgContent transform() {
        VendorResgContent t = new VendorResgContent();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setUpdateDatetime(updateDatetime);
        t.setResgId(resgId);
        t.setResName(resName);
        return t;
    }

}
