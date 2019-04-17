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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.entity.activity.ActivityShardingUtils;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;

import javax.inject.Named;
import java.util.*;

@Named
@CacheBean(type = SudokuUserRecord.class, useValueWrapper = true)
public class SudokuUserRecordDao extends AlpsDynamicMongoDao<SudokuUserRecord, String> {

    @Override
    protected void calculateCacheDimensions(SudokuUserRecord document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    protected String calculateDatabase(String template, SudokuUserRecord document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, SudokuUserRecord document) {
        String tableSuffix = ActivityShardingUtils.getTableSuffixById(document.getId());
        return StringUtils.formatMessage(template, tableSuffix);
    }

    private MongoConnection getMongoConnection(String activityId) {
        MongoNamespace mongoNamespace = getDocumentTableName(activityId);
        return createMongoConnection(mongoNamespace);
    }

    private MongoNamespace getDocumentTableName(String activityId) {
        SudokuUserRecord mock = new SudokuUserRecord();
        mock.setId(activityId + "-0000");
        return calculateDocumentMongoNamespace(mock);
    }

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();

        registerBeforeInsertListener(documents ->
                documents.stream()
                        .filter(d -> d.getId() == null)
                        .forEach(SudokuUserRecord::generateId)
        );
    }

    @CacheMethod
    public List<SudokuUserRecord> loadByUser(@CacheParameter("AID") String activityId, @CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        Query query = Query.query(criteria).with(new Sort("createTime"));
        return executeQuery(getMongoConnection(activityId), query);
    }

    @Override
    public SudokuUserRecord upsert(SudokuUserRecord document) {
        SudokuUserRecord result = $upsert(document);

        if (result != null) {
            String ck = SudokuUserRecord.ckActivityIdUserId(result.getActivityId(), result.getUserId());
            if (getCache().load(ck) != null) {
                ChangeCacheObject<List<SudokuUserRecord>> modifier = recordList -> {
                    SudokuUserRecord exists = recordList.stream().filter(e -> Objects.equals(e.getId(), result.getId())).findAny().orElse(null);
                    if (exists == null) {
                        recordList.add(0, result);
                    } else {
                        recordList.remove(exists);
                        recordList.add(result);
                    }

                    Collections.sort(recordList, (o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime()));

                    return recordList;
                };
                CacheValueModifierExecutor<List<SudokuUserRecord>> executor = getCache().createCacheValueModifier();
                executor.key(ck)
                        .expiration(getDefaultCacheExpirationInSeconds())
                        .modifier(modifier)
                        .execute();
            }
        }

        return result;
    }

    public Long loadAllCountByActivityId(String activityId) {
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Long>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    Criteria criteria = Criteria.where("activityId").is(activityId);
                    return executeCount(getMongoConnection(activityId), Query.query(criteria));
                })
                .execute();
    }

    public List<SudokuUserRecord> loadAllByActivityId(String activityId) {
        return RoutingPolicyExecutorBuilder.getInstance()
                .<List<SudokuUserRecord>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    Criteria criteria = Criteria.where("activityId").is(activityId);
                    return executeQuery(getMongoConnection(activityId), Query.query(criteria));
                })
                .execute();
    }

}
