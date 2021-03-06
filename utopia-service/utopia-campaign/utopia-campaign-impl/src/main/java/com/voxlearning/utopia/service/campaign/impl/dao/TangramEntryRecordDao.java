package com.voxlearning.utopia.service.campaign.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.utopia.entity.activity.ActivityShardingUtils;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TangramEntryRecord.class, useValueWrapper = true)
public class TangramEntryRecordDao extends AlpsDynamicMongoDao<TangramEntryRecord, String> {

    @Override
    protected void calculateCacheDimensions(TangramEntryRecord document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    protected String calculateDatabase(String template, TangramEntryRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, TangramEntryRecord document) {
        String tableSuffix = ActivityShardingUtils.getTableSuffixById(document.getId());
        return StringUtils.formatMessage(template, tableSuffix);
    }

    private MongoConnection getMongoConnection(String activityId) {
        MongoNamespace mongoNamespace = getDocumentTableName(activityId);
        return createMongoConnection(mongoNamespace);
    }

    private MongoNamespace getDocumentTableName(String activityId) {
        TangramEntryRecord mock = new TangramEntryRecord();
        mock.setId(activityId + "-0000");
        return calculateDocumentMongoNamespace(mock);
    }

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();

        registerBeforeInsertListener(documents ->
                documents.stream()
                        .filter(d -> d.getId() == null)
                        .forEach(TangramEntryRecord::generateId)
        );
    }

    @CacheMethod
    public TangramEntryRecord loadByUserId(@CacheParameter("USER_ID") Long userId, @CacheParameter("CODE") String code) {
        Criteria criteria = Criteria.where("activityCode").is(code).and("userId").is(userId);
        return executeQuery(getMongoConnection(code), Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @Override
    public TangramEntryRecord replace(TangramEntryRecord document) {
        TangramEntryRecord result = $replace(document);

        if (result != null) {
            String ck = TangramEntryRecord.ckUserCode(result.getUserId(), result.getActivityCode());
            getCache().set(ck, getDefaultCacheExpirationInSeconds(), result);
        }

        return result;
    }

    @Override
    public TangramEntryRecord upsert(TangramEntryRecord document) {
        TangramEntryRecord result = $upsert(document);

        if (result != null) {
            String ck = TangramEntryRecord.ckUserCode(result.getUserId(), result.getActivityCode());
            getCache().set(ck, getDefaultCacheExpirationInSeconds(), result);
        }

        return result;
    }

    public Long loadAllCount(String code) {
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Long>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> executeCount(getMongoConnection(code), new Query(Criteria.where("activityCode").is(code))))
                .execute();
    }

    public List<TangramEntryRecord> loadAll(String code) {
        return RoutingPolicyExecutorBuilder.getInstance()
                .<List<TangramEntryRecord>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> executeQuery(getMongoConnection(code), new Query(Criteria.where("activityCode").is(code))))
                .execute();
    }
}
