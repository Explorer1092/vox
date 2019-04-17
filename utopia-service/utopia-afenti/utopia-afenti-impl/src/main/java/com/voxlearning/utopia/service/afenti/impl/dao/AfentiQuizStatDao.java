package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.DynamicCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Ruib
 * @since 2016/10/12
 */
@Named
@CacheBean(type = AfentiQuizStat.class)
public class AfentiQuizStatDao extends DynamicCacheDimensionDocumentJdbcDao<AfentiQuizStat, Long> {

    @Override
    protected String calculateTableName(String template, AfentiQuizStat document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getUserId());
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            // keep back compatibility
            long mod = document.getUserId() % 2;
            return StringUtils.formatMessage(template, mod);
        } else {
            long mod = document.getUserId() % 10;
            return StringUtils.formatMessage(template, mod);
        }
    }

    @CacheMethod
    public List<AfentiQuizStat> queryByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        String tableName = getTableName(userId);
        return executeQuery(Query.query(criteria), tableName);
    }

    public boolean updateScoreAndSilver(AfentiQuizStat stat) {
        String tableName = getTableName(stat.getUserId());
        Criteria criteria = Criteria.where("ID").is(stat.getId()).and("USER_ID").is(stat.getUserId());
        Update update = Update.update("SCORE", stat.getScore()).set("SILVER", stat.getSilver());
        long rows = executeUpdate(update, criteria, tableName);
        if (rows > 0) {
            String key = CacheKeyGenerator.generateCacheKey(AfentiQuizStat.class,
                    new String[]{"UID"}, new Object[]{stat.getUserId()}, new Object[]{null});
            ChangeCacheObject<List<AfentiQuizStat>> modifier = currentValue -> {
                AfentiQuizStat document = currentValue.stream()
                        .filter(e -> Objects.equals(stat.getId(), e.getId()))
                        .findFirst()
                        .orElse(null);
                if (document == null) {
                    throw new UnsupportedOperationException();
                }
                document.setScore(stat.getScore());
                document.setSilver(stat.getSilver());
                document.setUpdateTime(new Date());
                return currentValue;
            };
            CacheValueModifierExecutor<List<AfentiQuizStat>> executor = getCache().createCacheValueModifier();
            executor.key(key)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(modifier)
                    .execute();
        }
        return rows > 0;
    }

    // ========================================================================
    // internal methods
    // ========================================================================

    private String getTableName(Long userId) {
        AfentiQuizStat mock = new AfentiQuizStat();
        mock.setUserId(userId);
        return getDocumentTableName(mock);
    }
}
