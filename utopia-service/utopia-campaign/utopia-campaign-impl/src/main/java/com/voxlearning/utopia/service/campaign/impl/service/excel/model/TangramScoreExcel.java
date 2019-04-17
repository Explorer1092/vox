package com.voxlearning.utopia.service.campaign.impl.service.excel.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TangramScoreExcel extends BaseExportScoreExcel {

    private String date;
    private String area;
    private String studentId;
    private String studentName;
    private String schoolId;
    private String schoolName;
    private String clazzId;
    private String clazzLevel;
    private String clazzName;
    private String score;

}