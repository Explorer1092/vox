package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * testEid:status:ek:et;practiceEid:status:ek:et,practiceEid:status:ek:et
 */
@Data
public class ExamEnTestFiveEids implements Serializable {
    private static final long serialVersionUID = 2808549764600053406L;

    ExamEnTestFiveEidItem eidItem;
    private Map<String/*eid*/,ExamEnTestFiveEidItem/*status 是否推荐*/> practiceEidsMap;

    public ExamEnTestFiveEids() {
        eidItem = new ExamEnTestFiveEidItem();
        practiceEidsMap = new LinkedHashMap<>(); // 需要eid保持原来的顺序
    }

    public boolean decode(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return false;

        String[] sLineArr = strLine.split(";");

        if (sLineArr.length <= 0 || StringUtils.isEmpty(sLineArr[0]))
            return false;

        String[] sArrTestEid = sLineArr[0].split(":");
        if (sArrTestEid.length < 4)
            return false;

        this.eidItem.setEid(sArrTestEid[0]);
        this.eidItem.setEidStatus(Integer.valueOf(sArrTestEid[1]));
        this.eidItem.setEk(sArrTestEid[2]);
        this.eidItem.setEt(sArrTestEid[3]);

        if (sLineArr.length < 2)
            return true;

        String[] sArrPracticeEid = sLineArr[1].split(",");
        if (sArrPracticeEid.length <= 0)
            return false;

        for (String eidStatus : sArrPracticeEid) {
            if (StringUtils.isEmpty(eidStatus))
                continue;
            String[] sArrTmp = eidStatus.split(":");
            if (sArrTmp.length < 4)
                continue;

            ExamEnTestFiveEidItem tmpItem = new ExamEnTestFiveEidItem();
            tmpItem.setEid(sArrTmp[0]);
            tmpItem.setEidStatus(Integer.valueOf(sArrTmp[1]));
            tmpItem.setEk(sArrTmp[2]);
            tmpItem.setEt(sArrTmp[3]);

            practiceEidsMap.put(tmpItem.getEid(), tmpItem);
        }

        return true;
    }

    // testEid:*:*;testEid:*:*
    public String encode(boolean isHeader) {
        if (!isHeader)
            return encode();

        if (StringUtils.isEmpty(eidItem.getEid()))
            return null;

        return eidItem.getEid() + ":" + eidItem.getEidStatus() + ":" + eidItem.getEk() + ":" + eidItem.getEt() + ";";
    }

    // testEid:pracitceEid,practiceEid
    public String encode() {
        if (StringUtils.isEmpty(eidItem.getEid())
                || practiceEidsMap.size() <= 0)
            return null;

        String strRet = eidItem.getEid() + ":" + eidItem.getEidStatus() + ":" + eidItem.getEk() + ":" + eidItem.getEt() + ";";

        for (ExamEnTestFiveEidItem practice : practiceEidsMap.values()) {
            strRet += practice.getEid()
                    + ":" + practice.getEidStatus()
                    + ":" + practice.getEk()
                    + ":" + practice.getEt()
                    + ",";
        }

        return strRet;
    }
}


