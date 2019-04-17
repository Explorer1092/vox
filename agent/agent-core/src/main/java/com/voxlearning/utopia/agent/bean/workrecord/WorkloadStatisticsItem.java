package com.voxlearning.utopia.agent.bean.workrecord;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chunlin.yu
 * @create 2018-01-23 16:55
 **/
@Setter
@Getter
public class WorkloadStatisticsItem extends WorkRecordStatisticsItem{

    public WorkloadStatisticsItem(Long id,Integer idType,String name,Double workload){
        super(id,idType,name);
        setWorkload(workload);
    }

    /**
     * 工作量
     */
    private Double workload;

}
