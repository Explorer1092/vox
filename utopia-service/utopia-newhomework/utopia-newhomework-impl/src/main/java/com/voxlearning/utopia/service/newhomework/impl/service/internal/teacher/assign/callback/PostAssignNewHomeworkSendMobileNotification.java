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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;

import javax.inject.Named;
import java.util.List;

/**
 * 发送jpush app 作业消息
 *
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/9
 */
@Named
public class PostAssignNewHomeworkSendMobileNotification extends NewHomeworkSpringBean implements PostAssignHomework {
    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        AlpsThreadPool.getInstance().submit(() -> {

            for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
                String t = "h5";

                String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/homework/category/newpracticenum.vpage",
                        MiscUtils.m("homeworkId", newHomework.getId()));
                String content = teacher.respectfulName()+"布置"+newHomework.getSubject().getValue()+"作业啦！作业截止："+DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日")+"，快去看看吧！";
//                String tag = "group_" + clazzGroup.getGroupId();
                List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId());
                appMessageServiceClient.sendAppJpushMessageByIds(
                        content,
                        AppMessageSource.STUDENT,
                        studentIds,
                        MiscUtils.m("s", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getType(), "link", link, "t", t, "key", "j",
                                "title", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getDescription()));
            }
        });
    }
}
