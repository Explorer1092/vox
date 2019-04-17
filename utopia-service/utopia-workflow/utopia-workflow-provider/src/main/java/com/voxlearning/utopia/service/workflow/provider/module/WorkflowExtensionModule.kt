package com.voxlearning.utopia.service.workflow.provider.module

import com.voxlearning.alps.annotation.common.Install
import com.voxlearning.alps.api.management.ExtensionModule
import com.voxlearning.alps.api.module.InitializeGoblinModuleContext
import com.voxlearning.utopia.service.workflow.provider.module.management.WorkflowConfigController

@Install
class WorkflowExtensionModule : ExtensionModule {

  override fun baseURL(): String? {
    return "/workflow/index.do"
  }

  override fun name(): String {
    return "WORKFLOW"
  }

  override fun initialize(context: InitializeGoblinModuleContext) {
    context.registerManagementController(WorkflowConfigController.INSTANCE)
  }
}