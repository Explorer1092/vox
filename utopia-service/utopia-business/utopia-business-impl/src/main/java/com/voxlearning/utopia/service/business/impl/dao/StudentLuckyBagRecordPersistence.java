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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.activity.StudentLuckyBagRecord;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/1/27.
 */
@Named
@CacheBean(type = StudentLuckyBagRecord.class)
public class StudentLuckyBagRecordPersistence extends AlpsStaticJdbcDao<StudentLuckyBagRecord, Long> {

    @Override
    protected void calculateCacheDimensions(StudentLuckyBagRecord document, Collection<String> dimensions) {
        dimensions.add(StudentLuckyBagRecord.ck_senderId(document.getSenderId()));
        dimensions.add(StudentLuckyBagRecord.ck_receiverId(document.getReceiverId()));
    }

    @CacheMethod
    public List<StudentLuckyBagRecord> loadBySenderId(@CacheParameter("senderId") Long senderId) {
        Criteria criteria = Criteria.where("SENDER_ID").is(senderId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<StudentLuckyBagRecord>> loadBySenderIds(@CacheParameter(value = "senderId", multiple = true) List<Long> senderIds) {
        Criteria criteria = Criteria.where("SENDER_ID").in(senderIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(StudentLuckyBagRecord::getSenderId));
    }

    @CacheMethod
    public Map<Long, StudentLuckyBagRecord> loadByReceiverIds(@CacheParameter(value = "receiverId", multiple = true) List<Long> receiverIds) {
        Criteria criteria = Criteria.where("RECEIVER_ID").in(receiverIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(StudentLuckyBagRecord::getReceiverId))
                .values()
                .stream()
                .map(e -> e.stream().findFirst().orElse(null))
                .filter(e -> e != null)
                .collect(Collectors.toMap(StudentLuckyBagRecord::getReceiverId, Function.identity()));
    }


    public void openLuckyBag(Long id) {
        StudentLuckyBagRecord original = $load(id);
        if (original == null) {
            return;
        }
        Update update = Update.update("STATUS", "OPEN");
        Criteria criteria = Criteria.where("ID").is(id);
        if ($update(update, criteria) > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
    }
}
