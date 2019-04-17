package com.voxlearning.utopia.service.campaign.impl.service.excel.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwentyFourScoreExcel extends BaseExportScoreExcel {

    private String date;
    private String schoolId;
    private String schoolName;
    private String clazzLevel;
    private String clazzId;
    private String clazzName;
    private String studentId;
    private String studentName;
    private String score;
    private String resetCount;
    private String skipCount;

}
