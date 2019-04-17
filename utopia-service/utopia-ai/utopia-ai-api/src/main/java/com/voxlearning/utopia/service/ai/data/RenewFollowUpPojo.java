package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guangqing
 * @since 2019/2/22
 */
@Getter
@Setter
public class RenewFollowUpPojo implements Serializable {
    private static final long serialVersionUID = -2086547580255297692L;
    private String giftPackIntroduce;//大礼包介绍
    private String giftPackImage;//大礼包介绍图片
    private String couponIntroduce;//优惠券介绍
    private String couponImage;//优惠券介绍图片
    private String groupIntroduce;//拼团介绍
    private String groupImage;//拼团介绍图片
    private String urgencyIntroduct;//紧迫性介绍
    private String buyLink;//购买链接
}
