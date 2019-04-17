package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;

abstract public class AncientPoetryResultProcessTemplate extends SpringContainerSupport {

    abstract public ModelType getProcessResultModel();

    abstract public void processResult(AncientPoetryProcessContext context);

}
