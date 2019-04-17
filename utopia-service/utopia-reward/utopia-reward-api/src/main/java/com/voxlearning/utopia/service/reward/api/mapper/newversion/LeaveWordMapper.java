package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class LeaveWordMapper implements Serializable {
    private Long visitorUserId;
    private String visitorUserName;
    private String visitorPortraitUrl;
    private Long leaveWordId;
    private String leaveWordPortraitUrl;
    private String leaveWordName;
    private Date createTime;
}
