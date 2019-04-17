package com.voxlearning.utopia.service.vendor.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyEntryGlobalMsg;

import javax.inject.Named;
import java.util.List;

/**
 * @author jiangpeng
 * @since 2017-06-21 上午11:56
 **/
@Named
public class MySelfStudyEntryReminderDao extends DynamicCacheDimensionDocumentMongoDao<MySelfStudyEntryGlobalMsg, String> {

    @Override
    protected String calculateDatabase(String template, MySelfStudyEntryGlobalMsg document) {
        return null;
    }

    /**
     * 利用分表,区分环境。
     * staging一个表,线上一个表
     * @param s
     * @param document
     * @return
     */
    @Override
    protected String calculateCollection(String s, MySelfStudyEntryGlobalMsg document) {
        Mode current = RuntimeMode.current();
        String env;
        if (current == Mode.STAGING)
            env = "_staging";
        else
            env = "";
        return StringUtils.formatMessage(s, env);
    }

    public List<MySelfStudyEntryGlobalMsg> getAll(){
        MongoConnection connection = getMongoConnection();
        Criteria criteria = Criteria.where("_id").exists();
        return executeQuery(connection, Query.query(criteria));
    }

    private MongoConnection getMongoConnection() {
        MongoNamespace namespace = calculateIdMongoNamespace("ttt");
        return createMongoConnection(namespace);
    }
}
