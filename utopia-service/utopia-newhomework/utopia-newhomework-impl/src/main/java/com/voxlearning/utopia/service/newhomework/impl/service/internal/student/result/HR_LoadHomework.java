package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 获取大作业信息
 *
 * @author Ruib
 * @version 0.1
 * @since 2016/1/14
 */
@Named
public class HR_LoadHomework extends SpringContainerSupport implements HomeworkResultTask {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    public void execute(HomeworkResultContext context) {
        NewHomework homework = newHomeworkLoader.load(context.getHomeworkId());
        if (homework == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST,
                    "op", "student homework result"
            ));
            logger.error("NewHomework {} not found", context.getHomeworkId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            return;
        }

        if (homework.getCreateAt() != null && homework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            context.errorResponse("此份作业已不允许作答");
            return;
        }

        Subject subject = homework.getSubject();
        if (Objects.equals(subject, Subject.UNKNOWN)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SUBJECT,
                    "op", "student homework result"
            ));
            logger.error("Cannot recognize NewHomework subject {}", homework.getSubject());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
            return;
        }

        context.setHomework(homework);
        context.setNewHomeworkType(homework.getType());
        context.setClazzGroupId(homework.getClazzGroupId());
        context.setSubject(subject);
        context.setRepair(homework.isHomeworkTerminated());
//
//        // 单独加的校验
//        if (StringUtils.equals(ObjectiveConfigType.BASIC_APP.name(), context.getObjectiveConfigType().name())) {
//            // 校验基础训练提交的题数和作业数量是否一致
//            PracticeType practiceType = practiceLoaderClient.loadPractice(SafeConverter.toLong(context.getPracticeId()));
//            if (practiceType != null) {
//                Integer categoryId = practiceType.getCategoryId() != null ? practiceType.getCategoryId() : 0;
//                String lessonId = context.getLessonId();
//
//                // 用户提交的题量
//                List<StudentHomeworkAnswer> userAnswers = context.getStudentHomeworkAnswers();
//                // 作业中的题量
//                List<NewHomeworkQuestion> questionList = homework.findNewHomeworkQuestions(ObjectiveConfigType.BASIC_APP, lessonId, categoryId);
//
//                if (CollectionUtils.isEmpty(questionList) || CollectionUtils.isEmpty(userAnswers) || userAnswers.size() != questionList.size()) {
//                    LogCollector.info("backend-general", MiscUtils.map(
//                            "env", RuntimeMode.getCurrentStage(),
//                            "usertoken", context.getUserId(),
//                            "agent", context.getUserAgent(),
//                            "mod1", context.getHomeworkId(),
//                            "mod2", ErrorCodeConstants.ERROR_CODE_SUBJECT,
//                            "mod3", JsonUtils.toJson(userAnswers),
//                            "mod4", JsonUtils.toJson(questionList),
//                            "op", "question count error"
//                    ));
//                    logger.error("question count error client:{}-{}, homeworkId:{}, userQuestion:{}", context.getClientType(), context.getClientName(), homework.getId(), JsonUtils.toJson(userAnswers));
//                    context.errorResponse("题目数量错误，请刷新后重试");
//                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
//                }
//            }
//        }
    }
}
