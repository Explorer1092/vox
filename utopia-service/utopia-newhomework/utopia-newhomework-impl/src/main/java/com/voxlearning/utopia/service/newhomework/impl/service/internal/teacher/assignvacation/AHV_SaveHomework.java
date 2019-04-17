package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @since 2016/11/30
 */
@Named
public class AHV_SaveHomework extends SpringContainerSupport implements AssignVacationHomeworkTask {
    @Inject
    private VacationHomeworkPackageDao vacationHomeworkPackageDao;
    @Inject
    private VacationHomeworkCacheLoader vacationHomeworkCacheLoader;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Override
    public void execute(AssignVacationHomeworkContext context) {
        Date currentDate = new Date();
        Long teacherId = context.getTeacher().getId();
        String actionId = StringUtils.join(Arrays.asList(teacherId, currentDate.getTime()), "_");
        List<String> nowWinterPlanBookIds = new ArrayList<>();
        for (ClazzGroup clazzGroup : context.getClazzGroups()) {
            VacationHomeworkPackage vacationHomeworkPackage = new VacationHomeworkPackage();
            vacationHomeworkPackage.setActionId(actionId);
            vacationHomeworkPackage.setTeacherId(context.getGroupTeacherMap().get(clazzGroup.getGroupId()));
            vacationHomeworkPackage.setClazzGroupId(clazzGroup.getGroupId());
            vacationHomeworkPackage.setStartTime(context.getHomeworkStartTime());
            vacationHomeworkPackage.setEndTime(context.getHomeworkEndTime());
            vacationHomeworkPackage.setRemark(context.getRemark());
            vacationHomeworkPackage.setPlannedDays(context.getPlannedDays());
            vacationHomeworkPackage.setCreateAt(currentDate);
            vacationHomeworkPackage.setUpdateAt(currentDate);
            vacationHomeworkPackage.setSource(context.getHomeworkSourceType());
            vacationHomeworkPackage.setDisabled(false);
            vacationHomeworkPackage.setSubject(Subject.of(context.getGroupSubjectMap().get(clazzGroup.getGroupId())));
            vacationHomeworkPackage.setBookId(context.getGroupBookIdMap().get(clazzGroup.getGroupId()));
            VacationHomeworkWinterPlanCacheMapper vacationHomeworkWinterPlanCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(vacationHomeworkPackage.getBookId());
            if (vacationHomeworkWinterPlanCacheMapper == null) {
                nowWinterPlanBookIds.add(vacationHomeworkPackage.getBookId());
            }
            context.getAssignedHomeworks().put(clazzGroup, vacationHomeworkPackage);
        }

        if (CollectionUtils.isNotEmpty(nowWinterPlanBookIds)) {
            Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBookProfilesIncludeDisabled(nowWinterPlanBookIds);
            if (bookProfileMap.isEmpty()) {
                context.errorResponse("课本不存在请联系客服，bookIds：{}", StringUtils.join(nowWinterPlanBookIds, ","));
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_BOOK_NOT_EXIST);
                context.setTerminateTask(true);
            }
            context.errorResponse("{}教材没有假期作业内容，请更换后重试！", StringUtils.join(bookProfileMap.values().stream().map(NewBookProfile::getName).collect(Collectors.toList()), ","));
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
            return;
        }

        if (!context.getAssignedHomeworks().isEmpty()) {
            vacationHomeworkPackageDao.inserts(context.getAssignedHomeworks().values());
            context.setIntegral(0);
            context.setLotteryNumber(0);
        } else {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK,
                    "op", "assign vacation homework"
            ));
            context.errorResponse("vacation homework content is null homeworkSource:{}", JsonUtils.toJson(context.getSource()));
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
        }

    }
}
