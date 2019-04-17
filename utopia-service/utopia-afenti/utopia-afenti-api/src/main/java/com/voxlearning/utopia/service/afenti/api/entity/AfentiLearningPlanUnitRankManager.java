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

package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.data.RankType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 阿分题学习计划单元管卡管理
 * <pre>
 *     +---------------------+--------------+------+-----+---------+----------------+
 *     | Field               | Type         | Null | Key | Default | Extra          |
 *     +---------------------+--------------+------+-----+---------+----------------+
 *     | ID                  | bigint(20)   | NO   | PRI | NULL    | auto_increment |
 *     | BOOK_ID             | bigint(20)   | NO   | MUL | NULL    |                |
 *     | UNIT_ID             | bigint(20)   | NO   |     | NULL    |                |
 *     | UNIT_RANK           | int(11)      | NO   |     | NULL    |                |
 *     | UNIT_NAME           | varchar(255) | NO   |     | NULL    |                |
 *     | RANK                | int(11)      | NO   |     | NULL    |                |
 *     | KNOWLEDGE_POING     | varchar(255) | NO   |     | NULL    |                |
 *     | KNOWLEDGE_POING_NUM | int(11)      | NO   |     | NULL    |                |
 *     | RANK_TYPE           | varchar(20)  | NO   |     | NULL    |                |
 *     | CREATETIME          | datetime     | NO   |     | NULL    |                |
 *     | UPDATETIME          | datetime     | NO   |     | NULL    |                |
 *     +---------------------+--------------+------+-----+---------+----------------+
 * </pre>
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-09-09 1:09PM
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_LEARNING_PLAN_UNIT_RANK_MANAGER")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170906")
public class AfentiLearningPlanUnitRankManager extends LongIdEntity {

    private static final long serialVersionUID = -4597170153753228971L;

    @UtopiaSqlColumn(name = "NEW_BOOK_ID") private String newBookId; // 课本ID（新体系）
    @UtopiaSqlColumn(name = "NEW_UNIT_ID") private String newUnitId; // 单元ID（新体系）
    @UtopiaSqlColumn(name = "NEW_SECTION_ID") private String newSectionId; // 单元ID（新体系）
    @UtopiaSqlColumn(name = "RANK") private Integer rank; // 关卡
    @UtopiaSqlColumn(name = "UNIT_RANK") private Integer unitRank; // 单元关数
    @UtopiaSqlColumn(name = "UNIT_NAME") private String unitName; // 单元名称
    @UtopiaSqlColumn(name = "RANK_TYPE") private String rankType; // 关卡类型，BASE = 基础关卡，SUMMARIZE = 总结关卡
    @UtopiaSqlColumn(name = "SUBJECT") private Subject subject; // 学科
    @UtopiaSqlColumn(name = "TYPE") private AfentiLearningType type;
    @UtopiaSqlColumn(name = "RUNTIME_MODE") private Integer runtimeMode;    // 运行环境

    public static String ck_nbid(String newBookId) {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUnitRankManager.class, "NBID", newBookId);
    }

    public static String ck_all_nbid() {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUnitRankManager.class, "ALL");
    }

    public static String ck_all_nbid_preparation() {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUnitRankManager.class, "ALL:P");
    }

    public static String ck_all_nbid_review() {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUnitRankManager.class, "ALL:R");
    }

    public int fetchUnitRank() {
        return unitRank == null ? 0 : unitRank;
    }

    public RankType fetchRankType() {
        return RankType.of(rankType);
    }

    public static AfentiLearningPlanUnitRankManager newInstance() {
        AfentiLearningPlanUnitRankManager manager = new AfentiLearningPlanUnitRankManager();
        manager.unitRank = 0;
        manager.unitName = "";
        manager.rank = 0;
        manager.rankType = "";
        manager.newBookId = "";
        manager.newUnitId = "";
        manager.newSectionId = "";
        return manager;
    }
}
