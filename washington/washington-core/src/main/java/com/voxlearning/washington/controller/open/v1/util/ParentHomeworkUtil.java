package com.voxlearning.washington.controller.open.v1.util;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import java.util.*;

/**
 * @author shiwe.liao
 * @since in 2015/12/15.
 */
public class ParentHomeworkUtil {
    //从批量获取的作业错题列表里面找出指定作业的错题数量
    public static int getWrongCountWithHomeworkId(NewHomework.Location location, Map<String, List<Map<String, Object>>> wrongQuestionIds) {
        if (location == null || MapUtils.isEmpty(wrongQuestionIds)) {
            return 0;
        }
        int wrongCount = 0;
        String date = DateUtils.dateToString(new Date(location.getCreateTime()), "yyyy.MM.dd");
        List<Map<String, Object>> mapList = wrongQuestionIds.get(date);
        if (CollectionUtils.isNotEmpty(mapList)) {
            Map<String, Object> doLocationWrongQuestionIds = mapList.stream().filter(p -> location.getId().equals(p.get("homeworkId"))).findFirst().orElse(Collections.emptyMap());
            if (MapUtils.isNotEmpty(doLocationWrongQuestionIds)) {
                wrongCount = ((Set) doLocationWrongQuestionIds.get("qid")).size();
            }
        }
        return wrongCount;
    }

}
