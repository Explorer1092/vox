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

package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jia HuanYin
 * @since 2015/12/2
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_teacher_fake")
public class CrmTeacherFake implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = 7625516530464487097L;

    @DocumentId
    private String id;
    private Long teacherId;
    private String teacherName;
    private Long schoolId;
    private String schoolName;
    private Long fakerId;
    private String fakerName;
    private String fakerPhone;
    private String fakeNote;
    private String reviewer;
    private String reviewerName;
    private String reviewNote;
    private ReviewStatus reviewStatus;
    private Date reviewTime;
    private Date createTime;
    private Date updateTime;

    @Override
    public void touchCreateTime(long timestamp) {
        if (createTime == null) {
            createTime = new Date(timestamp);
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateTime = new Date(timestamp);
    }
}
