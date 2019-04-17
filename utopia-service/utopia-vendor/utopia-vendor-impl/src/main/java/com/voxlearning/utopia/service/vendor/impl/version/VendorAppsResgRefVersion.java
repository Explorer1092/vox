package com.voxlearning.utopia.service.vendor.impl.version;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.dao.mysql.support.CommonVersionSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.vendor.impl.version.VendorAppsResgRefVersion")
public class VendorAppsResgRefVersion extends CommonVersionSupport {

    @Inject private DataSourceConnectionBuilder dataSourceConnectionBuilder;

    public VendorAppsResgRefVersion() {
        super("vendor.VendorAppsResgRef", 0);
    }

    @Override
    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return dataSourceConnectionBuilder.getDataSourceConnection(DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
                .getNamedParameterJdbcTemplate();
    }
}
