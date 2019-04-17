package com.voxlearning.utopia.service.vendor.impl.push.processor;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.impl.push.HomeworkMessageAbstractProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 28/7/2016
 */
@Named
public class HomeworkMessageProcessor_Finish extends HomeworkMessageAbstractProcessor {

    @Inject private RaikouSystem raikouSystem;

    public HomeworkMessageProcessor_Finish() {
        this.source = AppMessageSource.HOMEWORK_FINISH;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        homeworkMessageProcessorManager.regist(this);
    }

    @Override
    protected void doProcess(Map<String, Object> messageMap) {
        String sj = (String) messageMap.get("subject");
        Long userId = SafeConverter.toLong(messageMap.get("userId"));
        Long timestamp = SafeConverter.toLong(messageMap.get("timestamp"));
        String homeworkId = (String) messageMap.get("homeworkId");

        Subject subject = Subject.ofWithUnknown(sj);
        if (subject == Subject.UNKNOWN) {
            return;
        }

        Map<Long, List<StudentParent>> parentMap = parentLoaderClient.loadStudentParents(Collections.singletonList(userId));
        if (MapUtils.isEmpty(parentMap)) {
            return;
        }

        List<StudentParent> parents = parentMap.get(userId);
        if (CollectionUtils.isEmpty(parents)) {
            return;
        }
        List<Long> parentIds = parents.stream()
                .map(StudentParent::getParentUser)
                .map(User::getId)
                .collect(Collectors.toList());

        User student = raikouSystem.loadUser(userId);

        //构造消息参数
        String content = subject.getValue() + "练习已完成，去帮他复习下今天所学的内容吧！";
        if (null != student) {
            content = student.getProfile().getRealname() + content;
        }

        String homeworkType = SafeConverter.toString(messageMap.get("homeworkType"), "");
        Map<String, Object> extInfo = new HashMap<>();
        if (homeworkType.equalsIgnoreCase("OCR")) {
            extInfo.put("url", UrlUtils.buildUrlQuery("/view/mobile/parent/ocrhomework/student_questions_detail",
                    MapUtils.m("homeworkId", homeworkId, "subject", sj, "objectiveConfigType", Subject.ENGLISH.name().equals(sj) ? ObjectiveConfigType.OCR_DICTATION : ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)));
        } else {
            extInfo.put("url", "/view/mobile/parent/homework/report_notice?hid=" + homeworkId);
        }
        extInfo.put("studentId", userId);
        extInfo.put("tag", ParentMessageTag.完成作业.name());
        extInfo.put("homeworkId", homeworkId);
        //新的push参数
        extInfo.put("s", ParentAppPushType.HOMEWORK_FINISH.name());
        Long sendTimeEpochMilli = Instant.ofEpochMilli(timestamp).plusSeconds(30 * 60).toEpochMilli();
        if (RuntimeMode.current().le(Mode.STAGING)) {
            sendTimeEpochMilli = Instant.now().toEpochMilli();
        }

        appMessageService.sendAppJpushMessageByIds(content, this.source, parentIds, extInfo, sendTimeEpochMilli);
    }
}
