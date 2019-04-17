package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PsrPrimaryAppEnMatchEks implements Serializable {
    private static final long serialVersionUID = 1174523177135202404L;

    public PsrPrimaryAppEnMatchEks() {
        matchLessonIdEks = new HashMap<>();
    }

    public List<String> getMatchEksByLessonIds(List<String> lessonIds) {
        List<String> retList = new ArrayList<>();

        if (lessonIds == null || lessonIds.size() <= 0)
            return retList;

        for (String lesson : lessonIds) {
            if (matchLessonIdEks.containsKey(lesson)) {
                matchLessonIdEks.get(lesson).stream().filter(ek -> !retList.contains(ek)).forEach(retList::add);
            }
        }

        return retList;
    }

    // key=cat value=ver \t 80800550012273:bag:crayon:dog:duck \t 30870017:bag:duck:crayon:dog:pig

    public String encode() {

        String retStr = "1";     // version

        for (Map.Entry<String, List<String>> entry : matchLessonIdEks.entrySet()) {
            retStr += "\t" + entry.getKey();
            for (String ekTmp : entry.getValue()) {
                retStr += ":" + ekTmp;
            }
        }

        return retStr;
    }

    public void decodeAndSet(String strEks) {
        this.setMatchLessonIdEks(decode(strEks));
    }

    public Map<String, List<String>> decode(String strEks) {
        Map<String, List<String>> retMap = new HashMap<>();
        if (StringUtils.isEmpty(strEks))
            return retMap;

        String[] sArr = strEks.split("\t");
        if (sArr.length < 2)
            return retMap;

        String ver = sArr[0];

        for (int i=1; i<sArr.length; i++) {
            if (StringUtils.isEmpty(sArr[i]))
                continue;
            String[] tmpArr = sArr[i].split(":");
            if (tmpArr.length < 2)
                continue;
            List<String> matchEks = new ArrayList<>();
            for (int j=1; j<tmpArr.length; j ++)
                matchEks.add(tmpArr[j]);

            retMap.put(tmpArr[0], matchEks);
        }

        return retMap;
    }

    private String ek;  //
    private Map<String/*LessonId*/, List<String/*配错的eks*/>> matchLessonIdEks;  // 配错项列表 - eks - 按lesson 区分
}
