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
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named("com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord")
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

    public Page<TwoFourPointEntityRecord> loadPage(String code, Integer page, Integer pageSize) {
        int realPage = page <= 0 ? 0 : page;
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Page<TwoFourPointEntityRecord>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    Query query = new Query(Criteria.where("code").is(code));
                    long count = executeCount(getMongoConnection(code), query);
                    List<TwoFourPointEntityRecord> data = executeQuery(getMongoConnection(code), query.skip(realPage * pageSize).limit(pageSize));
                    return new PageImpl<>(data, new PageRequest(realPage, pageSize), count);
                })
                .execute();
    }
}
