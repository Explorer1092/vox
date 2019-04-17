package com.voxlearning.utopia.service.afenti.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Ruib
 * @since 2016/8/15
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AfentiBox {
    GOLDEN(20),
    SILVER(10),
    BRONZE(5);

    @Getter private final int count;

    public static AfentiBox safeParse(String name) {
        try {
            return AfentiBox.valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
