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

package com.voxlearning.utopia.service.nekketsu.parkour.entity;


import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sadi.Wan on 2014/8/20.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-parkour")
@DocumentCollection(collection = "vox_nekketsu_parkour_region_rank")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151027")
public class ParkourRegionRank implements Serializable {
    private static final long serialVersionUID = -7922033694222386119L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;              // id格式： regionCode_stageId
    private Integer regionCode;
    private Integer stageId;
    private List<ParkourRankDetail> rankList;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ParkourRegionRank.class, id);
    }

    public ParkourRegionRank initializeIfNecessary() {
        if (getRegionCode() == null) setRegionCode(0);
        if (getStageId() == null) setStageId(0);
        if (getRankList() == null) setRankList(new LinkedList<>());
        return this;
    }
}
