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

package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.zone.api.ZoneLoader;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Deprecated
public class ZoneLoaderClient {

    @Getter
    @ImportService(interfaceClass = ZoneLoader.class)
    private ZoneLoader zoneLoader;

    /**
     * 整体作业结果页面显示土豪榜
     * 排行：土豪榜用户，及其前后各2名的头像、姓名、学豆。若排名之前或之后没有人，就显示剩下的。
     */
    public List<Map<String, Object>> complementHomeworkShowSilverRank(List<Map<String, Object>> ranks, Long userId) {
        List<Map<String, Object>> rankList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ranks)) {
            return rankList;
        }

        int j = 1;
        for (Map<String, Object> rank : ranks) {
            rank.put("rank", j);
            j++;
        }

        int myRank = 0;
        for (int i = 0; i < ranks.size(); i++) {
            Long studentId = SafeConverter.toLong(ranks.get(i).get("studentId"));
            myRank++;
            if (userId.equals(studentId)) break;
        }

        if (myRank == 1) {
            if (ranks.size() > 2) {
                rankList = ranks.subList(0, 2);
            } else {
                rankList = ranks;
            }
        } else if (myRank == ranks.size()) {
            rankList = ranks.subList(ranks.size() - 2, ranks.size());
        } else {
            rankList = ranks.subList(myRank - 2, myRank + 1);
        }

        return rankList;
    }
}
