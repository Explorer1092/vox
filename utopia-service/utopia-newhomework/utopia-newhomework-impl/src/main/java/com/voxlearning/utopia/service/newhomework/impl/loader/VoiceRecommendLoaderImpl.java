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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.VoiceRecommendLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendData;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendDataSentenceRef;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.impl.dao.VoiceRecommendDataPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.VoiceRecommendDataSentenceRefPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.VoiceRecommendDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/5/31
 */
@Named
@Service(interfaceClass = VoiceRecommendLoader.class)
@ExposeService(interfaceClass = VoiceRecommendLoader.class)
public class VoiceRecommendLoaderImpl implements VoiceRecommendLoader {

    @Inject private VoiceRecommendDao voiceRecommendDao;
    @Inject private VoiceRecommendDataPersistence voiceRecommendDataPersistence;
    @Inject private VoiceRecommendDataSentenceRefPersistence voiceRecommendDataSentenceRefPersistence;

    @Override
    public Map<String, VoiceRecommend> loadByHomeworkIds(Collection<String> homeworkIds) {
        return voiceRecommendDao.loads(homeworkIds);
    }

    @Override
    public Map<Long, VoiceRecommendData> findVoiceRecommendDataBySentenceIds(Collection<Long> sentenceIds) {
        if (CollectionUtils.isEmpty(sentenceIds)) {
            return Collections.emptyMap();
        }
        // SentenceVoiceRecommendRef的recommendIds存的是逗号分隔的推荐id
        Map<Long, VoiceRecommendDataSentenceRef> map = voiceRecommendDataSentenceRefPersistence.loads(sentenceIds);
        if (MapUtils.isEmpty(map)) {
            return Collections.emptyMap();
        }

        Map<Long, VoiceRecommendData> result = new HashMap<>();
        for (Map.Entry<Long, VoiceRecommendDataSentenceRef> entry : map.entrySet()) {
            Long key = entry.getKey();
            String recommendIdStr = entry.getValue().getRecommendIds();
            if (StringUtils.isNotBlank(recommendIdStr) && (StringUtils.split(recommendIdStr, ",") != null && StringUtils.split(recommendIdStr, ",").length > 0)) {
                List<String> recommendIds = Arrays.asList(StringUtils.split(recommendIdStr, ","));
                if (CollectionUtils.isNotEmpty(recommendIds)) {
                    Set<Long> rids = recommendIds.stream().map(SafeConverter::toLong).collect(Collectors.toSet());
                    VoiceRecommendData value = voiceRecommendDataPersistence.loads(rids).values().stream().filter(o -> !Objects.equals(Boolean.TRUE, o.getDisabled())).findFirst().orElse(null);
                    if (value != null) {
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }
}
