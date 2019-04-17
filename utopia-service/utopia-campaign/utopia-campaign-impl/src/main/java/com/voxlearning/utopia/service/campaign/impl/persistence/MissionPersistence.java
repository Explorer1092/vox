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
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.api.constant.MissionState;
import com.voxlearning.utopia.entity.mission.Mission;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.service.campaign.impl.persistence.MissionPersistence")
@CacheBean(type = Mission.class)
public class MissionPersistence extends StaticMySQLPersistence<Mission, Long> {

    @Override
    protected void calculateCacheDimensions(Mission document, Collection<String> dimensions) {
        dimensions.add(Mission.ck_id(document.getId()));
        dimensions.add(Mission.ck_studentId(document.getStudentId()));
    }

    @CacheMethod
    public Set<Mission.Location> queryLocations(@CacheParameter("S") Long studentId) {
        Criteria criteria = Criteria.where("STUDENT_ID").is(studentId);
        Query query = Query.query(criteria);
        query.field().includes("ID", "CREATE_DATETIME", "STUDENT_ID", "MISSION_STATE", "MISSION_DATETIME");
        return query(query).stream()
                .map(Mission::toLocation)
                .collect(Collectors.toSet());
    }

    @CacheMethod
    public Map<Long, Set<Mission.Location>> queryLocations(@CacheParameter(value = "S", multiple = true) Collection<Long> studentIds) {
        Criteria criteria = Criteria.where("STUDENT_ID").in(studentIds);
        Query query = Query.query(criteria);
        query.field().includes("ID", "CREATE_DATETIME", "STUDENT_ID", "MISSION_STATE", "MISSION_DATETIME");
        Map<Long, Set<Mission.Location>> map = query(query).stream()
                .map(Mission::toLocation)
                .collect(Collectors.groupingBy(Mission.Location::getStudentId, Collectors.toSet()));
        return MapUtils.resort(map, studentIds, Collections.emptySet());
    }

    public boolean increaseFinishCount(Long id, int delta) {
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = new Update().inc("FINISH_COUNT", delta);
        long rows = $update(update, criteria);
        if (rows > 0) {
            getCache().delete(Mission.ck_id(id));
        }
        return rows > 0;
    }

    public boolean updateComplete(Long id) {
        Mission original = $load(id);
        if (original == null || original.getMissionState() == MissionState.COMPLETE) {
            return false;
        }
        Criteria criteria = Criteria.where("ID").is(id).and("MISSION_STATE").ne(MissionState.COMPLETE);
        Update update = Update.update("MISSION_STATE", MissionState.COMPLETE).currentDate("COMPLETE_DATETIME");
        long rows = $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(original);
        }
        return rows > 0;
    }

    public boolean updateImg(Long id, String img) {
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("IMG", img);
        long rows = $update(update, criteria);
        if (rows > 0) {
            getCache().delete(Mission.ck_id(id));
        }
        return rows > 0;
    }
}
