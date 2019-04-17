package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 拜访教研员的目的
 * Created by yaguang.wang on 2016/10/20.
 */
@Getter
@AllArgsConstructor
public enum VisitedResearchersIntention {
    MEET_FIRST(1, "首次接洽"),
    MAINTAINING_CLIENT(2, "客户维护"),
    PROMOTE_MEETING(3, "促进组会"),
    ASK_INTRODUCE(4, "寻求介绍");

    private final int intention;
    private final String describe;

    private static final Map<Integer, VisitedResearchersIntention> intentionMap;

    static {
        intentionMap = new HashMap<>();
        for (VisitedResearchersIntention type : VisitedResearchersIntention.values()) {
            intentionMap.put(type.getIntention(), type);
        }
    }

    public static VisitedResearchersIntention typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return VisitedResearchersIntention.intentionMap.get(id);
    }
}
