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
 * 阿分题学习计划学生关卡结果
 * <pre>
 *     +--------------------+-------------+------+-----+---------+----------------+
 *     | Field              | Type        | Null | Key | Default | Extra          |
 *     +--------------------+-------------+------+-----+---------+----------------+
 *     | ID                 | bigint(20)  | NO   | PRI | NULL    | auto_increment |
 *     | CREATETIME         | datetime    | NO   |     | NULL    |                |
 *     | UPDATETIME         | datetime    | NO   |     | NULL    |                |
 *     | RANK               | int(11)     | NO   |     | NULL    |                |
 *     | USER_ID            | bigint(20)  | NO   | MUL | NULL    |                |
 *     | BOOK_ID            | bigint(20)  | NO   |     | NULL    |                |
 *     | UNIT_ID            | bigint(20)  | NO   |     | NULL    |                |
 *     | UNIT_RANK          | int(11)     | NO   |     | NULL    |                |
 *     | SCENE_ID           | int(11)     | YES  |     | NULL    |                |
 *     | PLAN_TYPE          | varchar(11) | NO   |     | NULL    |                |
 *     | MAX_STAR_NUM       | int(11)     | NO   |     | NULL    |                |
 *     | RIGHT_RATE         | int(11)     | NO   |     | NULL    |                |
 *     | ERROR_BOOK_KEY_NUM | int(11)     | NO   |     | NULL    |                |
 *     +--------------------+-------------+------+-----+---------+----------------+
 * </pre>
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-09-09 12:07PM
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_LEARNING_PLAN_RANK_RESULT")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class AfentiLearningPlanRankResult extends LongIdEntity {

    private static final long serialVersionUID = -7696826105097070467L;

    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "BOOK_ID") @Deprecated private Long bookId;
    @UtopiaSqlColumn(name = "UNIT_ID") @Deprecated private Long unitId;
    @UtopiaSqlColumn(name = "RANK") private Integer rank; // 某本书的某个单元的第rank关
    @UtopiaSqlColumn(name = "UNIT_RANK") private Integer unitRank; // 某本书的第unitRank个单元
    @UtopiaSqlColumn(name = "SCENE_ID") @Deprecated private Integer sceneId; // 场景ID
    @UtopiaSqlColumn(name = "PLAN_TYPE") private String planType; // 类型(基础题，附加题)
    @UtopiaSqlColumn(name = "MAX_STAR_NUM") private Integer maxStarNum; // 已经达到几颗星
    @UtopiaSqlColumn(name = "RIGHT_RATE") private Integer rightRate; // 正确率
    @UtopiaSqlColumn(name = "ERROR_BOOK_KEY_NUM") private Integer errorBookKey; // 错题本钥匙数
    @UtopiaSqlColumn(name = "NEW_BOOK_ID") private String newBookId;
    @UtopiaSqlColumn(name = "NEW_UNIT_ID") private String newUnitId;
    @UtopiaSqlColumn(name = "SUBJECT") private Subject subject;

    public static String ck_uid_nbid(Long userId, String newBookId) {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanRankResult.class,
                new String[]{"UID", "NBID"}, new Object[]{userId, newBookId});
    }
}
