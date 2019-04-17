package com.voxlearning.utopia.service.campaign.api.mapper.dp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class StudentPlanningWeekInfo implements Serializable {

    private int weekDay;
    private List<PlansBean> plans;

    @Data
    @NoArgsConstructor
    public static class PlansBean implements Serializable {
        private String timeQuantum;
        private List<Plan> list;

        @Data
        public static class Plan implements java.io.Serializable {
            private String name;
            private String icon;
            private String startTime;
            private String endTime;
        }
    }
}
