package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

public enum JztReportType {
    FinishAndUnChecked(1),
    FinishAndChecked(2);
    @Getter
    private final int key;

    JztReportType(int key) {
        this.key = key;
    }

    public static JztReportType of(int key) {
        for (JztReportType t : values()) {
            if (t.getKey() == key) {
                return t;
            }
        }
        return null;
    }
}
