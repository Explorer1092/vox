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
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Created by Sadi.Wan on 2014/8/18.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-parkour")
@DocumentCollection(collection = "vox_nekketsu_parkour_personal_stage_record")
@DocumentIndexes({
        @DocumentIndex(def = "{'stageId':1,'latestPlayTime':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151026")
public class PersonalStageRecord implements Serializable, Comparable<PersonalStageRecord> {
    private static final long serialVersionUID = 5360203091273267005L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;
    private Long roleId;
    private Integer stageId;        // 关卡ID
    private Integer personalBest;   // 最快通关时间 毫秒
    private Date personalBestAchieveTime;   // 最快通关时间取得日期
    private Integer starBest;   // 最多星星数
    private Date startBestAchieveTime;  // 最多星星数取得日期
    private Double correctRate; // 最高正确率
    private Date bestCorrectRateAchieveTime;    // 最高正确率取得日期
    private Integer timePerQuestion;    // 每题耗时平均值。只在取得最高正确率时更新此字段
    private Date latestPlayTime;    // 最近一次玩本关的时间
    private Set<WordPuzzle> achievedPuzzle; // 本关获得的单词碎片

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(PersonalStageRecord.class, id);
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") PersonalStageRecord o) {
        int pb1 = (personalBest == null ? 0 : personalBest);
        int pb2 = (o.personalBest == null ? 0 : o.personalBest);
        int ret = Integer.compare(pb1, pb2);
        if (ret != 0) return ret;
        long t1 = (personalBestAchieveTime == null ? 0 : personalBestAchieveTime.getTime());
        long t2 = (o.personalBestAchieveTime == null ? 0 : o.personalBestAchieveTime.getTime());
        ret = Long.compare(t1, t2);
        if (ret != 0) return ret;
        long r1 = (roleId == null ? 0 : roleId);
        long r2 = (o.roleId == null ? 0 : o.roleId);
        return Long.compare(r1, r2);
    }
}
