package com.voxlearning.utopia.service.voice.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import javax.inject.Named

@Named
@RequestMapping("/")
class WelcomeController {

  @RequestMapping(value = ["index.vpage"], method = [RequestMethod.GET])
  fun index(): String {
    return "welcome"
  }

}