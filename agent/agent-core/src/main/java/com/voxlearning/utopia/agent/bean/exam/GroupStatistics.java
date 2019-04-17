package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.utopia.service.user.api.entities.ArtScienceType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 班组统计
 * @author chunlin.yu
 * @create 2018-03-14 20:06
 **/
@Getter
@Setter
public class GroupStatistics implements Serializable{
    private static final long serialVersionUID = -5245038922707347931L;

    /**
     * 班组ID
     */
    private Long groupId;

    /**
     * 班级名字
     */
    private String clazzName;

    private ArtScienceType artScienceType;

    /**
     * 班级人数
     */
    private Long stuCount;

    /**
     * 参考人数
     */
    private Long participateCount;

    /**
     * 渗透率
     */
    private Double permeability;

}
