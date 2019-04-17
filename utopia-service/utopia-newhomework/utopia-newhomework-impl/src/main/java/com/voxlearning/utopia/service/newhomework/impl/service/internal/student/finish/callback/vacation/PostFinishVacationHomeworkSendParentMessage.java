package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkCacheLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.entity.WinterDayPlan;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author guoqiang.li
 * @since 2016/12/12
 */
@Named
public class PostFinishVacationHomeworkSendParentMessage extends NewHomeworkSpringBean implements PostFinishVacationHomework {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Inject private VacationHomeworkCacheLoaderImpl vacationHomeworkCacheLoader;

    private static final String messageContent = "家长你好，你的孩子{}已完成假期作业第{}周-Day{}{}，请查看学习报告。";

    @Override
    public void afterVacationHomeworkFinished(FinishVacationHomeworkContext context) {
        VacationHomework vacationHomework = context.getVacationHomework();
        String studentName = "";
        Student student = studentLoaderClient.loadStudent(vacationHomework.getStudentId());
        if (student != null) {
            studentName = student.fetchRealname();
        }
        String weekRank = NewHomeworkUtils.transferToChinese(SafeConverter.toString(vacationHomework.getWeekRank()));
        String dayRank = SafeConverter.toString(vacationHomework.getDayRank());
        String dayPlanName = "";
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(vacationHomework.getPackageId());
        if (vacationHomeworkPackage != null) {
            VacationHomeworkWinterPlanCacheMapper winterPlanCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(vacationHomeworkPackage.getBookId());
            if (winterPlanCacheMapper != null) {
                Map<String, WinterDayPlan> dayPlanMap = winterPlanCacheMapper.getDayPlan();
                String key = StringUtils.join(Arrays.asList(vacationHomework.getWeekRank(), vacationHomework.getDayRank()), "-");
                if (dayPlanMap.containsKey(key)) {
                    dayPlanName = dayPlanMap.get(key).getName();
                }
            }
        }
        String content = StringUtils.formatMessage(messageContent, studentName, weekRank, dayRank, dayPlanName);
        String url = UrlUtils.buildUrlQuery("/view/vacationhomework/answerdetail", MapUtils.m("homeworkId", vacationHomework.getId(), "studentId", vacationHomework.getStudentId()));
        List<StudentParent> parentList = parentLoaderClient.loadStudentParents(vacationHomework.getStudentId());
        if (CollectionUtils.isNotEmpty(parentList)) {
            List<Long> userIdList = new ArrayList<>();
            parentList.stream()
                    .filter(parent -> parent.getParentUser() != null)
                    .forEach(parent -> {
                        userIdList.add(parent.getParentUser().getId());
                        AppMessage appUserMessage = new AppMessage();
                        appUserMessage.setUserId(parent.getParentUser().getId());
                        appUserMessage.setContent(content);
                        appUserMessage.setMessageType(ParentMessageType.REMINDER.getType());
                        appUserMessage.setLinkUrl(url);
                        appUserMessage.setLinkType(1);
                        Map<String, Object> extInfo = new HashMap<>();
                        extInfo.put("type", ParentMessageType.REMINDER.name());
                        extInfo.put("tag", ParentMessageTag.报告.name());
                        appUserMessage.setExtInfo(extInfo);
                        messageCommandServiceClient.getMessageCommandService().createAppMessage(appUserMessage);
                    });
            // 增加jpush
            appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, userIdList,
                    MiscUtils.m("url", url,
                            "tag", ParentMessageTag.报告.name(),
                            "s", ParentAppPushType.REPORT.name(),
                            "studentId",""));
        }
    }
}
