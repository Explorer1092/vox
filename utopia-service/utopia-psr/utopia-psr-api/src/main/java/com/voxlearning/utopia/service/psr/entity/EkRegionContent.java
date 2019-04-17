package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.random.RandomUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class EkRegionContent implements Serializable {

    private static final long serialVersionUID = 6769397798938577308L;

    // 知识点
    private String ek;

    /*
     * key 是 regionCode : 地区代码
     * value 是 该题型在该地区下的信息
     */
    private Map<Integer, EkRegionItem> ekRegionsInfo = new HashMap<>();

    public EkRegionItem getEkRegionItemByRegion(Integer region) {
        if (region == null)
            return null;

        if (ekRegionsInfo.containsKey(region))
            return ekRegionsInfo.get(region);

        return null;
    }

    // 随机获取一个,补充逻辑
    public EkRegionItem getEkRegionItemRandom() {
        if (ekRegionsInfo.size() <= 0)
            return null;

        Integer randomIndex = RandomUtils.nextInt(ekRegionsInfo.size());

        Integer index = 1;
        for (Map.Entry<Integer, EkRegionItem> entry : ekRegionsInfo.entrySet()) {
            if (randomIndex.equals(index++))
                return entry.getValue();
        }

        return null;
    }
}

