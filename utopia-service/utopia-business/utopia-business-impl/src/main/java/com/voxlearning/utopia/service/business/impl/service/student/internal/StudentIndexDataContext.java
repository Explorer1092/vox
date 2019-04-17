/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.internal;

/**
 * @author Rui.Bao
 * @since 2014-08-08 11:10 AM
 */

import com.voxlearning.utopia.service.business.impl.service.student.buffer.UserLoaderBuffer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

public class StudentIndexDataContext implements Serializable {
    private static final long serialVersionUID = 7686785587180303460L;

    // in
    @Getter
    @NonNull
    private final StudentDetail student;
    public final long timestamp;

    // ========================================================================
    // Buffer support
    // ========================================================================
    public UserLoaderBuffer __userLoaderBuffer;

    // ========================================================================
    // Group system
    // ========================================================================
    public List<GroupMapper> __studentGroups = Collections.emptyList();
    public Set<Long> __groupIds = Collections.emptySet();
    public Set<Long> __studentIds = Collections.emptySet();
    public Set<Long> __teacherIds = Collections.emptySet();

    // ========================================================================
    // Homework location system
    // ========================================================================


    public List<NewHomework.Location> __englishNormalHomeworkLocations = new LinkedList<>();
    public List<NewHomework.Location> __englishTermReviewHomeworkLocations = new LinkedList<>();
    public List<NewHomework.Location> __englishOcrHomeworkLocations = new LinkedList<>();

    public List<NewHomework.Location> __mathNormalHomeworkLocations = new LinkedList<>();
    public List<NewHomework.Location> __mathTermReviewHomeworkLocations = new LinkedList<>();
    public List<NewHomework.Location> __mathOcrHomeworkLocations = new LinkedList<>();

    public List<NewHomework.Location> __chineseNormalHomeworkLocations = new LinkedList<>();
    public List<NewHomework.Location> __chineseTermReviewHomeworkLocations = new LinkedList<>();

    public List<NewHomework.Location> __mothersDayHomeworkLocations = new LinkedList<>();
    public List<NewHomework.Location> __kidsDayHomeworkLocations = new LinkedList<>();

    // ========================================================================
    // Homework cards
    // ========================================================================
    public final List<Map<String, Object>> __homeworkCards = new LinkedList<>();
    public final List<Map<String, Object>> __makeUpHomeworkCards = new LinkedList<>();
    public final List<Map<String, Object>> __basicReviewHomeworkCards = new LinkedList<>();
    public final List<Map<String, Object>> __correctionHomeworkCards = new LinkedList<>();
    public final List<Map<String, Object>> __outsideReadingCards = new LinkedList<>();
    public final List<Map<String, Object>> __ancientPoetryActivityCards = new LinkedList<>();
    // ========================================================================
    // NewExam cards
    // ========================================================================
    public final List<Map<String, Object>> __enterableNewExamCards = new LinkedList<>();
    public final List<Map<String, Object>> __registrableNewExamCards = new LinkedList<>();
    public final List<Map<String, Object>> __enterableUnitTestCards = new LinkedList<>();

    // ========================================================================
    // Control system
    // ========================================================================
    public boolean __newExamExist = false;                              // 是否存在考试
    public boolean __ignoreMakeUpEnglishHomework = false;               // 当前英语作业未检查且未过期，忽略补做作业检查
    public boolean __ignoreMakeUpMathHomework = false;                  // 当前数学作业未检查且未过期，忽略补做作业检查
    public boolean __ignoreMakeUpChineseHomework = false;               // 当前语文作业未检查且未过期，忽略补做作业检查
    public boolean __ignoreMakeUpOcrEnglishHomework = false;            // 当前英语纸质作业未检查且未过期，忽略补做作业检查
    public boolean __ignoreMakeUpOcrMathHomework = false;               // 当前数学纸质作业未检查且未过期，忽略补做作业检查
    public boolean __ignoreMakeUpEnglishTermReviewHomework = false;     // 当前英语期末复习作业未检查且未过期，忽略补做作业检查
    public boolean __ignoreMakeUpChineseTermReviewHomework = false;     // 当前语文期末复习作业未检查且未过期，忽略补做作业检查
    public boolean __ignoreMakeUpMathTermReviewHomework = false;        // 当前数学期末复习作业未检查且未过期，忽略补做作业检查


    @Deprecated
    @Getter
    @Setter
    private boolean englishWorkbookCardExists = false;

    // out
    @Getter private final Map<String, Object> param = new LinkedHashMap<>();

    public StudentIndexDataContext(StudentDetail student) {
        this.student = Objects.requireNonNull(student);
        this.timestamp = System.currentTimeMillis();
        getParam().put("homeworkCards", __homeworkCards);
        getParam().put("makeUpHomeworkCards", __makeUpHomeworkCards);
        getParam().put("enterableNewExamCards", __enterableNewExamCards);
        getParam().put("registrableNewExamCards", __registrableNewExamCards);
        getParam().put("basicReviewHomeworkCards", __basicReviewHomeworkCards);
        getParam().put("enterableUnitTestCards", __enterableUnitTestCards);
        getParam().put("correctionHomeworkCards", __correctionHomeworkCards);
        getParam().put("outsideReadingCards", __outsideReadingCards);
        getParam().put("ancientPoetryActivityCards", __ancientPoetryActivityCards);
    }
}
