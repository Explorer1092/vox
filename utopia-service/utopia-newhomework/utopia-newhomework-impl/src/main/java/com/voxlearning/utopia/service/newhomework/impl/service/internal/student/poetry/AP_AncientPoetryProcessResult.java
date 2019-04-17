package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template.AncientPoetryResultProcessFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template.AncientPoetryResultProcessTemplate;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class AP_AncientPoetryProcessResult extends SpringContainerSupport implements AncientPoetryResultTask{
    @Inject private AncientPoetryResultProcessFactory ancientPoetryResultProcessFactory;

    @Override
    public void execute(AncientPoetryProcessContext context) {
        AncientPoetryResultProcessTemplate template = ancientPoetryResultProcessFactory.getTemplate(context.getModelType());
        template.processResult(context);
    }
}
