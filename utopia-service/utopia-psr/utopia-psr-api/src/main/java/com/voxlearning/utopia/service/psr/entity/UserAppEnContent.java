package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ChaoLi Lee on 14-7-25.
 */
@Data
public class UserAppEnContent implements Serializable {

    private static final long serialVersionUID = -6061130103111800957L;

    private Long userId;
    private Integer ver;
    private Map<String, UserAppEnEkItem> ekMap;

    public boolean isEkMapNull() {
        return (ekMap == null);
    }

    public List<String> getEks() {

        if (isEkMapNull()) return null;

        List<String> list = new ArrayList<String>();
        for (Map.Entry<String, UserAppEnEkItem> entry : ekMap.entrySet()) {
            list.add(entry.getKey());
        }

        return list;
    }

    public String formatToString() {
        String strOut = "";
        strOut += userId.toString();
        strOut += "\t";

        for (Map.Entry<String, UserAppEnEkItem> entry : ekMap.entrySet()) {
            strOut += entry.getValue().getEk();
            strOut += ";" + entry.getValue().getStatus().toString();
            strOut += ";" + entry.getValue().getDays();
            strOut += ";" + entry.getValue().getAccuracyRate();
            strOut += ";" + entry.getValue().getTypeRight() + ";";
        }

        return strOut;
    }
}
