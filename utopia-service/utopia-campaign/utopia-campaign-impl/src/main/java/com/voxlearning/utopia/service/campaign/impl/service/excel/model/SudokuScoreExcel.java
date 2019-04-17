package com.voxlearning.utopia.service.campaign.impl.service.excel.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SudokuScoreExcel extends BaseExportScoreExcel {

    private String id;
    private String title;
    private String curDate;
    private String studentId;
    private String studentName;
    private String clazzId;
    private String clazzName;
    private String clazzLevel;
    private String schoolId;
    private String schoolName;
    private String areaName;
    private String cityName;
    private String provinceName;
    private String patternName;
    private String limitAmount;
    private String limitTime;
    private String finshCount;
    private String finshTime;
    private String endTime;

}
