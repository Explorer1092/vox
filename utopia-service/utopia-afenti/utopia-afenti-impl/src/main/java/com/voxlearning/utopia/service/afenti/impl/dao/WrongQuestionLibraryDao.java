package com.voxlearning.utopia.service.afenti.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/5/19
 */
@Named
@UtopiaCacheSupport(WrongQuestionLibrary.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class WrongQuestionLibraryDao extends AlpsDynamicMongoDao<WrongQuestionLibrary, String> {

    private static final int COLLECTION_COUNT = 1000;

    @Override
    protected void calculateCacheDimensions(WrongQuestionLibrary source, Collection<String> dimensions) {
        dimensions.add(WrongQuestionLibrary.generateCacheKeyById(source.getId()));
        dimensions.add(WrongQuestionLibrary.generateCacheKeyByUserIdAndSubject(source.getUserId(), source.getSubject()));
    }

    @Override
    protected String calculateDatabase(String template, WrongQuestionLibrary entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, WrongQuestionLibrary entity) {
        if (entity == null || entity.getId() == null) return null;

        String[] elements = StringUtils.split(entity.getId(), ".");
        if (elements.length != 3) return null;

        long mod = SafeConverter.toLong(elements[0]) % COLLECTION_COUNT;
        return StringUtils.formatMessage(template, mod);
    }

    @UtopiaCacheable
    public List<WrongQuestionLibrary> findByUserIdAndSubject(@UtopiaCacheKey(name = "userId") Long userId,
                                                             @UtopiaCacheKey(name = "subject") Subject subject) {
        if (userId == null || subject == null || subject == Subject.UNKNOWN) return Collections.emptyList();
        String pattern = StringUtils.formatMessage("^{}\\.{}\\.", userId, subject);
        Criteria criteria = Criteria.where("_id").regex(Pattern.compile(pattern)).and("disabled").is(false);
        MongoNamespace namespace = calculateIdMongoNamespace(WrongQuestionLibrary.generateId(userId, subject, "1"));
        MongoConnection connection = createMongoConnection(namespace);
        return executeQuery(connection, Query.query(criteria));
    }

    public WrongQuestionLibrary updateState(String id, AfentiState state, String seid) {
        if (StringUtils.isBlank(id) || null == state) return null;
        WrongQuestionLibrary lib = new WrongQuestionLibrary();
        lib.setId(id);
        lib.setState(state);
        lib.setUpdateAt(new Date());
        if (StringUtils.isNotBlank(seid)) lib.setSeid(seid);
        return replace(lib);
    }

    public WrongQuestionLibrary disableLibrary(String id) {
        if (StringUtils.isBlank(id)) return null;
        WrongQuestionLibrary lib = new WrongQuestionLibrary();
        lib.setId(id);
        lib.setDisabled(true);
        return replace(lib);
    }

    public void disableLibrary(Collection<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        String id = ids.stream().findFirst().orElse("");
        String[] idArray = id.split("\\.");
        Long userId = SafeConverter.toLong(idArray[0]);
        Subject subject = Subject.safeParse(idArray[1]);
        MongoNamespace namespace = calculateIdMongoNamespace(WrongQuestionLibrary.generateId(userId, subject, "1"));
        MongoConnection connection = createMongoConnection(namespace);
        Criteria crt =  Criteria.where("_id").in(ids);
        updateMany(connection, crt,  Update.update("disabled", true).set("updateAt", new Date()));
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(WrongQuestionLibrary.generateCacheKeyByUserIdAndSubject(userId, subject));
        ids.forEach(e -> {
            cacheIds.add(WrongQuestionLibrary.generateCacheKeyById(e));
        });
        getCache().deletes(cacheIds);
    }
}
