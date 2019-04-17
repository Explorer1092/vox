package com.voxlearning.utopia.service.campaign.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.entity.activity.ActivityShardingUtils;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TwoFourPointEntityRecord.class, useValueWrapper = true)
public class TwoFourPointEntityRecordDao extends AlpsDynamicMongoDao<TwoFourPointEntityRecord, String> {

    @Override
    protected void calculateCacheDimensions(TwoFourPointEntityRecord document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    protected String calculateDatabase(String template, TwoFourPointEntityRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, TwoFourPointEntityRecord document) {
        String tableSuffix = ActivityShardingUtils.getTableSuffixById(document.getId());
        return StringUtils.formatMessage(template, tableSuffix);
    }

    private MongoConnection getMongoConnection(String activityId) {
        MongoNamespace mongoNamespace = getDocumentTableName(activityId);
        return createMongoConnection(mongoNamespace);
    }

    private MongoNamespace getDocumentTableName(String activityId) {
        TwoFourPointEntityRecord mock = new TwoFourPointEntityRecord();
        mock.setId(activityId + "-0000");
        return calculateDocumentMongoNamespace(mock);
    }

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();

        registerBeforeInsertListener(documents ->
                documents.stream()
                        .filter(d -> d.getId() == null)
                        .forEach(TwoFourPointEntityRecord::generateId)
        );
    }

    @CacheMethod
    public TwoFourPointEntityRecord loadByUserId(@CacheParameter("USER_ID") Long userId, @CacheParameter("CODE") String code) {
        Criteria criteria = Criteria.where("code").is(code).and("userId").is(userId);
        return executeQuery(getMongoConnection(code), Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @Override
    public TwoFourPointEntityRecord replace(TwoFourPointEntityRecord document) {
        TwoFourPointEntityRecord result = $replace(document);

        if (result != null) {
            String ck = TwoFourPointEntityRecord.ckUserCode(result.getUserId(), result.getCode());
            getCache().set(ck, getDefaultCacheExpirationInSeconds(), result);
        }

        return result;
    }

    @Override
    public TwoFourPointEntityRecord upsert(TwoFourPointEntityRecord document) {
        TwoFourPointEntityRecord result = $upsert(document);

        if (result != null) {
            String ck = TwoFourPointEntityRecord.ckUserCode(result.getUserId(), result.getCode());
            getCache().set(ck, getDefaultCacheExpirationInSeconds(), result);
        }

        return result;
    }

    public long skipCountAdd(TwoFourPointEntityRecord record) {
        Criteria criteria = Criteria.where("code").is(record.getCode()).and("userId").is(record.getUserId());
        Update update = new Update();
        update.inc("skipCount", 1);

        return executeUpdateOne(getMongoConnection(record.getCode()), criteria, update);
    }

    public long resetCountAdd(TwoFourPointEntityRecord record) {
        Criteria criteria = Criteria.where("code").is(record.getCode()).and("userId").is(record.getUserId());
        Update update = new Update();
        update.inc("resetCount", 1);

        return executeUpdateOne(getMongoConnection(record.getCode()), criteria, update);
    }

    public Long loadAllCount(String code) {
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Long>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    Criteria criteria = Criteria.where("code").is(code);
                    return executeCount(getMongoConnection(code), Query.query(criteria));
                })
                .execute();
    }

    public List<TwoFourPointEntityRecord> loadAll(String code) {
        return RoutingPolicyExecutorBuilder.getInstance()
                .<List<TwoFourPointEntityRecord>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    Criteria criteria = Criteria.where("code").is(code);
                    return executeQuery(getMongoConnection(code), Query.query(criteria));
                })
                .execute();
    }
}
