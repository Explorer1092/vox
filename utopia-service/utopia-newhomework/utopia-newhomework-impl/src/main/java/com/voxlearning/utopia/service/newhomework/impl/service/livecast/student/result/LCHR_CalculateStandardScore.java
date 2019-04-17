package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusDubbing;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.impl.dao.PictureBookPlusDubbingDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCHR_CalculateStandardScore extends SpringContainerSupport implements LiveCastHomeworkResultTask {

    @Inject private PracticeServiceClient practiceServiceClient;
    @Inject private PictureBookPlusDubbingDao pictureBookPlusDubbingDao;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {
        List<NewHomeworkQuestion> questions;
        List<NewHomeworkQuestion> oralQuestions;
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())) {
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(SafeConverter.toLong(context.getPracticeId()));
            if (practiceType == null) {
                logger.error("LiveCastHomework {} practice not exist {}", context.getHomeworkId(), context.getPracticeId());
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PRACTICE_NOT_EXIST);
                return;
            }
            questions = context.getLiveCastHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), context.getLessonId(), practiceType.getCategoryId());
        } else if (ObjectiveConfigType.READING.equals(context.getObjectiveConfigType())) {
            questions = context.getLiveCastHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), context.getPictureBookId());
            oralQuestions = context.getLiveCastHomework().findNewHomeworkOralQuestions(context.getObjectiveConfigType(), context.getPictureBookId());
            if (CollectionUtils.isNotEmpty(oralQuestions)) {
                questions.addAll(oralQuestions);
            }
        } else if (ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())) {
            questions = new ArrayList<>();
            questions.addAll(context.getLiveCastHomework().findNewHomeworkQuestions(context.getObjectiveConfigType(), context.getPictureBookId()));
            questions.addAll(context.getLiveCastHomework().findNewHomeworkOralQuestions(context.getObjectiveConfigType(), context.getPictureBookId()));
            List<NewHomeworkApp> newHomeworkApps = context.getLiveCastHomework().findNewHomeworkApps(context.getObjectiveConfigType());
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
        } else if (ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())) {
            if (StringUtils.isBlank(context.getVideoUrl())) {
                context.errorResponse("配音作品为空");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VIDEO_URL_IS_NULL);
                return;
            }
            questions = context.getLiveCastHomework().findNewHomeworkDubbingQuestions(context.getObjectiveConfigType(), context.getDubbingId());
        } else {
            questions = context.getLiveCastHomework().findNewHomeworkQuestions(context.getObjectiveConfigType());
        }

        if (CollectionUtils.isEmpty(questions)) {
            logger.error("LiveCastHomework {} does not contain practice {}", context.getHomeworkId(), context.getObjectiveConfigType());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }

        Map<String, Double> standardScore = new HashMap<>();
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.READING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())) {
            questions.forEach(e -> standardScore.put(e.getQuestionId(), e.getScore()));
        } else {
            NewHomeworkQuestion question = questions.stream()
                    .filter(q -> StringUtils.equals(q.getQuestionId(), context.getStudentHomeworkAnswers().get(0).getQuestionId()))
                    .findFirst()
                    .orElse(null);

            if (question == null) {
                logger.error("LiveCastHomework {} does not contain question {}", context.getHomeworkId(), context.getStudentHomeworkAnswers());
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
                return;
            }
            standardScore.put(question.getQuestionId(), question.getScore());
        }
        context.setStandardScore(standardScore);
    }
}
