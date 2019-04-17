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
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;

import javax.inject.Named;
import java.util.List;

/**
 * DAO implementation of {@link CampaignLotteryBigHistory}.
 *
 * @author Summer Yang
 * @author Xiaohai Zhang
 * @since Mar 15, 2016
 */
@Named("com.voxlearning.utopia.service.campaign.impl.dao.CampaignLotteryBigHistoryDao")
@CacheBean(type = CampaignLotteryBigHistory.class)
public class CampaignLotteryBigHistoryDao extends StaticCacheDimensionDocumentJdbcDao<CampaignLotteryBigHistory, Long> {

    @CacheMethod
    public List<CampaignLotteryBigHistory> loadByCampaignId(@CacheParameter("CID") Integer campaignId) {
        Criteria criteria = Criteria.where("CAMPAIGN_ID").is(campaignId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<CampaignLotteryBigHistory> loadByCampaignIdAndAwardId(@CacheParameter("CID") Integer campaignId,
                                                                      @CacheParameter("AID") Integer awardId) {
        Criteria criteria = Criteria.where("CAMPAIGN_ID").is(campaignId)
                .and("AWARD_ID").is(awardId);
        return query(Query.query(criteria));
    }

    @UtopiaCacheable(key = "ALL")
    public List<CampaignLotteryBigHistory> loadAllHistorys() {
        return query();
    }

    @CacheMethod
    public List<CampaignLotteryBigHistory> loadByUserId(@CacheParameter("UID") Long userId){
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(Query.query(criteria));
    }
}
