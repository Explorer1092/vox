package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation.callback;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.mapper.ScoreCircleQueueCommand;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkParentQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/12/6.
 */
@Named
public class PostAssignVacationHomeworkParentMessage extends NewHomeworkSpringBean implements PostAssignVacationHomework {
    @Inject private NewHomeworkParentQueueProducer newHomeworkParentQueueProducer;

    private static final String CONTENT_PATTERN = "家长好，我布置了假期作业。作业开始时间：{0}，请家长督促。";

    @Override
    public void afterVacationHomeworkAssigned(Teacher teacher, AssignVacationHomeworkContext context) {
        Map<ClazzGroup, VacationHomeworkPackage> assignedHomeworks = context.getAssignedHomeworks();
        if (MapUtils.isEmpty(assignedHomeworks)) {
            return;
        }

        Long teacherId = teacher.getId();
        String teacherName = teacher.fetchRealname();
        //这里只是取发送人的ID
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        teacherId = mainTeacherId == null ? teacherId : mainTeacherId;
        //这里才是取所有的学科
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());

        for (VacationHomeworkPackage vacationHomeworkPackage : context.getAssignedHomeworks().values()) {
            Set<Long> groupIdSet = new HashSet<>();
            groupIdSet.add(vacationHomeworkPackage.getClazzGroupId());
            String startDate = DateUtils.dateToString(context.getHomeworkStartTime(), "M月d日");
            Subject subject = vacationHomeworkPackage.getSubject();
            String iMContent = MessageFormat.format(CONTENT_PATTERN, startDate);

            //新的群组消息ScoreCircle
            ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
            circleQueueCommand.setGroupId(vacationHomeworkPackage.getClazzGroupId());
            circleQueueCommand.setCreateDate(new Date());
            circleQueueCommand.setGroupCircleType("VACATION_HOMEWORK_NEW");
            circleQueueCommand.setTypeId(vacationHomeworkPackage.getId());
            circleQueueCommand.setImgUrl("");
            circleQueueCommand.setLinkUrl(UrlUtils.buildUrlQuery("/view/mobile/activity/parent/vacation", MapUtils.m("subject", subject)));
            circleQueueCommand.setContent(iMContent);
            newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));

            //新的极光push
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("s", ParentAppPushType.HOMEWORK_ASSIGN.name());
            jpushExtInfo.put("url", UrlUtils.buildUrlQuery("/view/mobile/activity/parent/vacation", MapUtils.m("subject", subject)));
            List<String> groupTags = new LinkedList<>();
            groupIdSet.forEach(p -> groupTags.add(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(p))));

            String subjectsStr = "（" + StringUtils.join(subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).toArray(), "，") + "）";
            String em_push_title = teacherName + subjectsStr + "：" + iMContent;

            appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                    AppMessageSource.PARENT,
                    groupTags,
                    null,
                    jpushExtInfo);
        }
    }
}
