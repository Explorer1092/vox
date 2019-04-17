package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.dao.mysql.support.CommonVersionSupport;
import com.voxlearning.alps.dao.mysql.support.ManagedCommonVersion;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ManagedCommonVersion
public class ActivityConfigVersionDao extends CommonVersionSupport implements InitializingBean {

    // 有64个字符的限制,不可用全限定名, 类要是重名就完了, 先加个前缀
    private static final String VERSION_NAME = "CRM_" + ActivityConfig.class.getSimpleName();

    @Inject
    private DataSourceConnectionBuilder dataSourceConnectionBuilder;

    @Override
    public void afterPropertiesSet() {
        super.increment();
    }

    public ActivityConfigVersionDao() {
        super(VERSION_NAME, 1);
    }

    @Override
    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return dataSourceConnectionBuilder
                .getDataSourceConnection(DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
                .getNamedParameterJdbcTemplate();
    }
}
