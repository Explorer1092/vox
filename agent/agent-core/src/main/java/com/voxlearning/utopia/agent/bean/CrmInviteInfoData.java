package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by song.wang on 2016/4/13.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrmInviteInfoData {
    private String id;//邀请记录ID
    private Long inviteeId;//被邀请者ID
    private String headUrl;//头像地址
    private String inviteeName;//被邀请者名称
    private String inviteeSubject;//被邀请者所教的课程
    private String inviteeSchoolName;//被邀请者所在的学校
    private String inviteeMobile;//被邀请者电话
    private String inviteTime;

    private String inviteeVisitId;
    private Integer inviteeVisitStatus;//被邀请人的跟进状态 （0：默认值， 1：已回访  2：电话未接通）


    private Long inviterId;
    private String inviterName;//邀请者名称
    private String inviterMobile;//邀请者电话
    private String inviterSchoolName;//邀请者所在的学校
    private String inviterSubject;//邀请者的课程

    private String inviterVisitId;
    private Integer inviterVisitStatus;//邀请人的回访状态 （0：默认值， 1：已回访  2：电话未接通）

    public void setInviteeData(Long inviteeId, String headUrl, String inviteeName, String inviteeSubject, String inviteeSchoolName, String inviteeMobile, String inviteTime){
        this.inviteeId = inviteeId;
        this.headUrl = headUrl;
        this.inviteeName = inviteeName;
        this.inviteeSubject = inviteeSubject;
        this.inviteeSchoolName = inviteeSchoolName;
        this.inviteeMobile = inviteeMobile;
        this.inviteTime = inviteTime;
    }

    public void setInviterData(Long inviterId, String inviterName, String inviterMobile, String inviterSchoolName, String inviterSubject){
        this.inviterId = inviterId;
        this.inviterName = inviterName;
        this.inviterMobile = inviterMobile;
        this.inviterSchoolName = inviterSchoolName;
        this.inviterSubject = inviterSubject;
    }


}
