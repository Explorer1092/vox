/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller;

import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.washington.support.AbstractController;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;

/**
 * 七鱼客服 Controller
 * Created by haitian.gan on 2017/6/16.
 */
@Controller
@RequestMapping("/qiyukftest")
public class QiYuKeFuController extends AbstractController {

    private static final String TOKEN = "17zy_kf_token";
    private static final int TOKEN_EXPIRE_SECONDS = 7200;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    /**
     * 接口规范中，token的状态码枚举
     */
    @AllArgsConstructor
    private enum RLT {
        SUCCESS(0, "成功"),
        FAILED(1, "失败"),
        EXPIRED(2, "失效");

        private int code;
        private String desc;
    }


    /**
     * 获得AppId的配置信息
     *
     * @return
     */
    private String getAppId() {
        return commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(),
                "kefu_qiyu_appid");
    }

    /**
     * 获得AppSecret的配置信息
     *
     * @return
     */
    private String getAppSecret() {
        return commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(),
                "kefu_qifu_app_secret");
    }

    // @TODO 如果放缓存里面被flush掉，七鱼的请求过来就全部过期了。要不要放mongo或者aerospike?
    private String getCurrentToken() {
        return UserCache.getUserCache().get(TOKEN).getValue().toString();
    }


}
