package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsDynamicJdbDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.reward.entity.DebrisHistory;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Named
@CacheBean(type = DebrisHistory.class)
public class DebrisHistoryDao extends AlpsDynamicJdbDao<DebrisHistory, String> {

    @Override
    protected void calculateCacheDimensions(DebrisHistory document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    protected String calculateTableName(String template, DebrisHistory document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getUserId());

        long i = (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) ? 2 : 100;
        long mod = document.getUserId() % i;
        return StringUtils.formatMessage(template, mod);
    }

    @CacheMethod
    public List<DebrisHistory> loadByUserId(@CacheParameter("U") Long userId) {
        if (userId == null) return null;

        Criteria criteria = Criteria.where("USER_ID").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");

        DebrisHistory mock = new DebrisHistory();
        mock.setUserId(userId);
        String tableName = getDocumentTableName(mock);
        return executeQuery(Query.query(criteria).with(sort), tableName);
    }

    private String getTableName(Long userId) {
        DebrisHistory mock = new DebrisHistory();
        mock.setUserId(userId);
        return getDocumentTableName(mock);
    }
}
