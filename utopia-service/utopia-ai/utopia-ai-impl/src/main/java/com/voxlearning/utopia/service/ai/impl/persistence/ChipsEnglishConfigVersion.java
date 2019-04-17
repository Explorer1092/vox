package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.dao.mysql.support.CommonVersionSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Summer on 2018/11/14
 */
@Named
public class ChipsEnglishConfigVersion extends CommonVersionSupport {

    @Inject
    private DataSourceConnectionBuilder dataSourceConnectionBuilder;

    public ChipsEnglishConfigVersion() {
        super("ChipsEnglishConfigVersion", 1);
    }

    @Override
    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return dataSourceConnectionBuilder
                .getDataSourceConnection(DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
                .getNamedParameterJdbcTemplate();
    }
}
