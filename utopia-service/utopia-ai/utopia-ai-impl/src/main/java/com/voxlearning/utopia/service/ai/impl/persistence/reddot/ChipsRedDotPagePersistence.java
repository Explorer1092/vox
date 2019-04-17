package com.voxlearning.utopia.service.ai.impl.persistence.reddot;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.reddot.ChipsRedDotPage;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Named
@CacheBean(type = ChipsRedDotPage.class)
public class ChipsRedDotPagePersistence extends StaticMySQLPersistence<ChipsRedDotPage, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsRedDotPage document, Collection<String> dimensions) {
        dimensions.add(ChipsRedDotPage.ck_code(document.getCode()));
        dimensions.add(ChipsRedDotPage.ck_parent(document.getParent()));
    }

    @Override
    public ChipsRedDotPage load(Long id) {
        return super.$load(id);
    }

    public void insertOrUpdate(String code, String name, Long parentId) {
        ChipsRedDotPage document = new ChipsRedDotPage(code, name, parentId);
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0");
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            ChipsRedDotPage task = loadByCode(code);
            cleanCache(task);
        }
    }

    @CacheMethod
    public ChipsRedDotPage loadByCode(@CacheParameter(value = "C") String code) {
        Criteria criteria = Criteria.where("CODE").is(code).and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATETIME");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<ChipsRedDotPage> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @CacheMethod
    public List<ChipsRedDotPage> loadByParentPage(@CacheParameter(value = "P") Long parentPage) {
        Criteria criteria = Criteria.where("PARENT").is(parentPage).and("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    private void cleanCache(ChipsRedDotPage dotPage) {
        if (dotPage == null) {
            return;
        }
        Set<String> cacheIds = new HashSet<>();
        calculateCacheDimensions(dotPage, cacheIds);
        getCache().deletes(cacheIds);
    }
}
