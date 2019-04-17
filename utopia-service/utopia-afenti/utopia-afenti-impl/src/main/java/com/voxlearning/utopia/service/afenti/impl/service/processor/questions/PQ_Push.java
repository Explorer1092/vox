package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.afenti.impl.util.UserAbtestLoaderClientHelper;
import com.voxlearning.utopia.service.psr.consumer.UtopiaPsrServiceClient;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.psr.entity.PsrExamItem;
import com.voxlearning.utopia.service.question.api.entity.AfentiPreviewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionMapper;
import com.voxlearning.utopia.service.question.consumer.AfentiQuestionsLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.AbtestMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.NO_QUESTION;

/**
 * @author Ruib
 * @since 2016/7/18
 */
@Named
public class PQ_Push extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {
    @Inject private UtopiaPsrServiceClient utopiaPsrServiceClient;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private AfentiQuestionsLoaderClient afentiQuestionsLoaderClient;
    @Inject private UserAbtestLoaderClientHelper userAbtestLoaderClientHelper;

    @Override
    public void execute(PushQuestionContext context) {
        if (context.getLearningType() != AfentiLearningType.castle) {
            return;
        }
        // 终极关卡还走这里的推题
        if (context.getIsNewRankBook() && UtopiaAfentiConstants.getUnitType(context.getUnitId()) != UnitRankType.ULTIMATE) {
            return;
        }
        StudentDetail student = context.getStudent();
        int cityCode = SafeConverter.toInt(student.getCityCode());
        int clazzLevel = (null == student.getClazzLevelAsInteger()) ? 0 : student.getClazzLevelAsInteger();

        // 推题数量
        int qn = qn(student, context.getSubject(), clazzLevel);

        PsrExamContent psr;

        //中学英语取题
        if (student.getClazz().getClazzLevel().getLevel() > 6 &&
                student.getClazz().getClazzLevel().getLevel() < 10 &&
                context.getSubject() == Subject.ENGLISH) {
            List<AfentiPreviewQuestion> questionList = afentiQuestionsLoaderClient.loadPreviewQuestions(context.getBookId(), context.getUnitId(), context.getRank());
            if (CollectionUtils.isEmpty(questionList) || questionList.size() < 3) {
                logger.error("Afenti Primary English load question error, book {}, unit {}, rank {}", context.getBookId(), context.getUnitId(), context.getRank());
                context.setErrorCode(NO_QUESTION.getCode());
                context.errorResponse(NO_QUESTION.getInfo());
                return;
            }

            psr = new PsrExamContent();
            List<PsrExamItem> items = new ArrayList<>();
            questionList.forEach(question -> {
                PsrExamItem item = new PsrExamItem();
                item.setEid(question.getQuestionId());
                item.setAlogv("");
                item.setEk(question.getKnowledgePointId());
                item.setEt(question.getContentTypeId() == null ? "" : question.getContentTypeId().toString());
                item.setWeight(-1);
                items.add(item);
            });
            psr.setExamList(items);
            context.setPsr(psr);
            return;
        }

        try {
            if (context.isUltimate()) {
                psr = utopiaPsrServiceClient.getPsrExam("AFENTI", "student", context.getStudentId(), cityCode,
                        context.getBookId(), "-1", qn, (float) 0.7, (float) 0.85, clazzLevel, context.getSubject());
            } else {
                psr = utopiaPsrServiceClient.getPsrExam("AFENTI", "student", context.getStudentId(), cityCode,
                        context.getBookId(), context.getUnitId(), qn, (float) 0.7, (float) 0.85, clazzLevel, context.getSubject());
            }
        } catch (Exception ex) {
            logger.error("PQ_Push afenti call athena error " + context.getSubject().name(), ex);
            context.setErrorCode(DEFAULT.getCode());
            context.errorResponse(DEFAULT.getInfo());
            return;
        }

        if (psr == null || CollectionUtils.isEmpty(psr.getExamList()) || psr.getExamList().size() < qn) {
            logger.error("afenti call athena no qid " + context.getSubject().name());
            context.setErrorCode(DEFAULT.getCode());
            context.errorResponse(DEFAULT.getInfo());
            return;
        }

        // 测试环境可能有一些垃圾，干掉
        if (RuntimeMode.current().lt(Mode.STAGING)) {
            Map<String, QuestionMapper> questions = questionLoaderClient.loadQuestionMapperByQids(
                    psr.getExamList().stream().map(PsrExamItem::getEid).collect(Collectors.toSet()), true, true, true);

            psr.setExamList(psr.getExamList().stream().filter(e -> questions.containsKey(e.getEid()))
                    .collect(Collectors.toList()));

            if (CollectionUtils.isEmpty(psr.getExamList())) {
                context.setErrorCode(DEFAULT.getCode());
                context.errorResponse(DEFAULT.getInfo());
                return;
            }
        }

        context.setPsr(psr);
    }

    private int qn(StudentDetail student, Subject subject, int clazzLevel) {
        int qn;
        switch (subject) {
            case ENGLISH: {
                if (clazzLevel <= 2) {
                    qn = 9;
                } else {
                    String experimentId = RuntimeMode.lt(Mode.STAGING) ? "58535f9ce92b1b70a51d641d" : "5853606e1e346b17d4708409";
                    AbtestMapper abtestMapper = userAbtestLoaderClientHelper.generateUserAbtestInfo(student.getId(), experimentId);
                    if (abtestMapper.getPlanName().equals("A")) {
                        qn = 6;
                    } else {
                        qn = 12;
                    }
                }
                break;
            }
            case MATH: {
                qn = 6;
                break;
            }
            case CHINESE: {
                qn = RuntimeMode.current().lt(Mode.STAGING) ? 3 : 9;
                break;
            }
            default:
                qn = 0;
        }

        return qn;
    }
}
