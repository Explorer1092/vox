package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author qianlong.yang
 * @version 0.1
 * @since 2017/4/10
 */
@Getter
@Setter
@DocumentTable(table = "VOX_FAIRYLAND_CHANNEL")
@DocumentConnection(configName = "hs_vendor")
public class VendorFairylandChannel extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -5802862841564906010L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentField("DESCRIPTION") private String description;
    @DocumentField("PLATFORM") private Integer platform;
    @DocumentField("DISABLED") private Boolean disabled;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentUpdateTimestamp
    @DocumentField("UPDATE_DATETIME") private Date updateDatetime;
}
