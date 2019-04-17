package com.voxlearning.utopia.schedule.support.userquery;

import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.AnnotationUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSqlBuilder_Common;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.utopia.core.runtime.ProductDevelopment;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author changyuan.liu
 * @since 2017/8/3
 */
@Named
public class UserDataQuerier implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(UserDataQuerier.class);

    private UtopiaSql sql = UtopiaSqlFactory.instance().getDefaultUtopiaSql();
    private List<String> tableNames = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        DocumentTable annotation = AnnotationUtils.getAnnotation(User.class, DocumentTable.class);
        if (annotation == null) {
            logger.error("No DocumentTable annotation found for SyahUser!");
            return;
        }

        String pattern = annotation.table();

        // 默认100张表，直接hardcode
        int n = (ProductDevelopment.isProductionEnv() || ProductDevelopment.isStagingEnv()) ? 20 : 10;

        for (int i = 0; i < n; i++) {
            String tableName = StringUtils.formatMessage(pattern, i);
            tableNames.add(tableName);
        }
    }

    public List<Map<String, Object>> query(UserDataQueryStatement statement) {
        StringBuilder selectQuery = new StringBuilder("SELECT ");
        if (CollectionUtils.isNotEmpty(statement.getQueryFields())) {
            statement.getQueryFields().forEach(f -> selectQuery.append(f).append(","));
            selectQuery.deleteCharAt(selectQuery.length() - 1).append(" ");// remove comma
        } else {
            selectQuery.append("* ");
        }

        List<Map<String, Object>> result = new LinkedList<>();
        tableNames.forEach(t -> {
            StringBuilder query = new StringBuilder(selectQuery.toString());

            query.append("FROM ").append(t);
            if (StringUtils.isNoneBlank(statement.getWhereSql())) {
                query.append(" WHERE ").append(statement.getWhereSql());
            }
            UtopiaSqlBuilder_Common sqlBuilder = sql.withSql(query.toString());
            if (CollectionUtils.isNotEmpty(statement.getParams())) {
                sqlBuilder.useParams(statement.getParams());
            }
            List<Map<String, Object>> list = sqlBuilder.queryAll();
            result.addAll(list);
        });

//        // 处理原User表
//        addUctUsersData(result, statement);

        return result;
    }

//    private void addUctUsersData(List<Map<String, Object>> result, UserDataQueryStatement statement) {
//        StringBuilder query = new StringBuilder("SELECT ");
//        if (CollectionUtils.isNotEmpty(statement.getQueryFields())) {
//            statement.getQueryFields().forEach(f -> query.append(f).append(","));
//            query.deleteCharAt(query.length() - 1).append(" ");// remove comma
//        } else {
//            query.append("* ");
//        }
//
//        query.append("FROM UCT_USER");
//        if (StringUtils.isNoneBlank(statement.getWhereSql())) {
//            query.append(" WHERE ").append(statement.getWhereSql());
//        }
//
//        UtopiaSqlBuilder_Common sqlBuilder = sql.withSql(query.toString());
//        if (CollectionUtils.isNotEmpty(statement.getParams())) {
//            sqlBuilder.useParams(statement.getParams());
//        }
//        List<Map<String, Object>> uctUserResults = sqlBuilder.queryAll();
//
//        // remove duplicates
//        if (CollectionUtils.isNotEmpty(statement.getQueryFields())
//            && statement.getQueryFields().contains("ID")) {// only support ID field
//            Set<Long> set = result.stream().map(e -> SafeConverter.toLong(e.get("ID"))).collect(Collectors.toSet());
//            uctUserResults.forEach(r -> {
//                if (!set.contains(SafeConverter.toLong(r.get("ID")))) {
//                    result.add(r);
//                }
//            });
//        }
//    }

}
