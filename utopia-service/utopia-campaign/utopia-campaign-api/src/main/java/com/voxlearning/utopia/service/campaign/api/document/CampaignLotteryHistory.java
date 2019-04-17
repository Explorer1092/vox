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

package com.voxlearning.utopia.service.campaign.api.document;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 抽奖历史
 *
 * @author Alex Hu
 * @author Summer Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Oct 21, 2014
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_CAMPAIGN_LOTTERY_HISTORY_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160805")
public class CampaignLotteryHistory implements CacheDimensionDocument {
    private static final long serialVersionUID = 1366224143345561305L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;
    @DocumentCreateTimestamp
    private Date createDatetime;
    @DocumentUpdateTimestamp
    private Date updateDatetime;
    private Long userId;           // 用户ID
    private Integer campaignId;    // 活动ID
    private Integer awardId;       // 奖品ID

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"CID", "UID"}, new Object[]{campaignId, userId})
        };
    }
}
