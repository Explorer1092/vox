package com.voxlearning.washington.service;

import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;

/**
 * Created by tanguohong on 14-7-2.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract public class LoadChineseFlashGameContextFactory {

    public static LoadChineseFlashGameContext selfstudy(PracticeType chinesePractice, Long userId, Long clazzId, Long bookId, Long unitId, Long lessonId) {
        Validate.notNull(chinesePractice);
        LoadChineseFlashGameContextFactory.log.debug("Build selfstudy LoadFlashGameContext:");
        LoadChineseFlashGameContextFactory.log.debug("  +- studyType:    {}", StudyType.selfstudy);
        LoadChineseFlashGameContextFactory.log.debug("  +- chinesePractice: {}/{}", chinesePractice.getPracticeName(), chinesePractice.getId());
        LoadChineseFlashGameContextFactory.log.debug("  +- userId:     {}", userId);
        LoadChineseFlashGameContextFactory.log.debug("  +- clazzId:     {}", clazzId);
        LoadChineseFlashGameContextFactory.log.debug("  +- bookId:     {}", bookId);
        LoadChineseFlashGameContextFactory.log.debug("  +- unitId:     {}", unitId);
        LoadChineseFlashGameContextFactory.log.debug("  +- lessonId:     {}", lessonId);
        LoadChineseFlashGameContext context = new LoadChineseFlashGameContext(StudyType.selfstudy, chinesePractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);

        return context;
    }

    public static LoadChineseFlashGameContext homework(PracticeType chinesePractice, Long userId, Long clazzId, Long homeworkId, Long bookId, Long unitId, Long lessonId) {
        Validate.notNull(chinesePractice);
        LoadChineseFlashGameContextFactory.log.debug("Build homework LoadFlashGameContext:");
        LoadChineseFlashGameContextFactory.log.debug("  +- studyType:        {}", StudyType.homework);
        LoadChineseFlashGameContextFactory.log.debug("  +- chinesePractice:     {}/{}", chinesePractice.getPracticeName(), chinesePractice.getId());
        LoadChineseFlashGameContextFactory.log.debug("  +- userId:           {}", userId);
        LoadChineseFlashGameContextFactory.log.debug("  +- clazzId:          {}", clazzId);
        LoadChineseFlashGameContextFactory.log.debug("  +- bookId:     {}", bookId);
        LoadChineseFlashGameContextFactory.log.debug("  +- unitId:     {}", unitId);
        LoadChineseFlashGameContextFactory.log.debug("  +- lessonId:     {}", lessonId);
        LoadChineseFlashGameContextFactory.log.debug("  +- homeworkId:       {}", homeworkId);
        LoadChineseFlashGameContext context = new LoadChineseFlashGameContext(StudyType.homework, chinesePractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setHomeworkId(homeworkId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);

        return context;
    }

}
