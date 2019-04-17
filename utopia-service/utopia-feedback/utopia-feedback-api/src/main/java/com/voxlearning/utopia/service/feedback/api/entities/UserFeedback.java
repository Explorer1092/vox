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
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.*;

import java.util.Date;

/**
 * 记录用户反馈
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-8-21
 */
@DocumentTable(table = "VOX_USER_FEEDBACK")
@UtopiaCacheExpiration
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
public class UserFeedback extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -4564872723081677928L;

    @UtopiaSqlColumn @NonNull @Getter @Setter private Long userId;
    @UtopiaSqlColumn @Getter @Setter private Integer userType;
    @UtopiaSqlColumn @Getter @Setter private String realName;
    @UtopiaSqlColumn @NonNull @Getter @Setter private String content;
    @UtopiaSqlColumn @NonNull @Getter @Setter private String feedbackType;
    @UtopiaSqlColumn(name = "FEEDBACK_SUB_TYPE_1") @Getter @Setter private String feedbackSubType1;
    @UtopiaSqlColumn(name = "FEEDBACK_SUB_TYPE_2") @Getter @Setter private String feedbackSubType2;
    @UtopiaSqlColumn(name = "CONTACT_QQ") @Getter @Setter private String contactSensitiveQq;
    @UtopiaSqlColumn(name = "CONTACT_PHONE") @Getter @Setter private String contactSensitivePhone;
    @UtopiaSqlColumn @Getter @Setter private Integer state;
    @UtopiaSqlColumn @Getter @Setter private String refUrl;
    @UtopiaSqlColumn @Getter @Setter private String reply;
    @UtopiaSqlColumn @Getter @Setter private String tag;
    @UtopiaSqlColumn @Getter @Setter private Long tagId;
    @UtopiaSqlColumn @Getter @Setter private String ip;
    @UtopiaSqlColumn @Getter @Setter private String address;
    @UtopiaSqlColumn @Getter @Setter private Integer practiceType;
    @UtopiaSqlColumn @Getter @Setter private String practiceName;
    @UtopiaSqlColumn @Getter @Setter private String categoryType;
    @UtopiaSqlColumn(name = "CATEGORY_SUB_TYPE_1") @Getter @Setter private String categorySubType1;
    @UtopiaSqlColumn(name = "CATEGORY_SUB_TYPE_2") @Getter @Setter private String categorySubType2;
    @UtopiaSqlColumn(name = "EXT_STR_1") @Getter @Setter private String extStr1;
    @UtopiaSqlColumn(name = "EXT_STR_2") @Getter @Setter private String extStr2;
    @UtopiaSqlColumn @Getter @Setter private Long studentKeyParentId;
    @UtopiaSqlColumn @Getter @Setter private String operator;       // 实际解决人
    @UtopiaSqlColumn @Getter @Setter private String delegator;      // 派遣人
    @UtopiaSqlColumn @Getter @Setter private String comment;        // 备注
    @UtopiaSqlColumn @Getter @Setter private Date confirmDatetime;
    @UtopiaSqlColumn @Getter @Setter private String confirmUser;
    @UtopiaSqlColumn @Getter @Setter private Date resolveDatetime;
    @UtopiaSqlColumn @Getter @Setter private String resolveUser;
    @UtopiaSqlColumn @Getter @Setter private Date closeDatetime;
    @UtopiaSqlColumn @Getter @Setter private String closeUser;
    @UtopiaSqlColumn @Getter @Setter private Date deleteDatetime;
    @UtopiaSqlColumn @Getter @Setter private String deleteUser;
    @UtopiaSqlColumn @Getter @Setter private String deliverReason;
    @UtopiaSqlColumn @Getter @Setter private Date contactDatetime;
    @UtopiaSqlColumn @Getter @Setter private String contactUser;
    @UtopiaSqlColumn @Getter @Setter private String contactState;

    @DocumentFieldIgnore
    @Getter @Setter private String watcher;//只为了展示，不对应db字段
    @DocumentFieldIgnore
    @Getter @Setter private User feedbackUser;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(UserFeedback.class, id);
    }

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(UserFeedback.class, "userId", userId);
    }

}
