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
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * latestLoginDay: 最近一次登录天。每个月的几号。例如最后一次是1月30号登录，此字段为30.用来保证不在同一天内多次发放奖品
 * Created by Sadi.Wan on 2014/8/27.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-parkour")
@DocumentCollection(collection = "vox_nekketsu_parkour_login_prize")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151028")
public class ParkourLoginPrize implements Serializable, Comparable<ParkourLoginPrize> {
    private static final long serialVersionUID = 8872676019117901445L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    private Long userId;
    private Integer latestLoginDay;
    private List<LoginPrizeDetail> prizeDetailList;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ParkourLoginPrize.class, id);
    }

    public ParkourLoginPrize initializeIfNecessary() {
        if (getUserId() == null) setUserId(0L);
        if (getLatestLoginDay() == null) setLatestLoginDay(0);
        if (getPrizeDetailList() == null) setPrizeDetailList(new LinkedList<>());
        return this;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") ParkourLoginPrize o) {
        long c1 = (createTime == null ? 0 : createTime.getTime());
        long c2 = (o.createTime == null ? 0 : o.createTime.getTime());
        return Long.compare(c1, c2);
    }
}
