package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadCollection;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;

/**
 * 跟读作品集
 *
 * @author jiangpeng
 * @since 2017-03-10 下午8:49
 **/
@CacheBean(type = FollowReadCollection.class)
@Named
public class FollowReadCollectionDao extends DynamicCacheDimensionDocumentMongoDao<FollowReadCollection, String> {


    public FollowReadCollectionDao(){
        registerBeforeInsertListener(documents -> documents.stream()
                .filter(d -> d.getId() == null)
                .forEach(FollowReadCollection::generateId));
    }

    @Override
    protected String calculateDatabase(String template, FollowReadCollection document) {
        return null;
    }

    @Override
    protected String calculateCollection(String s, FollowReadCollection document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] segments = StringUtils.split(document.getId(), "-");
        if (segments.length != 2) throw new IllegalArgumentException();
        long mod = SafeConverter.toLong(segments[0]) % 10;
        return StringUtils.formatMessage(s, mod);
    }


    @CacheMethod
    public List<FollowReadCollection> findByUserId(@CacheParameter("SID") Long studentId) {
        MongoConnection connection = getMongoConnection(studentId);
        Criteria criteria = Criteria.where("studentId").is(studentId);
        return executeQuery(connection, Query.query(criteria));
    }


    private MongoConnection getMongoConnection(Long userId) {
        String mockId = userId + "-000000000000000000000000-";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

}
