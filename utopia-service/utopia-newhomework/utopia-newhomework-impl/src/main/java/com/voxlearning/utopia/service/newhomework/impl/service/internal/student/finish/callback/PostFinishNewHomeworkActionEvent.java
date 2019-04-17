package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.queue.UserLevelEventQueueProducer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author jiangpeng
 * 成长体系 作业完成后触发。。。
 */
@Named
public class PostFinishNewHomeworkActionEvent extends SpringContainerSupport implements PostFinishHomework {

    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Inject
    private PracticeLoaderClient practiceLoaderClient;

    @Inject
    private ActionServiceClient actionServiceClient;
    @Inject
    private UserLevelEventQueueProducer userLevelEventQueueProducer;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        if (context == null)
            return;
        if (context.getResult() == null) {
            return;
        }
        if (context.getUserId() == null)
            return;
        Integer score = context.getResult().processScore();
        if (score == null) {
            score = 0;
        }
        Long userId = context.getUserId();
        //完成作业
        actionServiceClient.finishHomework(userId, context.getClazzGroup().getSubject(), score);

        if (null != context.getResult().getRepair() && context.getResult().getRepair()) {
            notifyUserLevelRepairHomework(userId, context.getClazzGroup().getSubject(), context.getHomeworkId());
        } else {
            notifyUserLevelFinishHomework(userId, context.getClazzGroup().getSubject(), context.getHomeworkId());
        }

        //口算 绘本 口语
        NewHomeworkResult result = context.getResult();
        if (result != null) {
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = result.getPractices();
            if (practices != null) {
                //口算
                NewHomeworkResultAnswer mentalResult = practices.get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                if (mentalResult != null) {
                    LinkedHashMap<String, String> answers = mentalResult.getAnswers();
                    if (answers != null) {
                        Set<String> processId = new HashSet<>(answers.values());
                        Map<String, NewHomeworkProcessResult> stringNewHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(context.getHomeworkId(), processId);
                        if (stringNewHomeworkProcessResultMap != null) {
                            Long rightCount = stringNewHomeworkProcessResultMap.values().stream().filter(NewHomeworkProcessResult::getGrasp).count();
                            actionServiceClient.finishMental(userId, rightCount.intValue());
                        }
                    }

                }
                //绘本
                NewHomeworkResultAnswer readingResult = practices.get(ObjectiveConfigType.LEVEL_READINGS);
                if (readingResult != null) {
                    LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = readingResult.getAppAnswers();
                    if (appAnswers != null) {
                        //作业分数精度不高 直接用 >=判断了
                        Long scoreOver60Count = appAnswers.values().stream().filter(t -> t.getScore() != null && t.getScore() >= 60).count();
                        actionServiceClient.finishReading(userId, scoreOver60Count.intValue());
                    }
                }
                //口语
                NewHomeworkResultAnswer basicAppResult = practices.get(ObjectiveConfigType.BASIC_APP);
                if (basicAppResult != null) {
                    LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = basicAppResult.getAppAnswers();
                    if (appAnswers != null) {
                        Long scoreOver80Count = appAnswers.values().stream().filter(t -> {
                            Long practiceId = t.getPracticeId();
                            PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
                            return practiceType != null && practiceType.getNeedRecord() && t.getScore() != null && t.getScore() >= 79.5;//作业报告里的分数四舍五入了,这里>=80分就要记一次所以用79.5,与作业报告一致
                        }).count();
                        actionServiceClient.finishOral(userId, scoreOver80Count.intValue());
                    }

                }
            }
        }
    }

    private void notifyUserLevelFinishHomework(Long userId, Subject subject, String homeworkId) {
        Map<String, Object> info = new HashMap<>();
        info.put("type", "FINISH_HOMEWORK");
        info.put("userId", userId);
        info.put("subject", subject.name());
        info.put("hid", homeworkId);

        userLevelEventQueueProducer.getMessageProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(info)));
    }

    private void notifyUserLevelRepairHomework(Long userId, Subject subject, String homeworkId) {
        Map<String, Object> info = new HashMap<>();
        info.put("type", "REPAIR_HOMEWORK");
        info.put("userId", userId);
        info.put("subject", subject.name());
        info.put("hid", homeworkId);

        userLevelEventQueueProducer.getMessageProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(info)));
    }
}
