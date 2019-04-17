package com.voxlearning.utopia.agent.bean.workrecord;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chunlin.yu
 * @create 2018-01-23 17:41
 **/
@Getter
@Setter
public class WorkRecordStatisticsItem {

    public WorkRecordStatisticsItem(Long id,Integer idType,String name){
        setId(id);
        setIdType(idType);
        setName(name);
    }

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 数据类型 1:部门，2：个人
     */
    private Integer idType;

    /**
     * 显示名称
     */
    private String name;
}
