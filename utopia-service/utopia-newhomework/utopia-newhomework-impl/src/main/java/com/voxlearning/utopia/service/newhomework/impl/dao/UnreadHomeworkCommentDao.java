/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.client.result.DeleteResult;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.exception.IllegalMongoEntityException;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.newhomework.api.entity.UnreadHomeworkComment;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mongo dao implementation of {@link UnreadHomeworkComment}.
 *
 * @author Xiaohai Zhang
 * @author xuesong.zhang
 * @since Oct 19, 2015
 * @deprecated use {@link NewUnreadHomeworkCommentDao} instead.
 */
@Named
@UtopiaCacheSupport(UnreadHomeworkComment.class)
@Deprecated
public class UnreadHomeworkCommentDao extends StaticMongoDao<UnreadHomeworkComment, String> {

    @Override
    protected void calculateCacheDimensions(UnreadHomeworkComment source, Collection<String> dimensions) {
        dimensions.add(UnreadHomeworkComment.ck_studentId(source.getStudentId()));
        dimensions.add(UnreadHomeworkComment.ck_studentId_count(source.getStudentId()));
    }

    @Override
    protected void preprocessEntity(UnreadHomeworkComment entity) {
        super.preprocessEntity(entity);
        if (entity.getStudentId() == null) throw new IllegalMongoEntityException("Student id must not be null");
    }

    @Override
    public void inserts(Collection<UnreadHomeworkComment> entities) {
        super.__inserts_OTF(entities);

        entities.stream()
                .collect(Collectors.groupingBy(UnreadHomeworkComment::getStudentId))
                .entrySet()
                .forEach(e -> {
                    Long sid = e.getKey();
                    List<UnreadHomeworkComment> list = e.getValue();

                    String key = UnreadHomeworkComment.ck_studentId(sid);
                    ChangeCacheObject<List<UnreadHomeworkComment>> modifier = currentValue -> {
                        currentValue = new ArrayList<>(currentValue);
                        currentValue.addAll(list);
                        return currentValue;
                    };
                    getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), modifier);

                    key = UnreadHomeworkComment.ck_studentId_count(sid);
                    getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                            currentValue -> SafeConverter.toLong(currentValue) + list.size());
                });
    }

    /**
     * @deprecated use {@link NewUnreadHomeworkCommentDao#findByStudentId} instead.
     */
    @Deprecated
    @UtopiaCacheable
    public List<UnreadHomeworkComment> findByStudentId(@UtopiaCacheKey(name = "S") Long studentId) {
        Filter filter = filterBuilder.where("sid").is(studentId);
        Find find = Find.find(filter);
        return __find_OTF(find);
    }

    /**
     * @deprecated use {@link NewUnreadHomeworkCommentDao#countByStudentId} instead.
     */
    @Deprecated
    @UtopiaCacheable(validateMethodNamePrefix = false)
    public long countByStudentId(@UtopiaCacheKey(name = "C") Long studentId) {
        Filter filter = filterBuilder.where("sid").is(studentId);
        Find find = Find.find(filter);
        return __count_OTF(find);
    }

    /**
     * @deprecated use {@link NewUnreadHomeworkCommentDao#deleteByStudentId} instead.
     */
    @Deprecated
    public void deleteByStudentId(Long studentId) {
        Filter filter = filterBuilder.where("sid").is(studentId);
        DeleteResult deleteResult = createMongoConnection().collection.deleteMany(filter.toBsonDocument());
        if (deleteResult.getDeletedCount() > 0) {
            String key = UnreadHomeworkComment.ck_studentId(studentId);
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    currentValue -> Collections.emptyList());

            key = UnreadHomeworkComment.ck_studentId_count(studentId);
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    currentValue -> 0L);
        }
    }
}
