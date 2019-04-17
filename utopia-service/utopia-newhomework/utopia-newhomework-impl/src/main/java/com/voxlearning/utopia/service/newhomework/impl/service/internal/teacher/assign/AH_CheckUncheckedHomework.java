package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
public class AH_CheckUncheckedHomework extends AbstractAssignHomeworkProcessor {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    protected void doProcess(AssignHomeworkContext context) {
        Map<Long, List<NewHomework.Location>> groupHomeworkMap = newHomeworkLoader.loadNewHomeworksByClazzGroupIds(context.getGroupIds(), context.getTeacher().getSubject());
        // 检查分组是否有未检查作业
        for (Long groupId : context.getGroupIds()) {
            if (MapUtils.isNotEmpty(groupHomeworkMap) && CollectionUtils.isNotEmpty(groupHomeworkMap.get(groupId))) {
                //context.getNewHomeworkType().equals(h.getType())同类型的作业，布置作业（普通作业和类题作业）的前提条件是必须都没有为检查的作业
                boolean hasUnChecked = groupHomeworkMap.get(groupId).stream().filter(h -> context.getNewHomeworkType().equals(h.getType())).anyMatch(nh -> !nh.isChecked());
                //Activity还得判断HomeworkTag
                if (NewHomeworkType.Activity == context.getNewHomeworkType()) {
                    hasUnChecked = groupHomeworkMap.get(groupId).stream()
                            .filter(h -> context.getNewHomeworkType().equals(h.getType()))
                            .filter(h -> context.getHomeworkTag().equals(h.getHomeworkTag()))
                            .anyMatch(nh -> !nh.isChecked());
                }
                if (hasUnChecked) {
                    LogCollector.info("backend-general", MiscUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", context.getTeacher().getId(),
                            "mod2", ErrorCodeConstants.ERROR_CODE_HAVE_UNCHECK_HOMEWORK,
                            "mod3", JsonUtils.toJson(context.getSource()),
                            "op", "assign homework"
                    ));
                    context.errorResponse("班级中有未检查的作业");
                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HAVE_UNCHECK_HOMEWORK);
                    context.setTerminateTask(true);
                    return;
                }
            }
        }
    }
}
