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

package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

@Getter
@Setter
@DocumentConnection(configName = "mongo-middleschool")
@DocumentDatabase(database = "vox-studycraft")
@DocumentCollection(collection = "sc_homework_do_{}", dynamic = true)
public class MiddleSchoolHomeworkDo implements Serializable {

    private static final long serialVersionUID = -5089261844208455088L;

    @DocumentId
    private String id;

    @DocumentField("homework_id")
    private String homeworkId;

    @DocumentField("student_id")
    private Long studentId;

    @DocumentField("clazz_id")
    private Long clazzId;

    @DocumentField("create_time")
    private Date createTime;

    @DocumentField("practice")
    private LinkedHashMap<String, MiddleSchoolHomeworkDoPractice> practices;

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "subject", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = 8230392185578187329L;

        private String day;
        private Subject subject;
        private String hid;
        private String userId;

        @Override
        public String toString() {
            return day + "-" + subject + "-" + hid + "-" + userId;
        }
    }

    public ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = id.split("-");
        if (segments.length != 4) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        String uid = segments[3];
        return new ID(day, subject, hid, uid);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MiddleSchoolHomeworkDo.class, id);
    }
}
