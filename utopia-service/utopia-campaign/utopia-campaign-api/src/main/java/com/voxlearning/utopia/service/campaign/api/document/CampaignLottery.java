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
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Campaign lottery data structure.
 *
 * @author Alex
 * @serial
 * @since Oct 21, 2014
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_CAMPAIGN_LOTTERY")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170418")
public class CampaignLottery extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 1366224143345561305L;

    private Integer campaignId;        // 活动ID
    private Integer awardId;           // 奖品ID
    private String awardLevelName;     // 奖品级别
    private String awardName;          // 奖品名称
    private String awardContent;       // 奖品内容
    private Integer awardRate;         // 中奖率，万为单位的整数
    private Integer totalAwardLimit;   // 总中奖数量
    private Integer remainAwardNum;    // 剩余中奖数量
    private Date startTime;            // 开始时间
    private Date endTime;              // 结束时间

    @DocumentFieldIgnore
    private Integer sort;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ID", id),
                newCacheKey("CID", campaignId)
        };
    }
}
