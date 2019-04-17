package com.voxlearning.utopia.hydra.agent.http2

import com.voxlearning.alps.bootstrap.core.BootstrapApplicationEvent
import com.voxlearning.alps.bootstrap.core.StandaloneServer
import com.voxlearning.alps.bootstrap.core.UseSpringConfig
import com.voxlearning.alps.config.runtime.ProductConfigJson
import com.voxlearning.alps.core.util.StringUtils
import com.voxlearning.alps.lang.convert.SafeConverter
import com.voxlearning.alps.remote.hydra.agent.server.AgentHttp2Server
import java.util.function.Consumer

@UseSpringConfig("classpath*:/config/utopia-hydra-agent-http2.xml")
class Server : StandaloneServer() {

    override fun registerBootstrapTasks(): List<Consumer<BootstrapApplicationEvent>> {
        return listOf(Consumer {
            val rawConfigData = ProductConfigJson.getConfigMap()
            val hydra = rawConfigData["hydra"]

            // initialize configuration
            var host = "127.0.0.1"
            var port = 1889
            if (hydra is Map<*, *>) {
                val agent = hydra["agent"]
                if (agent is Map<*, *>) {
                    val h = SafeConverter.toString(agent["host"])
                    if (StringUtils.isNotEmpty(h)) {
                        host = h
                    }
                    val p = SafeConverter.toInt(agent["port"])
                    if (p > 0) {
                        port = p
                    }
                }
            }

            val server = AgentHttp2Server(host, port)
            server.start()
        })
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Server().bootstrap(args)
        }
    }
}