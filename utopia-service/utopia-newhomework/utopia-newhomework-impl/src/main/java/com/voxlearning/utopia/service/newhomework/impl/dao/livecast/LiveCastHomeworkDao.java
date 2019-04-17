package com.voxlearning.utopia.service.newhomework.impl.dao.livecast;

import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.bson.BsonConverter;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.exception.CannotCreateHomeworkException;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Named
@UtopiaCacheSupport(LiveCastHomework.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class LiveCastHomeworkDao extends AlpsStaticMongoDao<LiveCastHomework, String> {

    @Override
    protected void calculateCacheDimensions(LiveCastHomework document, Collection<String> dimensions) {
        dimensions.add(LiveCastHomework.ck_id(document.getId()));
        dimensions.add(LiveCastHomework.ck_clazzGroupId(document.getClazzGroupId()));
    }

    public void insert(LiveCastHomework entity) {
        long createTime;
        if (entity.getCreateAt() != null) {
            createTime = entity.getCreateAt().getTime();
        } else {
            createTime = new Date().getTime();
        }

        String month = MonthRange.newInstance(createTime).toString();
        String id = new LiveCastHomework.ID(month).toString();
        entity.setId(id);
        $insert(entity);

        LiveCastHomework liveCastHomework = $load(entity.getId());
        if (liveCastHomework == null) {
            throw new CannotCreateHomeworkException("Failed to write LiveCastHomework to the database");
        }

        // 缓存处理
        Map<String, LiveCastHomework> map = new LinkedHashMap<>();
        map.put(LiveCastHomework.ck_id(entity.getId()), entity);
        getCache().safeAdds(map, getDefaultCacheExpirationInSeconds());

        String ck_cg = LiveCastHomework.ck_clazzGroupId(entity.getClazzGroupId());
        getCache().createCacheValueModifier()
                .key(ck_cg)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToSet(currentValue, entity.toLocation()))
                .execute();
    }

    public void inserts(Collection<LiveCastHomework> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        for (LiveCastHomework liveCastHomework : entities) {
            insert(liveCastHomework);
        }
    }

    @CacheMethod
    public Map<Long, List<LiveCastHomework.Location>> loadLiveCastHomeworkByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> clazzGroupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(clazzGroupIds).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        query.field().includes("_id", "type", "teacherId", "clazzGroupId", "createAt", "startTime", "endTime", "actionId", "subject", "includeSubjective");

        Map<Long, List<LiveCastHomework.Location>> ret = query(query).stream()
                .map(LiveCastHomework::toLocation)
                .collect(Collectors.groupingBy(LiveCastHomework.Location::getClazzGroupId));
        return clazzGroupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    public Boolean updateDisabledTrue(String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }
        Criteria criteria = Criteria.where("_id").is(homeworkId);
        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("disabled", Boolean.TRUE);
        BsonDocument filter = criteriaTranslator.translate(criteria);
        BsonDocument updateBson = updateTranslator.translate(update);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER)
                .upsert(false);
        MongoConnection connection = createMongoConnection();
        BsonDocument document = connection.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter, updateBson, options);

        LiveCastHomework modified = BsonConverter.fromBsonDocument(document, getDocumentClass());
        if (Objects.nonNull(modified)) {
            getCache().createCacheValueModifier()
                    .key(LiveCastHomework.ck_id(homeworkId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_cg = LiveCastHomework.ck_clazzGroupId(modified.getClazzGroupId());
            List<LiveCastHomework.Location> cacheModify = new ArrayList<>();
            List<LiveCastHomework.Location> cacheLocations = getCache().load(ck_cg);
            if (CollectionUtils.isNotEmpty(cacheLocations)) {
                cacheModify.addAll(cacheLocations);
                for (LiveCastHomework.Location location : cacheLocations) {
                    if (StringUtils.equalsIgnoreCase(location.getId(), modified.getId())) {
                        cacheModify.remove(location);
                        break;
                    }
                }
            }

            getCache().createCacheValueModifier()
                    .key(ck_cg)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> cacheModify)
                    .execute();
        }

        return Objects.nonNull(modified);
    }
}
