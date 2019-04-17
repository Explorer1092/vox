package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewAccomplishmentLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Named
public class CH_LoadAccomplishment extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private NewAccomplishmentLoaderImpl newAccomplishmentLoader;

    @Override
    public void execute(CheckHomeworkContext context) {
        NewAccomplishment acc = newAccomplishmentLoader.loadNewAccomplishment(context.getHomework().toLocation());
        context.setAccomplishment(acc);
    }
}
