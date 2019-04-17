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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.business.api.entity.BizStudentVoice;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named("com.voxlearning.utopia.service.business.impl.persistence.BizStudentVoicePersistence")
public class BizStudentVoicePersistence extends NoCacheAsyncStaticMongoPersistence<BizStudentVoice, String> {

    public BizStudentVoicePersistence() {
        registerBeforeInsertListener(documents -> documents.stream()
                .filter(e -> e.getDataState() == null)
                .forEach(e -> e.setDataState("ENABLED")));
    }

    public List<BizStudentVoice> findByClazzIds(Collection<Long> clazzIds, Integer limit) {
        Criteria criteria = Criteria.where("clazzId").in(clazzIds);
        Query query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, "voiceTime"));
        int l = SafeConverter.toInt(limit);
        if (l > 0) {
            query = query.limit(l);
        }
        return query(query);
    }
}
