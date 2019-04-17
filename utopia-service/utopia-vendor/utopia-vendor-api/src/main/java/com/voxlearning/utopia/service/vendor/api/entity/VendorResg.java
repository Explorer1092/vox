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


package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.DisabledAccessor;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.List;

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
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160728")
@DocumentTable(table = "VOX_VENDOR_RESG")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
@DocumentConnection(configName = "hs_vendor")
public class VendorResg extends AbstractDatabaseEntity implements DisabledAccessor, CacheDimensionDocument {
    private static final long serialVersionUID = 4406459661360543742L;

    @UtopiaSqlColumn @NonNull private String cname;     // 中文名称
    @UtopiaSqlColumn private String ename;              // 英文名称
    @UtopiaSqlColumn private String description;        // 描述
    @UtopiaSqlColumn private Boolean disabled;          // 是否已被删除

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ALL_RESGS")
        };
    }

    @DocumentFieldIgnore
    private List<VendorResgContent> resgContentList;    // 内容列表

    public static String generateCacheKeyAll() {
        return CacheKeyGenerator.generateCacheKey(VendorResg.class, "ALL_RESGS");
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static VendorResg mockInstance() {
        VendorResg inst = new VendorResg();
        inst.cname = new ObjectId().toString();
        return inst;
    }

    public VendorResg withDisabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }
}
