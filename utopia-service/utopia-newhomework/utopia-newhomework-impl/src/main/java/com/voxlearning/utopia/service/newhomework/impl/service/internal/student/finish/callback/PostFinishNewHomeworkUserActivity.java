/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/18
 */
@Named
public class PostFinishNewHomeworkUserActivity extends SpringContainerSupport implements PostFinishHomework {

    @Inject private AsyncFootprintServiceClient asyncFootprintServiceClient;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        // 记录学生最近一次完成作业时间 TODO: 2016/1/18 这个是不是可以不要了。。。
        asyncFootprintServiceClient.studentFinishedHomework(context.getUserId());
    }
}
