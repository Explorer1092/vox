package com.voxlearning.utopia.service.voice.controller

import com.voxlearning.alps.annotation.common.Mode
import com.voxlearning.alps.annotation.meta.Subject
import com.voxlearning.alps.api.monitor.PublishMonitorGenericCountEvent
import com.voxlearning.alps.api.monitor.PublishMonitorGenericInvocationEvent
import com.voxlearning.alps.cache.atomic.AtomicLockManager
import com.voxlearning.alps.core.util.CacheKeyGenerator
import com.voxlearning.alps.core.util.CollectionUtils
import com.voxlearning.alps.core.util.MapUtils
import com.voxlearning.alps.core.util.StringUtils
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor
import com.voxlearning.alps.lang.convert.SafeConverter
import com.voxlearning.alps.lang.mapper.json.JsonUtils
import com.voxlearning.alps.lang.util.MapMessage
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils
import com.voxlearning.alps.runtime.RuntimeMode
import com.voxlearning.alps.spi.bootstrap.LogCollector
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_APP_KEY
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_FILE
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_FILE_INFO
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_SYS
import com.voxlearning.utopia.service.voice.support.ApiConstants.REQ_UUID
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_MESSAGE
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_BAD_REQUEST_CODE
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_DATA_ERROR_MSG
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_DUPLICATE_OPERATION
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_SCORE_IDS_ERROR
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_SUCCESS
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_RESULT_USER_ACCOUNT_NOT_EXIST_MSG
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_SUBJECTIVE_UPLOAD_FAIL_MSG
import com.voxlearning.utopia.service.voice.support.ApiConstants.RES_VOICE_SCORE_INFO
import com.voxlearning.utopia.service.voice.support.IllegalVendorUserException
import com.voxlearning.utopia.service.voice.support.VoiceExpand
import com.voxlearning.utopia.service.voice.support.VoiceParam
import lombok.Cleanup
import org.apache.http.Consts
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.SkipPrettyLogger
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import java.time.Instant
import java.util.*
import javax.servlet.http.HttpServletRequest
import kotlin.collections.HashMap

@Controller
@RequestMapping(value = ["/v1/student/voice/"])
class VoiceScoreController : AbstractApiController() {

    companion object {
        val logger = LoggerFactory.getLogger(VoiceScoreController::class.java)
    }

    private val DEFAULT_APP_KEY = "5d702d58446a11e8a6888cec4b45cd20"
    private val AI_TEACHER_APP_KEY = "5d700648446a11e8a8a78cec4b45cd20"

    private val JUNIOR_VOICE_17URL = "http://vox_jun.17zuoye.com/compute"
    private val MIDDLE_VOICE_17URL = "http://vox_mid.17zuoye.com/compute"

    private val REQ_APP_KEY_JUNIOR = "17JuniorStu"

    // 宁波半开放题型，2019-4-2，找文创
    private val YIQI_TO_SEMI_OPEN = "yiqi_to_semi_open"
    private val SEMI_OPEN_17URL = "http://semi-open-voice.17zuoye.com"

