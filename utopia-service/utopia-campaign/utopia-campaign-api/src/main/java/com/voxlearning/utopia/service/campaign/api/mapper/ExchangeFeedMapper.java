package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ExchangeFeedMapper implements java.io.Serializable {

    private Long teacherId;
    private String teacherName;
    private String schoolName;
    private String desc;
    private Date cTime;

    // 王老师 北京市海淀区实验小学 兑换成功
}
