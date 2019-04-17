package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PsrPrimaryAppMathContent implements Serializable {

    private static final long serialVersionUID = -6795610203793744812L;

    private String errorContent;
    private List<PsrPrimaryAppMathItem> appMathList;

    public PsrPrimaryAppMathContent() {
        appMathList = new ArrayList<>();
    }

    public String formatList() {
        String strOut = "[PrimaryAppMath:return code:" + errorContent + "]";

        Integer count = 0;

        if (appMathList != null) count = appMathList.size();

        strOut += "[retcount:" + count.toString() + "]";

        for (int i = 0; appMathList != null && i < appMathList.size(); i++) {
            // eid,eType,weight,
            strOut += "[ek:" + appMathList.get(i).getEk();
            strOut += " eType:" + appMathList.get(i).getEType();
            strOut += " time:" + appMathList.get(i).getTime().toString();
            strOut += " status:" + appMathList.get(i).getStatus();
            strOut += " weight:" + appMathList.get(i).getWeight();
            strOut += " algov:" + appMathList.get(i).getAlgov() + "]";
        }


        return strOut;
    }
}
