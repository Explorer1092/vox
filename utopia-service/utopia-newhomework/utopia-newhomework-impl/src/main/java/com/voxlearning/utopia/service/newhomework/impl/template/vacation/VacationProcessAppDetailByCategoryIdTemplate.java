
package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.CategoryHandlerContext;

abstract public class VacationProcessAppDetailByCategoryIdTemplate {

    abstract public NatureSpellingType getNatureSpellingType();

    abstract public void processPersonalCategory(CategoryHandlerContext categoryHandlerContext);

}
