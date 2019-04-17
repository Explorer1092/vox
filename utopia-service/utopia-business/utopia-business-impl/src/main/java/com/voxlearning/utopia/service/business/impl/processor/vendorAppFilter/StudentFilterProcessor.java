package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter;

import com.voxlearning.utopia.service.business.impl.processor.AbstractProcessor;
import com.voxlearning.utopia.service.business.impl.processor.annotation.ExecuteTaskSupport;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList.*;

import javax.inject.Named;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
@ExecuteTaskSupport({
        Filter_ValidateInput.class,
        Filter_LoadVendorAppsAndOrder.class,
        Filter_LoadBlackList.class,
        Filter_ByCommonChain.class,
        Filter_ByStudentAppSystem.class,
        Filter_ByStudentBlackList.class,
        Filter_PaidAndExpired.class,
        Filter_SortVendorApp.class
})
public class StudentFilterProcessor extends AbstractProcessor<VendorAppFilterContext> {
}
