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

package com.voxlearning.utopia.service.surl;

import com.voxlearning.alps.core.util.ClassUtils;
import com.voxlearning.alps.embed.jetty.legacy.EmbeddedJettyServer;
import com.voxlearning.alps.lang.util.ClassPathUtils;
import com.voxlearning.alps.logger.Logger;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.surl.handler.DefaultHandler;
import com.voxlearning.utopia.service.surl.handler.ShortUrlDecodeHandler;
import com.voxlearning.utopia.service.surl.handler.ShortUrlEncodeHandler;
import com.voxlearning.utopia.service.surl.handler.ShortUrlHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

final public class StartEmbeddedServer {

    private static final Logger logger = LoggerFactory.getLogger(StartEmbeddedServer.class);

    static final StartEmbeddedServer INSTANCE = new StartEmbeddedServer();

    private final AtomicBoolean started = new AtomicBoolean();

    public void doStart(ApplicationContext ac) {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        int serverPort = Configure.getServerPort();
        logger.info("ShortURL server port: {}", serverPort);

        EmbeddedJettyServer embeddedJettyServer = new EmbeddedJettyServer(serverPort, true);

        //encode handler
        ShortUrlEncodeHandler encodeHandler = new ShortUrlEncodeHandler(ac);
        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath(ShortUrlHandler.SHORT_URL_CREATE_PATH);
        contextHandler.setHandler(encodeHandler);
        contextHandler.setAllowNullPathInfo(true);
        embeddedJettyServer.addHandler(contextHandler);

        //decode handler
        ShortUrlDecodeHandler decodeHandler = new ShortUrlDecodeHandler(ac);
        embeddedJettyServer.addHandler(decodeHandler);

        //web handler
        String path = ClassPathUtils.getClassFileSystemPath(Server.class);
        if (new File(path + Server.WEBAPP_DIRECTORY).exists()) {
            path = path + Server.WEBAPP_DIRECTORY;
        } else {
            String separator = File.pathSeparator;
            String[] classPaths = System.getProperty("java.class.path").split(separator);
            for (String classPath : classPaths) {
                if (new File(classPath + Server.WEBAPP_DIRECTORY).exists()) {
                    path = classPath + Server.WEBAPP_DIRECTORY;
                    break;
                }
            }
        }

        WebAppContext webApp = new WebAppContext(path, "/");
        webApp.setClassLoader(ClassUtils.getDefaultClassLoader());
        webApp.setDescriptor("web.xml");
        webApp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        webApp.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
        webApp.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webApp.setCompactPath(true);
        embeddedJettyServer.addHandler(webApp);

        DefaultHandler defaultHandler = new DefaultHandler();
        embeddedJettyServer.addHandler(defaultHandler);

        EmbeddedServerContainer.INSTANCE.setValue(embeddedJettyServer);
    }
}
