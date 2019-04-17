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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNoticeHistory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * @author HuanYin Jia
 * @since 2015/5/22
 */
@Named
public class WechatNoticeHistoryPersistence extends NoCacheStaticMySQLPersistence<WechatNoticeHistory, Long> {

    public WechatNoticeHistoryPersistence() {
        registerBeforeInsertListener(documents -> documents.forEach(e -> {
            if (e.getDisabled() == null) e.setDisabled(false);
        }));
    }

    public List<WechatNoticeHistory> listAllByUserId(Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("DISABLED").is(false);
        Query query = Query.query(criteria)
                .with(new Sort(Sort.Direction.DESC, "CREATE_DATETIME"));
        return query(query);
    }

    public int deleteByCreateDatetime(Date date, int limit) {
        String iDate = DateUtils.dateToString(date);
        return getDataSourceConnection().getTransactionTemplate().execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus status) {
                return getDataSourceConnection().getJdbcTemplate()
                        .update("DELETE FROM VOX_WECHAT_NOTICE_HISTORY WHERE CREATE_DATETIME<? LIMIT ?", iDate, limit);
            }
        });
    }

}
