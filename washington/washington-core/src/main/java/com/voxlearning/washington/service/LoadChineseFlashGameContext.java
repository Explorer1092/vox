package com.voxlearning.washington.service;

import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by tanguohong on 14-7-2.
 */
@RequiredArgsConstructor
public class LoadChineseFlashGameContext implements Serializable {

    private static final long serialVersionUID = -8231953025232780828L;

    @Getter @NonNull private final StudyType studyType;
    @Getter @NonNull private final PracticeType chinesePractice;
    @Getter @Setter private Long userId;
    @Getter @Setter private Long clazzId;
    @Getter @Setter private Long bookId;
    @Getter @Setter private Long unitId;
    @Getter @Setter private Long lessonId;
    @Getter @Setter private Long homeworkId;
}
