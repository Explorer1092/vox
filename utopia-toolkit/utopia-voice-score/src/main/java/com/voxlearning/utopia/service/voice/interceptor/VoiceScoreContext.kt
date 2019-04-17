package com.voxlearning.utopia.service.voice.interceptor

import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext
import com.voxlearning.utopia.service.user.api.entities.User
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class VoiceScoreContext(request: HttpServletRequest?, response: HttpServletResponse?) :
    UtopiaHttpRequestContext(request, response) {

    var requestVendorApp: VendorApps? = null
    var requestUser: User? = null
    var vendorAppsUserRef: VendorAppsUserRef? = null
}