package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Created by song.wang on 2016/4/19.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClazzInfoData {
    private String schoolName;
    private StudentStatisticsData statisticsData;
    private List<GroupTeacherData> groupTeacherList;
}
