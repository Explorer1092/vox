package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation.callback;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/12/6.
 */
@Named
public class PostAssignVacationHomeworkSendMobileNotification extends NewHomeworkSpringBean implements PostAssignVacationHomework {

    @Inject private RaikouSDK raikouSDK;

    @Override
    public void afterVacationHomeworkAssigned(Teacher teacher, AssignVacationHomeworkContext context) {
        AlpsThreadPool.getInstance().submit(() -> {
            Map<ClazzGroup, VacationHomeworkPackage> assigned = context.getAssignedHomeworks();
            Set<Long> groupIds = context.getGroupBookIdMap().keySet();
            Map<Long, List<GroupStudentTuple>> groupStudentRefs = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .findByGroupIds(groupIds)
                    .stream()
                    .collect(Collectors.groupingBy(GroupStudentTuple::getGroupId));
            for (ClazzGroup clazzGroup : assigned.keySet()) {

                VacationHomeworkPackage vacationHomeworkPackage = assigned.get(clazzGroup);
                String link = UrlUtils.buildUrlQuery("/studentMobile/homework/vacation/packagelist.vpage",
                        MiscUtils.m("packageId", vacationHomeworkPackage.getId()));
                String content = "假期开始啦，" + context.getTeacher().respectfulName() + "布置了" + vacationHomeworkPackage.getSubject().getValue() + "假期作业，坚持学习，一直进步哦！";

                Date currentDate = new Date();
                long sendTimeEpochMilli = currentDate.getTime() + 1000 * 60 * 2; //默认分钟
                if (RuntimeMode.isProduction() && currentDate.before(NewHomeworkConstants.earliestVacationHomeworkStartDate(RuntimeMode.current()))) {
                    sendTimeEpochMilli = DateUtils.calculateDateDay(currentDate, SafeConverter.toInt(DateUtils.dayDiff(NewHomeworkConstants.earliestVacationHomeworkStartDate(RuntimeMode.current()), currentDate) + 1)).getTime();
                }
                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("link", link);
                extInfo.put("tag", ParentMessageTag.新作业.name());
                extInfo.put("t", "h5");
                extInfo.put("key", "j");
                extInfo.put("s", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getType());
                extInfo.put("title", "新作业提醒");
                List<GroupStudentTuple> studentRefs = groupStudentRefs.get(clazzGroup.getGroupId());
                if (CollectionUtils.isNotEmpty(studentRefs)) {
                    List<Long> studentIds = studentRefs.stream()
                            .map(GroupStudentTuple::getStudentId)
                            .collect(Collectors.toList());
                    if (currentDate.before(NewHomeworkConstants.earliestVacationHomeworkStartDate(RuntimeMode.current()))) {
                        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo, sendTimeEpochMilli);
                    } else {
                        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo);
                    }
                }
            }
        });
    }
}
