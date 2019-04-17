package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadSentenceResult;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 跟读打分结果到
 *
 * @author jiangpeng
 * @since 2017-03-10 下午5:48
 **/
@Named
@CacheBean(type = FollowReadSentenceResult.class)
public class FollowReadSentenceResultDao extends DynamicCacheDimensionDocumentMongoDao<FollowReadSentenceResult, String> {

    public FollowReadSentenceResultDao(){
        registerBeforeInsertListener(documents -> documents.stream()
                .filter(d -> d.getId() == null)
                .forEach(FollowReadSentenceResult::generateId));
    }

    @Override
    protected String calculateDatabase(String template, FollowReadSentenceResult document) {
        return null;
    }

    /**
     * 只在 job 使用,没有缓存,请勿在业务使用。
     * 只查30天内的
     * @param studentId
     * @return
     */
    @Deprecated
    public List<FollowReadSentenceResult> loadByStudentId(Long studentId){
        MongoConnection mongoConnection = getMongoConnection(studentId);
        Pattern pattern = Pattern.compile("^" + studentId + "-");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        Date thirtyDaysAgo = DateUtils.calculateDateDay(DayRange.current().getStartDate(), -30);
        Criteria criteria1 = Criteria.where("createTime").gte(thirtyDaysAgo);
        Query query = Query.query(Criteria.and(criteria, criteria1));
        return executeQuery(mongoConnection, query);
    }


    @Override
    protected String calculateCollection(String s, FollowReadSentenceResult document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] segments = StringUtils.split(document.getId(), "-");
        if (segments.length != 2) throw new IllegalArgumentException();
        long mod = SafeConverter.toLong(segments[0]) % 100;
        return StringUtils.formatMessage(s, mod);
    }


    private MongoConnection getMongoConnection(Long userId) {
        String mockId = userId + "-000000000000000000000000-";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

}
