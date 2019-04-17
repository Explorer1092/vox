package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusDubbing;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.impl.dao.PictureBookPlusDubbingDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取当前题目标准分
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_CalculateStandardScore extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private PracticeLoaderClient practiceLoaderClient;
    @Inject
    private PictureBookPlusDubbingDao pictureBookPlusDubbingDao;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        List<NewHomeworkQuestion> questions;
        List<NewHomeworkQuestion> oralQuestions;
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())) {
            PracticeType practiceType = practiceLoaderClient.loadPractice(SafeConverter.toLong(context.getPracticeId()));
            if (practiceType == null) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getVacationHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST,
                        "op", "student vacation homework result"
                ));
                logger.error("VacationHomework {} practice not exist {}", context.getVacationHomeworkId(), context.getPracticeId());
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST);
                return;
            }
            questions = context.getVacationHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), context.getLessonId(), practiceType.getCategoryId());
        } else if (ObjectiveConfigType.READING.equals(context.getObjectiveConfigType())) {
            questions = context.getVacationHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), context.getPictureBookId());
            oralQuestions = context.getVacationHomework().findNewHomeworkOralQuestions(context.getObjectiveConfigType(), context.getPictureBookId());
            if (CollectionUtils.isNotEmpty(oralQuestions)) {
                questions.addAll(oralQuestions);
            }
        } else if (ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())) {
            questions = new ArrayList<>();
            questions.addAll(context.getVacationHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), context.getPictureBookId()));
            questions.addAll(context.getVacationHomework().findNewHomeworkOralQuestions(context.getObjectiveConfigType(), context.getPictureBookId()));
            List<NewHomeworkApp> newHomeworkApps = context.getVacationHomework().findNewHomeworkApps(context.getObjectiveConfigType());
            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                NewHomeworkApp app = null;
                for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                    if (StringUtils.equalsIgnoreCase(newHomeworkApp.getPictureBookId(), context.getPictureBookId())) {
                        app = newHomeworkApp;
                        break;
                    }
                }
                // 当前绘本需要配音
                if (app != null && app.containsDubbing()) {
                    if (StringUtils.isBlank(context.getDubbingId())) {
                        context.errorResponse("绘本配音作品为空");
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DUBBING_ID_IS_NULL);
                        return;
                    } else {
                        PictureBookPlusDubbing pictureBookPlusDubbing = pictureBookPlusDubbingDao.load(context.getDubbingId());
                        if (pictureBookPlusDubbing == null) {
                            context.errorResponse("绘本配音作品不存在");
                            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DUBBING_NOT_EXISTS);
                            return;
                        } else {
                            context.setDubbingScore(pictureBookPlusDubbing.getScore());
                            context.setDubbingScoreLevel(pictureBookPlusDubbing.getScoreLevel());
                        }
                    }
                }
            }
        }else if (ObjectiveConfigType.KEY_POINTS.equals(context.getObjectiveConfigType())) {
            questions = context.getVacationHomework().findNewHomeworkKeyPointQuestions(context.getObjectiveConfigType(), context.getVideoId());
        } else if (ObjectiveConfigType.NEW_READ_RECITE.equals(context.getObjectiveConfigType()) || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(context.getObjectiveConfigType())) {
            questions = context.getVacationHomework().findNewHomeworkReadReciteQuestions(context.getObjectiveConfigType(), context.getQuestionBoxId());
        } else if (ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(context.getObjectiveConfigType())) {
            if (StringUtils.isBlank(context.getVideoUrl())) {
                context.errorResponse("配音作品为空");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VIDEO_URL_IS_NULL);
                return;
            }
            questions = context.getVacationHomework().findNewHomeworkDubbingQuestions(context.getObjectiveConfigType(), context.getDubbingId());
        } else {
            questions = context.getVacationHomework().findNewHomeworkQuestions(context.getObjectiveConfigType());
        }

        if (CollectionUtils.isEmpty(questions)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getVacationHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST,
                    "op", "student vacation homework result"
            ));
            logger.error("VacationHomework {} does not contain practice {}", context.getVacationHomeworkId(), context.getObjectiveConfigType());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }

        // 基础练习加上题目不一致的日志
        if (ObjectiveConfigType.BASIC_APP == context.getObjectiveConfigType() && CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
            Set<String> questionIds = questions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toSet());
            Set<String> answerQuestionIds = context.getStudentHomeworkAnswers().stream().map(StudentHomeworkAnswer::getQuestionId).collect(Collectors.toSet());
            if (!questionIds.equals(answerQuestionIds)) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getVacationHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST,
                        "answers", context.getStudentHomeworkAnswers(),
                        "homeworkId", context.getVacationHomeworkId(),
                        "lessonId", context.getLessonId(),
                        "practiceId", context.getPracticeId(),
                        "questionIds", questionIds,
                        "answerQuestionIds", answerQuestionIds,
                        "op", "BasicApp questions not match"
                ));
            }
        }

        Map<String, Double> standardScore = new HashMap<>();
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.READING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.KEY_POINTS.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NEW_READ_RECITE.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.MENTAL_ARITHMETIC.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(context.getObjectiveConfigType())) {
            questions.forEach(e -> standardScore.put(e.getQuestionId(), e.getScore()));
        } else {
            NewHomeworkQuestion question = questions.stream()
                    .filter(q -> StringUtils.equals(q.getQuestionId(), context.getStudentHomeworkAnswers().get(0).getQuestionId()))
                    .findFirst()
                    .orElse(null);

            if (question == null) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getVacationHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST,
                        "op", "student vacation homework result"
                ));
                logger.error("VacationHomework {} does not contain question {}", context.getVacationHomeworkId(), context.getStudentHomeworkAnswers());
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
                return;
            }
            standardScore.put(question.getQuestionId(), question.getScore());
        }
        context.setStandardScore(standardScore);
    }
}