    /**
     * 上传入口
     * file_info:{"size":"文件大小","text":"阅读文本","back_voice_coefficient","back_voice_mode"}
     */
    @SkipPrettyLogger
    @RequestMapping(value = ["score.vpage"], method = [(RequestMethod.POST)])
    @ResponseBody
    fun voiceScore(request: HttpServletRequest): MapMessage {
        val mapMessage = MapMessage()
        try {
            validateRequest(request, REQ_FILE_INFO)
        } catch (e: IllegalArgumentException) {
            if (e is IllegalVendorUserException) {
                mapMessage.add(RES_RESULT, e.code)
                mapMessage.add(RES_MESSAGE, e.message)
                return mapMessage
            }
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
            mapMessage.add(RES_MESSAGE, e.message)
            return mapMessage
        }

        val reqAppKey = getRequestString(request, REQ_APP_KEY)
        val sys = getRequestString(request, REQ_SYS)
        val uuid = getRequestString(request, REQ_UUID)
        val fileInfo = getRequestString(request, REQ_FILE_INFO)
        val map = JsonUtils.fromJson(fileInfo)
        if (MapUtils.isEmpty(map)) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
            mapMessage.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG)
            return mapMessage
        }

        val user = getApiRequestUser(request)
        if (user == null || user.id == null) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
            mapMessage.add(RES_MESSAGE, RES_RESULT_USER_ACCOUNT_NOT_EXIST_MSG)
            return mapMessage
        }
        var resultMap: Map<String, Any?> = HashMap()
        try {
            val ua = request.getHeader("User-Agent")
            val ip = getWebContext(request).realRemoteAddress
            val multipartRequest = request as MultipartHttpServletRequest
            val file = multipartRequest.getFile(REQ_FILE)
            if (file != null) {
                val lock = CacheKeyGenerator.generateCacheKey(this::class.java, null, user.id)
                AtomicLockManager.getInstance().acquireLock(lock)
                try {
                    resultMap = doUploadResult(user.id, file, map, ua, ip, sys, reqAppKey, uuid)
                } finally {
                    AtomicLockManager.getInstance().releaseLock(lock)
                }
            }

            if (MapUtils.isEmpty(resultMap) || resultMap["score_json"] == null || StringUtils.isBlank(resultMap["score_json"] as String)) {
                mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
                mapMessage.add(RES_MESSAGE, RES_RESULT_SCORE_IDS_ERROR)
                return mapMessage
            }

        } catch (ex: DuplicatedOperationException) {
            logger.warn("Upload voice writing (DUPLICATED OPERATION): (userId={})", user.id, ex.message)
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
            mapMessage.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION)
            return mapMessage
        } catch (ex: RuntimeException) {
            logger.warn("Upload voice failed writing: (userId={})", user.id, ex.message)
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
            mapMessage.add(RES_MESSAGE, ex.message)
            return mapMessage
        } catch (ex: Exception) {
            logger.warn("Upload voice failed writing: (userId={})", user.id, ex.message)
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
            mapMessage.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG)
            return mapMessage
        }


        if (MapUtils.isEmpty(resultMap)) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
            mapMessage.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG)
            return mapMessage
        }

        mapMessage.add(RES_VOICE_SCORE_INFO, resultMap)
        mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS)
        return mapMessage
    }

    /**
     * 云知声保存文件
     *
     * @param userId      用户id
     * @param file        文件
     * @param fileInfoMap 文件信息map
     * @return map
     */
    fun doUploadResult(
            userId: Long?,
            file: MultipartFile,
            fileInfoMap: Map<String, Any?>,
            ua: String,
            ip: String,
            sys: String,
            reqAppKey: String,
            uuid: String
    ): Map<String, Any> {
        if (file.size != SafeConverter.toLong(fileInfoMap["size"] as? Int)) {
            return emptyMap()
        }
        val resultMap = HashMap<String, Any>()
        val text = SafeConverter.toString(fileInfoMap["text"], "")
        var coefficient = SafeConverter.toString(fileInfoMap["back_voice_coefficient"], "") // 打分系数
        if (StringUtils.isBlank(coefficient)) {
            coefficient = SafeConverter.toString(fileInfoMap["voiceCoefficient"], "")
        }
        var mode = SafeConverter.toString(fileInfoMap["back_voice_mode"], "") // 打分模式
        if (StringUtils.isBlank(mode)) {
            mode = SafeConverter.toString(fileInfoMap["model"], "")
        }
        var language = SafeConverter.toString(fileInfoMap["back_voice_type"], "")
        if (StringUtils.isBlank(language)) {
            language = SafeConverter.toString(fileInfoMap["voice_type"], "")
        }
        // 打分引擎
        val voiceEngine = SafeConverter.toString(fileInfoMap["back_voice_engine"], "")
        val expand = fileInfoMap["expand"] as? Map<String, Any?> // 扩展属性
        var appkey = DEFAULT_APP_KEY
        if (MapUtils.isNotEmpty(expand)) {
            val voiceExpand = JsonUtils.safeConvertMapToObject<VoiceExpand>(expand, VoiceExpand::class.java)
            if (voiceExpand != null && StringUtils.isNotBlank(voiceExpand.source)) {
                appkey = voiceExpand.source!!
            }
        }

        val voiceParam = VoiceParam(
                file,
                text,
                coefficient,
                mode,
                userId,
                resultMap,
                ua,
                ip,
                language,
                appkey,
                sys,
                fileInfoMap,
                reqAppKey,
                uuid,
                voiceEngine
        )

        // AI_Teacher打分不受灰度控制
        if (StringUtils.equalsIgnoreCase(AI_TEACHER_APP_KEY, appkey)) {
            get17ZYVoiceScoreJson(voiceParam)
            return voiceParam.resultMap
        }

        // 中学部分的打分
        if (StringUtils.equalsIgnoreCase(REQ_APP_KEY_JUNIOR, reqAppKey)) {
            middleVoice(voiceParam)
            return voiceParam.resultMap
        }

        val use17VoiceScoreStr = SafeConverter.toString(
                commonConfigServiceClient!!.commonConfigBuffer.loadCommonConfigValue(
                        ConfigCategory.PRIMARY_PLATFORM_GENERAL.name,
                        "USE_17VOICE_SCORE"
                ), ""
        )

        val use17VoiceScore = StringUtils.split(use17VoiceScoreStr, ",")
        if (StringUtils.equalsIgnoreCase(language, Subject.CHINESE.name)) {
            getVoiceScoreJson(voiceParam)
        } else {
            if (StringUtils.isNotBlank(use17VoiceScoreStr) && use17VoiceScore.size == 2) {
                // use17VoiceScoreStr示例，(true，1|2|3)，使用自研打分，并且学生id最后一位包含在其中
                val userIds = Arrays.asList(*StringUtils.split(use17VoiceScore[1], "|"))
                val last = StringUtils.substring(userId.toString(), userId.toString().length - 1)
                if (CollectionUtils.containsAny(userIds, listOf(last))) {
                    // 命中
                    get17ZYVoiceScoreJson(voiceParam)
                } else {
                    // 没命中
                    getVoiceScoreJson(voiceParam)
                }
            } else if (StringUtils.isNotBlank(use17VoiceScoreStr)
                    && use17VoiceScore.size == 1
                    && SafeConverter.toBoolean(use17VoiceScore[0], false)
            ) {
                // use17VoiceScoreStr示例，(true/false)，全量使用或不使用自研打分
                get17ZYVoiceScoreJson(voiceParam)
            } else {
                // 用云知声把~
                getVoiceScoreJson(voiceParam)
            }
        }

        return voiceParam.resultMap
    }

    fun middleVoice(voiceParam: VoiceParam) {
        val use17VoiceScoreStr = SafeConverter.toString(
                commonConfigServiceClient!!.commonConfigBuffer.loadCommonConfigValue(
                        ConfigCategory.PRIMARY_PLATFORM_GENERAL.name,
                        "JUNIOR_USE_17VOICE_SCORE"
                ), ""
        )
        val use17VoiceScore = StringUtils.split(use17VoiceScoreStr, ",")
        if (StringUtils.isNotBlank(use17VoiceScoreStr) && use17VoiceScore.size == 2) {
            // use17VoiceScoreStr示例，(true，1|2|3)，使用自研打分，并且学生id最后一位包含在其中1
            val userIds = Arrays.asList(StringUtils.split(use17VoiceScore[1], "|"))
            val last = StringUtils.substring(voiceParam.userId.toString(), voiceParam.userId.toString().length - 1)
            if (CollectionUtils.containsAny(userIds, Collections.singletonList(last))) {
                // 命中
                get17ZYVoiceScoreJson(voiceParam)
            } else {
                // 没命中
                getVoiceScoreJson(voiceParam)
            }
        } else if (StringUtils.isNotBlank(use17VoiceScoreStr)
                && use17VoiceScore.size == 1
                && SafeConverter.toBoolean(use17VoiceScore[0], false)
        ) {
            // use17VoiceScoreStr示例，(true/false)，全量使用或不使用自研打分
            get17ZYVoiceScoreJson(voiceParam)
        } else {
            // 用云知声把~
            getVoiceScoreJson(voiceParam)
        }
    }


    /**
     * 自己研发的语音打分
     */
    fun get17ZYVoiceScoreJson(voiceParam: VoiceParam) {
        val start = Instant.now()
        try {
            internalGet17ZYVoiceScoreJson(voiceParam)
        } finally {
            val stop = Instant.now()
            val duration = stop.toEpochMilli() - start.toEpochMilli()
            PublishMonitorGenericInvocationEvent.publish("YZYInvocation", stop.epochSecond, duration)
            if (duration >= 3000) {
                PublishMonitorGenericCountEvent.publish("YZYTimeout", stop.epochSecond, 1)
            }
        }
    }

    /**
     * 自研打分
     */
    fun internalGet17ZYVoiceScoreJson(voiceParam: VoiceParam) {
        val resultMap = HashMap<String, Any>()

        val uuid = UUID.randomUUID().toString()
        val headers = HashMap<String, String?>()
        headers["appkey"] = voiceParam.appkey
        headers["session-id"] = uuid
        headers["device-id"] = voiceParam.deviceId
        headers["user-id"] = voiceParam.userId.toString()
        headers["protocol"] = "http"
        headers["sys"] = voiceParam.sys

        // 打分默认用小学的
        var voiceScoreUrl = JUNIOR_VOICE_17URL

        // 中学的打分
        if (StringUtils.equalsIgnoreCase(REQ_APP_KEY_JUNIOR, voiceParam.reqAppKey)) {
            voiceScoreUrl = MIDDLE_VOICE_17URL
        }
        // 宁波半开放题型切自研
        if (StringUtils.equalsIgnoreCase(YIQI_TO_SEMI_OPEN, voiceParam.voiceEngine)) {
            voiceScoreUrl = SEMI_OPEN_17URL
        }

        try {
            @Cleanup val `in` = voiceParam.multipartFile.inputStream
            val builder = MultipartEntityBuilder.create()
            builder.addTextBody("mode", StringUtils.upperCase(voiceParam.mode))
            builder.addTextBody("text", voiceParam.text)
            builder.addTextBody("codec", "opus")
            builder.addBinaryBody("voice", `in`, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8))
            // connect_timeout = 5000, socket_timeout = 10000
            val response = HttpRequestExecutor.defaultInstance()
                    .post(voiceScoreUrl)
                    .headers(headers)
                    .entity(builder.build())
                    .execute()

            // 获取返回数据
            val scoreJson = response.responseString
            val scoreMap = JsonUtils.convertJsonObjectToMap(scoreJson)
            val voiceUrl = if (MapUtils.isNotEmpty(scoreMap)) SafeConverter.toString(scoreMap["voiceURI"], "") else ""
            resultMap["score_json"] = scoreJson
            resultMap["file_url"] = voiceUrl
            voiceParam.resultMap = resultMap

            // 我是日志
            val logMap = MapUtils.map<String, String>(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", voiceParam.userId,
                    "agent", voiceParam.ua,
                    "mod1", voiceUrl,
                    "mod2", voiceParam.multipartFile.size,
                    "mod3", response.statusCode,
                    "mod4", JsonUtils.toJson(voiceParam.fileInfoMap),
                    "mod5", voiceParam.ip,
                    "mod6", "17zuoye",
                    "mod7", voiceParam.language,
                    "mod8", response.responseString,
                    "mod9", voiceParam.appkey,
                    "mod10", voiceParam.reqAppKey,
                    "mod11", voiceScoreUrl,
                    "op", "UsingBackgroundScore",
                    "time", System.currentTimeMillis()
            )
            if (RuntimeMode.le(Mode.TEST)) {
                logger.info(JsonUtils.toJson(logMap))
            }
            LogCollector.info("backend-general", logMap)
        } catch (e: Exception) {
            val logMap = MapUtils.map<String, String>(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", voiceParam.userId,
                    "mod6", "17zuoye",
                    "mod9", voiceParam.appkey,
                    "op", "UsingBackgroundScore Exception",
                    "mod1", e
            )
            logger.error("backend-general exception: " + JsonUtils.toJson(logMap))
            LogCollector.info("backend-general", logMap)
        }

    }

    /**
     * 云知声打分，配置的东西暂时先扔在这
     *
     * @param multipartFile 文件
     * @param text          参照文本
     * @param userId        用户id
     * @param resultMap     结果map
     */
    fun getVoiceScoreJson(voiceParam: VoiceParam) {
        val start = Instant.now()
        try {
            internalGetVoiceScoreJson(voiceParam)
        } finally {
            val stop = Instant.now()
            val duration = stop.toEpochMilli() - start.toEpochMilli()
            PublishMonitorGenericInvocationEvent.publish("YZSInvocation", stop.epochSecond, duration)
            if (duration >= 3000) {
                PublishMonitorGenericCountEvent.publish("YZSTimeout", stop.epochSecond, 1)
            }
        }
    }

    /**
     * 云之声打分
     */
    fun internalGetVoiceScoreJson(voiceParam: VoiceParam) {
        val uuid = UUID.randomUUID().toString()
        if (StringUtils.isBlank(voiceParam.coefficient)) {
            voiceParam.coefficient = "1.6"
            // coefficient = "1.6";
        }


        if (StringUtils.isBlank(voiceParam.mode)) {
            voiceParam.mode = "E"
        }

        val headers = HashMap<String, String>()
        headers["appkey"] = "zcdmqelsinu6i6dliln6nqowfq2v2euvduhzqxqx"
        headers["session-id"] = uuid
        headers["device-id"] = voiceParam.userId!!.toString()
        headers["score-coefficient"] = voiceParam.coefficient

        val resultMap = HashMap<String, Any>()

        try {
            @Cleanup val `in` = voiceParam.multipartFile.inputStream
            val builder = MultipartEntityBuilder.create()
            builder.addTextBody("mode", StringUtils.upperCase(voiceParam.mode))
            builder.addTextBody("text", voiceParam.text, ContentType.create("text/plain", Consts.UTF_8))
            builder.addBinaryBody("voice", `in`, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8))
            // connect_timeout = 5000, socket_timeout = 10000

            val response: AlpsHttpResponse
            if (StringUtils.equalsIgnoreCase(voiceParam.language, Subject.CHINESE.name)) {
                headers["X-EngineType"] = "oral.zh_CH"
                // 中文打分
                response = HttpRequestExecutor.defaultInstance()
                        .post("http://cn-edu.hivoice.cn/eval/opus")
                        .headers(headers)
                        .entity(builder.build())
                        .execute()
            } else {
                // 英文打分
                response = HttpRequestExecutor.defaultInstance()
                        .post("http://edu.hivoice.cn:8085/eval/opus")
                        .headers(headers)
                        .entity(builder.build())
                        .execute()
            }

            // 获取返回数据
            val sessionId = response.getFirstHeader("Session-Id").value
            val session = StringUtils.split(sessionId, ":")
            val voiceUrl =
                    "http://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/" + session[2] + "/" + session[1] + "/" + session[0]

            resultMap["score_json"] = response.responseString
            resultMap["file_url"] = voiceUrl
            voiceParam.resultMap = resultMap


            val logMap = MapUtils.map<String, String>(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", voiceParam.userId,
                    "agent", voiceParam.ua,
                    "mod1", voiceUrl,
                    "mod2", voiceParam.multipartFile.size,
                    "mod3", response.statusCode,
                    "mod4", JsonUtils.toJson(voiceParam.fileInfoMap),
                    "mod5", voiceParam.ip,
                    "mod6", "hivoice",
                    "mod7", voiceParam.language,
                    "mod8", response.responseString,
                    "mod10", voiceParam.reqAppKey,
                    "op", "UsingBackgroundScore",
                    "time", System.currentTimeMillis()
            )
            if (RuntimeMode.le(Mode.TEST)) {
                logger.info(JsonUtils.toJson(logMap))
            }
            LogCollector.info("backend-general", logMap)
        } catch (e: Exception) {
            LogCollector.info(
                    "backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", voiceParam.userId,
                    "mod6", "hivoice",
                    "op", "UsingBackgroundScore Exception",
                    "mod1", e
            )
            )
        }

    }
}