package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class WarmHeartPlanMapper implements Serializable {

    private static final long serialVersionUID = 3244122384955586166L;

    private List<Plan> plans;

    @Getter
    @Setter
    public static class Plan implements Serializable {

        private static final long serialVersionUID = -5227577927509178090L;

        private String id;
        private String name;
        private Date beginDate;
        private Date endDate;
    }

}
