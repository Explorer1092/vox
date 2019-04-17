package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class NewTermClazzClockMapper implements Serializable {

    private static final long serialVersionUID = -4858444926972434935L;

    //累计打卡人数
    private Integer clockPeople;

    //单个目标累计打卡超过3天
    private Integer exceedThreeClockPeople;

    //单个目标累计打卡超过7天
    private Integer exceedSevenClockPeople;

    //单个目标累计打卡超过21天
    private Integer exceedTwentyOneClockPeople;

    private List<StudentUser> studentUsers;

    @Getter
    @Setter
    public static class StudentUser implements Serializable {

        private static final long serialVersionUID = -6606071504851118090L;

        private Long studentId;

        private String name;

    }
}
