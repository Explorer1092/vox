package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsEnglishClass.class)
public class ChipsEnglishClassPersistence extends StaticMySQLPersistence<ChipsEnglishClass, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishClass document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishClass.ck_product_id(document.getProductId()));
        dimensions.add(ChipsEnglishClass.ck_id(document.getId()));
        dimensions.add(ChipsEnglishClass.ck_teacher(document.getTeacher()));
    }

    @CacheMethod
    public List<ChipsEnglishClass> loadByProductId(@CacheParameter("PID") String productId) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("PRODUCT_ID").is(productId);
        return query(Query.query(criteria));
    }

    public List<ChipsEnglishClass> loadAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<ChipsEnglishClass> loadByTeacherName(String teacherName) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("TEACHER").is(teacherName);
        return query(Query.query(criteria));
    }

    public void insertOrUpdate(ChipsEnglishClass document) {
        ChipsEnglishClass upsert = upsert(document);
        if (upsert != null) {
            cleanCache(upsert);
        }
    }

    private void cleanCache(ChipsEnglishClass document) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsEnglishClass.ck_product_id(document.getProductId()));
        cacheIds.add(ChipsEnglishClass.ck_id(document.getId()));
        cacheIds.add(ChipsEnglishClass.ck_teacher(document.getTeacher()));
        getCache().deletes(cacheIds);
    }

    public void disableByClazzId(Long clazzId) {
        ChipsEnglishClass clazz = load(clazzId);
        Update update = new Update();
        update.set("UPDATETIME", new Date()).set("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(clazzId).and("DISABLED").is(false);
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0L) {
            cleanCache(clazz);
        }
    }

    public void updateProductId(ChipsEnglishClass clazz , String productId) {
        Update update = new Update();
        update.set("UPDATETIME", new Date()).set("PRODUCT_ID", productId);
        Criteria criteria = Criteria.where("ID").is(clazz.getId());
        long res = executeUpdate(update, criteria, getTableName());
        if (res > 0L) {
            cleanCache(clazz, productId);
        }
    }

    private void cleanCache(ChipsEnglishClass document,String productId) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsEnglishClass.ck_product_id(productId));//替换后的产品
        cacheIds.add(ChipsEnglishClass.ck_product_id(document.getProductId()));//原产品
        cacheIds.add(ChipsEnglishClass.ck_id(document.getId()));
        cacheIds.add(ChipsEnglishClass.ck_teacher(document.getTeacher()));
        getCache().deletes(cacheIds);
    }

}
