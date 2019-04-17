package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.core.ObjectIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentTable(table = "VOX_VENDOR_USER_REF")
@DocumentConnection(configName = "hs_misc")
public class VendorUserRef extends AbstractDatabaseEntityWithDisabledField implements Serializable {

    @UtopiaSqlColumn
    private String appKey;

    @UtopiaSqlColumn
    private Long userId;

    @UtopiaSqlColumn
    private String productId;

}
