package com.voxlearning.utopia.service.dubbing.provider;

import com.voxlearning.alps.bootstrap.core.StandaloneServer;
import com.voxlearning.alps.bootstrap.core.UseSpringConfig;

/**
 * @author shiwei.liao
 * @since 2017-8-23
 */
@UseSpringConfig("classpath*:/config/utopia-dubbing-provider.xml")
public class Server extends StandaloneServer {
    public static void main(String[] args) {
        new Server().bootstrap(args);
    }
}
