package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkSubjectiveContentLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
@Named
public class NewHomeworkPhotoObjectiveContentLoader extends NewHomeworkSubjectiveContentLoader {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.PHOTO_OBJECTIVE;
    }
}
