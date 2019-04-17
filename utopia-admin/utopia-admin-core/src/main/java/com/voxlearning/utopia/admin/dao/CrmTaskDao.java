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

package com.voxlearning.utopia.admin.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.WriteConcern;
import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.CrmTaskStatus;
import com.voxlearning.utopia.api.constant.CrmTaskType;
import com.voxlearning.utopia.entity.crm.CrmTask;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/10/20
 */
@Named
public class CrmTaskDao extends StaticMongoDao<CrmTask, String> {

    @Override
    protected void calculateCacheDimensions(CrmTask source, Collection<String> dimensions) {
    }

    public Page<CrmTask> smartFind(Date createStart, Date createEnd, Date endStart, Date endEnd, Date finishStart, Date finishEnd, Collection<String> creators, Collection<String> executors, UserType userType, Collection<CrmTaskType> types, CrmTaskStatus status, Pageable pageable) {
        Filter filter = filterBuilder.build();
        smartFilter(filter, "createTime", createStart, createEnd);
        smartFilter(filter, "endTime", endStart, endEnd);
        smartFilter(filter, "updateTime", finishStart, finishEnd);
        filterIn(filter, "creator", creators);
        filterIn(filter, "executor", executors);
        filterIs(filter, "userType", userType);
        filterIn(filter, "type", types);
        filterIs(filter, "status", status);
        filterIs(filter, "disabled", false);
        return find(filter, pageable);
    }

    public List<CrmTask> findByTime(Date createStart, Date createEnd, CrmTaskType type, CrmTaskStatus status) {
        Filter filter = filterBuilder.build();
        smartFilter(filter, "createTime", createStart, createEnd);
        filterIs(filter, "type", type);
        filterIs(filter, "status", status);
        return findAll(filter);
    }

    public Page<CrmTask> smartFind(Date endStart, Date endEnd, String creator, String executor, Long userId, CrmTaskStatus status, Pageable pageable) {
        Filter filter = filterBuilder.build();
        smartFilter(filter, "endTime", endStart, endEnd);
        filterIs(filter, "creator", creator);
        filterIs(filter, "executor", executor);
        filterIs(filter, "userId", userId);
        filterIs(filter, "status", status);
        filterIs(filter, "disabled", false);
        return find(filter, pageable);
    }

    public long smartCount(Long userId, Date endStart, Date endEnd, Collection<CrmTaskStatus> statuses) {
        Filter filter = filterBuilder.build();
        filterIs(filter, "userId", userId);
        smartFilter(filter, "endTime", endStart, endEnd);
        filterIn(filter, "status", statuses);
        filterIs(filter, "disabled", false);
        return count(filter);
    }

    private void filterIs(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).is(value);
        }
    }

    private void filterIn(Filter filter, String key, Collection<?> values) {
        if (values != null) {
            filter.and(key).in(values);
        }
    }

    private void filterStatus(Filter filter, Collection<?> inValues, Collection<?> notInValues) {
        if (inValues != null || notInValues != null) {
            filter.and("status");
        }
        if (inValues != null) {
            filter.in(inValues);
        }
        if (notInValues != null) {
            filter.nin(notInValues);
        }
    }

    private void filterNe(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).ne(value);
        }
    }

    private void smartFilter(Filter filter, String key, Object foot, Object top) {
        if (foot != null && top != null) {
            filter.and(key).gte(foot).lt(top);
        } else if (foot != null) {
            filter.and(key).gte(foot);
        } else if (top != null) {
            filter.and(key).lt(top);
        }
    }

    private Page<CrmTask> find(Filter filter, Pageable pageable) {
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    private List<CrmTask> findAll(Filter filter) {
        return __find_OTF(Find.find(filter));
    }

    private long count(Filter filter) {
        return __count_OTF(filter.toBsonDocument());
    }

    public List<CrmTask> findByAgentTaskIds(Collection<String> agentTaskIds) {
        if(CollectionUtils.isEmpty(agentTaskIds)){
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.build();
        filterIn(filter, "agentTaskId", agentTaskIds);
        return __find_OTF(filter.toBsonDocument());
    }

    public MapMessage disabledCrmTask(List<String> taskDetailId) {
        MongoNamespace namespace = new MongoNamespace("vox-crm", "vox_task");
        Filter filter = filterBuilder.build();
        filterIn(filter, "agentTaskId", taskDetailId);
        Update update = updateBuilder.build();
        update.set("disabled", true);
        UpdateResult updateResult = createMongoConnection(namespace).collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .updateMany(filter.toBsonDocument(), update.toBsonDocument());
        if (updateResult.wasAcknowledged()) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("删除任务详情失败");
    }
}
