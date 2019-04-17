package com.voxlearning.utopia.service.afenti.impl.service.processor.invitation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.LoadInvitationMsgContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserFootprint;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 开通同学列表信息
 *
 * @author peng.zhang.a
 * @since 16-7-20
 */
@Named
public class LIM_GenerateUsingUserList extends SpringContainerSupport implements IAfentiTask<LoadInvitationMsgContext> {
    @Inject AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(LoadInvitationMsgContext context) {
        List<Long> userIds = new ArrayList<>(context.getUsingUserMap().keySet());

        // 获取用户信息列表
        Map<Long, AfentiLearningPlanUserFootprint> lastestLearningTimeMap = afentiLoader
                .loadAfentiLearningPlanUserFootprintByUserIdsAndSubject(userIds, context.getSubject());

        context.getUsingUserMap().forEach((userId, userMsg) -> {
            Date lastLearningTime = lastestLearningTimeMap.containsKey(userId)
                    ? lastestLearningTimeMap.get(userId).getUpdateTime()
                    : new Date();
            Integer starNum = afentiLoader.loadUserTotalStar(userId, context.getSubject());
            userMsg.put("starNum", starNum);
            userMsg.put("lastLearningTime", lastLearningTime);
        });

        // 按照星星排序
        List<Map<String, Object>> usingUserList = context.getUsingUserMap().values()
                .stream()
                .sorted((p1, p2) -> Integer.compare((int) p2.get("starNum"), (int) p1.get("starNum")))
                .collect(Collectors.toList());

        // 增加rank值
        int index = 1;
        int rank = 1;
        int lastStarNum = Integer.MIN_VALUE;
        for (Map<String, Object> map : usingUserList) {
            int starNum = (int) map.get("starNum");
            if (starNum < lastStarNum) {
                rank = index;
            }
            map.put("rank", rank);
            map.put("userId", String.valueOf(map.getOrDefault("userId", 0L)));
            lastStarNum = starNum;
            index++;
        }

        context.getResult().put("usingUserList", usingUserList);
    }
}
