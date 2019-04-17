package com.voxlearning.utopia.service.business.impl.dao.buffer.version;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.dao.mysql.support.CommonVersionSupport;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceWrapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class NewTeacherResourceWrapperVersion extends CommonVersionSupport {

    // 有64个字符的限制,不可用全限定名
    private static final String VERSION_NAME = NewTeacherResourceWrapper.class.getSimpleName();

    @Inject
    private DataSourceConnectionBuilder dataSourceConnectionBuilder;

    public NewTeacherResourceWrapperVersion() {
        super(VERSION_NAME, 1);
    }

    @Override
    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return dataSourceConnectionBuilder
                .getDataSourceConnection(DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
                .getNamedParameterJdbcTemplate();
    }
}
