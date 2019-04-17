package com.voxlearning.utopia.schedule.module

import com.voxlearning.alps.annotation.common.Install
import com.voxlearning.alps.api.management.ExtensionModule
import com.voxlearning.alps.api.module.InitializeGoblinModuleContext
import com.voxlearning.utopia.schedule.module.management.ScheduleController

@Install
class ScheduleExtensionModule : ExtensionModule {

  override fun baseURL(): String? {
    return "/schedule_service/index.do"
  }

  override fun name(): String {
    return "SCHEDULE"
  }

  override fun initialize(context: InitializeGoblinModuleContext) {
    context.registerManagementController(ScheduleController.INSTANCE)
  }
}