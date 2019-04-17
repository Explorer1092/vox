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

package com.voxlearning.utopia.service.business.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.NoCacheAsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.ExamAnswer;

import javax.inject.Named;
import java.util.List;

/**
 * Created by shiwei.liao on 2015/6/5.
 */
@Named("com.voxlearning.utopia.service.business.impl.persistence.ExamAnswerPersistence")
public class ExamAnswerPersistence extends NoCacheAsyncStaticMongoPersistence<ExamAnswer, String> {

    public List<ExamAnswer> findByExamId(String examId) {
        Criteria criteria = Criteria.where("eid").is(examId);
        Query query = Query.query(criteria).with(new Sort(Sort.Direction.ASC, "aid"));
        return query(query);
    }
}
