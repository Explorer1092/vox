package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class CH_PostCheckHomeworkCallbacks extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private PostCheckHomeworkStudentHomeworkStat postCheckHomeworkStudentHomeworkStat;
    @Inject private PostCheckHomeworkCacheRevelant postCheckHomeworkCacheRevelant;
    @Inject private PostCheckHomeworkTeacherActivateTeacher postCheckHomeworkTeacherActivateTeacher;
    @Inject private PostCheckHomeworkUpdateLatestCheckDate postCheckHomeworkUpdateLatestCheckDate;
    @Inject private PostCheckHomeworkLatest postCheckHomeworkLatest; // 必须在PostCheckHomeworkCacheRevelant之后
    @Inject private PostCheckHomeworkWechatMessage postCheckHomeworkWechatMessage;
    @Inject private PostCheckHomeworkAppMessage postCheckHomeworkAppMessage; //涉及学豆浆里数量。必须在postCheckHomeworkRewardInParentApp之后
    @Inject private PostCheckHomeworkAppParentMessage postCheckHomeworkAppParentMessage;
    @Inject private PostCheckHomeworkUpdateRewardActiveLv postCheckHomeworkUpdateRewardActiveLv;
    @Inject private PostCheckHomeworkStudyMaster postCheckHomeworkStudyMaster;
    @Inject private PostCheckHomeworkAddTagForPopup postCheckHomeworkAddTagForPopup;
    @Inject private PostCheckHomeworkPublishMessage postCheckHomeworkPublishMessage;

    private final List<PostCheckHomework> callbacks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        callbacks.add(postCheckHomeworkPublishMessage);
        callbacks.add(postCheckHomeworkStudentHomeworkStat);
        callbacks.add(postCheckHomeworkCacheRevelant);
        callbacks.add(postCheckHomeworkTeacherActivateTeacher);
        callbacks.add(postCheckHomeworkUpdateLatestCheckDate);
        callbacks.add(postCheckHomeworkLatest);
        callbacks.add(postCheckHomeworkWechatMessage);
        callbacks.add(postCheckHomeworkAppParentMessage);
        callbacks.add(postCheckHomeworkUpdateRewardActiveLv);
        callbacks.add(postCheckHomeworkStudyMaster);
        callbacks.add(postCheckHomeworkAppMessage);
        callbacks.add(postCheckHomeworkAddTagForPopup);
    }

    @Override
    public void execute(CheckHomeworkContext context) {
        AlpsThreadPool.getInstance().submit(() -> doExecute(context));
    }

    private void doExecute(CheckHomeworkContext context) {
        for (PostCheckHomework callback : callbacks)
            try {
                callback.afterHomeworkChecked(context);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
    }
}
