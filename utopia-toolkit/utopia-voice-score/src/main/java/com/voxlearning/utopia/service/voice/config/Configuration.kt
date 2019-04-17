package com.voxlearning.utopia.service.voice.config

import com.voxlearning.alps.core.config.NCS
import java.util.concurrent.atomic.AtomicInteger

object Configuration {

  private val listeningPort: AtomicInteger = AtomicInteger(5945)
  private val corePoolSize: AtomicInteger = AtomicInteger(64)
  private val maximumPoolSize: AtomicInteger = AtomicInteger(256)

  init {
    NCS.getProperty("com.voxlearning.utopia.voice.ListeningPort")?.run {
      listeningPort.set(trim().toInt())
    }
    NCS.getProperty("com.voxlearning.utopia.voice.CorePoolSize")?.run {
      corePoolSize.set(trim().toInt())
    }
    NCS.getProperty("com.voxlearning.utopia.voice.MaximumPoolSize")?.run {
      maximumPoolSize.set(trim().toInt())
    }
  }

  fun getListeningPort(): Int {
    return listeningPort.get()
  }

  fun getCorePoolSize(): Int {
    return corePoolSize.get()
  }

  fun getMaximumPoolSize(): Int {
    return maximumPoolSize.get()
  }

}