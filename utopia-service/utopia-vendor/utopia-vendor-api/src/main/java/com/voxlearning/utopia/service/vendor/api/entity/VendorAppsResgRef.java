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
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorAppsResgRef;
import lombok.*;
import org.bson.types.ObjectId;

/**
 * The 3rd Vendor App Information
 * FIXME: 根据查询条件，APP_ID, APP_KEY, RESG_ID应该有索引
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @serial
 * @since 2014-06-6
 */
@Getter
@Setter
@DocumentTable(table = "VOX_VENDOR_APPS_RESG_REF")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
@DocumentConnection(configName = "hs_vendor")
public class VendorAppsResgRef extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 4416459661360543742L;

    @UtopiaSqlColumn @NonNull private Long appId;       // 应用ID
    @UtopiaSqlColumn @NonNull private String appKey;    // 应用APPKEY
    @UtopiaSqlColumn @NonNull private Long resgId;      // 资源组ID

    @Override
    public String[] generateCacheDimensions() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @DocumentFieldIgnore
    private VendorResg resg;                            // 管理的资源组对象

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static VendorAppsResgRef mockInstance() {
        VendorAppsResgRef inst = new VendorAppsResgRef();
        inst.appId = 0L;
        inst.appKey = new ObjectId().toString();
        inst.resgId = 0L;
        return inst;
    }

    public VendorAppsResgRef withAppId(Long appId) {
        this.appId = appId;
        return this;
    }

    public VendorAppsResgRef withResgId(Long resgId) {
        this.resgId = resgId;
        return this;
    }

    public MDBVendorAppsResgRef transform() {
        MDBVendorAppsResgRef ref = new MDBVendorAppsResgRef();
        ref.setId(id);
        ref.setAppId(appId);
        ref.setAppKey(appKey);
        ref.setResgId(resgId);
        ref.setCreateDatetime(createDatetime);
        ref.setUpdateDatetime(updateDatetime);
        return ref;
    }
}
