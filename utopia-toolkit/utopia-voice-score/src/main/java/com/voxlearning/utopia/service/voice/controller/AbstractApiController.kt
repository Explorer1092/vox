package com.voxlearning.utopia.service.voice.controller

import com.voxlearning.alps.cipher.AesUtils
import com.voxlearning.alps.core.util.MDCUtils
import com.voxlearning.alps.core.util.MapUtils
import com.voxlearning.alps.core.util.StringUtils
import com.voxlearning.alps.lang.convert.SafeConverter
import com.voxlearning.alps.lang.util.DigestSignUtils
import com.voxlearning.alps.runtime.RuntimeMode
import com.voxlearning.alps.spi.bootstrap.LogCollector
import com.voxlearning.alps.spi.core.SyslogLevel
import com.voxlearning.utopia.api.constant.OperationSourceType
import com.voxlearning.utopia.api.constant.OrderProductServiceType
import com.voxlearning.utopia.core.config.CommonConfiguration
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode
import com.voxlearning.utopia.service.user.api.entities.User
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient
import com.voxlearning.utopia.service.user.consumer.UserServiceClient
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient
import com.voxlearning.utopia.service.voice.interceptor.VoiceScoreContext
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_APP_KEY
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_APP_NATIVE_VERSION
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_CHANNEL
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_SESSION_KEY
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_SIG
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_SYS
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_APP_ERROR_MSG
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_BAD_REQUEST_MSG
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_NEED_RELOGIN_CODE
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_SESSION_KEY_EXPIRED_MSG
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_USER_ERROR_MSG
import com.voxlearning.utopia.service.voice.support.IllegalVendorUserException
import java.util.*
import javax.inject.Inject
import javax.servlet.http.HttpServletRequest

abstract class AbstractApiController {

    companion object {
        const val OPEN_API_COLLECTION_NAME = "vendor_apps_logs"

        val VALID_APP_KEYS = OrderProductServiceType.getAllValidTypes().map { it.name }.toList()
        val VALID_MOBILE_APP_KEYS = listOf("17Student", "17Parent", "17Teacher")
    }

    @Inject protected val userLoaderClient: UserLoaderClient? = null
    @Inject protected val userServiceClient: UserServiceClient? = null
    @Inject protected val vendorLoaderClient: VendorLoaderClient? = null
    @Inject protected val commonConfigServiceClient: CommonConfigServiceClient? = null
    @Inject protected var asyncFootprintServiceClient : AsyncFootprintServiceClient ? = null

    protected var commonConfiguration = CommonConfiguration.getInstance()

    /* ======================================================================================
       以下代码负责 Request & Response
       ====================================================================================== */
    protected fun getWebContext(request: HttpServletRequest): VoiceScoreContext {
        return request.getAttribute(VoiceScoreContext::class.java.name) as VoiceScoreContext
    }

    protected fun getRequestString(request: HttpServletRequest, key: String): String {
        //then URLEncodedUtils can be used to parse query-string ?
        return request.getParameter(key) ?: ""
    }

    fun validateRequest(request: HttpServletRequest, vararg paramKeys: String) {
        val app = getApiRequestApp(request)
        if (app == null) {
            logValidateError(request, "error_app")
            throw IllegalArgumentException(RES_RESULT_APP_ERROR_MSG)
        }

        if (!isValidRequest(request, true, *paramKeys)) {
            logValidateError(request, "error_sig")
            throw IllegalArgumentException(RES_RESULT_BAD_REQUEST_MSG)
        }

        if (!isValidVendorUser(request)) {
            logValidateError(request, "error_vendor_user")
            throw IllegalVendorUserException(RES_RESULT_NEED_RELOGIN_CODE, RES_RESULT_SESSION_KEY_EXPIRED_MSG)
        }

        val curUser = getApiRequestUser(request)
        if (curUser == null) {
            logValidateError(request, "error_user")
            throw IllegalArgumentException(RES_RESULT_USER_ERROR_MSG)
        }
    }

