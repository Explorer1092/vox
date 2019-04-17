package com.voxlearning.washington.mapper.activity;

import com.voxlearning.alps.annotation.common.FieldValueSerializer;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeacherActivityMapper {

    @FieldValueSerializer(serializer = "com.voxlearning.alps.lang.mapper.json.StringDateSerializer")
    private Date startTime;
    @FieldValueSerializer(serializer = "com.voxlearning.alps.lang.mapper.json.StringDateSerializer")
    private Date endTime;

    List<ActivityDetail> details;

    @Data
    public static class ActivityDetail {
        private String activityId;
        private String title;
        private String type;
        private List<ClazzDetail> clazzDetails;
    }

    @Data
    public static class ClazzDetail {
        private Long clazzId;
        private String clazzName;
        //private Integer studentCount;
        private Long participantsCount;
    }
}
