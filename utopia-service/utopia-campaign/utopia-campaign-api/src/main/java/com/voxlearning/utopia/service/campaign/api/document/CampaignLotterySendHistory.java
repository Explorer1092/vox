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
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 抽奖次数赠送历史
 *
 * @author Summer Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Mar 15, 2016
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_CAMPAIGN_LOTTERY_SEND_HISTORY")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160805")
public class CampaignLotterySendHistory extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 4691421080432207641L;

    private Integer campaignId;            // 活动ID
    private Integer count;                 // 赠送抽奖次数
    private Long senderId;                 // 发送人ID
    private String senderName;             // 发送人姓名
    private Long receiverId;               // 接收人ID

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"CID", "RID"}, new Object[]{campaignId, receiverId})
        };
    }
}
