package com.voxlearning.utopia.service.surl.module.management

import com.voxlearning.utopia.service.surl.module.monitor.HandlerCountManager
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@RequestMapping("/surl")
class IndexManagement private constructor() {

  companion object {
    val instance = IndexManagement()
  }

  @RequestMapping(value = ["index.do"], method = [RequestMethod.GET])
  fun index(model: Model): String {
    model.addAttribute("handlerCountManager", HandlerCountManager.instance)
    return "surl/index"
  }
}