package com.voxlearning.utopia.service.reward.buffer.internal;


import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.alps.api.context.NamedBean;
import com.voxlearning.alps.spi.test.AbstractTestExecutionListener;
import com.voxlearning.alps.spi.test.TestContext;

@Install
public class ResetJVMRewardActivityBuffer extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        ApplicationContextScanner.getInstance()
                .getBeansOfType(ApplicationContextScanner.Scope.ALL, JVMRewardActivityBuffer.Aware.class)
                .stream()
                .map(NamedBean::getBean)
                .forEach(JVMRewardActivityBuffer.Aware::resetRewardActivityBuffer);
    }
}
