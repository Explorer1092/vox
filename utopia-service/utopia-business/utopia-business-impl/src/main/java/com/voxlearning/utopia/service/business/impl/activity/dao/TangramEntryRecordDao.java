package com.voxlearning.utopia.service.business.impl.activity.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.entity.activity.ActivityShardingUtils;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named("com.voxlearning.utopia.service.business.impl.activity.dao.TangramEntryRecordDao")
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

    public Page<TangramEntryRecord> loadPage(String activityCode, Integer page, Integer pageSize) {
        int realPage = page <= 0 ? 0 : page;
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Page<TangramEntryRecord>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    Query query = new Query(Criteria.where("activityCode").is(activityCode));
                    long count = executeCount(getMongoConnection(activityCode), query);
                    List<TangramEntryRecord> data = executeQuery(getMongoConnection(activityCode), query.skip(realPage * pageSize).limit(pageSize));
                    return new PageImpl<>(data, new PageRequest(realPage, pageSize), count);
                })
                .execute();
    }

}
