package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PsrPrimaryAppUserEks implements Serializable {

    private static final long serialVersionUID = -9094059527402621550L;

    private String errorContent;
    private List<PsrPrimaryAppUserEkItem> ekList;

    public PsrPrimaryAppUserEks() {
        ekList = new ArrayList<>();
    }

    /*
     * type = [Math,En]
     */
    public String formatList(String type) {
        String strOut = "[PrimaryAppUserEks:" + type + ":return code:" + errorContent + "]";

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

