package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PsrPrimaryAppEnMatchContent implements Serializable {

    private static final long serialVersionUID = -8714766330895260551L;

    private String errorContent;
    private List<PsrPrimaryAppEnMatchItem> appEnMatchList;

    public PsrPrimaryAppEnMatchContent() {
        appEnMatchList = new ArrayList<>();
    }

    public boolean isSuccess() {
        return (errorContent.equals("success"));
    }

    public String formatList() {
        String strOut = "[PsrPrimaryAppEnMatch:return code:" + errorContent + "]";

        Integer count = 0;

        if (appEnMatchList != null) count = appEnMatchList.size();

        strOut += "[retcount:" + count.toString() + "]";

        for (int i = 0; appEnMatchList != null && i < appEnMatchList.size(); i++) {
            // ek,eid,et,weight,alogV,matchList
            strOut += "[eid:" + appEnMatchList.get(i).getEid();
            strOut += " eType:" + appEnMatchList.get(i).getEType();
            strOut += " status:" + appEnMatchList.get(i).getStatus();
            strOut += " weight:" + appEnMatchList.get(i).getWeight();
            strOut += " algov:" + appEnMatchList.get(i).getAlgov();
            strOut += " matchCount:" + appEnMatchList.get(i).getMatchEidsMap().size();
            strOut += " matchEids:{";
            for(Map.Entry<String,String> entry : appEnMatchList.get(i).getMatchEidsMap().entrySet()) {
                strOut += "\"" + entry.getKey() + "\"";
                strOut += ":\"" + entry.getValue() + "\",";
            }
            strOut += "}";
            strOut += "]";
        }

        return strOut;
    }
}
