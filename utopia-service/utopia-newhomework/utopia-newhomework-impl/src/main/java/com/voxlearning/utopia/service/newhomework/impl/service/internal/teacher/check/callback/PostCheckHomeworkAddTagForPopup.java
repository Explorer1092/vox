package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 老师检查作业的时候缓存一个作业已检查的状态。7天过期。
 * 家长端在查popup弹窗的时候需要这个检查状态。这样不用再去查整个作业来获取检查状态了
 *
 * @author shiwei.liao
 * @since 2017-3-6
 */
@Named
public class PostCheckHomeworkAddTagForPopup extends SpringContainerSupport implements PostCheckHomework {

    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        if (StringUtils.isBlank(context.getHomeworkId())) {
            return;
        }
        newHomeworkCacheService.getStudentFinishHomeworkPopupManager().addHomeworkCheckTag(context.getHomeworkId());
    }
}
