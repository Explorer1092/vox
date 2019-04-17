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

package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendData;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guoqiang.li
 * @since 2016/5/31
 */
@ServiceVersion(version = "20180331")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface VoiceRecommendLoader extends IPingable {
    Map<String, VoiceRecommend> loadByHomeworkIds(Collection<String> homeworkIds);

    /**
     * 根据sentenceIds获取相应的推荐语音，如果未查到就不返回<br />
     * sentenceIds的大小和返回Map的大小不一定一致
     * by xuesong.zhang
     *
     * @param sentenceIds 句子id
     * @return map
     */
    @Idempotent
    @CacheMethod(
            type = VoiceRecommendData.class
    )
    Map<Long, VoiceRecommendData> findVoiceRecommendDataBySentenceIds(@CacheParameter(multiple = true) Collection<Long> sentenceIds);
}
