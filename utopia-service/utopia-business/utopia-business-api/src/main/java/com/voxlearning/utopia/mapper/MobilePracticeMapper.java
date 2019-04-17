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

package com.voxlearning.utopia.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by libin on 14-3-26.
 */
public class MobilePracticeMapper implements Serializable {
    private static final long serialVersionUID = -6676200487062645531L;

    @Getter @Setter private int taskId;
    @Getter @Setter private String taskName;
    @Getter @Setter private String taskVersion;
    @Getter @Setter private String taskIconUrl;
    @Getter @Setter private String taskDownLoadUrl;
    @Getter @Setter private String taskMD5;
    @Getter @Setter private Map<String, Object> taskData;
    @Getter @Setter private Boolean taskIsFinished;

    @Getter @Setter @JsonIgnore private String homeworkDetailId;
    @Getter @Setter @JsonIgnore private Long bookId;
    @Getter @Setter @JsonIgnore private Long pointId;
    @Getter @Setter @JsonIgnore private Long unitId;
    @Getter @Setter @JsonIgnore private Long lessonId;
    @Getter @Setter @JsonIgnore private Integer questionNum;

}
