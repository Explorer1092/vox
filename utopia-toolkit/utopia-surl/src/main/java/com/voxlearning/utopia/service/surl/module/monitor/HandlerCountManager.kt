package com.voxlearning.utopia.service.surl.module.monitor

import java.util.*
import java.util.concurrent.atomic.LongAdder

class HandlerCountManager private constructor() {

  companion object {
    val instance = HandlerCountManager()
  }

  private val buffer = EnumMap<HandlerType, LongAdder>(HandlerType::class.java)

  init {
    HandlerType.values().forEach {
      buffer[it] = LongAdder()
    }
  }

  fun increment(type: HandlerType) {
    buffer[type]!!.increment()
  }

  fun toList(): List<HandlerCount> {
    return buffer.map { HandlerCount(it.key, it.value.sum()) }.toList()
  }
}