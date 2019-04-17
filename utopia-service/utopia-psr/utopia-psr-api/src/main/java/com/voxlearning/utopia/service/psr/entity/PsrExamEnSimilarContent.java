package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PsrExamEnSimilarContent implements Serializable {

    private static final long serialVersionUID = -8714766330895260551L;

    private String errorContent;
    private List<PsrExamEnSimilarItem> examList;

    public PsrExamEnSimilarContent() {
        examList = new ArrayList<>();
    }

    public boolean isSuccess() {
        return (errorContent.equals("success"));
    }

    public String formatList() {
        String strOut = "[ExamEnSimilar:return code:" + errorContent + "]";

        Integer count = 0;

        if (examList != null) count = examList.size();

        strOut += "[retcount:" + count.toString() + "]";

        for (int i = 0; examList != null && i < examList.size(); i++) {
            // ek,eid,et,weight,alogV
            strOut += "[ek:" + examList.get(i).getEk();
            strOut += " eid:" + examList.get(i).getEid();
            strOut += " et:" + examList.get(i).getEt();
            strOut += " weight:" + examList.get(i).getWeight();
            strOut += " alogv:" + examList.get(i).getAlogv();
            strOut += " lastDate:" + examList.get(i).getLastDate().toString();
            strOut += " sum:" + examList.get(i).getSum().toString();
            strOut += " rate:" + examList.get(i).getRate().toString();
            strOut += " similarity:" + examList.get(i).getSimilarity().toString();
            strOut += "]";
        }

        return strOut;
    }
}
