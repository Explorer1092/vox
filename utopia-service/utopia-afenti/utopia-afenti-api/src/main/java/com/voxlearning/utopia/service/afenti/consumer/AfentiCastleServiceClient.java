package com.voxlearning.utopia.service.afenti.consumer;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.afenti.api.AfentiCastleService;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.*;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.*;

/**
 * @author Ruib
 * @since 2016/6/28
 */
public class AfentiCastleServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(AfentiCastleServiceClient.class);

    @ImportService(interfaceClass = AfentiCastleService.class)
    private AfentiCastleService afentiCastleService;

    public MapMessage login(StudentDetail student, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> result;
        try {
            result = afentiCastleService.login(student, subject);
        } catch (Exception ex) {
            logger.error("Failed login user {}, subject {}", student.getId(), subject, ex);
            result = new LinkedHashMap<>();
        }

        if (MapUtils.isEmpty(result))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(result);

        return mesg;
    }

    public MapMessage activeBook(Long studentId, String bookId, Subject subject, AfentiLearningType type) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (StringUtils.isBlank(bookId) || !AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("activeAfentiBook")
                    .keys(studentId)
                    .callback(new AtomicCallback<MapMessage>() {
                        @Override
                        public MapMessage execute() {
                            return afentiCastleService.activeBook(studentId, bookId, subject, type);
                        }
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed activating book {} for user {}", bookId, studentId, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public List<NewBookProfile> fetchChangeBookHistory(Long studentId, Subject subject, AfentiLearningType type) {
        if (null == studentId || !AVAILABLE_SUBJECT.contains(subject))
            return Collections.emptyList();

        try {
            return afentiCastleService.fetchChangeBookHistory(studentId, subject, type);
        } catch (Exception ex) {
            logger.error("Failed getting user {} change book history", studentId, ex);
            return Collections.emptyList();
        }
    }

    public List<NewBookProfile> fetchGradeBookList(ClazzLevel clazzLevel, Subject subject, AfentiLearningType type) {
        if (null == clazzLevel || !AVAILABLE_SUBJECT.contains(subject) ||
                (!(clazzLevel.getLevel() > 6 && clazzLevel.getLevel() < 10 && subject == Subject.ENGLISH) //中学英语
                        && !AVAILABLE_GRADE.contains(clazzLevel)) || type == null)
            return Collections.emptyList();

        try {
            return afentiCastleService.fetchGradeBookList(clazzLevel, subject, type);
        } catch (Exception ex) {
            logger.error("Failed getting grade {} subject {} books.", clazzLevel, subject, ex);
            return Collections.emptyList();
        }
    }

    public MapMessage fetchBookUnits(StudentDetail student, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> map;
        try {
            map = afentiCastleService.fetchBookUnits(student, subject);
        } catch (Exception ex) {
            logger.error("Failed fetching user {} book units", student.getId(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(map);
        return mesg;
    }

    public MapMessage fetchBookUnits(StudentDetail student, Subject subject, AfentiLearningType learningType) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> map;
        try {
            map = afentiCastleService.fetchBookUnits(student, subject, learningType);
        } catch (Exception ex) {
            logger.error("Failed fetching user {} book units", student.getId(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(map);
        return mesg;
    }

    public MapMessage fetchUnitRanks(StudentDetail student, String unitId, Subject subject, AfentiLearningType type) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject) || StringUtils.isBlank(unitId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> map;
        try {
            map = afentiCastleService.fetchUnitRanks(student, unitId, subject, type);
        } catch (Exception ex) {
            logger.error("Failed fetching user {} unit ranks, the unit id is {}", student.getId(), unitId, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(map);
        return mesg;
    }

    public MapMessage fetchReviewRanks(StudentDetail student, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> map;
        try {
            map = afentiCastleService.fetchReviewRanks(student, subject);
        } catch (Exception ex) {
            logger.error("Failed fetchReviewRanks. userId {} , the subject is {}", student.getId(), subject, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(map);
        return mesg;
    }

    public MapMessage generateReviewFamilyReward(StudentDetail student, String unitId, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (StringUtils.isEmpty(unitId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("generateReviewFamilyReward")
                    .keys(student.getId())
                    .callback(new AtomicCallback<MapMessage>() {
                        @Override
                        public MapMessage execute() {
                            return afentiCastleService.generateReviewFamilyReward(student, unitId, subject);
                        }
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed generateReviewFamilyReward. userId {} , the unitId is {}", student.getId(), unitId, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchReviewRanking(StudentDetail student, Subject subject) {
        if (student == null)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        try {
            return afentiCastleService.fetchReviewRanking(student, subject);
        }  catch (Exception ex) {
            logger.error("Failed fetchReviewRanking. userId {} , the subject is {}", student.getId(), subject, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchReviewHomeRanking(StudentDetail student) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());

        try {
            return afentiCastleService.fetchReviewFamilyRanking(student);
        }  catch (Exception ex) {
            logger.error("Failed fetchReviewHomeRanking. userId {} ", student.getId(),  ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchQuestions(StudentDetail student, String unitId, Integer rank, Subject subject, AfentiLearningType type) {
        if (null == student || student.getId() == null)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject) || StringUtils.isBlank(unitId) || null == rank)
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("fetchQuestions")
                    .keys(student.getId(), unitId, rank)
                    .callback(() -> afentiCastleService.fetchQuestions(student, unitId, rank, subject, type))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed fetch questions for user {}, unit {}, rank {}", student.getId(), unitId, rank, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchReviewQuestions(StudentDetail student, String unitId, Subject subject) {
        if (null == student || student.getId() == null)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (StringUtils.isBlank(unitId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("fetchReviewQuestions")
                    .keys(student.getId(), unitId)
                    .callback(() -> afentiCastleService.fetchReviewQuestions(student, unitId, subject))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed fetchReviewQuestions for user {}, unit {}", student.getId(), unitId, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage processCastleResult(CastleResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("SaveAfentiCastleResult")
                    .keys(ctx.getStudent().getId())
                    .callback(() -> afentiCastleService.processCastleResult(ctx))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed save afenti castle result for context {}", JsonStringSerializer.getInstance().serialize(ctx), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage processReviewResult(ReviewResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("SaveAfentiReveiewResult")
                    .keys(ctx.getStudent().getId())
                    .callback(() -> afentiCastleService.processReveiewResult(ctx))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed save afenti review result for context {}", JsonStringSerializer.getInstance().serialize(ctx), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchQuizInfo(StudentDetail student, Subject subject, String unitId) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_QUIZ_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> result;
        try {
            result = afentiCastleService.fetchQuizInfo(student, subject, unitId);
        } catch (Exception ex) {
            logger.error("Failed fetch quiz info. user {}, subject {}, unit {}", student.getId(), subject, unitId, ex);
            result = new LinkedHashMap<>();
        }

        if (MapUtils.isEmpty(result))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(result);
        return mesg;
    }

    public MapMessage fetchQuizQuestions(StudentDetail student, Subject subject, String unitId) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AVAILABLE_QUIZ_SUBJECT.contains(subject) || StringUtils.isBlank(unitId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("fetchQuizQuestions")
                    .keys(student.getId(), unitId)
                    .callback(() -> afentiCastleService.fetchQuizQuestions(student, subject, unitId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed fetch quiz questions for user {}, unit {}", student.getId(), unitId, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchPeparationVideo(StudentDetail student, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());

        try {
            return afentiCastleService.fetchPreparationVideo(student, subject);
        } catch (Exception ex) {
            logger.error("Failed fetching math preparation video  user{} ", student.getId(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchQuizReport(StudentDetail student, Subject subject, AfentiQuizType quizType, String contentId) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AVAILABLE_QUIZ_SUBJECT.contains(subject) || StringUtils.isBlank(contentId) || null == quizType)
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        if (quizType == AfentiQuizType.TERM_QUIZ && subject != Subject.MATH)
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> result;
        try {
            result = afentiCastleService.fetchQuizReport(student, subject, quizType, contentId);
        } catch (Exception ex) {
            logger.error("Failed fetch quiz report. user {}, subject {}, type {}, content",
                    student.getId(), subject, quizType.name(), contentId, ex);
            result = new LinkedHashMap<>();
        }

        if (MapUtils.isEmpty(result))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(result);
        return mesg;
    }

    public MapMessage processQuizResult(QuizResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        if (!AVAILABLE_QUIZ_SUBJECT.contains(ctx.getSubject()))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("SaveAfentiQuizResult")
                    .keys(ctx.getStudent().getId())
                    .callback(() -> afentiCastleService.processQuizResult(ctx))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed save afenti quiz result for context {}", JsonStringSerializer.getInstance().serialize(ctx), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchTermQuizInfo(StudentDetail student) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());

        Map<String, Object> result;
        try {
            result = afentiCastleService.fetchTermQuizInfo(student);
        } catch (Exception ex) {
            logger.error("Failed fetch term quiz data for student {}", student.getId(), ex);
            result = new LinkedHashMap<>();
        }

        if (MapUtils.isEmpty(result))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(result);
        return mesg;
    }

    public MapMessage fetchTermQuizQuestions(StudentDetail student, Subject subject, String bookId) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AVAILABLE_TERM_QUIZ_SUBJECT.contains(subject) || StringUtils.isBlank(bookId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("fetchTermQuizQuestions")
                    .keys(student.getId(), bookId)
                    .callback(() -> afentiCastleService.fetchTermQuizQuestions(student, subject, bookId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed fetch term quiz questions for user {}, book {}", student.getId(), bookId, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchTermQuizReport(StudentDetail student, Subject subject) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AVAILABLE_TERM_QUIZ_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        Map<String, Object> result;
        try {
            result = afentiCastleService.fetchTermQuizReport(student, subject);
        } catch (Exception ex) {
            logger.error("Failed fetch term quiz report. user {}, subject {}", student.getId(), subject, ex);
            result = new LinkedHashMap<>();
        }

        if (MapUtils.isEmpty(result))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        MapMessage mesg = MapMessage.successMessage();
        mesg.putAll(result);
        return mesg;
    }

    public MapMessage processTermQuizResult(TermQuizResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        if (!AVAILABLE_TERM_QUIZ_SUBJECT.contains(ctx.getSubject()))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("SaveAfentiTermQuizResult")
                    .keys(ctx.getStudent().getId())
                    .callback(() -> afentiCastleService.processTermQuizResult(ctx))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed save afenti term quiz result for context {}", JsonStringSerializer.getInstance().serialize(ctx), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }
}
