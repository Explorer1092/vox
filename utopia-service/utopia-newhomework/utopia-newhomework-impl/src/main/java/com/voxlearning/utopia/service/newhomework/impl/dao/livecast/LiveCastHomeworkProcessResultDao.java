package com.voxlearning.utopia.service.newhomework.impl.dao.livecast;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.alps.lang.support.RangeableIdVersion;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;

import javax.inject.Named;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Named
@UtopiaCacheSupport(LiveCastHomeworkProcessResult.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class LiveCastHomeworkProcessResultDao extends DynamicCacheDimensionDocumentMongoDao<LiveCastHomeworkProcessResult, String> {

    public LiveCastHomeworkProcessResultDao() {
        registerBeforeInsertListener(documents -> documents.forEach(document -> {
            String id = document.getId();
            if (RangeableIdVersion.V1.parse(id) == null) {
                throw new IllegalArgumentException();
            }
        }));
    }

    @Override
    protected String calculateDatabase(String template, LiveCastHomeworkProcessResult document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, LiveCastHomeworkProcessResult document) {
        RangeableId rangeableId = RangeableIdVersion.V1.parse(document.getId());
        Objects.requireNonNull(rangeableId);
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.M).toString());
    }

    /**
     * 更新老师对题的批改状态
     * 参数有点多，原因是为了避免许多校验带来的性能损耗，直接在update的时候都做了。
     *
     * @param id          id
     * @param hid         作业id
     * @param qid         试题id
     * @param userId      做题人的id
     * @param teacherMark 老师评语
     * @return boolean
     */
    public Boolean updateCorrection(String id,
                                    String hid,
                                    String qid,
                                    Long userId,
                                    Double score,
                                    String teacherMark,
                                    String correctionImg,
                                    String correctionVoice,
                                    Double percentage
                                    ) {

        Criteria criteria = Criteria.where("_id").is(id);

        Update update = new Update();

        // 得分
        if (score >= 0) {
            update = update.set("score", score);
        }
        // 评语
        if (StringUtils.isNotBlank(teacherMark)) {
            update = update.set("teacherMark", teacherMark);
        }
        // 批改
        if (StringUtils.isNotBlank(correctionImg)) {
            update = update.set("correctionImg", correctionImg);
        }
        if (StringUtils.isNotBlank(correctionVoice)) {
            update = update.set("correctionVoice", correctionVoice);
        }
        if (percentage >= 0) {
            update = update.set("percentage", percentage);
        }
        update = update.currentDate("updateAt");
        update.set("review",true);

        MongoNamespace namespace = calculateIdMongoNamespace(id);
        MongoConnection connection = createMongoConnection(namespace);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        LiveCastHomeworkProcessResult modified = executeFindOneAndUpdate(connection, criteria, update, options);

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(CacheKeyGenerator.generateCacheKey(LiveCastHomeworkProcessResult.class, id))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }

        return modified != null;
    }
}
