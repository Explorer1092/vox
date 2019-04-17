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
        Filter_ValidateInputForParent.class,
        Filter_LoadVendorAppsAndOrder.class,
        Filter_LoadBlackList.class,
        Filter_ByCommonChainForParent.class,
        Filter_ByParentChain.class,
        Filter_ByParentBlackList.class,
        Filter_SortVendorApp.class
})
public class ParentFilterProcessor extends AbstractProcessor<VendorAppFilterContext> {
}
