package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.google.common.collect.Maps;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.SelfHomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkCorrectStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyAccomplishmentDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.SelfStudyHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Named
public class FSS_UpdateSelfStudyHomeworkResult extends SpringContainerSupport implements FinishSelfStudyHomeworkTask {

    @Inject private SelfStudyHomeworkResultDao selfStudyHomeworkResultDao;
    @Inject private SelfStudyHomeworkPublisher selfStudyHomeworkPublisher;
    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private SelfStudyAccomplishmentDao selfStudyAccomplishmentDao;

    @Override
    public void execute(FinishSelfStudyHomeworkContext context) {
        SelfStudyHomeworkResult modified = selfStudyHomeworkResultDao.finishHomework(
                context.getSelfStudyHomework().toLocation(),
                context.getObjectiveConfigType(),
                context.getPracticeScore(),
                context.getPracticeDuration(),
                context.isPracticeFinished(),
                context.isHomeworkFinished()
        );

        if (modified != null && context.isHomeworkFinished()) {
            // 更新SelfStudyAccomplishment表
            SelfStudyHomework selfStudyHomework = context.getSelfStudyHomework();
            selfStudyAccomplishmentDao.finishedSelfStudyHomework(selfStudyHomework.getSourceHomeworkId(), selfStudyHomework.getStudentId(), selfStudyHomework.getId(), new Date());

            updateHomeworkCorrectStatus(context.getSelfStudyHomework());//更新订正状态为已订正
            //学生完成订正发送topic
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", SelfHomeworkPublishMessageType.finished);
            map.put("hid", modified.getHomeworkId());
            map.put("subject", modified.getSubject());
            selfStudyHomeworkPublisher.getMessagePublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
            context.setSelfStudyHomeworkResult(modified);
        }
    }

    private void updateHomeworkCorrectStatus(SelfStudyHomework selfStudyHomework) {

        String sourceHomeworkId = selfStudyHomework.getSourceHomeworkId();
        NewHomework newHomework = newHomeworkLoader.load(sourceHomeworkId);
        String day = DayRange.newInstance(newHomework.toLocation().getCreateTime()).toString();
        SubHomeworkResultExtendedInfo.ID id = new SubHomeworkResultExtendedInfo.ID(day, newHomework.getSubject(), sourceHomeworkId, String.valueOf(selfStudyHomework.getStudentId()));

        Map<String, String> infoMap = Maps.newHashMap();
        infoMap.put(NewHomeworkConstants.HOMEWORK_CORRECT_STATUS, HomeworkCorrectStatus.CORRECT_FINISH.name());
        newHomeworkResultService.updateSubHomeworkResultExtendedInfo(id.toString(), infoMap);
    }
}
