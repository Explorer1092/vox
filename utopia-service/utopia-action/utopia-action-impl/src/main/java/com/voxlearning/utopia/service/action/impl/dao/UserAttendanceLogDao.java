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
import com.voxlearning.utopia.service.action.api.document.UserAttendanceLog;

import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 19/8/2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.UserAttendanceLogDao")
@CacheBean(type = UserAttendanceLog.class, useValueWrapper = true)
@CacheDimension(value = CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class UserAttendanceLogDao extends DynamicCacheDimensionDocumentMongoDao<UserAttendanceLog, String> {
    @Override
    protected String calculateDatabase(String template, UserAttendanceLog document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());

        //ID格式:clazzId-userId-yyyyMMdd
        String[] ids = document.getId().split("-");
        if (ids.length != 3) throw new IllegalArgumentException();
        if (ids[2].length() != 8) throw new IllegalArgumentException();

        return StringUtils.formatMessage(template, ids[2].substring(0, 6));
    }

    @Override
    protected String calculateCollection(String template, UserAttendanceLog document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());

        String[] ids = document.getId().split("-");
        if (ids.length != 3) throw new IllegalArgumentException();
        Long clazzId = SafeConverter.toLong(ids[0], -1);
        if (-1 == clazzId) throw new IllegalArgumentException();

        return StringUtils.formatMessage(template, clazzId % 100);
    }

    @CacheMethod
    public List<UserAttendanceLog> findByClazzId(@CacheParameter("CID") Long clazzId) {
        Pattern pattern = Pattern.compile("^" + clazzId + "-");
        Criteria criteria = Criteria.where("_id").regex(pattern);

        return executeQuery(calculateMongoConnection(clazzId), Query.query(criteria)).stream()
                .sorted((o1, o2) -> o1.getId().compareTo(o2.getId()))
                .collect(Collectors.toList());
    }

    private MongoConnection calculateMongoConnection(Long clazzId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String day = formatter.format(LocalDateTime.now());

        String mockId = SafeConverter.toLong(clazzId) + "-0000-" + day;
        MongoNamespace mongoNamespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(mongoNamespace);
    }
}
