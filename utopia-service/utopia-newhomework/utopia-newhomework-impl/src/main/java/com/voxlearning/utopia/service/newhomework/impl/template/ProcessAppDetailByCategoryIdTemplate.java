package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryClazzHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryHandlerContext;


abstract public class ProcessAppDetailByCategoryIdTemplate {

    abstract public NatureSpellingType getNatureSpellingType();

    abstract public void processPersonalCategory(CategoryHandlerContext categoryHandlerContext);

    abstract public void processClazzCategory(CategoryClazzHandlerContext categoryClazzHandlerContext);
}
