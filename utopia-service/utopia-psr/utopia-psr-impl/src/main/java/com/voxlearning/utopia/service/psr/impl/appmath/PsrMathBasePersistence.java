/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.appmath;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.content.api.entity.MathBase;
import com.voxlearning.utopia.service.content.consumer.MathContentLoaderClient;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class PsrMathBasePersistence extends SpringContainerSupport {

    @Inject private MathContentLoaderClient mathContentLoaderClient;

    private Map<Long/*pointId*/, Map<Long/*eid*/, String/*pattern*/>> pointPatternMap;

    public PsrMathBasePersistence() {
        pointPatternMap = new HashMap<>();
    }

    public boolean addToMap(MathBase mathBase) {
        if (mathBase == null)
            return false;
        Map<Long, String> eids = null;
        if (!pointPatternMap.containsKey(mathBase.getPointId())) {
            eids = new HashMap<>();
            eids.put(mathBase.getId(), mathBase.getBaseDscp());
        } else {
            eids = pointPatternMap.get(mathBase.getPointId());
            if (eids == null)
                eids = new HashMap<>();
            if (!eids.containsKey(mathBase.getId()))
                eids.put(mathBase.getId(), mathBase.getBaseDscp());
        }
        pointPatternMap.put(mathBase.getPointId(), eids);
        return true;
    }

    public String findAppMathPatternByPointIdEid(String pointId, Long eid) {
        if (StringUtils.isEmpty(pointId) || eid == null)
            return null;
        return findAppMathPatternByPointIdEid(PsrTools.stringToLong(pointId), eid);
    }

    public String findAppMathPatternByPointIdEid(Long pointId, Long eid) {
        if (pointId == null)
            return null;
        if (!pointPatternMap.containsKey(pointId)) {
            // cache中没有
            List<MathBase> mathBaseList = mathContentLoaderClient.loadMathPointBases(pointId);
            if (mathBaseList == null || mathBaseList.size() <= 0)
                return null;
            for (MathBase mathBase : mathBaseList) {
                addToMap(mathBase);
            }
        }

        // 数据库中没有 该pointId的信息
        if (!pointPatternMap.containsKey(pointId))
            return null;

        // 从cache中取值
        Map<Long, String> eids = pointPatternMap.get(pointId);
        if (eids == null || eids.size() <= 0)
            return null;

        if (eids.containsKey(eid))
            return "pattern#" + eids.get(eid);
        else
            return null;
    }
}
