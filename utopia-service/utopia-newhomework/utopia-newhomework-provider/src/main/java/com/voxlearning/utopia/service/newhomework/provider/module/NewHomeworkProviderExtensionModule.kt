package com.voxlearning.utopia.service.newhomework.provider.module

import com.voxlearning.alps.annotation.common.Install
import com.voxlearning.alps.api.management.ExtensionModule
import com.voxlearning.alps.api.module.InitializeGoblinModuleContext
import com.voxlearning.utopia.service.newhomework.provider.management.controller.NewHomeworkController

@Install
class NewHomeworkProviderExtensionModule : ExtensionModule {

  override fun baseURL(): String? {
    return "/newhomework-provider/index.do"
  }

  override fun name(): String {
    return "NEWHOMEWORK PROVIDER"
  }

  override fun initialize(context: InitializeGoblinModuleContext) {
    context.registerManagementController(NewHomeworkController.INSTANCE)

    context.registerExtensionModule(object : ExtensionModule {
      override fun name(): String {
        return "NEWHOMEWORK CLIENT"
      }

      override fun baseURL(): String? {
        return "/newhomework-provider/index.do?beanType=client"
      }
    })
    context.registerExtensionModule(object : ExtensionModule {
      override fun name(): String {
        return "NEWHOMEWORK IMPL"
      }

      override fun baseURL(): String? {
        return "/newhomework-provider/index.do?beanType=impl"
      }
    })
  }
}