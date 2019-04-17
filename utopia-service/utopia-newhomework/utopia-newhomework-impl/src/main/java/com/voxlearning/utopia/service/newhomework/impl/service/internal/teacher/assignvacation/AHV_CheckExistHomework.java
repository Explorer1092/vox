package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkLoaderImpl;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/11/29.
 */
@Named
public class AHV_CheckExistHomework extends SpringContainerSupport implements AssignVacationHomeworkTask {
    @Inject
    private VacationHomeworkLoaderImpl vacationHomeworkLoader;

    @Override
    public void execute(AssignVacationHomeworkContext context) {
        List<Long> groupIds = context.getClazzGroups().stream().map(ClazzGroup::getGroupId).collect(Collectors.toList());
        Map<Long, List<VacationHomeworkPackage.Location>> groupHomeworkMap = vacationHomeworkLoader.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
        // 检查分组是否已经布置假期作业了
        for (ClazzGroup clazzGroup : context.getClazzGroups()) {
            Long groupId = clazzGroup.getGroupId();
            if (groupHomeworkMap.get(groupId) != null) {
                Subject subject = Subject.of(context.getGroupSubjectMap().get(groupId));
                List<VacationHomeworkPackage.Location> locations = groupHomeworkMap.get(groupId).stream().filter(p -> p.getSubject().equals(subject)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(locations)) {
                    LogCollector.info("backend-general", MiscUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", context.getTeacher().getId(),
                            "mod1", ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_PACKAGE_EXIST,
                            "mod2", context.getSource(),
                            "op", "assign vacation homework"
                    ));
                    context.errorResponse("班级中已经布置过假期作业");
                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_PACKAGE_EXIST);
                    context.setTerminateTask(true);
                    return;

                }
            }
        }
    }
}
