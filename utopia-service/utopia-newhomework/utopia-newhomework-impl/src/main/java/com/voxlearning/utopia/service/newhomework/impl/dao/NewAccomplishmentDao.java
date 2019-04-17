/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkShardMongoHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2016-01-07
 */
@Named
public class NewAccomplishmentDao {
    @Inject private NewAccomplishmentAsyncDao newAccomplishmentAsyncDao;
    @Inject private NewAccomplishmentShardDao newAccomplishmentShardDao;

    public NewAccomplishment load(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        if (NewHomeworkShardMongoHelper.isShardAccomplishmentId(id)) {
            return newAccomplishmentShardDao.load(id);
        }
        return newAccomplishmentAsyncDao.load(id);
    }

    public Map<String, NewAccomplishment> loads(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Map<String, NewAccomplishment> accomplishmentMap = new HashMap<>();
        Set<String> shardIds = new HashSet<>();
        Set<String> asyncIds = new HashSet<>();
        for (String id : ids) {
            if (NewHomeworkShardMongoHelper.isShardAccomplishmentId(id)) {
                shardIds.add(id);
            } else {
                asyncIds.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(shardIds)) {
            Map<String, NewAccomplishment> accomplishmentShardMap = newAccomplishmentShardDao.loads(shardIds);
            accomplishmentShardMap.forEach(accomplishmentMap::put);
        }
        if (CollectionUtils.isNotEmpty(asyncIds)) {
            Map<String, NewAccomplishment> accomplishmentAsyncMap = newAccomplishmentAsyncDao.loads(asyncIds);
            accomplishmentAsyncMap.forEach(accomplishmentMap::put);
        }
        return MapUtils.resort(accomplishmentMap, ids);
    }

    /**
     * 学生完成指定的作业
     *
     * @param id             Accomplishment的ID，包含作业详情
     * @param studentId      学号
     * @param accomplishDate 完成时间，如果为null则取当前时间
     * @param ip             学生完成作业时前端送来的IP地址(optional)
     * @param repair         是否补做(optional)
     * @param clientType     端类型，参见HomeworkSourceType
     * @param clientName     端名称
     */
    public void studentFinished(NewAccomplishment.ID id,
                                Long studentId,
                                Date accomplishDate,
                                String ip,
                                Boolean repair,
                                String clientType,
                                String clientName) {
        if (NewHomeworkShardMongoHelper.isShardAccomplishmentId(id.toString())) {
            newAccomplishmentShardDao.studentFinished(id, studentId, accomplishDate, ip, repair, clientType, clientName);
        } else {
            newAccomplishmentAsyncDao.studentFinished(id, studentId, accomplishDate, ip, repair, clientType, clientName);
        }
    }
}
