package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PsrPrimaryAppEnUserEks implements Serializable {

    private static final long serialVersionUID = -4782237810045566188L;

    private String errorContent;
    private List<PsrPrimaryAppEnUserEkItem> ekList;

    public PsrPrimaryAppEnUserEks() {
        ekList = new ArrayList<>();
    }

    public String formatList() {
        String strOut = "[PrimaryAppEnUserEks:return code:" + errorContent + "]";

        Integer count = 0;

        if (ekList != null) count = ekList.size();

        strOut += "[retcount:" + count.toString() + "]";

        for (int i = 0; ekList != null && i < ekList.size(); i++) {
            // eid,eType,weight,
            strOut += " [ek:" + ekList.get(i).getEk();
            strOut += " status:" + ekList.get(i).getStatus() + "]";
        }

        return strOut;
    }
}
