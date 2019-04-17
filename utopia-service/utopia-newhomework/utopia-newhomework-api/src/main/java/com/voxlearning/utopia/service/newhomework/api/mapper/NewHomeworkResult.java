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

package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class NewHomeworkResult extends BaseHomeworkResult implements Serializable {

    private static final long serialVersionUID = -1100717908579956633L;

    private String id;
    private Date createAt;                                                          // 作业生成时间
    private Date updateAt;                                                          // 作业更新时间
    private Boolean finishCorrect;                                                  // 全部完成批改
    private Boolean repair;                                                         // 是否补做完成（true/false） 慎用，16年9月1日之前数据为空
    private Boolean urge;//催促
    private Integer beanNum;//家长奖励学豆数

    @JsonIgnore
    public boolean isCorrected() {
        return finishCorrect != null && finishCorrect;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "subject", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = 1761342947251257529L;

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
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        String uid = segments[3];
        return new ID(day, subject, hid, uid);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = 2552375471484983061L;

        private String id;
        private String homeworkId;
        private Subject subject;
        private String actionId;
        private Long clazzGroupId;
        private Long userId;
        private Date createAt;
        private Date updateAt;
        private Date finishAt;
    }

    public Location toLocation() {
        Location location = new Location();
        location.id = getId();
        location.homeworkId = homeworkId;
        location.subject = subject;
        location.actionId = actionId;
        location.clazzGroupId = clazzGroupId;
        location.userId = userId;
        location.createAt = createAt;
        location.updateAt = updateAt;
        location.finishAt = finishAt;
        return location;
    }
}
