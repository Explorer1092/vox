/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.surl;

import com.voxlearning.alps.bootstrap.core.BootstrapApplicationEvent;
import com.voxlearning.alps.bootstrap.core.StandaloneServer;
import com.voxlearning.alps.bootstrap.core.UseSpringConfig;
import com.voxlearning.alps.embed.jetty.legacy.EmbeddedJettyServer;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by xin.xin on 9/25/15.
 */
@UseSpringConfig("classpath*:/config/utopia-surl.xml")
public class Server extends StandaloneServer {

    public static final String WEBAPP_DIRECTORY = "/WEB-INF";

    public static void main(String[] args) {
        new Server().bootstrap(args);
    }

    @Override
    protected List<Consumer<BootstrapApplicationEvent>> registerBootstrapTasks() {
        Consumer<BootstrapApplicationEvent> consumer = event -> {
            ApplicationContext context = event.getApplicationContext();
            StartEmbeddedServer.INSTANCE.doStart(context);
        };
        return Collections.singletonList(consumer);
    }

    @Override
    protected void runAsDaemon(BootstrapApplicationEvent event) {
        EmbeddedJettyServer embeddedJettyServer = EmbeddedServerContainer.INSTANCE.getValue();
        embeddedJettyServer.start();
        embeddedJettyServer.join();
    }
}
