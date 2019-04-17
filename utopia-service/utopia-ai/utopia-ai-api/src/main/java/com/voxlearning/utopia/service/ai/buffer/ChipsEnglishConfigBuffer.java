package com.voxlearning.utopia.service.ai.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Summer on 2018/11/14
 */
public class ChipsEnglishConfigBuffer extends NearBuffer<List<ChipsEnglishPageContentConfig>> {

    private final Map<String, ChipsEnglishPageContentConfig> map = new HashMap<>();

    public Map<String, ChipsEnglishPageContentConfig> toMap() {
        return supplyWithReadLock((Supplier<Map<String, ChipsEnglishPageContentConfig>>) () -> new HashMap<>(map));
    }

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<ChipsEnglishPageContentConfig> data) {
        map.clear();
        data.forEach(p -> map.put(p.getName(), p));
    }
}
