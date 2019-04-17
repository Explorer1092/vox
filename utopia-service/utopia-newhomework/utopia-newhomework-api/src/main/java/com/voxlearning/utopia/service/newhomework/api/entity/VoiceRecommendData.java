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

package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 推荐音频
 *
 * @author xuesong.zhang
 * @since 2016-06-01
 */
@Getter
@Setter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_VOICE_RECOMMEND_DATA")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20160601")
public class VoiceRecommendData extends LongIdEntityWithDisabledField {
    private static final long serialVersionUID = -6838207308804623633L;

    @DocumentField("STUDENT_ID") private Long studentId;
    @DocumentField("OLD_VOICE_URL") private String oldVoiceUrl;
    @DocumentField("VOICE_URL") private String voiceUrl;
    @DocumentField("SENTENCE_ID") private Long sentenceId;
    @DocumentField("SENTENCE_ENTEXT") private String sentenceEntext;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(VoiceRecommendData.class, id);
    }
}
