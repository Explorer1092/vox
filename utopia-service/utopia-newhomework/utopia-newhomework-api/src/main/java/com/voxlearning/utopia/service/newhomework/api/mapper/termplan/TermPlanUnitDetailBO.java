package com.voxlearning.utopia.service.newhomework.api.mapper.termplan;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2018/3/6
 */

@Setter
@Getter
public class TermPlanUnitDetailBO implements Serializable {
    private static final long serialVersionUID = -6852021405902856756L;

    private String unitId;
    private String unitName;
    private boolean selected;
    private Integer doneKnowledgePointNum;
    private Integer totalKnowledgePointNum;
}
