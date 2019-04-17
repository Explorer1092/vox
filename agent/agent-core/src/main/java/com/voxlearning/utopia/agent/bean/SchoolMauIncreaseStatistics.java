package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 学校月活增长封装
 *
 * @author chunlin.yu
 * @create 2017-11-09 15:26
 **/
@Getter
@Setter
public class SchoolMauIncreaseStatistics implements Serializable{


    private static final long serialVersionUID = -5464877439054033345L;
    private Long id;

    private IdType idType;

    /**
     * 显示名称
     */
    private String name;

    /**
     * 总学校数
     */
    private int allSchoolCount;

    /**
     * 增长数
     */
    private int increaseSchoolCount;



    public enum IdType{
        USER,
        GROUP,
        GROUP_CITY,
        OTHER_SCHOOL;
    }
}
