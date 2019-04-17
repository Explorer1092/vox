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
import java.util.List;

@Getter
@Setter
@DocumentConnection(configName = "mongo-middleschool")
@DocumentDatabase(database = "vox-studycraft")
@DocumentCollection(collection = "sc_homework_do_question_{}", dynamic = true)
public class MiddleSchoolHomeworkDoQuestion implements Serializable {

    private static final long serialVersionUID = 1846772599782364486L;

    @DocumentId
    private String id;

    @DocumentField("homework_do_id")
    private String homeworkDoId;

    @DocumentField("homework_id")
    private String homeworkId;

    @DocumentField("practice_type")
    private String practiceType;

    @DocumentField("homeworkCreateTime")
    private Date homework_create_time;

    @DocumentField("teacher_id")
    private Long teacherId;

    @DocumentField("student_id")
    private Long studentId;

    @DocumentField("clazz_id")
    private Long clazzId;

    @DocumentField("subject_id")
    private Integer subjectId;

    @DocumentField("type")
    private Integer type;

    @DocumentField("tags")
    private List<String> tags;

    @DocumentField("question_id")
    private String questionId;

    @DocumentField("word_id")
    private String wordId;

    @DocumentField("duration")
    private Long duration;

    @DocumentField("content_type_id")
    private Integer contentTypeId;

    @DocumentField("tag_id")
    private Integer tagId;

    @DocumentField("score")
    private Integer score;

    @DocumentField("created_at")
    private Date createdAt;

    @DocumentField("sub_content_datas")
    private List<MiddleSchoolHomeworkDoQuestion.SubContentData> subContentDatas; // 每小题数据

    /**
     * 每道小题的详情数据
     */
    @Getter
    @Setter
    public static class SubContentData implements Serializable {
        private static final long serialVersionUID = 5030825250797231711L;
        @DocumentField("score")
        private Integer score;
        @DocumentField("is_right")
        private Integer isRight;
        @DocumentField("answers")
        private List<String> answers;
        @DocumentField("is_blank_right_list")
        private List<Integer> isBlankRightList;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -7221716627855317992L;

        private String randomId;
        private String time;

        @Override
        public String toString() {
            return randomId + "-" + time;
        }
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MiddleSchoolHomeworkDoQuestion.class, id);
    }

    public Subject getSubject() {
        if(this.subjectId == 1) {
            return Subject.CHINESE;
        } else if(this.subjectId == 2) {
            return Subject.MATH;
        } else if(this.subjectId == 3) {
            return Subject.ENGLISH;
        }
        return Subject.UNKNOWN;
    }
}