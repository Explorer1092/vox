/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.api.mapper;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalPagination extends PageImpl<JournalPagination.JournalMapper> {
    private static final long serialVersionUID = -9220924609467834256L;

    public JournalPagination() {
        this(Collections.emptyList());
    }

    public JournalPagination(List<JournalMapper> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public JournalPagination(List<JournalMapper> content) {
        super(content);
    }

    @Data
    public static class JournalMapper implements Serializable {
        private static final long serialVersionUID = -5216094415203760283L;

        private Long journalId;
        private Long clazzId;
        private Long relevantUserId;
        private String relevantUserName;
        private String relevantUserImg;
        private UserType relevantUserType;
        private ClazzJournalType journalType;
        private String date;
        private Long bubble;
        private Integer likeCount;
        private Boolean canLike;
        private List<String> names;
        private Boolean canClazzLeak = false;
        private Boolean canComment;
        private List<Map<String, Object>> comments;
        private Map<String, Object> param = new HashMap<>();
    }
}
