package com.voxlearning.utopia.service.surl.module

import com.voxlearning.alps.annotation.common.Install
import com.voxlearning.alps.api.management.ExtensionModule
import com.voxlearning.alps.api.module.InitializeGoblinModuleContext
import com.voxlearning.utopia.service.surl.module.management.IndexManagement

@Install
class SURLModule : ExtensionModule {

  override fun name(): String {
    return "SURL"
  }

  override fun baseURL(): String? {
    return "/surl/index.do"
  }

  override fun initialize(context: InitializeGoblinModuleContext) {
    context.registerManagementController(IndexManagement.instance)
  }
}