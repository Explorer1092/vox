package com.voxlearning.utopia.service.ai.impl.persistence.reddot;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.reddot.ChipsUserRedDotPageRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Named
@CacheBean(type = ChipsUserRedDotPageRecord.class)
public class ChipsUserRedDotRecordPagePersistence extends StaticMySQLPersistence<ChipsUserRedDotPageRecord, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsUserRedDotPageRecord document, Collection<String> dimensions) {
        dimensions.add(ChipsUserRedDotPageRecord.ck_user(document.getUserId()));
    }

    @Override
    public ChipsUserRedDotPageRecord load(Long id) {
        return super.$load(id);
    }

    public void insertOrUpdate(Long userId, Long pageId, Boolean read) {
        ChipsUserRedDotPageRecord document = new ChipsUserRedDotPageRecord(userId, pageId, read);
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0");
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(userId);
        }
    }

    @CacheMethod
    public List<ChipsUserRedDotPageRecord> loadByUser(@CacheParameter(value = "U") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    private void cleanCache(Long userId) {
        if (userId == null) {
            return;
        }
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsUserRedDotPageRecord.ck_user(userId));
        getCache().deletes(cacheIds);
    }
}
