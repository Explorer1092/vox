package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamPaper;

/**
 * @author chunlin.yu
 * @create 2018-03-14 19:18
 **/
public class AgentExamPaperVO extends AgentExamPaper {

    private static final long serialVersionUID = -1173310523611206103L;

    /**
     * 科目中文描述
     * @return
     */
    public String getSubjectDes(){
        if (getSubject() != null){
            return getSubject().getValue();
        }
        return null;
    }


    public static AgentExamPaperVO fromAgentExamPaper(AgentExamPaper examPaper) {
        if (null == examPaper) {
            return null;
        }

        AgentExamPaperVO paperVO = new AgentExamPaperVO();
        try {
            BeanUtils.copyProperties(paperVO,examPaper);
        } catch (Exception e) {
            return null;
        }
        return paperVO;
    }


    public AgentExamPaper toAgentExamPaper(){
        AgentExamPaper examPaper = new AgentExamPaper();
        try {
            BeanUtils.copyProperties(examPaper,this);
        } catch (Exception e) {
            return null;
        }
        return examPaper;
    }
}
