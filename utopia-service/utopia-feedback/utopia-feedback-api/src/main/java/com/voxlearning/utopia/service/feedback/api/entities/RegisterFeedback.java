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

package com.voxlearning.utopia.service.feedback.api.entities;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.api.constant.RegisterFeedbackCategory;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;


/**
 * 用户注册手机验证反馈表
 *
 * @author xin.xin
 * @since 2014-03-03
 */
@DocumentTable(table = "VOX_REG_FEEDBACK")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20160725")
public class RegisterFeedback extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 4605036224664087557L;

    @UtopiaSqlColumn(name = "MOBILE")
    @Getter
    @Setter
    private String sensitiveMobile;

    @UtopiaSqlColumn(name = "VERIFICATION_CODE")
    @Getter
    @Setter
    private String verificationCode;
    @UtopiaSqlColumn(name = "STATE")
    @Getter
    @Setter
    private Integer state;
    @UtopiaSqlColumn(name = "OPERATION")
    @Getter
    @Setter
    private String operation;
    @UtopiaSqlColumn(name = "OPERATOR")
    @Getter
    @Setter
    private String operator;
    @UtopiaSqlColumn(name = "CATEGORY")
    @Getter
    @Setter
    private RegisterFeedbackCategory category;
    @UtopiaSqlColumn(name = "USER_ID")
    @Getter
    @Setter
    private Long userId;
    @UtopiaSqlColumn(name = "CONTENT")
    @Getter
    @Setter
    private String content;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(RegisterFeedback.class, id);
    }

}
