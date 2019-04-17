/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.push.impl.persistence;

import com.mongodb.client.result.DeleteResult;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.NoCacheAsyncStaticMongoPersistence;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.push.api.entity.AppJpushMessageRetry;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.util.List;

/**
 * @author shiwe.liao
 * @since 2016/1/18
 */
@Named("com.voxlearning.utopia.service.push.impl.persistence.AppJpushMessageRetryPersistence")
public class AppJpushMessageRetryPersistence extends NoCacheAsyncStaticMongoPersistence<AppJpushMessageRetry, ObjectId> {

    public List<AppJpushMessageRetry> loadRetryList() {
        Criteria criteria = Criteria.where("status").is(0)
                .and("retryCount").lt(4);
        Query query = Query.query(criteria)
                .with(new Sort(Sort.Direction.ASC, "updateTime"))
                .limit(3000);
        return query(query);
    }


    public void updateRetrySuccess(ObjectId id) {
        AppJpushMessageRetry inst = new AppJpushMessageRetry();
        inst.setId(id);
        inst.setStatus(1);
        replace(inst);
    }

    public void updateRetryFailed(ObjectId id, Integer retryCount) {
        AppJpushMessageRetry inst = new AppJpushMessageRetry();
        inst.setId(id);
        inst.setRetryCount(retryCount);
        replace(inst);
    }

    public void updateRetryFailed(ObjectId id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update()
                .inc("retryCount", 1)
                .currentDate("updateTime");
        $executeFindOneAndUpdate(createMongoConnection(), criteria, update).awaitUninterruptibly();
    }

    //删除历史消息
    public MapMessage cleanUp(Long time) {
        if (time == null || time == 0) {
            return MapMessage.errorMessage("删除日期错误");
        }
        Criteria criteria = Criteria.where("createTime").lt(time);
        DeleteResult deleteResult = $executeRemove(createMongoConnection(), Query.query(criteria))
                .getUninterruptibly();
        if (deleteResult.getDeletedCount() == 0) {
            return MapMessage.errorMessage("删除历史消息不成功");
        }
        return MapMessage.successMessage().add("count", deleteResult.getDeletedCount());
    }
}
