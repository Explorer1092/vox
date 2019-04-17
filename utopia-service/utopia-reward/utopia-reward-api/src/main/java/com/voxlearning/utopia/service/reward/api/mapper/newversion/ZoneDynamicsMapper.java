package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class ZoneDynamicsMapper implements Serializable {

    private String year;
    private List<ZoneDynamicsMapper.LeaveWordDay> yearDataList;

    @Getter
    @Setter
    public static class  LeaveWordDay implements Serializable {
        private String date;
        List<ZoneDynamicsMapper. LeaveWordDay.LeaveWordEntity> dayDataList;

        @Getter
        @Setter
        public static class LeaveWordEntity implements Serializable {
            private long visitorUserId;
            private String visitorUserName;
            private String visitorPortraitUrl;
            private long leaveWordId;
            private String leaveWordPortraitUrl;
            private String leaveWordName;
            private Date createTime;
            private String hourTime;
        }
    }

}
