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
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 阿分题学习计划奖励历史
 * <pre>
 *     +--------------------+--------------+------+-----+---------+----------------+
 *     | Field              | Type         | Null | Key | Default | Extra          |
 *     +--------------------+--------------+------+-----+---------+----------------+
 *     | ID                 | bigint(20)   | NO   | PRI | NULL    | auto_increment |
 *     | CREATETIME         | datetime     | NO   |     | NULL    |                |
 *     | UPDATETIME         | datetime     | NO   |     | NULL    |                |
 *     | USER_ID            | bigint(20)   | NO   | MUL | NULL    |                |
 *     | BOOK_ID            | bigint(20)   | NO   |     | NULL    |                |
 *     | UNIT_ID            | bigint(20)   | NO   |     | NULL    |                |
 *     | RANK               | int(11)      | NO   |     | NULL    |                |
 *     | UNIT_RANK          | int(11)      | NO   |     | NULL    |                |
 *     | REWARD_TYPE        | int(11)      | NO   |     | NULL    |                |
 *     | SILVER             | int(11)      | NO   |     | NULL    |                |
 *     | SUCCESSIVER_SILVER | int(11)      | NO   |     | NULL    |                |
 *     | PK_VITALITY        | int(11)      | NO   |     | NULL    |                |
 *     | PK_EQUIPMENT       | int(11)      | NO   |     | NULL    |                |
 *     | RECEIVED           | bit(1)       | NO   |     | NULL    |                |
 *     | PLAN_TYPE          | varchar(20)  | NO   |     | NULL    |                |
 *     | COMMENT            | varchar(255) | YES  |     | NULL    |                |
 *     +--------------------+--------------+------+-----+---------+----------------+
 * </pre>
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-09-09 11:52AM
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_LEARNING_PLAN_REWARD_HISTORY")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class AfentiLearningPlanRewardHistory extends LongIdEntity {

    private static final long serialVersionUID = -7609064794175068276L;

    @UtopiaSqlColumn(name = "USER_ID") private Long userId; // 用户ID
    @UtopiaSqlColumn(name = "BOOK_ID") @Deprecated private Long bookId; // 课本ID
    @UtopiaSqlColumn(name = "UNIT_ID") @Deprecated private Long unitId; // 单元ID
    @UtopiaSqlColumn(name = "RANK") private Integer rank; // // 某本书的某个单元的第rank关
    @UtopiaSqlColumn(name = "UNIT_RANK") private Integer unitRank; // 某本书的第unitRank个单元
    @UtopiaSqlColumn(name = "REWARD_TYPE") private Integer rewardType; // 获奖类别 用获得的星级区分
    @UtopiaSqlColumn(name = "SILVER") private Integer silver; // 银币
    @UtopiaSqlColumn(name = "SUCCESSIVER_SILVER") private Integer successiveSilver; // 连续奖励
    @UtopiaSqlColumn(name = "PK_VITALITY") private Integer pkVitality; // PK活力
    @UtopiaSqlColumn(name = "PK_EQUIPMENT") private Integer pkEquipment; // PK装备
    @UtopiaSqlColumn(name = "RECEIVED") private Boolean received; // 是否领取
    @UtopiaSqlColumn(name = "PLAN_TYPE") private String planType; // 类型 (基础题、附加题)
    @UtopiaSqlColumn(name = "COMMENT") private String comment; // 奖励内容详情
    @UtopiaSqlColumn(name = "NEW_BOOK_ID") private String newBookId; // 课本ID
    @UtopiaSqlColumn(name = "NEW_UNIT_ID") private String newUnitId; // 单元ID
    @UtopiaSqlColumn(name = "SUBJECT") private Subject subject; // 学科

    public static String ck_uid_nbid(Long userId, String newBookId) {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanRewardHistory.class,
                new String[]{"UID", "NBID"}, new Object[]{userId, newBookId});
    }
}
