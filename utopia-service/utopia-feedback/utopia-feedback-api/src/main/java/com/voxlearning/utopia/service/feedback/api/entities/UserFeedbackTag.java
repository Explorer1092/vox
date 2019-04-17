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
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.*;

/**
 * Created by Shuai Huan on 2014/6/17.
 */
@DocumentTable(table = "VOX_USER_FEEDBACK_TAG")
@UtopiaCacheExpiration
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
public class UserFeedbackTag extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -2316554523485634353L;

    @UtopiaSqlColumn @Getter @Setter private Long parentId;
    @UtopiaSqlColumn @NonNull @Getter @Setter private String name;
    @UtopiaSqlColumn @Getter @Setter private String watcherName;
    @UtopiaSqlColumn @Getter @Setter private Integer priority;
    @UtopiaSqlColumn @Getter @Setter private Integer redmineUserId;

    //redmine分级：1:低 2:普通 3:高 4:紧急 5:立刻
    public static final Integer PRIORITY_LEVEL_PROMPT = 5;
    public static final Integer PRIORITY_LEVEL_EMERGENT = 4;
    public static final Integer PRIORITY_LEVEL_HIGH = 3;
    public static final Integer PRIORITY_LEVEL_MIDDLE = 2;
    public static final Integer PRIORITY_LEVEL_LOW = 1;
    public static final Integer PRIORITY_LEVEL_INVALID = 0;

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(UserFeedbackTag.class, "ALL");
    }

}
