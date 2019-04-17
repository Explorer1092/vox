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

public class AgentDebugMain {

    //TODO: 在 idea 的运行配置中，请选中“Single instance only”，这样就不会因为多个实例冲突了。

    //TODO: 可以根据需要，自定义端口
    public static final int HTTP_PORT = 8083;

    public static void main(String[] args) throws Exception {

        // =============== NEW CONFIGURATION INITIALIZE --- ADD BY ZHAO REX ==============================
        ConfigService.initConfigService("washington", "dev", "dev", "cdo1a.test.17zuoye.net");
        // =============== NEW CONFIGURATION INITIALIZE END --- ADD BY ZHAO REX ==========================

        DebugJettyServer srv = new DebugJettyServer();
        srv.start(HTTP_PORT, ClassPathUtils.getClassFileSystemPath(AgentDebugMain.class));
        srv.join();
    }
}
