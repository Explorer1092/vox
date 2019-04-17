package com.voxlearning.utopia.service.psr.provider.module

import com.voxlearning.alps.annotation.common.Install
import com.voxlearning.alps.api.management.ExtensionModule
import com.voxlearning.alps.api.module.InitializeGoblinModuleContext
import com.voxlearning.utopia.service.psr.provider.module.management.PsrManagementController

@Install
class PsrExtensionModule : ExtensionModule {

  override fun baseURL(): String? {
    return "/psr/index.do"
  }

  override fun name(): String {
    return "PSR"
  }

  override fun initialize(context: InitializeGoblinModuleContext) {
    context.registerManagementController(PsrManagementController.INSTANCE)
  }
}