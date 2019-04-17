package com.voxlearning.utopia.agent.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 合作伙伴类别
 * @author: kaibo.he
 * @create: 2019-04-01 21:12
 **/
@AllArgsConstructor
@Getter
public enum AgentPartnerType {
    BOOK_STORE("书店"),
    STATIONERY_STORE("文具店"),
    PRINT_SHOP("打印店"),
    TRUSTEESHIP("托管机构"),
    CATERING_ORGANIZATION("餐饮机构"),
    CONVENIENCE_STORE("便利店"),
    MANICURE_SHOP("美甲店"),
    YOGA_SHOP("瑜伽店"),
    KINDERGARTEN("幼儿园"),
    EARLY_LEARNING_CENTRE("早教中心"),
    ART_TRAINING("美术培训"),
    DANCE_TRAINING("舞蹈培训"),
    ENGLISH_TRAINING("英语培训"),
    OTHER("其他");

    private final String desc;
}
