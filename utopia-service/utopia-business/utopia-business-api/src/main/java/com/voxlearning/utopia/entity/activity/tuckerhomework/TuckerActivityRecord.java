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

package com.voxlearning.utopia.entity.activity.tuckerhomework;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 2018春季开学老师端活动
 *
 * @author yuechen.wang
 * @since 2018-02-02
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_tucker_activity_record_new")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180424")
public class TuckerActivityRecord implements CacheDimensionDocument {
    private static final long serialVersionUID = -2237659722551908121L;

    public static final DateRange ActivityRange;
    static {
        if(RuntimeMode.isProduction())
            ActivityRange = new DateRange(
                    DateUtils.stringToDate("2018-04-27 00:00:00"),
                    DateUtils.stringToDate("2018-05-14 03:00:00")
            );
        else{
            ActivityRange = new DateRange(
                    DateUtils.stringToDate("2018-04-25 00:00:00"),
                    DateUtils.stringToDate("2018-05-01 23:59:59")
            );
        }
    }

    @DocumentId private String id;
    private Long teacherId;            // 老师主账号ID
    private Long schoolId;             // 学校ID
    private String activityCode;       // 活动代码
    private Integer currentLevel;      // 当前等级
    private List<Integer> finishWeek;  // 完成周
    private Boolean awardReceived;     // 奖励领取情况
    private Long lastPushTime;         // 记录上一次发push的时间

    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public TuckerActivityRecord() {

    }

    public TuckerActivityRecord(Long teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public void updateCurrentLevel(int level) {
        if (currentLevel == null || currentLevel == 0 || currentLevel <= level) {
            currentLevel = level;
        }
    }

    public void updateFinishWeek(int week) {
        if (finishWeek == null) finishWeek = new ArrayList<>();
        if (!finishWeek.contains(week)) finishWeek.add(week);
    }

}
