package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PsrPrimaryAppEnContent implements Serializable {

    private static final long serialVersionUID = -7865639410702415834L;

    private String errorContent;
    private List<PsrPrimaryAppEnItem> appEnList;

    public PsrPrimaryAppEnContent() {
        appEnList = new ArrayList<>();
    }

    public String formatList() {
        String strOut = "[PrimaryAppEn:return code:" + errorContent + "]";

        Integer count = 0;

        if (appEnList != null) count = appEnList.size();

        strOut += "[retcount:" + count.toString() + "]";

        for (int i = 0; appEnList != null && i < appEnList.size(); i++) {
            // eid,eType,weight,
            strOut += "[eid:" + appEnList.get(i).getEid();
            strOut += " eType:" + appEnList.get(i).getEType();
            strOut += " status:" + appEnList.get(i).getStatus();
            strOut += " weight:" + appEnList.get(i).getWeight();
            strOut += " algov:" + appEnList.get(i).getAlgov() + "]";
        }

        return strOut;
    }
}
