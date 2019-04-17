package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校月活数据
 *
 * @author chunlin.yu
 * @create 2017-11-09 15:35
 **/
@Getter
@Setter
public class SchoolMauDetail {

    /**
     * 学校ID
     */
    private Long schoolId;


    /**
     * 月活数量
     */
    private int mauCount;

    /**
     * 上月月活
     */
    private int lmMauCount;
}
