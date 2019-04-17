package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamGrade;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 大考
 *
 * @author chunlin.yu
 * @create 2018-04-18 21:24
 **/

public class AgentExamGradeVO extends AgentExamGrade {
    private static final long serialVersionUID = -7312271568926618747L;


    /**
     * 大考3科学生数
     */
    @Getter
    @Setter
    private long bgExamGte3StuCount;

    /**
     * 大考6科学生数
     */
    @Getter
    @Setter
    private long bgExamGte6StuCount;

    @Getter
    @Setter
    private AgentGradeDetails agentGradeDetails;

    /**
     * 获取年级名字
     *
     * @return
     */
    public String getGradeDes() {
        if (null != getGrade()) {
            ClazzLevel clazzLevel = ClazzLevel.parse(getGrade());
            if (clazzLevel != null) {
                return clazzLevel.getDescription();
            }
        }
        return null;
    }

    /**
     * 科目数
     */
    public int getSubjectCount(){
        if (CollectionUtils.isNotEmpty(examSubjectVOList)){
            return examSubjectVOList.size();
        }
        return 0;
    }


    /**
     * 大考学科列表
     */
    @Getter
    @Setter
    List<AgentExamSubjectVO> examSubjectVOList;

    public static AgentExamGradeVO fromAgentExamGrade(AgentExamGrade agentExamGrade) {
        if (null == agentExamGrade) {
            return null;
        }
        AgentExamGradeVO vo = new AgentExamGradeVO();
        try {
            BeanUtils.copyProperties(vo, agentExamGrade);
        } catch (Exception e) {
            return null;
        }
        return vo;
    }

    public AgentExamGrade toAgentExamGrade() {
        AgentExamGrade agentExamGrade = new AgentExamGrade();
        try {
            BeanUtils.copyProperties(agentExamGrade, this);
        } catch (Exception e) {
            return null;
        }
        return agentExamGrade;
    }
}
