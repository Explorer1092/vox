package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamSchool;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学校大考信息
 *
 * @author chunlin.yu
 * @create 2018-03-14 17:07
 **/

@Setter
@Getter
public class AgentExamSchoolVO extends AgentExamSchool {


    private static final long serialVersionUID = -7415419946503232418L;

    private Long ownerId;

    private String ownerName;

    /**
     * 大考3科学生数
     */
    private Long bgExamGte3StuCount;

    /**
     * 大考6科学生数
     */
    private Long bgExamGte6StuCount;

    private String monthStr;


    /**
     * 大考列表
     */
    List<AgentExamGradeVO> examGradeVOList;

    public static AgentExamSchoolVO fromAgentExamSchool(AgentExamSchool examSchool) {
        if (null == examSchool) {
            return null;
        }
        AgentExamSchoolVO vo = new AgentExamSchoolVO();
        try {
            BeanUtils.copyProperties(vo, examSchool);
        } catch (Exception e) {
            return null;
        }
        return vo;
    }

    public AgentExamSchool toAgentExamSchool() {
        AgentExamSchool examSchool = new AgentExamSchool();
        try {
            BeanUtils.copyProperties(examSchool, this);
        } catch (Exception e) {
            return null;
        }
        return examSchool;
    }
}
