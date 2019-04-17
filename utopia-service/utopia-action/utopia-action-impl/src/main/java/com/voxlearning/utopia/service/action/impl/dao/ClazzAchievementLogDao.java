package com.voxlearning.utopia.service.action.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.document.ClazzAchievementLog;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author xinxin
 * @since 26/8/2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.ClazzAchievementLogDao")
@CacheBean(type = ClazzAchievementLog.class)
@CacheDimension(value = CacheDimensionDistribution.OTHER_FIELDS)
public class ClazzAchievementLogDao extends DynamicCacheDimensionDocumentMongoDao<ClazzAchievementLog, String> {
    @Override
    protected String calculateDatabase(String template, ClazzAchievementLog document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, ClazzAchievementLog document) {
        //ID: clazz-AchievementType-AchievementLevel
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());

        String[] ids = document.getId().split("-");
        if (ids.length != 3) throw new IllegalArgumentException();
        Long clazzId = SafeConverter.toLong(ids[0], -1);
        if (-1 == clazzId) throw new IllegalArgumentException();

        return StringUtils.formatMessage(template, clazzId % 100);
    }

    @CacheMethod
    public List<ClazzAchievementLog> findByClazzId(@CacheParameter("CID") Long clazzId) {
        String mockId = SafeConverter.toLong(clazzId) + "-SenSuanZi-2";
        MongoNamespace mongoNamespace = calculateIdMongoNamespace(mockId);
        MongoConnection connection = createMongoConnection(mongoNamespace);

        Pattern pattern = Pattern.compile("^" + clazzId + "-");

        Criteria criteria = Criteria.where("_id").regex(pattern);

        return executeQuery(connection, Query.query(criteria));
    }

}
