package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class UserAppMathContent implements Serializable {

    private static final long serialVersionUID = -7616174814811888321L;

    private Long userId;
    private Map<String, UserAppMathEkItem> ekMap;
    private Map<String, UserAppMathEtItem> etMap;

    public boolean isEkMapNull() {
        return (ekMap == null);
    }

    public boolean isEtMapNull() {
        return (ekMap == null);
    }

    public List<String> getEks() {

        if (isEkMapNull()) return null;

        List<String> list = new ArrayList<>();
        for (Map.Entry<String, UserAppMathEkItem> entry : ekMap.entrySet()) {
            list.add(entry.getKey());
        }

        return list;
    }

    public String formatToString() {
        String strOut = "";
        strOut += userId.toString();
        strOut += "\t";

        for (Map.Entry<String, UserAppMathEkItem> entry : ekMap.entrySet()) {
            strOut += entry.getValue().getEk();
            strOut += ";" + entry.getValue().getStatus().toString();
            strOut += ";" + ((Integer) entry.getValue().getDays()).toString();
            strOut += ";" + ((Double) entry.getValue().getAccuracyRate()).toString() + ";";
        }

        strOut += "\t";

        for (Map.Entry<String, UserAppMathEtItem> entry : etMap.entrySet()) {
            strOut += entry.getValue().getEt();
            strOut += ";" + ((Double) entry.getValue().getAccuracyRate()).toString() + ";";
        }

        return strOut;
    }
}
