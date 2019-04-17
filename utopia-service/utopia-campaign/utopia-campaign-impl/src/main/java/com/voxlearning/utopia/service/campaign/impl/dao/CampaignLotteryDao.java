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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;

import javax.inject.Named;
import java.util.List;

/**
 * DAO implementation of {@link CampaignLottery}.
 *
 * @author Xiaohai Zhang
 * @since Aug 5, 2016
 */
@Named("com.voxlearning.utopia.service.campaign.impl.dao.CampaignLotteryDao")
@CacheBean(type = CampaignLottery.class)
public class CampaignLotteryDao extends StaticCacheDimensionDocumentJdbcDao<CampaignLottery, Long> {

    @CacheMethod
    public List<CampaignLottery>  findByCampaignId(@CacheParameter("CID") Integer campaignId) {
        Criteria criteria = Criteria.where("CAMPAIGN_ID").is(campaignId);
        Sort sort = new Sort(Sort.Direction.ASC, "AWARD_ID");
        return query(Query.query(criteria).with(sort));
    }

    public int updateRate(final long id, final int rate) {
        CampaignLottery original = $load(id);
        if (original == null) {
            return 0;
        }
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("AWARD_RATE", rate);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(original);
        }
        return rows;
    }

    public int incRemainAwardNum(Long id,int delta){
        CampaignLottery org = $load(id);
        if(org == null)
            return 0;

        Criteria criteria = Criteria.where("ID").is(id);

        Update update = new Update();
        update.inc("REMAIN_AWARD_NUM",delta);

        int affectRows = (int)$update(update,criteria);
        if(affectRows > 0){
            evictDocumentCache(org);
        }

        return affectRows;
    }
}
