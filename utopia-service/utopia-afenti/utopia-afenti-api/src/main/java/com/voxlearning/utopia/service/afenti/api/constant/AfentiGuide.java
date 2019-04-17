/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.afenti.api.constant;

/**
 * AFENTI guide definitions.
 *
 * @author Maofeng Lu
 * @author Xiaohai Zhang
 * @since 2013-09-22 03:32PM
 */
public enum AfentiGuide {

    AFENTI_INDEX,               // 第一次进入阿分题首页
    DO_RIGHT_QUESTIONS,         // 做对一道题，介绍钥匙和进度条
    DO_ERROR_QUESTIONS,         // 做错一道题，介绍错题因子和解析
    GET_START_REWARD,           // 第一次获得星星奖励兑换中心
    FACTOR_FACTORY,             // 第一次进入因子工厂
    AFENTI_ENGLISH_RANK_INDEX,  // 第一次英语进入排行榜
    AFENTI_MATH_RANK_INDEX,     // 第一次进入数学排行榜

    AFENTI_ENGLISH_FIRST_ENTER_ULTIMATE_RANK, // 阿分题英语第一次进入终极关卡
    AFENTI_MATH_FIRST_ENTER_ULTIMATE_RANK; // 阿分题数学第一次进入终极关卡


    public static boolean isValid(String name) {
        if (name == null) {
            return false;
        }
        try {
            AfentiGuide.valueOf(name);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
