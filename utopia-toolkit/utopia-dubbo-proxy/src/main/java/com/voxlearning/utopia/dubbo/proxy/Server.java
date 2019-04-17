/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.dubbo.proxy;

import com.voxlearning.alps.bootstrap.core.BootstrapApplicationEvent;
import com.voxlearning.alps.bootstrap.core.StandaloneServer;
import com.voxlearning.alps.bootstrap.core.UseSpringConfig;
import com.voxlearning.alps.config.data.common.CommonConfig;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.lang.convert.SafeConverter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Map;

@UseSpringConfig("classpath*:/config/utopia-dubbo-proxy.xml")
public class Server extends StandaloneServer {

    public static final String VERSION = "1.0.20160924";

    public static void main(String[] args) {
        new Server().bootstrap(args);
    }

    @Override
    protected void runAsDaemon(BootstrapApplicationEvent event) {
        // read bindings from config
        CommonConfig commonConfig = ConfigManager.getInstance().getCommonConfig();
        Map<String, String> configs = commonConfig.getConfigs();

        String bindingIp = configs.getOrDefault("dubbo_proxy_binding_ip", "127.0.0.1");
        String bindingPort = configs.getOrDefault("dubbo_proxy_binding_port", "1888");

        //noinspection UnnecessaryLocalVariable
        String host = bindingIp;
        int port = SafeConverter.toInt(bindingPort);

        logger.info("Binding host: {}", host);
        logger.info("Binding port: {}", port);

        // 1 个acceptor线程, n个工作线程
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(512);

        ShutdownDubboProxy.bossGroupRef.set(bossGroup);
        ShutdownDubboProxy.workerGroupRef.set(workerGroup);

        try {
            DubboProxyServer server = new DubboProxyServer();

            ServerBootstrap b = new ServerBootstrap()
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(server);

            Channel channel = b.bind(host, port).sync().channel();

            logger.info("Dubbo Proxy Server ({}): http://{}:{}/", VERSION, host, port);

            channel.closeFuture().sync();
        } catch (InterruptedException ex) {
            logger.error("interrupted", ex);
        }
    }
}
