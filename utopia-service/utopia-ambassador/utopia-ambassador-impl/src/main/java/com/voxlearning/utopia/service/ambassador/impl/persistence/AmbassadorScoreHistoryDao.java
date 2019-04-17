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

package com.voxlearning.utopia.service.ambassador.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorScoreHistory;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DAO implementation of {@link AmbassadorScoreHistory}.
 *
 * @author Summer Yang
 * @since Nov 9, 2015
 */
@Named
public class AmbassadorScoreHistoryDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorScoreHistory, Long> {

    public Integer loadAmbassadorTotalScore(Long ambassadorId, Date beginDate) {
        String s = "SELECT SUM(SCORE) AS SCORE FROM VOX_AMBASSADOR_SCORE_HISTORY " +
                "WHERE AMBASSADOR_ID=? AND CREATE_DATETIME>=? AND DISABLED=FALSE";
        AtomicReference<String> sql = new AtomicReference<>(s);
        return SafeConverter.toInt(getJdbcTemplate().queryForObject(sql.get(), new Object[]{ambassadorId, beginDate}, Integer.class));
    }

    public List<AmbassadorScoreHistory> loadScoreHistory(Long ambassadorId, Date beginDate) {
        Criteria criteria = Criteria.where("AMBASSADOR_ID").is(ambassadorId)
                .and("CREATE_DATETIME").gte(beginDate)
                .and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public void disableAmbassadorScore(Long ambassadorId) {
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("AMBASSADOR_ID").is(ambassadorId);
        $update(update, criteria);
    }
}
