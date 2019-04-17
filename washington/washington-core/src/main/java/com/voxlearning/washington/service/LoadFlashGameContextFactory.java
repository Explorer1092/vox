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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for building {@link LoadFlashGameContext}.
 *
 * @author Xiaohai Zhang
 * @see LoadFlashGameContext
 * @since 2013-06-08 11:13
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract public class LoadFlashGameContextFactory {
    public static LoadFlashGameContext selfstudy(PracticeType englishPractice, Long userId, Long clazzId, Long bookId, Long unitId, Long lessonId) {
        Validate.notNull(englishPractice);
        LoadFlashGameContextFactory.log.debug("Build selfstudy LoadFlashGameContext:");
        LoadFlashGameContextFactory.log.debug("  +- studyType:    {}", StudyType.selfstudy);
        LoadFlashGameContextFactory.log.debug("  +- englishPractice: {}/{}", englishPractice.getSubPracticeName_1(), englishPractice.getId());
        LoadFlashGameContextFactory.log.debug("  +- userId:       {}", userId);
        LoadFlashGameContextFactory.log.debug("  +- clazzId:      {}", clazzId);
        LoadFlashGameContextFactory.log.debug("  +- bookId:       {}", bookId);
        LoadFlashGameContextFactory.log.debug("  +- unitId:       {}", unitId);
        LoadFlashGameContextFactory.log.debug("  \\- lessonId:     {}", lessonId);

        LoadFlashGameContext context = new LoadFlashGameContext(StudyType.selfstudy, englishPractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);

        return context;
    }

    public static LoadFlashGameContext newSelfstudy(PracticeType practice, Long userId, String lessonId, String qids, String bookId) {
        Validate.notNull(practice);
        LoadFlashGameContextFactory.log.debug("Build selfstudy LoadFlashGameContext:");
        LoadFlashGameContextFactory.log.debug("  +- studyType:    {}", StudyType.selfstudy);
        LoadFlashGameContextFactory.log.debug("  +- englishPractice: {}/{}", practice.getSubPracticeName_1(), practice.getId());
        LoadFlashGameContextFactory.log.debug("  +- userId:       {}", userId);
        LoadFlashGameContextFactory.log.debug("  \\- lessonId:     {}", lessonId);
        LoadFlashGameContext context = new LoadFlashGameContext(StudyType.selfstudy, practice);
        context.setUserId(userId);
        context.setNewLessonId(lessonId);
        context.setQids(qids);
        context.setNewBookId(bookId);

        return context;
    }

    public static LoadFlashGameContext newPictureBookSelfstudy(PracticeType practice, Long userId, String pictureBookId) {
        Validate.notNull(practice);
        LoadFlashGameContextFactory.log.debug("Build selfstudy LoadFlashGameContext:");
        LoadFlashGameContextFactory.log.debug("  +- studyType:    {}", StudyType.selfstudy);
        LoadFlashGameContextFactory.log.debug("  +- englishPractice: {}/{}", practice.getSubPracticeName_1(), practice.getId());
        LoadFlashGameContextFactory.log.debug("  +- userId:       {}", userId);
        LoadFlashGameContext context = new LoadFlashGameContext(StudyType.selfstudy, practice);
        context.setUserId(userId);
        context.setPictureBookId(pictureBookId);

        return context;
    }

    public static LoadFlashGameContext homework(PracticeType englishPractice, Long userId, Long clazzId, String homeworkId, Long bookId, Long unitId, Long lessonId) {
        Validate.notNull(englishPractice);
        LoadFlashGameContextFactory.log.debug("Build homework LoadFlashGameContext:");
        LoadFlashGameContextFactory.log.debug("  +- studyType:        {}", StudyType.homework);
        LoadFlashGameContextFactory.log.debug("  +- englishPractice:     {}/{}", englishPractice.getSubPracticeName_1(), englishPractice.getId());
        LoadFlashGameContextFactory.log.debug("  +- userId:           {}", userId);
        LoadFlashGameContextFactory.log.debug("  +- clazzId:          {}", clazzId);
        LoadFlashGameContextFactory.log.debug("  +- homeworkId:       {}", homeworkId);
        LoadFlashGameContextFactory.log.debug("  +- bookId:       {}", bookId);
        LoadFlashGameContextFactory.log.debug("  +- unitId:       {}", unitId);
        LoadFlashGameContextFactory.log.debug("  +- lessonId:       {}", lessonId);

        LoadFlashGameContext context = new LoadFlashGameContext(StudyType.homework, englishPractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setHomeworkId(homeworkId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);

        return context;
    }

    /**
     * 绘本和游戏的区别是lessonId和pictureBookId有没有值
     */
    public static LoadFlashGameContext newHomework(PracticeType practice, Long userId, Long clazzId, String homeworkId, String lessonId, String pictureBookId, String homeworkType, String objectiveConfigType) {
        Validate.notNull(practice);
        LoadFlashGameContextFactory.log.debug("Build homework LoadFlashGameContext:");
        LoadFlashGameContextFactory.log.debug("  +- studyType:        {}", StudyType.homework);
        LoadFlashGameContextFactory.log.debug("  +- englishPractice:     {}/{}", practice.getSubPracticeName_1(), practice.getId());
        LoadFlashGameContextFactory.log.debug("  +- userId:           {}", userId);
        LoadFlashGameContextFactory.log.debug("  +- clazzId:          {}", clazzId);
        LoadFlashGameContextFactory.log.debug("  +- homeworkId:       {}", homeworkId);
        LoadFlashGameContextFactory.log.debug("  +- lessonId:       {}", lessonId);
        LoadFlashGameContextFactory.log.debug("  +- pictureBookId:       {}", pictureBookId);
        LoadFlashGameContextFactory.log.debug("  +- homeworkType        {}", homeworkType);
        LoadFlashGameContextFactory.log.debug("  +- objectiveConfigType       {}", objectiveConfigType);

        LoadFlashGameContext context;
        if (StringUtils.equalsIgnoreCase(NewHomeworkType.WinterVacation.name(), homeworkType)
                || StringUtils.equalsIgnoreCase(NewHomeworkType.SummerVacation.name(), homeworkType)) {
            context = new LoadFlashGameContext(StudyType.vacationHomework, practice);
        } else {
            context = new LoadFlashGameContext(StudyType.homework, practice);
        }
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setHomeworkId(homeworkId);
        context.setNewLessonId(lessonId);
        context.setPictureBookId(pictureBookId);
        context.setNewHomeworkType(homeworkType);
        context.setObjectiveConfigType(objectiveConfigType);

        return context;
    }

    public static LoadFlashGameContext vacationHomework(PracticeType englishPractice, Long userId, Long clazzId, Long groupId, Long bookId, Long unitId, Long lessonId, String homeworkId, Long packageId) {
        Validate.notNull(englishPractice);
        LoadFlashGameContextFactory.log.debug("Build vacationHomework LoadFlashGameContext:");
        LoadFlashGameContextFactory.log.debug("  +- studyType:        {}", StudyType.vacationHomework);
        LoadFlashGameContextFactory.log.debug("  +- englishPractice:     {}/{}", englishPractice.getSubPracticeName_1(), englishPractice.getId());
        LoadFlashGameContextFactory.log.debug("  +- userId:           {}", userId);
        LoadFlashGameContextFactory.log.debug("  +- clazzId:          {}", clazzId);
        LoadFlashGameContextFactory.log.debug("  +- groupId:          {}", groupId);
        LoadFlashGameContextFactory.log.debug("  +- bookId:           {}", bookId);
        LoadFlashGameContextFactory.log.debug("  +- unitId:           {}", unitId);
        LoadFlashGameContextFactory.log.debug("  +- lessonId:         {}", lessonId);
        LoadFlashGameContextFactory.log.debug("  +- homeworkId:       {}", homeworkId);
        LoadFlashGameContextFactory.log.debug("  \\- packageId: {}", packageId);

        LoadFlashGameContext context = new LoadFlashGameContext(StudyType.vacationHomework, englishPractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setGroupId(groupId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);
        context.setHomeworkId(homeworkId);
        context.setPackageId(packageId);

        return context;
    }
}
