package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class EkEidListContent implements Serializable {

    private static final long serialVersionUID = 0L;

    /** 本次推题汇总，排重使用，(防止不同的知识点 推同一道题) */
    private List<EkToEidContent> ekList;

    public EkEidListContent() {
        ekList = new ArrayList<>();
    }

    public boolean isEkListNull() {
        return (ekList == null);
    }

    public void addItemByEk(String ek, EidItem eidItem) {
        if (StringUtils.isEmpty(ek) || eidItem == null) return;

        if (isEkListNull()) ekList = new ArrayList<EkToEidContent>();
//        if (eidList == null)
//            eidList = new ArrayList<>();

        boolean isAddByEk = false;// 是否找到对应的ek 并插入
        for (int i = 0; i < ekList.size(); i++) {
            EkToEidContent ekToEidContent = ekList.get(i);

            if (ekToEidContent == null || ekToEidContent.getEk().compareTo(ek) != 0) continue;

            isAddByEk = true;
            if (!ekList.get(i).isEidListNull()) ekList.get(i).getEidList().add(eidItem);
        }


        if (ekList.size() <= 0 || !isAddByEk) {
            // 没找到对应的ek，则创建新的

            EkToEidContent ekToEidContent = new EkToEidContent();
            ekToEidContent.setEk(ek);
            List<EidItem> list = new ArrayList<>();
            list.add(eidItem);
            ekToEidContent.setEidList(list);
            ekList.add(ekToEidContent);
        }


//        eidList.add(eidItem.getEid());
    }

    public List<String> getEids() {
        List<String> list = new ArrayList<>();

        if (isEkListNull()) return list;

        for (EkToEidContent ekToEidContent : ekList) {
            if (ekToEidContent.isEidListNull()) continue;
            for (EidItem eidItem : ekToEidContent.getEidList()) {
                list.add(eidItem.getEid());
            }
        }

        return list;
    }
}
