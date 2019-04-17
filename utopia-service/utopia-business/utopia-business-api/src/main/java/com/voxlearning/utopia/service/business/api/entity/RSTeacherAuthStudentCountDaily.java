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

package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author changyuan.liu
 * @since 2015/5/13
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"day", "teacherId", "studentCount", "duplicateStudentCount"})
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-rstaff")
@DocumentCollection(collection = "rs_teacher_auth_stu_count_day")
@DocumentIndexes({
        @DocumentIndex(def = "{'teaId':1,'day':-1}", unique = true, background = true)
})
public class RSTeacherAuthStudentCountDaily implements Serializable {
    private static final long serialVersionUID = -242628318850812476L;

    @DocumentId private String id;
    @DocumentField("day") private Integer day;
    @DocumentField("teaId") private Long teacherId;
    @DocumentField("stuCount") private Integer studentCount;
    @DocumentField("dupCount") private Integer duplicateStudentCount;
}
