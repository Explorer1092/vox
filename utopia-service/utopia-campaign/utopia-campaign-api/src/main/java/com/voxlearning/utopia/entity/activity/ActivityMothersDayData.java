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

package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author RuiBao
 * @version 0.1
 * @since 4/27/2015
 */
@Getter
@Setter
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_ACTIVITY_MOTHERS_DAY_DATA")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ActivityMothersDayData implements Serializable, PrimaryKeyAccessor<Long> {
    private static final long serialVersionUID = 6623128390441995489L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("STUDENT_ID")
    private Long studentId;

    @DocumentField("STUDENT_NAME")
    private String studentName;

    @DocumentField("HOMEWORK")
    private Integer homework; // 完成作业数量

    @DocumentField("WORD")
    private Integer word; // 掌握单词数量

    @DocumentField("KNOWLEDGE")
    private Integer knowledge; // 学会的知识点数量

    @Override
    public Long getId() {
        return getStudentId();
    }

    @Override
    public void setId(Long id) {
        setStudentId(id);
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ActivityMothersDayData.class, id);
    }
}
