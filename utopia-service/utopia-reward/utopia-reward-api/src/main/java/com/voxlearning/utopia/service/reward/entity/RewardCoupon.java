package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 奖品中心，兑换券
 * Created by haitian.gan on 2017/7/19.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_COUPON")
@UtopiaCacheRevision("20170721")
public class RewardCoupon extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -3989909375762851139L;

    @UtopiaSqlColumn private Long productId;
    @UtopiaSqlColumn private String name;
    @UtopiaSqlColumn private Boolean sendSms;
    @UtopiaSqlColumn private Boolean sendMsg;
    @UtopiaSqlColumn private String smsTpl;
    @UtopiaSqlColumn private String msgTpl;
    @UtopiaSqlColumn private String integralComment;

}
