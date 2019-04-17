package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate;

import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ContentValueForQuestion implements Serializable {
    private static final long serialVersionUID = 5872173793810830821L;
    private String qId;
    private List<StudentPersonalInfo> studentContentInfo;
    private double totalScore;
    private int size;
    private List<Map<String, Object>> sentences;
    private List<Map<String, Object>> errorStudentInformation;
    private List<Map<String, Object>> rightStudentInformation;
    private AppOralScoreLevel appOralScoreLevel;
    private int rate;

}
