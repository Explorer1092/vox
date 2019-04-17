package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@DocumentTable(table = "VOX_VENDOR_USAGE_STAT")
@DocumentConnection(configName = "hs_misc")
public class VendorUsageStat extends AbstractDatabaseEntityWithDisabledField implements Serializable, CacheDimensionDocument {

    private static final long serialVersionUID = 6579668321678199103L;
    @UtopiaSqlColumn
    private String appKey;

    @UtopiaSqlColumn
    private String yearMonth;

    @UtopiaSqlColumn
    private Long totalNum;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"ak", "ym"}, new Object[]{appKey, yearMonth})
        };
    }
}
