package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;


@Named
@CacheBean(type = ChipsUserCourse.class)
public class ChipsUserCoursePersistence extends StaticMySQLPersistence<ChipsUserCourse, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsUserCourse document, Collection<String> dimensions) {
        dimensions.add(ChipsUserCourse.ck_user_id(document.getUserId()));
    }

    @Override
    public ChipsUserCourse load(Long id) {
        return $load(id);
    }

    public void disabled(ChipsUserCourse document) {
        Update update = new Update();
        update.set("DISABLED", true).set("UPDATETIME", new Date());
        Criteria criteria = Criteria.where("ID").is(document.getId()).and("DISABLED").is(false);
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0) {
            cleanCache(document.getUserId());
        }
    }

    @CacheMethod
    public List<ChipsUserCourse> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public void insertOrUpdate(ChipsUserCourse chipsUserCourse) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), chipsUserCourse, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0");
        if (chipsUserCourse.getActive() != null) {
            sql.append(" ,`ACTIVE`= b'" + (chipsUserCourse.getActive() ? 1 : 0) +"'");
        }
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(chipsUserCourse.getUserId());
        }
    }

    public void updateProductAndProductItem(Long userId, String productId, String productItemId, String originProductId) {
        List<ChipsUserCourse> courseList = loadByUserId(userId);
        ChipsUserCourse chipsUserCourse = courseList.stream().filter(c -> c.getProductId().equals(originProductId)).findFirst().orElse(null);
        if (chipsUserCourse == null) {
            return;
        }
        disabled(chipsUserCourse);//先disable掉老的数据
        chipsUserCourse.setId(null);//
        chipsUserCourse.setProductId(productId);
        chipsUserCourse.setProductItemId(productItemId);
        chipsUserCourse.setOperation(ChipsUserCourse.Operation.CHANGE);
//        insert(chipsUserCourse);//新插入一条
        insertOrUpdate(chipsUserCourse);
//        Update update = new Update();
//        update.set("UPDATETIME", new Date()).set("PRODUCT_ID", productId).set("PRODUCT_ITEM_ID", productItemId).set("OPERATION",ChipsUserCourse.Operation.CHANGE.name());
//        Criteria criteria = Criteria.where("userId").is(userId);
//        long res = executeUpdate(update, criteria, getTableName());
//        if (res > 0L) {
//           cleanCache(userId);
//        }
    }

    private void cleanCache(Long userId) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsUserCourse.ck_user_id(userId));
        getCache().deletes(cacheIds);
    }
}
