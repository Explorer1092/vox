package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Data
public class PsrExamEnSimilarContentEx implements Serializable {

    private static final long serialVersionUID = -8714766330895260551L;

    private String errorContent;
    private Map<String/*qid*/,List<PsrExamEnSimilarItemEx>> similarMap;

    public PsrExamEnSimilarContentEx() {
        similarMap = new HashMap<>();
    }

    public boolean isSuccess() {
        return (errorContent.equals("success"));
    }

    public String formatList() {
        String strOut = "[ExamEnSimilarEx:return code:" + errorContent + "]";

        Integer count = 0;

        if (similarMap != null) count = similarMap.size();

        strOut += "[retcount:" + count.toString() + "]";

        for (Map.Entry<String, List<PsrExamEnSimilarItemEx>> entry : similarMap.entrySet()) {
            for (PsrExamEnSimilarItemEx item : entry.getValue()) {
                strOut += "[eid:" + item.getEid();
                strOut += " similarity:" + item.getSimilarity();
                strOut += "]";
            }
        }

        return strOut;
    }
}
