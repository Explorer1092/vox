package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/17
 */
@Named
public class PostCheckHomeworkAppParentMessage extends SpringContainerSupport implements PostCheckHomework {
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        if (CollectionUtils.isEmpty(context.getStudents())) return;
        if (context.getTeacher() == null) return;
        Map<Long, User> students = context.getStudents().stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
        NewAccomplishment acc = context.getAccomplishment();
        Long teacherId = context.getTeacherId();
        NewHomework homework = newHomeworkLoader.loadNewHomework(context.getHomeworkId());
        Teacher teacher = context.getTeacher();
        //这里只是取发送人的ID
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        teacherId = mainTeacherId == null ? teacherId : mainTeacherId;
        //这里才是取所有的学科
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
        int unFinishCount = 0;
        for (Long studentId : students.keySet()) {
            if (acc == null || acc.size() <= 0 || !acc.getDetails().containsKey(String.valueOf(studentId))) {
                unFinishCount++;
            }
        }
        Set<Long> groupIdSet = new HashSet<>();
        groupIdSet.add(context.getGroupId());

        StringBuilder iMFinishContent;
        if (homework.getType() == NewHomeworkType.TermReview) {
            iMFinishContent = new StringBuilder("家长好，期末复习作业已检查");
        } else {
            iMFinishContent = new StringBuilder("家长好，在线作业已检查");
        }
        if (unFinishCount < 10 && unFinishCount > 0) {
            iMFinishContent.append("，").append(unFinishCount).append("名同学未完成，请家长提醒");
        } else if (unFinishCount >= 10) {
            iMFinishContent.append("，").append("感谢家长们对孩子学习的关注");
        }

        String teacherName = teacher.fetchRealname();
        List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
        String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
        String em_push_title = teacherName + subjectsStr + "：" + iMFinishContent;
        //新的极光push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("s", ParentAppPushType.HOMEWORK_CHECK.name());
        if (NewHomeworkType.OCR == homework.getType()) {
            jpushExtInfo.put("url", UrlUtils.buildUrlQuery("/view/mobile/parent/ocrhomework/student_questions_detail",
                    MapUtils.m("homeworkId", homework.getId(), "subject", homework.getSubject(), "objectiveConfigType", Subject.ENGLISH == homework.getSubject() ? ObjectiveConfigType.OCR_DICTATION : ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)));
        } else {
            jpushExtInfo.put("url", "/view/mobile/parent/homework/report_detail?tab=personal&hid=" + homework.getId());
        }
        List<String> groupTags = new LinkedList<>();
        groupIdSet.forEach(p -> groupTags.add(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(p))));
        appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                AppMessageSource.PARENT,
                groupTags,
                null,
                jpushExtInfo);
    }
}
