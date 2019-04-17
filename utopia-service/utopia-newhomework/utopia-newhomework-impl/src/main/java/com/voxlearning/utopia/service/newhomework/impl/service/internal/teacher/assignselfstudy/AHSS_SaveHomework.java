package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignselfstudy;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.homework.api.constant.SelfHomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.SelfStudyHomeworkPublisher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Named
public class AHSS_SaveHomework extends SpringContainerSupport implements AssignSelfStudyHomeworkTask {

    @Inject private SelfStudyHomeworkDao selfStudyHomeworkDao;
    @Inject private SelfStudyHomeworkBookDao selfStudyHomeworkBookDao;
    @Inject private SelfStudyHomeworkPublisher selfStudyHomeworkPublisher;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    public void execute(AssignSelfStudyHomeworkContext context) {

        try {
            Date currentDate = new Date();
            SelfStudyHomework homework = new SelfStudyHomework();
            String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
            String id = new SelfStudyHomework.ID(month, context.getStudentId()).toString();
            homework.setId(id);
            homework.setSubject(context.getSubject());
            homework.setClazzGroupId(context.getGroupId());
            homework.setSourceHomeworkId(context.getSourceHomeworkId());
            homework.setStudentId(context.getStudentId());
            homework.setStartTime(currentDate);
            // 来个50年
            homework.setEndTime(DateUtils.addYears(currentDate, 50));
            homework.setRemark(context.getRemark());
            homework.setDes(context.getDes());
            homework.setDuration(context.getDuration());
            homework.setCreateAt(currentDate);
            homework.setUpdateAt(currentDate);
            homework.setSource(context.getHomeworkSourceType());
            homework.setDisabled(false);
            homework.setChecked(false);
            homework.setPractices(context.getPractices());
            homework.setIncludeSubjective(context.isIncludeSubjective());
            homework.setAdditions(context.getAdditions());
            homework.setType(context.getNewHomeworkType());
            homework.setHomeworkTag(context.getHomeworkTag());
            selfStudyHomeworkDao.insert(homework);

            SelfStudyHomeworkBook homeworkBook = new SelfStudyHomeworkBook();
            homeworkBook.setId(homework.getId());
            homeworkBook.setSubject(context.getSubject());
            homeworkBook.setClazzGroupId(context.getGroupId());
            homeworkBook.setPractices(context.getPracticesBooksMap());
            selfStudyHomeworkBookDao.insert(homeworkBook);

            homework = selfStudyHomeworkDao.load(homework.getId());
            if (homework != null && StringUtils.isNotBlank(homework.getId())) {
                NewHomework newHomework = newHomeworkLoader.load(homework.getSourceHomeworkId());
                //布置成功作业发送topic
                Map<String, Object> map = new HashMap<>();
                map.put("messageType", SelfHomeworkPublishMessageType.assign);
                map.put("hid", homework.getId());
                //作业布置时间
                map.put("createAt", newHomework != null && newHomework.getCreateAt() != null ? newHomework.getCreateAt().getTime() : new Date().getTime());
                map.put("sid", homework.getStudentId());
                map.put("subject", homework.getSubject());
                selfStudyHomeworkPublisher.getMessagePublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
                context.setHomeworkId(homework.getId());
                context.setAssignedHomework(homework);
            }

        } catch (Exception e) {
            context.errorResponse("SelfStudyHomework assign error, homeworkSource:{}", JsonUtils.toJson(context.getSource()));
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
        }
    }
}
