package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.utopia.service.user.api.entities.ArtScienceType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 大考统计
 *
 * @author chunlin.yu
 * @create 2018-03-14 20:03
 **/
@Getter
@Setter
public class ExamStatistics implements Serializable {

    private static final long serialVersionUID = -450900704636109072L;

    private ArtScienceType artScienceType;

    /**
     * 学生渗透率
     */
    private Double stuPermeability;

    /**
     * 学生渗透率是否符合
     */
    private Boolean meetStuPermeability;

    /**
     * 班级渗透率
     */
    private Double groupPermeability;

    /**
     * 班级渗透率是否符合
     */
    private Boolean meetGroupPermeability;

    /**
     * 参考人数
     */
    private Long participateCount;

    /**
     * 参考人数参考人数是否符合
     */
    private Boolean meetParticipateCount;

    /**
     * 班组最小完成率
     */
    private Double groupMinCompletionRate;

    /**
     * 班组最小完成率是否符合
     */
    private Boolean meetGroupMinCompletionRate;


}
