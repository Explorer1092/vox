package com.voxlearning.utopia.hydra.agent;

import com.voxlearning.alps.bootstrap.core.BootstrapApplicationEvent;
import com.voxlearning.alps.bootstrap.core.StandaloneServer;
import com.voxlearning.alps.bootstrap.core.UseSpringConfig;
import com.voxlearning.alps.config.runtime.ProductConfigJson;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.remote.hydra.agent.server.AgentHttpServer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author changyuan
 * @since 2017/3/6
 */
@UseSpringConfig("classpath*:/config/utopia-hydra-agent.xml")
public class Server extends StandaloneServer {

    public static void main(String[] args) {
        new Server().bootstrap(args);
    }

    @Override
    protected List<Consumer<BootstrapApplicationEvent>> registerBootstrapTasks() {
        return Collections.singletonList(event -> {
            Map rawConfigData = ProductConfigJson.getConfigMap();
            Object hydra = rawConfigData.get("hydra");

            // initialize configuration
            String host = "127.0.0.1";
            int port = 1889;
            if (hydra instanceof Map) {
                Object agent = ((Map) hydra).get("agent");
                if (agent instanceof Map) {
                    Map agentMap = (Map) agent;
                    String h = SafeConverter.toString(agentMap.get("host"));
                    if (StringUtils.isNotEmpty(h)) {
                        host = h;
                    }
                    int p = SafeConverter.toInt(agentMap.get("port"));
                    if (p > 0) {
                        port = p;
                    }
                }
            }

            AgentHttpServer server = new AgentHttpServer(host, port);
            server.start();
        });
    }
}
