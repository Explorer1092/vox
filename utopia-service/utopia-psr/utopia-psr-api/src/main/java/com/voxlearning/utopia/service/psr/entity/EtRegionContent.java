package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.random.RandomUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class EtRegionContent implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * 题型
     */
    private String et;
    /**
     * key 是 regionCode：地区代码
     * value 是 该题型在该地区下的信息
     */
    private Map<Integer, EtRegionItem> etRegionInfo = new HashMap<>();

    public EtRegionItem getEtRegionItemByRegion(Integer region) {
        if (region == null)
            return null;

        if (etRegionInfo.containsKey(region))
            return etRegionInfo.get(region);

        return null;
    }

    // 随机获取一个,补充逻辑
    public EtRegionItem getEtRegionItemRandom() {
        if (etRegionInfo.size() <= 0)
            return null;

        Integer randomIndex = RandomUtils.nextInt(etRegionInfo.size());

        Integer index = 1;
        for (Map.Entry<Integer, EtRegionItem> entry : etRegionInfo.entrySet()) {
            if (randomIndex.equals(index++))
                return entry.getValue();
        }

        return null;
    }

}
