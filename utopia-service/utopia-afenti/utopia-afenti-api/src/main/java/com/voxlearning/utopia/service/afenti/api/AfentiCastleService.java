package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Ruib
 * @since 2016/6/28
 */
@ServiceVersion(version = "20171206")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
public interface AfentiCastleService extends IPingable {

    Map<String, Object> login(StudentDetail student, Subject subject);

    MapMessage activeBook(Long studentId, String bookId, Subject subject, AfentiLearningType type);

    List<NewBookProfile> fetchChangeBookHistory(Long studentId, Subject subject, AfentiLearningType type);

    List<NewBookProfile> fetchGradeBookList(ClazzLevel clazzLevel, Subject subject, AfentiLearningType type);

    @Deprecated
    Map<String, Object> fetchBookUnits(StudentDetail student, Subject subject);

    Map<String, Object> fetchBookUnits(StudentDetail student, Subject subject, AfentiLearningType learningType);

    MapMessage fetchPreparationVideo(StudentDetail student, Subject subject);

    Map<String, Object> fetchUnitRanks(StudentDetail student, String unitId, Subject subject, AfentiLearningType learningType);

    Map<String, Object> fetchReviewRanks(StudentDetail student, Subject subject);

    MapMessage fetchQuestions(StudentDetail student, String unitId, Integer rank, Subject subject, AfentiLearningType learningType);

    MapMessage fetchReviewQuestions(StudentDetail student, String unitId, Subject subject);

    MapMessage processCastleResult(CastleResultContext ctx);

    MapMessage processReveiewResult(ReviewResultContext ctx);

    Map<String, Object> fetchQuizInfo(StudentDetail student, Subject subject, String unitId);

    MapMessage fetchQuizQuestions(StudentDetail student, Subject subject, String unitId);

    Map<String, Object> fetchQuizReport(StudentDetail student, Subject subject, AfentiQuizType quizType, String contentId);

    MapMessage processQuizResult(QuizResultContext ctx);

    Map<String, Object> fetchTermQuizInfo(StudentDetail student);

    MapMessage fetchTermQuizQuestions(StudentDetail student, Subject subject, String bookId);

    Map<String, Object> fetchTermQuizReport(StudentDetail student, Subject subject);

    MapMessage processTermQuizResult(TermQuizResultContext ctx);

    MapMessage increaseFamilyJoinNumber(Long studentId, int number);

    MapMessage generateReviewFamilyReward(StudentDetail student, String unitId, Subject subject);

    MapMessage fetchReviewRanking(StudentDetail student, Subject subject);

    MapMessage fetchReviewFamilyRanking(StudentDetail student);
}
