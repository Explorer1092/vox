package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ActivitySchoolRecordView {
    private Long schoolId;
    private String schoolName;

    private Integer joinUserCount;               // 参与活动人数
//
//    private List<ActivitySchoolIndicatorData> dataList = new ArrayList<>();
//
//    @Data
//    public static class ActivitySchoolIndicatorData {
//        private String indicatorName;
//        private Object indicatorValue;
//    }
}
