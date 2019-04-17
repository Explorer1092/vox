package com.voxlearning.utopia.service.afenti.api.constant;

/**
 * @author peng.zhang.a
 * @since 16-7-27
 */
public enum AfentiRankType {
    school, national;

    public static AfentiRankType safeParse(String name) {
        try {
            return AfentiRankType.valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
