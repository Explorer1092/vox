package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;

import javax.inject.Named;
import java.util.Collections;

/**
 * 传入参数验证：学生为空　或者无班级，或者是中学生直接返回为空
 *
 * @author peng.zhang.a
 * @since 16-10-17
 */
@Named
public class Filter_ValidateInput extends FilterBase {
    @Override
    public void execute(VendorAppFilterContext context) {
        //学生为空 班级为空 ，是中学生直接返回空
        if (context.getStudentDetail() == null
                || context.getStudentDetail().getClazz() == null
                || (!context.getStudentDetail().isPrimaryStudent() && !context.getStudentDetail().isInfantStudent())) {
            context.setResultVendorApps(Collections.emptyList());
            context.terminateTask();
        }
    }
}
