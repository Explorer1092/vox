package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsGroupShopping;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsGroupShopping.class)
public class ChipsGroupShoppingPersistence extends StaticMySQLPersistence<ChipsGroupShopping, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsGroupShopping document, Collection<String> dimensions) {
        dimensions.add(ChipsGroupShopping.ck_active());
        dimensions.add(ChipsGroupShopping.ck_code(document.getCode()));
    }

    @Override
    public ChipsGroupShopping load(Long id) {
        return $load(id);
    }

    @CacheMethod
    public ChipsGroupShopping loadByCode(@CacheParameter(value = "C") String code) {
        Criteria criteria = Criteria.where("CODE").is(code);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }


    public void insertOrUpdate(Long userId, String orderId, String code) {
        ChipsGroupShopping chipsGroupShopping = new ChipsGroupShopping(userId, orderId, code);
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), chipsGroupShopping, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0");
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(code);
        }
    }

    public void insertOrUpdate(Long userId, String orderId, String code, int number) {
        ChipsGroupShopping chipsGroupShopping = new ChipsGroupShopping(userId, orderId, code);
        chipsGroupShopping.setNumber(number);
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), chipsGroupShopping, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0");
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            cleanCache(code);
        }
    }

    /**
     * 取22 小时前的数据
     * @return
     */
    @CacheMethod(key = "active")
    public List<ChipsGroupShopping> loadInGroupingRecord() {
        Date comDate = DateUtils.addHours(new Date(), -22);
        Criteria criteria = Criteria.where("NUMBER").is(1).and("CREATETIME").gt(comDate);
        Sort sort = new Sort(Sort.Direction.ASC, "CREATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public boolean updateNumber(String code, Integer from, Integer to) {
        Criteria criteria = Criteria.where("NUMBER").is(from).and("CODE").is(code);
        Update update = new Update().set("NUMBER", to)
                .set("UPDATETIME", new Date());
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0) {
            cleanCache(code);
            return true;
        }

        return false;
    }

    private void cleanCache(String code) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsGroupShopping.ck_active());
        cacheIds.add(ChipsGroupShopping.ck_code(code));
        getCache().deletes(cacheIds);
    }
}
