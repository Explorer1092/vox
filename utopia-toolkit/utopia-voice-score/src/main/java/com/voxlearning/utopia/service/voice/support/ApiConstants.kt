package com.voxlearning.utopia.service.voice.support

object ApiConstants {
    const val REQ_FILE = "file"
    const val REQ_FILE_INFO = "file_info"

    const val REQ_APP_NATIVE_VERSION = "ver"
    const val REQ_CHANNEL = "channel"
    const val REQ_SYS = "sys"
    const val REQ_UUID = "uuid"
    const val REQ_SIG = "sig"
    const val REQ_APP_KEY = "app_key"
    const val REQ_SESSION_KEY = "session_key"

    const val RES_RESULT_BAD_REQUEST_CODE = "400"
    const val RES_RESULT_NEED_RELOGIN_CODE = "900";//先加上这个字段，app端做预处理。如果出现则跳回登录页。

    const val RES_RESULT_DUPLICATE_OPERATION = "操作进行中，请不要重复操作!"
    const val RES_RESULT_USER_ACCOUNT_NOT_EXIST_MSG = "账号不存在，请核对后重新输入，或者注册成为新用户"
    const val RES_RESULT_DATA_ERROR_MSG = "请求的数据错误，请联系系统管理员解决此问题"
    const val RES_RESULT_APP_ERROR_MSG = "请求的应用状态错误，请联系系统管理员解决此问题!"
    const val RES_RESULT_BAD_REQUEST_MSG = "无效的HTTP请求!"
    const val RES_RESULT_USER_ERROR_MSG = "请求的用户状态错误，请联系系统管员解决此问题!"
    const val RES_RESULT_SESSION_KEY_EXPIRED_MSG = "登录信息已过期，请重新登录!"
    const val RES_RESULT_SCORE_IDS_ERROR = "打分结果错误";

    const val RES_RESULT = "result"
    const val RES_RESULT_SUCCESS = "success"
    const val RES_MESSAGE = "message"

    const val RES_VOICE_SCORE_INFO = "voice_score"
    const val RES_SUBJECTIVE_UPLOAD_FAIL_MSG = "上传失败！"
}