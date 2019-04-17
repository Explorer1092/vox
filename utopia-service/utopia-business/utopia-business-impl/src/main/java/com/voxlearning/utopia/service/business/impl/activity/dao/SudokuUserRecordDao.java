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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.activity.ActivityShardingUtils;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named("com.voxlearning.utopia.service.business.impl.activity.dao.SudokuUserRecordDao")
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

    public Page<SudokuUserRecord> loadPage(String activityId, Integer page, Integer pageSize) {
        int realPage = page <= 0 ? 0 : page;
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Page<SudokuUserRecord>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    Sort sort = new Sort("userId");
                    Query query = new Query(Criteria.where("activityId").is(activityId)).with(sort);
                    long count = executeCount(getMongoConnection(activityId), query);
                    List<SudokuUserRecord> data = executeQuery(getMongoConnection(activityId), query.skip(realPage * pageSize).limit(pageSize));
                    return new PageImpl<>(data, new PageRequest(realPage, pageSize), count);
                })
                .execute();
    }
}
