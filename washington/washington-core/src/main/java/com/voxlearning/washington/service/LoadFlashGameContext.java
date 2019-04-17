/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.washington.service;

import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Context for loading flash game.
 *
 * @author Xiaohai Zhang
 * @since 2013-06-08 11:12
 */
@RequiredArgsConstructor
public class LoadFlashGameContext implements Serializable {
    private static final long serialVersionUID = -2977613948192499917L;

    @Getter @NonNull private final StudyType studyType;
    @Getter @NonNull private final PracticeType englishPractice;
    @Getter @Setter private Long userId;
    @Getter @Setter private Long clazzId;
    @Getter @Setter private Long groupId;
    @Getter @Setter private Long bookId;
    @Getter @Setter private Long unitId;
    @Getter @Setter private Long lessonId;
    @Getter @Setter private String homeworkId;
    @Getter @Setter private Long packageId;
    @Getter @Setter private Integer clazzLevel; //年级，提供作业录音数据分析
    @Getter @Setter private Integer prvRgnCode; //省编码，提供作业录音数据分析
    @Getter @Setter private String newLessonId;
    @Getter @Setter private String qids;
    @Getter @Setter private String pictureBookId; // 用于绘本
    @Getter @Setter private String newHomeworkType;
    @Getter @Setter private String objectiveConfigType;
    @Getter @Setter private String token; // 用于livecast
    @Getter @Setter private String newBookId;
}