    fun getApiRequestUser(request: HttpServletRequest): User? {
        val ctx = getWebContext(request)

        var requestUser: User? = ctx.requestUser

        if (requestUser == null) {
            val appKey = getRequestString(request, REQ_APP_KEY)
            val sessionKey = getRequestString(request, REQ_SESSION_KEY)

            // get user info by user id
            val userId = decodeUserIdFromSessionKey(commonConfiguration.getSessionEncryptKey(), sessionKey)
            if (userId == null || userId <= 0L) {
                return null
            }

            var appUserRef: VendorAppsUserRef? =
                if (ctx.vendorAppsUserRef == null) vendorLoaderClient!!.loadVendorAppUserRef(
                    appKey,
                    userId
                ) else ctx.vendorAppsUserRef
            if (appUserRef == null) {
                // return null;

                // FIXME 先临时处理一版, Shensz和17JuniorTea/17JuniorStu/17JuniorPar的关系
                if ("Shensz" != appKey) {
                    return null
                }
                val userType = SafeConverter.toString(userId)
                var convertAppKey = "17JuniorStu"
                if (userType.startsWith("1")) {
                    convertAppKey = "17JuniorTea"
                } else if (userType.startsWith("2")) {
                    convertAppKey = "17JuniorPar"
                }

                appUserRef = vendorLoaderClient!!.loadVendorAppUserRef(convertAppKey, userId)
                if (appUserRef == null) {
                    return null
                }
            }

            if (sessionKey != appUserRef.sessionKey) {
                // return null;

                // FIXME 先临时处理一版, Shensz和17JuniorTea/17JuniorStu/17JuniorPar的关系
                if ("Shensz" != appKey) {
                    return null
                }

                val userType = SafeConverter.toString(userId)
                var convertAppKey = "17JuniorStu"
                if (userType.startsWith("1")) {
                    convertAppKey = "17JuniorTea"
                } else if (userType.startsWith("2")) {
                    convertAppKey = "17JuniorPar"
                }

                appUserRef = vendorLoaderClient!!.loadVendorAppUserRef(convertAppKey, userId)
                if (appUserRef == null || sessionKey != appUserRef.sessionKey) {
                    return null
                }
            }

            requestUser = userLoaderClient!!.loadUser(userId)
            ctx.requestUser = requestUser
            ctx.vendorAppsUserRef = appUserRef
        }

        return requestUser
    }

    // 得到请求的Request App
    fun getApiRequestApp(request: HttpServletRequest): VendorApps? {
        val ctx = getWebContext(request)

        var requestApp: VendorApps? = ctx.requestVendorApp
        if (requestApp == null) {
            val appKey = getRequestString(request, REQ_APP_KEY)
            if (StringUtils.isEmpty(appKey)) {
                return null
            }

            requestApp = vendorLoaderClient!!.extension.loadVendorApp(appKey)
            ctx.requestVendorApp = requestApp
        }

        return requestApp
    }

