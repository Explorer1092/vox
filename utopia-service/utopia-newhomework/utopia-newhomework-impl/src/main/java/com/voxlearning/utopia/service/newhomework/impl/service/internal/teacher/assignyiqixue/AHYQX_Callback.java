/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignYiQiXueHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue.callback.PostAssignYiQiXueHomeworkUpdateClazzBook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
public class AHYQX_Callback extends SpringContainerSupport implements AssignYiQiXueHomeworkTask {
    @Inject private PostAssignYiQiXueHomeworkUpdateClazzBook postAssignYiQiXueHomeworkUpdateClazzBook;

    private final Collection<PostAssignYiQiXueHomework> assignYiQiXueHomeworkCallbacks = new LinkedHashSet<>();

    @Override
    public void execute(AssignHomeworkContext context) {
        if (context.isSuccessful()) {

            assignYiQiXueHomeworkCallbacks.addAll(Arrays.asList(
                    postAssignYiQiXueHomeworkUpdateClazzBook
            ));
            for (PostAssignYiQiXueHomework callback : assignYiQiXueHomeworkCallbacks)
                callback.afterYiQiXueHomeworkAssigned(context.getTeacher(), context);
        }
    }

}
