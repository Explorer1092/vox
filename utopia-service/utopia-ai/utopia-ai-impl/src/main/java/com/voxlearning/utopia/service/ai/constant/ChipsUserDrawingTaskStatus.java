package com.voxlearning.utopia.service.ai.constant;

public enum ChipsUserDrawingTaskStatus {
    unaccessible, underway, finished;
    public static ChipsUserDrawingTaskStatus safe(String name) {
        try {
            return ChipsUserDrawingTaskStatus.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
