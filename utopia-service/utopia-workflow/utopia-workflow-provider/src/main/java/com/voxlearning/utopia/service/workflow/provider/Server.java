package com.voxlearning.utopia.service.workflow.provider;

import com.voxlearning.alps.bootstrap.core.StandaloneServer;
import com.voxlearning.alps.bootstrap.core.UseSpringConfig;

/**
 * Utopia JZT server implementation.
 *
 * @author fugui chang
 * @since 2016-11-02
 */
@UseSpringConfig("classpath*:/config/utopia-workflow-provider.xml")
public class Server extends StandaloneServer {
    public static void main(String[] args) {
        new Server().bootstrap(args);
    }
}
