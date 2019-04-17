package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamSubject;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentKlxScanPaper;

import java.lang.reflect.InvocationTargetException;

/**
 * @author chunlin.yu
 * @create 2018-03-15 20:06
 **/
public class AgentKlxScanPaperVO extends AgentKlxScanPaper {
    private static final long serialVersionUID = 1249620692438058509L;

    /**
     * 科目中文描述
     * @return
     */
    public String getPaperSubjectDes(){
        if (getPaperSubject() != null){
            Subject subject = Subject.safeParse(getPaperSubject());
            if (null != subject){
                return subject.getValue();
            }
        }
        return null;
    }

    public static AgentKlxScanPaperVO fromAgentKlxScanPaper(AgentKlxScanPaper paper) {
        if (paper == null) {
            return null;
        }
        AgentKlxScanPaperVO vo = new AgentKlxScanPaperVO();
        try {
            BeanUtils.copyProperties(vo,paper);
        } catch (Exception e) {
            return null;
        }
        return vo;
    }
}
