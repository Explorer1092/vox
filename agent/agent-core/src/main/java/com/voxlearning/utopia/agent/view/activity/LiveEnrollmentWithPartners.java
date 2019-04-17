package com.voxlearning.utopia.agent.view.activity;

import com.voxlearning.utopia.agent.persist.entity.activity.LiveEnrollment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/12/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LiveEnrollmentWithPartners extends LiveEnrollment {


    private List<LiveEnrollmentPartner> partnerList = new ArrayList<>();

    @Data
    public static class LiveEnrollmentPartner{
        private String id;
        private Long partnerId;
        private String partnerName;
        private Date workTime;
    }
}
