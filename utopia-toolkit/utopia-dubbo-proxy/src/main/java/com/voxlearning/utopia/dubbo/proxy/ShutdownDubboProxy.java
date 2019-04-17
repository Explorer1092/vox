package com.voxlearning.utopia.dubbo.proxy;

import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.api.bootstrap.BeforeShutdownModule;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.logger.LoggerUtils;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

@Install
final public class ShutdownDubboProxy implements BeforeShutdownModule {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownDubboProxy.class);

    static final AtomicReference<NioEventLoopGroup> bossGroupRef = new AtomicReference<>();
    static final AtomicReference<NioEventLoopGroup> workerGroupRef = new AtomicReference<>();

    @Override
    public void beforeShutdownModule() {
        NioEventLoopGroup bossGroup = bossGroupRef.get();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            LoggerUtils.info(logger, "Shutdown netty boss group", true);
        }
        NioEventLoopGroup workerGroup = workerGroupRef.get();
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            LoggerUtils.info(logger, "Shutdown netty worker group", true);
        }
    }
}
