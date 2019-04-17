package com.voxlearning.utopia.service.reward.impl.dao.newversion;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.dao.mysql.support.CommonVersionSupport;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategoryRef;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ProductCategoryRefVersion extends CommonVersionSupport {

    private static final String VERSION_NAME = ProductCategoryRef.class.getSimpleName();

    @Inject
    private DataSourceConnectionBuilder dataSourceConnectionBuilder;

    public ProductCategoryRefVersion() {
        super(VERSION_NAME, 1);
    }

    @Override
    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return dataSourceConnectionBuilder
                .getDataSourceConnection(DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
                .getNamedParameterJdbcTemplate();
    }
}
