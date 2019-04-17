package com.voxlearning.utopia.service.afenti.api.context;

import lombok.Getter;

/**
 * @author peng.zhang.a
 * @since 16-8-15
 */
@Deprecated
public enum GiftType {
    USING_DAYS_15(15, 100),
    USING_DAYS_30(30, 150),
    USING_DAYS_60(60, 300),
    USING_DAYS_90(90, 500),
    USING_DAYS_180(180, 700),
    USING_DAYS_360(360, 1000);

    @Getter private int cumulativeNum;
    @Getter private int beansNum;

    GiftType(Integer cumulativeNum, Integer beansNum) {
        this.cumulativeNum = cumulativeNum;
        this.beansNum = beansNum;
    }
    
    public static GiftType safeParse(String giftType) {
        try {
            return GiftType.valueOf(giftType);
        } catch (Exception e) {
            return null;
        }
    }
}
