package com.voxlearning.utopia.service.vendor.impl.version;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.dao.mysql.support.CommonVersionSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2018-09-21 下午7:34
 **/
@Named
public class LiveCastCourseBufferVersion extends CommonVersionSupport {


    @Inject
    private DataSourceConnectionBuilder dataSourceConnectionBuilder;

    protected LiveCastCourseBufferVersion() {
        super("vendor.LiveCastCourse", 10000);
    }

    @Override
    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return dataSourceConnectionBuilder.getDataSourceConnection(DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
                .getNamedParameterJdbcTemplate();
    }
}
