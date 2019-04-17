package com.voxlearning.utopia.service.newhomework.api.client.callback;

import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/18
 */
public interface PostFinishHomework {
    void afterHomeworkFinished(FinishHomeworkContext context);
}
