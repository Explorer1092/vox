package com.voxlearning.utopia.service.vendor.mdb;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentDDL;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsResgRef;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@DocumentConnection(configName = "utopia")
@DocumentTable(table = "MDB_VENDOR_APPS_RESG_REF")
@DocumentDDL(path = "ddl/mdb/MDB_VENDOR_APPS_RESG_REF.ddl")
public class MDBVendorAppsResgRef implements Serializable {
    private static final long serialVersionUID = 1665225800629331411L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("ID") private Long id;
    @DocumentField("APP_ID") private Long appId;
    @DocumentField("APP_KEY") private String appKey;
    @DocumentField("RESG_ID") private Long resgId;
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentField("UPDATE_DATETIME") private Date updateDatetime;

    public VendorAppsResgRef transform() {
        VendorAppsResgRef ref = new VendorAppsResgRef();
        ref.setId(this.getId());
        ref.setAppId(this.getAppId());
        ref.setAppKey(this.getAppKey());
        ref.setResgId(this.getResgId());
        ref.setCreateDatetime(this.getCreateDatetime());
        ref.setUpdateDatetime(this.getUpdateDatetime());
        return ref;
    }
}
