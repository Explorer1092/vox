package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.client.result.DeleteResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.newhomework.api.entity.UnreadHomeworkComment;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 老的没删，重新写一个异步的
 *
 * @author xuesong.zhang
 * @since 2017/8/17
 */
@Named
@CacheBean(type = UnreadHomeworkComment.class)
public class NewUnreadHomeworkCommentDao extends AsyncStaticMongoPersistence<UnreadHomeworkComment, String> {
    @Override
    protected void calculateCacheDimensions(UnreadHomeworkComment document, Collection<String> dimensions) {
        dimensions.add(UnreadHomeworkComment.ck_studentId(document.getStudentId()));
        dimensions.add(UnreadHomeworkComment.ck_studentId_count(document.getStudentId()));
    }

    public void inserts(Collection<UnreadHomeworkComment> entities) {
        super.$inserts(entities);
        entities.stream()
                .collect(Collectors.groupingBy(UnreadHomeworkComment::getStudentId))
                .forEach((sid, list) -> {
                    String key = UnreadHomeworkComment.ck_studentId(sid);
                    ChangeCacheObject<List<UnreadHomeworkComment>> modifier = currentValue -> {
                        currentValue = new ArrayList<>(currentValue);
                        currentValue.addAll(list);
                        return currentValue;
                    };

                    CacheValueModifierExecutor<List<UnreadHomeworkComment>> executor = getCache().createCacheValueModifier();
                    executor.key(key)
                            .expiration(getDefaultCacheExpirationInSeconds())
                            .modifier(modifier)
                            .execute();

                    key = UnreadHomeworkComment.ck_studentId_count(sid);

                    getCache().createCacheValueModifier()
                            .key(key)
                            .expiration(getDefaultCacheExpirationInSeconds())
                            .modifier(currentValue -> SafeConverter.toLong(currentValue) + list.size())
                            .execute();
                });
    }

    @CacheMethod
    public List<UnreadHomeworkComment> findByStudentId(@CacheParameter(value = "S") Long studentId) {
        Criteria criteria = Criteria.where("sid").is(studentId);
        return $executeQuery(createMongoConnection(), Query.query(criteria)).getUninterruptibly();
    }

    @CacheMethod(validateMethodNamePrefix = false)
    public long countByStudentId(@CacheParameter(value = "C") Long studentId) {
        Criteria criteria = Criteria.where("sid").is(studentId);
        return $executeCount(createMongoConnection(), Query.query(criteria)).getUninterruptibly();
    }

    public void deleteByStudentId(Long studentId) {
        Criteria criteria = Criteria.where("sid").is(studentId);
        DeleteResult deleteResult = $executeRemove(createMongoConnection(), Query.query(criteria)).getUninterruptibly();
        if (deleteResult.getDeletedCount() > 0) {
            String key = UnreadHomeworkComment.ck_studentId(studentId);
            getCache().createCacheValueModifier()
                    .key(key)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> Collections.emptyList())
                    .execute();

            key = UnreadHomeworkComment.ck_studentId_count(studentId);
            getCache().createCacheValueModifier()
                    .key(key)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> 0L)
                    .execute();
        }
    }
}
