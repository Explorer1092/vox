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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.business.api.constant.TuckerTitle;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;

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
@DocumentCollection(collection = "vox_weekly_homework_report")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180424")
public class TuckerWeeklyHomeworkReport implements CacheDimensionDocument {
    private static final long serialVersionUID = -2237659722551908121L;

    public static final DateRange CalculateRange = new DateRange(
            DateUtils.stringToDate("2018-03-12 00:00:00"),
            DateUtils.stringToDate("2018-04-15 23:59:59")
    );

    @DocumentId private Long id;            // 老师主账号ID
    private Long mathTeacherId;             // 数学老师ID
    private Long schoolId;                  // 学校ID
    private Boolean success;                // 是否达成
    private Integer currentLevel;           // 当前称号
    private Integer formerLevel;            // 之前称号

    List<ClazzTuckerHomeworkInfo> classHomeworkInfo;

    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public TuckerWeeklyHomeworkReport() {

    }

    public TuckerWeeklyHomeworkReport(Long teacherId) {
        this.id = teacherId;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public TuckerTitle currentTitle() {
        return TuckerTitle.parse(currentLevel);
    }

    public TuckerTitle formerTitle() {
        return TuckerTitle.parse(formerLevel);
    }

    /**
     * [至少一个班检查作业>=3次]&[每次作业完成人数>=20人]
     */
    public boolean accomplishTuckerTask() {
        return CollectionUtils.isNotEmpty(classHomeworkInfo)
                && classHomeworkInfo.stream().anyMatch(ClazzTuckerHomeworkInfo::accomplish);
    }

    public long getAccomplishNum(){
        // 所有作业汇总在一起，查人数，每天只记录满足条件的一条
        Set<String> assignTimeSet = new HashSet<>();
        return Optional.ofNullable(classHomeworkInfo)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(chi -> Optional.ofNullable(chi.getHomeworkList())
                        .orElse(Collections.emptyList())
                        .stream())
                .filter(hw -> hw.getAccomplishCount() >= ClazzTuckerHomeworkInfo.AccomplishCount)
                // 同一天只记录一次
                .filter(hw -> assignTimeSet.add(dateToString(hw.getAssignTime(),FORMAT_SQL_DATE)))
                .count();
    }

    public void levelUp() {
        currentLevel++;
    }

    public boolean hasLevelUp() {
        return currentLevel > formerLevel;
    }
}
