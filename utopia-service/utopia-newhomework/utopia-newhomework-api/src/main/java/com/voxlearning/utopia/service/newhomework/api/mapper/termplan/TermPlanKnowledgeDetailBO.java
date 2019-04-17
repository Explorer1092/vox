package com.voxlearning.utopia.service.newhomework.api.mapper.termplan;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2018/3/7
 */

@Setter
@Getter
public class TermPlanKnowledgeDetailBO implements Serializable {
    private static final long serialVersionUID = -8694412777403554665L;

    private String category;
    private List<KnowledgeRightRate> knowledgeRightRates;

    @Setter
    @Getter
    public static class KnowledgeRightRate implements Serializable {
        private static final long serialVersionUID = -6885858189995572548L;

        private String knowledgePointId;
        private String knowledgePointName;
        private Integer knowledgeRightRate;
    }
}
