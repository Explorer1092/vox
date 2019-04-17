package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.bson.BsonConverter;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserDepartment;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mizar User DAO class
 * Created by alex on 2016/8/16.
 */
@Named
@CacheBean(type = MizarUserDepartment.class)
public class MizarUserDepartmentDao extends AlpsStaticMongoDao<MizarUserDepartment, String> {

    @Override
    protected void calculateCacheDimensions(MizarUserDepartment document, Collection<String> dimensions) {
        dimensions.add(MizarUserDepartment.ck_user(document.getUserId()));
        dimensions.add(MizarUserDepartment.ck_department(document.getDepartmentId()));
    }

    @CacheMethod
    public List<MizarUserDepartment> findByUser(@CacheParameter(value = "uid") String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").is(userId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toList());
    }

    public List<MizarUserDepartment> findByUserIds(Collection<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").in(userIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        return new ArrayList<>(query(query));
    }

    @CacheMethod
    public List<MizarUserDepartment> findByDepartment(@CacheParameter(value = "did") String departmentId) {
        if (StringUtils.isBlank(departmentId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("departmentId").is(departmentId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toList());
    }

    @CacheMethod
    public Map<String, List<MizarUserDepartment>> findByDepartments(@CacheParameter(value = "did", multiple = true) Collection<String> departmentIds) {
        if (CollectionUtils.isEmpty(departmentIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("departmentId").in(departmentIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(MizarUserDepartment::getDepartmentId));
    }

    public MizarUserDepartment disableUserDepartment(final String userId, final String departmentId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("departmentId").is(departmentId).and("disabled").is(false);
        Bson filter = criteriaTranslator.translate(criteria);
        Update update = Update.update("disabled", true);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).findOneAndUpdate(filter, updateTranslator.translate(update));
        MizarUserDepartment userDepartment = BsonConverter.fromBsonDocument(document, getDocumentClass());
        if (userDepartment != null) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(userDepartment, cacheKeys);
            getCache().delete(cacheKeys);
        }

        return userDepartment;
    }


}
