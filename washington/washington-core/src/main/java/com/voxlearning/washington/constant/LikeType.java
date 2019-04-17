package com.voxlearning.washington.constant;

import lombok.Getter;

@Getter
public enum LikeType {

    EMPTY("", "", 0l, 0l),
    EDUCATION_PIONEER("教育先锋", "感谢您给予了孩子如父母般的关爱！", 1L, 5L),
    WARM_AMBASSADOR("暖心大使", "您一定是家长和孩子最爱戴的老师！", 6L, 25L),
    MODEL_GARDENER("模范园丁", "暖宝宝再暖，也不如您暖心！", 26L, 40L),
    EDUCATION_STAR("教育之星", "点滴进步，离不开您辛勤培育！", 41L, 88L),
    SPICY_FRESH_TEACHER("麻辣鲜师", "感谢您为家校共育作出的卓越贡献！", 89L, Long.MAX_VALUE),;

    private String title;

    private String text;

    private Long start;

    private Long end;

    LikeType(String title, String text, Long start, Long end) {
        this.title = title;
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public static LikeType of(long likeCount) {

        if (likeCount == 0L) {
            return EMPTY;
        }

        for (LikeType likeType : LikeType.values()) {
            if (likeType.start <= likeCount && likeCount <= likeType.end) {
                return likeType;
            }
        }

        return null;
    }
}
