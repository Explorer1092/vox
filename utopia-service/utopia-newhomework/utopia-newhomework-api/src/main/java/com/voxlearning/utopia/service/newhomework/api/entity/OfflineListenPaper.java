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

package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 纸质试卷线下听力
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-exam")
@DocumentCollection(collection = "offlineListenPaper")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20150807")
public class OfflineListenPaper implements Serializable {
    private static final long serialVersionUID = 479529859340648587L;

    @DocumentId private String id;                      //主键
    private String title;
    private Integer totalTime;                          //时长(秒)
    private List<String> fileIds;                       //听力文件IDs
    private String comment;                             //备注
    @DocumentField("pid") private String paperId;       //标准试卷ID
    @DocumentField("ctId") private String creatorId;
    @DocumentField("utId") private String updatorId;
    @DocumentField("ct") @DocumentCreateTimestamp private Date createTimestamp;
    @DocumentField("ut") @DocumentUpdateTimestamp private Date updateTimestamp;

    public static String cacheKeyFromId(String id) {
        return CacheKeyGenerator.generateCacheKey(OfflineListenPaper.class, id);
    }
}
