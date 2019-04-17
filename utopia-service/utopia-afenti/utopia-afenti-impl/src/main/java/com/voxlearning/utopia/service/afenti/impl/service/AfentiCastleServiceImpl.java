/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.afenti.api.AfentiCastleService;
import com.voxlearning.utopia.service.afenti.api.constant.*;
import com.voxlearning.utopia.service.afenti.api.context.*;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserBookRef;
import com.voxlearning.utopia.service.afenti.impl.athena.AfentiPushQuestionServiceClient;
import com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiParentRewardService;
import com.voxlearning.utopia.service.afenti.impl.service.processor.book.FetchGradeBookProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.login.LoginProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.questions.FetchRankQuestionsDataProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.FetchQuizReportProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.QuizResultProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term.FetchTermQuizInfoProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term.FetchTermQuizQuestionProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term.FetchTermQuizReportProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term.TermQuizResultProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit.FetchQuizInfoProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit.FetchQuizQuestionProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.ranks.FetchUnitRankDataProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.result.CastleResultProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.review.FetchReviewRanksDataProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.review.homeRanking.FetchReviewHomeRankingDataProceesor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions.FetchReviewRankQuestionsDataProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.review.ranking.FetchReviewRankingProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.review.result.ReviewResultProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.units.FetchBookUnitsDataProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.video.prepraration.FetchPreparationVideoDataProcessor;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.NEED_LOGIN;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_GRADE;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * @author Ruib
 * @since 2016/6/28
 */
@Named
@ExposeService(interfaceClass = AfentiCastleService.class)
public class AfentiCastleServiceImpl extends UtopiaAfentiSpringBean implements AfentiCastleService {

    @Inject private AfentiServiceImpl afentiService;
    @Inject private LoginProcessor loginProcessor;
    @Inject private FetchGradeBookProcessor fetchGradeBookProcessor;
    @Inject private FetchBookUnitsDataProcessor fetchBookUnitsDataProcessor;
    @Inject private FetchUnitRankDataProcessor fetchUnitRankDataProcessor;
    @Inject private FetchRankQuestionsDataProcessor fetchRankQuestionsDataProcessor;
    @Inject private CastleResultProcessor castleResultProcessor;
    @Inject private FetchQuizInfoProcessor fetchQuizInfoProcessor;
    @Inject private FetchQuizQuestionProcessor fetchQuizQuestionProcessor;
    @Inject private FetchQuizReportProcessor fetchQuizReportProcessor;
    @Inject private QuizResultProcessor quizResultProcessor;
    @Inject private FetchTermQuizInfoProcessor fetchTermQuizInfoProcessor;
    @Inject private FetchTermQuizQuestionProcessor fetchTermQuizQuestionProcessor;
    @Inject private FetchTermQuizReportProcessor fetchTermQuizReportProcessor;
    @Inject private TermQuizResultProcessor termQuizResultProcessor;
    @Inject private FetchPreparationVideoDataProcessor fetchPreparationVideoDataProcessor;
    @Inject private AfentiPushQuestionServiceClient afentiPushQuestionServiceClient;
    @Inject private FetchReviewRanksDataProcessor fetchReviewRanksDataProcessor;
    @Inject private FetchReviewRankQuestionsDataProcessor fetchReviewRankQuestionsDataProcessor;
    @Inject private ReviewResultProcessor reviewResultProcessor;
    @Inject private AfentiParentRewardService afentiParentRewardService;
    @Inject private AfentiQueueProducer afentiQueueProducer;
    @Inject private FetchReviewRankingProcessor fetchReviewRankingProcessor;
    @Inject private FetchReviewHomeRankingDataProceesor fetchReviewHomeRankingDataProceesor;

