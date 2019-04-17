package com.voxlearning.washington.service;

import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;

/**
 * @author Guohong Tan
 * @version 0.1
 * @since 13-7-8
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract public class LoadMathFlashGameContextFactory {

    public static LoadMathFlashGameContext selfstudy(PracticeType mathPractice, Long userId, Long clazzId, Long bookId, Long unitId, Long lessonId, Long pointId, String dataType, Integer amount) {
        Validate.notNull(mathPractice);
        LoadMathFlashGameContextFactory.log.debug("Build selfstudy LoadFlashGameContext:");
        LoadMathFlashGameContextFactory.log.debug("  +- studyType:    {}", StudyType.selfstudy);
        LoadMathFlashGameContextFactory.log.debug("  +- mathPractice: {}/{}", mathPractice.getPracticeName(), mathPractice.getId());
        LoadMathFlashGameContextFactory.log.debug("  +- userId:     {}", userId);
        LoadMathFlashGameContextFactory.log.debug("  +- clazzId:     {}", clazzId);
        LoadMathFlashGameContextFactory.log.debug("  +- bookId:     {}", bookId);
        LoadMathFlashGameContextFactory.log.debug("  +- unitId:     {}", unitId);
        LoadMathFlashGameContextFactory.log.debug("  +- lessonId:     {}", lessonId);
        LoadMathFlashGameContextFactory.log.debug("  +- pointId:       {}", pointId);
        LoadMathFlashGameContextFactory.log.debug("  +- dataType:      {}", dataType);
        LoadMathFlashGameContext context = new LoadMathFlashGameContext(StudyType.selfstudy, mathPractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);
        context.setPointId(pointId);
        context.setQuestionNum(amount);
        context.setDataType(dataType);

        return context;
    }

    public static LoadMathFlashGameContext homework(PracticeType mathPractice, Long userId, Long clazzId, String homeworkId, Long bookId, Long unitId, Long lessonId, Long pointId, Integer questionNum, String dataType, String homeworkType) {
        Validate.notNull(mathPractice);
        LoadMathFlashGameContextFactory.log.debug("Build homework LoadFlashGameContext:");
        LoadMathFlashGameContextFactory.log.debug("  +- studyType:        {}", StudyType.homework);
        LoadMathFlashGameContextFactory.log.debug("  +- mathPractice:     {}/{}", mathPractice.getPracticeName(), mathPractice.getId());
        LoadMathFlashGameContextFactory.log.debug("  +- userId:           {}", userId);
        LoadMathFlashGameContextFactory.log.debug("  +- clazzId:          {}", clazzId);
        LoadMathFlashGameContextFactory.log.debug("  +- bookId:     {}", bookId);
        LoadMathFlashGameContextFactory.log.debug("  +- unitId:     {}", unitId);
        LoadMathFlashGameContextFactory.log.debug("  +- lessonId:     {}", lessonId);
        LoadMathFlashGameContextFactory.log.debug("  +- pointId:       {}", pointId);
        LoadMathFlashGameContextFactory.log.debug("  +- questionNum:       {}", questionNum);
        LoadMathFlashGameContextFactory.log.debug("  +- homeworkId:       {}", homeworkId);


        LoadMathFlashGameContext context = new LoadMathFlashGameContext(StudyType.homework, mathPractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setHomeworkId(homeworkId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);
        context.setPointId(pointId);
        context.setQuestionNum(questionNum);
        context.setDataType(dataType);
        context.setHomeworkType(homeworkType);

        return context;
    }

    public static LoadMathFlashGameContext vacationHomework(PracticeType mathPractice, Long userId, Long clazzId, Long groupId, String homeworkId, Long bookId, Long unitId, Long lessonId, Long pointId, Integer questionNum, Long packageId, String dataType) {
        Validate.notNull(mathPractice);
        LoadMathFlashGameContextFactory.log.debug("Build homework LoadFlashGameContext:");
        LoadMathFlashGameContextFactory.log.debug("  +- studyType:        {}", StudyType.vacationHomework);
        LoadMathFlashGameContextFactory.log.debug("  +- mathPractice:     {}/{}", mathPractice.getPracticeName(), mathPractice.getId());
        LoadMathFlashGameContextFactory.log.debug("  +- userId:           {}", userId);
        LoadMathFlashGameContextFactory.log.debug("  +- clazzId:          {}", clazzId);
        LoadMathFlashGameContextFactory.log.debug("  +- groupId:          {}", groupId);
        LoadMathFlashGameContextFactory.log.debug("  +- bookId:     {}", bookId);
        LoadMathFlashGameContextFactory.log.debug("  +- unitId:     {}", unitId);
        LoadMathFlashGameContextFactory.log.debug("  +- lessonId:     {}", lessonId);
        LoadMathFlashGameContextFactory.log.debug("  +- pointId:       {}", pointId);
        LoadMathFlashGameContextFactory.log.debug("  +- questionNum:       {}", questionNum);
        LoadMathFlashGameContextFactory.log.debug("  +- homeworkId:       {}", homeworkId);
        LoadMathFlashGameContextFactory.log.debug("  +- packageId:       {}", packageId);
        LoadMathFlashGameContextFactory.log.debug("  +- dataType:       {}", dataType);


        LoadMathFlashGameContext context = new LoadMathFlashGameContext(StudyType.vacationHomework, mathPractice);
        context.setUserId(userId);
        context.setClazzId(clazzId);
        context.setGroupId(groupId);
        context.setHomeworkId(homeworkId);
        context.setBookId(bookId);
        context.setUnitId(unitId);
        context.setLessonId(lessonId);
        context.setPointId(pointId);
        context.setQuestionNum(questionNum);
        context.setPackageId(packageId);
        context.setDataType(dataType);

        return context;
    }

}
