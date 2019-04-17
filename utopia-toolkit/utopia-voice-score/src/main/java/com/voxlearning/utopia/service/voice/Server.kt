package com.voxlearning.utopia.service.voice

import com.voxlearning.alps.bootstrap.core.BootstrapApplicationEvent
import com.voxlearning.alps.bootstrap.core.StandaloneServer
import com.voxlearning.alps.bootstrap.core.UseSpringConfig
import com.voxlearning.alps.embed.core.filter.DefaultFilter
import com.voxlearning.alps.embed.core.handler.DispatcherHandler
import com.voxlearning.alps.embed.core.manager.ServletServerManager
import com.voxlearning.alps.embed.core.resource.ClassPathStaticResourceManager
import com.voxlearning.alps.embed.core.resource.InjvmStaticResourceBuffer
import com.voxlearning.alps.embed.core.setting.ServerSetting
import com.voxlearning.alps.spi.embed.EmbedServerProvider
import com.voxlearning.alps.webmvc.handler.RequestHandlerManagerBuilder
import com.voxlearning.utopia.service.voice.config.Configuration
import org.springframework.core.io.ClassPathResource

@UseSpringConfig("classpath*:/config/utopia-voice-score.xml")
class Server : StandaloneServer() {

  override fun runAsDaemon(event: BootstrapApplicationEvent?) {
    val handler = DispatcherHandler()
    handler.registerFilter(DefaultFilter())
    handler.registerWelcomeFile("/index.vpage")
    val builder = RequestHandlerManagerBuilder.getInstance()
    val handlerManager = builder.build("UtopiaVoiceScore", event?.applicationContext)
    handler.registerRequestHandlerManager(handlerManager, ".vpage")
    handler.registerFaviconResource(ClassPathResource("WEB-INF/favicon/favicon.ico"))
    val buffer = InjvmStaticResourceBuffer(8192)
    handler.registerStaticResourceManager(ClassPathStaticResourceManager(buffer, "/static", "/WEB-INF/static"))

    val setting = ServerSetting.builder()
        .name("UtopiaVoiceScoreServer")
        .system(EmbedServerProvider.JETTY)
        .useGzip(true)
        .applyConnectorSetting {
          it.port(Configuration.getListeningPort())
        }
        .applyThreadPoolSetting {
          it.corePoolSize(Configuration.getCorePoolSize())
          it.maximumPoolSize(Configuration.getMaximumPoolSize())
        }
        .applyHandlerSetting {
          it.contextPath("/")
          it.handler(handler)
        }
        .build()

    val server = ServletServerManager.getInstance().createEmbedServer(setting)
    server.start()

    super.runAsDaemon(event)
  }
}