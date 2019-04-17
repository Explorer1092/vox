package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonBookRef;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = AIUserLessonBookRef.class)
public class AIUserLessonBookRefPersistence extends StaticMySQLPersistence<AIUserLessonBookRef, Long> {

    @Override
    protected void calculateCacheDimensions(AIUserLessonBookRef document, Collection<String> dimensions) {
        dimensions.add(AIUserLessonBookRef.ck_uid(document.getUserId()));
    }

    @CacheMethod
    public List<AIUserLessonBookRef> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(Query.query(criteria));
    }

    public void insertOrUpdate(AIUserLessonBookRef document) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0");
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(document);
        }
    }

    public void deleteByUser(Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("DISABLED").is(false);
        Update update = new Update();
        update.set("UPDATETIME", new Date()).set("DISABLED", true);
        long result = executeUpdate(update, criteria, getTableName());
        if (Long.compare(result, 0L) > 0) {
            cleanCache(userId);
        }
    }

    private void cleanCache(AIUserLessonBookRef document) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(AIUserLessonBookRef.ck_uid(document.getUserId()));
        getCache().deletes(cacheIds);
    }

    private void cleanCache(Long userId) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(AIUserLessonBookRef.ck_uid(userId));
        getCache().deletes(cacheIds);
    }
}
