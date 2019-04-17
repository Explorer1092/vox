package com.voxlearning.washington.service;

import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Guohong Tan
 * @author Rui Bao
 * @version 0.1
 * @since 13-7-8
 */
@RequiredArgsConstructor
public class LoadMathFlashGameContext implements Serializable {

    private static final long serialVersionUID = -666391429792170721L;

    @Getter @NonNull private final StudyType studyType;
    @Getter @NonNull private final PracticeType mathPractice;
    @Getter @Setter private Long userId;
    @Getter @Setter private Long clazzId;
    @Getter @Setter private Long groupId;
    @Getter @Setter private Long bookId;
    @Getter @Setter private Long unitId;
    @Getter @Setter private Long lessonId;
    @Getter @Setter private String homeworkId;
    @Getter @Setter private Long pointId;
    @Getter @Setter private Integer questionNum;
    @Getter @Setter private Long packageId;

    //参见LoadMathFlashGameContext.dataType  MathBase.baseType  MathHomeworkPracticeRef.dataType，是一个含义，作业/预习/xxx，默认值是 1（作业）
    @Getter @Setter private String dataType;
    //作业类型(数学包含作业和口算作业)
    @Getter @Setter private String homeworkType;
}
