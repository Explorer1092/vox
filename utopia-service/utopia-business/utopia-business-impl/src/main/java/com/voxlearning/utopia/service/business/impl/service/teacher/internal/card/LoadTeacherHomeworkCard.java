package com.voxlearning.utopia.service.business.impl.service.teacher.internal.card;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.business.api.constant.TeacherCardType;
import com.voxlearning.utopia.mapper.TeacherCardMapper;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkCard;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2017/4/17
 */
@Named
public class LoadTeacherHomeworkCard extends AbstractTeacherCardDataLoader {
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;

    @Override
    protected TeacherCardDataContext doProcess(TeacherCardDataContext context) {
        List<NewHomeworkCard> newHomeworkCardList = newHomeworkLoaderClient.loadNewHomeworkCard(context.getTeacher().getId());
        if (CollectionUtils.isEmpty(newHomeworkCardList)) {
            return context;
        }
        for (NewHomeworkCard newHomeworkCard : newHomeworkCardList) {
            TeacherCardMapper cardMapper = generateTeacherCardMapper(newHomeworkCard, context.getTeacher(), context.getImgDomain());
            if (cardMapper != null) {
                context.taskCards.add(cardMapper);
            }
        }
        return context;
    }

    private TeacherCardMapper generateTeacherCardMapper(NewHomeworkCard newHomeworkCard, Teacher teacher, String imgDomain) {
        HomeworkTaskStatus taskStatus = newHomeworkCard.getTaskStatus();
        HomeworkTaskType taskType = newHomeworkCard.getTaskType();
        if (taskStatus == null || taskType == null) {
            return null;
        }
        newHomeworkCard.setPcImgUrl(processImgUrl(newHomeworkCard.getPcImgUrl(), imgDomain));
        newHomeworkCard.setNativeImgUrl(processImgUrl(newHomeworkCard.getNativeImgUrl(), imgDomain));
        newHomeworkCard.setH5ImgUrl(processImgUrl(newHomeworkCard.getH5ImgUrl(), imgDomain));
        TeacherCardMapper newHomeworkCardMapper = new TeacherCardMapper();
        newHomeworkCardMapper.setCardType(TeacherCardType.HOMEWORK);
        newHomeworkCardMapper.setCardName(newHomeworkCard.getTaskName());
        newHomeworkCardMapper.setCardDescription(newHomeworkCard.getTaskDescription());
        newHomeworkCardMapper.setProgress(taskStatus.getDescription());
        newHomeworkCardMapper.setProgressColor(taskStatus.getColor());
        newHomeworkCardMapper.setImgUrl(taskType.getImgUrl());
        if (taskType == HomeworkTaskType.ACTIVITY_HOMEWORK) {
            newHomeworkCardMapper.setImgUrl(newHomeworkCard.getNativeImgUrl());
        }
        newHomeworkCardMapper.setDetailUrl(taskType.getDetailUrl());
        newHomeworkCardMapper.setCardDetails(JsonUtils.toJson(newHomeworkCard));
        newHomeworkCardMapper.setTeacherId(teacher.getId());
        newHomeworkCardMapper.setSubject(teacher.getSubject());
        return newHomeworkCardMapper;
    }

    private String processImgUrl(String imgUrl, String imgDomain) {
        if (StringUtils.isNotEmpty(imgUrl) && !imgUrl.startsWith("http")) {
            imgUrl = imgDomain + imgUrl;
        }
        return imgUrl;
    }
}
