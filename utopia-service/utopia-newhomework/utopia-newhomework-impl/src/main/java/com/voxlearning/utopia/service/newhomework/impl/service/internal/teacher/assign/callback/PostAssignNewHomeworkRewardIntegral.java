package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class PostAssignNewHomeworkRewardIntegral extends NewHomeworkSpringBean implements PostAssignHomework {

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        String configJson = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "ah_integral_reward");
        if(StringUtils.isNotBlank(configJson)){
            Map<String, Object> objMap = JsonUtils.fromJson(configJson);
            Date startAt = SafeConverter.toDate(objMap.get("startAt"));
            Date endAt = SafeConverter.toDate(objMap.get("endAt"));

            Date currentDate = new Date();
            if (startAt != null && endAt != null && currentDate.after(startAt) && currentDate.before(endAt)) {
                Boolean reward = false;
                Map<String, Object> typeMap = (Map<String, Object>) objMap.get("objectiveConfigType");
                String comment = IntegralType.TEACHER_ASSIGN_WEEKEND_HOMEWORK_REWARD.getDescription();
                int rewardIntegral = 0;
                for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
                    Subject subject = newHomework.getSubject();
                    Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContents = newHomework.findPracticeContents();
                    if (practiceContents != null) {
                        for(ObjectiveConfigType objectiveConfigType : practiceContents.keySet()){
                            rewardIntegral = SafeConverter.toInt(typeMap.get(objectiveConfigType.name()));
                            if(typeMap.keySet().contains(objectiveConfigType.name())){
                                if (ObjectiveConfigType.LEVEL_READINGS == objectiveConfigType && Subject.CHINESE == subject) {
                                    continue;
                                }
                                if(ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)) comment = "老师布置绘本作业任务奖励";
                                if(ObjectiveConfigType.MENTAL_ARITHMETIC.equals(objectiveConfigType)) comment = "老师布置口算作业任务奖励";
                                reward = true;
                                break;
                            }
                        }
                    }
                    break;
                }
                if(reward){
                    IntegralHistory integralHistory = new IntegralHistory(teacher.getId(),
                            IntegralType.TEACHER_ASSIGN_WEEKEND_HOMEWORK_REWARD, rewardIntegral);
                    integralHistory.setComment(comment);
                    String uniqueKey = StringUtils.join(Arrays.asList(IntegralType.TEACHER_ASSIGN_WEEKEND_HOMEWORK_REWARD.name(), DateUtils.dateToString(startAt, "yyyyMMdd"), teacher.getId()),"-");
                    integralHistory.setUniqueKey(uniqueKey);
                    userIntegralService.changeIntegral(integralHistory);
                }
            }
        }


    }
}
