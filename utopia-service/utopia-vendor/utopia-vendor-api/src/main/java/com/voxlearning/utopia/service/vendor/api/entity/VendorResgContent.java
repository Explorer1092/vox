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


package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorResgContent;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

/**
 * The 3rd Vendor App Information
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @serial
 * @since 2014-06-6
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_vendor")
@DocumentTable(table = "VOX_VENDOR_RESG_CONTENT")
public class VendorResgContent extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 4416459661360543742L;

    @DocumentField("RESG_ID") private Long resgId;      // 资源组ID
    @DocumentField("RES_NAME") private String resName;   // 资源名称

    @Override
    public String[] generateCacheDimensions() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static VendorResgContent mockInstance() {
        VendorResgContent inst = new VendorResgContent();
        inst.resgId = 0L;
        inst.resName = new ObjectId().toString();
        return inst;
    }

    public MDBVendorResgContent transform() {
        MDBVendorResgContent t = new MDBVendorResgContent();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setUpdateDatetime(updateDatetime);
        t.setResgId(resgId);
        t.setResName(resName);
        return t;
    }
}
