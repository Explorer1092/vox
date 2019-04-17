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
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.DateFormatParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Sadi.Wan on 2014/8/29.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-parkour")
@DocumentCollection(collection = "vox_nekketsu_parkour_login_prize_month_conf")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151028")
public class LoginPrizeMonthConf implements Serializable, Comparable<LoginPrizeMonthConf> {
    private static final long serialVersionUID = -6792411866285416558L;

    @DocumentId private Integer id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    private Map<Gender, List<String>> prizeItemId;  // key:MALE,FEMAIL;value:itemId

    public LoginPrizeMonthConf initializeIfNecessary() {
        if (getPrizeItemId() == null) setPrizeItemId(new LinkedHashMap<>());
        return this;
    }

    public static String ck_id(Integer id) {
        return CacheKeyGenerator.generateCacheKey(LoginPrizeMonthConf.class, id);
    }

    public void setSelfIdByNow() {
        id = genIdByNow();
    }

    public static int genIdByNow() {
        return Integer.parseInt(DateFormatParser.getInstance().format(Calendar.getInstance().getTime(), "yyyyMM"));
    }

    public static int genIdByCalendar(Calendar cal) {
        return Integer.parseInt(DateFormatParser.getInstance().format(cal.getTime(), "yyyyMM"));
    }

    public static int genIdNextMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        return Integer.parseInt(DateFormatParser.getInstance().format(cal.getTime(), "yyyyMM"));
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") LoginPrizeMonthConf o) {
        int i1 = (id == null ? 0 : id);
        int i2 = (o.id == null ? 0 : o.id);
        return Integer.compare(i1, i2);
    }
}
