package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/12/12
 */

@Named
public class PostFinishVacationHomeworkSaveStudents extends NewHomeworkSpringBean implements PostFinishVacationHomework {
    @Override
    public void afterVacationHomeworkFinished(FinishVacationHomeworkContext context) {
        VacationHomework vacationHomework = context.getVacationHomework();
        Long studentId = vacationHomework.getStudentId();
        String vacationHomeworkId = vacationHomework.getId();
        String[] prefixStr = vacationHomeworkId.split("-");
        StringBuilder prefix = new StringBuilder();
        if (prefixStr.length == 4) {
            for (int i = 0; i < 2; i++) {
                prefix.append(prefixStr[i]).append("-");
            }
            prefix.append(prefixStr[2]);
        }
        String key = "FVH:" + prefix;
        CacheObject<List<Long>> cacheObject = HomeworkCache.getHomeworkCache().get(key);
        List<Long> finishedStudentIds = cacheObject.getValue();
        if (CollectionUtils.isEmpty(finishedStudentIds)) {
            finishedStudentIds = new ArrayList<>();
        }
        finishedStudentIds.add(studentId);
        HomeworkCache.getHomeworkCache().set(key, 0, finishedStudentIds);
    }
}
