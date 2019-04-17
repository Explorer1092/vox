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

import com.nature.configuration.client.config.ConfigService;
import com.voxlearning.alps.embed.jetty.legacy.DebugJettyServer;
import com.voxlearning.alps.lang.util.ClassPathUtils;

/**
 * @author Xin Xin
 * @since 10/15/15
 * <p>
 * 从WSD复制过来的
 */
public class LuffyDebugMain {
    //TODO: 在 idea 的运行配置中，请选中“Single instance only”，这样就不会因为多个实例冲突了。

    //TODO: 可以根据需要，自定义端口
    public static final int HTTP_PORT = 8190;

    public static void main(String[] args) throws Exception {
        /**
         * 为什么启动需要10秒多？时间分配如下：
         * jetty WebAppClassLoader.loadClass 2-3秒
         * spring CachedIntrospectionResult.<init>  2-3秒  主要用于be an的缓存，后续处理各种依赖之类的？
         * spring JdkDynamicAopProxy.getProxy()  0.5 秒
         * 各种xml加载 大概1-2秒
         * 各种注解处理 大概1-2秒
         * dubbo各种初始化 大概1-2秒
         */

        // =============== NEW CONFIGURATION INITIALIZE --- ADD BY ZHAO REX ==============================
        ConfigService.initConfigService("washington", "dev", "dev", "cdo1a.test.17zuoye.net");
        // =============== NEW CONFIGURATION INITIALIZE END --- ADD BY ZHAO REX ==========================

        // EmbeddedProfiler ep = new EmbeddedProfiler(); ep.interval = 1; ep.startCollecting();
        DebugJettyServer srv = new DebugJettyServer();
        srv.start(HTTP_PORT, ClassPathUtils.getClassFileSystemPath(LuffyDebugMain.class));
        //ep.stopCollecting(); System.out.println(ep.getTop(100));
        srv.join();
    }
}
