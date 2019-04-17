package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_XQB_SIGN_UP")
public class XqbSignUp extends AbstractDatabaseEntity{

    private static final long serialVersionUID = 3364196119877703209L;

    @UtopiaSqlColumn private String studentName;
    @UtopiaSqlColumn private String parentName;
    @UtopiaSqlColumn private String phone;
    @UtopiaSqlColumn private Boolean online;
    @UtopiaSqlColumn private String worksUrl;
    @UtopiaSqlColumn private String worksName;
    @UtopiaSqlColumn private String regionCode;
    @UtopiaSqlColumn(name = "AGENT_CODE") private String agentcode;      // 华泰公司业务员
}
