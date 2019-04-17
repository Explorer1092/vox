package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Administrator on 2016/4/12.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrmInviteClueListData {
    private String id;//数据记录的键
    private String inviterName; //邀请人姓名
    private String inviteename; //被邀请者名称

    private String progress;//被邀请人的使用情况（登录，认证等）
    private Integer inviterVisitStatus;//邀请人的回访状态 （0：默认值， 1：已回访  2：电话未接通）
    private Integer inviteeVisitStatus;//被邀请人的跟进状态 （0：默认值， 1：已回访  2：电话未接通）

}
