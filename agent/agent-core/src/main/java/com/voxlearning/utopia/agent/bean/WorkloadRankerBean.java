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
public class WorkloadRankerBean implements Comparable, Serializable {
    private static final long serialVersionUID = -1008422888166977511L;

    private String ranker;
    private int workload;

    public WorkloadRankerBean(String ranker) {
        this.ranker = ranker;
    }

    public void increase(int workload) {
        this.workload += workload;
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof WorkloadRankerBean)) {
            return -1;
        }
        WorkloadRankerBean bean = (WorkloadRankerBean) other;
        return bean.workload - this.workload;
    }
}
