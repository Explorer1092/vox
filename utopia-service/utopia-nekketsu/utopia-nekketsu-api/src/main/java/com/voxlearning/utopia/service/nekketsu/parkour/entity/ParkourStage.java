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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UseEqualsValidateCache;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sadi.Wan on 2014/8/18.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-parkour")
@DocumentCollection(collection = "vox_nekketsu_parkour_stage")
@DocumentIndexes({
        @DocumentIndex(def = "{'wordList.stageId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151025")
@EqualsAndHashCode(of = "stageId")
@UseEqualsValidateCache
public class ParkourStage implements Serializable {
    private static final long serialVersionUID = -1904615035020815690L;

    @DocumentId private Integer stageId;
    private String topic;
    private List<ParkourWord> wordList;
    private Integer stageCoinBonus;     // 三星金币奖励数
    private List<Integer> timeForStar;  // 获取星星所需要的通关时间
    private List<Integer> exp;          // 获取星星对应的获得经验数
    private ParkourStageAi stageAi;
    private Integer barricadeCount;     // 障碍数量
    private Integer pickCountCount;     // 散落在赛道上的金币数
    private Integer failErrorCount;     // 最大答错题数，答错题目超过这个数量，本关直接失败
    private Integer distance;           // 赛道长度

    @JsonIgnore
    public List<String> getWordIdLst() {
        List<String> rtn = Collections.emptyList();
        if (wordList != null && !wordList.isEmpty()) {
            rtn = new ArrayList<>(wordList.size());
            for (ParkourWord pword : wordList) {
                rtn.add(pword.getWordId());
            }
        }
        return rtn;
    }

    public static String ck_id(Integer id) {
        return CacheKeyGenerator.generateCacheKey(ParkourStage.class, id);
    }

    public static String ck_all_count() {
        return CacheKeyGenerator.generateCacheKey(ParkourStage.class, "COUNT");
    }
}
