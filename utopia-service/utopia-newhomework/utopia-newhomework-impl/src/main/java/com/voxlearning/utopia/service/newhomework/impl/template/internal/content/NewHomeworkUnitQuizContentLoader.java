package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkQuizContentLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
@Named
public class NewHomeworkUnitQuizContentLoader extends NewHomeworkQuizContentLoader {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.UNIT_QUIZ;
    }
}
