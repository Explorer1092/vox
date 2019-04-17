package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.service.vendor.api.entity.ParentShareTextRead;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.mongodb.client.model.ReturnDocument.AFTER;

/**
 * Created by jiangpeng on 16/7/18.
 */
@Named
@UtopiaCacheSupport(ParentShareTextRead.class)
public class ParentShareTextReadDao extends AlpsStaticMongoDao<ParentShareTextRead, String> {


    @Override
    protected void calculateCacheDimensions(ParentShareTextRead source, Collection<String> dimensions) {
        dimensions.add(ParentShareTextRead.generateCacheKey(source.getParentIdMd5()));
    }


    @CacheMethod(
            type = ParentShareTextRead.class
    )
    public ParentShareTextRead loadByParentIdFileMd5(@CacheParameter(value = "PID") Long parentId, @CacheParameter(value = "MD5") String fileMd5) {
        if (parentId == null || StringUtils.isBlank(fileMd5))
            return null;
        Criteria criteriaParentId = Criteria.where("parent_id").is(parentId);
        Criteria criteriaMd5 = Criteria.where("file_md5").is(fileMd5);
        Criteria andCriteria = Criteria.and(criteriaParentId, criteriaMd5);

        Query query = Query.query(andCriteria);
        return query(query).stream().findFirst().orElse(null);

    }


    public ParentShareTextRead loadIfPresentElseInsertByPidMd5(ParentShareTextRead parentShareTextRead) {
        if (parentShareTextRead == null) {
            return null;
        }
        Criteria criteriaParentId = Criteria.where("parent_id").is(parentShareTextRead.getParentId());
        Criteria criteriaMd5 = Criteria.where("file_md5").is(parentShareTextRead.getFileMd5());
        Criteria andCriteria = Criteria.and(criteriaParentId, criteriaMd5);
        Bson filter = criteriaTranslator.translate(andCriteria);
        BsonDocument update = new BsonDocument("$setOnInsert", convertDocument(parentShareTextRead));
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER);
        MongoConnection connection = createMongoConnection();
        BsonDocument bsonDocument = connection.collection.findOneAndUpdate(filter, update, options);
        ParentShareTextRead parentShareTextRead1 = convertBsonDocument(bsonDocument);
        Set<String> keys = new HashSet<>();
        calculateCacheDimensions(parentShareTextRead1, keys);;
        getCache().delete(keys);
        return parentShareTextRead1;
    }

}
