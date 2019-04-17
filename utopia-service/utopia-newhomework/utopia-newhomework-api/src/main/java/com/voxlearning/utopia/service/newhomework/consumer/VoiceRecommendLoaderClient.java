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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newhomework.api.VoiceRecommendLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendData;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/5/31
 */
public class VoiceRecommendLoaderClient implements VoiceRecommendLoader {

    @ImportService(interfaceClass = VoiceRecommendLoader.class)
    private VoiceRecommendLoader remoteReference;

    @Override
    public Map<String, VoiceRecommend> loadByHomeworkIds(Collection<String> homeworkIds) {
        return remoteReference.loadByHomeworkIds(homeworkIds);
    }

    public List<VoiceRecommend> loadExcludeNoRecommend(Collection<String> homeworkIds) {
        return loadByHomeworkIds(homeworkIds).values()
                .stream()
                .filter(VoiceRecommend::hasRecommend)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, VoiceRecommendData> findVoiceRecommendDataBySentenceIds(Collection<Long> sentenceIds) {
        if (CollectionUtils.isEmpty(sentenceIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.findVoiceRecommendDataBySentenceIds(sentenceIds);
    }
}
