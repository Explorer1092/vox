package com.voxlearning.utopia.service.newhomework.api.mapper.termplan;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2018/3/12
 */

@Setter
@Getter
public class KnowledgePointGraspBO implements Serializable {
    private static final long serialVersionUID = 3200339812026297380L;

    private String kpId;                            // 知识点id
    private Integer groupRightRate;                 // 班级正确率
    private Integer cityRightRate;                  // 全市平均正确率
    private Integer cityTopTenPercentRightRate;     // 全市top10%正确率

}
