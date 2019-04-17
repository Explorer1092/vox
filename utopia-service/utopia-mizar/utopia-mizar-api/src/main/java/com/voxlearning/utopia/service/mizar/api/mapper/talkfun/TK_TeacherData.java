package com.voxlearning.utopia.service.mizar.api.mapper.talkfun;

import lombok.Data;

import java.io.Serializable;

/**
 * 欢拓新增/修改主播返回参数
 *
 * @author yuechen.wang 2017/01/10
 */
@Data
public class TK_TeacherData implements Serializable {

    private static final Long serialVersionUID = 1559355259683496377L;

    @TkField("nickname") private String nickname;             // 主播昵称
    @TkField("intro") private String intro;                   // 简介
    @TkField("partner_id") private Integer partnerId;         // 合作方id
    @TkField("thirdAccount") private String thirdAccount;     // 第三方绑定账号
    @TkField("bid") private String bid;                       // 主播id
    @TkField("p_150") private String avatar;                  // 头像: 150x150
    @TkField("p_40") private String avatarThumb;              // 头像: 40x40
    @TkField(value = "createTime", timestamp = true) private String createTime;      // 创建的时间戳
    @TkField(value = "expireTime", timestamp = true) private String expireTime;      // 过期时间
}
