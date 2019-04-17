package com.voxlearning.utopia.service.vendor.impl.support;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.piclisten.support.OrderSynchronizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

/**
 * @author xinxin
 * @since 7/11/17.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestOrderSynchronizer extends SpringContainerSupport {

    @Test
    public void test() {
        Map<String, OrderSynchronizer> synchronizerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), OrderSynchronizer.class);
        Assert.assertEquals(true, MapUtils.isNotEmpty(synchronizerMap));

    }
}
