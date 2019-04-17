package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.util.MapUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.campaign.client.StudentActivityServiceClient;
import com.voxlearning.utopia.service.campaign.mapper.StudentParticipated;
import com.voxlearning.utopia.service.crm.api.ActivityConfigLoader;
import com.voxlearning.utopia.service.crm.api.ActivityConfigService;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class LoadStudentAppActivityCard extends AbstractStudentAppIndexDataLoader {

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    private StudentActivityServiceClient activityServiceClient;

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {
        StudentDetail student = context.getStudent();
        Long clazzId = student.getClazzId();
        if (clazzId == null) return context;

        List<GroupMapper> studentGroups = context.__studentGroups;
        if (studentGroups.isEmpty()) {
            return context;
        }

        boolean haveMathTeacher = studentGroups.stream().anyMatch(i -> Objects.equals(i.getSubject(), Subject.MATH));
        List<ActivityConfig> activityConfigs = activityConfigServiceClient.getCanParticipateActivity(student);
        ActivityConfig clazzConfig = ObjectUtils.get(() -> activityConfigServiceClient.loadClazzActivityConfig(Collections.singleton(student.getClazzId())).get(student.getClazzId()));
        if (clazzConfig != null) {
            activityConfigs.add(clazzConfig);
        }
        // 今日已参加的活动不再显示卡片
        List<ActivityConfig> result = new ArrayList<>();
        for (ActivityConfig config : activityConfigs) {
            // 如果活动限制数学学科,学生却没有数学老师, 则不显示
            boolean limitMath = config.limitMath();
            if (limitMath && !haveMathTeacher) continue;

            // 如果没报名,则不显示 过滤掉老师的布置
            if (isNewActivity(config.getId()) && !config.hasTeacher()) {
                Boolean isNotSignUp = activityConfigServiceClient.isNotSignUp(businessCacheSystem.CBS.persistence, config.getId(), student.getClazzId());
                if (isNotSignUp) continue;
            }

            StudentParticipated isProhibit = new StudentParticipated(true); // 默认允许进入

            if (config.getType() == ActivityTypeEnum.TANGRAM) {
                isProhibit = activityServiceClient.allowParticipatedTangram(student.getId(), config.getId());
            } else if (config.getType() == ActivityTypeEnum.TWENTY_FOUR) {
                isProhibit = activityServiceClient.allowParticipatedTwentyFour(student.getId(), config.getId());
            } else if (config.getType() == ActivityTypeEnum.SUDOKU) {
                isProhibit = activityServiceClient.allowParticipatedSudoku(student.getId(), config.getId(),
                        config.getRules().getPattern().name(),
                        config.getRules().getLimitTime(),
                        config.getRules().getLimitAmount());
            }
            if (isProhibit.getAllow()) result.add(config);
        }

        if (CollectionUtils.isEmpty(result)) return context;

        // 不知道壳需要哪些参数,仿照作业卡片写的
        for (ActivityConfig activityConfig : result) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("homeworkId", "");
            resultMap.put("endDate", activityConfig.getEndTime());
            resultMap.put("homeworkType", "EXPAND_BASIC_REVIEW_MATH");
            resultMap.put("desc", activityConfig.getTitle());
            resultMap.put("makeup", true);
            resultMap.put("subject", "MATH");
            resultMap.put("types", Collections.singletonList("BASIC_REVIEW"));
            resultMap.put("params", JsonUtils.toJson(MapUtils.m("packageId", "", "subject", "MATH", "activityId", activityConfig.getId())));
            resultMap.put("url", activityConfig.getType().getUrl() + activityConfig.getId());
            resultMap.put("startComment", "参与活动");
            context.__basicReviewHomeworkCards.add(resultMap);
        }
        return context;
    }

    private boolean isNewActivity(String activityId) {
        if (StringUtils.isEmpty(activityId) || activityId.length() < 24) {
            return false;
        }
        int timestamp = new ObjectId(activityId).getTimestamp();
        return timestamp >= ActivityConfigService.TEACHER_SIGN_UP_ON_LINE_TIME;
    }
}
