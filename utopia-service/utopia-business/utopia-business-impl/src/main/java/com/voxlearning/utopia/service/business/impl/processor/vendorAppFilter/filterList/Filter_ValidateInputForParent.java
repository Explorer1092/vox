package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;

import javax.inject.Named;
import java.util.Collections;

/**
 * @author jiangpeng
 * @since 2019-01-03 4:05 PM
 **/
@Named
public class Filter_ValidateInputForParent extends FilterBase {

    @Override
    public void execute(VendorAppFilterContext context) {
        //学生为空 直接返回空
        if (context.getStudentDetail() == null){
            context.setResultVendorApps(Collections.emptyList());
            context.terminateTask();
        }
    }
}
