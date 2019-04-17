package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.bson.BsonConverter;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mizar User DAO class
 * Created by alex on 2016/8/16.
 */
@Named
@UtopiaCacheSupport(MizarUser.class)
public class MizarUserDao extends StaticCacheDimensionDocumentMongoDao<MizarUser, String> {

    @UtopiaCacheable(key = "ALL")
    public List<MizarUser> findAll() {
        return query();
    }

    @UtopiaCacheable
    public MizarUser findById(@UtopiaCacheKey String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        Criteria criteria = Criteria.where("_id").is(id).and("status").ne(9);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @UtopiaCacheable
    public Map<String, MizarUser> findByIds(@UtopiaCacheKey(multiple = true) Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        Criteria criteria = Criteria.where("_id").in(ids).and("status").ne(9);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toMap(MizarUser::getId, Function.identity()));
    }

    @UtopiaCacheable
    public MizarUser findByAccount(@UtopiaCacheKey(name = "ACCOUNT") String accountName) {
        if (StringUtils.isBlank(accountName)) {
            return null;
        }
        Criteria criteria = Criteria.where("accountName").is(accountName).and("status").ne(9);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }


    @UtopiaCacheable
    public MizarUser findByMobile(@UtopiaCacheKey(name = "MOBILE") String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return null;
        }
        Criteria criteria = Criteria.where("mobile").is(mobile).and("status").ne(9);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    public MizarUser closeAccount(final String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Bson filter = criteriaTranslator.translate(criteria);
        Update update = new Update();
        update.set("status", 9);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).findOneAndUpdate(filter, updateTranslator.translate(update));
        MizarUser user = BsonConverter.fromBsonDocument(document, getDocumentClass());
        if (user != null) {
            evictDocumentCache(user);
        }
        return user;
    }

}
