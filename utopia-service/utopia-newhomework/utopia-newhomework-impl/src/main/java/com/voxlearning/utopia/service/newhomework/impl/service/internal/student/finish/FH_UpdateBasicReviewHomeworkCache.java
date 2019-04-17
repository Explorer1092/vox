package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.loader.BasicReviewHomeworkCacheLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 2017秋季期末基础复习 完成作业后更新缓存
 *
 * @author guoqiang.li
 * @since 2017/11/14
 */
@Named
public class FH_UpdateBasicReviewHomeworkCache extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private BasicReviewHomeworkCacheLoaderImpl basicReviewHomeworkCacheLoader;

    @Override
    public void execute(FinishHomeworkContext context) {
        if (!Objects.equals(NewHomeworkType.BasicReview, context.getNewHomeworkType())) {
            return;
        }
        basicReviewHomeworkCacheLoader.addOrModifyBasicReviewHomeworkCacheMapper(context.getResult(), context.getHomework().getBasicReviewPackageId(), context.getUserId());
    }
}
