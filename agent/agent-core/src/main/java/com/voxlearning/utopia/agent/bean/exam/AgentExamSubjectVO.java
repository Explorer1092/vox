package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamSubject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 大考科目
 *
 * @author chunlin.yu
 * @create 2018-03-14 19:07
 **/
public class AgentExamSubjectVO extends AgentExamSubject {


    private static final long serialVersionUID = 6869780509459169422L;


    /**
     * 科目中文描述
     *
     * @return
     */
    public String getSubjectDes() {
        if (getSubject() != null) {
            return getSubject().getValue();
        }
        return null;
    }

    /**
     * 试卷数量
     */
    @Setter
    @Getter
    private int paperCount;

    /**
     * 大考参考学生数
     */
    public Integer getParticipateCount(){
        List<ExamStatistics> examStatistics = getExamStatistics();
        if (CollectionUtils.isNotEmpty(examStatistics)){
            int participateCount = 0;
            for (int i = 0; i < examStatistics.size(); i++) {
                participateCount +=examStatistics.get(i).getParticipateCount();
            }
            return participateCount;
        }
        return 0;
    }

    /**
     * 是否满足大考要求
     */
    public Boolean getMeetExamRequirement() {
        if (MapUtils.isNotEmpty(examRequirement)) {
            for (String key : examRequirement.keySet()) {
                Boolean aBoolean = examRequirement.get(key);
                if (Objects.equals(aBoolean, false)) {
                    return false;
                }
            }
        }else {
            return false;
        }
        return true;
    }

    /**
     * 大考要求满足情况
     */

    @Setter
    @Getter
    private Map<String, Boolean> examRequirement;

    /**
     * 大考统计
     */

    @Setter
    @Getter
    private List<ExamStatistics> examStatistics;

    /**
     * 班组统计
     */

    @Setter
    @Getter
    private List<GroupStatistics> groupStatistics;

    /**
     * 大考对应试卷列表
     */
    @Setter
    @Getter
    private List<AgentExamPaperVO> examPaperVOList;

    public static AgentExamSubjectVO fromAgentExamSubject(AgentExamSubject examSubject) {
        if (examSubject == null) {
            return null;
        }
        AgentExamSubjectVO subjectVO = new AgentExamSubjectVO();
        try {
            BeanUtils.copyProperties(subjectVO, examSubject);
        } catch (Exception e) {
            return null;
        }
        return subjectVO;
    }


    public AgentExamSubject toAgentExamSubject() {
        AgentExamSubject examSubject = new AgentExamSubject();
        try {
            BeanUtils.copyProperties(examSubject, this);
        } catch (Exception e) {
            return null;
        }
        return examSubject;
    }
}
