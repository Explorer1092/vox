package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.AsyncAvengerHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Objects;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class CH_UpdateHomeworkChecked extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkServiceImpl newHomeworkService;
    @Inject private AsyncAvengerHomeworkServiceImpl asyncAvengerHomeworkService;

    @Override
    public void execute(CheckHomeworkContext context) {
        Boolean b = newHomeworkService.updateNewHomeworkChecked(context.getHomeworkId(), Boolean.TRUE, new Date(), context.getCheckHomeworkSource());

        if (Objects.equals(Boolean.TRUE, b)) {
            NewHomework homework = newHomeworkLoader.load(context.getHomeworkId());
            NewHomeworkBook homeworkBook = newHomeworkLoader.loadNewHomeworkBook(context.getHomeworkId());
            asyncAvengerHomeworkService.informHomeworkToBigData(homework, homeworkBook);
            context.setHomework(homework);
        } else {
            context.errorResponse("检查作业失败");
        }
    }
}
