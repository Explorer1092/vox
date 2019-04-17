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

package com.voxlearning.utopia.service.campaign.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.mission.MissionProgress;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * {@link MissionProgress} persistence implementation.
 *
 * @author RuiBao
 * @version 0.1
 * @since 1/9/2015
 */
@Named("com.voxlearning.utopia.service.campaign.impl.persistence.MissionProgressPersistence")
@CacheBean(type = MissionProgress.class)
public class MissionProgressPersistence extends StaticMySQLPersistence<MissionProgress, Long> {

    @Override
    protected void calculateCacheDimensions(MissionProgress document, Collection<String> dimensions) {
        dimensions.add(MissionProgress.ck_missionId(document.getMissionId()));
    }

    @CacheMethod
    public List<MissionProgress> findByMissionId(@CacheParameter("missionId") Long missionId) {
        Criteria criteria = Criteria.where("MISSION_ID").is(missionId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }
}
