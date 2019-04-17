package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.impl.processor.Processor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
public class VendorAppFilterManager extends SpringContainerSupport {

    @Inject private ParentFilterProcessor parentProcessor;
    @Inject private StudentFilterProcessor childrenProcessor;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    public Processor getParentProcessor() {
        return parentProcessor;
    }

    public Processor getChildrenProcessor() {
        return childrenProcessor;
    }
}
