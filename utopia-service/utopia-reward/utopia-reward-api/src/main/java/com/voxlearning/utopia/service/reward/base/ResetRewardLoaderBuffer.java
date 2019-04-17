package com.voxlearning.utopia.service.reward.base;

import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.spi.core.SingletonBeanLoader;
import com.voxlearning.alps.spi.test.AbstractTestExecutionListener;
import com.voxlearning.alps.spi.test.TestContext;

@Install
final public class ResetRewardLoaderBuffer extends AbstractTestExecutionListener {
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        SingletonBeanLoader.getInstance()
                .beansOfTypeIncludingAncestors(testContext.getApplicationContext(), AbstractRewardLoader.class)
                .values().forEach(AbstractRewardLoader::resetBuffer);
    }
}
