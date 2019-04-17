/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * @author yeuchen.wang
 * @since 2017/03/21.
 */

@Named
@CacheBean(type = AppPushWfMessage.class)
public class AppPushWfMessageDao extends StaticCacheDimensionDocumentMongoDao<AppPushWfMessage, String> {

    public AppPushWfMessage findByRecordId(Long recordId) {
        if (recordId == null || recordId <= 0L) {
            return null;
        }
        Criteria criteria = Criteria.where("recordId").is(recordId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public int updateStatus(Long recordId, String status) {
        Criteria criteria = Criteria.where("recordId").is(recordId);
        Update update = Update.update("status", status);
        return (int) executeUpdateOne(createMongoConnection(), criteria, update);
    }

    public List<AppPushWfMessage> findToBeSent() {
        Criteria criteria = Criteria.where("sendTime").lte(new Date())
                .and("status").is("processed").and("sendStatus").is("waiting");
        return query(Query.query(criteria));
    }
}
