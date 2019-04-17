package com.voxlearning.utopia.service.zone.provider.module

import com.voxlearning.alps.annotation.common.Install
import com.voxlearning.alps.api.management.ExtensionModule
import com.voxlearning.alps.api.module.InitializeGoblinModuleContext
import com.voxlearning.utopia.service.zone.provider.module.management.ClazzZoneProductBufferController
import com.voxlearning.utopia.service.zone.provider.module.management.GiftBufferController
import com.voxlearning.utopia.service.zone.provider.module.management.UserMoodBufferController
import com.voxlearning.utopia.service.zone.provider.module.management.ZoneController

@Install
class ZoneExtensionModule : ExtensionModule {

  override fun baseURL(): String? {
    return "/zone-provider/index.do"
  }

  override fun name(): String {
    return "ZONE"
  }

  override fun initialize(context: InitializeGoblinModuleContext) {
    context.registerManagementController(ZoneController.INSTANCE)
    context.registerManagementController(UserMoodBufferController.INSTANCE)
    context.registerManagementController(GiftBufferController.INSTANCE)
    context.registerManagementController(ClazzZoneProductBufferController.INSTANCE)
  }
}