package com.voxlearning.utopia.service.voice.interceptor

import com.voxlearning.alps.spi.webmvc.RequestHandler
import com.voxlearning.alps.spi.webmvc.ServletRequest
import com.voxlearning.alps.spi.webmvc.ServletResponse
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor
import com.voxlearning.utopia.service.voice.controller.VoiceScoreController
import java.lang.Exception

class VoiceScoreInterceptor : AbstractRequestHandlerInterceptor() {

    override fun preHandle(
        request: ServletRequest,
        response: ServletResponse?,
        handler: RequestHandler?
    ): Boolean {
        handler?.bean as? VoiceScoreController ?: return true

        val controller = handler.bean as VoiceScoreController

        val context = VoiceScoreContext(request.servletRequest, response?.servletResponse)
        request.servletRequest.setAttribute(VoiceScoreContext::class.java.name, context)

        // do logger
        controller.logApiCallInfo(request.servletRequest)

        // record login count
        controller.logLoginCount(request.servletRequest)

        // manually insert userName(userId) into MDC,so that we can see userId in logs
        controller.insertUserNameIntoMDC(request.servletRequest)

        return true
    }

    override fun afterCompletion(
        request: ServletRequest,
        response: ServletResponse?,
        handler: RequestHandler?,
        ex: Exception?
    ) {
        request.servletRequest.removeAttribute(VoiceScoreContext::class.java.name)
    }
}