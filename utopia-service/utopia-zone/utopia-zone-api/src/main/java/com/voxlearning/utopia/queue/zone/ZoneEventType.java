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

package com.voxlearning.utopia.queue.zone;

/**
 * Message type definitions of which sending into clazz zone queue.
 *
 * @author Xiaohai Zhang
 * @since Feb 25, 2015
 */
public enum ZoneEventType {

    UNRECOGNIZED,

    CLAZZ_JOURNAL,
    DELETE_BUBBLE,
    CLEANUP_CLAZZ_JOURNAL,      // 删除数据库中指定日期之前的ClazzJournal记录
    CLEANUP_CLAZZ_ZONE_COMMENT, // 删除数据库中指定日期之前的ClazzZoneComment记录
    CLEANUP_LIKE_DETAIL,        // 删除数据库中指定日期之前的LikeDetail记录
    CLEANUP_GIFT_HISTORY,       // 删除数据库中的礼物历史

    INC_MONTH_STUDY_MASTER_COUNT, // 增加月学霸次数
    CREATE_TEACHER_LATEST,        // 老师首页班级动态

    IncreaseStudyMasterCountByOne
}
