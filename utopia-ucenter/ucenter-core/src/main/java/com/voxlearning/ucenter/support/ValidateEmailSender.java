/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.ucenter.support;

import com.voxlearning.alps.cipher.DesUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.EmailRule;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.api.mapper.EmailReceiptor;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
public class ValidateEmailSender {

    @Inject private EmailServiceClient emailServiceClient;

    public MapMessage sendValidateEmail(final EmailReceiptor receiptor,
                                        final String mainSiteBaseUrl) {

        if (null == receiptor || receiptor.getUserId() == null) {
            return MapMessage.errorMessage();
        }
        // 参数验证
        if (StringUtils.isBlank(receiptor.getEmail()) || !EmailRule.isEmail(receiptor.getEmail())) {
            return MapMessage.errorMessage("请输入正确的邮箱");
        }

        Map<String, Object> validationInfo = MiscUtils.map("userId", receiptor.getUserId(), "timestamp", System.currentTimeMillis(), "email", receiptor.getEmail());
        String legacy_des_key = ConfigManager.instance().getCommonConfig().getConfigs().get("legacy_des_key");
        if (legacy_des_key == null) {
            throw new ConfigurationException("No 'legacy_des_key' configured");
        }
        String code = DesUtils.encryptHexString(legacy_des_key, JsonUtils.toJson(validationInfo));
        String link = mainSiteBaseUrl + "/active/email/active_email.vpage";
        link = UrlUtils.buildUrlQuery(link, MiscUtils.m("code", code));

        // 发送验证邮件
        Map<String, Object> content = MiscUtils.map("link", link, "name", StringUtils.defaultString(receiptor.getRealname()));

        emailServiceClient.createTemplateEmail(EmailTemplate.bindemail)
                .to(receiptor.getEmail())
                .subject("一起作业网(17zuoye.com)验证邮件")
                .content(content)
                .send();
        return MapMessage.successMessage();
    }
}
