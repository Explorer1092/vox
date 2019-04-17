package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkQuizContentLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

import static com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType.MID_QUIZ;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
@Named
public class NewHomeworkMidQuizContentLoader extends NewHomeworkQuizContentLoader {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return MID_QUIZ;
    }
}
