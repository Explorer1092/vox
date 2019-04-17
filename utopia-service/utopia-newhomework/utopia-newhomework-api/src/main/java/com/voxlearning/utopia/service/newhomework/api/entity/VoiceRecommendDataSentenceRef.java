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
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 推荐音频和sentence的关系表
 *
 * @author xuesong.zhang
 * @since 2016-06-01
 */
@Setter
@Getter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_VOICE_RECOMMEND_DATA_SENTENCE_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20160601")
public class VoiceRecommendDataSentenceRef implements Serializable {
    private static final long serialVersionUID = -634551719424311449L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("SENTENCE_ID") private Long sentenceId;
    @DocumentField("RECOMMEND_IDS") private String recommendIds;

    public static String ck_sentenceId(Long sentenceId) {
        return CacheKeyGenerator.generateCacheKey(VoiceRecommendDataSentenceRef.class, sentenceId);
    }
}
