package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/7/30
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkloadRankBean implements Serializable {
    private static final long serialVersionUID = 640461666658010680L;

    private WorkloadRankerBean top1;
    private WorkloadRankerBean top2;
    private WorkloadRankerBean top3;
}
