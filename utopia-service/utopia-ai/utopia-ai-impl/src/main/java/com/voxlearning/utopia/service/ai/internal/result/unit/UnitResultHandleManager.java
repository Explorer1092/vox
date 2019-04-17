package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;

import javax.inject.Named;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Named
public final class UnitResultHandleManager {

    private final static Map<ChipsUnitType, UnitResultHandler> MAP = new ConcurrentHashMap<>();

    protected void register(ChipsUnitType type, UnitResultHandler handler) {
        MAP.put(type, handler);
    }

    public UnitResultHandler get(ChipsUnitType type) {
        return MAP.get(type);
    }
}
