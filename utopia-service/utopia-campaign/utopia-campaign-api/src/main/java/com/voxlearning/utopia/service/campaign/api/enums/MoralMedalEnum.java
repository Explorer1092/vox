package com.voxlearning.utopia.service.campaign.api.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum MoralMedalEnum {

    团结协作(10, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_10.png"),
    尊师敬长(11, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_11.png"),
    保护环境(12, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_12.png"),
    有责任心(13, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_13.png"),
    诚实勇敢(14, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_14_v3.png"),
    积极自信(15, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_15.png"),
    自尊自律(16, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_16.png"),
    友爱宽容(17, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_17.png"),
    文明礼貌(18, 1, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_18_v2.png"),

    勤思好学(19, 2, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_19.png"),
    积极发言(20, 2, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_20.png"),
    遵守纪律(21, 2, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_21.png"),
    认真听讲(22, 2, "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/common/cross_images/deyu/medal_icon_22.png"),

    ;

    private Integer id;
    private Integer typeId;
    private String icon;

    MoralMedalEnum(int id, int typeId, String icon) {
        this.id = id;
        this.typeId = typeId;
        this.icon = icon;
    }

    public static MoralMedalEnum valueOfById(Integer id) {
        for (MoralMedalEnum itemEnum : values()) {
            if (Objects.equals(itemEnum.id, id)) {
                return itemEnum;
            }
        }
        return null;
    }

    public static String getIconById(Integer id) {
        for (MoralMedalEnum item : values()) {
            if (Objects.equals(item.id, id)) {
                return item.icon;
            }
        }
        return null;
    }

}
