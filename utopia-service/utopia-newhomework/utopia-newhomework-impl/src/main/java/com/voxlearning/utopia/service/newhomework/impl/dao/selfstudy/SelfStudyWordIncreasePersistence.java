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

package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyWordIncrease;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 这个表的数据由大数据每周五写一次，然后定时任务跑一次生成作业
 * 好像缓存也没什么用
 *
 * @author xuesong.zhang
 * @since 2017/2/15
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyWordIncreasePersistence")
public class SelfStudyWordIncreasePersistence extends NoCacheStaticMySQLPersistence<SelfStudyWordIncrease, Long> {

    public Map<Long, List<SelfStudyWordIncrease>> findAllByClazzGroupId() {
        List<SelfStudyWordIncrease> list = query();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(o -> (Objects.nonNull(o.getSubject()) && Objects.nonNull(o.getClazzGroupId()) && StringUtils.isNoneBlank(o.getBookId(), o.getUnitId(), o.getKnowledgePointId())))
                .collect(Collectors.groupingBy(SelfStudyWordIncrease::getClazzGroupId));
    }


}
