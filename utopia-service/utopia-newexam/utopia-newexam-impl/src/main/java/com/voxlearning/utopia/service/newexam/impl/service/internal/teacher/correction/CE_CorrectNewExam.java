package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamProcessResultDao;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamRegistrationDao;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamResultDao;
import com.voxlearning.utopia.service.newexam.impl.queue.NewExamQueueProducer;
import com.voxlearning.utopia.service.question.api.entity.NewExam;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Named
public class CE_CorrectNewExam extends SpringContainerSupport implements CorrectNewExamTask {
    @Inject private NewExamResultDao newExamResultDao;
    @Inject private NewExamProcessResultDao newExamProcessResultDao;
    @Inject private NewExamRegistrationDao newExamRegistrationDao;
    @Inject private NewExamQueueProducer newExamQueueProducer;

    @Override
    public void execute(CorrectNewExamContext context) {
        NewExam newExam = context.getNewExam();
        Map<String, NewExamResult> newExamResultMap = context.getNewExamResultMap();
        Map<String, NewExamRegistration> newExamRegistrationMap = context.getNewExamRegistrationMap();
        Map<Long, NewExamProcessResult> newExamProcessResultMap = context.getNewExamProcessResultMap();
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();

        List<Map<String, Object>> errorUsers = new ArrayList<>();
        List<Map<String, Object>> successUsers = new ArrayList<>();
        for (Long userId : context.getUserScoreMap().keySet()) {
            Double correctScore = context.getUserScoreMap().get(userId);
            NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), SafeConverter.toString(userId));
            NewExamResult newExamResult = newExamResultMap.get(id.toString());
            NewExamRegistration newExamRegistration = newExamRegistrationMap.get(id.toString());
            NewExamProcessResult newExamProcessResult = newExamProcessResultMap.get(userId);
            Map<String, Object> user = new HashMap<>();
            user.put("userId", userId);
            if (newExamResult == null || newExamRegistration == null || newExamProcessResult == null) {
                user.put("errorInfo", JsonUtils.toJson(MiscUtils.m("newExamResultIsNull", newExamResult == null, "newExamRegistrationIsNull", newExamRegistration == null, "newExamProcessResultIsNull", newExamProcessResult == null)));
                errorUsers.add(user);
            } else {
                if (context.getIsNewOral()) {
                    Integer subId = context.getSubId();
                    Double subStandardScore = context.getSubStandardScore();
                    if (correctScore > subStandardScore) {
                        user.put("errorInfo", "批改分数{" + correctScore + "}>{最高分" + subStandardScore + "}");
                        errorUsers.add(user);
                    } else {
                        List<Double> subScore = newExamProcessResult.getCorrectSubScore() != null ? newExamProcessResult.getCorrectSubScore() : newExamProcessResult.getSubScore();
                        if (subScore.size() < subId) {
                            user.put("errorInfo", "子题id错误");
                            errorUsers.add(user);
                        } else {
                            double differenceSubScore = correctScore - subScore.get(subId);
                            double newExamProcessResultScore = (newExamProcessResult.getCorrectScore() != null ? newExamProcessResult.getCorrectScore() :
                                    SafeConverter.toDouble(newExamProcessResult.getScore())) + differenceSubScore;
                            double newExamResultScore = (newExamResult.getCorrectScore() != null ? newExamResult.getCorrectScore() :
                                    SafeConverter.toDouble(newExamResult.getScore())) + differenceSubScore;
                            subScore.set(subId, correctScore);
                            NewExamProcessResult result = newExamProcessResultDao.correctNewExam(newExamProcessResult.getId(), newExamProcessResultScore, subScore);
                            newExamQueueProducer.sendSaveResultMessage(result);
                            newExamRegistrationDao.correctNewExam(id.toString(), newExamResultScore);
                            newExamResultDao.correctNewExam(id.toString(), newExamResultScore);
                            successUsers.add(user);
                        }
                    }
                } else {
                    if (correctScore > context.getStandardScore()) {
                        user.put("errorInfo", "批改分数{" + correctScore + "}>{最高分" + context.getStandardScore() + "}");
                        errorUsers.add(user);
                    } else {
                        double differenceScore = correctScore - (newExamProcessResult.getCorrectScore() != null ? newExamProcessResult.getCorrectScore() : SafeConverter.toDouble(newExamProcessResult.getScore()));
                        double newExamResultScore = (newExamResult.getCorrectScore() != null ? newExamResult.getCorrectScore() : SafeConverter.toDouble(newExamResult.getScore())) + differenceScore;
                        NewExamProcessResult result = newExamProcessResultDao.correctNewExam(newExamProcessResult.getId(), correctScore, null);
                        newExamQueueProducer.sendSaveResultMessage(result);
                        newExamRegistrationDao.correctNewExam(id.toString(), newExamResultScore);
                        newExamResultDao.correctNewExam(id.toString(), newExamResultScore);
                        successUsers.add(user);
                    }
                }
            }

        }
        context.setCorrectErrorUsers(errorUsers);
        context.setCorrectSuccessUsers(successUsers);
    }
}
