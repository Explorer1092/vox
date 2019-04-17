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

package com.voxlearning.utopia.agent.dao;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.api.constant.CrmContactType;
import com.voxlearning.utopia.api.constant.CrmTaskRecordCategory;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/10/24
 */
@Named("agent.CrmTaskRecordDao")
public class CrmTaskRecordDao extends StaticMongoDao<CrmTaskRecord, String> {

    @Override
    protected void calculateCacheDimensions(CrmTaskRecord source, Collection<String> dimensions) {
    }

    public Page<CrmTaskRecord> smartFind(Date createStart, Date createEnd, Collection<String> recorders, Collection<CrmContactType> contactTypes, Collection<CrmTaskRecordCategory> firstCategories, CrmTaskRecordCategory secondCategory, CrmTaskRecordCategory thirdCategory, UserType userType, Pageable pageable) {
        Filter filter = filterBuilder.build();
        smartFilter(filter, "createTime", createStart, createEnd);
        filterIn(filter, "recorder", recorders);
        filterIn(filter, "contactType", contactTypes);
        filterIn(filter, "firstCategory", firstCategories);
        filterIs(filter, "secondCategory", secondCategory);
        filterIs(filter, "thirdCategory", thirdCategory);
        filterIs(filter, "userType", userType);
        return find(filter, pageable);
    }

    public List<CrmTaskRecord> smartFindAll(Date createStart, Date createEnd, Collection<String> recorders, Collection<CrmContactType> contactTypes, Collection<CrmTaskRecordCategory> firstCategories, CrmTaskRecordCategory secondCategory, CrmTaskRecordCategory thirdCategory, UserType userType) {
        Filter filter = filterBuilder.build();
        smartFilter(filter, "createTime", createStart, createEnd);
        filterIn(filter, "recorder", recorders);
        filterIn(filter, "contactType", contactTypes);
        filterIn(filter, "firstCategory", firstCategories);
        filterIs(filter, "secondCategory", secondCategory);
        filterIs(filter, "thirdCategory", thirdCategory);
        filterIs(filter, "userType", userType);
        return findAll(filter);
    }

    public Page<CrmTaskRecord> smartFind(Date createStart, Date createEnd, String recorder, CrmContactType contactType, Long userId, Pageable pageable) {
        Filter filter = filterBuilder.build();
        smartFilter(filter, "createTime", createStart, createEnd);
        filterIs(filter, "recorder", recorder);
        filterIs(filter, "contactType", contactType);
        filterIs(filter, "userId", userId);
        return find(filter, pageable);
    }

    public List<CrmTaskRecord> findUserIdIs(Long userId) {
        Filter filter = filterBuilder.where("userId").is(userId);
        return findAll(filter);
    }

    public List<CrmTaskRecord> findUserIdIn(Collection<Long> userIds) {
        Filter filter = filterBuilder.build();
        filterIn(filter, "userId", userIds);
        return findAll(filter);
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

    private void smartFilter(Filter filter, String key, Object foot, Object top) {
        if (foot != null && top != null) {
            filter.and(key).gte(foot).lt(top);
        } else if (foot != null) {
            filter.and(key).gte(foot);
        } else if (top != null) {
            filter.and(key).lt(top);
        }
    }

    public CrmTaskRecord updateTaskId(String id, String taskId) {
        Update update = updateBuilder.build().set("taskId", taskId);
        return __update_OTF(id, update);
    }

    public List<CrmTaskRecord> findByAgentTaskDetailId(String agentTaskId) {
        Filter filter = filterBuilder.where("agentTaskId").is(agentTaskId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return __find_OTF(Find.find(filter).with(sort));
    }

    public Long countByTaskDetailId(String agentTaskId) {
        Filter filter = filterBuilder.where("agentTaskId").is(agentTaskId);
        return count(filter);
    }

    public List<CrmTaskRecord> loadByRecorderAndTaskId(String recorder, String taskId) {
        Filter filter = filterBuilder.where("recorder").is(recorder);
        filterIs(filter, "taskId", taskId);
        return findAll(filter);
    }

    private Page<CrmTaskRecord> find(Filter filter, Pageable pageable) {
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    private List<CrmTaskRecord> findAll(Filter filter) {
        return __find_OTF(filter.toBsonDocument());
    }

    private long count(Filter filter) {
        return __count_OTF(filter.toBsonDocument());
    }
}
