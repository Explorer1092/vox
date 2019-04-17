package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkCacheLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Named
public class AHV_CheckHomeworkData extends SpringContainerSupport implements AssignVacationHomeworkTask {

    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private VacationHomeworkCacheLoaderImpl vacationHomeworkCacheLoader;

    @Override
    public void execute(AssignVacationHomeworkContext context) {
        Set<String> bookIdSet = new HashSet<>(context.getGroupBookIdMap().values());
        Map<String, NewBookProfile> bookMap  = newContentLoaderClient.loadBookProfilesIncludeDisabled(bookIdSet);
        for (String bookId : bookIdSet) {
            NewBookProfile bookProfile = bookMap.get(bookId);
            if (bookProfile == null) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod1", ErrorCodeConstants.ERROR_CODE_BOOK_NOT_EXIST,
                        "mod2", context.getSource(),
                        "op", "assign vacation homework"
                ));
                context.errorResponse("教材不存在");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_BOOK_NOT_EXIST);
                context.setTerminateTask(true);
                return;
            }
            VacationHomeworkWinterPlanCacheMapper winterPlanCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(bookId);
            if (winterPlanCacheMapper == null || MapUtils.isEmpty(winterPlanCacheMapper.getDayPlan()) || winterPlanCacheMapper.getDayPlan().size() != 30) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod1", ErrorCodeConstants.ERROR_CODE_WINTER_PLAN_NOT_EXIST,
                        "mod2", context.getSource(),
                        "op", "assign vacation homework"
                ));
                context.errorResponse(bookProfile.getName() + "假期作业内容错误，请更换教材!");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_WINTER_PLAN_NOT_EXIST);
                context.setTerminateTask(true);
                return;
            }
        }
    }
}
