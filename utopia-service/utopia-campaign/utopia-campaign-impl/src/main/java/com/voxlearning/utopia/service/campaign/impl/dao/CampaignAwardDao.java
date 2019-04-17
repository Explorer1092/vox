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
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.campaign.api.document.CampaignAward;

import javax.inject.Named;
import java.util.List;

/**
 * DAO implementation of {@link CampaignAward}.
 *
 * @author Xiaohai Zhang
 * @since Aug 6, 2016
 */
@Named("com.voxlearning.utopia.service.campaign.impl.dao.CampaignAwardDao")
@CacheBean(type = CampaignAward.class)
public class CampaignAwardDao extends StaticCacheDimensionDocumentJdbcDao<CampaignAward, Long> {

    @CacheMethod
    public List<CampaignAward> findCampaignAwards(@CacheParameter("CID") Integer campaignId,
                                                  @CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("CAMPAIGN_ID").is(campaignId);
        return query(Query.query(criteria));
    }
}
