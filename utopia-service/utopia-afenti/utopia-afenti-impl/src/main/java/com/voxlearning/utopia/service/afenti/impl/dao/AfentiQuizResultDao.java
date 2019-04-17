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
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Ruib
 * @since 2016/10/13
 */
@Named
@CacheBean(type = AfentiQuizResult.class)
public class AfentiQuizResultDao extends DynamicCacheDimensionDocumentJdbcDao<AfentiQuizResult, Long> {

    @Override
    protected String calculateTableName(String template, AfentiQuizResult document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getUserId());
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            // keep back compatibility
            long mod = document.getUserId() % 2;
            return StringUtils.formatMessage(template, mod);
        } else {
            long mod = document.getUserId() % 100;
            return StringUtils.formatMessage(template, mod);
        }
    }

    @CacheMethod
    public List<AfentiQuizResult> queryByUserIdAndNewBookId(@CacheParameter("UID") Long userId,
                                                            @CacheParameter("NBID") String newBookId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("NEW_BOOK_ID").is(newBookId);
        String tableName = getTableName(userId);
        return executeQuery(Query.query(criteria), tableName);
    }

    public boolean updateRightAndErrorNums(AfentiQuizResult result) {
        String tableName = getTableName(result.getUserId());
        Criteria criteria = Criteria.where("ID").is(result.getId()).and("USER_ID").is(result.getUserId());
        Update update = Update.update("RIGHT_NUM", result.getRightNum()).set("ERROR_NUM", result.getErrorNum());
        long rows = executeUpdate(update, criteria, tableName);
        if (rows > 0) {
            String key = CacheKeyGenerator.generateCacheKey(AfentiQuizResult.class,
                    new String[]{"UID", "NBID"}, new Object[]{result.getUserId(), result.getNewBookId()}, new Object[]{null, ""});
            ChangeCacheObject<List<AfentiQuizResult>> modifier = currentValue -> {
                AfentiQuizResult document = currentValue.stream()
                        .filter(e -> Objects.equals(result.getId(), e.getId()))
                        .findFirst()
                        .orElse(null);
                if (document == null) {
                    throw new UnsupportedOperationException();
                }
                document.setRightNum(result.getRightNum());
                document.setErrorNum(result.getErrorNum());
                document.setUpdateTime(new Date());
                return currentValue;
            };
            CacheValueModifierExecutor<List<AfentiQuizResult>> executor = getCache().createCacheValueModifier();
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
        AfentiQuizResult mock = new AfentiQuizResult();
        mock.setUserId(userId);
        return getDocumentTableName(mock);
    }
}
