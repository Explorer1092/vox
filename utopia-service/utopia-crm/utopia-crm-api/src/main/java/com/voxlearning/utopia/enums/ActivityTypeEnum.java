package com.voxlearning.utopia.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ActivityTypeEnum {

    TANGRAM("七巧板", "/view/mobile/student/wonderland/openapp?url=/resources/apps/hwh5/jigsawpuzzle/V1_0_0/index.html?code=", "https://cdn-cnc.17zuoye.cn/resources/mobile/student/images/jigsawpuzzle_add.jpg"),
    TWENTY_FOUR("二十四点", "/view/mobile/student/wonderland/openapp?url=/resources/apps/hwh5/twentyfourpoints/V1_0_0/index.html?code=", "https://cdn-cnc.17zuoye.cn/resources/mobile/student/images/twentyfourpoints_add.jpg"),
    SUDOKU("数独", "/view/mobile/student/wonderland/openapp?url=/resources/apps/hwh5/sudoku/V1_0_0/index.html?activityId=", "https://cdn-cnc.17zuoye.cn/resources/mobile/student/images/sudoku_add.jpg");

    ActivityTypeEnum(String name, String url, String image) {
        this.name = name;
        this.url = url;
        this.image = image;
    }

    private String name;

    private String url;

    private String image;

    public static ActivityTypeEnum getType(String type) {
        for (ActivityTypeEnum activityTypeEnum : ActivityTypeEnum.values()) {
            if (Objects.equals(type, activityTypeEnum.name())) {
                return activityTypeEnum;
            }
        }
        return null;
    }
}
