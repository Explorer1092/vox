package com.voxlearning.utopia.service.newhomework.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.service.DPHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExposeService(interfaceClass = DPHomeworkService.class)
public class DPHomeworkServiceImpl extends SpringContainerSupport implements DPHomeworkService {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    public NewHomework loadNewHomework(String homeworkId) {
        return newHomeworkLoader.loadNewHomework(homeworkId);
    }
}
