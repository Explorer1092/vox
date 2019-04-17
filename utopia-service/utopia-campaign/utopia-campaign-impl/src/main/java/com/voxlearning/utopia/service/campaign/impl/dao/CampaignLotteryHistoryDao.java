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

package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.DynamicCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;

import javax.inject.Named;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * DAO implementation of {@link CampaignLotteryHistory}.
 *
 * @author Xiaohai Zhang
 * @since Aug 5, 2016
 */
@Named("com.voxlearning.utopia.service.campaign.impl.dao.CampaignLotteryHistoryDao")
@CacheBean(type = CampaignLotteryHistory.class)
public class CampaignLotteryHistoryDao extends DynamicCacheDimensionDocumentJdbcDao<CampaignLotteryHistory, Long> {

    @Override
    protected String calculateTableName(String template, CampaignLotteryHistory document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getUserId());
        long mod;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            // keep back compatibility
            mod = document.getUserId() % 2;
        } else {
            mod = document.getUserId() % 100;
        }
        return StringUtils.formatMessage(template, mod);
    }

    @CacheMethod
    public List<CampaignLotteryHistory> findCampaignLotteryHistories(@CacheParameter("CID") Integer campaignId,
                                                                     @CacheParameter("UID") Long userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, -1);       // within recent 1 month
        Date startDate = calendar.getTime();

        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("CAMPAIGN_ID").is(campaignId)
                .and("CREATE_DATETIME").gte(startDate);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");

        CampaignLotteryHistory mock = new CampaignLotteryHistory();
        mock.setUserId(userId);
        String tableName = getDocumentTableName(mock);
        return executeQuery(Query.query(criteria).with(sort), tableName);
    }
}
