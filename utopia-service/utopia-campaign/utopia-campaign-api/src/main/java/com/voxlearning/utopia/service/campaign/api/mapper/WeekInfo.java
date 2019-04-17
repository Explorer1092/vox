package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Data;

import java.util.List;

@Data
public class WeekInfo implements java.io.Serializable {

    private Integer week;
    private List<Plan> plans;

    @Data
    public static class Plan implements java.io.Serializable {
        private String name;
        private String icon;
        private String startTime;
        private String endTime;
    }
}
