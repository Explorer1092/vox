package com.voxlearning.utopia.service.reward.entity;

import lombok.Getter;

import java.util.*;

public enum DebrisType {

    UNKNOWN(-1, "UNKNOWN", DebrisCategory.MISC),
    TOBY(1, "托比", DebrisCategory.MISC);

    private static final Map<Integer, DebrisType> integralTypes;
    private static final Map<String, List<Integer>> categories;

    static {
        // 保证没有重复的TYPE
        Set<Integer> TYPES = new HashSet<>();
        for (DebrisType debrisType : values()) {
            if (TYPES.contains(debrisType.type)) {
                throw new IllegalStateException("艹，认真点！" + debrisType.type + "重了！合并完了好好检查下，认真修！");
            }
            TYPES.add(debrisType.type);
        }

        integralTypes = new LinkedHashMap<>();
        categories = new LinkedHashMap<>();
        for (DebrisType debrisType : values()) {
            integralTypes.put(debrisType.getType(), debrisType);
            if (categories.containsKey(debrisType.getCategory().name())) {
                categories.get(debrisType.getCategory().name()).add(debrisType.getType());
            } else {
                List<Integer> list = new ArrayList<>();
                list.add(debrisType.getType());
                categories.put(debrisType.getCategory().name(), list);
            }
        }
    }

    @Getter
    private final int type;
    @Getter
    private final String description;
    @Getter
    private final DebrisCategory category;

    DebrisType(int type, String description, DebrisCategory category) {
        this.type = type;
        this.description = description == null ? "" : description.trim();
        this.category = category;
    }

    public static DebrisType of(Integer type) {
        DebrisType debrisType = integralTypes.get(type);
        return debrisType == null ? UNKNOWN : debrisType;
    }
}
