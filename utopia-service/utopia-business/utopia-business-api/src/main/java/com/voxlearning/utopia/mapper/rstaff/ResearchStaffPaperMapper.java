package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Changyuan on 2015/1/7.
 */
@Data
public class ResearchStaffPaperMapper implements Serializable {

    private static final long serialVersionUID = 6974746476475667204L;

    private String examPaperId;
    private String examPaperName;
    private String authorName;

    private String state;   // 试卷状态
    private Date createTime;
    private String createTimeStr;

    private int authTeacherCntHasExamPaper;
    private int studentCntInAuthClazz;
    private int completeStudentCnt;
    private int questionNum;
}