    fun decodeUserIdFromSessionKey(key: String, sessionKey: String): Long? {
        try {
            val data = AesUtils.decryptHexString(key, sessionKey)
            return java.lang.Long.valueOf(data.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        } catch (e: RuntimeException) {
            //e.printStackTrace();
            return -1L
        }

    }

    fun logApiCallInfo(request: HttpServletRequest) {

        // don't log unknown app keys
        val appKey = getRequestString(request, REQ_APP_KEY)
        if (!VALID_APP_KEYS.contains(appKey)) {
            return
        }

        val loggingInfo = HashMap<String, String>()
        loggingInfo[REQ_APP_KEY] = appKey
        loggingInfo[REQ_SESSION_KEY] = getRequestString(request, REQ_SESSION_KEY)
        if (StringUtils.isNoneBlank(getRequestString(request, "source_app"))) {
            loggingInfo["source_app"] = getRequestString(request, "source_app")
        }

        var userId = ""
        val requestUser = getApiRequestUser(request)
        if (requestUser != null) {
            userId = requestUser.id.toString()
        }
        loggingInfo["user_id"] = userId

        var apiName = request.requestURI
        if (apiName.indexOf(".") > 0) {
            apiName = apiName.substring(0, apiName.indexOf("."))
        }
        loggingInfo["api_name"] = apiName

        if ("/v1/clazz/share" == apiName
            || "/v1/clazz/sysshare" == apiName
            || "/v1/user/integral/add" == apiName
            || apiName.startsWith("/v1/user/wechat/")
            || apiName.startsWith("/v1/appmessage/")
        ) {
            loggingInfo["op"] = "batch"
        }

        loggingInfo["app_client_ip"] = getWebContext(request).realRemoteAddress

        LogCollector.getInstance().collect(SyslogLevel.info, OPEN_API_COLLECTION_NAME, loggingInfo)
    }

    // 记录登录次数
    fun logLoginCount(request: HttpServletRequest) {
        val user = getApiRequestUser(request)
        val vendorApps = getApiRequestApp(request)
        // redmine 19054
        // 排除除了学生App端,家长App端,老师App端的所有第三方应用
        if (vendorApps == null || !VALID_MOBILE_APP_KEYS.contains(vendorApps.appKey)) {
            return
        }
        if (user != null && user.id != null) {
            asyncFootprintServiceClient!!.asyncFootprintService.postUserLogin(
                user.id,
                getWebContext(request).realRemoteAddress,
                UserRecordMode.LOGIN, // 这里应该是记错了，这块应该算VALIDATE
                OperationSourceType.app,
                true
            )
        }
    }

    private fun logValidateError(request: HttpServletRequest, reason: String) {
        try {
            val curUser = getApiRequestUser(request)
            com.voxlearning.alps.spi.bootstrap.LogCollector.info(
                "app_validate_request_error_logs",
                MapUtils.map(
                    "app_key", getRequestString(request, REQ_APP_KEY),
                    "system", getRequestString(request, REQ_SYS),
                    "version", getRequestString(request, REQ_APP_NATIVE_VERSION),
                    "has_session_key", StringUtils.isNotBlank(getRequestString(request, REQ_SESSION_KEY)),
                    "reason", reason,
                    "user_id", if (curUser != null) curUser.id else 0,
                    "uri", request.requestURI,
                    "env", RuntimeMode.getCurrentStage(),
                    "time", com.voxlearning.alps.calendar.DateUtils.dateToString(Date()),
                    "params", getRequestAllParamsStr(request),
                    "channel", getRequestString(request, REQ_CHANNEL)
                )
            )
        } catch (ignore: Exception) {
            // ignore it
        }

    }

    private fun getRequestAllParamsStr(request: HttpServletRequest): String {
        val iterator = request.parameterMap.entries.iterator()
        val param = StringBuilder()
        var i = 0
        while (iterator.hasNext()) {
            i++
            val entry = iterator.next()
            if (i == 1)
                param.append("?").append(entry.key).append("=")
            else
                param.append("&").append(entry.key).append("=")
            if (entry.value is Array<String>) {
                param.append(entry.value[0])
            } else {
                param.append(entry.value)
            }
        }
        return param.toString()
    }

    fun isValidRequest(request: HttpServletRequest, hasSessionKey: Boolean, vararg paramKeys: String): Boolean {
        // get the app key from request
        val appKey = getRequestString(request, REQ_APP_KEY)
        if (StringUtils.isEmpty(appKey)) {
            return false
        }

        // validate the sig
        val genRequestSig = generateRequestSig(request, hasSessionKey, getSecretKey(request), *paramKeys)
        val orgRequestSig = getRequestString(request, REQ_SIG)

        return genRequestSig == orgRequestSig
    }

    // 验证客户端传过来的sessionkey是否跟服务端匹配
    // 有些特殊需求会更新sessionkey，导致前后端不一致，进而需要客户端重新登录
    // 方便以后用作sessionkey过期的检验
    fun isValidVendorUser(request: HttpServletRequest): Boolean {
        val sessionKey = getRequestString(request, REQ_SESSION_KEY)
        val ctx = getWebContext(request)
        var vendorAppsUserRef: VendorAppsUserRef? = ctx.vendorAppsUserRef
        if (vendorAppsUserRef == null) {
            val userId = decodeUserIdFromSessionKey(commonConfiguration.sessionEncryptKey, sessionKey)
            vendorAppsUserRef = vendorLoaderClient!!.loadVendorAppUserRef(getRequestString(request, REQ_APP_KEY), userId)
            ctx.vendorAppsUserRef = vendorAppsUserRef
        }
        return vendorAppsUserRef != null && StringUtils.equals(vendorAppsUserRef.sessionKey, sessionKey)
    }

    private fun generateRequestSig(request: HttpServletRequest, hasSessionKey: Boolean, secretKey: String, vararg paramKeys: String): String {
        val paramMap = HashMap<String, String>()
        paramMap[REQ_APP_KEY] = getRequestString(request, REQ_APP_KEY)
        if (hasSessionKey) {
            paramMap[REQ_SESSION_KEY] = getRequestString(request, REQ_SESSION_KEY)
        }

        for (paramKey in paramKeys) {
            paramMap[paramKey] = getRequestString(request, paramKey)
        }

        return DigestSignUtils.signMd5(paramMap, secretKey)
    }

    private fun getSecretKey(request: HttpServletRequest): String {
        return getApiRequestApp(request) ?.secretKey ?: ""
    }

    fun insertUserNameIntoMDC(request: HttpServletRequest) {
        val user = getApiRequestUser(request)
        if (user != null) {
            MDCUtils.insertUserIdMDC(user.id)
        }
    }
}