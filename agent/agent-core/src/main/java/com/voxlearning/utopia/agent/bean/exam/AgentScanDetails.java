package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.athena.bean.bigexam.ScanDetails;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author chunlin.yu
 * @create 2018-04-19 22:27
 **/

public class AgentScanDetails implements Serializable{
    private static final long serialVersionUID = -3337738206489345672L;

    @Getter
    @Setter
    private ScanDetails scanDetails;

    @Getter
    @Setter
    private String paperName;

    public String getSubjectDes(){
        if (scanDetails != null){
            String subjectStr = scanDetails.getSubject();
            Subject subject = Subject.safeParse(subjectStr);
            if (null != subject){
                return subject.getValue();
            }
        }
        return null;
    }
}
