package com.voxlearning.utopia.service.campaign.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicRankingHelper {

    public static void calcDynamicRank(List<Map<String, Object>> current, List<Map<String, Object>> prev, String fieldName) {
        if (CollectionUtils.isEmpty(prev)) {
            for (int i = 0; i < current.size(); i++) {
                Map<String, Object> item = current.get(i);
                item.put("dynamicRank", "");
            }
            return;
        }

        Map<String, Integer> prevRankData = new HashMap<>();
        for (int i = 0; i < prev.size(); i ++) {
            prevRankData.put(SafeConverter.toString(prev.get(i).get(fieldName)), i);
        }

        for (int i = 0; i < current.size(); i++) {
            Map<String, Object> item = current.get(i);
            String key = SafeConverter.toString(item.get(fieldName));
            if (prevRankData.containsKey(key)) {
                int prevRank = prevRankData.get(key);
                if (i > prevRank) {
                    item.put("dynamicRank", Dynamic.DOWN.name());
                } else if (i < prevRank) {
                    item.put("dynamicRank", Dynamic.UP.name());
                } else {
                    item.put("dynamicRank", Dynamic.FLAT.name());
                }

            } else {
                item.put("dynamicRank", Dynamic.NEW_COURSE.name());
            }
        }
    }

    @AllArgsConstructor
    public enum Dynamic {
        UP("上升"),
        DOWN("下降"),
        FLAT("不变"),
        NEW_COURSE("新增");
        public String desc;
    }
}
