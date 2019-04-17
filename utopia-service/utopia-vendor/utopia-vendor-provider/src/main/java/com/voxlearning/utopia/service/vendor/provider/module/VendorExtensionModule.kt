package com.voxlearning.utopia.service.vendor.provider.module

import com.voxlearning.alps.annotation.common.Install
import com.voxlearning.alps.api.management.ExtensionModule
import com.voxlearning.alps.api.module.InitializeGoblinModuleContext
import com.voxlearning.utopia.service.vendor.provider.module.management.*

@Install
class VendorExtensionModule : ExtensionModule {

  override fun baseURL(): String? {
    return "/vendor-provider/index.do"
  }

  override fun name(): String {
    return "VENDOR"
  }

  override fun initialize(context: InitializeGoblinModuleContext) {
    context.registerManagementController(VendorController.INSTANCE)
    context.registerManagementController(FairylandProductBufferController.INSTANCE)
    context.registerManagementController(VendorAppsBufferController.INSTANCE)
    context.registerManagementController(VendorAppsResgRefBufferController.INSTANCE)
    context.registerManagementController(VendorResgContentBufferController.INSTANCE)
  }
}