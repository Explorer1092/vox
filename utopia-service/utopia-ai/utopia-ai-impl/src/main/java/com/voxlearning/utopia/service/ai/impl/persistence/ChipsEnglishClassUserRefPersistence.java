package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUserRef;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsEnglishClassUserRef.class)
public class ChipsEnglishClassUserRefPersistence extends StaticMySQLPersistence<ChipsEnglishClassUserRef, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishClassUserRef document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishClassUserRef.ck_class_id(document.getChipsClassId()));
        dimensions.add(ChipsEnglishClassUserRef.ck_user_id(document.getUserId()));
    }

    @Override
    public ChipsEnglishClassUserRef load(Long id) {
        return $load(id);
    }

    public void disabled(ChipsEnglishClassUserRef userRef) {
        Update update = new Update();
        update.set("DISABLED", true).set("UPDATETIME", new Date());
        Criteria criteria = Criteria.where("ID").is(userRef.getId()).and("DISABLED").is(false);
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0) {
            cleanCache(userRef);
        }
    }

    @CacheMethod
    public List<ChipsEnglishClassUserRef> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("USER_ID").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<ChipsEnglishClassUserRef> loadByClassId(@CacheParameter("CID") Long chipsClassId) {
        Sort sort = new Sort(Sort.Direction.ASC, "USER_ID");
        Criteria criteria =  Criteria.where("DISABLED").is(false).and("CHIPS_CLASS_ID").is(chipsClassId);
        return query(Query.query(criteria).with(sort));
    }

    public void insertOrUpdate(ChipsEnglishClassUserRef document) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0,`IN_GROUP`=" + document.getInGroup());
        if (StringUtils.isNotBlank(document.getOrderRef())) {
            sql.append(" ,`ORDER_REF`=\"" + document.getOrderRef()+"\"");
        }
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(document);
        }
    }

    public void insertOrUpdateOrderReffer(ChipsEnglishClassUserRef document) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW()");
        sql.append(" ,`ORDER_REF`=\"" + document.getOrderRef()+"\"");
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(document);
        }
    }

//    public void update(ChipsEnglishClassUserRef document) {
//        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
//        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
//        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`USER_ID`=" + document.getUserId() + " ,`CHIPS_CLASS_ID`=" + document.getChipsClassId()
//                + ",`IN_GROUP`=" + (document.getInGroup() == null ? false:document.getInGroup()));
//        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
//        if (res > 0) {
//            cleanCache(document);
//        }
//
//    }

//    public void updateClazzId(List<Long> userIds, Long clazzId, Long aimClazzId) {
//        Update update = new Update();
//        update.set("CHIPS_CLASS_ID", aimClazzId);
//        update.set("UPDATETIME", new Date());
//        Criteria criteria = Criteria.where("USER_ID").in(userIds).and("CHIPS_CLASS_ID").is(clazzId).and("DISABLED").is(false);
//        long res = executeUpdate(update, criteria, getTableName());
//        if (res > 0) {
//            cleanCache(userIds, clazzId, aimClazzId);
//        }
//    }

    private void cleanCache(List<Long> userIds, Long clazzId, Long aimClazzId) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsEnglishClassUserRef.ck_class_id(clazzId));
        cacheIds.add(ChipsEnglishClassUserRef.ck_class_id(aimClazzId));
        if (CollectionUtils.isNotEmpty(userIds)) {
            userIds.forEach(e -> cacheIds.add(ChipsEnglishClassUserRef.ck_user_id(e)));
        }
        getCache().deletes(cacheIds);
    }

    private void cleanCache(ChipsEnglishClassUserRef document) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsEnglishClassUserRef.ck_class_id(document.getChipsClassId()));
        cacheIds.add(ChipsEnglishClassUserRef.ck_user_id(document.getUserId()));
        getCache().deletes(cacheIds);
    }
}
