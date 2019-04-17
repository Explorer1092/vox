/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.spr;

import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;

import java.util.LinkedHashMap;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/14/2015
 */

public class MissionCreationContext extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = -3828351919138455354L;

    private MissionCreationContext(WishType wishType, MissionType missionType) {
        put("wishType", wishType);
        put("missionType", missionType);
    }

    public static MissionCreationContext of(WishType withType, MissionType missionType) {
        return new MissionCreationContext(withType, missionType);
    }

    public WishType getWishType() {
        return (WishType) get("wishType");
    }

    public MissionType getMissionType() {
        return (MissionType) get("missionType");
    }

    public Long getStudentId() {
        return (Long) get("studentId");
    }

    public Long getParentId() {
        return (Long) get("parentId");
    }

    public Long getMissionId() {
        return (Long) get("missionId");
    }

    public Integer getIntegral() {
        return (Integer) get("integral");
    }

    public String getWish() {
        return (String) get("wish");
    }

    public Integer getTotalCount() {
        return (Integer) get("totalCount");
    }

    public String getMission() {
        return (String) get("mission");
    }

    public MissionCreationContext with(String name, Object value) {
        put(name, value);
        return this;
    }

    public MissionCreationContext withStudentId(Long studentId) {
        with("studentId", studentId);
        return this;
    }

    public MissionCreationContext withParentId(Long parentId) {
        with("parentId", parentId);
        return this;
    }

    public MissionCreationContext withMissionId(Long missionId) {
        with("missionId", missionId);
        return this;
    }

    public MissionCreationContext withIntegral(Integer integral) {
        with("integral", integral);
        return this;
    }

    public MissionCreationContext withWish(String wish) {
        with("wish", wish);
        return this;
    }

    public MissionCreationContext withTotalCount(Integer totalCount) {
        with("totalCount", totalCount);
        return this;
    }

    public MissionCreationContext withMission(String mission) {
        with("mission", mission);
        return this;
    }
}
