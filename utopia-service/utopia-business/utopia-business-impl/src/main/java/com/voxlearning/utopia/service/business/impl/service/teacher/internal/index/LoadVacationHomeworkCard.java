package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by tanguohong on 2016/12/26.
 */
@Named
public class LoadVacationHomeworkCard extends AbstractTeacherIndexDataLoader {
    @Inject
    VacationHomeworkLoaderClient vacationHomeworkLoaderClient;
    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        FlightRecorder.dot("LVH_START");
        //assignVacationHomeworked默认false表示未布置和不显示两种意思，为了性能2017-02-12 23:59:59之后就不再查数默认不显示
        Boolean assignVacationHomeworked = false;
        if(NewHomeworkConstants.VH_START_DATE_LATEST.after(new Date())){
            Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(context.getTeacher().getId());
            // 分组id
            List<Long> groupIds = new LinkedList<>();
            Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, false);
            teacherGroups.forEach((teacherId, groups) -> groups.stream().filter(g->!Subject.CHINESE.equals(g.getSubject())).forEach(group -> {
                if (group.isTeacherGroupRefStatusValid(teacherId)) {
                    // 分组id
                    groupIds.add(group.getId());
                }
            }));
            // 分组作业
            Map<Long, List<VacationHomeworkPackage.Location>> groupLocationMap = vacationHomeworkLoaderClient.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
            if(!MapUtils.isEmpty(groupLocationMap)){
                assignVacationHomeworked = true;
            }
        }
        context.getParam().put("assignVacationHomeworked", assignVacationHomeworked);
        FlightRecorder.dot("LVH_END");
        return context;
    }
}
