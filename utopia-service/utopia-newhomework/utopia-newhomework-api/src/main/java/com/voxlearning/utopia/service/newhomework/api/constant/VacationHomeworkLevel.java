package com.voxlearning.utopia.service.newhomework.api.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum VacationHomeworkLevel {

    LEVEL_1(1, 30),
    LEVEL_2(2, 50);

    @Getter private final int level;
    @Getter private final int studentIntegral;

    private static final Map<Integer, VacationHomeworkLevel> vacationHomeworkLevelMap = new HashMap<>();

    static {
        for (VacationHomeworkLevel vacationHomeworkLevel : values()) {
            vacationHomeworkLevelMap.put(vacationHomeworkLevel.getLevel(), vacationHomeworkLevel);
        }
    }

    public static VacationHomeworkLevel fromVacationHomeworkLevel(Integer level) {
        return vacationHomeworkLevelMap.get(level);
    }
}
