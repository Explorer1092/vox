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

package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.api.constant.MagicValueType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2015/12/1.
 */
@DocumentConnection(configName = "hs_platform")
@DocumentTable(table = "VOX_STUDENT_MAGIC_VALUE_HISTORY")
@NoArgsConstructor
@UtopiaCacheExpiration
public class StudentMagicValueHistory extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 4301773454160197514L;
    @UtopiaSqlColumn @Getter @Setter private Long magicianId;       // 魔法师ID
    @UtopiaSqlColumn @Getter @Setter private MagicValueType valueType;      // 获取魔力值类型
    @UtopiaSqlColumn @Getter @Setter private Integer levelValue;    // 获取魔力值数量
}