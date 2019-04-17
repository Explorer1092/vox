/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.luffy.controller;

/**
 * Open Platform API Constants CLass.
 *
 * @author Zhilong Hu
 * @since 2014-06-6
 */
public class ApiConstants {

    // API request parameter key definitions
    public static final String REQ_STUDENT_ID = "sid";
    public static final String RES_CLAZZ_LEVEL = "clazz_level";
    public static final String RES_CLAZZ_LEVEL_NAME = "clazz_level_name";
    public static final String RES_PURCHASE_URL = "purchase_url";

    // Response Result Code definitions
    public static final String RES_RESULT_NO_SESSION_KEY_CODE = "800";
    public static final String RES_RESULT_NO_OPEN_ID_CODE = "801";

    public static final String RES_RESULT_NEED_RELOGIN_CODE = "900";//先加上这个字段，app端做预处理。如果出现则跳回登录页。
    public static final String RES_RESULT_DECODE_FAILED_CODE = "901";//解密失败

    public static final String RES_RESULT_UNIT_ERROR_MSG = "单元ID错误!";

    public static final String RES_USER_BOOK = "user_books";
    public static final String RES_PURCHASE_TEXT = "purchase_text";

    public static final String REQ_APP_NATIVE_VERSION = "ver";

}
