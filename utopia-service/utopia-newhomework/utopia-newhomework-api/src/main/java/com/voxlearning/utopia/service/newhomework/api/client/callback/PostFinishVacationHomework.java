package com.voxlearning.utopia.service.newhomework.api.client.callback;

import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;

/**
 * @author guoqiang.li
 * @since 2016/12/12
 */
public interface PostFinishVacationHomework {
    void afterVacationHomeworkFinished(FinishVacationHomeworkContext finishVacationHomeworkContext);
}
