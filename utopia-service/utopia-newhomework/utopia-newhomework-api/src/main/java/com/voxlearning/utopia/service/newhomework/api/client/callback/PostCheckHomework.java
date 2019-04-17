package com.voxlearning.utopia.service.newhomework.api.client.callback;

import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
public interface PostCheckHomework {
    void afterHomeworkChecked(CheckHomeworkContext context);
}
