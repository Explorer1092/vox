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

package com.voxlearning.utopia.service.reward.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.reward.api.AsyncRewardShortUrlService;

import javax.inject.Named;
import java.util.Map;

@Named("com.voxlearning.utopia.service.reward.impl.service.AsyncRewardShortUrlServiceImpl")
@ExposeService(interfaceClass = AsyncRewardShortUrlService.class)
public class AsyncRewardShortUrlServiceImpl extends SpringContainerSupport implements AsyncRewardShortUrlService {

    @Override
    public AlpsFuture<String> dwzTinyUrl(String longUrl) {
        String responseStr = HttpRequestExecutor.defaultInstance()
                .post("http://dwz.cn/create.php")
                .addParameter("url", longUrl)
                .execute()
                .getResponseString();
        Map<String, Object> map = JsonUtils.fromJson(responseStr);
        if (map == null || Integer.parseInt(map.get("status").toString()) != 0) {
            return new ValueWrapperFuture<>(null);
        } else {
            return new ValueWrapperFuture<>(map.get("tinyurl").toString());
        }
    }

    @Override
    public AlpsFuture<String> i7TinyUrl(String longUrl) {
        String responseStr = HttpRequestExecutor.defaultInstance()
                .post("http://www.17zyw.cn/crt")
                .addParameter("url", longUrl)
                .execute()
                .getResponseString();
        if (StringUtils.isNotBlank(responseStr)) {
            return new ValueWrapperFuture<>("http://www.17zyw.cn/" + responseStr);
        } else {
            return new ValueWrapperFuture<>(longUrl);
        }
    }
}
