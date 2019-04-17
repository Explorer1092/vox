package com.voxlearning.utopia.service.psr.entity;

//UserExamQuestionResultInfo

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
class Rate {
    int allCount;    // 该知识点 做题的数量
    int rightCount;  // 该知识点 做对题的数量
}

public class UserExamQuestionResultInfo implements Serializable {
    private static final long serialVersionUID = 1179004182041853632L;

    private Map<String/*doc_id不带版本号*/, Boolean> eidsInfo;
    private Map<String, Rate> eksInfo;

    public Rate getEksRate(String ek) {
        if (ek == null || eksInfo == null || eksInfo.size() <= 0 || !eksInfo.containsKey(ek))
            return null;

        return eksInfo.get(ek);
    }

    public UserExamQuestionResultInfo() {
        eidsInfo = new LinkedHashMap<>();
        eksInfo = new LinkedHashMap<>();
    }

    public int getEidAllCount(String ek) {
        if (ek == null || eksInfo == null || eksInfo.size() <= 0 || !eksInfo.containsKey(ek))
            return 0;

        return eksInfo.get(ek).allCount;
    }

    public double rightRate(String ek) {
        double rate = 0.0;

        if (ek == null || eksInfo == null || eksInfo.size() <= 0 || !eksInfo.containsKey(ek))
            return rate;
        Rate tmpRate = eksInfo.get(ek);
        if (tmpRate == null || tmpRate.allCount == 0)
            return rate;

        rate = tmpRate.rightCount / tmpRate.allCount;

        return (rate);
    }

    public Boolean isEidMaster(String eid) {
        if (eid == null || eidsInfo == null || eidsInfo.size() <= 0 || StringUtils.isEmpty(eid))
            return false;

        // 使用DocId比对(不带版本号的题Id)
        String docId = eid;
        if (eid.contains("-"))
            docId = eid.substring(0, eid.indexOf("-"));
        else if (eid.contains("."))
            docId = eid.substring(0, eid.indexOf("."));

        if (!eidsInfo.containsKey(docId))
            return false;
        return (eidsInfo.get(docId));
    }

    // 初始化当天做题信息以及知识点信息
    public void setEksEidsInfo(Map<String/*ek*/, Boolean> eksInfo, Map<String/*eid*/, Boolean> eidsInfo) {
        if (eidsInfo != null && eidsInfo.size() > 0)
            this.eidsInfo.putAll(eidsInfo);

        if (eksInfo != null && eksInfo.size() > 0) {
            for (Map.Entry<String, Boolean> entry : eksInfo.entrySet())
                insertEks(entry.getKey(), entry.getValue());
        }
    }

    private void insertEks(String ek, boolean master) {
        if (StringUtils.isEmpty(ek))
            return;
        Rate rate = null;
        if (!eksInfo.containsKey(ek)) {
            rate = new Rate();
        } else {
            rate = eksInfo.get(ek);
        }
        rate.allCount++;
        if (master)
            rate.rightCount++;
        eksInfo.put(ek, rate);
    }

}

