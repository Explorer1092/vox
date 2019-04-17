package com.voxlearning.utopia.service.afenti.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Summer on 2018/4/3
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PicBookPurchaseGift {
    H_B("10006", "汉堡", "https://oss-image.17zuoye.com/wonderland/2017/10/11/20171011152324766783.png", "增加100点伙伴成长值"),
    B_J_L("10005", "冰激凌", "https://oss-image.17zuoye.com/wonderland/2017/07/28/20170728174104123059.png", "增加50点伙伴成长值"),
    J_J_K("50001", "竞技卡", "https://oss-image.17zuoye.com/wonderland/2017/09/27/20170927214301216134.png", ""),
    W_N_B_S("21001", "万能宝石", "https://oss-image.17zuoye.com/wonderland/2017/10/11/20171011111905622799.png", "可在所有伙伴进化时使用"),
    Z_X_J_F("99999", "自学积分", "https://oss-image.17zuoye.com/wonderland/reward/img/2017/07/20/20170720173210259463.png", "");


    @Getter private final String id;
    @Getter private final String cname;
    @Getter private final String img;
    @Getter private final String desc;
}