    @Override
    public Map<String, Object> login(StudentDetail student, Subject subject) {
        if (null == student || !AfentiUtils.isSubjectAvailable(subject)) return Collections.emptyMap();

        LoginContext context;
        try {
            context = loginProcessor.process(new LoginContext(student, subject));
            if (context.isSuccessful()) return context.getResult();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Collections.emptyMap();
    }

    @Override
    public MapMessage activeBook(Long studentId, String bookId, Subject subject, AfentiLearningType type) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).add("errorCode", NEED_LOGIN.getCode());
        if (StringUtils.isBlank(bookId) || !AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).add("errorCode", DEFAULT.getCode());

        AfentiBook afentiBook = (AfentiBook) afentiService.fetchAfentiBook(studentId, subject, type).get("book");
        if (null == afentiBook) return MapMessage.errorMessage(DEFAULT.getInfo()).add("errorCode", DEFAULT.getCode());

        // 没有换书
        if (Objects.equals(afentiBook.book.getId(), bookId)) return MapMessage.successMessage();

        // 验证教材
        if (MapUtils.isEmpty(newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singletonList(bookId))))
            return MapMessage.errorMessage(DEFAULT.getInfo()).add("errorCode", DEFAULT.getCode());

        try {
            // 将原来正在使用的教材变成未使用状态
            afentiLearningPlanUserBookRefPersistence.inactivate(studentId, subject, type);
            // 将当前教材变成使用中状态
            if (!afentiLearningPlanUserBookRefPersistence.activate(studentId, subject, bookId, type)) {
                afentiLearningPlanUserBookRefPersistence.persist(AfentiLearningPlanUserBookRef
                        .newInstance(studentId, true, bookId, subject, type));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).add("errorCode", DEFAULT.getCode());
        }
        return MapMessage.successMessage();
    }

    @Override
    public List<NewBookProfile> fetchChangeBookHistory(Long studentId, Subject subject, AfentiLearningType type) {
        if (null == studentId || !AfentiUtils.isSubjectAvailable(subject)) return Collections.emptyList();
        List<AfentiBook> books = afentiService.fetchAfentiBooks(studentId, subject, type);
        return books.stream().map(b -> b.book).collect(Collectors.toList());
    }

    @Override
    public List<NewBookProfile> fetchGradeBookList(ClazzLevel clazzLevel, Subject subject, AfentiLearningType type) {
        if (!AVAILABLE_SUBJECT.contains(subject) || (!(clazzLevel.getLevel() > 6 && clazzLevel.getLevel() < 10 && subject == Subject.ENGLISH) //中学英语
                && !AVAILABLE_GRADE.contains(clazzLevel)))
            return Collections.emptyList();

        FetchGradeBookContext context;
        try {
            context = fetchGradeBookProcessor.process(new FetchGradeBookContext(clazzLevel, subject, type));
            return context.getBooks();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> fetchBookUnits(StudentDetail student, Subject subject) {
        return fetchBookUnits(student, subject, AfentiLearningType.castle);
    }

    @Override
    public Map<String, Object> fetchBookUnits(StudentDetail student, Subject subject, AfentiLearningType learningType) {
        if (null == student || null == student.getClazz() || !AfentiUtils.isSubjectAvailable(subject))
            return MiscUtils.m("units", new ArrayList<>(), "bookName", "");

        FetchBookUnitsContext context;
        try {
            context = fetchBookUnitsDataProcessor.process(new FetchBookUnitsContext(student, subject, learningType));
            if (context.isSuccessful())
                return MiscUtils.m("units", context.getUnits(), "bookName", context.getBookName());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MiscUtils.m("units", new ArrayList<>(), "bookName", "");
    }

    @Override
    public MapMessage fetchPreparationVideo(StudentDetail student, Subject subject) {
        if (null == student || null == student.getClazz())
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());

        FetchPreparationVideoContext context;
        try {
            context = fetchPreparationVideoDataProcessor.process(new FetchPreparationVideoContext(student, subject));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("videos", context.getVideos())
                        .add("bookName", context.getBookName());
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public Map<String, Object> fetchUnitRanks(StudentDetail student, String unitId, Subject subject, AfentiLearningType learningType) {
        if (null == student || !AfentiUtils.isSubjectAvailable(subject) || StringUtils.isBlank(unitId))
            return MiscUtils.m("ranks", new ArrayList<>(), "count", 0);

        try {
            // 判断单元类型
            UnitRankType rankType = UtopiaAfentiConstants.getUnitType(unitId);
            if (rankType == null) {
                return MiscUtils.m("ranks", new ArrayList<>(), "count", 0);
            }
            FetchUnitRanksContext context = fetchUnitRankDataProcessor.process(new FetchUnitRanksContext(student, subject, unitId, learningType, rankType));
            if (context.isSuccessful())
                return MiscUtils.m("ranks", context.getRanks(), "count", context.getCount(), "unitName", context.getUnitName());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MiscUtils.m("ranks", new ArrayList<>(), "count", 0, "unitName", "");
    }

    @Override
    public Map<String, Object> fetchReviewRanks(StudentDetail student, Subject subject) {
        if (null == student || !AfentiUtils.isSubjectAvailable(subject))
            return MiscUtils.m("ranks", new ArrayList<>(), "count", 0, "bookName", "","homeJoined", 0, "paid", false);
        try {
            FetchReviewRanksContext context = fetchReviewRanksDataProcessor.process(new FetchReviewRanksContext(student, subject));
            if (context.isSuccessful())
                return MiscUtils.m("ranks", context.getRanks(), "count", context.getCount(), "bookName", context.getBookName(),"homeJoined", context.getHomeJoined(), "paid", context.isPaid());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MiscUtils.m("ranks", new ArrayList<>(), "count", 0, "bookName", "", "homeJoined", 0, "paid", false);
    }

    @Override
    public MapMessage fetchQuestions(StudentDetail student, String unitId, Integer rank, Subject subject, AfentiLearningType learningType) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject) || StringUtils.isBlank(unitId) || null == rank)
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        FetchRankQuestionsContext context;
        try {
            context = fetchRankQuestionsDataProcessor.process(new FetchRankQuestionsContext(student, subject, unitId, rank, learningType));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("knowledges", context.getKnowledges())
                        .add("questions", context.getQuestions())
                        .add("sectionInfo", context.getSectionInfo());
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public MapMessage fetchReviewQuestions(StudentDetail student, String unitId, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject) || StringUtils.isBlank(unitId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            FetchReviewQuestionsContext context = fetchReviewRankQuestionsDataProcessor.process(new FetchReviewQuestionsContext(student, subject, unitId));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("knowledges", context.getKnowledges())
                        .add("questions", context.getQuestions());
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public MapMessage processCastleResult(CastleResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage();

        CastleResultContext context;
        try {
            context = castleResultProcessor.process(ctx);
            if (context.isSuccessful()) {
                MapMessage mesg = MapMessage.successMessage();
                mesg.putAll(context.getResult());
                return mesg;
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public MapMessage processReveiewResult(ReviewResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage();
        ReviewResultContext context;
        try {
            context = reviewResultProcessor.process(ctx);
            if (context.isSuccessful()) {
                MapMessage mesg = MapMessage.successMessage();
                mesg.putAll(context.getResult());
                return mesg;
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public Map<String, Object> fetchQuizInfo(StudentDetail student, Subject subject, String unitId) {
        if (null == student || !AfentiUtils.isQuizSubjectAvailable(subject)) return Collections.emptyMap();

        FetchQuizInfoContext context;
        try {
            context = fetchQuizInfoProcessor.process(new FetchQuizInfoContext(student, subject, unitId));
            if (context.isSuccessful()) return context.getResult();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Collections.emptyMap();
    }

    @Override
    public MapMessage fetchQuizQuestions(StudentDetail student, Subject subject, String unitId) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AfentiUtils.isQuizSubjectAvailable(subject) || StringUtils.isBlank(unitId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        FetchQuizQuestionContext context;
        try {
            context = fetchQuizQuestionProcessor.process(new FetchQuizQuestionContext(student, subject, unitId));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("kpc", context.getKpc()).add("qc", context.getQc())
                        .add("ic", context.getIc()).add("questions", context.getQuestions());
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public Map<String, Object> fetchQuizReport(StudentDetail student, Subject subject, AfentiQuizType quizType, String contentId) {
        if (null == student || !AfentiUtils.isQuizSubjectAvailable(subject) || null == quizType)
            return Collections.emptyMap();
        if (quizType == AfentiQuizType.TERM_QUIZ && subject != Subject.MATH) return Collections.emptyMap();

        FetchQuizReportContext context;
        try {
            context = fetchQuizReportProcessor.process(new FetchQuizReportContext(student, subject, quizType, contentId));
            if (context.isSuccessful()) return context.getResult();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Collections.emptyMap();
    }

    @Override
    public MapMessage processQuizResult(QuizResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage();

        QuizResultContext context;
        try {
            context = quizResultProcessor.process(ctx);
            if (context.isSuccessful()) {
                MapMessage mesg = MapMessage.successMessage();
                mesg.putAll(context.getResult());
                return mesg;
            } else {
                return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public Map<String, Object> fetchTermQuizInfo(StudentDetail student) {
        if (null == student || null == student.getClazz()) return Collections.emptyMap();

        FetchTermQuizInfoContext context;
        try {
            context = fetchTermQuizInfoProcessor.process(new FetchTermQuizInfoContext(student));
            if (context.isSuccessful()) return context.getResult();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Collections.emptyMap();
    }

    @Override
    public MapMessage fetchTermQuizQuestions(StudentDetail student, Subject subject, String bookId) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AfentiUtils.isTermQuizSubjectAvailable(subject) || StringUtils.isBlank(bookId))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        FetchTermQuizQuestionContext context;
        try {
            context = fetchTermQuizQuestionProcessor.process(new FetchTermQuizQuestionContext(student, subject, bookId));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("questions", context.getQuestions());
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public Map<String, Object> fetchTermQuizReport(StudentDetail student, Subject subject) {
        if (null == student || !AfentiUtils.isTermQuizSubjectAvailable(subject))
            return Collections.emptyMap();

        FetchTermQuizReportContext context;
        try {
            context = fetchTermQuizReportProcessor.process(new FetchTermQuizReportContext(student, subject));
            if (context.isSuccessful()) return context.getResult();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Collections.emptyMap();
    }

    @Override
    public MapMessage processTermQuizResult(TermQuizResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage();

        try {
            TermQuizResultContext context = termQuizResultProcessor.process(ctx);
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("result", context.getResult());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
    }

    @Override
    public MapMessage increaseFamilyJoinNumber(Long studentId, int number) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("T", AfentiQueueMessageType.FAMILY_JOIN);
        message.put("S", studentId);
        message.put("N", number);
        afentiQueueProducer.getProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage generateReviewFamilyReward(StudentDetail student, String unitId, Subject subject) {
        int number = afentiParentRewardService.sendReveiewParentReward(student, unitId, subject);
        return MapMessage.successMessage().add("count", number);
    }

    @Override
    public MapMessage fetchReviewRanking(StudentDetail student, Subject subject) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        try {
            FetchReviewRankingContext context = fetchReviewRankingProcessor.process(new FetchReviewRankingContext(student, subject));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("ranking", context.getRanking())
                        .add("totalRanks", context.getTotalRanks())
                        .add("totalQuestions", context.getTotalQuestions())
                        .add("rightRate", context.getRightRate());
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Override
    public MapMessage fetchReviewFamilyRanking(StudentDetail student) {
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        try {
            FetchReviewHomeRankingContext context = fetchReviewHomeRankingDataProceesor.process(new FetchReviewHomeRankingContext(student));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("ranking", context.getRanking())
                        .add("homeJoinNum", context.getHomeJoinNum())
                        .add("userName", context.getUserName());
            } else {
                return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }
}
