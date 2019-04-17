package com.voxlearning.utopia.service.campaign.api.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public enum WarmHeartTargetEnum {

    英文绘本(1, 1, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/study-icon01.png", "19:30", "21:00"),
    点读机(2, 1, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/study-icon02.png", "7:00", "8:00"),
    趣味配音(3, 1, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/study-icon04.png", "19:00", "20:00"),
    亲子讲故事(4, 1, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/study-icon06.png", "19:00", "21:00"),
    练习乐器(5, 1, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/study-icon07.png", "19:00", "20:00"),
    亲子下棋(6, 1, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/study-icon08.png", "19:00", "20:00"),

    亲子运动(7, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon01.png", "18:00", "20:00"),
    说声我爱你(8, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon02.png", "18:00", "21:00"),
    送孩子上学(9, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon03.png", "6:00", "8:00"),
    不玩手机(10, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon04.png", "18:00", "22:00"),
    亲子做家务(11, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon05.png", "18:00", "20:00"),
    陪孩子聊天(12, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon12.png", "18:00", "21:00"),
    //不抽烟(13, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon11.png", "18:00", "22:00"),
    表扬孩子(14, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon10.png", "18:00", "21:00"),
    亲子记账(15, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon06.png", "18:00", "21:00"),
    亲子讲笑话(16, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon07.png", "17:00", "20:30"),
    亲子打电话(17, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon08.png", "17:00", "21:00"),
    玩亲子游戏(18, 2, "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/warm_heart/life-icon09.png", "17:00", "20:30"),;
    private Integer id;
    private Integer typeId;
    private String icon;
    private String startTime;
    private String endTime;

    WarmHeartTargetEnum(Integer id, Integer typeId, String icon, String startTime, String endTime) {
        this.id = id;
        this.typeId = typeId;
        this.icon = icon;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Map<String, String> iconMap;

    static {
        iconMap = new HashMap<>();
        WarmHeartTargetEnum[] values = WarmHeartTargetEnum.values();
        for (WarmHeartTargetEnum value : values) {
            iconMap.put(value.name(), value.getIcon());
        }
    }

}
