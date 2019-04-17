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

package com.voxlearning.utopia.service.wechat.api.entities;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2015/7/2.
 */
@Getter
@Setter
@DocumentTable(table = "VOX_WECHAT_LITTLE_CHAMPION")
@NoArgsConstructor
@UtopiaCacheExpiration
@Deprecated
public class WechatLittleChampion extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -5859382332367729793L;
    @UtopiaSqlColumn private Long studentId;
    @UtopiaSqlColumn private String studentName;
    @UtopiaSqlColumn private Integer score;
    @UtopiaSqlColumn private Subject subject;
    @UtopiaSqlColumn private Long teacherId;
    @UtopiaSqlColumn private String helpContent;
    @UtopiaSqlColumn private String story;
    @UtopiaSqlColumn private String schoolCategory;
    @UtopiaSqlColumn private String schoolName;
    @UtopiaSqlColumn private String parentName;
    @UtopiaSqlColumn(name = "PARENT_MOBILE") private String parentSensitiveMobile;
    @UtopiaSqlColumn private Integer status; // 0 提交  1 确认

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(WechatLittleChampion.class, id);
    }

    public static String ck_teacherId(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(WechatLittleChampion.class,
                new String[]{"teacherId"},
                new Object[]{teacherId},
                new Object[]{0L});
    }

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(WechatLittleChampion.class,
                new String[]{"studentId"},
                new Object[]{studentId},
                new Object[]{0L});
    }

}
