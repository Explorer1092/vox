package com.voxlearning.utopia.agent.bean.honeycomb;

import lombok.Data;

@Data
public class HoneycombUserData {
    private Long honeycombId;             // 蜂巢用户ID
    private String nickName;              // 昵称
    private String mobile;                // 手机号  123****9999 形式
    private String headPortrait;          // 头像
}
