package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by song.wang on 2016/4/25.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatisticsAndTeacherData {
    private TeacherSnapshot teacherData;
    private StudentStatisticsData statisticsData;

}
