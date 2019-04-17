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

package com.voxlearning.utopia.service.psr.impl.dao.newhomework;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.utopia.service.psr.entity.newhomework.QuestionSimilarityProfile;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/8/1
 * Time: 19:55
 * To change this template use File | Settings | File Templates.
 */
@Named
@UtopiaCacheSupport(QuestionSimilarityProfile.class)

public class QuestionSimilarityProfileDao extends StaticMongoDao<QuestionSimilarityProfile, String> {

    @Override
    protected void calculateCacheDimensions(QuestionSimilarityProfile source, Collection<String> dimensions) {

    }

    /**
     * 根据题包ID和题包里面题目的ID去查找配置的类题
     * @param boxId
     * @param questionIds
     * @return
     */
    public Map<String, List<QuestionSimilarityProfile>> loadQuestionSimilarityProfileOfBox(String boxId, Collection<String> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyMap();
        }
        Filter filter = filterBuilder.build()
                .and("pak_id").is(boxId)
                .and("question_id").in(questionIds)
                .and("deleted_at").exists(false);
        return __find_OTF(Find.find(filter), ReadPreference.primary()).stream().collect(Collectors.groupingBy(QuestionSimilarityProfile::getDoc_id));
    }

}
